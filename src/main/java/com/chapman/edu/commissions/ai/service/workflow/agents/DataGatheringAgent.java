package com.chapman.edu.commissions.ai.service.workflow.agents;

import com.chapman.edu.commissions.ai.service.workflow.WorkflowAgent;
import com.chapman.edu.commissions.ai.service.workflow.WorkflowStage;
import com.chapman.edu.commissions.ai.service.workflow.WorkflowState;
import com.chapman.edu.commissions.orm.entity.*;
import com.chapman.edu.commissions.orm.repository.CommissionCalculationRepository;
import com.chapman.edu.commissions.orm.repository.CommissionPlanRepository;
import com.chapman.edu.commissions.orm.repository.DealRepository;
import com.chapman.edu.commissions.orm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ============================================================
 * AGENTIC WORKFLOW: Data Gathering Agent
 * ============================================================
 *
 * CONCEPT: Specialized Agent for Data Collection
 * ------------------------------------------------------------
 * This is the FIRST agent in the commission review workflow.
 * Its job is to:
 *
 * 1. Parse the user's request to identify the sales rep
 * 2. Query repositories for all relevant commission data
 * 3. Structure the raw data into a comprehensive summary
 * 4. Use the AI to produce a clean, organized data brief
 *
 * WHY A DEDICATED GATHERING AGENT?
 * Data collection is a distinct responsibility from analysis.
 * By isolating it, we get:
 * - FOCUSED PROMPT: The AI prompt is optimized for data organization
 * - REUSABLE OUTPUT: Other agents consume a clean data brief
 * - CLEAR FAILURE MODE: If data is missing, we know at this stage
 * - SEPARATION OF CONCERNS: Gathering ≠ analyzing ≠ reporting
 *
 * DATA FLOW:
 *   User Request → "Review Alice Johnson's commissions"
 *         ↓
 *   Parse name → Query repos → Raw data
 *         ↓
 *   AI organizes → Structured data brief
 *         ↓
 *   state.putData("gathered_data", dataBrief)
 *   state.putData("sales_rep_name", "Alice Johnson")
 */
@Component
public class DataGatheringAgent implements WorkflowAgent {

    private static final Logger log = LoggerFactory.getLogger(DataGatheringAgent.class);

    private final ChatClient chatClient;
    private final UserRepository userRepository;
    private final DealRepository dealRepository;
    private final CommissionCalculationRepository calculationRepository;
    private final CommissionPlanRepository planRepository;

    public DataGatheringAgent(ChatClient commissionChatClient,
                              UserRepository userRepository,
                              DealRepository dealRepository,
                              CommissionCalculationRepository calculationRepository,
                              CommissionPlanRepository planRepository) {
        this.chatClient = commissionChatClient;
        this.userRepository = userRepository;
        this.dealRepository = dealRepository;
        this.calculationRepository = calculationRepository;
        this.planRepository = planRepository;
    }

    @Override
    public String getName() {
        return "Data Gathering Agent";
    }

    @Override
    public WorkflowStage getStage() {
        return WorkflowStage.GATHERING;
    }

    /**
     * Gathers all commission data relevant to the user's request.
     *
     * STRATEGY:
     * 1. Use the AI to extract the sales rep name from the request
     * 2. Query each repository for that rep's data
     * 3. Combine into a structured raw data block
     * 4. Use the AI to organize into a clean brief
     * 5. Store in state for downstream agents
     */
    @Override
    @Transactional(readOnly = true)
    public void execute(WorkflowState state) {
        log.info("[{}] Starting data gathering for: '{}'", getName(), state.getOriginalRequest());

        // Step 1: Extract the sales rep name from the request using AI
        String salesRepName = extractSalesRepName(state.getOriginalRequest());
        state.putData("sales_rep_name", salesRepName);
        log.info("[{}] Identified sales rep: {}", getName(), salesRepName);

        // Step 2: Query repositories for raw data
        StringBuilder rawData = new StringBuilder();

        // Find the user
        List<User> users = userRepository.searchByName(salesRepName);
        if (users.isEmpty()) {
            state.putData("gathered_data", "No sales representative found matching: " + salesRepName);
            state.addFlag("NO_DATA");
            state.logStage(getStage(), "No user found for '" + salesRepName + "'");
            return;
        }

        User user = users.get(0);
        rawData.append("=== SALES REPRESENTATIVE ===\n");
        rawData.append(String.format("Name: %s | ID: %s | Department: %s | Territory: %s | Active: %s\n\n",
                user.getFullName(), user.getId(),
                user.getDepartment() != null ? user.getDepartment() : "N/A",
                user.getTerritory() != null ? user.getTerritory() : "N/A",
                user.isActive()));

        // Find their deals
        List<Deal> deals = dealRepository.findAll().stream()
                .filter(d -> d.getSalesRep() != null && d.getSalesRep().getId().equals(user.getId()))
                .collect(Collectors.toList());
        rawData.append("=== DEALS ===\n");
        if (deals.isEmpty()) {
            rawData.append("No deals found.\n\n");
        } else {
            for (Deal deal : deals) {
                rawData.append(String.format("- %s | Value: $%s | Status: %s | Close Date: %s\n",
                        deal.getTitle(),
                        deal.getValue().toPlainString(),
                        deal.getStatus(),
                        deal.getCloseDate() != null ? deal.getCloseDate().toString() : "N/A"));
            }
            rawData.append("\n");
        }

        // Find their commission calculations
        List<CommissionCalculation> calcs = calculationRepository.findBySalesRepId(user.getId());
        rawData.append("=== COMMISSION CALCULATIONS ===\n");
        if (calcs.isEmpty()) {
            rawData.append("No commission calculations found.\n\n");
        } else {
            for (CommissionCalculation calc : calcs) {
                rawData.append(String.format(
                        "- ID: %s | Base: $%s | Gross: $%s | Net: $%s | Status: %s | Deal: %s | Plan: %s | Date: %s\n",
                        calc.getId(),
                        calc.getBaseCommission().toPlainString(),
                        calc.getGrossCommission().toPlainString(),
                        calc.getNetCommission().toPlainString(),
                        calc.getStatus(),
                        calc.getDeal() != null ? calc.getDeal().getTitle() : "N/A",
                        calc.getPlan() != null ? calc.getPlan().getName() : "N/A",
                        calc.getCalculationDate() != null ? calc.getCalculationDate().toString() : "N/A"));
            }
            rawData.append("\n");
        }

        // Find relevant commission plans
        rawData.append("=== COMMISSION PLANS ===\n");
        List<CommissionPlan> plans = planRepository.findByStatus(PlanStatus.ACTIVE);
        for (CommissionPlan plan : plans) {
            rawData.append(String.format("Plan: %s | Status: %s | Currency: %s\n",
                    plan.getName(), plan.getStatus(), plan.getCurrency()));

            Optional<CommissionPlan> withTiers = planRepository.findByIdWithTiers(plan.getId());
            if (withTiers.isPresent() && withTiers.get().getTiers() != null) {
                for (CommissionTier tier : withTiers.get().getTiers()) {
                    rawData.append(String.format("  Tier: %s | $%s–%s at %s%%\n",
                            tier.getName(),
                            tier.getLowerBound().toPlainString(),
                            tier.getUpperBound() != null ? "$" + tier.getUpperBound().toPlainString() : "unlimited",
                            tier.getRate().toPlainString()));
                }
            }
            rawData.append("\n");
        }

        // Step 3: Use AI to organize into a structured brief
        String dataBrief = organizeDataBrief(salesRepName, rawData.toString());

        // Step 4: Store in state
        state.putData("gathered_data", dataBrief);
        state.putData("raw_data", rawData.toString());

        String summary = String.format("Collected %d deal(s), %d calculation(s), %d plan(s) for %s",
                deals.size(), calcs.size(), plans.size(), user.getFullName());
        state.logStage(getStage(), summary);
        log.info("[{}] {}", getName(), summary);
    }

    // ============================================================
    // AI-POWERED HELPERS
    // ============================================================

    /**
     * Uses the AI to extract a sales rep name from a natural language request.
     *
     * EXAMPLE:
     * Input:  "Review Alice Johnson's commission performance for Q1"
     * Output: "Alice Johnson"
     */
    private String extractSalesRepName(String request) {
        try {
            String response = chatClient.prompt()
                    .system("Extract ONLY the sales representative's name from the user request. " +
                            "Return just the name, nothing else. If no name is found, return 'unknown'.")
                    .user(request)
                    .call()
                    .content();
            return response != null ? response.trim() : "unknown";
        } catch (Exception e) {
            log.warn("[{}] Failed to extract name via AI, falling back to request text: {}",
                    getName(), e.getMessage());
            return request;
        }
    }

    /**
     * Uses the AI to organize raw data into a clean, structured brief.
     *
     * WHY AI FOR ORGANIZATION?
     * Raw database output is verbose and unstructured. The AI can:
     * - Group related data logically
     * - Highlight key metrics (total commission, deal count, etc.)
     * - Remove redundant information
     * - Present data in a format optimized for downstream AI agents
     */
    private String organizeDataBrief(String salesRepName, String rawData) {
        try {
            return chatClient.prompt()
                    .system("""
                            You are a data analyst preparing a structured brief for a commission review.
                            Organize the raw data into clear sections with key metrics highlighted.
                            Include totals and counts. Keep the format consistent and machine-readable.
                            Do NOT add analysis or opinions — just organize the facts.""")
                    .user(String.format(
                            "Organize the following raw commission data for %s into a structured brief:\n\n%s",
                            salesRepName, rawData))
                    .call()
                    .content();
        } catch (Exception e) {
            log.warn("[{}] AI organization failed, using raw data: {}", getName(), e.getMessage());
            return rawData;
        }
    }
}
