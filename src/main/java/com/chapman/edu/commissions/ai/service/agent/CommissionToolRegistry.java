package com.chapman.edu.commissions.ai.service.agent;

import com.chapman.edu.commissions.ai.service.vectorstore.EmbeddingSearchService;
import com.chapman.edu.commissions.orm.entity.*;
import com.chapman.edu.commissions.orm.repository.CommissionCalculationRepository;
import com.chapman.edu.commissions.orm.repository.CommissionPlanRepository;
import com.chapman.edu.commissions.orm.repository.DealRepository;
import com.chapman.edu.commissions.orm.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ============================================================
 * REACT AGENT: Commission Tool Registry
 * ============================================================
 *
 * CONCEPT: Registering Domain-Specific Tools for the Agent
 * ------------------------------------------------------------
 * This component creates and registers all tools available to the
 * CommissionReActAgent. Each tool wraps a database query or
 * calculation, exposing it as a simple string-in, string-out function.
 *
 * TOOL DESIGN PRINCIPLES:
 *
 * 1. SINGLE RESPONSIBILITY: Each tool does ONE thing
 *    - lookup_user finds a user
 *    - lookup_deals finds deals
 *    - They don't overlap or combine operations
 *
 * 2. CLEAR DESCRIPTIONS: The AI reads these to decide which tool to use
 *    - Bad: "Gets user data"
 *    - Good: "Look up a sales representative by name. Returns their ID,
 *            department, territory, and role."
 *
 * 3. STRUCTURED OUTPUT: Tools return formatted text the AI can parse
 *    - Include field labels: "Name: Alice Johnson"
 *    - Use consistent formatting across tools
 *    - Include "not found" messages so the AI knows to try differently
 *
 * 4. READ-ONLY: Agent tools should NEVER modify data
 *    - Agents can call tools many times during reasoning
 *    - Accidental writes could corrupt data
 *    - For write operations, use separate service methods with explicit confirmation
 *
 * WHY A REGISTRY?
 * Separating tool definitions from the agent makes both more testable
 * and maintainable. New tools can be added without modifying the agent.
 * The registry pattern also enables conditional tool registration
 * (e.g., admin-only tools based on user roles).
 */
@Component
public class CommissionToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(CommissionToolRegistry.class);

    private final CommissionReActAgent agent;
    private final UserRepository userRepository;
    private final DealRepository dealRepository;
    private final CommissionCalculationRepository calculationRepository;
    private final CommissionPlanRepository planRepository;
    private final EmbeddingSearchService searchService;

    public CommissionToolRegistry(CommissionReActAgent agent,
                                   UserRepository userRepository,
                                   DealRepository dealRepository,
                                   CommissionCalculationRepository calculationRepository,
                                   CommissionPlanRepository planRepository,
                                   EmbeddingSearchService searchService) {
        this.agent = agent;
        this.userRepository = userRepository;
        this.dealRepository = dealRepository;
        this.calculationRepository = calculationRepository;
        this.planRepository = planRepository;
        this.searchService = searchService;
    }

    /**
     * Registers all commission tools at application startup.
     *
     * @PostConstruct ensures this runs after dependency injection is complete
     * but before the application starts serving requests.
     */
    @PostConstruct
    public void registerTools() {
        log.info("Registering commission agent tools...");

        agent.registerTool(createLookupUserTool());
        agent.registerTool(createLookupDealsTool());
        agent.registerTool(createLookupCalculationsTool());
        agent.registerTool(createLookupPlanTool());
        agent.registerTool(createCalculateTotalTool());
        agent.registerTool(createSearchKnowledgeBaseTool());

        log.info("Registered {} agent tools", agent.getTools().size());
    }

    // ============================================================
    // TOOL: lookup_user
    // ============================================================

    /**
     * Tool that looks up a sales representative by name.
     *
     * INPUT: A name or search term (e.g., "Alice", "Bob Smith")
     * OUTPUT: User details including ID, department, territory, roles
     *
     * WHY NAME-BASED LOOKUP?
     * Users ask questions using names ("How much did Alice earn?"),
     * not internal IDs. This tool bridges natural language to database keys.
     */
    private Tool createLookupUserTool() {
        return new Tool(
                "lookup_user",
                "Look up a sales representative by name. Returns their ID, full name, " +
                "department, territory, and roles. Input: the person's name (e.g., 'Alice').",
                input -> {
                    List<User> users = userRepository.searchByName(input.trim());
                    if (users.isEmpty()) {
                        return "No user found matching '" + input + "'.";
                    }

                    StringBuilder sb = new StringBuilder();
                    for (User user : users) {
                        sb.append(String.format(
                                "User: %s | ID: %s | Department: %s | Territory: %s | Active: %s | Roles: %s\n",
                                user.getFullName(),
                                user.getId(),
                                user.getDepartment() != null ? user.getDepartment() : "N/A",
                                user.getTerritory() != null ? user.getTerritory() : "N/A",
                                user.isActive(),
                                user.getRoles()
                        ));
                    }
                    return sb.toString().trim();
                }
        );
    }

    // ============================================================
    // TOOL: lookup_deals
    // ============================================================

    /**
     * Tool that looks up deals by status or sales rep ID.
     *
     * INPUT FORMAT: "status:WON" or "rep:<userId>" or "all"
     * OUTPUT: List of deals with title, value, status, and sales rep
     *
     * FLEXIBLE INPUT PARSING:
     * The AI might call this tool in different ways. We parse the input
     * to determine the query type, with a fallback to status-based search.
     */
    private Tool createLookupDealsTool() {
        return new Tool(
                "lookup_deals",
                "Look up deals in the system. Input format: 'status:WON' to filter by status " +
                "(WON, OPEN, LOST), 'rep:<userId>' to find deals for a specific sales rep ID, " +
                "or 'all' to list all deals. Returns deal title, value, status, and sales rep.",
                input -> {
                    String trimmed = input.trim();
                    List<Deal> deals;

                    if (trimmed.toLowerCase().startsWith("rep:")) {
                        String repId = trimmed.substring(4).trim();
                        deals = dealRepository.findAll().stream()
                                .filter(d -> d.getSalesRep() != null && d.getSalesRep().getId().equals(repId))
                                .collect(Collectors.toList());
                    } else if (trimmed.toLowerCase().startsWith("status:")) {
                        String statusStr = trimmed.substring(7).trim().toUpperCase();
                        try {
                            DealStatus status = DealStatus.valueOf(statusStr);
                            deals = dealRepository.findByStatus(status);
                        } catch (IllegalArgumentException e) {
                            return "Invalid status '" + statusStr + "'. Valid values: WON, OPEN, LOST.";
                        }
                    } else {
                        deals = dealRepository.findAll();
                    }

                    if (deals.isEmpty()) {
                        return "No deals found for query: " + trimmed;
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("Found %d deal(s):\n", deals.size()));
                    for (Deal deal : deals) {
                        sb.append(String.format(
                                "- %s | Value: $%s | Status: %s | Rep: %s | Close Date: %s\n",
                                deal.getTitle(),
                                deal.getValue().toPlainString(),
                                deal.getStatus(),
                                deal.getSalesRep() != null ? deal.getSalesRep().getFullName() : "N/A",
                                deal.getCloseDate() != null ? deal.getCloseDate().toString() : "N/A"
                        ));
                    }
                    return sb.toString().trim();
                }
        );
    }

    // ============================================================
    // TOOL: lookup_calculations
    // ============================================================

    /**
     * Tool that looks up commission calculations for a sales rep.
     *
     * INPUT: A sales rep user ID
     * OUTPUT: Commission calculations with amounts, status, and deal info
     */
    private Tool createLookupCalculationsTool() {
        return new Tool(
                "lookup_calculations",
                "Look up commission calculations for a sales rep by their user ID. " +
                "Returns base commission, net commission, status, and associated deal. " +
                "Input: the sales rep's user ID.",
                input -> {
                    String userId = input.trim();
                    List<CommissionCalculation> calcs = calculationRepository.findBySalesRepId(userId);

                    if (calcs.isEmpty()) {
                        return "No commission calculations found for user ID: " + userId;
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("Found %d calculation(s) for user %s:\n", calcs.size(), userId));
                    BigDecimal total = BigDecimal.ZERO;

                    for (CommissionCalculation calc : calcs) {
                        sb.append(String.format(
                                "- ID: %s | Base: $%s | Net: $%s | Status: %s | Deal: %s | Date: %s\n",
                                calc.getId(),
                                calc.getBaseCommission().toPlainString(),
                                calc.getNetCommission().toPlainString(),
                                calc.getStatus(),
                                calc.getDeal() != null ? calc.getDeal().getTitle() : "N/A",
                                calc.getCalculationDate() != null ? calc.getCalculationDate().toString() : "N/A"
                        ));
                        total = total.add(calc.getNetCommission());
                    }

                    sb.append(String.format("Total net commission: $%s", total.toPlainString()));
                    return sb.toString().trim();
                }
        );
    }

    // ============================================================
    // TOOL: lookup_plan
    // ============================================================

    /**
     * Tool that looks up a commission plan with its tiers.
     *
     * INPUT: Plan name or "active" to find active plans
     * OUTPUT: Plan details including tier structure and rates
     */
    private Tool createLookupPlanTool() {
        return new Tool(
                "lookup_plan",
                "Look up a commission plan by name or find all active plans. " +
                "Input: plan name to search, or 'active' to list all active plans. " +
                "Returns plan name, status, tiers with rate ranges.",
                input -> {
                    String trimmed = input.trim();
                    List<CommissionPlan> plans;

                    if (trimmed.equalsIgnoreCase("active")) {
                        plans = planRepository.findByStatus(PlanStatus.ACTIVE);
                    } else {
                        plans = planRepository.findByNameContainingIgnoreCase(trimmed);
                    }

                    if (plans.isEmpty()) {
                        return "No commission plans found for: " + trimmed;
                    }

                    StringBuilder sb = new StringBuilder();
                    for (CommissionPlan plan : plans) {
                        sb.append(String.format(
                                "Plan: %s | Status: %s | Currency: %s | Effective: %s\n",
                                plan.getName(),
                                plan.getStatus(),
                                plan.getCurrency(),
                                plan.getEffectiveStartDate() != null ? plan.getEffectiveStartDate().toString() : "N/A"
                        ));

                        // Load tiers
                        Optional<CommissionPlan> withTiers = planRepository.findByIdWithTiers(plan.getId());
                        if (withTiers.isPresent() && withTiers.get().getTiers() != null) {
                            sb.append("  Tiers:\n");
                            for (CommissionTier tier : withTiers.get().getTiers()) {
                                sb.append(String.format(
                                        "  - %s: $%s–%s at %s%%\n",
                                        tier.getName(),
                                        tier.getLowerBound().toPlainString(),
                                        tier.getUpperBound() != null ? "$" + tier.getUpperBound().toPlainString() : "unlimited",
                                        tier.getRate().toPlainString()
                                ));
                            }
                        }
                    }
                    return sb.toString().trim();
                }
        );
    }

    // ============================================================
    // TOOL: calculate_total
    // ============================================================

    /**
     * Tool that performs commission calculations.
     *
     * INPUT: "deal_value * rate" (e.g., "150000 * 12")
     * OUTPUT: Calculated commission amount
     *
     * WHY A CALCULATION TOOL?
     * LLMs are notoriously unreliable at arithmetic. By providing
     * a calculation tool, the agent can get precise results instead
     * of approximating. This is critical for financial applications.
     */
    private Tool createCalculateTotalTool() {
        return new Tool(
                "calculate_total",
                "Calculate a commission amount. Input format: 'deal_value * rate_percent' " +
                "(e.g., '150000 * 12' calculates 12% of $150,000). " +
                "Can also sum values: 'sum:1000,2000,3000'. Returns the precise result.",
                input -> {
                    String trimmed = input.trim();

                    // Handle sum operation
                    if (trimmed.toLowerCase().startsWith("sum:")) {
                        String[] values = trimmed.substring(4).split(",");
                        BigDecimal total = BigDecimal.ZERO;
                        for (String val : values) {
                            try {
                                total = total.add(new BigDecimal(val.trim()));
                            } catch (NumberFormatException e) {
                                return "Error: Invalid number '" + val.trim() + "' in sum.";
                            }
                        }
                        return String.format("Sum total: $%s", total.setScale(2, RoundingMode.HALF_UP).toPlainString());
                    }

                    // Handle multiplication (deal_value * rate)
                    if (trimmed.contains("*")) {
                        String[] parts = trimmed.split("\\*");
                        if (parts.length != 2) {
                            return "Error: Expected format 'value * rate_percent' (e.g., '150000 * 12').";
                        }
                        try {
                            BigDecimal value = new BigDecimal(parts[0].trim());
                            BigDecimal rate = new BigDecimal(parts[1].trim());
                            BigDecimal commission = value.multiply(rate)
                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                            return String.format(
                                    "$%s * %s%% = $%s",
                                    value.toPlainString(),
                                    rate.toPlainString(),
                                    commission.toPlainString());
                        } catch (NumberFormatException e) {
                            return "Error: Could not parse numbers. Use format: '150000 * 12'.";
                        }
                    }

                    return "Error: Unknown format. Use 'value * rate_percent' or 'sum:v1,v2,v3'.";
                }
        );
    }

    // ============================================================
    // TOOL: search_knowledge_base
    // ============================================================

    /**
     * Tool that searches the vector store for commission knowledge.
     *
     * INPUT: A natural language search query
     * OUTPUT: Top matching documents from the vector store
     *
     * This tool brings RAG capabilities into the ReAct agent,
     * allowing it to search the semantic knowledge base when
     * structured database queries aren't sufficient.
     */
    private Tool createSearchKnowledgeBaseTool() {
        return new Tool(
                "search_knowledge_base",
                "Search the commission knowledge base using semantic search. " +
                "Input: a natural language query about commissions, deals, plans, or reps. " +
                "Returns the most relevant documents from the vector store.",
                input -> {
                    List<Document> results = searchService.search(input.trim(), 3);
                    if (results.isEmpty()) {
                        return "No relevant documents found for: " + input;
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("Found %d relevant document(s):\n", results.size()));
                    for (int i = 0; i < results.size(); i++) {
                        String content = results.get(i).getText();
                        if (content.length() > 300) {
                            content = content.substring(0, 300) + "...";
                        }
                        sb.append(String.format("[%d] %s\n", i + 1, content));
                    }
                    return sb.toString().trim();
                }
        );
    }
}
