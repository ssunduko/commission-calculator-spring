package com.chapman.edu.commissions.ai.rag;

import com.chapman.edu.commissions.ai.service.rag.CommissionRagService;
import com.chapman.edu.commissions.ai.service.vectorstore.EmbeddingSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommissionRagService — Unit Tests")
class CommissionRagServiceTest {

    @Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @Mock
    private EmbeddingSearchService searchService;

    @InjectMocks
    private CommissionRagService ragService;

    private List<Document> sampleDocs;

    @BeforeEach
    void setUp() {
        Document doc1 = new Document("Commission plan 'Standard' with 5% starter tier.",
                Map.of("type", "commission_plan", "entityId", "plan-001"));
        Document doc2 = new Document("Deal 'Acme Corp' worth $150,000, status WON.",
                Map.of("type", "deal", "entityId", "deal-001"));
        sampleDocs = List.of(doc1, doc2);
    }

    @Nested
    @DisplayName("answerQuestion")
    class AnswerQuestion {

        @Test
        @DisplayName("should retrieve documents and generate grounded response")
        void shouldRetrieveDocsAndGenerateResponse() {
            when(searchService.search("What commission plans exist?", 5)).thenReturn(sampleDocs);
            when(searchService.extractContext(sampleDocs))
                    .thenReturn("[Result 1] Commission plan 'Standard' with 5% starter tier.\n" +
                            "[Result 2] Deal 'Acme Corp' worth $150,000, status WON.\n");
            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("The Standard commission plan is available with a 5% starter tier.");

            String result = ragService.answerQuestion("What commission plans exist?");

            assertThat(result).contains("Standard");
            verify(searchService).search("What commission plans exist?", 5);
        }

        @Test
        @DisplayName("should handle when no documents are found")
        void shouldHandleNoDocumentsFound() {
            when(searchService.search(anyString(), eq(5))).thenReturn(Collections.emptyList());
            when(searchService.extractContext(Collections.emptyList()))
                    .thenReturn("No relevant information found.");
            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("I don't have enough information to answer this question.");

            String result = ragService.answerQuestion("Tell me about non-existent plans");

            assertThat(result).contains("don't have enough information");
        }
    }

    @Nested
    @DisplayName("answerTypedQuestion")
    class AnswerTypedQuestion {

        @Test
        @DisplayName("should use filtered search by document type")
        void shouldUseFilteredSearch() {
            when(searchService.searchByType("What deals are open?", "deal", 5))
                    .thenReturn(sampleDocs);
            when(searchService.extractContext(sampleDocs))
                    .thenReturn("[Result 1] Deal 'Acme Corp' worth $150,000.");
            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("Acme Corp is an open deal worth $150,000.");

            String result = ragService.answerTypedQuestion("What deals are open?", "deal");

            assertThat(result).contains("Acme Corp");
            verify(searchService).searchByType("What deals are open?", "deal", 5);
        }
    }

    @Nested
    @DisplayName("generatePerformanceReport")
    class GeneratePerformanceReport {

        @Test
        @DisplayName("should perform multi-retrieval RAG across document types")
        void shouldPerformMultiRetrieval() {
            Document userDoc = new Document("Alice Johnson, Enterprise Sales",
                    Map.of("type", "user"));
            Document dealDoc = new Document("Deal Acme Corp $150,000 WON",
                    Map.of("type", "deal"));
            Document calcDoc = new Document("Commission $19,800 APPROVED",
                    Map.of("type", "commission_calculation"));
            Document planDoc = new Document("Standard Plan, 5-15% rates",
                    Map.of("type", "commission_plan"));

            when(searchService.searchByType(contains("Alice"), eq("user"), eq(2)))
                    .thenReturn(List.of(userDoc));
            when(searchService.searchByType(contains("deals"), eq("deal"), eq(5)))
                    .thenReturn(List.of(dealDoc));
            when(searchService.searchByType(contains("commission calculations"), eq("commission_calculation"), eq(5)))
                    .thenReturn(List.of(calcDoc));
            when(searchService.searchByType(contains("commission plan"), eq("commission_plan"), eq(3)))
                    .thenReturn(List.of(planDoc));

            when(searchService.extractContext(anyList())).thenReturn("extracted context");
            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("Performance Report for Alice Johnson: Strong performer...");

            String result = ragService.generatePerformanceReport("Alice");

            assertThat(result).contains("Alice Johnson");
            // Verify 4 separate retrieval calls were made
            verify(searchService, times(4)).searchByType(anyString(), anyString(), anyInt());
            verify(searchService, times(4)).extractContext(anyList());
        }
    }
}
