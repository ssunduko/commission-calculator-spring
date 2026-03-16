package com.chapman.edu.commissions.architecture.cleanarchitecture.application.service;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CreateDisputeCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.DisputeResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.ResolveDisputeCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in.DisputeUseCase;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.DisputeRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.DomainException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.EntityNotFoundException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.Dispute;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DisputeStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Application service implementing dispute management use cases.
 */
@Service
public class DisputeService implements DisputeUseCase {

    private final DisputeRepositoryPort disputeRepository;

    public DisputeService(DisputeRepositoryPort disputeRepository) {
        this.disputeRepository = disputeRepository;
    }

    @Override
    public DisputeResult createDispute(CreateDisputeCommand command) {
        command.validate();
        Dispute dispute = new Dispute(
                command.calculationId(),
                command.salesRepId(),
                command.title(),
                command.description()
        );
        Dispute saved = disputeRepository.save(dispute);
        return DisputeResult.from(saved);
    }

    @Override
    public DisputeResult getDispute(String id) {
        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dispute", id));
        return DisputeResult.from(dispute);
    }

    @Override
    public List<DisputeResult> getAllDisputes() {
        return disputeRepository.findAll().stream()
                .map(DisputeResult::from)
                .toList();
    }

    @Override
    public List<DisputeResult> getDisputesBySalesRep(String salesRepId) {
        return disputeRepository.findBySalesRepId(salesRepId).stream()
                .map(DisputeResult::from)
                .toList();
    }

    @Override
    public List<DisputeResult> getDisputesByStatus(DisputeStatus status) {
        return disputeRepository.findByStatus(status).stream()
                .map(DisputeResult::from)
                .toList();
    }

    @Override
    public DisputeResult resolveDispute(String id, ResolveDisputeCommand command) {
        command.validate();
        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dispute", id));

        if (dispute.getStatus() == DisputeStatus.APPROVED || dispute.getStatus() == DisputeStatus.REJECTED) {
            throw new DomainException("Dispute has already been resolved");
        }

        dispute.setStatus(command.approved() ? DisputeStatus.APPROVED : DisputeStatus.REJECTED);
        dispute.setResolution(command.resolution());
        dispute.setResolvedBy(command.resolvedBy());
        dispute.setResolvedDate(LocalDateTime.now());

        Dispute saved = disputeRepository.save(dispute);
        return DisputeResult.from(saved);
    }

    @Override
    public DisputeResult escalateDispute(String id) {
        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dispute", id));

        if (dispute.isEscalated()) {
            throw new DomainException("Dispute is already escalated");
        }
        if (dispute.getStatus() == DisputeStatus.APPROVED || dispute.getStatus() == DisputeStatus.REJECTED) {
            throw new DomainException("Cannot escalate a resolved dispute");
        }

        dispute.setEscalated(true);
        dispute.setStatus(DisputeStatus.ESCALATED);

        Dispute saved = disputeRepository.save(dispute);
        return DisputeResult.from(saved);
    }

    @Override
    public void deleteDispute(String id) {
        disputeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dispute", id));
        disputeRepository.deleteById(id);
    }
}
