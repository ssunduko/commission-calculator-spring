package com.chapman.edu.commissions.ai.vectorstore;

import com.chapman.edu.commissions.ai.service.vectorstore.EmbeddingSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmbeddingSearchService — Unit Tests")
class EmbeddingSearchServiceTest {

    @Mock
    private VectorStore vectorStore;

    @InjectMocks
    private EmbeddingSearchService searchService;

    @Nested
    @DisplayName("search")
    class Search {

        @Test
        @DisplayName("should perform similarity search with correct parameters")
        void shouldPerformSimilaritySearch() {
            Document doc = new Document("Commission plan Standard", Map.of("type", "commission_plan"));
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));

            List<Document> results = searchService.search("commission plans", 5);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getText()).contains("Standard");

            ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
            verify(vectorStore).similaritySearch(captor.capture());
            SearchRequest captured = captor.getValue();
            assertThat(captured.getQuery()).isEqualTo("commission plans");
            assertThat(captured.getTopK()).isEqualTo(5);
            assertThat(captured.getSimilarityThreshold()).isEqualTo(0.5);
        }

        @Test
        @DisplayName("should return empty list when no matches found")
        void shouldReturnEmptyWhenNoMatches() {
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(Collections.emptyList());

            List<Document> results = searchService.search("nonexistent topic", 5);

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("searchByType")
    class SearchByType {

        @Test
        @DisplayName("should apply type filter to search request")
        void shouldApplyTypeFilter() {
            Document doc = new Document("Deal Acme Corp", Map.of("type", "deal"));
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));

            List<Document> results = searchService.searchByType("enterprise deals", "deal", 3);

            assertThat(results).hasSize(1);

            ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
            verify(vectorStore).similaritySearch(captor.capture());
            SearchRequest captured = captor.getValue();
            assertThat(captured.getQuery()).isEqualTo("enterprise deals");
            assertThat(captured.getTopK()).isEqualTo(3);
            assertThat(captured.getFilterExpression()).isNotNull();
        }
    }

    @Nested
    @DisplayName("searchByEntity")
    class SearchByEntity {

        @Test
        @DisplayName("should apply entity ID filter with lower threshold")
        void shouldApplyEntityFilter() {
            Document doc = new Document("Specific entity document", Map.of("entityId", "ent-001"));
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));

            List<Document> results = searchService.searchByEntity("details", "ent-001", 2);

            assertThat(results).hasSize(1);

            ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
            verify(vectorStore).similaritySearch(captor.capture());
            SearchRequest captured = captor.getValue();
            assertThat(captured.getSimilarityThreshold()).isEqualTo(0.3);
            assertThat(captured.getFilterExpression()).isNotNull();
        }
    }

    @Nested
    @DisplayName("extractContext")
    class ExtractContext {

        @Test
        @DisplayName("should return default message for null list")
        void shouldReturnDefaultForNull() {
            String context = searchService.extractContext(null);
            assertThat(context).isEqualTo("No relevant information found.");
        }

        @Test
        @DisplayName("should return default message for empty list")
        void shouldReturnDefaultForEmptyList() {
            String context = searchService.extractContext(Collections.emptyList());
            assertThat(context).isEqualTo("No relevant information found.");
        }

        @Test
        @DisplayName("should format documents as numbered results")
        void shouldFormatAsNumberedResults() {
            Document doc1 = new Document("First document content", Map.of("type", "deal"));
            Document doc2 = new Document("Second document content", Map.of("type", "plan"));

            String context = searchService.extractContext(List.of(doc1, doc2));

            assertThat(context)
                    .contains("[Result 1] First document content")
                    .contains("[Result 2] Second document content");
        }
    }
}
