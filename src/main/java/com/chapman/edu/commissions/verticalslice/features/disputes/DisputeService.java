package com.chapman.edu.commissions.verticalslice.features.disputes;

import com.chapman.edu.commissions.verticalslice.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.verticalslice.infrastructure.exceptions.ValidationException;
import com.chapman.edu.commissions.verticalslice.domain.Dispute;
import com.chapman.edu.commissions.verticalslice.domain.DisputeStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing disputes.
 * Contains business logic for dispute operations.
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
}
