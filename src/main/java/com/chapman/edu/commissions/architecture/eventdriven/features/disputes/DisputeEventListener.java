package com.chapman.edu.commissions.architecture.eventdriven.features.disputes;

import com.chapman.edu.commissions.architecture.eventdriven.domain.event.DisputeCreatedEvent;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.DisputeEscalatedEvent;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.DisputeResolvedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * CONCEPT: Cross-Cutting Concern via Event Listener
 *
 * Dispute events are particularly important for business operations:
 * - Created: Manager needs to be notified
 * - Escalated: Higher management needs to be notified
 * - Resolved: Sales rep needs to be notified of the outcome
 *
 * By handling these as events, we decouple notification logic
 * from dispute business logic.
 */
@Component
public class DisputeEventListener {

    private static final Logger log = LoggerFactory.getLogger(DisputeEventListener.class);

    @Async
    @EventListener
    @Order(10)
    public void onDisputeCreated(DisputeCreatedEvent event) {
        log.info("[EVENT] Dispute created: '{}' for calculationId={}, salesRep={} [eventId={}]",
                event.getTitle(), event.getCalculationId(), event.getSalesRepId(), event.getEventId());
    }

    @Async
    @EventListener
    @Order(10)
    public void onDisputeEscalated(DisputeEscalatedEvent event) {
        log.info("[EVENT] Dispute ESCALATED: disputeId={}, calculationId={}, salesRep={} [eventId={}]",
                event.getDisputeId(), event.getCalculationId(), event.getSalesRepId(), event.getEventId());
    }

    @Async
    @EventListener
    @Order(10)
    public void onDisputeResolved(DisputeResolvedEvent event) {
        log.info("[EVENT] Dispute resolved: disputeId={}, approved={}, resolution='{}' [eventId={}]",
                event.getDisputeId(), event.isApproved(), event.getResolution(), event.getEventId());
    }
}
