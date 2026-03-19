package com.chapman.edu.commissions.architecture.orthogonal.processor;

import com.chapman.edu.commissions.architecture.orthogonal.aspects.auditing.AuditLogRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.commands.CreateDealCommand;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.queries.GetAllDealsQuery;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.queries.GetAllPlansQuery;
import com.chapman.edu.commissions.architecture.orthogonal.features.calculations.queries.GetAllCalculationsQuery;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.CommandBus;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.QueryBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * PROCESSOR: Orthogonal Architecture (CQRS + AOP) Demonstration
 * ============================================================
 *
 * CONCEPT: Orthogonal Architecture
 * ------------------------------------------------------------
 * Operations are modeled as COMMAND and QUERY objects. Each has
 * exactly ONE handler (single responsibility). Cross-cutting
 * concerns are applied via AOP aspects that are INDEPENDENT
 * DIMENSIONS — orthogonal to the business logic.
 *
 * CQRS PATTERN:
 *
 *   ┌──────────────┐          ┌──────────────────┐
 *   │  COMMAND      │  ──→    │  CommandHandler   │  ──→  Write
 *   │  (write ops)  │         │  (one per command) │
 *   └──────────────┘          └──────────────────┘
 *
 *   ┌──────────────┐          ┌──────────────────┐
 *   │  QUERY        │  ──→    │  QueryHandler     │  ──→  Read
 *   │  (read ops)   │         │  (one per query)   │
 *   └──────────────┘          └──────────────────┘
 *
 * AOP ASPECT CHAIN (orthogonal dimensions):
 *
 *   Request → @Order(1) Logging
 *           → @Order(2) Validation
 *           → @Order(3) Auditing
 *           → @Order(4) Performance
 *           → Handler.handle() ← PURE BUSINESS LOGIC
 *           → Response bubbles back
 *
 * KEY INSIGHT: Each aspect is independent. Adding a new aspect
 * (e.g., caching) automatically applies to ALL handlers.
 */
@Service("orthogonalProcessor")
public class OrthogonalProcessor {

    private static final Logger log = LoggerFactory.getLogger(OrthogonalProcessor.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final AuditLogRepository auditLogRepository;

    public OrthogonalProcessor(CommandBus commandBus,
                                QueryBus queryBus,
                                AuditLogRepository auditLogRepository) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.auditLogRepository = auditLogRepository;
    }

    // ============================================================
    // DEMO 1: Command/Query Separation (CQRS)
    // ============================================================

    /**
     * Demonstrates CQRS — Commands and Queries are first-class objects
     * dispatched through separate buses.
     *
     * COMMANDS (write operations):
     *   CreateDealCommand → CreateDealHandler → saves to DB
     *   UpdateDealCommand → UpdateDealHandler → updates DB
     *   DeleteDealCommand → DeleteDealHandler → deletes from DB
     *
     * QUERIES (read operations):
     *   GetDealQuery      → GetDealHandler    → reads from DB
     *   GetAllDealsQuery  → GetAllDealsHandler → reads from DB
     *
     * WHY SEPARATE?
     * - Reads and writes have different scaling needs
     * - Commands can be validated, audited, replayed
     * - Queries can be cached, optimized independently
     */
    public Map<String, Object> demonstrateCqrs() {
        log.info("[Orthogonal] Demonstrating CQRS — Command/Query Separation");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "CQRS — Commands modify state; Queries read state");

        // Dispatch a COMMAND through the CommandBus
        var createdDeal = commandBus.dispatch(
                new CreateDealCommand("CQRS Demo Deal", new BigDecimal("95000"), "usr-001"));
        results.put("command_dispatched", "CreateDealCommand → CommandBus → CreateDealHandler");
        results.put("command_result", createdDeal.toString());

        // Dispatch a QUERY through the QueryBus
        var allDeals = queryBus.dispatch(new GetAllDealsQuery(null, null));
        results.put("query_dispatched", "GetAllDealsQuery → QueryBus → GetAllDealsHandler");
        results.put("query_result_count", allDeals.size());

        results.put("cqrs_benefits", Map.of(
                "independent_scaling", "Read and write paths can scale differently",
                "single_responsibility", "Each handler does exactly ONE thing",
                "bus_pattern", "PipelineBus auto-discovers handlers via @PostConstruct"));

        return results;
    }

    // ============================================================
    // DEMO 2: Pipeline Bus — Auto-Discovery
    // ============================================================

    /**
     * Demonstrates the PipelineBus — a mediator that auto-discovers
     * all CommandHandler and QueryHandler beans at startup.
     *
     * HOW IT WORKS:
     * 1. @PostConstruct scans ApplicationContext for all handlers
     * 2. Maps each handler to its Command/Query type
     * 3. dispatch(command) looks up the right handler and calls handle()
     *
     * ADDING A NEW FEATURE:
     * 1. Create a new Command (e.g., ArchiveDealCommand)
     * 2. Create its Handler (e.g., ArchiveDealHandler implements CommandHandler)
     * 3. PipelineBus discovers it automatically — no registration needed
     */
    public Map<String, Object> demonstratePipelineBus() {
        log.info("[Orthogonal] Demonstrating Pipeline Bus");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "PipelineBus — mediator that auto-discovers and routes handlers");

        // Show that different types route to different handlers
        results.put("command_routing", Map.of(
                "CreateDealCommand", "→ CreateDealHandler",
                "UpdateDealCommand", "→ UpdateDealHandler",
                "DeleteDealCommand", "→ DeleteDealHandler",
                "CreatePlanCommand", "→ CreatePlanHandler",
                "CalculateCommissionCommand", "→ CalculateCommissionHandler"));

        results.put("query_routing", Map.of(
                "GetDealQuery", "→ GetDealHandler",
                "GetAllDealsQuery", "→ GetAllDealsHandler",
                "GetPlanQuery", "→ GetPlanHandler",
                "GetAllPlansQuery", "→ GetAllPlansHandler"));

        results.put("auto_discovery", "PipelineBus uses @PostConstruct + ApplicationContext " +
                "to find all CommandHandler/QueryHandler beans at startup");
        results.put("adding_new_handler", "Just create a @Component implementing CommandHandler — " +
                "PipelineBus picks it up automatically");

        // Prove multiple query types work through same bus
        var deals = queryBus.dispatch(new GetAllDealsQuery(null, null));
        var plans = queryBus.dispatch(GetAllPlansQuery.all());
        results.put("deals_via_bus", deals.size());
        results.put("plans_via_bus", plans.size());

        return results;
    }

    // ============================================================
    // DEMO 3: AOP Aspect Chain — Orthogonal Concerns
    // ============================================================

    /**
     * Demonstrates how AOP aspects form orthogonal dimensions
     * that automatically wrap ALL handlers.
     *
     * ASPECT CHAIN (in execution order):
     *
     *   @Order(1) LoggingAspect
     *     → Logs handler class, command type, and execution time
     *
     *   @Order(2) ValidationAspect
     *     → Auto-calls command.validate() if the method exists
     *     → Catches IllegalArgumentException for bad inputs
     *
     *   @Order(3) AuditingAspect
     *     → Persists every command to audit_log table
     *     → Records success/failure, handler name, timestamp
     *
     *   @Order(4) PerformanceAspect
     *     → Measures handler execution time
     *     → Warns if > 500ms (configurable threshold)
     *
     * KEY INSIGHT: These aspects are ORTHOGONAL — each is independent.
     * The handler contains ZERO cross-cutting code. Pure business logic.
     */
    public Map<String, Object> demonstrateAopAspectChain() {
        log.info("[Orthogonal] Demonstrating AOP Aspect Chain");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "AOP Aspects — orthogonal cross-cutting concerns");

        results.put("aspect_chain", List.of(
                "@Order(1) LoggingAspect — logs handler execution and timing",
                "@Order(2) ValidationAspect — auto-calls command.validate()",
                "@Order(3) AuditingAspect — persists command to audit_log",
                "@Order(4) PerformanceAspect — flags slow handlers (>500ms)"));

        results.put("orthogonal_means", "Each aspect is independent — " +
                "adding LoggingAspect doesn't affect ValidationAspect");

        results.put("handler_stays_clean", "CreateDealHandler.handle() contains ONLY " +
                "business logic — no logging, no validation, no auditing code");

        // Dispatch a command and observe audit log growth
        long auditBefore = auditLogRepository.count();

        commandBus.dispatch(
                new CreateDealCommand("Aspect Demo Deal", new BigDecimal("60000"), "usr-001"));

        long auditAfter = auditLogRepository.count();

        results.put("audit_entries_before", auditBefore);
        results.put("audit_entries_after", auditAfter);
        results.put("new_audit_entries", auditAfter - auditBefore);
        results.put("proof", "AuditingAspect automatically recorded the command " +
                "— handler code didn't mention auditing");

        return results;
    }

    // ============================================================
    // DEMO 4: Automatic Audit Trail
    // ============================================================

    /**
     * Demonstrates the automatic audit trail created by the
     * AuditingAspect. Every command is recorded with:
     * - Operation name (command class name)
     * - Handler name
     * - Status (SUCCESS or FAILURE)
     * - Timestamp
     * - Serialized command payload
     */
    public Map<String, Object> demonstrateAuditTrail() {
        log.info("[Orthogonal] Demonstrating Automatic Audit Trail");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "Automatic Audit Trail — AuditingAspect records every command");

        var allLogs = auditLogRepository.findAllByOrderByOccurredAtDesc();
        results.put("total_audit_entries", allLogs.size());

        // Show recent entries
        var recentLogs = allLogs.stream()
                .limit(5)
                .map(entry -> Map.of(
                        "operation", entry.getOperation(),
                        "handler", entry.getHandlerName(),
                        "status", entry.getStatus(),
                        "when", entry.getOccurredAt().toString()))
                .toList();
        results.put("recent_audit_entries", recentLogs);

        // Show entries by status
        var successCount = auditLogRepository.findByStatusOrderByOccurredAtDesc("SUCCESS").size();
        var failureCount = auditLogRepository.findByStatusOrderByOccurredAtDesc("FAILURE").size();
        results.put("successful_commands", successCount);
        results.put("failed_commands", failureCount);

        results.put("audit_properties", Map.of(
                "automatic", "No code changes needed — aspect handles it",
                "includes_payload", "Full command serialized as JSON",
                "queryable", "REST API at /api/orthogonal/audit-log"));

        return results;
    }

    // ============================================================
    // DEMO 5: Command Validation via Aspect
    // ============================================================

    /**
     * Demonstrates how the ValidationAspect automatically validates
     * commands before they reach the handler.
     *
     * Commands implement validate() which throws IllegalArgumentException
     * for invalid inputs. The ValidationAspect calls validate()
     * via reflection before the handler executes.
     *
     * FLOW:
     *   Command dispatched → ValidationAspect.validate() →
     *     Valid:   proceed to handler
     *     Invalid: throw before handler runs (no DB call)
     */
    public Map<String, Object> demonstrateCommandValidation() {
        log.info("[Orthogonal] Demonstrating Command Validation via Aspect");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "ValidationAspect auto-calls command.validate() before handler");

        // Valid command
        try {
            var result = commandBus.dispatch(
                    new CreateDealCommand("Valid Deal", new BigDecimal("50000"), "usr-001"));
            results.put("valid_command_result", "SUCCESS — handler executed");
        } catch (Exception e) {
            results.put("valid_command_result", "ERROR — " + e.getMessage());
        }

        // Invalid command — empty title
        try {
            commandBus.dispatch(new CreateDealCommand("", new BigDecimal("50000"), "usr-001"));
            results.put("invalid_command_result", "ERROR — should have been caught");
        } catch (Exception e) {
            results.put("invalid_command_caught", true);
            results.put("invalid_command_error", e.getMessage());
        }

        results.put("validation_flow", List.of(
                "1. CommandBus.dispatch(command)",
                "2. ValidationAspect intercepts via @Around",
                "3. Calls command.validate() via reflection",
                "4. If valid → proceeds to handler",
                "5. If invalid → throws BEFORE handler runs (no DB call)"));

        return results;
    }
}
