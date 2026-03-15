package com.chapman.edu.commissions.ai.processor;

import com.chapman.edu.commissions.ai.service.workflow.CommissionWorkflowOrchestrator;
import com.chapman.edu.commissions.ai.service.workflow.WorkflowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ============================================================
 * PROCESSOR: WorkflowProcessor
 * ============================================================
 *
 * CONCEPT: Agentic Workflow (Multi-Agent Orchestration)
 * ------------------------------------------------------------
 * This processor demonstrates the Agentic Workflow pattern,
 * where MULTIPLE specialized AI agents collaborate on a complex
 * task, coordinated by an orchestrator.
 *
 * HOW IT DIFFERS FROM OTHER PATTERNS:
 *
 * 1. SIMPLE PROMPT (AiProcessor):
 *    User → AI → Answer
 *    One call, one perspective.
 *
 * 2. RAG (RagProcessor):
 *    User → Retrieve → AI + Context → Answer
 *    One retrieval, one generation.
 *
 * 3. ReAct AGENT (ReActProcessor):
 *    User → [Think → Act → Observe]* → Answer
 *    ONE agent reasons in a loop with tools.
 *
 * 4. AGENTIC WORKFLOW (this processor):
 *    User → Agent₁ → Agent₂ → Agent₃ → Agent₄ → Report
 *    MULTIPLE agents, each with its own AI persona, collaborate
 *    through shared state. An orchestrator manages the pipeline.
 *
 * THE KEY INSIGHT:
 * Each agent in the workflow has a DIFFERENT SYSTEM PROMPT and
 * DIFFERENT EXPERTISE. The data gathering agent thinks like a
 * data analyst. The compliance agent thinks like an auditor.
 * The anomaly agent thinks like a data scientist. The report
 * agent thinks like a senior manager.
 *
 * This "division of cognitive labor" produces richer, more
 * nuanced results than any single AI call could achieve.
 *
 * EXAMPLE WORKFLOW:
 *   Request: "Review Alice Johnson's commission performance"
 *
 *   Agent 1 (Data Gathering):
 *     → Queries repos for Alice's deals, calculations, plans
 *     → Produces structured data brief
 *
 *   Agent 2 (Compliance):
 *     → Reads data brief
 *     → Checks each calculation against plan rules
 *     → Flags: "COMPLIANCE_ISSUE" (rate mismatch found)
 *
 *   Agent 3 (Anomaly):
 *     → Reads data brief + compliance findings
 *     → Detects a commission 2.5σ above mean
 *     → Flags: "HIGH_RISK"
 *
 *   Agent 4 (Report):
 *     → Reads ALL previous outputs
 *     → Synthesizes into executive-level review report
 *     → Adapts tone based on flags (urgent for HIGH_RISK)
 */
@Service
public class WorkflowProcessor {

    private static final Logger log = LoggerFactory.getLogger(WorkflowProcessor.class);

    private final CommissionWorkflowOrchestrator orchestrator;

    public WorkflowProcessor(CommissionWorkflowOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /**
     * Demonstrates the agentic workflow with a commission review.
     *
     * @param request The review request (e.g., "Review Alice's commissions")
     * @return Map containing the report, stage log, and metadata
     */
    public Map<String, Object> demonstrateWorkflow(String request) {
        Map<String, Object> demo = new LinkedHashMap<>();
        demo.put("pattern", "Agentic Workflow (Multi-Agent Orchestration)");
        demo.put("request", request);

        WorkflowResult result = orchestrator.executeReview(request);

        demo.put("success", result.isSuccess());
        demo.put("final_report", result.getFinalReport());
        demo.put("total_stages", result.getTotalStages());
        demo.put("stage_log", result.getStageLog());
        demo.put("flags", result.getFlags());

        return demo;
    }

    /**
     * Returns information about the workflow's registered agents.
     */
    public Map<String, String> getWorkflowAgents() {
        return orchestrator.getRegisteredAgents();
    }
}
