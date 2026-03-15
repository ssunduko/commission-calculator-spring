package com.chapman.edu.commissions.ai.service.workflow;

import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * AGENTIC WORKFLOW: Workflow Result
 * ============================================================
 *
 * CONCEPT: The Final Output of an Orchestrated Agent Workflow
 * ------------------------------------------------------------
 * After the orchestrator runs all agents through their stages,
 * it produces a WorkflowResult that contains:
 *
 * - The final report (the primary output)
 * - Stage-by-stage log (for transparency and auditing)
 * - Any flags raised during processing
 * - All accumulated data (for programmatic consumers)
 * - Success/failure status
 *
 * TRANSPARENCY:
 * Like the ReAct AgentResult, this exposes the full processing
 * chain so users and auditors can understand HOW the conclusion
 * was reached. The stage log shows what each specialized agent
 * contributed to the final report.
 *
 * ┌─────────────────────────────────────────────────────────┐
 * │ WorkflowResult                                          │
 * │                                                         │
 * │ ┌─────────────────────────────────────────────────────┐ │
 * │ │ Stage Log                                           │ │
 * │ │  [1] Data Gathering: Collected 5 deals, 3 calcs...  │ │
 * │ │  [2] Compliance: All calculations match plan rules  │ │
 * │ │  [3] Anomaly: 1 outlier detected (>2σ from mean)    │ │
 * │ │  [4] Report: Generated comprehensive review         │ │
 * │ └─────────────────────────────────────────────────────┘ │
 * │                                                         │
 * │ ┌─────────────────────────────────────────────────────┐ │
 * │ │ Final Report                                        │ │
 * │ │ "Commission Review for Alice Johnson..."            │ │
 * │ └─────────────────────────────────────────────────────┘ │
 * │                                                         │
 * │ Flags: [HIGH_RISK]  Success: true  Stages: 4           │
 * └─────────────────────────────────────────────────────────┘
 */
public class WorkflowResult {

    private final String originalRequest;
    private final String finalReport;
    private final List<String> stageLog;
    private final List<String> flags;
    private final Map<String, String> allData;
    private final boolean success;

    public WorkflowResult(String originalRequest, String finalReport,
                          List<String> stageLog, List<String> flags,
                          Map<String, String> allData, boolean success) {
        this.originalRequest = originalRequest;
        this.finalReport = finalReport;
        this.stageLog = stageLog;
        this.flags = flags;
        this.allData = allData;
        this.success = success;
    }

    public String getOriginalRequest() {
        return originalRequest;
    }

    public String getFinalReport() {
        return finalReport;
    }

    public List<String> getStageLog() {
        return stageLog;
    }

    public List<String> getFlags() {
        return flags;
    }

    public Map<String, String> getAllData() {
        return allData;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getTotalStages() {
        return stageLog.size();
    }
}
