package com.chapman.edu.commissions.ai.service.workflow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * AGENTIC WORKFLOW: Shared Workflow State
 * ============================================================
 *
 * CONCEPT: Mutable State Shared Across Agents in a Workflow
 * ------------------------------------------------------------
 * In an agentic workflow, multiple specialized agents collaborate
 * to solve a complex task. They need a SHARED STATE object to:
 *
 * 1. PASS DATA FORWARD: Agent A's output becomes Agent B's input
 * 2. ACCUMULATE FINDINGS: Each agent adds to the growing picture
 * 3. SIGNAL CONDITIONS: Agents can flag issues for downstream agents
 * 4. TRACK PROVENANCE: Record which agent produced which finding
 *
 * COMPARISON TO ReAct SCRATCHPAD:
 * ┌──────────────────────┬────────────────────────────────────┐
 * │ ReAct Scratchpad      │ Workflow State                     │
 * ├──────────────────────┼────────────────────────────────────┤
 * │ Linear text log       │ Structured key-value data          │
 * │ Single agent writes   │ Multiple agents read/write         │
 * │ Tool observations     │ Agent analyses and findings        │
 * │ Grows per step        │ Grows per stage                    │
 * │ Fed back to same AI   │ Fed to different specialized AIs   │
 * └──────────────────────┴────────────────────────────────────┘
 *
 * DESIGN PRINCIPLE:
 * The state is intentionally simple — a map of string keys to
 * string values. This keeps the interface uniform across agents
 * and makes it easy to serialize (for logging, persistence, or
 * distributed workflows). Agents parse what they need from the
 * string values.
 */
public class WorkflowState {

    /** The original user request that triggered the workflow. */
    private final String originalRequest;

    /** The current workflow stage. */
    private WorkflowStage currentStage;

    /**
     * Key-value data accumulated by agents.
     *
     * Common keys:
     * - "gathered_data"       → Raw data collected by the gathering agent
     * - "compliance_findings" → Issues found by the compliance agent
     * - "anomaly_findings"    → Anomalies detected by the anomaly agent
     * - "final_report"        → The completed review report
     * - "sales_rep_name"      → The sales rep being reviewed
     * - "error"               → Error message if a stage fails
     */
    private final Map<String, String> data = new LinkedHashMap<>();

    /**
     * Ordered log of stage completions with summaries.
     * Each entry: "STAGE_NAME: summary of what was found"
     */
    private final List<String> stageLog = new ArrayList<>();

    /**
     * Flags raised by agents that downstream agents should consider.
     * Example: "HIGH_RISK" flag set by anomaly agent tells the
     * reporting agent to highlight the findings prominently.
     */
    private final List<String> flags = new ArrayList<>();

    public WorkflowState(String originalRequest) {
        this.originalRequest = originalRequest;
        this.currentStage = WorkflowStage.GATHERING;
    }

    // ============================================================
    // STATE ACCESS METHODS
    // ============================================================

    public String getOriginalRequest() {
        return originalRequest;
    }

    public WorkflowStage getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(WorkflowStage currentStage) {
        this.currentStage = currentStage;
    }

    /**
     * Store a named piece of data produced by an agent.
     */
    public void putData(String key, String value) {
        data.put(key, value);
    }

    /**
     * Retrieve data by key. Returns null if not present.
     */
    public String getData(String key) {
        return data.get(key);
    }

    /**
     * Returns all accumulated data (read-only view for prompt building).
     */
    public Map<String, String> getAllData() {
        return Map.copyOf(data);
    }

    /**
     * Log a stage completion with a brief summary.
     */
    public void logStage(WorkflowStage stage, String summary) {
        stageLog.add(stage.getDisplayName() + ": " + summary);
    }

    public List<String> getStageLog() {
        return List.copyOf(stageLog);
    }

    /**
     * Raise a flag for downstream agents to consider.
     */
    public void addFlag(String flag) {
        if (!flags.contains(flag)) {
            flags.add(flag);
        }
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    public List<String> getFlags() {
        return List.copyOf(flags);
    }
}
