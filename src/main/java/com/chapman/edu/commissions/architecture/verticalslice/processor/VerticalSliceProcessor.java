package com.chapman.edu.commissions.architecture.verticalslice.processor;

import com.chapman.edu.commissions.architecture.verticalslice.features.calculations.CalculateCommissionRequest;
import com.chapman.edu.commissions.architecture.verticalslice.features.calculations.CommissionCalculationService;
import com.chapman.edu.commissions.architecture.verticalslice.features.deals.CreateDealRequest;
import com.chapman.edu.commissions.architecture.verticalslice.features.deals.DealService;
import com.chapman.edu.commissions.architecture.verticalslice.features.disputes.DisputeService;
import com.chapman.edu.commissions.architecture.verticalslice.features.plans.CommissionPlanService;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.mcp.McpCommissionTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * PROCESSOR: Vertical Slice Architecture Demonstration
 * ============================================================
 *
 * CONCEPT: Vertical Slice Architecture
 * ------------------------------------------------------------
 * The simplest architecture — organize by FEATURE (vertical slice),
 * not by technical layer. Each feature owns its controller, service,
 * repository, DTOs — all in the same package.
 *
 * PACKAGE STRUCTURE:
 *
 *   features/
 *   ├── deals/              ← Everything about deals
 *   │   ├── DealController
 *   │   ├── DealService
 *   │   ├── DealRepository
 *   │   ├── DealResponse
 *   │   └── CreateDealRequest
 *   ├── plans/              ← Everything about plans
 *   │   ├── CommissionPlanController
 *   │   ├── CommissionPlanService
 *   │   └── ...
 *   ├── calculations/       ← Everything about calculations
 *   └── disputes/           ← Everything about disputes
 *
 * CONTRAST WITH LAYERED ARCHITECTURE:
 *
 *   Layered:                    Vertical Slice:
 *   controllers/                features/deals/
 *     DealController              DealController
 *     PlanController              DealService
 *   services/                     DealRepository
 *     DealService               features/plans/
 *     PlanService                 PlanController
 *   repositories/                 PlanService
 *     DealRepository              PlanRepository
 *     PlanRepository
 *
 * KEY BENEFITS:
 * - Feature changes touch ONE package, not multiple layers
 * - Minimal abstractions — concrete classes, not interfaces
 * - Easy to understand for newcomers
 * - Fast development — no ceremony
 */
@Service("verticalSliceProcessor")
public class VerticalSliceProcessor {

    private static final Logger log = LoggerFactory.getLogger(VerticalSliceProcessor.class);

    private final DealService dealService;
    private final CommissionPlanService planService;
    private final CommissionCalculationService calcService;
    private final DisputeService disputeService;
    private final McpCommissionTools mcpTools;
    private final List<ToolCallback> toolCallbacks;

    public VerticalSliceProcessor(DealService dealService,
                                   CommissionPlanService planService,
                                   CommissionCalculationService calcService,
                                   DisputeService disputeService,
                                   McpCommissionTools mcpTools,
                                   List<ToolCallback> toolCallbacks) {
        this.dealService = dealService;
        this.planService = planService;
        this.calcService = calcService;
        this.disputeService = disputeService;
        this.mcpTools = mcpTools;
        this.toolCallbacks = toolCallbacks;
    }

    // ============================================================
    // DEMO 1: Feature-First Organization
    // ============================================================

    /**
     * Demonstrates how vertical slices organize by feature,
     * keeping all related code in one package.
     *
     * CHANGE LOCALITY:
     * Adding a new field to Deals? Change ONE package:
     *   features/deals/ — update entity, DTO, service, controller
     *
     * In layered architecture, you'd touch:
     *   entities/, repositories/, services/, controllers/, dtos/
     *   (5 different packages for 1 feature change!)
     */
    public Map<String, Object> demonstrateFeatureFirstOrganization() {
        log.info("[Vertical Slice] Demonstrating Feature-First Organization");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "Feature-First — all code for a feature lives in one package");

        results.put("feature_packages", Map.of(
                "features/deals/", "DealController + DealService + DealRepository + DealResponse + CreateDealRequest",
                "features/plans/", "PlanController + PlanService + PlanRepository + PlanResponse + CreatePlanRequest",
                "features/calculations/", "CalcController + CalcService + CalcRepository + CalcResponse + CalcRequest",
                "features/disputes/", "DisputeController + DisputeService + DisputeRepository + DisputeResponse"));

        results.put("change_locality", Map.of(
                "add_deal_field", "Change only features/deals/ (1 package)",
                "layered_equivalent", "Change entities/, repositories/, services/, controllers/, dtos/ (5 packages)"));

        // Show all features are operational
        var deals = dealService.getAllDeals();
        var plans = planService.getAllPlans();
        var calcs = calcService.getAllCalculations();
        results.put("deals_count", deals.size());
        results.put("plans_count", plans.size());
        results.put("calculations_count", calcs.size());

        return results;
    }

    // ============================================================
    // DEMO 2: Minimal Abstractions
    // ============================================================

    /**
     * Demonstrates the vertical slice principle of minimal abstractions.
     *
     * CONTRAST:
     *
     * Clean Architecture:
     *   DealController → DealUseCase (interface) → DealService (impl)
     *     → DealRepositoryPort (interface) → SpringDataDealRepository (impl)
     *   4 classes + 2 interfaces = 6 files for one feature
     *
     * Vertical Slice:
     *   DealController → DealService → DealRepository
     *   3 concrete classes, 0 interfaces = 3 files for one feature
     *
     * Vertical slice trades flexibility for simplicity.
     * No ports, no adapters, no use case interfaces — just code.
     */
    public Map<String, Object> demonstrateMinimalAbstractions() {
        log.info("[Vertical Slice] Demonstrating Minimal Abstractions");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "Minimal Abstractions — concrete classes, not interfaces");

        results.put("comparison", Map.of(
                "clean_architecture", Map.of(
                        "files_per_feature", "6+ (controller, use case interface, service, repo port, repo impl, DTOs)",
                        "interfaces", "DealUseCase, DealRepositoryPort",
                        "indirection", "High — every call goes through an interface"),
                "vertical_slice", Map.of(
                        "files_per_feature", "3-5 (controller, service, repository, request/response DTOs)",
                        "interfaces", "None — services are concrete classes",
                        "indirection", "Low — direct dependency injection")));

        // Demonstrate direct service usage — no ports or interfaces
        var deal = dealService.createDeal(
                new CreateDealRequest("Vertical Slice Demo", new BigDecimal("80000"), "usr-001"));

        results.put("service_call", "dealService.createDeal() — direct call, no interface layer");
        results.put("created_deal", deal.title() + " (ID: " + deal.id() + ")");

        results.put("trade_off", "Less flexible (can't swap implementations) but faster to develop and easier to understand");

        return results;
    }

    // ============================================================
    // DEMO 3: Cross-Feature Communication
    // ============================================================

    /**
     * Demonstrates how features communicate in vertical slice.
     *
     * Commission calculation needs data from both Deals and Plans.
     * In vertical slice, the calculation service directly depends
     * on the deal and plan repositories.
     *
     * This is simpler than Clean Architecture (where it would go
     * through ports) but creates coupling between features.
     */
    public Map<String, Object> demonstrateCrossFeatureCommunication() {
        log.info("[Vertical Slice] Demonstrating Cross-Feature Communication");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "Cross-Feature Communication — direct dependencies between slices");

        var deals = dealService.getAllDeals();
        var plans = planService.getAllPlans();

        if (deals.isEmpty() || plans.isEmpty()) {
            results.put("status", "SKIPPED");
            results.put("reason", "No deals or plans available");
            return results;
        }

        var deal = deals.get(0);
        var plan = plans.get(0);

        results.put("communication_pattern", Map.of(
                "calculation_depends_on", "DealRepository and CommissionPlanRepository — from other features",
                "how", "Direct injection — CommissionCalculationService injects both repositories",
                "trade_off", "Simple and fast, but creates coupling between features"));

        try {
            var calc = calcService.calculateCommission(
                    new CalculateCommissionRequest(deal.id(), plan.id()));

            results.put("status", "SUCCESS");
            results.put("deal", deal.title());
            results.put("plan", plan.name());
            results.put("base_commission", calc.baseCommission().toString());
            results.put("cross_feature_proof", "Calculation feature accessed Deal and Plan data");
        } catch (Exception e) {
            results.put("status", "ERROR");
            results.put("error", e.getMessage());
        }

        return results;
    }

    // ============================================================
    // DEMO 4: Rapid Feature Development
    // ============================================================

    /**
     * Demonstrates the rapid development benefit of vertical slices.
     *
     * ADDING A NEW FEATURE:
     * 1. Create a new package: features/newfeature/
     * 2. Add Entity, Repository, Service, Controller, DTOs
     * 3. Done — no interfaces, no ports, no abstract base classes
     *
     * TIME TO ADD FEATURE:
     * - Vertical Slice: ~5 files, all in one package
     * - Clean Architecture: ~10+ files across multiple packages
     * - DDD: ~8+ files plus aggregate design decisions
     */
    public Map<String, Object> demonstrateRapidDevelopment() {
        log.info("[Vertical Slice] Demonstrating Rapid Development");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "Rapid Development — minimum ceremony, maximum productivity");

        results.put("steps_to_add_feature", List.of(
                "1. Create features/newfeature/ package",
                "2. Add JPA Entity with @Entity",
                "3. Add Repository extending JpaRepository",
                "4. Add Service with @Service",
                "5. Add Controller with @RestController",
                "6. Add Request/Response DTOs (records)",
                "Done — no interfaces, no ports, no registration"));

        results.put("comparison_file_count", Map.of(
                "vertical_slice", "5-6 files in 1 package",
                "clean_architecture", "10+ files across 4+ packages",
                "ddd", "8+ files plus aggregate design decisions",
                "orthogonal", "8+ files (command, handler, query, handler, DTO, controller, ...)"));

        results.put("when_to_use_vertical_slice", Map.of(
                "good_for", "Small teams, MVPs, rapid prototyping, CRUD-heavy apps",
                "not_ideal_for", "Large teams needing strict boundaries, complex domain logic"));

        return results;
    }

    // ============================================================
    // DEMO 5: Full Feature Walkthrough
    // ============================================================

    /**
     * Demonstrates a complete vertical slice feature walkthrough:
     * create a deal, assign a plan, calculate commission.
     *
     * Every operation goes directly through the feature's service.
     * No intermediary layers, no abstract ports, no command buses.
     */
    public Map<String, Object> demonstrateFullFeatureWalkthrough() {
        log.info("[Vertical Slice] Demonstrating Full Feature Walkthrough");

        Map<String, Object> results = new LinkedHashMap<>();

        // Step 1: Create a deal (deals feature)
        var deal = dealService.createDeal(
                new CreateDealRequest("Full Walkthrough Deal", new BigDecimal("100000"), "usr-001"));
        results.put("step_1_create_deal", deal.title() + " ($" + deal.value() + ")");

        // Step 2: Get a plan (plans feature)
        var plans = planService.getAllPlans();
        if (plans.isEmpty()) {
            results.put("step_2_get_plan", "No plans available");
            return results;
        }
        var plan = plans.get(0);
        results.put("step_2_get_plan", plan.name());

        // Step 3: Calculate commission (calculations feature)
        try {
            var calc = calcService.calculateCommission(
                    new CalculateCommissionRequest(deal.id(), plan.id()));
            results.put("step_3_calculate", "$" + calc.baseCommission() + " base commission");
            results.put("step_3_gross", "$" + calc.grossCommission() + " gross");
        } catch (Exception e) {
            results.put("step_3_calculate", "Error: " + e.getMessage());
        }

        results.put("architecture_note", "Each step called a service directly — " +
                "no ports, no commands, no buses. Simple and effective.");

        return results;
    }

    // ============================================================
    // DEMO 6: MCP Server — AI Agent Integration
    // ============================================================

    /**
     * Demonstrates the Model Context Protocol (MCP) server integration.
     *
     * MCP allows AI agents (like Claude) to interact with the commission
     * calculator by exposing service methods as TOOLS. The AI agent can:
     * - Create deals, plans, disputes
     * - Query data by various criteria
     * - Calculate commissions
     * - Resolve disputes
     *
     * HOW IT WORKS:
     *
     *   ┌─────────────┐     MCP Protocol     ┌──────────────────┐
     *   │  AI Agent    │ ──── tool call ────→ │  MCP Server      │
     *   │  (Claude)    │ ←─── result ──────── │  (Spring Boot)   │
     *   └─────────────┘                       └────────┬─────────┘
     *                                                  │
     *                                         ┌────────▼─────────┐
     *                                         │ McpCommissionTools│
     *                                         │ (@Tool methods)   │
     *                                         └────────┬─────────┘
     *                                                  │
     *                                         ┌────────▼─────────┐
     *                                         │ Feature Services  │
     *                                         │ (DealService, etc)│
     *                                         └──────────────────┘
     *
     * ARCHITECTURE FIT:
     * MCP tools are a thin facade over existing feature services.
     * Vertical slice's simplicity makes this easy — the @Tool methods
     * just delegate to the same services that REST controllers use.
     *
     * TOOL CATEGORIES:
     * - Deal Management (7 tools): CRUD + query by status/rep
     * - Commission Plan Management (7 tools): CRUD + activate + add rules
     * - Dispute Management (8 tools): CRUD + resolve + escalate
     * - Commission Calculation (5 tools): calculate + query
     */
    public Map<String, Object> demonstrateMcpServer() {
        log.info("[Vertical Slice] Demonstrating MCP Server — AI Agent Integration");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "MCP Server — expose commission tools for AI agent integration");

        // Count @Tool annotated methods on McpCommissionTools
        long toolCount = Arrays.stream(McpCommissionTools.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Tool.class))
                .count();
        results.put("total_mcp_tools", toolCount);

        // List registered tool callbacks
        results.put("registered_tool_callbacks", toolCallbacks.size());

        // Group tools by category using @Tool name prefix
        Map<String, List<String>> toolsByCategory = new LinkedHashMap<>();
        for (Method method : McpCommissionTools.class.getDeclaredMethods()) {
            Tool toolAnnotation = method.getAnnotation(Tool.class);
            if (toolAnnotation != null) {
                String name = toolAnnotation.name();
                String category;
                if (name.toLowerCase().contains("deal")) {
                    category = "Deal Management";
                } else if (name.toLowerCase().contains("plan") || name.toLowerCase().contains("rule")) {
                    category = "Commission Plan Management";
                } else if (name.toLowerCase().contains("dispute")) {
                    category = "Dispute Management";
                } else if (name.toLowerCase().contains("calc") || name.toLowerCase().contains("commission")) {
                    category = "Commission Calculation";
                } else {
                    category = "Other";
                }
                toolsByCategory.computeIfAbsent(category, k -> new java.util.ArrayList<>()).add(name);
            }
        }
        results.put("tools_by_category", toolsByCategory);

        // Demonstrate that MCP tools use the same services as REST
        results.put("architecture_insight", Map.of(
                "mcp_facade", "McpCommissionTools wraps feature services with @Tool annotations",
                "same_services", "MCP tools call DealService, PlanService, etc. — same as REST controllers",
                "zero_duplication", "Business logic lives in feature services; MCP and REST are both thin adapters",
                "registration", "CommissionCalculatorApplication registers tools via ToolCallbacks.from(mcpTools)"));

        // Demonstrate MCP tool invocation (same as calling the service directly)
        var deals = mcpTools.getAllDeals();
        results.put("mcp_tool_invocation", "mcpTools.getAllDeals() returned " + deals.size() + " deals");

        results.put("ai_agent_workflow", List.of(
                "1. AI agent connects to MCP server",
                "2. Agent discovers available tools (27 tools)",
                "3. Agent calls tools as needed: createDeal, calculateCommission, etc.",
                "4. Tools delegate to feature services (same business logic as REST)",
                "5. Results returned to agent for reasoning"));

        return results;
    }
}
