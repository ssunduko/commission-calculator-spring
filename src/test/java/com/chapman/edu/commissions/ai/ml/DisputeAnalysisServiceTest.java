package com.chapman.edu.commissions.ai.ml;

import com.chapman.edu.commissions.ai.service.prompt.PromptTemplateService;
import com.chapman.edu.commissions.ai.service.ml.DisputeAnalysisService;
import com.chapman.edu.commissions.orm.entity.*;
import com.chapman.edu.commissions.orm.repository.DisputeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DisputeAnalysisService — Unit Tests")
class DisputeAnalysisServiceTest {

    @Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @Mock
    private PromptTemplateService promptTemplateService;

    @Mock
    private DisputeRepository disputeRepository;

    @InjectMocks
    private DisputeAnalysisService disputeAnalysisService;

    private User testUser;
    private Deal testDeal;
    private CommissionCalculation testCalc;
    private Dispute testDispute;

    @BeforeEach
    void setUp() {
        testUser = new User("alice", "alice@test.com", "Alice", "Johnson");
        testUser.setId("user-001");

        testDeal = new Deal("Enterprise License", new BigDecimal("150000"), testUser);
        testDeal.setId("deal-001");
        testDeal.setStatus(DealStatus.WON);

        testCalc = new CommissionCalculation(testDeal, testUser, new BigDecimal("18000"));
        testCalc.setId("calc-001");
        testCalc.setGrossCommission(new BigDecimal("19800"));
        testCalc.setNetCommission(new BigDecimal("19800"));

        testDispute = new Dispute(testCalc, testUser, "Wrong Rate Applied",
                "The Growth tier rate was applied instead of Enterprise tier");
        testDispute.setId("disp-001");
    }

    @Nested
    @DisplayName("analyzeDispute")
    class AnalyzeDispute {

        @Test
        @DisplayName("should return error message when dispute not found")
        void shouldReturnErrorWhenDisputeNotFound() {
            when(disputeRepository.findById("nonexistent")).thenReturn(Optional.empty());

            String result = disputeAnalysisService.analyzeDispute("nonexistent");

            assertThat(result).isEqualTo("Dispute not found with ID: nonexistent");
            verifyNoInteractions(chatClient);
        }

        @Test
        @DisplayName("should generate analysis for valid dispute")
        void shouldGenerateAnalysisForValidDispute() {
            when(disputeRepository.findById("disp-001")).thenReturn(Optional.of(testDispute));
            when(promptTemplateService.createDisputeAnalysisPrompt(
                    anyString(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString()
            )).thenReturn(new Prompt("dispute analysis prompt"));
            when(chatClient.prompt(any(Prompt.class)).call().content())
                    .thenReturn("The dispute appears to have merit. The Enterprise tier rate of 12% should have been applied.");

            String result = disputeAnalysisService.analyzeDispute("disp-001");

            assertThat(result).contains("merit");
            verify(promptTemplateService).createDisputeAnalysisPrompt(
                    eq("Wrong Rate Applied"),
                    eq("The Growth tier rate was applied instead of Enterprise tier"),
                    eq("Alice Johnson"),
                    eq("INITIATED"),
                    eq("18000"),
                    eq("19800"),
                    eq("19800"),
                    eq("150000")
            );
        }
    }

    @Nested
    @DisplayName("triageDispute")
    class TriageDispute {

        @Test
        @DisplayName("should return error when dispute not found")
        void shouldReturnErrorWhenDisputeNotFound() {
            when(disputeRepository.findById("nonexistent")).thenReturn(Optional.empty());

            String result = disputeAnalysisService.triageDispute("nonexistent");

            assertThat(result).isEqualTo("Dispute not found");
        }

        @Test
        @DisplayName("should return priority assessment for valid dispute")
        void shouldReturnPriorityAssessment() {
            when(disputeRepository.findById("disp-001")).thenReturn(Optional.of(testDispute));
            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("PRIORITY: HIGH - Significant commission amount at stake with clear rate discrepancy");

            String result = disputeAnalysisService.triageDispute("disp-001");

            assertThat(result).startsWith("PRIORITY: HIGH");
        }
    }
}
