package com.books.search.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestResponse {
    private List<Suggestion> suggestions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Suggestion {
        private Integer idLibro;
        private String titulo;
        private String autores;
        private double score;
    }
}
