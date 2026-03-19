package com.chapman.edu.commissions.architecture.cleanarchitecture.processor;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CalculateCommissionCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CreateDealCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in.CommissionCalculationUseCase;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in.CommissionPlanUseCase;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in.DealUseCase;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in.DisputeUseCase;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.DealRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ============================================================
 * PROCESSOR: Clean Architecture Demonstration
 * ============================================================
 *
 * CONCEPT: Clean Architecture (Hexagonal / Ports & Adapters)
 * ------------------------------------------------------------
 * Clean Architecture organizes code into concentric layers where
 * dependencies ALWAYS point inward. The domain model at the center
 * is completely independent of frameworks, databases, and UI.
 *
 * LAYER STRUCTURE:
 *
 * ┌──────────────────────────────────────────────────────┐
 * │  ADAPTERS (outermost)                                │
 * │  ├── IN:  Controllers (driving adapters)             │
 * │  └── OUT: JPA Repositories (driven adapters)         │
 * ├──────────────────────────────────────────────────────┤
 * │  APPLICATION (use cases)                             │
 * │  ├── Ports IN:  Use case interfaces                  │
 * │  ├── Ports OUT: Repository interfaces                │
 * │  ├── Services:  Use case implementations             │
 * │  └── DTOs:      Commands & Results                   │
 * ├──────────────────────────────────────────────────────┤
 * │  DOMAIN (innermost — no framework dependencies)      │
 * │  ├── Entities with business methods                  │
 * │  └── Domain exceptions                               │
 * └──────────────────────────────────────────────────────┘
 *
 * KEY PRINCIPLE: The Dependency Rule
 * Inner layers NEVER know about outer layers.
 * - Domain knows nothing about Spring, JPA, or REST
 * - Application defines PORT interfaces (not implementations)
 * - Adapters implement ports, bridging frameworks to domain
 *
 * THIS PROCESSOR DEMONSTRATES:
 * 1. Dependency Inversion — calling use case ports, not implementations
 * 2. Port/Adapter pattern — input ports for driving, output ports for driven
 * 3. Command objects — immutable DTOs with validation
 * 4. Layer isolation — each layer has clear responsibilities
 */
@Service("cleanArchProcessor")
public class CleanArchitectureProcessor {

    private static final Logger log = LoggerFactory.getLogger(CleanArchitectureProcessor.class);

    // ─────────────────────────────────────────────
    // CONCEPT: Dependency Inversion via Input Ports
    // ─────────────────────────────────────────────
    // This processor depends on USE CASE INTERFACES (ports),
    // NOT on concrete service implementations.
    // Spring injects the actual service at runtime.
    //
    //   Processor → DealUseCase (interface)
    //                    ↑
    //              DealService (implementation)
    //
    // This is the Dependency Inversion Principle (DIP) in action.

    private final DealUseCase dealUseCase;
    private final CommissionCalculationUseCase calculationUseCase;
    private final CommissionPlanUseCase planUseCase;
    private final DisputeUseCase disputeUseCase;

    public CleanArchitectureProcessor(DealUseCase dealUseCase,
                                       CommissionCalculationUseCase calculationUseCase,
                                       CommissionPlanUseCase planUseCase,
                                       DisputeUseCase disputeUseCase) {
        this.dealUseCase = dealUseCase;
        this.calculationUseCase = calculationUseCase;
        this.planUseCase = planUseCase;
        this.disputeUseCase = disputeUseCase;
    }

    // ============================================================
    // DEMO 1: Dependency Inversion via Input Ports
    // ============================================================

    /**
     * Demonstrates the core Clean Architecture principle: all interactions
     * go through INPUT PORTS (use case interfaces).
     *
     * FLOW:
     *   Processor → DealUseCase (port) → DealService (impl) → DealRepositoryPort (port) → JPA (adapter)
     *
     * The processor never touches DealService directly. If we swapped
     * DealService for a different implementation, this code would not change.
     */
    public Map<String, Object> demonstrateDependencyInversion() {
        log.info("[Clean Architecture] Demonstrating Dependency Inversion via Input Ports");

        Map<String, Object> results = new LinkedHashMap<>();

        // All calls go through the USE CASE interface (input port)
        var allDeals = dealUseCase.getAllDeals();
        var allPlans = planUseCase.getAllPlans();
        var allCalculations = calculationUseCase.getAllCalculations();

        results.put("concept", "Dependency Inversion — all calls go through Use Case Ports");
        results.put("deal_port_type", "DealUseCase (interface)");
        results.put("plan_port_type", "CommissionPlanUseCase (interface)");
        results.put("calculation_port_type", "CommissionCalculationUseCase (interface)");
        results.put("total_deals", allDeals.size());
        results.put("total_plans", allPlans.size());
        results.put("total_calculations", allCalculations.size());
        results.put("principle", "Inner layers define interfaces; outer layers implement them");

        log.info("[Clean Architecture] Retrieved {} deals, {} plans, {} calculations via use case ports",
                allDeals.size(), allPlans.size(), allCalculations.size());

        return results;
    }

    // ============================================================
    // DEMO 2: Command Objects with Validation
    // ============================================================

    /**
     * Demonstrates how Clean Architecture uses COMMAND objects (DTOs)
     * to cross layer boundaries. Commands are immutable records with
     * built-in validation.
     *
     * WHY COMMANDS?
     * - Immutable: cannot be modified after creation
     * - Self-validating: validate() ensures data integrity at the boundary
     * - Layer-independent: commands don't reference domain entities
     * - Testable: easy to construct with known values
     *
     * COMMAND FLOW:
     *   Controller creates CreateDealCommand
     *     → Command.validate() checks invariants
     *     → DealUseCase.createDeal(command)
     *     → DealService maps command → domain entity
     *     → Repository saves entity
     *     → DealResult returned (another DTO)
     */
    public Map<String, Object> demonstrateCommandPattern() {
        log.info("[Clean Architecture] Demonstrating Command Objects with Validation");

        Map<String, Object> results = new LinkedHashMap<>();

        // Step 1: Create a valid command
        CreateDealCommand validCommand = new CreateDealCommand(
                "Clean Arch Demo Deal", new BigDecimal("75000.00"), "usr-001");
        results.put("valid_command", Map.of(
                "title", validCommand.title(),
                "value", validCommand.value().toString(),
                "salesRepId", validCommand.salesRepId()));

        // Step 2: Demonstrate validation — the command validates itself
        try {
            validCommand.validate();
            results.put("validation_passed", true);
        } catch (IllegalArgumentException e) {
            results.put("validation_passed", false);
            results.put("validation_error", e.getMessage());
        }

        // Step 3: Demonstrate invalid command detection
        try {
            CreateDealCommand invalidCommand = new CreateDealCommand("", null, "usr-001");
            invalidCommand.validate();
            results.put("invalid_command_caught", false);
        } catch (IllegalArgumentException e) {
            results.put("invalid_command_caught", true);
            results.put("invalid_command_error", e.getMessage());
        }

        // Step 4: Execute through the use case port
        var createdDeal = dealUseCase.createDeal(validCommand);
        results.put("deal_created_via_port", true);
        results.put("created_deal_id", createdDeal.id());

        log.info("[Clean Architecture] Command validated and executed through DealUseCase port");

        return results;
    }

    // ============================================================
    // DEMO 3: Port & Adapter Pattern
    // ============================================================

    /**
     * Demonstrates the Port & Adapter (Hexagonal) pattern.
     *
     * PORTS are interfaces defined by the APPLICATION layer:
     *   - INPUT PORTS (driving): DealUseCase, CommissionCalculationUseCase
     *     → Define WHAT the application can do
     *     → Implemented by application services
     *     → Called by controllers (driving adapters)
     *
     *   - OUTPUT PORTS (driven): DealRepositoryPort, CommissionPlanRepositoryPort
     *     → Define WHAT the application needs
     *     → Implemented by infrastructure (JPA repositories)
     *     → Called by application services
     *
     * ADAPTERS are concrete implementations:
     *   - DRIVING ADAPTERS: REST controllers, CLI, test harness (this processor!)
     *     → Convert external input → use case calls
     *   - DRIVEN ADAPTERS: JPA repositories, external APIs, file systems
     *     → Convert use case needs → infrastructure calls
     *
     * THIS PROCESSOR IS A DRIVING ADAPTER — it drives the application
     * through input ports, just like a REST controller would.
     */
    public Map<String, Object> demonstratePortAdapterPattern() {
        log.info("[Clean Architecture] Demonstrating Port & Adapter Pattern");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "Hexagonal Architecture — Ports & Adapters");

        // Input ports (driving) — we call these
        results.put("input_ports", Map.of(
                "DealUseCase", "Defines deal operations (create, read, update, delete)",
                "CommissionCalculationUseCase", "Defines calculation operations",
                "CommissionPlanUseCase", "Defines plan operations",
                "DisputeUseCase", "Defines dispute operations"));

        // Output ports (driven) — services call these
        results.put("output_ports", Map.of(
                "DealRepositoryPort", "Abstracts deal persistence",
                "CommissionCalculationRepositoryPort", "Abstracts calculation persistence",
                "CommissionPlanRepositoryPort", "Abstracts plan persistence",
                "UserRepositoryPort", "Abstracts user persistence"));

        // Driving adapters — things that drive the application
        results.put("driving_adapters", Map.of(
                "DealController", "REST adapter at /api/clean/deals",
                "CleanArchitectureProcessor", "This processor — also a driving adapter!"));

        // Driven adapters — things the application drives
        results.put("driven_adapters", Map.of(
                "SpringDataDealRepository", "JPA adapter implementing DealRepositoryPort",
                "SpringDataCommissionPlanRepository", "JPA adapter implementing CommissionPlanRepositoryPort"));

        // Demonstrate by using multiple ports
        var deals = dealUseCase.getAllDeals();
        var plans = planUseCase.getAllPlans();
        results.put("proof_of_decoupling",
                "This processor calls DealUseCase and CommissionPlanUseCase without knowing " +
                "that DealService and CommissionPlanService implement them");
        results.put("deals_via_port", deals.size());
        results.put("plans_via_port", plans.size());

        return results;
    }

    // ============================================================
    // DEMO 4: Full Use Case Flow — Calculate Commission
    // ============================================================

    /**
     * Demonstrates the full Clean Architecture flow for calculating
     * a commission, crossing all layers:
     *
     * 1. ADAPTER (this processor) → creates CalculateCommissionCommand
     * 2. APPLICATION (CommissionCalculationUseCase) → validates and orchestrates
     * 3. SERVICE (CommissionCalculationService) → loads deal + plan via output ports
     * 4. DOMAIN (CommissionCalculation entity) → performs business logic
     * 5. INFRASTRUCTURE (JPA repository) → persists the result
     *
     * LAYER CROSSING:
     *   Adapter → [Input Port] → Application → [Output Port] → Infrastructure
     *                                ↕
     *                             Domain
     */
    public Map<String, Object> demonstrateFullUseCaseFlow() {
        log.info("[Clean Architecture] Demonstrating Full Use Case Flow");

        Map<String, Object> results = new LinkedHashMap<>();

        // Get existing deals and plans to use
        var deals = dealUseCase.getAllDeals();
        var plans = planUseCase.getAllPlans();

        if (deals.isEmpty() || plans.isEmpty()) {
            results.put("status", "SKIPPED");
            results.put("reason", "No deals or plans available for calculation");
            return results;
        }

        var deal = deals.get(0);
        var plan = plans.get(0);

        results.put("step_1_adapter", "Processor creates CalculateCommissionCommand");
        results.put("step_2_port", "Command sent through CommissionCalculationUseCase (input port)");
        results.put("step_3_service", "Service loads Deal and Plan through repository ports (output ports)");
        results.put("step_4_domain", "CommissionCalculation entity performs business logic");
        results.put("step_5_infrastructure", "JPA repository persists the result");

        // Execute the use case
        try {
            CalculateCommissionCommand command = new CalculateCommissionCommand(deal.id(), plan.id());
            var calculation = calculationUseCase.calculateCommission(command);

            results.put("status", "SUCCESS");
            results.put("deal_used", deal.title());
            results.put("plan_used", plan.name());
            results.put("calculation_id", calculation.id());
            results.put("base_commission", calculation.baseCommission().toString());
            results.put("gross_commission", calculation.grossCommission().toString());
            results.put("net_commission", calculation.netCommission().toString());

            log.info("[Clean Architecture] Commission calculated: ${} on deal '{}' using plan '{}'",
                    calculation.grossCommission(), deal.title(), plan.name());
        } catch (Exception e) {
            results.put("status", "ERROR");
            results.put("error", e.getMessage());
            log.warn("[Clean Architecture] Calculation failed: {}", e.getMessage());
        }

        return results;
    }

    // ============================================================
    // DEMO 5: Layer Isolation Verification
    // ============================================================

    /**
     * Demonstrates that each layer is properly isolated by showing
     * how the processor (an adapter) can ONLY interact with the
     * application through well-defined ports.
     *
     * KEY OBSERVATIONS:
     * - Processor cannot directly access repositories (output ports)
     * - Processor cannot directly instantiate domain entities
     * - Processor communicates via Commands (in) and Results (out)
     * - Each layer has a single, clear responsibility
     */
    public Map<String, Object> demonstrateLayerIsolation() {
        log.info("[Clean Architecture] Demonstrating Layer Isolation");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("layer_responsibilities", Map.of(
                "domain", "Business rules, entities with behavior, domain exceptions",
                "application", "Use case orchestration, port definitions, command/result DTOs",
                "adapter_in", "HTTP translation — maps REST requests to commands",
                "adapter_out", "Persistence translation — maps entities to JPA operations",
                "infrastructure", "Framework config, security, exception handling"));

        results.put("what_processor_CAN_do", Map.of(
                "call_input_ports", "dealUseCase.getAllDeals(), calculationUseCase.calculateCommission()",
                "create_commands", "new CreateDealCommand(...), new CalculateCommissionCommand(...)",
                "receive_results", "DealResult, CalculationResult (immutable DTOs)"));

        results.put("what_processor_CANNOT_do", Map.of(
                "access_repositories", "DealRepositoryPort is not injected — only services use it",
                "modify_entities", "Domain entities are not exposed — only Results are returned",
                "bypass_validation", "Commands validate() before execution"));

        // Demonstrate: we receive Results (DTOs), not Entities
        var deals = dealUseCase.getAllDeals();
        if (!deals.isEmpty()) {
            var deal = deals.get(0);
            results.put("result_type", deal.getClass().getSimpleName());
            results.put("result_is_record", deal.getClass().isRecord());
            results.put("result_proof", "DealResult is an immutable record — not a mutable JPA entity");
        }

        return results;
    }
}
