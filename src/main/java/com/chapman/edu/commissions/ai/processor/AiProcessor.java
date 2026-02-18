package com.chapman.edu.commissions.ai.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ============================================================
 * PROCESSOR: AiProcessor
 * ============================================================
 *
 * CONCEPT: Spring AI Framework Setup and Configuration
 * ------------------------------------------------------------
 * This processor demonstrates how Spring AI integrates with Spring Boot
 * to provide AI capabilities in the Commission Calculator application.
 *
 * SPRING AI ARCHITECTURE OVERVIEW:
 *
 * ┌─────────────────────────────────────────────────────────────┐
 * │                    APPLICATION LAYER                        │
 * │  AiProcessor / Services / Controllers                      │
 * ├─────────────────────────────────────────────────────────────┤
 * │                  SPRING AI ABSTRACTION                      │
 * │  ChatClient (fluent API) ← ChatClient.Builder              │
 * │     ↓                                                       │
 * │  ChatModel (model interface) ← AnthropicChatModel          │
 * ├─────────────────────────────────────────────────────────────┤
 * │               AUTO-CONFIGURATION LAYER                      │
 * │  spring-ai-anthropic-spring-boot-starter                    │
 * │     → Reads application.properties                          │
 * │     → Creates AnthropicChatModel bean                       │
 * │     → Creates ChatClient.Builder bean (prototype-scoped)    │
 * ├─────────────────────────────────────────────────────────────┤
 * │                    API TRANSPORT                             │
 * │  HTTPS calls to https://api.anthropic.com/v1/messages       │
 * │  Authentication: Bearer token (spring.ai.anthropic.api-key) │
 * └─────────────────────────────────────────────────────────────┘
 *
 * KEY CONCEPTS DEMONSTRATED:
 *
 * 1. CHATCLIENT INJECTION:
 *    The ChatClient bean is created in AiConfig with a default system prompt.
 *    All processors/services share the same ChatClient, ensuring consistent
 *    behavior across the application.
 *
 * 2. SYSTEM PROMPTS (Role Assignment):
 *    System prompts establish the AI's persona and constraints.
 *    They are sent with EVERY request and shape all responses.
 *    AiConfig sets: "You are an expert commission calculator assistant..."
 *
 * 3. CONFIGURATION PROPERTIES:
 *    Spring AI reads model configuration from application.properties:
 *    - API key, model selection, max tokens, temperature
 *    These are injected via @Value for runtime inspection.
 *
 * 4. FLUENT API PATTERNS:
 *    ChatClient provides a builder-pattern API:
 *    chatClient.prompt().system("...").user("...").call().content()
 *
 * 5. ERROR RESILIENCE:
 *    AI calls can fail (rate limits, network issues, invalid input).
 *    This processor demonstrates graceful error handling patterns.
 */
@Service
public class AiProcessor {

    private static final Logger log = LoggerFactory.getLogger(AiProcessor.class);

    private final ChatClient chatClient;

    @Value("${spring.ai.anthropic.chat.options.model:unknown}")
    private String modelName;

    @Value("${spring.ai.anthropic.chat.options.max-tokens:1024}")
    private int maxTokens;

    @Value("${spring.ai.anthropic.chat.options.temperature:0.3}")
    private double temperature;

    @Value("${spring.ai.mcp.server.name:commission-calculator}")
    private String mcpServerName;

    public AiProcessor(ChatClient commissionChatClient) {
        this.chatClient = commissionChatClient;
    }

    // ============================================================
    // 1. BASIC AI INTERACTION — ChatClient Fluent API
    // ============================================================

    /**
     * Demonstrates the simplest ChatClient interaction pattern.
     *
     * CHATCLIENT FLUENT API CHAIN:
     * chatClient.prompt()  → Creates a new PromptRequest builder
     *   .user(message)     → Sets the user message (what the user is asking)
     *   .call()            → Executes the HTTP call to Claude API (synchronous)
     *   .content()         → Extracts the text content from ChatResponse
     *
     * NOTE: The system prompt from AiConfig is automatically included
     * because the ChatClient was built with .defaultSystem() in AiConfig.
     *
     * @param query A natural language question about commissions
     * @return The AI-generated response
     */
    public String processSimpleQuery(String query) {
        log.info("Processing simple query: '{}'", query);

        String response = chatClient.prompt()
                .user(query)
                .call()
                .content();

        log.info("Simple query processed successfully");
        return response;
    }

    // ============================================================
    // 2. SYSTEM PROMPT OVERRIDE — Role Assignment
    // ============================================================

    /**
     * Demonstrates overriding the default system prompt with a custom role.
     *
     * SYSTEM PROMPT LAYERING:
     * - AiConfig sets a DEFAULT system prompt for all requests
     * - Individual calls can OVERRIDE it with .system() for specific roles
     * - This is useful when one service needs a different AI persona
     *
     * PROMPT ENGINEERING — ROLE ASSIGNMENT:
     * Assigning a role ("You are a senior sales analyst...") dramatically
     * improves response quality because the AI adopts domain expertise,
     * vocabulary, and reasoning patterns appropriate to that role.
     *
     * COMMISSION CALCULATOR ROLES:
     * - "commission analyst" → Precise calculations, rate breakdowns
     * - "sales coach" → Motivational, actionable advice
     * - "finance auditor" → Risk-focused, compliance-oriented
     * - "data scientist" → Statistical analysis, trend identification
     *
     * @param query The commission-related question
     * @param role  The specialist role for the AI to adopt
     * @return A role-appropriate AI response
     */
    public String processWithSystemContext(String query, String role) {
        log.info("Processing query with role '{}': '{}'", role, query);

        String systemPrompt = String.format("""
                You are a %s for a sales commission organization.
                You specialize in analyzing commission plans, deal performance,
                and payout structures.

                Respond from the perspective of your role.
                Be precise with numbers and format currency as $X,XXX.XX.
                Keep your response concise and actionable.
                """, role);

        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(query)
                .call()
                .content();

        log.info("Role-based query processed successfully");
        return response;
    }

    // ============================================================
    // 3. STRUCTURED COMMISSION ANALYSIS
    // ============================================================

    /**
     * Demonstrates structured output prompting for commission analysis.
     *
     * PROMPT ENGINEERING — STRUCTURED OUTPUT:
     * By explicitly requesting a specific format in the prompt, we ensure
     * the AI returns consistent, parseable responses. This is critical
     * for downstream processing (e.g., displaying in a UI, logging to DB).
     *
     * CONTEXT INJECTION:
     * Domain-specific data (deal details, plan info, calculation amounts)
     * is injected directly into the user message. This gives the AI the
     * precise context it needs to generate an accurate analysis.
     *
     * @param dealTitle      The title of the deal
     * @param dealValue      The monetary value of the deal
     * @param commissionRate The applied commission rate (as a percentage string)
     * @param commissionAmount The calculated commission amount
     * @return A structured AI analysis of the commission
     */
    public String analyzeCommission(String dealTitle, String dealValue,
                                     String commissionRate, String commissionAmount) {
        log.info("Analyzing commission for deal: '{}'", dealTitle);

        String response = chatClient.prompt()
                .system("""
                        You are a commission calculation auditor.
                        Always respond with a structured analysis containing exactly these sections:

                        ## Calculation Verification
                        [Verify the math: deal value × rate = commission]

                        ## Rate Assessment
                        [Is this rate appropriate for the deal size?]

                        ## Recommendations
                        [Actionable recommendations for the sales rep]
                        """)
                .user(String.format("""
                        Analyze this commission calculation:

                        Deal: %s
                        Deal Value: $%s
                        Commission Rate: %s%%
                        Commission Amount: $%s
                        """, dealTitle, dealValue, commissionRate, commissionAmount))
                .call()
                .content();

        log.info("Commission analysis completed for deal: '{}'", dealTitle);
        return response;
    }

    // ============================================================
    // 4. MULTI-TURN CONTEXT — Conversation Simulation
    // ============================================================

    /**
     * Demonstrates providing conversational context to the AI.
     *
     * CONTEXT WINDOW:
     * Claude processes all text in its "context window" (the combined
     * system prompt + user messages). By providing prior context as part
     * of the user message, we simulate a multi-turn conversation.
     *
     * WHY THIS MATTERS FOR COMMISSIONS:
     * A sales manager might ask a follow-up question like:
     *   "What if we increased the rate to 15%?"
     * Without the original context, the AI wouldn't know which deal
     * or calculation is being discussed.
     *
     * @param priorContext Previous conversation or analysis context
     * @param followUpQuery The follow-up question
     * @return An AI response that accounts for prior context
     */
    public String processWithContext(String priorContext, String followUpQuery) {
        log.info("Processing follow-up query with prior context");

        String response = chatClient.prompt()
                .system("""
                        You are a commission calculator assistant continuing a conversation.
                        The user will provide prior context and a follow-up question.
                        Base your answer on the prior context provided.
                        """)
                .user(String.format("""
                        Previous analysis:
                        ---
                        %s
                        ---

                        Follow-up question: %s
                        """, priorContext, followUpQuery))
                .call()
                .content();

        log.info("Follow-up query processed successfully");
        return response;
    }

    // ============================================================
    // 5. CONFIGURATION INSPECTION
    // ============================================================

    /**
     * Returns the current Spring AI configuration as a structured map.
     *
     * CONFIGURATION AWARENESS:
     * This method demonstrates how Spring AI configuration properties
     * flow from application.properties into the running application.
     *
     * PROPERTY HIERARCHY (highest priority wins):
     * 1. Command line args: --spring.ai.anthropic.chat.options.model=X
     * 2. Environment variables: SPRING_AI_ANTHROPIC_CHAT_OPTIONS_MODEL=X
     * 3. application.properties: spring.ai.anthropic.chat.options.model=X
     * 4. Default values (in @Value annotations)
     *
     * SECURITY NOTE:
     * The API key is intentionally NOT exposed here. In production,
     * API keys should be stored in a secrets manager (AWS Secrets Manager,
     * HashiCorp Vault) and never logged or returned in API responses.
     *
     * @return A map of current AI configuration properties
     */
    public Map<String, Object> getAiConfiguration() {
        log.info("Retrieving AI configuration details");

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("model", modelName);
        config.put("maxTokens", maxTokens);
        config.put("temperature", temperature);
        config.put("mcpServerName", mcpServerName);
        config.put("temperatureDescription", describeTemperature(temperature));

        return config;
    }

    // ============================================================
    // 6. AI HEALTH CHECK — Connection Validation
    // ============================================================

    /**
     * Validates the AI service connection with a lightweight test call.
     *
     * HEALTH CHECK PATTERN:
     * In production, you'd register this with Spring Boot Actuator's
     * health endpoint. This method sends a minimal prompt to verify:
     * 1. The API key is valid
     * 2. The model is reachable
     * 3. The response is received successfully
     *
     * COST CONSIDERATION:
     * Even health checks consume API tokens. The prompt is kept
     * intentionally short to minimize cost. In production, consider
     * rate-limiting health checks or using a cheaper model.
     *
     * @return A map with status ("UP" or "DOWN") and details
     */
    public Map<String, Object> healthCheck() {
        log.info("Running AI service health check");

        Map<String, Object> health = new LinkedHashMap<>();
        health.put("model", modelName);

        try {
            String response = chatClient.prompt()
                    .user("Respond with exactly: OK")
                    .call()
                    .content();

            health.put("status", "UP");
            health.put("responseReceived", response != null && !response.isBlank());
            log.info("AI health check: UP");
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            log.warn("AI health check: DOWN — {}", e.getMessage());
        }

        return health;
    }

    /**
     * Describes the temperature setting in human-readable terms.
     *
     * TEMPERATURE IN AI MODELS:
     * Temperature controls the randomness of the AI's token selection:
     * - 0.0: Deterministic — always picks the most probable token
     * - 0.3: Low creativity — consistent, factual responses (good for analysis)
     * - 0.7: Balanced — good for general conversation
     * - 1.0: Maximum creativity — diverse, surprising responses
     *
     * For commission calculations, low temperature (0.3) is preferred
     * because we want consistent, accurate numerical analysis.
     */
    private String describeTemperature(double temp) {
        if (temp <= 0.1) return "Deterministic (minimal randomness)";
        if (temp <= 0.3) return "Low creativity (consistent, factual — ideal for financial analysis)";
        if (temp <= 0.7) return "Balanced (good for general conversation)";
        return "High creativity (diverse, surprising responses)";
    }
}
