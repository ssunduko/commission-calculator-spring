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
 * AGENTIC WORKFLOW: Anomaly Analysis Agent
 * ============================================================
 *
 * CONCEPT: Specialized Agent for Pattern Detection
 * ------------------------------------------------------------
 * This is the THIRD agent in the commission review workflow.
 * It analyzes the gathered data for statistical anomalies and
 * suspicious patterns that might indicate errors or fraud.
 *
 * WHAT IT DETECTS:
 * 1. STATISTICAL OUTLIERS: Commissions far above/below average
 * 2. PATTERN BREAKS: Sudden changes in commission amounts
 * 3. RATIO ANOMALIES: Unusual commission-to-deal-value ratios
 * 4. TIMING PATTERNS: Clustered calculations or unusual dates
 * 5. CROSS-REP COMPARISON: Performance relative to peers
 *
 * AGENT COLLABORATION:
 * This agent reads the compliance agent's findings. If compliance
 * already flagged an issue, the anomaly agent can investigate
 * whether it's an isolated error or part of a broader pattern.
 *
 * This demonstrates a key agentic workflow concept: AGENTS BUILD
 * ON EACH OTHER'S WORK. Each agent has access to all previous
 * agents' outputs through the shared WorkflowState.
 *
 * DATA FLOW:
 *   state.getData("gathered_data")       →  Primary input
 *   state.getData("compliance_findings") →  Cross-reference
 *         ↓
 *   state.putData("anomaly_findings", findings)
 *   state.addFlag("HIGH_RISK") if critical anomalies found
 */
@Component
public class AnomalyAnalysisAgent implements WorkflowAgent {

    private static final Logger log = LoggerFactory.getLogger(AnomalyAnalysisAgent.class);

    private final ChatClient chatClient;

    public AnomalyAnalysisAgent(ChatClient commissionChatClient) {
        this.chatClient = commissionChatClient;
    }

    @Override
    public String getName() {
        return "Anomaly Analysis Agent";
    }

    @Override
    public WorkflowStage getStage() {
        return WorkflowStage.ANOMALY;
    }

    /**
     * Analyzes commission data for anomalies and suspicious patterns.
     *
     * AGENT PERSONA:
     * This agent operates as a "fraud analyst / data scientist" —
     * it looks for things that don't fit the expected pattern.
     * Its system prompt emphasizes statistical thinking and
     * pattern recognition.
     *
     * CROSS-AGENT AWARENESS:
     * Note how this agent reads compliance_findings from state.
     * This is the key difference from ReAct: in ReAct, a single
     * agent sees all tool observations. In a workflow, each agent
     * builds on previous agents' analyzed findings.
     */
    @Override
    public void execute(WorkflowState state) {
        log.info("[{}] Starting anomaly analysis", getName());

        String gatheredData = state.getData("gathered_data");
        if (gatheredData == null || state.hasFlag("NO_DATA")) {
            state.putData("anomaly_findings", "Skipped — no data available for anomaly analysis.");
            state.logStage(getStage(), "Skipped due to missing data");
            return;
        }

        String salesRepName = state.getData("sales_rep_name");
        String complianceFindings = state.getData("compliance_findings");

        // Build context that includes previous agent's work
        StringBuilder context = new StringBuilder();
        context.append("=== COMMISSION DATA ===\n");
        context.append(gatheredData);
        context.append("\n\n");

        if (complianceFindings != null) {
            context.append("=== COMPLIANCE AGENT FINDINGS ===\n");
            context.append(complianceFindings);
            context.append("\n\n");
        }

        if (state.hasFlag("COMPLIANCE_ISSUE")) {
            context.append("NOTE: The compliance agent flagged critical issues. " +
                    "Investigate whether these are isolated errors or part of a pattern.\n");
        }

        try {
            String findings = chatClient.prompt()
                    .system("""
                            You are a data scientist specializing in financial anomaly detection.
                            Analyze the commission data for patterns that deviate from expectations.

                            ANALYSIS FRAMEWORK:
                            1. STATISTICAL ANALYSIS
                               - Calculate mean and range of commission amounts
                               - Identify values that seem unusually high or low
                               - Check commission-to-deal-value ratios for consistency

                            2. PATTERN ANALYSIS
                               - Look for sudden jumps or drops in commission amounts
                               - Check for unusual clustering of calculation dates
                               - Compare deal values to commission amounts across entries

                            3. CROSS-REFERENCE WITH COMPLIANCE
                               - If compliance flagged issues, assess if they indicate
                                 a broader problem or an isolated error
                               - Check if anomalies align with compliance findings

                            4. RISK ASSESSMENT
                               Assign an overall risk level:
                               - LOW RISK: All patterns normal, no anomalies
                               - MEDIUM RISK: Minor anomalies worth monitoring
                               - HIGH RISK: Significant anomalies requiring investigation

                            FORMAT YOUR RESPONSE AS:
                            ## Risk Level: [LOW / MEDIUM / HIGH]

                            ## Statistical Summary
                            [Key metrics and distributions]

                            ## Anomalies Detected
                            [List each anomaly with evidence]

                            ## Recommendations
                            [What actions should be taken]

                            Be specific. Reference actual numbers from the data.""")
                    .user(String.format(
                            "Analyze the following commission data for anomalies for %s:\n\n%s",
                            salesRepName, context))
                    .call()
                    .content();

            state.putData("anomaly_findings", findings);

            // Set flags based on risk level
            if (findings != null) {
                String lower = findings.toLowerCase();
                if (lower.contains("risk level: high") || lower.contains("## risk level: high")) {
                    state.addFlag("HIGH_RISK");
                    state.logStage(getStage(), "HIGH RISK — significant anomalies detected");
                } else if (lower.contains("risk level: medium") || lower.contains("## risk level: medium")) {
                    state.addFlag("MEDIUM_RISK");
                    state.logStage(getStage(), "MEDIUM RISK — minor anomalies detected");
                } else {
                    state.logStage(getStage(), "LOW RISK — no significant anomalies");
                }
            }

            log.info("[{}] Anomaly analysis complete. Flags: {}", getName(), state.getFlags());

        } catch (Exception e) {
            log.error("[{}] Anomaly analysis failed: {}", getName(), e.getMessage());
            state.putData("anomaly_findings", "Anomaly analysis failed: " + e.getMessage());
            state.addFlag("STAGE_ERROR");
            state.logStage(getStage(), "Error: " + e.getMessage());
        }
    }
}
