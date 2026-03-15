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
 * AGENTIC WORKFLOW: Compliance Check Agent
 * ============================================================
 *
 * CONCEPT: Specialized Agent for Rule Validation
 * ------------------------------------------------------------
 * This is the SECOND agent in the commission review workflow.
 * It takes the gathered data and validates each commission
 * calculation against the plan rules and tier structure.
 *
 * WHAT IT CHECKS:
 * 1. TIER ACCURACY: Is the correct tier rate applied for the deal value?
 * 2. PLAN ASSIGNMENT: Is the calculation using the correct plan?
 * 3. STATUS CONSISTENCY: Do deal/calculation statuses align?
 * 4. MATHEMATICAL ACCURACY: Do base × rate = gross commission?
 * 5. BOUNDARY CONDITIONS: Are tier boundaries handled correctly?
 *
 * WHY AI FOR COMPLIANCE?
 * Traditional rule engines check exact conditions, but commission
 * plans have nuance:
 * - Effective date overlaps
 * - Pro-rated periods
 * - Multi-tier calculations
 * - Exception clauses
 *
 * The AI can reason about these edge cases while also explaining
 * its findings in natural language for human reviewers.
 *
 * DATA FLOW:
 *   state.getData("gathered_data")  →  AI compliance analysis
 *         ↓
 *   state.putData("compliance_findings", findings)
 *   state.addFlag("COMPLIANCE_ISSUE") if problems found
 */
@Component
public class ComplianceCheckAgent implements WorkflowAgent {

    private static final Logger log = LoggerFactory.getLogger(ComplianceCheckAgent.class);

    private final ChatClient chatClient;

    public ComplianceCheckAgent(ChatClient commissionChatClient) {
        this.chatClient = commissionChatClient;
    }

    @Override
    public String getName() {
        return "Compliance Check Agent";
    }

    @Override
    public WorkflowStage getStage() {
        return WorkflowStage.COMPLIANCE;
    }

    /**
     * Validates commission calculations against plan rules.
     *
     * AGENT PERSONA:
     * This agent operates as a "commission auditor" — its system
     * prompt is tuned for rule validation, not general conversation.
     * Each agent in the workflow has its own specialized persona.
     */
    @Override
    public void execute(WorkflowState state) {
        log.info("[{}] Starting compliance check", getName());

        String gatheredData = state.getData("gathered_data");
        if (gatheredData == null || state.hasFlag("NO_DATA")) {
            state.putData("compliance_findings", "Skipped — no data available for compliance check.");
            state.logStage(getStage(), "Skipped due to missing data");
            return;
        }

        String salesRepName = state.getData("sales_rep_name");

        try {
            String findings = chatClient.prompt()
                    .system("""
                            You are a senior commission compliance auditor. Your job is to verify that
                            every commission calculation follows the plan rules correctly.

                            FOR EACH CALCULATION, CHECK:
                            1. TIER RATE: Given the deal value and the plan's tier structure, is the
                               correct commission rate being applied? Show which tier the deal falls into.
                            2. MATH CHECK: Does deal_value × tier_rate = base_commission?
                               Does base + adjustments = gross? Does gross - deductions = net?
                            3. STATUS ALIGNMENT: If a deal is LOST, there should be no APPROVED commission.
                               If a deal is WON, any PENDING commission should be reviewed.
                            4. PLAN VALIDITY: Is the commission plan ACTIVE? Is the calculation date
                               within the plan's effective date range?

                            FORMAT YOUR RESPONSE AS:
                            ## Compliance Summary
                            [Overall assessment: PASS / ISSUES FOUND / CRITICAL ISSUES]

                            ## Detailed Findings
                            [For each calculation, show your verification work]

                            ## Issues (if any)
                            [List specific compliance problems with severity: LOW / MEDIUM / HIGH]

                            Be precise with numbers. Show your math. Flag any discrepancies.""")
                    .user(String.format(
                            "Perform a compliance review of the following commission data for %s:\n\n%s",
                            salesRepName, gatheredData))
                    .call()
                    .content();

            state.putData("compliance_findings", findings);

            // Analyze findings for flags
            if (findings != null) {
                String lower = findings.toLowerCase();
                if (lower.contains("critical issues") || lower.contains("high")) {
                    state.addFlag("COMPLIANCE_ISSUE");
                    state.logStage(getStage(), "Critical compliance issues found");
                } else if (lower.contains("issues found") || lower.contains("medium")) {
                    state.addFlag("COMPLIANCE_WARNING");
                    state.logStage(getStage(), "Minor compliance issues found");
                } else {
                    state.logStage(getStage(), "All calculations comply with plan rules");
                }
            }

            log.info("[{}] Compliance check complete. Flags: {}", getName(), state.getFlags());

        } catch (Exception e) {
            log.error("[{}] Compliance check failed: {}", getName(), e.getMessage());
            state.putData("compliance_findings", "Compliance check failed: " + e.getMessage());
            state.addFlag("STAGE_ERROR");
            state.logStage(getStage(), "Error: " + e.getMessage());
        }
    }
}
