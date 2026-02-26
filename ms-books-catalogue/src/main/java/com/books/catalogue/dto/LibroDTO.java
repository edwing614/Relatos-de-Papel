package com.books.catalogue.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibroDTO {
    private Integer idLibro;
    private String codigo;
    private String codigoIsbn;
    private String titulo;
    private String descripcion;
    private BigDecimal precio;
    private Boolean visible;
    private LocalDate fechaPublicacion;
    private Integer rating;
    private List<AutorDTO> autores;
    private List<CategoriaDTO> categorias;
    private Integer stockDisponible;
}
