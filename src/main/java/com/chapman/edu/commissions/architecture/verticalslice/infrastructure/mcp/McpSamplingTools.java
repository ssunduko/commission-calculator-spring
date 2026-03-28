package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.mcp;

import com.chapman.edu.commissions.architecture.verticalslice.features.calculations.CommissionCalculationService;
import com.chapman.edu.commissions.architecture.verticalslice.features.deals.DealService;
import com.chapman.edu.commissions.architecture.verticalslice.features.disputes.DisputeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP Sampling Tools — Server-side tools that request LLM completions from the client.
 *
 * CONCEPT: MCP Sampling
 * ─────────────────────
 * Normal MCP flow:   Client (Claude) → calls tool → Server (this app)
 * Sampling flow:     Server (this app) → requests completion → Client (Claude)
 *
 * Sampling lets a tool handler ask the connected AI client to "think" about
 * data that the server has gathered, combining server-side data access with
 * client-side AI reasoning in a single tool invocation.
 *
 * ARCHITECTURE:
 *
 *   ┌─────────────┐                        ┌──────────────────┐
 *   │  AI Client   │  1. calls tool         │  MCP Server      │
 *   │  (Claude)    │ ─────────────────────→ │  (Spring Boot)   │
 *   │              │                        │                  │
 *   │              │  2. server gathers     │  - fetch deals   │
 *   │              │     data from DB       │  - fetch calcs   │
 *   │              │                        │  - fetch disputes│
 *   │              │                        │                  │
 *   │              │  3. createMessage()    │  - builds prompt │
 *   │              │ ←───────────────────── │    with data     │
 *   │              │                        │                  │
 *   │  4. Claude   │  5. returns            │                  │
 *   │  generates   │     completion         │  6. returns      │
 *   │  response    │ ─────────────────────→ │     result       │
 *   └─────────────┘                        └──────────────────┘
 *
 * These tools CANNOT use @Tool annotation because they need access to
 * McpSyncServerExchange for the createMessage() call. They are registered
 * directly as McpServerFeatures.SyncToolSpecification beans.
 */
@Component
public class McpSamplingTools {

    private static final Logger log = LoggerFactory.getLogger(McpSamplingTools.class);

    private final CommissionCalculationService calculationService;
    private final DealService dealService;
    private final DisputeService disputeService;
    private final ObjectMapper objectMapper;

    public McpSamplingTools(CommissionCalculationService calculationService,
                            DealService dealService,
                            DisputeService disputeService,
                            ObjectMapper objectMapper) {
        this.calculationService = calculationService;
        this.dealService = dealService;
        this.disputeService = disputeService;
        this.objectMapper = objectMapper;
    }

    /**
     * Returns all sampling-enabled tool specifications.
     * Each tool receives the McpSyncServerExchange to call createMessage().
     */
    public List<McpServerFeatures.SyncToolSpecification> getToolSpecifications() {
        return List.of(
                explainCommissionTool(),
                analyzeDisputeTool(),
                salesPerformanceSummaryTool()
        );
    }

    // ================================================================
    // Tool 1: Explain Commission Calculation
    // ================================================================

    private McpServerFeatures.SyncToolSpecification explainCommissionTool() {
        var schema = McpSchema.Tool.builder()
                .name("explainCommission")
                .description("Explain a commission calculation in plain language using AI. " +
                        "The server fetches the calculation, deal, and plan data, then asks " +
                        "the connected AI client to generate a human-readable explanation. " +
                        "Requires: calculationId (string).")
                .inputSchema(new McpSchema.JsonSchema(
                        "object",
                        Map.of("calculationId", Map.of("type", "string", "description", "Commission calculation ID")),
                        List.of("calculationId"),
                        null, null, null
                ))
                .build();

        return new McpServerFeatures.SyncToolSpecification(schema, (exchange, args) -> {
            String calcId = getStringArg(args, "calculationId");
            log.info("[Sampling] explainCommission for calculation: {}", calcId);

            try {
                var calc = calculationService.getCalculation(calcId);
                String dataContext = objectMapper.writeValueAsString(calc);

                String prompt = "You are a commission calculation expert. Explain this commission " +
                        "calculation in clear, non-technical language that a sales rep would understand. " +
                        "Include the deal value, commission rate applied, base commission, and final amount. " +
                        "Here is the calculation data:\n\n" + dataContext;

                String explanation = requestSampling(exchange, prompt);
                return textResult(explanation);
            } catch (Exception e) {
                log.error("[Sampling] Error explaining commission", e);
                return textResult("Error: " + e.getMessage());
            }
        });
    }

    // ================================================================
    // Tool 2: Analyze Dispute
    // ================================================================

    private McpServerFeatures.SyncToolSpecification analyzeDisputeTool() {
        var schema = McpSchema.Tool.builder()
                .name("analyzeDispute")
                .description("Analyze a commission dispute using AI. The server fetches the dispute " +
                        "details and related calculation, then asks the connected AI client to assess " +
                        "validity and recommend resolution. Requires: disputeId (string).")
                .inputSchema(new McpSchema.JsonSchema(
                        "object",
                        Map.of("disputeId", Map.of("type", "string", "description", "Dispute ID to analyze")),
                        List.of("disputeId"),
                        null, null, null
                ))
                .build();

        return new McpServerFeatures.SyncToolSpecification(schema, (exchange, args) -> {
            String disputeId = getStringArg(args, "disputeId");
            log.info("[Sampling] analyzeDispute for dispute: {}", disputeId);

            try {
                var dispute = disputeService.getDispute(disputeId);
                String disputeData = objectMapper.writeValueAsString(dispute);

                // Also fetch the related calculation if available
                String calcData = "Not available";
                try {
                    var calc = calculationService.getCalculation(dispute.calculationId());
                    calcData = objectMapper.writeValueAsString(calc);
                } catch (Exception ignored) {}

                String prompt = "You are a commission dispute analyst. Analyze this dispute and provide:\n" +
                        "1. Assessment of whether the dispute appears valid\n" +
                        "2. Key factors in the dispute\n" +
                        "3. Recommended resolution (approve, reject, or request more info)\n" +
                        "4. Suggested resolution notes\n\n" +
                        "Dispute data:\n" + disputeData + "\n\n" +
                        "Related calculation:\n" + calcData;

                String analysis = requestSampling(exchange, prompt);
                return textResult(analysis);
            } catch (Exception e) {
                log.error("[Sampling] Error analyzing dispute", e);
                return textResult("Error: " + e.getMessage());
            }
        });
    }

    // ================================================================
    // Tool 3: Sales Performance Summary
    // ================================================================

    private McpServerFeatures.SyncToolSpecification salesPerformanceSummaryTool() {
        var schema = McpSchema.Tool.builder()
                .name("summarizeSalesPerformance")
                .description("Generate an AI-powered sales performance summary for a rep. " +
                        "The server fetches all deals, calculations, and disputes for the rep, " +
                        "then asks the AI client to produce insights and recommendations. " +
                        "Requires: salesRepId (string).")
                .inputSchema(new McpSchema.JsonSchema(
                        "object",
                        Map.of("salesRepId", Map.of("type", "string", "description", "Sales representative ID")),
                        List.of("salesRepId"),
                        null, null, null
                ))
                .build();

        return new McpServerFeatures.SyncToolSpecification(schema, (exchange, args) -> {
            String repId = getStringArg(args, "salesRepId");
            log.info("[Sampling] summarizeSalesPerformance for rep: {}", repId);

            try {
                var deals = dealService.getDealsBySalesRep(repId);
                var calculations = calculationService.getCalculationsBySalesRep(repId);
                var disputes = disputeService.getDisputesBySalesRep(repId);

                String prompt = "You are a sales performance analyst. Based on the following data, " +
                        "provide a comprehensive performance summary including:\n" +
                        "1. Total deals and their value breakdown (won/lost/open)\n" +
                        "2. Commission earnings summary\n" +
                        "3. Any disputes and their impact\n" +
                        "4. Key strengths and areas for improvement\n" +
                        "5. Actionable recommendations\n\n" +
                        "Deals (" + deals.size() + "):\n" + objectMapper.writeValueAsString(deals) + "\n\n" +
                        "Commissions (" + calculations.size() + "):\n" + objectMapper.writeValueAsString(calculations) + "\n\n" +
                        "Disputes (" + disputes.size() + "):\n" + objectMapper.writeValueAsString(disputes);

                String summary = requestSampling(exchange, prompt);
                return textResult(summary);
            } catch (Exception e) {
                log.error("[Sampling] Error summarizing sales performance", e);
                return textResult("Error: " + e.getMessage());
            }
        });
    }

    // ================================================================
    // Helpers
    // ================================================================

    /**
     * Request an LLM completion from the connected MCP client via sampling.
     */
    private String requestSampling(McpSyncServerExchange exchange, String prompt) {
        var samplingRequest = McpSchema.CreateMessageRequest.builder()
                .messages(List.of(new McpSchema.SamplingMessage(
                        McpSchema.Role.USER,
                        new McpSchema.TextContent(prompt)
                )))
                .maxTokens(1024)
                .build();

        McpSchema.CreateMessageResult result = exchange.createMessage(samplingRequest);
        log.info("[Sampling] Received response, model: {}, stopReason: {}",
                result.model(), result.stopReason());

        if (result.content() instanceof McpSchema.TextContent text) {
            return text.text();
        }
        return result.content().toString();
    }

    @SuppressWarnings("unchecked")
    private String getStringArg(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required argument: " + key);
        }
        return value.toString();
    }

    private McpSchema.CallToolResult textResult(String text) {
        List<McpSchema.Content> content = List.of(new McpSchema.TextContent(text));
        return new McpSchema.CallToolResult(content, Boolean.FALSE);
    }
}
