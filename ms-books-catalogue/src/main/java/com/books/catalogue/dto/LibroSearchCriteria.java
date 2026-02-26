package com.books.catalogue.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibroSearchCriteria {
    private String titulo;
    private String autor;
    private String categoria;
    private String codigoIsbn;
    private BigDecimal precioMin;
    private BigDecimal precioMax;
    private Boolean visible;
    private LocalDate fechaPublicacionDesde;
    private LocalDate fechaPublicacionHasta;
    private Integer ratingMin;
    private Integer ratingMax;
}
