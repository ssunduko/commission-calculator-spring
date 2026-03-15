package com.chapman.edu.commissions.ai.service.workflow;

/**
 * ============================================================
 * AGENTIC WORKFLOW: Workflow Stage Enum
 * ============================================================
 *
 * CONCEPT: Named Stages in an Orchestrated Agent Pipeline
 * ------------------------------------------------------------
 * Each stage represents a phase in the agentic workflow. The
 * orchestrator advances through stages sequentially, but can
 * also SKIP stages or REPEAT them based on intermediate results.
 *
 * STAGE FLOW:
 *
 *   ┌──────────────┐
 *   │  GATHERING   │ → Collect relevant commission data
 *   └──────┬───────┘
 *          ↓
 *   ┌──────────────┐
 *   │  COMPLIANCE  │ → Validate against plan rules
 *   └──────┬───────┘
 *          ↓
 *   ┌──────────────┐
 *   │  ANOMALY     │ → Detect statistical outliers
 *   └──────┬───────┘
 *          ↓
 *   ┌──────────────┐
 *   │  REPORTING   │ → Generate final review report
 *   └──────┬───────┘
 *          ↓
 *   ┌──────────────┐
 *   │  COMPLETED   │ → Workflow finished
 *   └──────────────┘
 *
 * WHY AN ENUM OVER FREE-FORM STRINGS?
 * Enums enforce a finite set of valid stages, preventing typos
 * and making it easy to reason about transitions. The ordinal()
 * provides natural ordering for sequential workflows.
 */
public enum WorkflowStage {

    /** Collect deals, calculations, plans, and user data. */
    GATHERING("Data Gathering",
            "Collect all commission data relevant to the review request."),

    /** Validate that calculations match plan rules and tier structures. */
    COMPLIANCE("Compliance Check",
            "Verify each commission calculation against the plan rules and tier structure."),

    /** Detect statistical outliers and suspicious patterns. */
    ANOMALY("Anomaly Analysis",
            "Identify statistically unusual calculations and potential issues."),

    /** Produce a comprehensive human-readable review report. */
    REPORTING("Report Generation",
            "Synthesize findings into a final commission review report."),

    /** Terminal stage — workflow is done. */
    COMPLETED("Completed",
            "The workflow has finished processing.");

    private final String displayName;
    private final String description;

    WorkflowStage(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
