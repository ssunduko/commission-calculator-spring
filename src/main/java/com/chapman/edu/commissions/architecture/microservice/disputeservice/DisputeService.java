package com.chapman.edu.commissions.architecture.microservice.disputeservice;

import com.chapman.edu.commissions.architecture.microservice.common.dto.CreateDisputeRequest;
import com.chapman.edu.commissions.architecture.microservice.common.dto.DisputeDto;
import com.chapman.edu.commissions.architecture.microservice.common.dto.ResolveDisputeRequest;
import com.chapman.edu.commissions.architecture.microservice.disputeservice.domain.Dispute;
import com.chapman.edu.commissions.architecture.microservice.disputeservice.domain.DisputeStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing disputes.
 * Contains business logic for dispute operations in the microservice architecture.
 */
@Service
public class DisputeService {

    private final DisputeRepository disputeRepository;

    public DisputeService(DisputeRepository disputeRepository) {
        this.disputeRepository = disputeRepository;
    }

    public DisputeDto createDispute(CreateDisputeRequest request) {
        request.validate();

        Dispute dispute = new Dispute(
            request.calculationId(),
            request.salesRepId(),
            request.title(),
            request.description()
        );

        Dispute savedDispute = disputeRepository.save(dispute);
        return toDto(savedDispute);
    }

    public DisputeDto getDispute(String id) {
        Dispute dispute = disputeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Dispute not found: " + id));
        return toDto(dispute);
    }

    public List<DisputeDto> getAllDisputes() {
        return disputeRepository.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public List<DisputeDto> getDisputesBySalesRep(String salesRepId) {
        return disputeRepository.findBySalesRepId(salesRepId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public List<DisputeDto> getDisputesByStatus(DisputeStatus status) {
        return disputeRepository.findByStatus(status).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public DisputeDto resolveDispute(String id, ResolveDisputeRequest request) {
        request.validate();

        Dispute dispute = disputeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Dispute not found: " + id));

        if (dispute.getStatus() == DisputeStatus.RESOLVED ||
            dispute.getStatus() == DisputeStatus.APPROVED ||
            dispute.getStatus() == DisputeStatus.REJECTED) {
            throw new RuntimeException("Dispute is already resolved");
        }

        dispute.setResolution(request.resolution());
        dispute.setResolvedBy(request.resolvedBy());
        dispute.setStatus(request.approved() ? DisputeStatus.APPROVED : DisputeStatus.REJECTED);

        Dispute updatedDispute = disputeRepository.save(dispute);
        return toDto(updatedDispute);
    }

    public DisputeDto escalateDispute(String id) {
        Dispute dispute = disputeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Dispute not found: " + id));

        if (dispute.isEscalated()) {
            throw new RuntimeException("Dispute is already escalated");
        }

        dispute.setEscalated(true);

        Dispute updatedDispute = disputeRepository.save(dispute);
        return toDto(updatedDispute);
    }

    public void deleteDispute(String id) {
        if (!disputeRepository.existsById(id)) {
            throw new RuntimeException("Dispute not found: " + id);
        }
        disputeRepository.deleteById(id);
    }

    private DisputeDto toDto(Dispute dispute) {
        return new DisputeDto(dispute.getId(), dispute.getCalculationId(), dispute.getSalesRepId(),
            dispute.getTitle(), dispute.getDescription(), dispute.getStatus().name(),
            dispute.isEscalated(), dispute.getCreatedDate(),
            dispute.getResolution(),
            dispute.getComments() != null ? dispute.getComments().size() : 0);
    }
}
