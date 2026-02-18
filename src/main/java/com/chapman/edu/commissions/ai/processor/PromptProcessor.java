package com.chapman.edu.commissions.ai.processor;

import com.chapman.edu.commissions.ai.service.prompt.PromptTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * ============================================================
 * PROCESSOR: PromptProcessor
 * ============================================================
 *
 * CONCEPT: Prompt Engineering and Template Management
 * ------------------------------------------------------------
 * This processor demonstrates the core prompt engineering techniques
 * used to get high-quality AI responses for commission analysis.
 *
 * WHAT IS PROMPT ENGINEERING?
 * Prompt engineering is the practice of crafting input text (prompts)
 * to AI models in ways that produce the most useful, accurate, and
 * consistent responses. It is both an art and a science.
 *
 * PROMPT ENGINEERING TECHNIQUES DEMONSTRATED:
 *
 * ┌──────────────────────────────────────────────────────────────┐
 * │ TECHNIQUE           │ PURPOSE                                │
 * ├──────────────────────────────────────────────────────────────┤
 * │ Role Assignment     │ Establish AI persona and expertise     │
 * │ Template Variables  │ Inject dynamic domain data             │
 * │ Structured Output   │ Ensure consistent response format      │
 * │ Chain-of-Thought    │ Improve reasoning accuracy             │
 * │ Few-Shot Examples   │ Guide output format with examples      │
 * │ Context Injection   │ Ground responses in specific data      │
 * └──────────────────────────────────────────────────────────────┘
 *
 * SPRING AI PROMPT TEMPLATES:
 * Spring AI provides PromptTemplate — similar to Thymeleaf for AI prompts.
 * Templates use {variableName} syntax for placeholder substitution.
 *
 * Template sources:
 * - .st files in classpath:/prompts/ (managed by PromptTemplateService)
 * - Inline strings (for simple, one-off prompts)
 *
 * BEST PRACTICES:
 * 1. Store complex prompts in external .st files for easy iteration
 * 2. Use descriptive variable names ({dealTitle}, not {t})
 * 3. Test prompts with diverse inputs to ensure robustness
 * 4. Version-control prompts alongside application code
 * 5. Keep each prompt focused on a single task (Single Responsibility)
 */
@Service
public class PromptProcessor {

    private static final Logger log = LoggerFactory.getLogger(PromptProcessor.class);

    private final ChatClient chatClient;
    private final PromptTemplateService promptTemplateService;

    public PromptProcessor(ChatClient commissionChatClient,
                           PromptTemplateService promptTemplateService) {
        this.chatClient = commissionChatClient;
        this.promptTemplateService = promptTemplateService;
    }

    // ============================================================
    // 1. ROLE ASSIGNMENT — Establishing AI Persona
    // ============================================================

    /**
     * Demonstrates ROLE ASSIGNMENT prompt engineering.
     *
     * TECHNIQUE: ROLE ASSIGNMENT
     * By telling the AI "You are a [specific expert]...", we:
     * - Activate domain-specific knowledge in the model
     * - Set expectations for vocabulary, tone, and depth
     * - Improve accuracy for specialized tasks
     *
     * COMMISSION CALCULATOR APPLICATION:
     * We assign the role of "senior commission analyst" to get responses
     * that use proper financial terminology, consider edge cases in
     * commission structures, and provide actionable insights.
     *
     * COMPARISON:
     * Without role: "The commission is $18,000."
     * With role:    "Based on the Enterprise tier (12% rate) applied to
     *               the $150,000 deal value, the base commission of $18,000
     *               falls within expected parameters. The Q1 accelerator
     *               bonus of 10% brings the gross to $19,800."
     *
     * @param query A commission-related question
     * @return An expert-level analysis from the assigned role
     */
    public String processWithRoleAssignment(String query) {
        log.info("Processing with role assignment: '{}'", query);

        String response = chatClient.prompt()
                .system("""
                        You are a senior commission analyst with 15 years of experience
                        in sales compensation design. You have deep expertise in:
                        - Tiered commission structures (starter, growth, enterprise, strategic)
                        - Accelerator bonuses and quarterly incentives
                        - Commission dispute resolution
                        - Sales performance benchmarking

                        When analyzing commissions:
                        - Always show your calculations step by step
                        - Reference specific tier names and rates
                        - Format all currency as $X,XXX.XX
                        - Highlight any anomalies or optimization opportunities
                        """)
                .user(query)
                .call()
                .content();

        log.info("Role assignment processing completed");
        return response;
    }

    // ============================================================
    // 2. CHAIN-OF-THOUGHT — Step-by-Step Reasoning
    // ============================================================

    /**
     * Demonstrates CHAIN-OF-THOUGHT (CoT) prompt engineering.
     *
     * TECHNIQUE: CHAIN-OF-THOUGHT PROMPTING
     * CoT instructs the AI to "think step by step" before giving its
     * final answer. This dramatically improves accuracy for:
     * - Mathematical calculations (commission amounts)
     * - Multi-step reasoning (which tier applies to this deal?)
     * - Complex analysis (is this commission anomalous?)
     *
     * HOW IT WORKS:
     * Without CoT: "The commission is $18,000" (may be wrong, no explanation)
     * With CoT:
     *   Step 1: Deal value = $150,000
     *   Step 2: $150,000 falls in Enterprise tier ($75K-$200K)
     *   Step 3: Enterprise tier rate = 12%
     *   Step 4: $150,000 × 0.12 = $18,000
     *   Answer: The commission is $18,000
     *
     * WHY IT WORKS:
     * Forcing the model to articulate intermediate steps reduces errors
     * because each step can be verified independently. It also makes the
     * AI's reasoning transparent and auditable — critical for financial apps.
     *
     * @param dealValue       The deal's monetary value
     * @param commissionPlan  Description of the commission plan and tiers
     * @return A step-by-step commission calculation with reasoning
     */
    public String processWithChainOfThought(String dealValue, String commissionPlan) {
        log.info("Processing with chain-of-thought for deal value: ${}", dealValue);

        String response = chatClient.prompt()
                .system("""
                        You are a commission calculator. You MUST think step by step.
                        For every calculation, show your complete reasoning process.
                        """)
                .user(String.format("""
                        Calculate the commission for a deal worth $%s.

                        Commission Plan Details:
                        %s

                        IMPORTANT: Think through this step by step:
                        Step 1: Identify the deal value
                        Step 2: Determine which commission tier applies based on the deal value
                        Step 3: Look up the commission rate for that tier
                        Step 4: Calculate base commission = deal value × rate
                        Step 5: Check for any applicable bonuses
                        Step 6: Calculate final commission amount

                        Show ALL steps with your reasoning before giving the final answer.
                        """, dealValue, commissionPlan))
                .call()
                .content();

        log.info("Chain-of-thought processing completed");
        return response;
    }

    // ============================================================
    // 3. FEW-SHOT PROMPTING — Learning from Examples
    // ============================================================

    /**
     * Demonstrates FEW-SHOT prompt engineering.
     *
     * TECHNIQUE: FEW-SHOT PROMPTING
     * By providing examples of desired input→output pairs, we teach the AI
     * the exact format and reasoning pattern we expect. This is especially
     * useful for:
     * - Ensuring consistent output format across different inputs
     * - Teaching domain-specific classification rules
     * - Demonstrating edge cases and how to handle them
     *
     * ZERO-SHOT vs FEW-SHOT vs MANY-SHOT:
     * - Zero-shot: No examples, just instructions
     * - Few-shot (2-5 examples): Best balance of guidance and token cost
     * - Many-shot (10+ examples): Maximum consistency, but expensive
     *
     * COMMISSION APPLICATION:
     * We provide examples of commission tier classifications so the AI
     * consistently determines the correct tier for any deal value.
     *
     * @param dealTitle The title/name of the deal
     * @param dealValue The monetary value of the deal
     * @return A tier classification and commission estimate
     */
    public String processWithFewShot(String dealTitle, String dealValue) {
        log.info("Processing with few-shot for deal: '{}' (${})", dealTitle, dealValue);

        String response = chatClient.prompt()
                .system("You are a commission tier classifier for the Standard Sales Plan.")
                .user(String.format("""
                        Classify the following deal into the correct commission tier and
                        calculate the expected commission. Follow the exact format shown
                        in the examples below.

                        === EXAMPLES ===

                        Deal: "Small Biz Starter Pack" — $10,000
                        → Tier: Starter ($0–$25,000)
                        → Rate: 5%%
                        → Commission: $10,000 × 5%% = $500.00
                        → Insight: Entry-level deal. Consider upselling to reach Growth tier.

                        Deal: "Mid-Market SaaS License" — $50,000
                        → Tier: Growth ($25,000–$75,000)
                        → Rate: 8%%
                        → Commission: $50,000 × 8%% = $4,000.00
                        → Insight: Solid Growth tier deal. $25K more would qualify for Enterprise.

                        Deal: "Enterprise Platform Deal" — $120,000
                        → Tier: Enterprise ($75,000–$200,000)
                        → Rate: 12%%
                        → Commission: $120,000 × 12%% = $14,400.00
                        → Insight: Strong Enterprise deal. Q1 accelerator adds 10%% bonus.

                        === YOUR TURN ===

                        Deal: "%s" — $%s
                        → Classify this deal using the same format as the examples above.
                        """, dealTitle, dealValue))
                .call()
                .content();

        log.info("Few-shot processing completed for deal: '{}'", dealTitle);
        return response;
    }

    // ============================================================
    // 4. TEMPLATE-BASED PROCESSING — External .st Files
    // ============================================================

    /**
     * Demonstrates TEMPLATE-BASED prompt engineering using .st files.
     *
     * TECHNIQUE: EXTERNAL PROMPT TEMPLATES
     * Instead of hardcoding prompts in Java, templates are stored in
     * classpath:/prompts/*.st files. This provides:
     * - Separation of concerns (prompt logic ≠ business logic)
     * - Easy iteration (edit .st file, no recompile needed in dev)
     * - Version control and code review of prompt changes
     * - Reuse across multiple services
     *
     * TEMPLATE SYNTAX:
     * {variableName} → Replaced at runtime with actual values
     * Templates are loaded as Spring Resource objects via @Value
     *
     * TEMPLATE FILES IN THIS PROJECT:
     * - commission-analysis.st: Deal commission analysis
     * - dispute-analysis.st: Dispute resolution analysis
     * - commission-forecast.st: Revenue forecasting
     * - anomaly-detection.st: Statistical anomaly analysis
     *
     * @param dealTitle      The deal title
     * @param dealValue      The deal value
     * @param salesRepName   The sales representative's name
     * @param dealStatus     The current deal status
     * @param planName       The commission plan name
     * @param commissionRate The commission rate
     * @param baseCommission The calculated base commission
     * @return An AI analysis using the commission-analysis template
     */
    public String processWithTemplate(String dealTitle, String dealValue,
                                       String salesRepName, String dealStatus,
                                       String planName, String commissionRate,
                                       String baseCommission) {
        log.info("Processing with template for deal: '{}'", dealTitle);

        // PromptTemplateService loads the .st file from classpath
        // and substitutes {variables} with provided values
        Prompt prompt = promptTemplateService.createCommissionAnalysisPrompt(
                dealTitle, dealValue, salesRepName, dealStatus,
                planName, commissionRate, baseCommission
        );

        // Send the rendered prompt to Claude
        String response = chatClient.prompt(prompt)
                .call()
                .content();

        log.info("Template-based processing completed for deal: '{}'", dealTitle);
        return response;
    }

    /**
     * Demonstrates using the forecast template with chain-of-thought prompting.
     *
     * The commission-forecast.st template is designed to elicit structured
     * analysis by requesting: trend analysis, key factors, projections,
     * and recommendations — forcing the AI to reason through each aspect.
     *
     * @param salesRepName  The sales representative's name
     * @param historicalData Formatted historical commission data
     * @param pipelineData  Formatted current pipeline data
     * @return An AI-generated commission forecast
     */
    public String processWithForecastTemplate(String salesRepName,
                                               String historicalData,
                                               String pipelineData) {
        log.info("Processing forecast template for: {}", salesRepName);

        Prompt prompt = promptTemplateService.createForecastPrompt(
                salesRepName, historicalData, pipelineData
        );

        String response = chatClient.prompt(prompt)
                .call()
                .content();

        log.info("Forecast template processing completed for: {}", salesRepName);
        return response;
    }

    // ============================================================
    // 5. INLINE TEMPLATES — Quick Prototyping
    // ============================================================

    /**
     * Demonstrates INLINE prompt templates using PromptTemplate directly.
     *
     * INLINE vs EXTERNAL TEMPLATES:
     * - Inline: String-based templates defined in code
     *   → Best for: Simple, one-off prompts; quick prototyping
     *   → Downside: Requires recompile to change; clutters code
     *
     * - External (.st files): Templates loaded from classpath resources
     *   → Best for: Complex prompts; production use; reusable templates
     *   → Downside: Additional file management overhead
     *
     * PromptTemplate VARIABLE SUBSTITUTION:
     * The PromptTemplate class processes {variableName} placeholders
     * and replaces them with values from the provided Map.
     *
     * @param question The user's natural language question
     * @param context  Domain context to ground the response
     * @return An AI answer grounded in the provided context
     */
    public String processWithInlineTemplate(String question, String context) {
        log.info("Processing with inline template: '{}'", question);

        // Use PromptTemplateService's inline Q&A prompt
        Prompt prompt = promptTemplateService.createQuestionAnswerPrompt(question, context);

        String response = chatClient.prompt(prompt)
                .call()
                .content();

        log.info("Inline template processing completed");
        return response;
    }

    // ============================================================
    // 6. STRUCTURED OUTPUT — Consistent Response Format
    // ============================================================

    /**
     * Demonstrates STRUCTURED OUTPUT prompt engineering for dispute analysis.
     *
     * TECHNIQUE: STRUCTURED OUTPUT REQUESTS
     * By explicitly defining the response structure in the prompt,
     * we ensure the AI returns consistent, machine-parseable responses.
     *
     * APPLICATIONS IN COMMISSION CALCULATOR:
     * - Dispute triage: Always returns PRIORITY: [HIGH|MEDIUM|LOW]
     * - Anomaly detection: Always returns NORMAL or ANOMALOUS
     * - Commission breakdown: Always returns numbered sections
     *
     * This makes it possible to build reliable UIs and workflows
     * on top of AI responses.
     *
     * @param disputeTitle       The dispute title
     * @param disputeDescription What the dispute is about
     * @param commissionAmount   The disputed commission amount
     * @param dealValue          The underlying deal value
     * @return A consistently structured dispute assessment
     */
    public String processWithStructuredOutput(String disputeTitle,
                                               String disputeDescription,
                                               String commissionAmount,
                                               String dealValue) {
        log.info("Processing structured output for dispute: '{}'", disputeTitle);

        String response = chatClient.prompt()
                .system("""
                        You are a commission dispute analyst. You MUST respond using
                        EXACTLY this structure (no deviations):

                        PRIORITY: [HIGH | MEDIUM | LOW]
                        VALIDITY: [VALID | INVALID | NEEDS_REVIEW]
                        AMOUNT_AT_STAKE: $[amount]

                        ASSESSMENT:
                        [2-3 sentences analyzing the dispute]

                        RECOMMENDATION:
                        [1-2 sentences with the recommended action]

                        NEXT_STEPS:
                        1. [First action item]
                        2. [Second action item]
                        3. [Third action item]
                        """)
                .user(String.format("""
                        Analyze this commission dispute:

                        Dispute: %s
                        Description: %s
                        Commission Amount: $%s
                        Deal Value: $%s
                        """, disputeTitle, disputeDescription, commissionAmount, dealValue))
                .call()
                .content();

        log.info("Structured output processing completed for dispute: '{}'", disputeTitle);
        return response;
    }

    // ============================================================
    // 7. DYNAMIC TEMPLATE CONSTRUCTION
    // ============================================================

    /**
     * Demonstrates building a PromptTemplate dynamically at runtime.
     *
     * TECHNIQUE: DYNAMIC PROMPT CONSTRUCTION
     * Sometimes the prompt structure itself needs to vary based on
     * the available data. This method constructs a template that
     * adapts to the provided variables.
     *
     * USE CASE:
     * When generating a commission summary, the available data varies:
     * - New reps may have no historical data
     * - Some deals may not have close dates
     * - Not all calculations have bonuses applied
     *
     * The prompt adapts by including only relevant sections.
     *
     * @param variables A map of variable names to their values
     * @return An AI-generated analysis based on the available data
     */
    public String processWithDynamicTemplate(Map<String, Object> variables) {
        log.info("Processing with dynamic template, {} variables provided", variables.size());

        StringBuilder templateBuilder = new StringBuilder();
        templateBuilder.append("Analyze the following commission data and provide insights:\n\n");

        // Dynamically build template sections based on available data
        if (variables.containsKey("salesRep")) {
            templateBuilder.append("Sales Representative: {salesRep}\n");
        }
        if (variables.containsKey("dealTitle")) {
            templateBuilder.append("Deal: {dealTitle}\n");
        }
        if (variables.containsKey("dealValue")) {
            templateBuilder.append("Deal Value: ${dealValue}\n");
        }
        if (variables.containsKey("commissionPlan")) {
            templateBuilder.append("Commission Plan: {commissionPlan}\n");
        }
        if (variables.containsKey("commissionAmount")) {
            templateBuilder.append("Commission Amount: ${commissionAmount}\n");
        }
        if (variables.containsKey("historicalData")) {
            templateBuilder.append("\nHistorical Performance:\n{historicalData}\n");
        }

        templateBuilder.append("""

                Provide a concise analysis covering:
                1. Summary of the data provided
                2. Key observations or patterns
                3. Actionable recommendations
                """);

        PromptTemplate template = new PromptTemplate(templateBuilder.toString());
        Prompt prompt = template.create(variables);

        String response = chatClient.prompt(prompt)
                .call()
                .content();

        log.info("Dynamic template processing completed");
        return response;
    }
}
