package com.chapman.edu.commissions.architecture.eventdriven.features.deals;

import com.chapman.edu.commissions.architecture.eventdriven.domain.event.DealCreatedEvent;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.DealDeletedEvent;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.DealUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * CONCEPT: Event Listener (Reactive Handler)
 *
 * Event Listeners react to domain events without the publisher knowing.
 * This class handles deal-related events for cross-cutting concerns:
 * - Logging significant business actions
 * - Triggering notifications (simulated via log)
 * - Updating derived data
 *
 * @Async makes these handlers non-blocking — the deal service returns
 * immediately while listeners process events in background threads.
 *
 * @Order(10) ensures these run AFTER the EventStoreListener (@Order(1))
 * so events are persisted before processing.
 */
@Component
public class DealEventListener {

    private static final Logger log = LoggerFactory.getLogger(DealEventListener.class);

    @Async
    @EventListener
    @Order(10)
    public void onDealCreated(DealCreatedEvent event) {
        log.info("[EVENT] Deal created: '{}' (value={}, salesRep={}) [eventId={}]",
                event.getTitle(), event.getValue(), event.getSalesRepId(), event.getEventId());
    }

    @Async
    @EventListener
    @Order(10)
    public void onDealUpdated(DealUpdatedEvent event) {
        log.info("[EVENT] Deal updated: {} changed from '{}' to '{}' [dealId={}, eventId={}]",
                event.getUpdatedField(), event.getOldValue(), event.getNewValue(),
                event.getDealId(), event.getEventId());
    }

    @Async
    @EventListener
    @Order(10)
    public void onDealDeleted(DealDeletedEvent event) {
        log.info("[EVENT] Deal deleted: dealId={}, salesRep={} [eventId={}]",
                event.getDealId(), event.getSalesRepId(), event.getEventId());
    }
}
