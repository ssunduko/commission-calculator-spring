package com.chapman.edu.commissions.ai.workflow;

import com.chapman.edu.commissions.ai.service.workflow.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CommissionWorkflowOrchestrator.
 *
 * TESTING STRATEGY:
 * We use lightweight stub agents (not mocked ChatClient) so that
 * tests verify the ORCHESTRATION LOGIC — sequencing, state passing,
 * early termination, error handling — without depending on AI calls.
 *
 * Each stub agent writes predictable data to WorkflowState, letting
 * us assert that the orchestrator passes state correctly between agents.
 */
@DisplayName("CommissionWorkflowOrchestrator — Unit Tests")
class CommissionWorkflowOrchestratorTest {

    // ============================================================
    // STUB AGENTS (lightweight replacements for real agents)
    // ============================================================

    /** Stub that simulates successful data gathering. */
    static class StubGatheringAgent implements WorkflowAgent {
        @Override public String getName() { return "Stub Gathering Agent"; }
        @Override public WorkflowStage getStage() { return WorkflowStage.GATHERING; }
        @Override
        public void execute(WorkflowState state) {
            state.putData("sales_rep_name", "Alice Johnson");
            state.putData("gathered_data", "Alice has 3 deals totaling $285,000 and 2 commission calculations.");
            state.logStage(getStage(), "Collected 3 deals, 2 calculations for Alice Johnson");
        }
    }

    /** Stub that simulates a gathering agent finding no data. */
    static class StubNoDataGatheringAgent implements WorkflowAgent {
        @Override public String getName() { return "Stub No-Data Gathering Agent"; }
        @Override public WorkflowStage getStage() { return WorkflowStage.GATHERING; }
        @Override
        public void execute(WorkflowState state) {
            state.putData("sales_rep_name", "Unknown Person");
            state.putData("gathered_data", "No sales representative found.");
            state.addFlag("NO_DATA");
            state.logStage(getStage(), "No user found for 'Unknown Person'");
        }
    }

    /** Stub that simulates compliance check with issues. */
    static class StubComplianceAgent implements WorkflowAgent {
        @Override public String getName() { return "Stub Compliance Agent"; }
        @Override public WorkflowStage getStage() { return WorkflowStage.COMPLIANCE; }
        @Override
        public void execute(WorkflowState state) {
            // Verify it can read gathering agent's output
            String gathered = state.getData("gathered_data");
            if (gathered == null) {
                state.putData("compliance_findings", "Skipped — no data.");
                return;
            }
            state.putData("compliance_findings",
                    "Compliance Summary: ISSUES FOUND. Rate mismatch on deal #2.");
            state.addFlag("COMPLIANCE_WARNING");
            state.logStage(getStage(), "Minor compliance issues found");
        }
    }

    /** Stub that simulates anomaly analysis. */
    static class StubAnomalyAgent implements WorkflowAgent {
        @Override public String getName() { return "Stub Anomaly Agent"; }
        @Override public WorkflowStage getStage() { return WorkflowStage.ANOMALY; }
        @Override
        public void execute(WorkflowState state) {
            // Can read both gathering and compliance data
            String compliance = state.getData("compliance_findings");
            state.putData("anomaly_findings",
                    "Risk Level: LOW. No significant anomalies detected. " +
                    "Compliance note: " + (compliance != null ? "reviewed" : "unavailable"));
            state.logStage(getStage(), "LOW RISK — no significant anomalies");
        }
    }

    /** Stub that simulates report generation. */
    static class StubReportAgent implements WorkflowAgent {
        @Override public String getName() { return "Stub Report Agent"; }
        @Override public WorkflowStage getStage() { return WorkflowStage.REPORTING; }
        @Override
        public void execute(WorkflowState state) {
            String salesRep = state.getData("sales_rep_name");
            state.putData("final_report",
                    "# Commission Review Report: " + salesRep + "\n\n" +
                    "## Executive Summary\nReview completed with " + state.getFlags().size() + " flag(s).");
            state.logStage(getStage(), "Generated comprehensive review report");
        }
    }

    /** Stub that throws an exception. */
    static class StubFailingAgent implements WorkflowAgent {
        @Override public String getName() { return "Stub Failing Agent"; }
        @Override public WorkflowStage getStage() { return WorkflowStage.COMPLIANCE; }
        @Override
        public void execute(WorkflowState state) {
            throw new RuntimeException("Simulated agent failure");
        }
    }

    // ============================================================
    // TESTS
    // ============================================================

    @Nested
    @DisplayName("Successful Workflow Execution")
    class SuccessfulExecution {

        @Test
        @DisplayName("should execute all stages in order and produce a report")
        void shouldExecuteAllStages() {
            CommissionWorkflowOrchestrator orchestrator = new CommissionWorkflowOrchestrator(
                    List.of(
                            new StubGatheringAgent(),
                            new StubComplianceAgent(),
                            new StubAnomalyAgent(),
                            new StubReportAgent()
                    )
            );

            WorkflowResult result = orchestrator.executeReview("Review Alice Johnson's commissions");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getFinalReport()).contains("Commission Review Report: Alice Johnson");
            assertThat(result.getTotalStages()).isEqualTo(4);
            assertThat(result.getStageLog()).hasSize(4);
        }

        @Test
        @DisplayName("should pass state between agents correctly")
        void shouldPassStateBetweenAgents() {
            CommissionWorkflowOrchestrator orchestrator = new CommissionWorkflowOrchestrator(
                    List.of(
                            new StubGatheringAgent(),
                            new StubComplianceAgent(),
                            new StubAnomalyAgent(),
                            new StubReportAgent()
                    )
            );

            WorkflowResult result = orchestrator.executeReview("Review Alice");

            // Anomaly agent should have read compliance findings
            assertThat(result.getAllData().get("anomaly_findings")).contains("reviewed");
            // Report should reference Alice (from gathering agent's output)
            assertThat(result.getFinalReport()).contains("Alice Johnson");
        }

        @Test
        @DisplayName("should accumulate flags from multiple agents")
        void shouldAccumulateFlags() {
            CommissionWorkflowOrchestrator orchestrator = new CommissionWorkflowOrchestrator(
                    List.of(
                            new StubGatheringAgent(),
                            new StubComplianceAgent(),
                            new StubAnomalyAgent(),
                            new StubReportAgent()
                    )
            );

            WorkflowResult result = orchestrator.executeReview("Review Alice");

            assertThat(result.getFlags()).contains("COMPLIANCE_WARNING");
        }
    }

    @Nested
    @DisplayName("Early Termination")
    class EarlyTermination {

        @Test
        @DisplayName("should skip analysis stages when no data is found")
        void shouldSkipAnalysisWhenNoData() {
            CommissionWorkflowOrchestrator orchestrator = new CommissionWorkflowOrchestrator(
                    List.of(
                            new StubNoDataGatheringAgent(),
                            new StubComplianceAgent(),
                            new StubAnomalyAgent(),
                            new StubReportAgent()
                    )
            );

            WorkflowResult result = orchestrator.executeReview("Review Unknown Person");

            assertThat(result.getFlags()).contains("NO_DATA");
            // Should have gathering + report stages only (compliance and anomaly skipped)
            assertThat(result.getStageLog()).hasSize(2);
            assertThat(result.getFinalReport()).contains("Unknown Person");
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("should continue workflow when one agent fails")
        void shouldContinueOnAgentFailure() {
            CommissionWorkflowOrchestrator orchestrator = new CommissionWorkflowOrchestrator(
                    List.of(
                            new StubGatheringAgent(),
                            new StubFailingAgent(),     // compliance agent that throws
                            new StubAnomalyAgent(),
                            new StubReportAgent()
                    )
            );

            WorkflowResult result = orchestrator.executeReview("Review Alice");

            // Workflow should complete but with error flag
            assertThat(result.getFlags()).contains("STAGE_ERROR");
            assertThat(result.isSuccess()).isFalse();
            // Report should still be generated
            assertThat(result.getFinalReport()).contains("Commission Review Report");
        }
    }

    @Nested
    @DisplayName("Agent Registration")
    class AgentRegistration {

        @Test
        @DisplayName("should report registered agents")
        void shouldReportRegisteredAgents() {
            CommissionWorkflowOrchestrator orchestrator = new CommissionWorkflowOrchestrator(
                    List.of(
                            new StubGatheringAgent(),
                            new StubComplianceAgent(),
                            new StubAnomalyAgent(),
                            new StubReportAgent()
                    )
            );

            var agents = orchestrator.getRegisteredAgents();

            assertThat(agents).hasSize(4);
            assertThat(agents.get("Data Gathering")).isEqualTo("Stub Gathering Agent");
            assertThat(agents.get("Compliance Check")).isEqualTo("Stub Compliance Agent");
            assertThat(agents.get("Anomaly Analysis")).isEqualTo("Stub Anomaly Agent");
            assertThat(agents.get("Report Generation")).isEqualTo("Stub Report Agent");
        }

        @Test
        @DisplayName("should handle missing stage agent gracefully")
        void shouldHandleMissingAgent() {
            // Only register gathering and report agents (skip compliance and anomaly)
            CommissionWorkflowOrchestrator orchestrator = new CommissionWorkflowOrchestrator(
                    List.of(
                            new StubGatheringAgent(),
                            new StubReportAgent()
                    )
            );

            WorkflowResult result = orchestrator.executeReview("Review Alice");

            // Should still produce a report, with skipped stages logged
            assertThat(result.getFinalReport()).contains("Commission Review Report");
            assertThat(result.getStageLog()).anyMatch(s -> s.contains("Skipped"));
        }
    }

    @Nested
    @DisplayName("WorkflowState")
    class WorkflowStateTests {

        @Test
        @DisplayName("should maintain state integrity across operations")
        void shouldMaintainStateIntegrity() {
            WorkflowState state = new WorkflowState("test request");

            assertThat(state.getOriginalRequest()).isEqualTo("test request");
            assertThat(state.getCurrentStage()).isEqualTo(WorkflowStage.GATHERING);

            state.putData("key1", "value1");
            state.putData("key2", "value2");
            assertThat(state.getData("key1")).isEqualTo("value1");
            assertThat(state.getAllData()).hasSize(2);

            state.addFlag("FLAG_A");
            state.addFlag("FLAG_A"); // duplicate should be ignored
            state.addFlag("FLAG_B");
            assertThat(state.getFlags()).hasSize(2);
            assertThat(state.hasFlag("FLAG_A")).isTrue();
            assertThat(state.hasFlag("FLAG_C")).isFalse();

            state.logStage(WorkflowStage.GATHERING, "Done");
            assertThat(state.getStageLog()).hasSize(1);
            assertThat(state.getStageLog().get(0)).contains("Data Gathering: Done");
        }
    }
}
