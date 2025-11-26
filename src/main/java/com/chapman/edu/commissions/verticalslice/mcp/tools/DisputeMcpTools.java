package com.chapman.edu.commissions.verticalslice.mcp.tools;

import com.chapman.edu.commissions.verticalslice.domain.DisputeStatus;
import com.chapman.edu.commissions.verticalslice.features.disputes.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP Tools for Dispute Management.
 * Exposes dispute-related operations through the Model Context Protocol.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DisputeMcpTools {

    private final DisputeService disputeService;

    public DisputeResponse createDispute(String calculationId, String salesRepId, String title, String description) {

        log.info("MCP Tool: Creating dispute for calculation={}, salesRep={}", calculationId, salesRepId);

        CreateDisputeRequest request = new CreateDisputeRequest(
                calculationId, salesRepId, title, description
        );
        return disputeService.createDispute(request);
    }

    public DisputeResponse resolveDispute(String disputeId, String resolution, String resolvedBy, Boolean approved) {

        log.info("MCP Tool: Resolving dispute ID={} by {}", disputeId, resolvedBy);

        ResolveDisputeRequest request = new ResolveDisputeRequest(
                resolution,
                resolvedBy,
                approved != null ? approved : false
        );
        return disputeService.resolveDispute(disputeId, request);
    }

    public DisputeResponse getDispute(String disputeId) {

        log.info("MCP Tool: Getting dispute with ID={}", disputeId);
        return disputeService.getDispute(disputeId);
    }

    public List<DisputeResponse> listDisputes(String salesRepId, String status) {

        log.info("MCP Tool: Listing disputes with salesRepId={}, status={}", salesRepId, status);

        if (salesRepId != null && !salesRepId.isBlank()) {
            return disputeService.getDisputesBySalesRep(salesRepId);
        } else if (status != null && !status.isBlank()) {
            DisputeStatus disputeStatus = DisputeStatus.valueOf(status.toUpperCase());
            return disputeService.getDisputesByStatus(disputeStatus);
        } else {
            return disputeService.getAllDisputes();
        }
    }

    public DisputeResponse escalateDispute(String disputeId) {

        log.info("MCP Tool: Escalating dispute ID={}", disputeId);
        return disputeService.escalateDispute(disputeId);
    }

    public String deleteDispute(String disputeId) {

        log.info("MCP Tool: Deleting dispute ID={}", disputeId);
        disputeService.deleteDispute(disputeId);
        return "Dispute " + disputeId + " deleted successfully";
    }
}
