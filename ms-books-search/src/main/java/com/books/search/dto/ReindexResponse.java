package com.books.search.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReindexResponse {
    private String message;
    private long indexedCount;
    private long tookMs;
    private String indexName;
    private String timestamp;
}
