package com.chapman.edu.commissions.ai.service.prompt;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * ============================================================
 * SPRING AI SERVICE: PromptTemplateService
 * ============================================================
 *
 * CONCEPT: Prompt Engineering and Template Management
 * ------------------------------------------------------------
 * Prompt engineering is the practice of designing and optimizing the
 * text instructions (prompts) sent to AI models to get the best results.
 *
 * KEY PROMPT ENGINEERING TECHNIQUES DEMONSTRATED:
 *
 * 1. ROLE ASSIGNMENT (System Prompt):
 *    "You are an expert commission calculator assistant..."
 *    Establishes the AI's persona and domain expertise.
 *    This significantly improves response quality for domain-specific tasks.
 *
 * 2. STRUCTURED OUTPUT REQUESTS:
 *    "Please provide: 1. ... 2. ... 3. ..."
 *    Asking for numbered/structured responses ensures completeness
 *    and consistency across different queries.
 *
 * 3. CONTEXT INJECTION (Template Variables):
 *    "{dealTitle}", "${dealValue}", "{salesRepName}"
 *    Injecting specific data into prompts provides the AI with the
 *    context it needs to give accurate, relevant responses.
 *
 * 4. FEW-SHOT PROMPTING:
 *    Including examples in the prompt to guide the AI's response format.
 *    (Demonstrated in the commission explainer prompts.)
 *
 * 5. CHAIN-OF-THOUGHT (CoT):
 *    "Explain your reasoning step by step" encourages the model to
 *    show its work, leading to more accurate calculations.
 *
 * SPRING AI PROMPT TEMPLATES:
 * Spring AI provides PromptTemplate, which works like Thymeleaf or
 * StringTemplate for AI prompts. Templates are stored as .st files
 * in src/main/resources/prompts/ and loaded via @Value + Resource.
 *
 * TEMPLATE SYNTAX:
 * - {variableName}: Placeholder replaced at runtime with actual values
 * - Templates are loaded from classpath resources
 * - Variables are passed as a Map<String, Object>
 *
 * BEST PRACTICES:
 * - Store prompts in external files (not hardcoded) for easy iteration
 * - Use descriptive variable names in templates
 * - Version-control your prompts alongside code
 * - Test prompts with different inputs to ensure robustness
 * - Keep prompts focused on one task (Single Responsibility)
 */
@Service
public class PromptTemplateService {

    /**
     * Spring's @Value with "classpath:" loads files from src/main/resources.
     * The Resource abstraction allows reading the template file content.
     */
    @Value("classpath:/prompts/commission-analysis.st")
    private Resource commissionAnalysisTemplate;

    @Value("classpath:/prompts/dispute-analysis.st")
    private Resource disputeAnalysisTemplate;

    @Value("classpath:/prompts/commission-forecast.st")
    private Resource commissionForecastTemplate;

    @Value("classpath:/prompts/anomaly-detection.st")
    private Resource anomalyDetectionTemplate;

    /**
     * Creates a prompt for analyzing a commission calculation.
     *
     * PROMPT TEMPLATE WORKFLOW:
     * 1. Load the template from the .st resource file
     * 2. Create a Map of variable name → value pairs
     * 3. PromptTemplate replaces {placeholders} with actual values
     * 4. The resulting Prompt object is sent to the ChatModel
     *
     * @param dealTitle     The title/name of the deal
     * @param dealValue     The monetary value of the deal
     * @param salesRepName  The name of the sales representative
     * @param dealStatus    Current status of the deal (OPEN, WON, etc.)
     * @param planName      Name of the commission plan applied
     * @param commissionRate The commission rate percentage
     * @param baseCommission The calculated base commission amount
     * @return A Prompt object ready to be sent to the AI model
     */
    public Prompt createCommissionAnalysisPrompt(String dealTitle, String dealValue,
                                                  String salesRepName, String dealStatus,
                                                  String planName, String commissionRate,
                                                  String baseCommission) {
        PromptTemplate template = new PromptTemplate(commissionAnalysisTemplate);
        return template.create(Map.of(
                "dealTitle", dealTitle,
                "dealValue", dealValue,
                "salesRepName", salesRepName,
                "dealStatus", dealStatus,
                "planName", planName,
                "commissionRate", commissionRate,
                "baseCommission", baseCommission
        ));
    }

    /**
     * Creates a prompt for analyzing a commission dispute.
     *
     * This template demonstrates DOMAIN-SPECIFIC PROMPT ENGINEERING:
     * The prompt includes both the dispute details AND the related
     * commission data, giving the AI full context to make a fair assessment.
     */
    public Prompt createDisputeAnalysisPrompt(String disputeTitle, String disputeDescription,
                                               String salesRepName, String disputeStatus,
                                               String baseCommission, String grossCommission,
                                               String netCommission, String dealValue) {
        PromptTemplate template = new PromptTemplate(disputeAnalysisTemplate);
        return template.create(Map.of(
                "disputeTitle", disputeTitle,
                "disputeDescription", disputeDescription,
                "salesRepName", salesRepName,
                "disputeStatus", disputeStatus,
                "baseCommission", baseCommission,
                "grossCommission", grossCommission,
                "netCommission", netCommission,
                "dealValue", dealValue
        ));
    }

    /**
     * Creates a prompt for commission forecasting.
     *
     * CHAIN-OF-THOUGHT PROMPTING:
     * The template asks for "trend analysis" and "key factors",
     * which forces the model to reason through the data step by step
     * before making predictions. This technique significantly improves
     * the accuracy of AI-generated forecasts.
     */
    public Prompt createForecastPrompt(String salesRepName, String historicalData,
                                        String pipelineData) {
        PromptTemplate template = new PromptTemplate(commissionForecastTemplate);
        return template.create(Map.of(
                "salesRepName", salesRepName,
                "historicalData", historicalData,
                "pipelineData", pipelineData
        ));
    }

    /**
     * Creates a prompt for anomaly detection in commission calculations.
     *
     * STRUCTURED INPUT + STRUCTURED OUTPUT:
     * This prompt provides statistical context (average rate, standard deviation)
     * alongside raw data, then requests a structured risk assessment.
     * This combination helps the AI make informed judgments rather than
     * just pattern-matching on the data.
     */
    public Prompt createAnomalyDetectionPrompt(String calculationsSummary, String planRules,
                                                String avgRate, String stdDeviation) {
        PromptTemplate template = new PromptTemplate(anomalyDetectionTemplate);
        return template.create(Map.of(
                "calculationsSummary", calculationsSummary,
                "planRules", planRules,
                "avgRate", avgRate,
                "stdDeviation", stdDeviation
        ));
    }

    /**
     * Creates an ad-hoc prompt using inline template strings.
     *
     * INLINE TEMPLATES:
     * For simple, one-off prompts that don't need their own file,
     * you can create PromptTemplate directly from a string.
     * Use this for quick prototyping, then extract to a file for production.
     *
     * @param question The user's natural language question
     * @param context  Relevant domain context to include
     * @return A Prompt with the question and context combined
     */
    public Prompt createQuestionAnswerPrompt(String question, String context) {
        String templateString = """
                You are a commission calculation expert. Answer the following question
                using the provided context. If the context doesn't contain enough
                information to answer fully, say so and provide what you can.

                Context:
                {context}

                Question: {question}

                Provide a clear, concise answer.
                """;
        PromptTemplate template = new PromptTemplate(templateString);
        return template.create(Map.of(
                "question", question,
                "context", context
        ));
    }
}
