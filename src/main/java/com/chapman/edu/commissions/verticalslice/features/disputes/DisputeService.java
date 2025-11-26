package com.chapman.edu.commissions.verticalslice.features.disputes;

import com.chapman.edu.commissions.verticalslice.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.verticalslice.infrastructure.exceptions.ValidationException;
import com.chapman.edu.commissions.verticalslice.domain.Dispute;
import com.chapman.edu.commissions.verticalslice.domain.DisputeStatus;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing disputes.
 * Contains business logic for dispute operations.
 * Exposed as MCP tools for AI agent access.
 */
@Service
public class DisputeService {
    private final DisputeRepository disputeRepository;

    public DisputeService(DisputeRepository disputeRepository) {
        this.disputeRepository = disputeRepository;
    }

    @Tool(name = "createDispute",
            description = "Create a new dispute for a commission calculation. Specify calculation ID, sales rep ID, title, and description.")
    public DisputeResponse createDispute(CreateDisputeRequest request) {
        request.validate();

        Dispute dispute = new Dispute(
            request.calculationId(),
            request.salesRepId(),
            request.title(),
            request.description()
        );

        Dispute savedDispute = disputeRepository.save(dispute);
        return DisputeResponse.from(savedDispute);
    }

    @Tool(name = "getDispute",
            description = "Get a dispute by its ID. Returns the dispute details including status and resolution.")
    public DisputeResponse getDispute(String id) {
        Dispute dispute = disputeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dispute", id));
        return DisputeResponse.from(dispute);
    }

    @Tool(name = "getAllDisputes",
            description = "Get all disputes in the system. Returns a list of all disputes with their details.")
    public List<DisputeResponse> getAllDisputes() {
        return disputeRepository.findAll().stream()
            .map(DisputeResponse::from)
            .collect(Collectors.toList());
    }

    @Tool(name = "getDisputesBySalesRep",
            description = "Get all disputes for a specific sales representative. Specify the sales rep ID.")
    public List<DisputeResponse> getDisputesBySalesRep(String salesRepId) {
        return disputeRepository.findBySalesRepId(salesRepId).stream()
            .map(DisputeResponse::from)
            .collect(Collectors.toList());
    }

    @Tool(name = "getDisputesByStatus",
            description = "Get all disputes with a specific status (OPEN, IN_REVIEW, RESOLVED, APPROVED, REJECTED). Specify the status.")
    public List<DisputeResponse> getDisputesByStatus(DisputeStatus status) {
        return disputeRepository.findByStatus(status).stream()
            .map(DisputeResponse::from)
            .collect(Collectors.toList());
    }

    @Tool(name = "resolveDispute",
            description = "Resolve a dispute by approving or rejecting it. Specify dispute ID, resolution notes, resolver ID, and approved status.")
    public DisputeResponse resolveDispute(String id, ResolveDisputeRequest request) {
        request.validate();

        Dispute dispute = disputeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dispute", id));

        if (dispute.getStatus() == DisputeStatus.RESOLVED ||
            dispute.getStatus() == DisputeStatus.APPROVED ||
            dispute.getStatus() == DisputeStatus.REJECTED) {
            throw new ValidationException("Dispute is already resolved");
        }

        dispute.setResolution(request.resolution());
        dispute.setResolvedBy(request.resolvedBy());
        dispute.setStatus(request.approved() ? DisputeStatus.APPROVED : DisputeStatus.REJECTED);

        Dispute updatedDispute = disputeRepository.save(dispute);
        return DisputeResponse.from(updatedDispute);
    }

    @Tool(name = "escalateDispute",
            description = "Escalate a dispute to higher management. Specify the dispute ID.")
    public DisputeResponse escalateDispute(String id) {
        Dispute dispute = disputeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dispute", id));

        if (dispute.isEscalated()) {
            throw new ValidationException("Dispute is already escalated");
        }

        dispute.setEscalated(true);

        Dispute updatedDispute = disputeRepository.save(dispute);
        return DisputeResponse.from(updatedDispute);
    }

    @Tool(name = "deleteDispute",
            description = "Delete a dispute by its ID. This permanently removes the dispute from the system.")
    public void deleteDispute(String id) {
        if (!disputeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dispute", id);
        }
        disputeRepository.deleteById(id);
    }
}
