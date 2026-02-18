package com.chapman.edu.commissions.ai.prompt;

import com.chapman.edu.commissions.ai.service.prompt.PromptTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PromptTemplateService — Unit Tests")
class PromptTemplateServiceTest {

    private PromptTemplateService promptTemplateService;

    @BeforeEach
    void setUp() {
        promptTemplateService = new PromptTemplateService();

        // Inject classpath resources that @Value would normally provide
        ReflectionTestUtils.setField(promptTemplateService, "commissionAnalysisTemplate",
                new ClassPathResource("/prompts/commission-analysis.st"));
        ReflectionTestUtils.setField(promptTemplateService, "disputeAnalysisTemplate",
                new ClassPathResource("/prompts/dispute-analysis.st"));
        ReflectionTestUtils.setField(promptTemplateService, "commissionForecastTemplate",
                new ClassPathResource("/prompts/commission-forecast.st"));
        ReflectionTestUtils.setField(promptTemplateService, "anomalyDetectionTemplate",
                new ClassPathResource("/prompts/anomaly-detection.st"));
    }

    @Test
    @DisplayName("createCommissionAnalysisPrompt should substitute all template variables")
    void createCommissionAnalysisPrompt_shouldSubstituteVariables() {
        Prompt prompt = promptTemplateService.createCommissionAnalysisPrompt(
                "Acme Corp Deal",
                "150000",
                "Alice Johnson",
                "WON",
                "Standard Plan",
                "12",
                "18000"
        );

        String content = prompt.getContents();
        assertThat(content)
                .contains("Acme Corp Deal")
                .contains("150000")
                .contains("Alice Johnson")
                .contains("WON")
                .contains("Standard Plan")
                .contains("18000");
    }

    @Test
    @DisplayName("createDisputeAnalysisPrompt should substitute all template variables")
    void createDisputeAnalysisPrompt_shouldSubstituteVariables() {
        Prompt prompt = promptTemplateService.createDisputeAnalysisPrompt(
                "Wrong Rate",
                "Enterprise tier rate should have been applied",
                "Bob Smith",
                "INITIATED",
                "18000",
                "19800",
                "19800",
                "150000"
        );

        String content = prompt.getContents();
        assertThat(content)
                .contains("Wrong Rate")
                .contains("Enterprise tier rate should have been applied")
                .contains("Bob Smith")
                .contains("INITIATED")
                .contains("18000")
                .contains("19800")
                .contains("150000");
    }

    @Test
    @DisplayName("createForecastPrompt should substitute all template variables")
    void createForecastPrompt_shouldSubstituteVariables() {
        Prompt prompt = promptTemplateService.createForecastPrompt(
                "Alice Johnson",
                "- Date: 2026-01-15 | Base: $18000 | Net: $19800 | Status: APPROVED",
                "- Deal: MegaCorp | Value: $500000 | Status: OPEN"
        );

        String content = prompt.getContents();
        assertThat(content)
                .contains("Alice Johnson")
                .contains("$18000")
                .contains("MegaCorp");
    }

    @Test
    @DisplayName("createAnomalyDetectionPrompt should substitute all template variables")
    void createAnomalyDetectionPrompt_shouldSubstituteVariables() {
        Prompt prompt = promptTemplateService.createAnomalyDetectionPrompt(
                "- ID: calc-001 | Net: $19800 | Status: APPROVED",
                "Standard commission rules apply",
                "20126.67",
                "14500.00"
        );

        String content = prompt.getContents();
        assertThat(content)
                .contains("calc-001")
                .contains("$19800")
                .contains("20126.67")
                .contains("14500.00");
    }

    @Test
    @DisplayName("createQuestionAnswerPrompt should create inline prompt with question and context")
    void createQuestionAnswerPrompt_shouldCreateInlinePrompt() {
        Prompt prompt = promptTemplateService.createQuestionAnswerPrompt(
                "What is the commission rate for enterprise deals?",
                "Enterprise tier: 12% for deals between $75,000 and $200,000"
        );

        String content = prompt.getContents();
        assertThat(content)
                .contains("What is the commission rate for enterprise deals?")
                .contains("Enterprise tier: 12%")
                .contains("commission calculation expert");
    }
}
