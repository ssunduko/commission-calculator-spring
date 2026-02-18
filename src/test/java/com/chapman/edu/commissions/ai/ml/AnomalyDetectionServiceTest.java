package com.chapman.edu.commissions.ai.ml;

import com.chapman.edu.commissions.ai.service.prompt.PromptTemplateService;
import com.chapman.edu.commissions.ai.service.ml.AnomalyDetectionService;
import com.chapman.edu.commissions.orm.entity.*;
import com.chapman.edu.commissions.orm.repository.CommissionCalculationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnomalyDetectionService — Unit Tests")
class AnomalyDetectionServiceTest {

    @Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @Mock
    private PromptTemplateService promptTemplateService;

    @Mock
    private CommissionCalculationRepository calculationRepository;

    @InjectMocks
    private AnomalyDetectionService anomalyDetectionService;

    private User alice;
    private User bob;
    private List<CommissionCalculation> testCalculations;

    @BeforeEach
    void setUp() {
        alice = new User("alice", "alice@test.com", "Alice", "Johnson");
        alice.setId("user-001");
        bob = new User("bob", "bob@test.com", "Bob", "Smith");
        bob.setId("user-002");

        Deal deal1 = new Deal("Deal A", new BigDecimal("150000"), alice);
        deal1.setId("deal-001");
        Deal deal2 = new Deal("Deal B", new BigDecimal("35000"), bob);
        deal2.setId("deal-002");
        Deal deal3 = new Deal("Deal C", new BigDecimal("250000"), alice);
        deal3.setId("deal-003");

        CommissionCalculation calc1 = new CommissionCalculation(deal1, alice, new BigDecimal("18000"));
        calc1.setId("calc-001");
        calc1.setNetCommission(new BigDecimal("19800"));
        calc1.setStatus(CommissionStatus.APPROVED);

        CommissionCalculation calc2 = new CommissionCalculation(deal2, bob, new BigDecimal("2800"));
        calc2.setId("calc-002");
        calc2.setNetCommission(new BigDecimal("3080"));
        calc2.setStatus(CommissionStatus.APPROVED);

        CommissionCalculation calc3 = new CommissionCalculation(deal3, alice, new BigDecimal("37500"));
        calc3.setId("calc-003");
        calc3.setNetCommission(new BigDecimal("37500"));
        calc3.setStatus(CommissionStatus.CALCULATED);

        testCalculations = List.of(calc1, calc2, calc3);
    }

    @Nested
    @DisplayName("detectAnomalies")
    class DetectAnomalies {

        @Test
        @DisplayName("should return message when no calculations exist")
        void shouldReturnMessageWhenNoCalculations() {
            when(calculationRepository.findAll()).thenReturn(Collections.emptyList());

            String result = anomalyDetectionService.detectAnomalies();

            assertThat(result).isEqualTo("No commission calculations available for anomaly detection.");
            verifyNoInteractions(chatClient);
        }

        @Test
        @DisplayName("should compute statistics and call AI for analysis")
        void shouldComputeStatisticsAndCallAi() {
            when(calculationRepository.findAll()).thenReturn(testCalculations);
            when(promptTemplateService.createAnomalyDetectionPrompt(
                    anyString(), anyString(), anyString(), anyString()
            )).thenReturn(new Prompt("anomaly prompt"));
            when(chatClient.prompt(any(Prompt.class)).call().content())
                    .thenReturn("Anomaly detected in calc-003: $37,500 is >2σ from mean.");

            String result = anomalyDetectionService.detectAnomalies();

            assertThat(result).contains("Anomaly");

            // Verify statistics were passed to the prompt template
            ArgumentCaptor<String> summaryCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> avgCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> stdDevCaptor = ArgumentCaptor.forClass(String.class);

            verify(promptTemplateService).createAnomalyDetectionPrompt(
                    summaryCaptor.capture(), anyString(), avgCaptor.capture(), stdDevCaptor.capture()
            );

            // Mean of 19800, 3080, 37500 = 60380 / 3 = 20126.67
            assertThat(avgCaptor.getValue()).isEqualTo("20126.67");
            // Summary should contain all calculation IDs
            assertThat(summaryCaptor.getValue())
                    .contains("calc-001")
                    .contains("calc-002")
                    .contains("calc-003");
        }

        @Test
        @DisplayName("should flag calculations beyond 2 standard deviations")
        void shouldFlagAnomalousCalculations() {
            // Create dataset where one value IS a >2σ outlier.
            // With 6 "normal" values around $100 and 1 extreme at $1000,
            // the mean ≈ $228.57, stddev ≈ $321.5, so 2σ ≈ $643.
            // |1000-228.57| = 771.43 > 643 → FLAGGED
            Deal dealN = new Deal("Normal Deal", new BigDecimal("10000"), alice);
            dealN.setId("deal-norm");

            java.util.List<CommissionCalculation> skewedCalcs = new java.util.ArrayList<>();
            for (int i = 0; i < 6; i++) {
                CommissionCalculation c = new CommissionCalculation(dealN, alice, new BigDecimal("100"));
                c.setId("calc-n" + i);
                c.setNetCommission(new BigDecimal("100"));
                c.setStatus(CommissionStatus.APPROVED);
                skewedCalcs.add(c);
            }
            CommissionCalculation outlier = new CommissionCalculation(dealN, alice, new BigDecimal("1000"));
            outlier.setId("calc-outlier");
            outlier.setNetCommission(new BigDecimal("1000"));
            outlier.setStatus(CommissionStatus.APPROVED);
            skewedCalcs.add(outlier);

            when(calculationRepository.findAll()).thenReturn(skewedCalcs);
            when(promptTemplateService.createAnomalyDetectionPrompt(
                    anyString(), anyString(), anyString(), anyString()
            )).thenReturn(new Prompt("anomaly prompt"));
            when(chatClient.prompt(any(Prompt.class)).call().content())
                    .thenReturn("Analysis complete");

            anomalyDetectionService.detectAnomalies();

            ArgumentCaptor<String> summaryCaptor = ArgumentCaptor.forClass(String.class);
            verify(promptTemplateService).createAnomalyDetectionPrompt(
                    summaryCaptor.capture(), anyString(), anyString(), anyString()
            );

            String summary = summaryCaptor.getValue();
            assertThat(summary).contains("[FLAGGED");
        }
    }

    @Nested
    @DisplayName("checkSingleCalculation")
    class CheckSingleCalculation {

        @Test
        @DisplayName("should return error when calculation not found")
        void shouldReturnErrorWhenNotFound() {
            when(calculationRepository.findById("nonexistent")).thenReturn(Optional.empty());

            String result = anomalyDetectionService.checkSingleCalculation("nonexistent");

            assertThat(result).isEqualTo("Calculation not found");
        }

        @Test
        @DisplayName("should check single calculation against population")
        void shouldCheckAgainstPopulation() {
            CommissionCalculation calc = testCalculations.get(0);
            when(calculationRepository.findById("calc-001")).thenReturn(Optional.of(calc));
            when(calculationRepository.findAll()).thenReturn(testCalculations);
            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("NORMAL - This calculation is within expected range.");

            String result = anomalyDetectionService.checkSingleCalculation("calc-001");

            assertThat(result).startsWith("NORMAL");
        }

        @Test
        @DisplayName("should handle empty population gracefully")
        void shouldHandleEmptyPopulation() {
            CommissionCalculation calc = testCalculations.get(0);
            when(calculationRepository.findById("calc-001")).thenReturn(Optional.of(calc));
            when(calculationRepository.findAll()).thenReturn(Collections.emptyList());
            when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("Cannot determine - insufficient population data.");

            String result = anomalyDetectionService.checkSingleCalculation("calc-001");

            assertThat(result).isNotNull();
        }
    }
}
