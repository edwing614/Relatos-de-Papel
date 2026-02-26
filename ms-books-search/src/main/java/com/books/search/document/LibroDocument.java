package com.books.search.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Document(indexName = "libros")
@Setting(settingPath = "elasticsearch/settings.json")
@Mapping(mappingPath = "elasticsearch/mappings.json")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibroDocument {

    @Id
    private Integer idLibro;

    private String codigo;
    private String codigoIsbn;
    private String titulo;
    private String descripcion;
    private BigDecimal precio;
    private Boolean visible;
    private LocalDate fechaPublicacion;
    private Integer rating;
    private Integer stockDisponible;
    private List<AutorNested> autores;
    private List<CategoriaNested> categorias;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AutorNested {
        private Integer idAutor;
        private String nombre;
        private String pais;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoriaNested {
        private Short idCategoria;
        private String nombre;
    }
}
