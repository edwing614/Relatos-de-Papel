package com.books.search.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DidYouMeanResponse {
    private String q;
    private List<String> suggestions;
}
