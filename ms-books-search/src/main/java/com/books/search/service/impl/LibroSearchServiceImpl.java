package com.books.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.books.search.document.LibroDocument;
import com.books.search.dto.*;
import com.books.search.service.LibroSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibroSearchServiceImpl implements LibroSearchService {

    private final ElasticsearchClient esClient;
    private final WebClient.Builder webClientBuilder;

    private static final String INDEX = "libros";

    @Override
    public SearchResponse search(SearchRequest request) {
        try {
            var searchResponse = esClient.search(s -> {
                s.index(INDEX)
                    .from(request.getPage() * request.getSize())
                    .size(request.getSize());

                // Build main query
                List<Query> mustQueries = new ArrayList<>();
                List<Query> filterQueries = new ArrayList<>();

                // Always filter visible=true
                filterQueries.add(TermQuery.of(t -> t.field("visible").value(true))._toQuery());

                // Full-text query on titulo + descripcion + autores.nombre
                if (request.getQuery() != null && !request.getQuery().isBlank()) {
                    String q = request.getQuery().trim();
                    mustQueries.add(BoolQuery.of(bq -> bq
                        .should(
                            MultiMatchQuery.of(mm -> mm
                                .query(q)
                                .fields("titulo", "titulo._2gram", "titulo._3gram", "descripcion")
                                .type(TextQueryType.BoolPrefix)
                                .fuzziness("AUTO")
                            )._toQuery(),
                            NestedQuery.of(nq -> nq
                                .path("autores")
                                .query(MatchQuery.of(m -> m
                                    .field("autores.nombre")
                                    .query(q)
                                    .fuzziness("AUTO")
                                )._toQuery())
                            )._toQuery(),
                            NestedQuery.of(nq -> nq
                                .path("categorias")
                                .query(MatchQuery.of(m -> m
                                    .field("categorias.nombre.text")
                                    .query(q)
                                )._toQuery())
                            )._toQuery()
                        )
                        .minimumShouldMatch("1")
                    )._toQuery());
                }

                // Category filter
                if (request.getCategorias() != null && !request.getCategorias().isEmpty()) {
                    List<FieldValue> catValues = request.getCategorias().stream()
                        .map(FieldValue::of)
                        .collect(Collectors.toList());
                    filterQueries.add(NestedQuery.of(nq -> nq
                        .path("categorias")
                        .query(TermsQuery.of(tq -> tq
                            .field("categorias.nombre")
                            .terms(new TermsQueryField.Builder().value(catValues).build())
                        )._toQuery())
                    )._toQuery());
                }

                // Author filter
                if (request.getAutores() != null && !request.getAutores().isEmpty()) {
                    List<FieldValue> autValues = request.getAutores().stream()
                        .map(FieldValue::of)
                        .collect(Collectors.toList());
                    filterQueries.add(NestedQuery.of(nq -> nq
                        .path("autores")
                        .query(TermsQuery.of(tq -> tq
                            .field("autores.nombre.keyword")
                            .terms(new TermsQueryField.Builder().value(autValues).build())
                        )._toQuery())
                    )._toQuery());
                }

                // Rating filter
                if (request.getRatingMin() != null) {
                    filterQueries.add(RangeQuery.of(rq -> rq
                        .field("rating")
                        .gte(co.elastic.clients.json.JsonData.of(request.getRatingMin()))
                    )._toQuery());
                }

                // Price filter
                if (request.getPrecioMin() != null) {
                    filterQueries.add(RangeQuery.of(rq -> rq
                        .field("precio")
                        .gte(co.elastic.clients.json.JsonData.of(request.getPrecioMin().doubleValue()))
                    )._toQuery());
                }
                if (request.getPrecioMax() != null) {
                    filterQueries.add(RangeQuery.of(rq -> rq
                        .field("precio")
                        .lte(co.elastic.clients.json.JsonData.of(request.getPrecioMax().doubleValue()))
                    )._toQuery());
                }

                s.query(BoolQuery.of(bq -> bq
                    .must(mustQueries)
                    .filter(filterQueries)
                )._toQuery());

                // Aggregations for facets
                s.aggregations("categorias", Aggregation.of(a -> a
                    .nested(n -> n.path("categorias"))
                    .aggregations("nombres", Aggregation.of(a2 -> a2
                        .terms(t -> t.field("categorias.nombre").size(20))
                    ))
                ));

                s.aggregations("autores", Aggregation.of(a -> a
                    .nested(n -> n.path("autores"))
                    .aggregations("nombres", Aggregation.of(a2 -> a2
                        .terms(t -> t.field("autores.nombre.keyword").size(20))
                    ))
                ));

                s.aggregations("ratings", Aggregation.of(a -> a
                    .terms(t -> t.field("rating").size(5))
                ));

                return s;
            }, LibroDocument.class);

            List<LibroDocument> results = searchResponse.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            long totalHits = searchResponse.hits().total() != null
                ? searchResponse.hits().total().value() : 0;

            Map<String, List<SearchResponse.FacetBucket>> facets = extractFacets(searchResponse);

            return SearchResponse.builder()
                .results(results)
                .totalHits(totalHits)
                .page(request.getPage())
                .size(request.getSize())
                .totalPages((int) Math.ceil((double) totalHits / request.getSize()))
                .facets(facets)
                .build();

        } catch (IOException e) {
            log.error("Error searching Elasticsearch", e);
            throw new RuntimeException("Error en búsqueda", e);
        }
    }

    @Override
    public SuggestResponse suggest(String query, int maxResults) {
        try {
            var searchResponse = esClient.search(s -> s
                .index(INDEX)
                .size(maxResults)
                .query(BoolQuery.of(bq -> bq
                    .must(MultiMatchQuery.of(mm -> mm
                        .query(query)
                        .fields("titulo", "titulo._2gram", "titulo._3gram")
                        .type(TextQueryType.BoolPrefix)
                    )._toQuery())
                    .filter(TermQuery.of(t -> t.field("visible").value(true))._toQuery())
                )._toQuery())
                .source(sc -> sc.filter(f -> f.includes("idLibro", "titulo", "autores.nombre"))),
                LibroDocument.class
            );

            List<SuggestResponse.Suggestion> suggestions = searchResponse.hits().hits().stream()
                .filter(h -> h.source() != null)
                .map(hit -> {
                    LibroDocument doc = hit.source();
                    String autoresStr = "";
                    if (doc.getAutores() != null) {
                        autoresStr = doc.getAutores().stream()
                            .map(LibroDocument.AutorNested::getNombre)
                            .collect(Collectors.joining(", "));
                    }
                    return SuggestResponse.Suggestion.builder()
                        .idLibro(doc.getIdLibro())
                        .titulo(doc.getTitulo())
                        .autores(autoresStr)
                        .score(hit.score() != null ? hit.score() : 0)
                        .build();
                })
                .collect(Collectors.toList());

            return SuggestResponse.builder().suggestions(suggestions).build();

        } catch (IOException e) {
            log.error("Error getting suggestions", e);
            throw new RuntimeException("Error en sugerencias", e);
        }
    }

    @Override
    public DidYouMeanResponse didYouMean(String query, int maxResults) {
        try {
            // Use a fuzzy match on titulo.suggest and descripcion.suggest to find close matches
            var searchResponse = esClient.search(s -> s
                .index(INDEX)
                .size(maxResults)
                .query(BoolQuery.of(bq -> bq
                    .must(MultiMatchQuery.of(mm -> mm
                        .query(query)
                        .fields("titulo.suggest^3", "descripcion.suggest")
                        .fuzziness("AUTO")
                        .prefixLength(1)
                    )._toQuery())
                    .filter(TermQuery.of(t -> t.field("visible").value(true))._toQuery())
                )._toQuery())
                .source(sc -> sc.filter(f -> f.includes("titulo"))),
                LibroDocument.class
            );

            // Collect unique titles as "did you mean" suggestions
            List<String> suggestions = searchResponse.hits().hits().stream()
                .filter(h -> h.source() != null && h.source().getTitulo() != null)
                .map(h -> h.source().getTitulo())
                .distinct()
                .limit(maxResults)
                .collect(Collectors.toList());

            return DidYouMeanResponse.builder()
                .q(query)
                .suggestions(suggestions)
                .build();

        } catch (IOException e) {
            log.error("Error in didYouMean", e);
            throw new RuntimeException("Error en correcciones", e);
        }
    }

    @Override
    public void indexAll(List<LibroDocument> libros) {
        try {
            // Delete index if exists
            boolean indexExists = esClient.indices().exists(e -> e.index(INDEX)).value();
            if (indexExists) {
                esClient.indices().delete(d -> d.index(INDEX));
            }

            // Recreate index with custom settings and mappings
            try (var configStream = getClass().getResourceAsStream("/elasticsearch/index-config.json")) {
                esClient.indices().create(c -> c
                    .index(INDEX)
                    .withJson(configStream)
                );
            }

            log.info("Index '{}' created with custom mappings", INDEX);

            List<BulkOperation> operations = libros.stream()
                .map(libro -> BulkOperation.of(op -> op
                    .index(i -> i
                        .index(INDEX)
                        .id(String.valueOf(libro.getIdLibro()))
                        .document(libro)
                    )
                ))
                .collect(Collectors.toList());

            if (!operations.isEmpty()) {
                BulkResponse bulkResponse = esClient.bulk(b -> b
                    .index(INDEX)
                    .operations(operations)
                );

                if (bulkResponse.errors()) {
                    log.error("Bulk indexing had errors");
                    bulkResponse.items().stream()
                        .filter(item -> item.error() != null)
                        .forEach(item -> log.error("Error: {}", item.error().reason()));
                }
            }

            log.info("Indexed {} libros", libros.size());
        } catch (IOException e) {
            log.error("Error indexing libros", e);
            throw new RuntimeException("Error indexando libros", e);
        }
    }

    @Override
    public void index(LibroDocument libro) {
        try {
            esClient.index(i -> i
                .index(INDEX)
                .id(String.valueOf(libro.getIdLibro()))
                .document(libro)
            );
        } catch (IOException e) {
            log.error("Error indexing libro {}", libro.getIdLibro(), e);
            throw new RuntimeException("Error indexando libro", e);
        }
    }

    @Override
    public ReindexResponse reindexFromCatalogue() {
        log.info("Starting reindex from ms-books-catalogue...");
        long startMs = System.currentTimeMillis();

        Map<String, Object> response;
        try {
            response = webClientBuilder.build()
                .get()
                .uri("http://ms-books-catalogue/libros?size=1000")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
        } catch (WebClientResponseException e) {
            log.error("Catalogue responded with error: {}", e.getStatusCode(), e);
            throw new RuntimeException("ms-books-catalogue no disponible: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Cannot reach ms-books-catalogue", e);
            throw new RuntimeException("No se pudo conectar con ms-books-catalogue: " + e.getMessage());
        }

        if (response == null || !response.containsKey("content")) {
            log.warn("No content from catalogue");
            return ReindexResponse.builder()
                .message("Sin contenido del catálogo")
                .indexedCount(0)
                .tookMs(System.currentTimeMillis() - startMs)
                .indexName(INDEX)
                .timestamp(Instant.now().toString())
                .build();
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

        List<LibroDocument> libros = content.stream()
            .map(this::mapToDocument)
            .collect(Collectors.toList());

        indexAll(libros);

        long tookMs = System.currentTimeMillis() - startMs;
        return ReindexResponse.builder()
            .message("Reindexación completada")
            .indexedCount(libros.size())
            .tookMs(tookMs)
            .indexName(INDEX)
            .timestamp(Instant.now().toString())
            .build();
    }

    @SuppressWarnings("unchecked")
    private LibroDocument mapToDocument(Map<String, Object> map) {
        List<LibroDocument.AutorNested> autores = new ArrayList<>();
        if (map.get("autores") instanceof List) {
            for (Map<String, Object> a : (List<Map<String, Object>>) map.get("autores")) {
                autores.add(LibroDocument.AutorNested.builder()
                    .idAutor((Integer) a.get("idAutor"))
                    .nombre((String) a.get("nombre"))
                    .pais((String) a.get("pais"))
                    .build());
            }
        }

        List<LibroDocument.CategoriaNested> categorias = new ArrayList<>();
        if (map.get("categorias") instanceof List) {
            for (Map<String, Object> c : (List<Map<String, Object>>) map.get("categorias")) {
                categorias.add(LibroDocument.CategoriaNested.builder()
                    .idCategoria(c.get("idCategoria") != null ? ((Number) c.get("idCategoria")).shortValue() : null)
                    .nombre((String) c.get("nombre"))
                    .build());
            }
        }

        return LibroDocument.builder()
            .idLibro((Integer) map.get("idLibro"))
            .codigo((String) map.get("codigo"))
            .codigoIsbn((String) map.get("codigoIsbn"))
            .titulo((String) map.get("titulo"))
            .descripcion((String) map.get("descripcion"))
            .precio(map.get("precio") != null ? new java.math.BigDecimal(map.get("precio").toString()) : null)
            .visible((Boolean) map.get("visible"))
            .fechaPublicacion(map.get("fechaPublicacion") != null
                ? java.time.LocalDate.parse(map.get("fechaPublicacion").toString()) : null)
            .rating(map.get("rating") != null ? ((Number) map.get("rating")).intValue() : null)
            .stockDisponible(map.get("stockDisponible") != null ? ((Number) map.get("stockDisponible")).intValue() : null)
            .autores(autores)
            .categorias(categorias)
            .build();
    }

    private Map<String, List<SearchResponse.FacetBucket>> extractFacets(
            co.elastic.clients.elasticsearch.core.SearchResponse<LibroDocument> response) {

        Map<String, List<SearchResponse.FacetBucket>> facets = new HashMap<>();

        // Categories facet (nested)
        var catAgg = response.aggregations().get("categorias");
        if (catAgg != null && catAgg.isNested()) {
            var nombres = catAgg.nested().aggregations().get("nombres");
            if (nombres != null && nombres.isSterms()) {
                facets.put("categorias", nombres.sterms().buckets().array().stream()
                    .map(b -> SearchResponse.FacetBucket.builder()
                        .key(b.key().stringValue())
                        .count(b.docCount())
                        .build())
                    .collect(Collectors.toList()));
            }
        }

        // Authors facet (nested)
        var autAgg = response.aggregations().get("autores");
        if (autAgg != null && autAgg.isNested()) {
            var nombres = autAgg.nested().aggregations().get("nombres");
            if (nombres != null && nombres.isSterms()) {
                facets.put("autores", nombres.sterms().buckets().array().stream()
                    .map(b -> SearchResponse.FacetBucket.builder()
                        .key(b.key().stringValue())
                        .count(b.docCount())
                        .build())
                    .collect(Collectors.toList()));
            }
        }

        // Ratings facet
        var ratAgg = response.aggregations().get("ratings");
        if (ratAgg != null && ratAgg.isLterms()) {
            facets.put("ratings", ratAgg.lterms().buckets().array().stream()
                .map(b -> SearchResponse.FacetBucket.builder()
                    .key(String.valueOf(b.key()))
                    .count(b.docCount())
                    .build())
                .collect(Collectors.toList()));
        }

        return facets;
    }
}
