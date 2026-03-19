package com.chapman.edu.commissions.architecture.ddd.processor;

import com.chapman.edu.commissions.architecture.ddd.application.calculation.CommissionCalculationApplicationService;
import com.chapman.edu.commissions.architecture.ddd.application.deal.DealApplicationService;
import com.chapman.edu.commissions.architecture.ddd.application.dto.CalculateCommissionRequest;
import com.chapman.edu.commissions.architecture.ddd.application.dto.CreateDealRequest;
import com.chapman.edu.commissions.architecture.ddd.application.plan.CommissionPlanApplicationService;
import com.chapman.edu.commissions.architecture.ddd.domain.calculation.CommissionCalculationService;
import com.chapman.edu.commissions.architecture.ddd.domain.deal.Deal;
import com.chapman.edu.commissions.architecture.ddd.domain.deal.DealRepository;
import com.chapman.edu.commissions.architecture.ddd.domain.plan.CommissionPlan;
import com.chapman.edu.commissions.architecture.ddd.domain.plan.CommissionPlanRepository;
import com.chapman.edu.commissions.architecture.ddd.domain.shared.AggregateRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ============================================================
 * PROCESSOR: Domain-Driven Design (DDD) Demonstration
 * ============================================================
 *
 * CONCEPT: Domain-Driven Design
 * ------------------------------------------------------------
 * DDD models software around the BUSINESS DOMAIN using a shared
 * vocabulary (Ubiquitous Language) between developers and domain
 * experts.
 *
 * KEY DDD BUILDING BLOCKS:
 *
 * ┌─────────────────────────────────────────────────────────┐
 * │  AGGREGATE ROOTS — Entry points to consistency boundaries│
 * │  Deal, CommissionPlan, CommissionCalculation, Dispute    │
 * │                                                         │
 * │  ENTITIES — Objects with identity                        │
 * │  DealProduct, CommissionRule, CommissionTier, Comment    │
 * │                                                         │
 * │  VALUE OBJECTS — Immutable, identity-less objects         │
 * │  BonusCalculation, AcceleratorCalculation, BonusRule     │
 * │                                                         │
 * │  DOMAIN SERVICES — Cross-aggregate business logic        │
 * │  CommissionCalculationService (stateless, static)       │
 * │                                                         │
 * │  REPOSITORIES — Persistence abstraction per aggregate    │
 * │  DealRepository, CommissionPlanRepository (domain-level) │
 * │                                                         │
 * │  APPLICATION SERVICES — Use case orchestration           │
 * │  DealApplicationService (coordinates, no business logic) │
 * └─────────────────────────────────────────────────────────┘
 *
 * KEY RULE: All changes to an aggregate go through its ROOT.
 * External code never touches DealProduct directly — it goes
 * through Deal (the aggregate root).
 */
@Service("dddProcessor")
public class DddProcessor {

    private static final Logger log = LoggerFactory.getLogger(DddProcessor.class);

    private final DealApplicationService dealAppService;
    private final CommissionCalculationApplicationService calcAppService;
    private final CommissionPlanApplicationService planAppService;
    private final DealRepository dealRepository;
    private final CommissionPlanRepository planRepository;

    public DddProcessor(DealApplicationService dealAppService,
                         CommissionCalculationApplicationService calcAppService,
                         CommissionPlanApplicationService planAppService,
                         DealRepository dealRepository,
                         CommissionPlanRepository planRepository) {
        this.dealAppService = dealAppService;
        this.calcAppService = calcAppService;
        this.planAppService = planAppService;
        this.dealRepository = dealRepository;
        this.planRepository = planRepository;
    }

    // ============================================================
    // DEMO 1: Aggregate Roots & Boundaries
    // ============================================================

    /**
     * Demonstrates the Aggregate Root pattern.
     *
     * An AGGREGATE is a cluster of domain objects treated as a single unit.
     * The AGGREGATE ROOT is the only entry point — all external access
     * goes through it.
     *
     * AGGREGATE BOUNDARIES IN THIS DOMAIN:
     *
     *   ┌─── Deal Aggregate ───────────────┐
     *   │  Deal (root)                      │
     *   │    └── DealProduct (entity)       │
     *   └──────────────────────────────────┘
     *
     *   ┌─── CommissionPlan Aggregate ─────┐
     *   │  CommissionPlan (root)            │
     *   │    ├── CommissionRule (entity)    │
     *   │    ├── CommissionTier (entity)    │
     *   │    └── BonusRule (value object)   │
     *   └──────────────────────────────────┘
     *
     * RULE: You never save a DealProduct directly — you save the Deal.
     */
    public Map<String, Object> demonstrateAggregateRoots() {
        log.info("[DDD] Demonstrating Aggregate Roots & Boundaries");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "Aggregate Root — single entry point to a consistency boundary");

        // Verify that Deal implements AggregateRoot
        results.put("deal_is_aggregate_root", AggregateRoot.class.isAssignableFrom(Deal.class));
        results.put("plan_is_aggregate_root", AggregateRoot.class.isAssignableFrom(CommissionPlan.class));

        results.put("aggregate_boundaries", Map.of(
                "Deal", "Root owns DealProducts — delete Deal cascades to products",
                "CommissionPlan", "Root owns Rules, Tiers, BonusRules — all accessed through plan",
                "CommissionCalculation", "Root owns BonusCalculations, AcceleratorCalculations",
                "Dispute", "Root owns DisputeComments — comments only via dispute"));

        results.put("aggregate_rules", Map.of(
                "rule_1", "External objects reference only the root",
                "rule_2", "All mutations go through the root",
                "rule_3", "Deleting root deletes the entire aggregate",
                "rule_4", "Root enforces all invariants"));

        // Show aggregates via application service
        var allDeals = dealAppService.getAllDeals();
        var allPlans = planAppService.getAllPlans();
        results.put("deal_aggregates_count", allDeals.size());
        results.put("plan_aggregates_count", allPlans.size());

        return results;
    }

    // ============================================================
    // DEMO 2: Domain Services — Cross-Aggregate Logic
    // ============================================================

    /**
     * Demonstrates Domain Services — stateless classes that contain
     * business logic spanning multiple aggregates.
     *
     * WHY A DOMAIN SERVICE?
     * Commission calculation requires BOTH a Deal (for value) and a
     * CommissionPlan (for rate). Neither aggregate should know about
     * the other's internals. The Domain Service coordinates between them.
     *
     * DOMAIN SERVICE RULES:
     * - Stateless (no fields, no dependencies)
     * - Operates only on domain objects passed as arguments
     * - Contains logic that doesn't belong to any single aggregate
     * - Lives in the DOMAIN layer (not application layer)
     *
     * COMPARE TO APPLICATION SERVICE:
     * - Application Service: orchestrates, loads aggregates, calls domain logic
     * - Domain Service: performs business logic that spans aggregates
     */
    public Map<String, Object> demonstrateDomainServices() {
        log.info("[DDD] Demonstrating Domain Services");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "Domain Service — stateless cross-aggregate business logic");
        results.put("service", "CommissionCalculationService");
        results.put("why_domain_service",
                "Calculation spans Deal and CommissionPlan aggregates — neither should know the other");

        // Use domain repository directly to get entities
        var deals = dealRepository.findAll();
        var plans = planRepository.findAll();

        if (!deals.isEmpty() && !plans.isEmpty()) {
            Deal deal = deals.get(0);
            CommissionPlan plan = plans.get(0);

            // Call the DOMAIN SERVICE directly — it's stateless and static
            BigDecimal baseCommission = CommissionCalculationService.calculateBaseCommission(deal, plan);

            results.put("deal_value", deal.getValue().toString());
            results.put("plan_name", plan.getName());
            results.put("calculated_base_commission", baseCommission.toString());
            results.put("domain_service_characteristics", Map.of(
                    "stateless", "No fields or injected dependencies",
                    "static_methods", "CommissionCalculationService.calculateBaseCommission(deal, plan)",
                    "pure_domain", "No Spring annotations, no framework imports",
                    "cross_aggregate", "Coordinates between Deal and CommissionPlan"));
        }

        return results;
    }

    // ============================================================
    // DEMO 3: Application Services vs Domain Services
    // ============================================================

    /**
     * Demonstrates the distinction between Application Services and
     * Domain Services in DDD.
     *
     * APPLICATION SERVICE (DealApplicationService):
     *   - Orchestrates use cases (load, validate, save)
     *   - Has dependencies (repositories)
     *   - Manages transactions
     *   - Maps between DTOs and domain objects
     *   - NO business logic
     *
     * DOMAIN SERVICE (CommissionCalculationService):
     *   - Pure business logic
     *   - Stateless, no dependencies
     *   - Operates on domain objects
     *   - Called BY application services
     */
    public Map<String, Object> demonstrateApplicationVsDomainService() {
        log.info("[DDD] Demonstrating Application Service vs Domain Service");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "Application Service orchestrates; Domain Service calculates");

        results.put("application_service", Map.of(
                "class", "DealApplicationService",
                "role", "Use case orchestration — load, validate, save",
                "has_dependencies", "Yes — DealRepository injected",
                "has_business_logic", "No — delegates to domain",
                "spring_managed", "Yes — @Service annotation",
                "example_method", "createDeal(CreateDealRequest) → maps DTO → saves via repo"));

        results.put("domain_service", Map.of(
                "class", "CommissionCalculationService",
                "role", "Business logic spanning aggregates",
                "has_dependencies", "No — private constructor, static methods only",
                "has_business_logic", "Yes — the actual calculation logic",
                "spring_managed", "No — plain Java class",
                "example_method", "calculateBaseCommission(Deal, CommissionPlan) → BigDecimal"));

        // Demonstrate the Application Service creating a deal
        try {
            var dealDto = dealAppService.createDeal(
                    new CreateDealRequest("DDD Demo Deal", new BigDecimal("50000"), "usr-001"));
            results.put("application_service_result", dealDto.title() + " (ID: " + dealDto.id() + ")");
        } catch (Exception e) {
            results.put("application_service_result", "Error: " + e.getMessage());
        }

        return results;
    }

    // ============================================================
    // DEMO 4: Repository per Aggregate
    // ============================================================

    /**
     * Demonstrates the DDD rule: ONE REPOSITORY PER AGGREGATE ROOT.
     *
     * In DDD, repositories are defined at the DOMAIN level as interfaces.
     * Infrastructure provides JPA implementations.
     *
     * IMPORTANT DISTINCTION:
     * - DDD Repository interface: lives in domain/ (no Spring imports)
     * - JPA implementation: lives in infrastructure/ (implements both)
     *
     *   domain/deal/DealRepository.java (interface — DOMAIN)
     *        ↑ implemented by
     *   infrastructure/persistence/JpaDealRepository.java (JPA — INFRASTRUCTURE)
     */
    public Map<String, Object> demonstrateRepositoryPerAggregate() {
        log.info("[DDD] Demonstrating Repository per Aggregate");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "One Repository per Aggregate Root — no repository for internal entities");

        results.put("repositories", Map.of(
                "DealRepository", "Persists Deal aggregate (including DealProducts)",
                "CommissionPlanRepository", "Persists Plan aggregate (including Rules, Tiers)",
                "CommissionCalculationRepository", "Persists Calculation aggregate",
                "DisputeRepository", "Persists Dispute aggregate (including Comments)",
                "UserRepository", "Persists User aggregate"));

        results.put("no_repository_for", Map.of(
                "DealProduct", "Accessed only through Deal aggregate root",
                "CommissionRule", "Accessed only through CommissionPlan root",
                "CommissionTier", "Accessed only through CommissionPlan root",
                "DisputeComment", "Accessed only through Dispute root"));

        // Domain repository is a plain Java interface — no Spring imports
        results.put("domain_repo_interface", "DealRepository in domain/deal/ — pure Java interface");
        results.put("jpa_implementation", "JpaDealRepository in infrastructure/persistence/ — extends JpaRepository + DealRepository");

        var deals = dealRepository.findAll();
        var plans = planRepository.findAll();
        results.put("deals_loaded_via_domain_repo", deals.size());
        results.put("plans_loaded_via_domain_repo", plans.size());

        return results;
    }

    // ============================================================
    // DEMO 5: Full DDD Flow — Calculate Commission
    // ============================================================

    /**
     * Demonstrates the complete DDD flow for commission calculation:
     *
     * 1. Controller receives CalculateCommissionRequest (DTO)
     * 2. APPLICATION SERVICE orchestrates:
     *    a. Load Deal aggregate via DealRepository
     *    b. Load CommissionPlan aggregate via CommissionPlanRepository
     *    c. Call DOMAIN SERVICE: CommissionCalculationService.calculateBaseCommission()
     *    d. Create CommissionCalculation aggregate with result
     *    e. Save via CommissionCalculationRepository
     * 3. Return CommissionCalculationDto (DTO)
     *
     * LAYERED ARCHITECTURE:
     *   Interfaces → Application → Domain ← Infrastructure
     */
    public Map<String, Object> demonstrateFullDddFlow() {
        log.info("[DDD] Demonstrating Full DDD Flow");

        Map<String, Object> results = new LinkedHashMap<>();

        var deals = dealAppService.getAllDeals();
        var plans = planAppService.getAllPlans();

        if (deals.isEmpty() || plans.isEmpty()) {
            results.put("status", "SKIPPED");
            results.put("reason", "No deals or plans available");
            return results;
        }

        var deal = deals.get(0);
        var plan = plans.get(0);

        results.put("step_1_interfaces", "Request received with dealId and planId");
        results.put("step_2_application", "ApplicationService loads aggregates from repositories");
        results.put("step_3_domain_service", "CommissionCalculationService.calculateBaseCommission(deal, plan)");
        results.put("step_4_aggregate", "New CommissionCalculation aggregate created with result");
        results.put("step_5_repository", "Aggregate saved via CommissionCalculationRepository");
        results.put("step_6_dto", "CommissionCalculationDto returned to caller");

        try {
            var calcResult = calcAppService.calculateCommission(
                    new CalculateCommissionRequest(deal.id(), plan.id()));

            results.put("status", "SUCCESS");
            results.put("deal_used", deal.title());
            results.put("plan_used", plan.name());
            results.put("base_commission", calcResult.baseCommission().toString());
            results.put("gross_commission", calcResult.grossCommission().toString());

            log.info("[DDD] Full DDD flow completed: ${} commission calculated", calcResult.grossCommission());
        } catch (Exception e) {
            results.put("status", "ERROR");
            results.put("error", e.getMessage());
        }

        return results;
    }
}
