package com.chapman.edu.commissions.architecture.eventdriven.features.disputes;

import com.chapman.edu.commissions.architecture.eventdriven.domain.Dispute;
import com.chapman.edu.commissions.architecture.eventdriven.domain.DisputeStatus;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.DisputeCreatedEvent;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.DisputeEscalatedEvent;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.DisputeResolvedEvent;
import com.chapman.edu.commissions.architecture.eventdriven.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.eventdriven.infrastructure.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CONCEPT: Event-Publishing Dispute Service
 *
 * Disputes follow a state-machine lifecycle: OPEN -> ESCALATED -> RESOLVED.
 * Each transition publishes a domain event, enabling loosely coupled reactions:
 *
 * - {@link DisputeCreatedEvent} — a new dispute is filed; listeners may notify
 *   the manager or pause payout processing for the disputed calculation.
 * - {@link DisputeResolvedEvent} — the dispute is approved or rejected; if
 *   approved, listeners may trigger commission recalculation.
 * - {@link DisputeEscalatedEvent} — the dispute is escalated to management;
 *   listeners may send urgent notifications or update SLA tracking.
 *
 * The service enforces business rules (e.g., cannot resolve an already-resolved
 * dispute) while remaining agnostic to what happens after the event is published.
 */
@Service
public class DisputeService {

    private static final Logger log = LoggerFactory.getLogger(DisputeService.class);

    private final DisputeRepository disputeRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DisputeService(DisputeRepository disputeRepository,
                          ApplicationEventPublisher eventPublisher) {
        this.disputeRepository = disputeRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new dispute and publishes a {@link DisputeCreatedEvent}.
     *
     * CONCEPT: Filing a dispute is a significant business event. Listeners
     * can react by notifying the responsible manager, pausing the payout
     * for the disputed commission, or opening a case in a ticketing system.
     */
    public DisputeResponse createDispute(CreateDisputeRequest request) {
        request.validate();

        Dispute dispute = new Dispute(
                request.calculationId(),
                request.salesRepId(),
                request.title(),
                request.description()
        );

        Dispute savedDispute = disputeRepository.save(dispute);

        log.info("Dispute created: id={}, calculationId={}", savedDispute.getId(), savedDispute.getCalculationId());

        eventPublisher.publishEvent(new DisputeCreatedEvent(
                savedDispute.getId(),
                savedDispute.getCalculationId(),
                savedDispute.getSalesRepId(),
                savedDispute.getTitle()
        ));

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

    /**
     * Resolves a dispute (approved or rejected) and publishes a {@link DisputeResolvedEvent}.
     *
     * CONCEPT: Resolution is the most consequential dispute event. If the dispute
     * is approved, a listener may trigger commission recalculation with adjusted
     * amounts. If rejected, the listener may notify the sales rep with the
     * rejection reason. The service publishes the event with the approval flag
     * and resolution text so listeners can branch on the outcome.
     */
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

        log.info("Dispute resolved: id={}, approved={}", id, request.approved());

        eventPublisher.publishEvent(new DisputeResolvedEvent(
                updatedDispute.getId(),
                updatedDispute.getCalculationId(),
                updatedDispute.getSalesRepId(),
                request.approved(),
                request.resolution()
        ));

        return DisputeResponse.from(updatedDispute);
    }

    /**
     * Escalates a dispute to management and publishes a {@link DisputeEscalatedEvent}.
     *
     * CONCEPT: Escalation signals urgency. Listeners may send high-priority
     * notifications, update SLA timers, or reassign the dispute to a senior
     * reviewer — all without this service needing to know those details.
     */
    public DisputeResponse escalateDispute(String id) {
        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute", id));

        if (dispute.isEscalated()) {
            throw new ValidationException("Dispute is already escalated");
        }

        dispute.setEscalated(true);

        Dispute updatedDispute = disputeRepository.save(dispute);

        log.info("Dispute escalated: id={}", id);

        eventPublisher.publishEvent(new DisputeEscalatedEvent(
                updatedDispute.getId(),
                updatedDispute.getCalculationId(),
                updatedDispute.getSalesRepId()
        ));

        return DisputeResponse.from(updatedDispute);
    }

    public void deleteDispute(String id) {
        if (!disputeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dispute", id);
        }
        disputeRepository.deleteById(id);
    }
}
