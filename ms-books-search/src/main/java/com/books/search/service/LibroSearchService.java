package com.books.search.service;

import com.books.search.document.LibroDocument;
import com.books.search.dto.*;

import java.util.List;

public interface LibroSearchService {

    SearchResponse search(SearchRequest request);

    SuggestResponse suggest(String query, int maxResults);

    DidYouMeanResponse didYouMean(String query, int maxResults);

    void indexAll(List<LibroDocument> libros);

    void index(LibroDocument libro);

    ReindexResponse reindexFromCatalogue();
}
