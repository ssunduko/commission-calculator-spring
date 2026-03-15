package com.chapman.edu.commissions.ai.controller;

import com.chapman.edu.commissions.ai.service.agent.AgentResult;
import com.chapman.edu.commissions.ai.service.agent.AgentStep;
import com.chapman.edu.commissions.ai.service.agent.CommissionReActAgent;
import com.chapman.edu.commissions.ai.service.ml.AnomalyDetectionService;
import com.chapman.edu.commissions.ai.service.ml.CommissionExplainerService;
import com.chapman.edu.commissions.ai.service.ml.DisputeAnalysisService;
import com.chapman.edu.commissions.ai.service.ml.ForecastingService;
import com.chapman.edu.commissions.ai.service.moderation.ModerationService;
import com.chapman.edu.commissions.ai.service.rag.CommissionRagService;
import com.chapman.edu.commissions.ai.service.workflow.CommissionWorkflowOrchestrator;
import com.chapman.edu.commissions.ai.service.workflow.WorkflowResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * REST CONTROLLER: CommissionController
 * ============================================================
 *
 * CONCEPT: Exposing AI Capabilities via REST API
 * ------------------------------------------------------------
 * This controller exposes all AI-powered commission services as
 * REST endpoints. It follows the same Spring MVC patterns used
 * in the ORM module's controllers.
 *
 * ENDPOINT CATEGORIES:
 *
 * 1. /api/ai/rag/** — RAG-powered Q&A endpoints
 *    Uses Retrieval-Augmented Generation to answer questions
 *    about commission data with grounded, accurate responses.
 *
 * 2. /api/ai/explain/** — Explainability endpoints
 *    Generates natural language explanations of calculations and plans.
 *
 * 3. /api/ai/disputes/** — Dispute analysis endpoints
 *    AI-powered analysis and triage of commission disputes.
 *
 * 4. /api/ai/forecast/** — Forecasting endpoints
 *    AI-generated commission forecasts for individuals and teams.
 *
 * 5. /api/ai/anomaly/** — Anomaly detection endpoints
 *    Identifies unusual commission calculations that need review.
 *
 * 6. /api/ai/moderation/** — AI moderation and guardrails endpoints
 *    Validates inputs, classifies content, and sanitizes outputs
 *    to ensure safe, on-topic AI interactions.
 *
 * 7. /api/ai/workflow/** — Agentic Workflow endpoints
 *    Multi-agent orchestrated workflows for complex tasks like
 *    commission reviews (data gathering → compliance → anomaly → report).
 *
 * ARCHITECTURE NOTE:
 * The controller is thin — it delegates all logic to service classes.
 * This follows the Spring best practice of keeping controllers as
 * "traffic directors" that route requests to the appropriate service.
 *
 * AI RESPONSE FORMAT:
 * All endpoints return Map<String, String> with an "analysis" or
 * "response" key containing the AI-generated text. This makes it
 * easy for frontend applications to extract and display the content.
 */
@RestController("aiCommissionController")
@RequestMapping("/api/ai")
public class CommissionController {

    private final CommissionRagService ragService;
    private final CommissionExplainerService explainerService;
    private final DisputeAnalysisService disputeAnalysisService;
    private final ForecastingService forecastingService;
    private final AnomalyDetectionService anomalyDetectionService;
    private final ModerationService moderationService;
    private final CommissionReActAgent reActAgent;
    private final CommissionWorkflowOrchestrator workflowOrchestrator;

    public CommissionController(CommissionRagService ragService,
                                CommissionExplainerService explainerService,
                                DisputeAnalysisService disputeAnalysisService,
                                ForecastingService forecastingService,
                                AnomalyDetectionService anomalyDetectionService,
                                ModerationService moderationService,
                                CommissionReActAgent reActAgent,
                                CommissionWorkflowOrchestrator workflowOrchestrator) {
        this.ragService = ragService;
        this.explainerService = explainerService;
        this.disputeAnalysisService = disputeAnalysisService;
        this.forecastingService = forecastingService;
        this.anomalyDetectionService = anomalyDetectionService;
        this.moderationService = moderationService;
        this.reActAgent = reActAgent;
        this.workflowOrchestrator = workflowOrchestrator;
    }

    // ============================================================
    // RAG (Retrieval-Augmented Generation) Endpoints
    // ============================================================

    /**
     * Ask a natural language question about commission data.
     *
     * RAG FLOW:
     * 1. User submits a question via POST body
     * 2. Question is embedded and searched against vector store
     * 3. Relevant documents are retrieved
     * 4. Documents + question are sent to Claude
     * 5. Claude generates a grounded answer
     *
     * Example questions:
     * - "What commission plans are available?"
     * - "How much did the enterprise deal earn?"
     * - "Who are the top performing sales reps?"
     */
    @PostMapping("/rag/ask")
    public ResponseEntity<Map<String, String>> askQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Question is required"));
        }

        // GUARDRAIL: Validate input before sending to AI
        ModerationService.ModerationResult inputCheck = moderationService.validateInput(question);
        if (!inputCheck.isAllowed()) {
            return ResponseEntity.badRequest().body(Map.of("error", inputCheck.getReason()));
        }

        String answer = ragService.answerQuestion(question);

        // GUARDRAIL: Sanitize output before returning to user
        String safeAnswer = moderationService.sanitizeOutput(answer);

        return ResponseEntity.ok(Map.of("question", question, "response", safeAnswer));
    }

    /**
     * Ask a question filtered by document type.
     *
     * This endpoint demonstrates FILTERED RAG — narrowing the search
     * to a specific category of documents for more focused answers.
     *
     * Valid types: deal, commission_plan, commission_calculation, user
     */
    @PostMapping("/rag/ask/{type}")
    public ResponseEntity<Map<String, String>> askTypedQuestion(
            @PathVariable String type,
            @RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Question is required"));
        }
        String answer = ragService.answerTypedQuestion(question, type);
        return ResponseEntity.ok(Map.of("question", question, "type", type, "response", answer));
    }

    /**
     * Generate a comprehensive performance report for a sales rep.
     *
     * This endpoint demonstrates MULTI-RETRIEVAL RAG — gathering
     * context from multiple document types to produce a rich report.
     */
    @GetMapping("/rag/report/{salesRepName}")
    public ResponseEntity<Map<String, String>> generateReport(@PathVariable String salesRepName) {
        String report = ragService.generatePerformanceReport(salesRepName);
        return ResponseEntity.ok(Map.of("salesRep", salesRepName, "report", report));
    }

    // ============================================================
    // Commission Explanation Endpoints
    // ============================================================

    /**
     * Explain a commission calculation in natural language.
     *
     * Uses Claude to generate a human-readable explanation of how
     * a specific commission was calculated, including rate breakdowns
     * and contextual insights.
     */
    @GetMapping("/explain/calculation/{calculationId}")
    public ResponseEntity<Map<String, String>> explainCalculation(
            @PathVariable String calculationId) {
        String explanation = explainerService.explainCalculation(calculationId);
        return ResponseEntity.ok(Map.of("calculationId", calculationId, "explanation", explanation));
    }

    /**
     * Explain a commission plan's structure in plain language.
     *
     * Uses Claude to describe plan tiers, rates, and bonuses in
     * terms a new sales representative would understand.
     */
    @GetMapping("/explain/plan/{planId}")
    public ResponseEntity<Map<String, String>> explainPlan(@PathVariable String planId) {
        String explanation = explainerService.explainPlan(planId);
        return ResponseEntity.ok(Map.of("planId", planId, "explanation", explanation));
    }

    // ============================================================
    // Dispute Analysis Endpoints
    // ============================================================

    /**
     * Perform a full AI analysis of a commission dispute.
     *
     * Returns: validity assessment, supporting/weakening factors,
     * recommended resolution, and next steps.
     */
    @GetMapping("/disputes/analyze/{disputeId}")
    public ResponseEntity<Map<String, String>> analyzeDispute(@PathVariable String disputeId) {
        String analysis = disputeAnalysisService.analyzeDispute(disputeId);
        return ResponseEntity.ok(Map.of("disputeId", disputeId, "analysis", analysis));
    }

    /**
     * Quick triage of a dispute for priority assessment.
     *
     * Returns: HIGH, MEDIUM, or LOW priority with brief reasoning.
     * Useful for queue management and escalation workflows.
     */
    @GetMapping("/disputes/triage/{disputeId}")
    public ResponseEntity<Map<String, String>> triageDispute(@PathVariable String disputeId) {
        String triage = disputeAnalysisService.triageDispute(disputeId);
        return ResponseEntity.ok(Map.of("disputeId", disputeId, "triage", triage));
    }

    // ============================================================
    // Forecasting Endpoints
    // ============================================================

    /**
     * Generate a commission forecast for an individual sales rep.
     *
     * Uses historical calculation data and current pipeline to
     * project future commission earnings.
     */
    @GetMapping("/forecast/user/{userId}")
    public ResponseEntity<Map<String, String>> forecastUser(@PathVariable String userId) {
        String forecast = forecastingService.forecastCommissions(userId);
        return ResponseEntity.ok(Map.of("userId", userId, "forecast", forecast));
    }

    /**
     * Generate a team-level commission forecast.
     *
     * Aggregates data across all sales reps for an organizational view.
     */
    @GetMapping("/forecast/team")
    public ResponseEntity<Map<String, String>> forecastTeam() {
        String forecast = forecastingService.forecastTeamCommissions();
        return ResponseEntity.ok(Map.of("forecast", forecast));
    }

    // ============================================================
    // Anomaly Detection Endpoints
    // ============================================================

    /**
     * Run anomaly detection across all commission calculations.
     *
     * Identifies calculations that are statistically unusual
     * and provides AI-powered analysis of each anomaly.
     */
    @GetMapping("/anomaly/detect")
    public ResponseEntity<Map<String, String>> detectAnomalies() {
        String analysis = anomalyDetectionService.detectAnomalies();
        return ResponseEntity.ok(Map.of("analysis", analysis));
    }

    /**
     * Check a specific calculation for anomalies.
     *
     * Quick check whether a single commission calculation is
     * within normal ranges compared to the population.
     */
    @GetMapping("/anomaly/check/{calculationId}")
    public ResponseEntity<Map<String, String>> checkAnomaly(@PathVariable String calculationId) {
        String result = anomalyDetectionService.checkSingleCalculation(calculationId);
        return ResponseEntity.ok(Map.of("calculationId", calculationId, "result", result));
    }

    // ============================================================
    // Moderation and Guardrails Endpoints
    // ============================================================

    /**
     * Validate user input against all guardrail checks.
     *
     * Runs the full input validation pipeline (length, injection,
     * topic boundary) and returns whether the input is allowed.
     *
     * GUARDRAILS APPLIED:
     * 1. Empty/null check
     * 2. Length limit (max 2000 characters)
     * 3. Prompt injection pattern detection
     * 4. Topic boundary enforcement (commission domain)
     */
    @PostMapping("/moderation/validate")
    public ResponseEntity<Map<String, String>> validateInput(@RequestBody Map<String, String> request) {
        String input = request.get("input");
        ModerationService.ModerationResult result = moderationService.validateInput(input);

        if (result.isAllowed()) {
            return ResponseEntity.ok(Map.of("status", "ALLOWED", "message", "Input passed all guardrail checks."));
        }
        return ResponseEntity.ok(Map.of("status", "BLOCKED", "reason", result.getReason()));
    }

    /**
     * Run AI-powered content classification on user input.
     *
     * Uses the AI model to determine whether the input is an
     * appropriate commission-related query. This catches nuanced
     * attacks that keyword-based filters miss.
     *
     * NOTE: This endpoint makes an extra AI API call, so it's
     * more expensive than the /validate endpoint.
     */
    @PostMapping("/moderation/classify")
    public ResponseEntity<Map<String, String>> classifyInput(@RequestBody Map<String, String> request) {
        String input = request.get("input");
        if (input == null || input.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Input is required"));
        }

        ModerationService.ModerationResult result = moderationService.classifyInput(input);

        if (result.isAllowed()) {
            return ResponseEntity.ok(Map.of("status", "ALLOWED", "message", "AI classified input as appropriate."));
        }
        return ResponseEntity.ok(Map.of("status", "BLOCKED", "reason", result.getReason()));
    }

    /**
     * Sanitize AI-generated text by redacting sensitive data.
     *
     * Scans the provided text for patterns matching SSNs, credit
     * card numbers, email addresses, and API keys, replacing them
     * with [REDACTED-*] placeholders.
     */
    @PostMapping("/moderation/sanitize")
    public ResponseEntity<Map<String, String>> sanitizeOutput(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Text is required"));
        }

        String sanitized = moderationService.sanitizeOutput(text);
        boolean wasModified = !sanitized.equals(text);

        return ResponseEntity.ok(Map.of(
                "original_length", String.valueOf(text.length()),
                "sanitized", sanitized,
                "redacted", String.valueOf(wasModified)
        ));
    }

    // ============================================================
    // ReAct Agent Endpoints
    // ============================================================

    /**
     * Execute the ReAct agent to answer a complex commission question.
     *
     * The agent reasons step-by-step, using tools to look up data
     * from the database and vector store, performing calculations,
     * and building up context before producing a final answer.
     *
     * REACT LOOP:
     * 1. AI thinks about what to do next (Thought)
     * 2. AI selects and calls a tool (Action)
     * 3. Tool returns data (Observation)
     * 4. Repeat until AI has enough info → Final Answer
     *
     * Example questions:
     * - "How much commission did Alice earn on her enterprise deals?"
     * - "Compare the top sales reps by total commission"
     * - "What rate applies to a $150,000 deal under the Standard Plan?"
     */
    @PostMapping("/agent/ask")
    public ResponseEntity<Map<String, Object>> agentAsk(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Question is required"));
        }

        // Validate input through moderation
        ModerationService.ModerationResult inputCheck = moderationService.validateInput(question);
        if (!inputCheck.isAllowed()) {
            return ResponseEntity.badRequest().body(Map.of("error", inputCheck.getReason()));
        }

        AgentResult result = reActAgent.execute(question);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("question", result.getOriginalQuestion());
        response.put("answer", moderationService.sanitizeOutput(result.getFinalAnswer()));
        response.put("success", result.isSuccess());
        response.put("totalSteps", result.getTotalSteps());

        // Include reasoning chain for transparency
        List<Map<String, String>> steps = result.getSteps().stream()
                .map(step -> Map.of(
                        "thought", step.getThought(),
                        "action", step.getAction() + "[" + step.getActionInput() + "]",
                        "observation", step.getObservation()
                ))
                .toList();
        response.put("reasoningChain", steps);

        return ResponseEntity.ok(response);
    }

    /**
     * List all tools available to the ReAct agent.
     *
     * Returns the name and description of each registered tool,
     * so users know what capabilities the agent has.
     */
    @GetMapping("/agent/tools")
    public ResponseEntity<Map<String, Object>> agentTools() {
        Map<String, Object> response = new LinkedHashMap<>();
        reActAgent.getTools().forEach((name, tool) ->
                response.put(name, tool.getDescription()));
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // Agentic Workflow Endpoints
    // ============================================================

    /**
     * Execute a full commission review workflow using multiple AI agents.
     *
     * AGENTIC WORKFLOW:
     * Unlike the ReAct agent (single agent + tools), this endpoint
     * orchestrates MULTIPLE specialized AI agents in a pipeline:
     *
     * 1. Data Gathering Agent → collects all commission data
     * 2. Compliance Check Agent → validates against plan rules
     * 3. Anomaly Analysis Agent → detects statistical outliers
     * 4. Report Generation Agent → synthesizes a final review
     *
     * Each agent has its own AI persona and writes findings to
     * shared state. The orchestrator manages sequencing and
     * early termination.
     *
     * Example requests:
     * - "Review Alice Johnson's commission performance"
     * - "Audit Bob Smith's commission calculations"
     * - "Generate a commission review for the enterprise team"
     */
    @PostMapping("/workflow/review")
    public ResponseEntity<Map<String, Object>> workflowReview(@RequestBody Map<String, String> request) {
        String reviewRequest = request.get("request");
        if (reviewRequest == null || reviewRequest.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request is required"));
        }

        // Validate input through moderation
        ModerationService.ModerationResult inputCheck = moderationService.validateInput(reviewRequest);
        if (!inputCheck.isAllowed()) {
            return ResponseEntity.badRequest().body(Map.of("error", inputCheck.getReason()));
        }

        WorkflowResult result = workflowOrchestrator.executeReview(reviewRequest);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("request", result.getOriginalRequest());
        response.put("report", moderationService.sanitizeOutput(result.getFinalReport()));
        response.put("success", result.isSuccess());
        response.put("totalStages", result.getTotalStages());
        response.put("stageLog", result.getStageLog());
        response.put("flags", result.getFlags());

        return ResponseEntity.ok(response);
    }

    /**
     * List the agents registered in the workflow pipeline.
     *
     * Returns each stage and its assigned agent, so users
     * understand the workflow structure.
     */
    @GetMapping("/workflow/agents")
    public ResponseEntity<Map<String, String>> workflowAgents() {
        return ResponseEntity.ok(workflowOrchestrator.getRegisteredAgents());
    }
}
