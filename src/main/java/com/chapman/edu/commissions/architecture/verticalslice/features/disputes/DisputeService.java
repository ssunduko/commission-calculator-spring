package com.chapman.edu.commissions.architecture.verticalslice.features.disputes;

import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ValidationException;
import com.chapman.edu.commissions.architecture.verticalslice.domain.Dispute;
import com.chapman.edu.commissions.architecture.verticalslice.domain.DisputeDocument;
import com.chapman.edu.commissions.architecture.verticalslice.domain.DisputeStatus;
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

    public DisputeResponse getDispute(String id) {
        Dispute dispute = disputeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dispute", id));
        return DisputeResponse.from(dispute);
    }

    public List<DisputeResponse> getAllDisputes() {
        return disputeRepository.findAll().stream()
            .map(DisputeResponse::from)
            .collect(Collectors.toList());
    }

    public List<DisputeResponse> getDisputesBySalesRep(String salesRepId) {
        return disputeRepository.findBySalesRepId(salesRepId).stream()
            .map(DisputeResponse::from)
            .collect(Collectors.toList());
    }

    public List<DisputeResponse> getDisputesByStatus(DisputeStatus status) {
        return disputeRepository.findByStatus(status).stream()
            .map(DisputeResponse::from)
            .collect(Collectors.toList());
    }

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

    public void deleteDispute(String id) {
        if (!disputeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dispute", id);
        }
        disputeRepository.deleteById(id);
    }

    public DisputeResponse addDocument(String id, AddDocumentRequest request) {
        request.validate();

        Dispute dispute = disputeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dispute", id));

        DisputeDocument doc = new DisputeDocument(
            request.name(),
            request.contentType(),
            request.sizeBytes(),
            request.uploadedBy()
        );
        dispute.addDocument(doc);

        Dispute updated = disputeRepository.save(dispute);
        return DisputeResponse.from(updated);
    }

    public DisputeResponse addComment(String id, AddCommentRequest request) {
        request.validate();

        Dispute dispute = disputeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dispute", id));

        if (request.userId() == null || request.userId().isBlank()) {
            dispute.addSystemComment(request.text());
        } else {
            dispute.addUserComment(request.userId(), request.userName(), request.text());
        }

        Dispute updated = disputeRepository.save(dispute);
        return DisputeResponse.from(updated);
    }
}
