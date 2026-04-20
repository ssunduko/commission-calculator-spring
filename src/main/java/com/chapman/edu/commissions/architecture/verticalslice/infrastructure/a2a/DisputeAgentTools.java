package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.a2a;

import com.chapman.edu.commissions.architecture.verticalslice.domain.DisputePriority;
import com.chapman.edu.commissions.architecture.verticalslice.features.calculations.CommissionCalculationResponse;
import com.chapman.edu.commissions.architecture.verticalslice.features.calculations.CommissionCalculationService;
import com.chapman.edu.commissions.architecture.verticalslice.features.disputes.CreateDisputeRequest;
import com.chapman.edu.commissions.architecture.verticalslice.features.disputes.DisputeResponse;
import com.chapman.edu.commissions.architecture.verticalslice.features.disputes.DisputeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tools the dispute-filing A2A agent can call. Each method is bound to the
 * agent's ChatClient via defaultTools(...) — the LLM picks which to invoke
 * based on the incoming task message.
 */
@Component
public class DisputeAgentTools {

    private static final Logger log = LoggerFactory.getLogger(DisputeAgentTools.class);

    private final DisputeService disputeService;
    private final CommissionCalculationService calculationService;

    public DisputeAgentTools(DisputeService disputeService,
                             CommissionCalculationService calculationService) {
        this.disputeService = disputeService;
        this.calculationService = calculationService;
    }

    @Tool(description = "List all commission calculations. Returns id, dealId, salesRepId, and amounts. "
        + "Use this first when you need a valid calculationId or salesRepId for filing a dispute.")
    public List<CommissionCalculationResponse> listCalculations() {
        return calculationService.getAllCalculations();
    }

    @Tool(description = "Find commission calculations for a given sales rep user id. "
        + "Returns every calculation attributed to that rep.")
    public List<CommissionCalculationResponse> listCalculationsForSalesRep(
        @ToolParam(description = "Sales rep user id") String salesRepId) {
        return calculationService.getCalculationsBySalesRep(salesRepId);
    }

    @Tool(description = "Get a single commission calculation by id. "
        + "Use this to verify a calculationId exists before filing a dispute.")
    public CommissionCalculationResponse getCalculation(
        @ToolParam(description = "Calculation UUID") String id) {
        return calculationService.getCalculation(id);
    }

    @Tool(description = "File a new commission dispute. Requires an existing calculationId and "
        + "salesRepId (normally the calculation's own salesRepId). Priority is one of "
        + "LOW, MEDIUM, HIGH, URGENT; defaults to MEDIUM when omitted.")
    public DisputeResponse createDispute(
        @ToolParam(description = "Commission calculation UUID to dispute") String calculationId,
        @ToolParam(description = "Sales rep user id (typically the calculation's salesRepId)") String salesRepId,
        @ToolParam(description = "Short title describing the dispute") String title,
        @ToolParam(description = "Detailed explanation of the dispute issue") String description,
        @ToolParam(description = "LOW | MEDIUM | HIGH | URGENT (optional; pass null for MEDIUM)") String priority) {
        DisputePriority p = null;
        if (priority != null && !priority.isBlank()) {
            try {
                p = DisputePriority.valueOf(priority.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                log.warn("A2A agent supplied invalid priority '{}', defaulting to MEDIUM", priority);
            }
        }
        CreateDisputeRequest request = new CreateDisputeRequest(calculationId, salesRepId, title, description, p);
        return disputeService.createDispute(request);
    }

    @Tool(description = "List existing disputes so the agent can verify a freshly-filed dispute "
        + "or report on open items.")
    public List<DisputeResponse> listDisputes() {
        return disputeService.getAllDisputes();
    }

    @Tool(description = "Get a single dispute by id. Use to confirm a filing succeeded.")
    public DisputeResponse getDispute(
        @ToolParam(description = "Dispute UUID") String id) {
        return disputeService.getDispute(id);
    }
}
