package com.chapman.edu.commissions.architecture.microservice.processor;

import com.chapman.edu.commissions.architecture.microservice.gateway.ServiceRegistry;
import com.chapman.edu.commissions.architecture.microservice.common.dto.CreateDealRequest;
import com.chapman.edu.commissions.architecture.microservice.dealservice.DealService;
import com.chapman.edu.commissions.architecture.microservice.planservice.PlanService;
import com.chapman.edu.commissions.architecture.microservice.calculationservice.CalculationService;
import com.chapman.edu.commissions.architecture.microservice.calculationservice.client.DealServiceClient;
import com.chapman.edu.commissions.architecture.microservice.calculationservice.client.PlanServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * PROCESSOR: Microservice Architecture Demonstration
 * ============================================================
 *
 * CONCEPT: Microservice Architecture
 * ------------------------------------------------------------
 * The application is split into INDEPENDENT SERVICES, each with
 * its own database, port, and deployment lifecycle. Services
 * communicate via REST/HTTP, not in-process method calls.
 *
 * SERVICE TOPOLOGY:
 *
 *   ┌─────────────────────┐
 *   │  API Gateway (8090)  │ ← Client entry point
 *   │  Routes /api/ms/**  │
 *   └────────┬────────────┘
 *            ├──→ Deal Service (8091)        — jdbc:h2:mem:dealservicedb
 *            ├──→ Plan Service (8092)        — jdbc:h2:mem:planservicedb
 *            ├──→ Calculation Service (8093) — jdbc:h2:mem:calcservicedb
 *            └──→ Dispute Service (8094)     — jdbc:h2:mem:disputeservicedb
 *
 * INTER-SERVICE COMMUNICATION:
 *   CalculationService needs Deal and Plan data
 *     → Calls DealServiceClient.getDeal(id)  (HTTP GET to port 8091)
 *     → Calls PlanServiceClient.getPlan(id)  (HTTP GET to port 8092)
 *
 * TRADE-OFFS:
 *   + Independent deployability, scaling, fault isolation
 *   - Network latency, data consistency, operational complexity
 *
 * NOTE: In this educational monolith, all services share one JVM.
 * In production, each would be a separate deployable process.
 */
@Service("microserviceProcessor")
public class MicroserviceProcessor {

    private static final Logger log = LoggerFactory.getLogger(MicroserviceProcessor.class);

    private final ServiceRegistry serviceRegistry;
    private final DealService dealService;
    private final PlanService planService;
    private final CalculationService calculationService;

    public MicroserviceProcessor(ServiceRegistry serviceRegistry,
                                  DealService dealService,
                                  PlanService planService,
                                  CalculationService calculationService) {
        this.serviceRegistry = serviceRegistry;
        this.dealService = dealService;
        this.planService = planService;
        this.calculationService = calculationService;
    }

    // ============================================================
    // DEMO 1: Service Topology & Registry
    // ============================================================

    /**
     * Demonstrates the microservice service registry and topology.
     *
     * The ServiceRegistry resolves service URLs from configuration.
     * In production, this would use Eureka, Consul, or Kubernetes DNS.
     *
     * Each service:
     * - Has its own @SpringBootApplication main class
     * - Runs on a dedicated port
     * - Has its own H2 database (database-per-service pattern)
     * - Exposes REST endpoints under its own prefix
     */
    public Map<String, Object> demonstrateServiceTopology() {
        log.info("[Microservice] Demonstrating Service Topology");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "Microservice Topology — independent services with own DB and port");

        results.put("services", Map.of(
                "Gateway", Map.of(
                        "url", "http://localhost:8090",
                        "database", "none — stateless proxy",
                        "role", "Routes /api/ms/** to appropriate service"),
                "DealService", Map.of(
                        "url", serviceRegistry.getDealServiceUrl(),
                        "database", "jdbc:h2:mem:dealservicedb",
                        "endpoints", "/api/deals/**"),
                "PlanService", Map.of(
                        "url", serviceRegistry.getPlanServiceUrl(),
                        "database", "jdbc:h2:mem:planservicedb",
                        "endpoints", "/api/plans/**"),
                "CalculationService", Map.of(
                        "url", serviceRegistry.getCalculationServiceUrl(),
                        "database", "jdbc:h2:mem:calcservicedb",
                        "endpoints", "/api/calculations/**"),
                "DisputeService", Map.of(
                        "url", serviceRegistry.getDisputeServiceUrl(),
                        "database", "jdbc:h2:mem:disputeservicedb",
                        "endpoints", "/api/disputes/**")));

        results.put("database_per_service", "Each service owns its data — no shared database");
        results.put("service_discovery", "ServiceRegistry resolves URLs from config properties");

        return results;
    }

    // ============================================================
    // DEMO 2: Independent Service Operations
    // ============================================================

    /**
     * Demonstrates that each service operates independently.
     * The Deal Service can create, read, and manage deals without
     * any knowledge of the Plan Service or Calculation Service.
     */
    public Map<String, Object> demonstrateIndependentServices() {
        log.info("[Microservice] Demonstrating Independent Service Operations");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "Service Independence — each service operates autonomously");

        // Deal Service operations
        var deal = dealService.createDeal(
                new CreateDealRequest("Microservice Demo Deal", new BigDecimal("120000"), "usr-001"));
        var allDeals = dealService.getAllDeals();
        results.put("deal_service", Map.of(
                "created", deal.title(),
                "total_deals", allDeals.size(),
                "independent", "DealService has no reference to PlanService or CalculationService"));

        // Plan Service operations
        var allPlans = planService.getAllPlans();
        results.put("plan_service", Map.of(
                "total_plans", allPlans.size(),
                "independent", "PlanService has no reference to DealService"));

        results.put("independence_proof", "Each service has its own repository, " +
                "own database, and own @SpringBootApplication entry point");

        return results;
    }

    // ============================================================
    // DEMO 3: Inter-Service Communication
    // ============================================================

    /**
     * Demonstrates REST-based inter-service communication.
     *
     * When CalculationService needs Deal or Plan data, it calls
     * other services via HTTP using REST clients:
     *
     *   CalculationService
     *     → DealServiceClient.getDeal(dealId)   [HTTP GET to :8091]
     *     → PlanServiceClient.getPlan(planId)    [HTTP GET to :8092]
     *
     * This is different from a monolith where CalculationService
     * would directly call DealRepository (in-process, no network).
     *
     * TRADE-OFF:
     * + Services can be deployed independently
     * - Every cross-service call adds network latency
     */
    public Map<String, Object> demonstrateInterServiceCommunication() {
        log.info("[Microservice] Demonstrating Inter-Service Communication");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "Inter-Service Communication via REST clients");

        results.put("communication_pattern", Map.of(
                "monolith", "CalculationService → DealRepository.findById() [in-process]",
                "microservice", "CalculationService → DealServiceClient.getDeal() [HTTP call]"));

        results.put("rest_clients", Map.of(
                "DealServiceClient", "HTTP client calling DealService at " + serviceRegistry.getDealServiceUrl(),
                "PlanServiceClient", "HTTP client calling PlanService at " + serviceRegistry.getPlanServiceUrl()));

        results.put("data_flow", List.of(
                "1. Client sends CalculateCommissionRequest to CalculationService",
                "2. CalculationService calls DealServiceClient.getDeal(dealId)",
                "3. DealServiceClient makes HTTP GET to DealService",
                "4. CalculationService calls PlanServiceClient.getPlan(planId)",
                "5. PlanServiceClient makes HTTP GET to PlanService",
                "6. CalculationService computes commission with both DTOs",
                "7. Result saved to CalculationService's own database"));

        results.put("trade_offs", Map.of(
                "advantage", "Services deploy independently, scale independently, fail independently",
                "disadvantage", "Network latency, partial failure handling, data consistency"));

        return results;
    }

    // ============================================================
    // DEMO 4: API Gateway Pattern
    // ============================================================

    /**
     * Demonstrates the API Gateway — a single entry point that
     * routes client requests to the appropriate microservice.
     *
     * GATEWAY ROUTING:
     *   /api/ms/deals/**        → DealService (8091)
     *   /api/ms/plans/**        → PlanService (8092)
     *   /api/ms/calculations/** → CalculationService (8093)
     *   /api/ms/disputes/**     → DisputeService (8094)
     *
     * WHY A GATEWAY?
     * - Single URL for clients (not 5 different ports)
     * - Cross-cutting: auth, rate limiting, logging
     * - Service discovery abstraction
     */
    public Map<String, Object> demonstrateApiGateway() {
        log.info("[Microservice] Demonstrating API Gateway Pattern");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "API Gateway — single entry point for all clients");

        results.put("routing_rules", Map.of(
                "/api/ms/deals/**", "→ DealService at " + serviceRegistry.getDealServiceUrl(),
                "/api/ms/plans/**", "→ PlanService at " + serviceRegistry.getPlanServiceUrl(),
                "/api/ms/calculations/**", "→ CalculationService at " + serviceRegistry.getCalculationServiceUrl(),
                "/api/ms/disputes/**", "→ DisputeService at " + serviceRegistry.getDisputeServiceUrl()));

        results.put("gateway_responsibilities", Map.of(
                "routing", "Routes requests to correct service based on URL path",
                "proxy", "Forwards request body, method, and headers",
                "health", "Provides /api/ms/health endpoint for monitoring",
                "abstraction", "Clients use one URL; gateway handles service discovery"));

        results.put("production_alternatives", Map.of(
                "Spring Cloud Gateway", "Reactive, production-grade API gateway",
                "Kong", "Open-source API gateway with plugins",
                "AWS API Gateway", "Managed cloud gateway",
                "Kubernetes Ingress", "K8s-native routing"));

        return results;
    }

    // ============================================================
    // DEMO 5: Database per Service
    // ============================================================

    /**
     * Demonstrates the Database-per-Service pattern.
     *
     * Each microservice has its own database. No service directly
     * accesses another service's database.
     *
     * CONSEQUENCE: You cannot do cross-service JOINs. If CalculationService
     * needs Deal data, it must call DealService's REST API.
     *
     * DATA CONSISTENCY:
     * - Monolith: ACID transactions across all tables
     * - Microservices: Eventual consistency via events or sagas
     */
    public Map<String, Object> demonstrateDatabasePerService() {
        log.info("[Microservice] Demonstrating Database per Service");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "Database per Service — each service owns its data exclusively");

        results.put("databases", Map.of(
                "DealService", "jdbc:h2:mem:dealservicedb — deals, deal_products tables",
                "PlanService", "jdbc:h2:mem:planservicedb — plans, rules, tiers tables",
                "CalculationService", "jdbc:h2:mem:calcservicedb — calculations table",
                "DisputeService", "jdbc:h2:mem:disputeservicedb — disputes, comments tables"));

        results.put("rules", Map.of(
                "no_shared_database", "Services NEVER read/write another service's DB",
                "no_cross_service_joins", "Cannot JOIN deals with plans — different databases",
                "data_via_api", "Need deal info? Call DealService API, not its database"));

        results.put("consistency_models", Map.of(
                "monolith", "Strong consistency — single DB, ACID transactions",
                "microservice", "Eventual consistency — each service is consistent, " +
                        "cross-service data may be briefly stale"));

        // Verify services have independent data
        var deals = dealService.getAllDeals();
        var plans = planService.getAllPlans();
        results.put("deal_service_data", deals.size() + " deals in dealservicedb");
        results.put("plan_service_data", plans.size() + " plans in planservicedb");

        return results;
    }
}
