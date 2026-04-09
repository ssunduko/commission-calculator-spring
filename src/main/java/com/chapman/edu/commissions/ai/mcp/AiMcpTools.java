package com.chapman.edu.commissions.ai.mcp;

import com.chapman.edu.commissions.ai.service.ml.AnomalyDetectionService;
import com.chapman.edu.commissions.ai.service.ml.CommissionExplainerService;
import com.chapman.edu.commissions.ai.service.ml.DisputeAnalysisService;
import com.chapman.edu.commissions.ai.service.ml.ForecastingService;
import com.chapman.edu.commissions.ai.service.rag.CommissionRagService;
import com.chapman.edu.commissions.ai.service.workflow.CommissionWorkflowOrchestrator;
import com.chapman.edu.commissions.ai.service.workflow.WorkflowResult;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * MCP Tools for the AI module.
 *
 * Exposes AI-powered commission analysis capabilities as MCP tools
 * accessible via the Streamable HTTP MCP endpoint at /mcp.
 *
 * These tools wrap the AI services (which use Claude via Spring AI ChatClient)
 * and make them available to external MCP clients like Claude Desktop.
 */
@Service
public class AiMcpTools {

    private final CommissionExplainerService explainerService;
    private final ForecastingService forecastingService;
    private final AnomalyDetectionService anomalyDetectionService;
    private final DisputeAnalysisService disputeAnalysisService;
    private final CommissionRagService ragService;
    private final CommissionWorkflowOrchestrator workflowOrchestrator;

    public AiMcpTools(@Lazy CommissionExplainerService explainerService,
                       @Lazy ForecastingService forecastingService,
                       @Lazy AnomalyDetectionService anomalyDetectionService,
                       @Lazy DisputeAnalysisService disputeAnalysisService,
                       @Lazy CommissionRagService ragService,
                       @Lazy CommissionWorkflowOrchestrator workflowOrchestrator) {
        this.explainerService = explainerService;
        this.forecastingService = forecastingService;
        this.anomalyDetectionService = anomalyDetectionService;
        this.disputeAnalysisService = disputeAnalysisService;
        this.ragService = ragService;
        this.workflowOrchestrator = workflowOrchestrator;
    }

    // ============================================================
    // Commission Explainer Tools
    // ============================================================

    @Tool(name = "explainCommissionCalculation",
          description = "Use AI to explain how a specific commission calculation was computed, including the plan rules, tiers, and adjustments applied. Provide the calculation ID.")
    public String explainCalculation(
            @ToolParam(description = "The commission calculation ID to explain") String calculationId) {
        return explainerService.explainCalculation(calculationId);
    }

    @Tool(name = "explainCommissionPlan",
          description = "Use AI to explain a commission plan in plain language, including its rules, tiers, rates, and how commissions are calculated under this plan. Provide the plan ID.")
    public String explainPlan(
            @ToolParam(description = "The commission plan ID to explain") String planId) {
        return explainerService.explainPlan(planId);
    }

    // ============================================================
    // Forecasting Tools
    // ============================================================

    @Tool(name = "forecastCommissions",
          description = "Use AI to forecast future commission earnings for a specific sales rep based on their historical deal performance and current pipeline. Provide the user/sales rep ID.")
    public String forecastCommissions(
            @ToolParam(description = "The sales rep user ID to forecast commissions for") String userId) {
        return forecastingService.forecastCommissions(userId);
    }

    @Tool(name = "forecastTeamCommissions",
          description = "Use AI to forecast commission earnings for the entire sales team, analyzing trends across all reps and providing team-level projections.")
    public String forecastTeamCommissions() {
        return forecastingService.forecastTeamCommissions();
    }

    // ============================================================
    // Anomaly Detection Tools
    // ============================================================

    @Tool(name = "detectCommissionAnomalies",
          description = "Use AI to scan all commission calculations for anomalies such as unusually high/low commissions, suspicious patterns, or calculation errors.")
    public String detectAnomalies() {
        return anomalyDetectionService.detectAnomalies();
    }

    @Tool(name = "checkCalculationForAnomalies",
          description = "Use AI to check a specific commission calculation for anomalies or irregularities. Provide the calculation ID.")
    public String checkSingleCalculation(
            @ToolParam(description = "The commission calculation ID to check") String calculationId) {
        return anomalyDetectionService.checkSingleCalculation(calculationId);
    }

    // ============================================================
    // Dispute Analysis Tools
    // ============================================================

    @Tool(name = "analyzeDispute",
          description = "Use AI to perform a detailed analysis of a commission dispute, examining the calculation, plan rules, and the rep's claim to recommend a resolution. Provide the dispute ID.")
    public String analyzeDispute(
            @ToolParam(description = "The dispute ID to analyze") String disputeId) {
        return disputeAnalysisService.analyzeDispute(disputeId);
    }

    @Tool(name = "triageDispute",
          description = "Use AI to quickly triage a commission dispute, categorizing its severity and recommending whether to approve, reject, or escalate. Provide the dispute ID.")
    public String triageDispute(
            @ToolParam(description = "The dispute ID to triage") String disputeId) {
        return disputeAnalysisService.triageDispute(disputeId);
    }

    // ============================================================
    // RAG (Retrieval-Augmented Generation) Tools
    // ============================================================

    @Tool(name = "askCommissionQuestion",
          description = "Ask a natural language question about commissions, plans, deals, or calculations. Uses RAG (Retrieval-Augmented Generation) to search the knowledge base and provide an accurate, grounded answer.")
    public String answerQuestion(
            @ToolParam(description = "The question to answer about commissions") String question) {
        return ragService.answerQuestion(question);
    }

    @Tool(name = "generatePerformanceReport",
          description = "Use AI to generate a comprehensive performance report for a specific sales representative, combining deal data, commission history, and AI analysis.")
    public String generatePerformanceReport(
            @ToolParam(description = "The name of the sales rep to generate a report for") String salesRepName) {
        return ragService.generatePerformanceReport(salesRepName);
    }

    // ============================================================
    // Workflow Orchestration Tools
    // ============================================================

    @Tool(name = "executeCommissionReview",
          description = "Execute a multi-agent AI workflow to review commissions. This orchestrates multiple AI agents (data gathering, anomaly analysis, compliance check, report generation) to produce a comprehensive review.")
    public WorkflowResult executeReview(
            @ToolParam(description = "Natural language description of what to review, e.g. 'Review all Q1 commissions for anomalies'") String request) {
        return workflowOrchestrator.executeReview(request);
    }
}
