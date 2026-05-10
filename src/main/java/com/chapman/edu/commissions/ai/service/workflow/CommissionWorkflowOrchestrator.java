package com.chapman.edu.commissions.ai.service.workflow;

import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * SPRING AI SERVICE: CommissionWorkflowOrchestrator
 * ============================================================
 *
 * CONCEPT: Agentic Workflow Pattern (Orchestrator)
 * ------------------------------------------------------------
 * The Agentic Workflow Pattern coordinates MULTIPLE specialized
 * AI agents to solve a complex task that no single agent could
 * handle well alone. An ORCHESTRATOR manages the agent pipeline,
 * passing shared state between agents and making routing decisions.
 *
 *   ┌─────────────────────────────────────────────────────────┐
 *   │                    ORCHESTRATOR                         │
 *   │                                                         │
 *   │  User Request: "Review Alice's commissions"             │
 *   │       │                                                 │
 *   │       ▼                                                 │
 *   │  ┌──────────────┐                                       │
 *   │  │ Gathering    │ → Collects deals, calcs, plans        │
 *   │  │ Agent        │   Writes: gathered_data               │
 *   │  └──────┬───────┘                                       │
 *   │         │ state                                         │
 *   │         ▼                                               │
 *   │  ┌──────────────┐                                       │
 *   │  │ Compliance   │ → Validates against plan rules        │
 *   │  │ Agent        │   Reads: gathered_data                │
 *   │  └──────┬───────┘   Writes: compliance_findings         │
 *   │         │ state                                         │
 *   │         ▼                                               │
 *   │  ┌──────────────┐                                       │
 *   │  │ Anomaly      │ → Detects outliers and patterns       │
 *   │  │ Agent        │   Reads: gathered_data + compliance   │
 *   │  └──────┬───────┘   Writes: anomaly_findings            │
 *   │         │ state                                         │
 *   │         ▼                                               │
 *   │  ┌──────────────┐                                       │
 *   │  │ Report       │ → Synthesizes everything into report  │
 *   │  │ Agent        │   Reads: ALL previous outputs         │
 *   │  └──────┬───────┘   Writes: final_report                │
 *   │         │                                               │
 *   │         ▼                                               │
 *   │  WorkflowResult (report + stage log + flags)            │
 *   └─────────────────────────────────────────────────────────┘
 *
 * AGENTIC WORKFLOW vs. ReAct vs. RAG:
 * ┌────────────────────┬────────────────┬──────────────────────┐
 * │ RAG                 │ ReAct          │ Agentic Workflow      │
 * ├────────────────────┼────────────────┼──────────────────────┤
 * │ 1 retrieval step    │ 1 agent,       │ N agents, each with  │
 * │ 1 generation step   │ N tool calls   │ own AI persona       │
 * │ Fixed pipeline      │ Dynamic loop   │ Orchestrated pipeline│
 * │ Single perspective  │ Single reasoner│ Multiple perspectives│
 * │ Fast, cheap         │ Medium cost    │ Higher cost, richer  │
 * │ Simple lookups      │ Multi-step Q&A │ Complex workflows    │
 * └────────────────────┴────────────────┴──────────────────────┘
 *
 * WHEN TO USE AGENTIC WORKFLOWS:
 * 1. Tasks requiring MULTIPLE PERSPECTIVES
 *    "Review this commission" → data + compliance + anomaly + report
 *
 * 2. Tasks with DISTINCT PHASES
 *    Each phase needs different expertise and prompting strategies
 *
 * 3. Tasks where QUALITY > SPEED
 *    Multiple focused AI calls produce better results than one big call
 *
 * 4. Tasks needing AUDITABILITY
 *    Each agent's contribution is logged separately
 *
 * WHEN NOT TO USE:
 * - Simple questions → Use RAG
 * - Multi-step data lookups → Use ReAct
 * - Real-time responses needed → Too slow (multiple AI calls)
 *
 * ACADEMIC REFERENCE:
 * Chase, H. (2024). "LangGraph: Multi-Agent Workflows."
 * Wu, Q., et al. (2023). "AutoGen: Enabling Next-Gen LLM Applications
 * via Multi-Agent Conversation." Microsoft Research.
 */
@Service
public class CommissionWorkflowOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(CommissionWorkflowOrchestrator.class);

    /**
     * Agents registered by stage.
     *
     * WHY A MAP BY STAGE?
     * The orchestrator needs to find the right agent for each stage.
     * Using a map indexed by WorkflowStage provides O(1) lookup and
     * makes the stage→agent mapping explicit.
     *
     * Spring injects all WorkflowAgent beans automatically via the
     * List<WorkflowAgent> constructor parameter. We then index them
     * by their declared stage.
     */
    private final Map<WorkflowStage, WorkflowAgent> agentsByStage = new LinkedHashMap<>();

    /**
     * Constructor: Spring injects all WorkflowAgent beans.
     *
     * SPRING DI PATTERN:
     * By declaring List<WorkflowAgent>, Spring automatically finds
     * all beans implementing the WorkflowAgent interface and injects
     * them. This makes adding new agents a simple matter of creating
     * a new @Component — no orchestrator changes needed.
     */
    public CommissionWorkflowOrchestrator(List<WorkflowAgent> agents) {
        for (WorkflowAgent agent : agents) {
            agentsByStage.put(agent.getStage(), agent);
            log.info("Registered workflow agent: {} → stage {}", agent.getName(), agent.getStage());
        }
        log.info("Workflow orchestrator initialized with {} agents", agentsByStage.size());
    }

    // ============================================================
    // CORE ORCHESTRATION
    // ============================================================

    /**
     * Executes the full commission review workflow.
     *
     * THE ORCHESTRATION ALGORITHM:
     *
     * 1. Create a fresh WorkflowState with the user's request
     *
     * 2. For each stage in order (GATHERING → COMPLIANCE → ANOMALY → REPORTING):
     *    a. Find the agent registered for this stage
     *    b. Set the current stage in state
     *    c. Execute the agent (reads/writes shared state)
     *    d. Check for early termination conditions
     *
     * 3. Extract the final report and build WorkflowResult
     *
     * ORCHESTRATOR RESPONSIBILITIES:
     * - SEQUENCING: Ensures agents run in the correct order
     * - STATE MANAGEMENT: Creates and passes the shared state
     * - ERROR HANDLING: Catches agent failures, decides whether to continue
     * - EARLY TERMINATION: Skips remaining stages if data is missing
     * - RESULT ASSEMBLY: Packages the final output
     *
     * @param request The user's natural language review request
     * @return WorkflowResult containing the report and metadata
     */
    @Observed(name = "commission.workflow.review", contextualName = "workflow-review")
    public WorkflowResult executeReview(String request) {
        log.info("Starting commission review workflow for: '{}'", request);

        // Step 1: Initialize shared state
        WorkflowState state = new WorkflowState(request);

        // Step 2: Define the stage execution order
        WorkflowStage[] stages = {
                WorkflowStage.GATHERING,
                WorkflowStage.COMPLIANCE,
                WorkflowStage.ANOMALY,
                WorkflowStage.REPORTING
        };

        // Step 3: Execute each stage
        for (WorkflowStage stage : stages) {
            WorkflowAgent agent = agentsByStage.get(stage);
            if (agent == null) {
                log.warn("No agent registered for stage: {}. Skipping.", stage);
                state.logStage(stage, "Skipped — no agent registered");
                continue;
            }

            state.setCurrentStage(stage);
            log.info("Executing stage: {} ({})", stage.getDisplayName(), agent.getName());

            try {
                agent.execute(state);
            } catch (Exception e) {
                log.error("Agent '{}' threw an unhandled exception: {}", agent.getName(), e.getMessage(), e);
                state.putData(stage.name().toLowerCase() + "_error", e.getMessage());
                state.addFlag("STAGE_ERROR");
                state.logStage(stage, "Unhandled error: " + e.getMessage());
            }

            // Early termination: if gathering found no data, skip analysis stages
            // but still run reporting (to produce a "no data" report)
            if (stage == WorkflowStage.GATHERING && state.hasFlag("NO_DATA")) {
                log.info("No data found — skipping analysis stages, proceeding to report");
                // Skip to REPORTING directly
                WorkflowAgent reportAgent = agentsByStage.get(WorkflowStage.REPORTING);
                if (reportAgent != null) {
                    state.setCurrentStage(WorkflowStage.REPORTING);
                    reportAgent.execute(state);
                }
                break;
            }
        }

        // Step 4: Mark workflow as completed
        state.setCurrentStage(WorkflowStage.COMPLETED);

        // Step 5: Build and return the result
        String finalReport = state.getData("final_report");
        if (finalReport == null) {
            finalReport = "Workflow completed but no report was generated.";
        }

        WorkflowResult result = new WorkflowResult(
                request,
                finalReport,
                state.getStageLog(),
                state.getFlags(),
                state.getAllData(),
                !state.hasFlag("STAGE_ERROR")
        );

        log.info("Workflow completed. Stages: {}, Flags: {}, Success: {}",
                result.getTotalStages(), result.getFlags(), result.isSuccess());

        return result;
    }

    /**
     * Returns information about registered agents (for debugging/API).
     */
    public Map<String, String> getRegisteredAgents() {
        Map<String, String> info = new LinkedHashMap<>();
        agentsByStage.forEach((stage, agent) ->
                info.put(stage.getDisplayName(), agent.getName()));
        return info;
    }
}
