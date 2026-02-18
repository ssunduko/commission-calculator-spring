package com.chapman.edu.commissions.ai.ml;

import com.chapman.edu.commissions.ai.service.prompt.PromptTemplateService;
import com.chapman.edu.commissions.ai.service.ml.CommissionExplainerService;
import com.chapman.edu.commissions.orm.entity.*;
import com.chapman.edu.commissions.orm.repository.CommissionCalculationRepository;
import com.chapman.edu.commissions.orm.repository.CommissionPlanRepository;
import com.chapman.edu.commissions.orm.repository.DealRepository;
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
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommissionExplainerService — Unit Tests")
class CommissionExplainerServiceTest {

    @Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @Mock
    private PromptTemplateService promptTemplateService;

    @Mock
    private CommissionCalculationRepository calculationRepository;

    @Mock
    private CommissionPlanRepository planRepository;

    @Mock
    private DealRepository dealRepository;

    @InjectMocks
    private CommissionExplainerService explainerService;

    private User testUser;
    private Deal testDeal;
    private CommissionPlan testPlan;
    private CommissionCalculation testCalc;

    @BeforeEach
    void setUp() {
        testUser = new User("alice", "alice@test.com", "Alice", "Johnson");
        testUser.setId("user-001");

        testDeal = new Deal("Enterprise License", new BigDecimal("150000"), testUser);
        testDeal.setId("deal-001");
        testDeal.setStatus(DealStatus.WON);

        testPlan = new CommissionPlan("Standard Plan", Currency.getInstance("USD"));
        testPlan.setId("plan-001");
        testPlan.setStatus(PlanStatus.ACTIVE);

        testCalc = new CommissionCalculation(testDeal, testUser, new BigDecimal("18000"));
        testCalc.setId("calc-001");
        testCalc.setPlan(testPlan);
        testCalc.setGrossCommission(new BigDecimal("19800"));
        testCalc.setNetCommission(new BigDecimal("19800"));
        testCalc.setStatus(CommissionStatus.APPROVED);
    }

    @Nested
    @DisplayName("explainCalculation")
    class ExplainCalculation {

        @Test
        @DisplayName("should return error message when calculation not found")
        void shouldReturnErrorWhenCalculationNotFound() {
            when(calculationRepository.findById("nonexistent")).thenReturn(Optional.empty());

            String result = explainerService.explainCalculation("nonexistent");

            assertThat(result).isEqualTo("Commission calculation not found with ID: nonexistent");
            verifyNoInteractions(chatClient);
        }

        @Test
        @DisplayName("should generate explanation for valid calculation")
        void shouldGenerateExplanationForValidCalculation() {
            when(calculationRepository.findById("calc-001")).thenReturn(Optional.of(testCalc));
            when(promptTemplateService.createCommissionAnalysisPrompt(
                    anyString(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString()
            )).thenReturn(new Prompt("test prompt"));
            when(chatClient.prompt(any(Prompt.class)).call().content())
                    .thenReturn("The commission of $18,000 was calculated at 12% of the $150,000 deal.");

            String result = explainerService.explainCalculation("calc-001");

            assertThat(result).contains("$18,000");
            verify(promptTemplateService).createCommissionAnalysisPrompt(
                    eq("Enterprise License"),
                    eq("150000"),
                    eq("Alice Johnson"),
                    eq("WON"),
                    eq("Standard Plan"),
                    eq("18000"),
                    eq("18000")
            );
        }

        @Test
        @DisplayName("should use 'Default Plan' when plan is null")
        void shouldUseDefaultPlanNameWhenPlanIsNull() {
            testCalc.setPlan(null);
            when(calculationRepository.findById("calc-001")).thenReturn(Optional.of(testCalc));
            when(promptTemplateService.createCommissionAnalysisPrompt(
                    anyString(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString()
            )).thenReturn(new Prompt("test prompt"));
            when(chatClient.prompt(any(Prompt.class)).call().content())
                    .thenReturn("Explanation");

            explainerService.explainCalculation("calc-001");

            verify(promptTemplateService).createCommissionAnalysisPrompt(
                    anyString(), anyString(), anyString(), anyString(),
                    eq("Default Plan"),
                    anyString(), anyString()
            );
        }
    }

    @Nested
    @DisplayName("explainPlan")
    class ExplainPlan {

        @Test
        @DisplayName("should return error message when plan not found")
        void shouldReturnErrorWhenPlanNotFound() {
            when(planRepository.findByIdWithTiers("nonexistent")).thenReturn(Optional.empty());

            String result = explainerService.explainPlan("nonexistent");

            assertThat(result).isEqualTo("Commission plan not found with ID: nonexistent");
            verifyNoInteractions(chatClient);
        }

        @Test
        @DisplayName("should generate explanation for plan with tiers")
        void shouldGenerateExplanationForPlanWithTiers() {
            CommissionTier tier1 = new CommissionTier("Starter", BigDecimal.ZERO,
                    new BigDecimal("25000"), new BigDecimal("5"));
            CommissionTier tier2 = new CommissionTier("Growth", new BigDecimal("25000"),
                    new BigDecimal("75000"), new BigDecimal("8"));
            testPlan.addTier(tier1);
            testPlan.addTier(tier2);

            when(planRepository.findByIdWithTiers("plan-001")).thenReturn(Optional.of(testPlan));
            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("This plan has two tiers...");

            String result = explainerService.explainPlan("plan-001");

            assertThat(result).isEqualTo("This plan has two tiers...");
        }

        @Test
        @DisplayName("should handle plan with no tiers")
        void shouldHandlePlanWithNoTiers() {
            when(planRepository.findByIdWithTiers("plan-001")).thenReturn(Optional.of(testPlan));
            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("This plan has no tiers configured.");

            String result = explainerService.explainPlan("plan-001");

            assertThat(result).isNotNull();
        }
    }
}
