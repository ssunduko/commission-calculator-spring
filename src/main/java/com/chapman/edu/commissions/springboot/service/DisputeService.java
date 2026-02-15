package com.chapman.edu.commissions.springboot.service;

import com.chapman.edu.commissions.model.Dispute;
import com.chapman.edu.commissions.model.DisputeStatus;
import com.chapman.edu.commissions.springboot.dto.request.CreateDisputeRequest;
import com.chapman.edu.commissions.springboot.exception.BusinessValidationException;
import com.chapman.edu.commissions.springboot.exception.ResourceNotFoundException;
import com.chapman.edu.commissions.springboot.repository.DisputeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for Dispute management business logic.
 */
@Service
public class DisputeService {

    private static final Logger logger = LoggerFactory.getLogger(DisputeService.class);

    private final DisputeRepository disputeRepository;

    public DisputeService(DisputeRepository disputeRepository) {
        this.disputeRepository = disputeRepository;
    }

    public Dispute createDispute(CreateDisputeRequest request) {
        Dispute dispute = new Dispute(
            request.getCalculationId(),
            request.getSalesRepId(),
            request.getTitle(),
            request.getDescription()
        );

        Dispute saved = disputeRepository.save(dispute);
        logger.info("Created dispute: {} (ID: {})", saved.getTitle(), saved.getId());
        return saved;
    }

    public Dispute getDisputeById(String id) {
        return disputeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute", "id", id));
    }

    public List<Dispute> getAllDisputes() {
        return disputeRepository.findAll();
    }

    public List<Dispute> getDisputesBySalesRep(String salesRepId) {
        return disputeRepository.findBySalesRepId(salesRepId);
    }

    public List<Dispute> getDisputesByStatus(DisputeStatus status) {
        return disputeRepository.findByStatus(status);
    }

    /**
     * Resolve a dispute with a resolution message.
     */
    public Dispute resolveDispute(String id, String resolution, String resolvedBy) {
        Dispute dispute = getDisputeById(id);

        if (dispute.getStatus() == DisputeStatus.RESOLVED ||
            dispute.getStatus() == DisputeStatus.CANCELLED) {
            throw new BusinessValidationException(
                "Dispute is already " + dispute.getStatus() + " and cannot be resolved again");
        }

        dispute.setStatus(DisputeStatus.RESOLVED);
        dispute.setResolution(resolution);
        dispute.setResolvedBy(resolvedBy);
        dispute.addSystemComment("Dispute resolved by " + resolvedBy + ": " + resolution);

        logger.info("Resolved dispute: {}", id);
        return disputeRepository.save(dispute);
    }

    /**
     * Escalate a dispute.
     */
    public Dispute escalateDispute(String id) {
        Dispute dispute = getDisputeById(id);

        if (dispute.isEscalated()) {
            throw new BusinessValidationException("Dispute is already escalated");
        }

        dispute.setEscalated(true);
        dispute.addSystemComment("Dispute has been escalated for management review.");

        logger.info("Escalated dispute: {}", id);
        return disputeRepository.save(dispute);
    }

    public long getDisputeCount() {
        return disputeRepository.count();
    }
}
