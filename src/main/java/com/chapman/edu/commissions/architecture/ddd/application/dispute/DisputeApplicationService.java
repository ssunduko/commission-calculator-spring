package com.chapman.edu.commissions.architecture.ddd.application.dispute;

import com.chapman.edu.commissions.architecture.ddd.application.dto.*;
import com.chapman.edu.commissions.architecture.ddd.domain.dispute.Dispute;
import com.chapman.edu.commissions.architecture.ddd.domain.dispute.DisputeRepository;
import com.chapman.edu.commissions.architecture.ddd.domain.dispute.DisputeStatus;
import com.chapman.edu.commissions.architecture.ddd.domain.shared.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application Service for dispute use cases.
 *
 * Orchestrates dispute creation, resolution, escalation, and retrieval
 * by coordinating the Dispute aggregate and its repository.
 */
@Service
@Transactional
public class DisputeApplicationService {

    private static final Logger log = LoggerFactory.getLogger(DisputeApplicationService.class);
    private final DisputeRepository disputeRepository;

    public DisputeApplicationService(DisputeRepository disputeRepository) {
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

        Dispute saved = disputeRepository.save(dispute);
        log.info("Dispute created: id={}, title={}", saved.getId(), saved.getTitle());
        return DisputeDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public DisputeDto getDispute(String id) {
        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new DomainException("Dispute not found: " + id));
        return DisputeDto.fromEntity(dispute);
    }

    @Transactional(readOnly = true)
    public List<DisputeDto> getAllDisputes() {
        return disputeRepository.findAll().stream().map(DisputeDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<DisputeDto> getDisputesBySalesRep(String salesRepId) {
        return disputeRepository.findBySalesRepId(salesRepId).stream()
                .map(DisputeDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<DisputeDto> getDisputesByStatus(DisputeStatus status) {
        return disputeRepository.findByStatus(status).stream()
                .map(DisputeDto::fromEntity).toList();
    }

    public DisputeDto resolveDispute(String id, ResolveDisputeRequest request) {
        request.validate();

        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new DomainException("Dispute not found: " + id));

        if (dispute.getStatus() == DisputeStatus.RESOLVED ||
                dispute.getStatus() == DisputeStatus.APPROVED ||
                dispute.getStatus() == DisputeStatus.REJECTED) {
            throw new DomainException("Dispute is already resolved");
        }

        dispute.setResolution(request.resolution());
        dispute.setResolvedBy(request.resolvedBy());
        dispute.setStatus(request.approved() ? DisputeStatus.APPROVED : DisputeStatus.REJECTED);

        Dispute updated = disputeRepository.save(dispute);
        log.info("Dispute resolved: id={}, approved={}", updated.getId(), request.approved());
        return DisputeDto.fromEntity(updated);
    }

    public DisputeDto escalateDispute(String id) {
        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new DomainException("Dispute not found: " + id));

        if (dispute.isEscalated()) {
            throw new DomainException("Dispute is already escalated");
        }

        dispute.setEscalated(true);

        Dispute updated = disputeRepository.save(dispute);
        log.info("Dispute escalated: id={}", updated.getId());
        return DisputeDto.fromEntity(updated);
    }

    public void deleteDispute(String id) {
        if (!disputeRepository.existsById(id)) {
            throw new DomainException("Dispute not found: " + id);
        }
        disputeRepository.deleteById(id);
    }
}
