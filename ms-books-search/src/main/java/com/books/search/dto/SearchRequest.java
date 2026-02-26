package com.books.search.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRequest {
    private String query;
    private List<String> categorias;
    private List<String> autores;
    private Integer ratingMin;
    private BigDecimal precioMin;
    private BigDecimal precioMax;
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 10;
}
