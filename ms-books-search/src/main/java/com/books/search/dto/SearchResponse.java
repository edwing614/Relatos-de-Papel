package com.books.search.dto;

import com.books.search.document.LibroDocument;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResponse {
    private List<LibroDocument> results;
    private long totalHits;
    private int page;
    private int size;
    private int totalPages;
    private Map<String, List<FacetBucket>> facets;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FacetBucket {
        private String key;
        private long count;
    }
}
