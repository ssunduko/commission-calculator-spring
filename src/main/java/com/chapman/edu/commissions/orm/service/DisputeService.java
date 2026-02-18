package com.chapman.edu.commissions.orm.service;

import com.chapman.edu.commissions.orm.entity.*;
import com.chapman.edu.commissions.orm.repository.CommissionCalculationRepository;
import com.chapman.edu.commissions.orm.repository.DisputeRepository;
import com.chapman.edu.commissions.orm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * SERVICE LAYER: DisputeService
 * ============================================================
 *
 * TRANSACTION PROPAGATION IN PRACTICE:
 * This service demonstrates REQUIRES_NEW propagation for audit logging.
 * When a dispute resolution fails, the audit log should still be recorded.
 *
 * ROLLBACK RULES:
 * By default, @Transactional rolls back on unchecked exceptions (RuntimeException)
 * and does NOT roll back on checked exceptions.
 *
 * You can customize this behavior:
 * - rollbackFor = {CheckedException.class}: Also roll back for checked exceptions
 * - noRollbackFor = {BusinessException.class}: Don't roll back for specific exceptions
 */
@Service
@Transactional(readOnly = true)
public class DisputeService {

    private static final Logger log = LoggerFactory.getLogger(DisputeService.class);

    private final DisputeRepository disputeRepository;
    private final CommissionCalculationRepository calculationRepository;
    private final UserRepository userRepository;

    public DisputeService(DisputeRepository disputeRepository,
                          CommissionCalculationRepository calculationRepository,
                          UserRepository userRepository) {
        this.disputeRepository = disputeRepository;
        this.calculationRepository = calculationRepository;
        this.userRepository = userRepository;
    }

    public Optional<Dispute> findById(String id) {
        return disputeRepository.findById(id);
    }

    public Optional<Dispute> findByIdWithComments(String id) {
        return disputeRepository.findByIdWithComments(id);
    }

    public Page<Dispute> findByStatus(DisputeStatus status, Pageable pageable) {
        return disputeRepository.findByStatus(status, pageable);
    }

    public List<Dispute> findBySalesRep(String salesRepId) {
        return disputeRepository.findBySalesRepId(salesRepId);
    }

    public List<Dispute> findOpenDisputesForManager(String managerId) {
        return disputeRepository.findOpenDisputesByManagerId(managerId);
    }

    /**
     * File a new dispute.
     *
     * TRANSACTION: The dispute creation, calculation status update,
     * and system comment are all in one transaction. If any fails,
     * everything rolls back.
     */
    @Transactional(readOnly = false)
    public Dispute fileDispute(String calculationId, String salesRepId, String title, String description) {
        log.info("Filing dispute for calculation: {} by: {}", calculationId, salesRepId);

        CommissionCalculation calculation = calculationRepository.findById(calculationId)
                .orElseThrow(() -> new IllegalArgumentException("Calculation not found: " + calculationId));

        User salesRep = userRepository.findById(salesRepId)
                .orElseThrow(() -> new IllegalArgumentException("Sales rep not found: " + salesRepId));

        // Update calculation status to DISPUTED
        calculation.setStatus(CommissionStatus.DISPUTED);
        calculationRepository.save(calculation);

        // Create the dispute
        Dispute dispute = new Dispute(calculation, salesRep, title, description);

        // Add a system comment
        DisputeComment systemComment = new DisputeComment(dispute, "Dispute filed. Awaiting manager review.", true);
        dispute.addComment(systemComment);

        return disputeRepository.save(dispute);
    }

    /**
     * Resolve a dispute.
     *
     * ROLLBACK RULES:
     * rollbackFor = Exception.class: Roll back on ANY exception (checked or unchecked).
     * This is appropriate for critical business operations where partial
     * completion would be worse than a full rollback.
     */
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public Dispute resolveDispute(String disputeId, String resolution, String resolvedBy,
                                   boolean approved) {
        log.info("Resolving dispute: {} approved={}", disputeId, approved);

        Dispute dispute = disputeRepository.findByIdWithComments(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found: " + disputeId));

        dispute.setResolution(resolution);
        dispute.setResolvedBy(resolvedBy);
        dispute.setStatus(approved ? DisputeStatus.APPROVED : DisputeStatus.REJECTED);

        // Add resolution comment
        String commentText = approved
                ? "Dispute APPROVED. Resolution: " + resolution
                : "Dispute REJECTED. Reason: " + resolution;
        DisputeComment comment = new DisputeComment(dispute, commentText, true);
        dispute.addComment(comment);

        // If approved, update the commission calculation status back to CALCULATED
        // so it can be recalculated
        if (approved) {
            CommissionCalculation calc = dispute.getCalculation();
            calc.setStatus(CommissionStatus.ADJUSTED);
            calculationRepository.save(calc);
        }

        return disputeRepository.save(dispute);
    }

    /**
     * Add a comment to a dispute.
     *
     * PROPAGATION.REQUIRES_NEW: This runs in its own transaction.
     * Even if the calling method's transaction fails, the comment is saved.
     * This is useful for audit trail entries that must be persisted
     * regardless of the outer transaction's outcome.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Dispute addComment(String disputeId, String userId, String userName, String text) {
        log.info("Adding comment to dispute: {} by: {}", disputeId, userName);

        Dispute dispute = disputeRepository.findByIdWithComments(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found: " + disputeId));

        DisputeComment comment = new DisputeComment(dispute, userId, userName, text);
        dispute.addComment(comment);

        return disputeRepository.save(dispute);
    }

    public List<Object[]> getDisputeStatusCounts() {
        return disputeRepository.countByStatus();
    }
}
