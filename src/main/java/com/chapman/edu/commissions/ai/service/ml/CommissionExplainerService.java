package com.chapman.edu.commissions.ai.service.ml;

import com.chapman.edu.commissions.ai.service.prompt.PromptTemplateService;
import com.chapman.edu.commissions.orm.entity.CommissionCalculation;
import com.chapman.edu.commissions.orm.entity.CommissionPlan;
import com.chapman.edu.commissions.orm.entity.Deal;
import com.chapman.edu.commissions.orm.repository.CommissionCalculationRepository;
import com.chapman.edu.commissions.orm.repository.CommissionPlanRepository;
import com.chapman.edu.commissions.orm.repository.DealRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

/**
 * ============================================================
 * SPRING AI SERVICE: CommissionExplainerService
 * ============================================================
 *
 * CONCEPT: AI Model Integration with Claude for Explainability
 * ------------------------------------------------------------
 * This service demonstrates how to use Claude AI to provide
 * natural language explanations of commission calculations.
 *
 * EXPLAINABLE AI (XAI) IN BUSINESS APPLICATIONS:
 * When an AI or algorithm makes a decision (e.g., calculating a commission),
 * stakeholders need to understand WHY the result is what it is.
 * This service uses Claude to:
 * 1. Analyze the calculation inputs (deal, plan, rates)
 * 2. Generate a human-readable explanation of the process
 * 3. Provide contextual insights (is this rate competitive? typical?)
 *
 * INTEGRATION PATTERN:
 * Service Layer → Loads domain data from JPA repositories
 *              → Builds prompt using PromptTemplateService
 *              → Sends prompt to ChatClient (backed by Claude)
 *              → Returns AI-generated explanation
 *
 * This demonstrates the BRIDGE pattern between traditional business
 * logic (ORM layer) and AI capabilities (Spring AI layer).
 */
@Service
public class CommissionExplainerService {

    private static final Logger log = LoggerFactory.getLogger(CommissionExplainerService.class);

    private final ChatClient chatClient;
    private final PromptTemplateService promptTemplateService;
    private final CommissionCalculationRepository calculationRepository;
    private final CommissionPlanRepository planRepository;
    private final DealRepository dealRepository;

    public CommissionExplainerService(ChatClient commissionChatClient,
                                       PromptTemplateService promptTemplateService,
                                       CommissionCalculationRepository calculationRepository,
                                       CommissionPlanRepository planRepository,
                                       DealRepository dealRepository) {
        this.chatClient = commissionChatClient;
        this.promptTemplateService = promptTemplateService;
        this.calculationRepository = calculationRepository;
        this.planRepository = planRepository;
        this.dealRepository = dealRepository;
    }

    /**
     * Generates a natural language explanation of a commission calculation.
     *
     * WORKFLOW:
     * 1. Load the calculation and related entities from the database
     * 2. Use PromptTemplateService to build a structured prompt
     * 3. Send the prompt to Claude via ChatClient
     * 4. Return the AI-generated explanation
     *
     * ChatClient FLUENT API:
     * - .prompt(Prompt): Sets the pre-built prompt from a template
     * - .call(): Executes the API call to Claude (synchronous)
     * - .content(): Extracts the text response from ChatResponse
     *
     * ERROR HANDLING:
     * If the calculation or related entities don't exist, we return
     * a descriptive error message instead of throwing an exception.
     * In production, you might throw custom exceptions handled by
     * a @ControllerAdvice.
     *
     * @param calculationId The ID of the commission calculation to explain
     * @return A natural language explanation of the calculation
     */
    public String explainCalculation(String calculationId) {
        log.info("Generating explanation for calculation: {}", calculationId);

        // Step 1: Load domain data from ORM layer
        CommissionCalculation calc = calculationRepository.findById(calculationId)
                .orElse(null);

        if (calc == null) {
            return "Commission calculation not found with ID: " + calculationId;
        }

        Deal deal = calc.getDeal();
        CommissionPlan plan = calc.getPlan();
        String planName = plan != null ? plan.getName() : "Default Plan";

        // Step 2: Build prompt using template service
        Prompt prompt = promptTemplateService.createCommissionAnalysisPrompt(
                deal.getTitle(),
                deal.getValue().toPlainString(),
                deal.getSalesRep().getFullName(),
                deal.getStatus().name(),
                planName,
                calc.getBaseCommission().toPlainString(),
                calc.getBaseCommission().toPlainString()
        );

        // Step 3: Call Claude AI via ChatClient
        // The .prompt() method accepts a pre-built Prompt object
        // that includes the template-rendered user message.
        String explanation = chatClient.prompt(prompt)
                .call()
                .content();

        log.info("Explanation generated for calculation: {}", calculationId);
        return explanation;
    }

    /**
     * Explains a commission plan's structure in plain language.
     *
     * This demonstrates using INLINE PROMPTING (no template file)
     * for simpler use cases. The prompt is constructed directly
     * in the ChatClient's fluent API using .user() and .system().
     *
     * WHEN TO USE INLINE vs. TEMPLATE:
     * - Inline: Simple prompts, prototyping, one-off queries
     * - Template: Complex prompts, reusable across services, production code
     *
     * @param planId The ID of the commission plan to explain
     * @return A plain-language explanation of the plan structure
     */
    public String explainPlan(String planId) {
        log.info("Generating explanation for plan: {}", planId);

        CommissionPlan plan = planRepository.findByIdWithTiers(planId)
                .orElse(null);

        if (plan == null) {
            return "Commission plan not found with ID: " + planId;
        }

        // Build plan details string
        StringBuilder planDetails = new StringBuilder();
        planDetails.append(String.format("Plan Name: %s\n", plan.getName()));
        planDetails.append(String.format("Status: %s\n", plan.getStatus()));
        planDetails.append(String.format("Currency: %s\n", plan.getCurrency()));

        if (!plan.getTiers().isEmpty()) {
            planDetails.append("\nCommission Tiers:\n");
            plan.getTiers().forEach(tier -> planDetails.append(String.format(
                    "  - %s: $%s to %s at %s%%\n",
                    tier.getName(),
                    tier.getLowerBound().toPlainString(),
                    tier.getUpperBound() != null ? "$" + tier.getUpperBound().toPlainString() : "unlimited",
                    tier.getRate().toPlainString()
            )));
        }

        // Inline prompt using ChatClient fluent API
        String explanation = chatClient.prompt()
                .system("You are a commission plan expert. Explain commission plans " +
                        "in simple terms that a new sales representative would understand.")
                .user(String.format("""
                        Please explain this commission plan in plain language:

                        %s

                        Include:
                        1. A simple summary of how commissions are earned
                        2. Examples of commission amounts for different deal sizes
                        3. Tips for maximizing earnings under this plan
                        """, planDetails))
                .call()
                .content();

        return explanation;
    }
}
