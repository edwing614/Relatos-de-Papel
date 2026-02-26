package com.books.search.controller;

import com.books.search.dto.*;
import com.books.search.service.LibroSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "API de búsqueda con Elasticsearch")
public class SearchController {

    private final LibroSearchService searchService;

    @GetMapping
    @Operation(summary = "Búsqueda full-text con facets",
        description = "Busca libros por texto libre con filtros y devuelve facets para refinamiento. Soporta fuzziness para tolerancia a typos.")
    public ResponseEntity<SearchResponse> search(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) List<String> categorias,
            @RequestParam(required = false) List<String> autores,
            @RequestParam(required = false) Integer ratingMin,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        SearchRequest request = SearchRequest.builder()
            .query(q)
            .categorias(categorias)
            .autores(autores)
            .ratingMin(ratingMin)
            .precioMin(precioMin)
            .precioMax(precioMax)
            .page(page)
            .size(size)
            .build();

        return ResponseEntity.ok(searchService.search(request));
    }

    @GetMapping("/suggest")
    @Operation(summary = "Autocompletado / Typeahead",
        description = "Devuelve sugerencias de títulos usando search_as_you_type")
    public ResponseEntity<SuggestResponse> suggest(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int maxResults) {
        return ResponseEntity.ok(searchService.suggest(q, maxResults));
    }

    @GetMapping("/didyoumean")
    @Operation(summary = "Correcciones / Did-you-mean",
        description = "Devuelve sugerencias de títulos corregidos usando fuzzy matching. Útil cuando la búsqueda devuelve 0 resultados por typos.")
    public ResponseEntity<DidYouMeanResponse> didYouMean(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int maxResults) {
        return ResponseEntity.ok(searchService.didYouMean(q, maxResults));
    }

    @PostMapping("/reindex")
    @Operation(summary = "Reindexar desde catálogo",
        description = "Obtiene todos los libros de ms-books-catalogue y los indexa en Elasticsearch. Idempotente.")
    public ResponseEntity<ReindexResponse> reindex() {
        return ResponseEntity.ok(searchService.reindexFromCatalogue());
    }
}
