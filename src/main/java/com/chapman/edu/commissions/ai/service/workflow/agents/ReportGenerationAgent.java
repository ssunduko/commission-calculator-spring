package com.chapman.edu.commissions.ai.service.workflow.agents;

import com.chapman.edu.commissions.ai.service.workflow.WorkflowAgent;
import com.chapman.edu.commissions.ai.service.workflow.WorkflowStage;
import com.chapman.edu.commissions.ai.service.workflow.WorkflowState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * ============================================================
 * AGENTIC WORKFLOW: Report Generation Agent
 * ============================================================
 *
 * CONCEPT: Specialized Agent for Synthesis and Reporting
 * ------------------------------------------------------------
 * This is the FINAL agent in the commission review workflow.
 * It synthesizes ALL previous agents' findings into a cohesive,
 * human-readable commission review report.
 *
 * WHAT MAKES THIS AGENT SPECIAL:
 * Unlike the other agents that produce intermediate analysis,
 * this agent produces the FINAL OUTPUT that the user sees.
 * It must:
 *
 * 1. SYNTHESIZE: Combine data + compliance + anomaly findings
 * 2. PRIORITIZE: Highlight the most important findings
 * 3. CONTEXTUALIZE: Explain what findings mean in business terms
 * 4. RECOMMEND: Suggest concrete next steps
 * 5. FORMAT: Produce a professional, readable report
 *
 * AGENT COLLABORATION CULMINATION:
 * This agent demonstrates the full power of the agentic workflow:
 * it reads outputs from ALL previous agents and produces something
 * greater than any single agent could create alone.
 *
 *   ┌─────────────┐   ┌────────────────┐   ┌──────────────┐
 *   │  Gathered    │   │  Compliance    │   │  Anomaly     │
 *   │  Data        │ + │  Findings      │ + │  Findings    │
 *   └──────┬──────┘   └───────┬────────┘   └──────┬───────┘
 *          └──────────────────┼───────────────────┘
 *                             ↓
 *                  ┌───────────────────┐
 *                  │  Report Agent     │
 *                  │  (AI Synthesis)   │
 *                  └─────────┬─────────┘
 *                            ↓
 *                  ┌───────────────────┐
 *                  │  Final Report     │
 *                  │  (User Output)    │
 *                  └───────────────────┘
 */
@Component
public class ReportGenerationAgent implements WorkflowAgent {

    private static final Logger log = LoggerFactory.getLogger(ReportGenerationAgent.class);

    private final ChatClient chatClient;

    public ReportGenerationAgent(ChatClient commissionChatClient) {
        this.chatClient = commissionChatClient;
    }

    @Override
    public String getName() {
        return "Report Generation Agent";
    }

    @Override
    public WorkflowStage getStage() {
        return WorkflowStage.REPORTING;
    }

    /**
     * Synthesizes all agents' findings into a final review report.
     *
     * AGENT PERSONA:
     * This agent operates as a "senior commission manager" who
     * writes executive-level reports. Its prompt emphasizes
     * clarity, actionability, and business context.
     *
     * ADAPTIVE REPORTING:
     * The report structure adapts based on workflow flags:
     * - HIGH_RISK → Urgent tone, prominent risk section
     * - COMPLIANCE_ISSUE → Detailed compliance breakdown
     * - NO_DATA → Short report explaining the data gap
     */
    @Override
    public void execute(WorkflowState state) {
        log.info("[{}] Starting report generation", getName());

        if (state.hasFlag("NO_DATA")) {
            String report = String.format(
                    "# Commission Review Report\n\n" +
                    "**Subject:** %s\n\n" +
                    "**Status:** Unable to complete review — no matching sales representative found.\n\n" +
                    "**Recommendation:** Verify the name and try again.",
                    state.getData("sales_rep_name"));
            state.putData("final_report", report);
            state.logStage(getStage(), "Generated minimal report (no data available)");
            return;
        }

        String salesRepName = state.getData("sales_rep_name");
        String gatheredData = state.getData("gathered_data");
        String complianceFindings = state.getData("compliance_findings");
        String anomalyFindings = state.getData("anomaly_findings");

        // Build comprehensive context from all agents
        StringBuilder allFindings = new StringBuilder();
        allFindings.append("=== DATA BRIEF ===\n");
        allFindings.append(gatheredData != null ? gatheredData : "No data collected.");
        allFindings.append("\n\n");

        allFindings.append("=== COMPLIANCE REVIEW ===\n");
        allFindings.append(complianceFindings != null ? complianceFindings : "Compliance check not performed.");
        allFindings.append("\n\n");

        allFindings.append("=== ANOMALY ANALYSIS ===\n");
        allFindings.append(anomalyFindings != null ? anomalyFindings : "Anomaly analysis not performed.");
        allFindings.append("\n\n");

        // Include workflow metadata
        allFindings.append("=== WORKFLOW FLAGS ===\n");
        for (String flag : state.getFlags()) {
            allFindings.append("- ").append(flag).append("\n");
        }
        allFindings.append("\n");

        allFindings.append("=== STAGE LOG ===\n");
        for (String entry : state.getStageLog()) {
            allFindings.append("- ").append(entry).append("\n");
        }

        // Build urgency context for the prompt
        String urgencyNote = "";
        if (state.hasFlag("HIGH_RISK")) {
            urgencyNote = "IMPORTANT: High-risk anomalies were detected. " +
                    "The report should prominently highlight these findings and recommend immediate action.";
        } else if (state.hasFlag("COMPLIANCE_ISSUE")) {
            urgencyNote = "NOTE: Compliance issues were found. " +
                    "Include a dedicated compliance section with specific remediation steps.";
        }

        try {
            String report = chatClient.prompt()
                    .system(String.format("""
                            You are a senior commission manager writing a formal commission review report.
                            Synthesize all findings from the data gathering, compliance check, and anomaly
                            analysis into a cohesive, actionable report.

                            %s

                            REPORT STRUCTURE:
                            # Commission Review Report: [Sales Rep Name]

                            ## Executive Summary
                            [2-3 sentences summarizing the overall status and key findings]

                            ## Commission Data Overview
                            [Key metrics: total deals, total commissions, plan details]

                            ## Compliance Review
                            [Summary of compliance findings — PASS or issues with details]

                            ## Anomaly Analysis
                            [Risk level and any anomalies detected]

                            ## Key Findings
                            [Numbered list of the most important takeaways]

                            ## Recommendations
                            [Specific, actionable next steps]

                            ## Risk Assessment
                            [Overall risk: LOW / MEDIUM / HIGH with justification]

                            GUIDELINES:
                            - Use specific numbers and reference actual data
                            - Format currency as $X,XXX.XX
                            - Be concise but thorough
                            - Prioritize actionable insights over raw data
                            - Maintain a professional, objective tone""", urgencyNote))
                    .user(String.format(
                            "Generate a comprehensive commission review report for %s based on " +
                            "the following multi-agent analysis:\n\n%s",
                            salesRepName, allFindings))
                    .call()
                    .content();

            state.putData("final_report", report);
            state.logStage(getStage(), "Generated comprehensive review report");
            log.info("[{}] Report generation complete", getName());

        } catch (Exception e) {
            log.error("[{}] Report generation failed: {}", getName(), e.getMessage());
            // Fallback: construct a basic report from raw data
            String fallbackReport = buildFallbackReport(salesRepName, state);
            state.putData("final_report", fallbackReport);
            state.addFlag("STAGE_ERROR");
            state.logStage(getStage(), "Generated fallback report due to AI error");
        }
    }

    /**
     * Builds a basic report when the AI call fails.
     *
     * GRACEFUL DEGRADATION:
     * Even if the final AI call fails, we can still produce a useful
     * report by assembling the previous agents' outputs. This ensures
     * the user always gets something, even in error scenarios.
     */
    private String buildFallbackReport(String salesRepName, WorkflowState state) {
        StringBuilder report = new StringBuilder();
        report.append(String.format("# Commission Review Report: %s\n\n", salesRepName));
        report.append("*Note: This is an abbreviated report due to a processing error.*\n\n");

        report.append("## Data Summary\n");
        report.append(state.getData("gathered_data") != null ?
                state.getData("gathered_data") : "No data collected.");
        report.append("\n\n");

        report.append("## Compliance Findings\n");
        report.append(state.getData("compliance_findings") != null ?
                state.getData("compliance_findings") : "Not available.");
        report.append("\n\n");

        report.append("## Anomaly Findings\n");
        report.append(state.getData("anomaly_findings") != null ?
                state.getData("anomaly_findings") : "Not available.");

        return report.toString();
    }
}
