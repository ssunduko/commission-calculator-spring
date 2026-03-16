package com.chapman.edu.commissions.architecture.eventdriven.infrastructure.events;

import com.chapman.edu.commissions.architecture.eventdriven.domain.event.DomainEvent;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.EventStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * CONCEPT: Event Store Listener (Event Sourcing Foundation)
 *
 * This listener captures ALL domain events and persists them to the Event Store.
 * It runs synchronously (no @Async) and with @Order(1) to ensure events are
 * stored before any other listener processes them.
 *
 * This is the foundation of Event Sourcing — every state change is recorded
 * as an immutable event, creating a complete audit trail.
 */
@Component
public class EventStoreListener {

    private static final Logger log = LoggerFactory.getLogger(EventStoreListener.class);

    private final EventStoreRepository eventStoreRepository;
    private final ObjectMapper objectMapper;

    public EventStoreListener(EventStoreRepository eventStoreRepository, ObjectMapper objectMapper) {
        this.eventStoreRepository = eventStoreRepository;
        this.objectMapper = objectMapper;
    }

    @EventListener
    @Order(1)
    public void onDomainEvent(DomainEvent event) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", event.getEventType(), e);
            payload = "{ \"error\": \"serialization failed\" }";
        }

        EventStore entry = new EventStore(
                event.getEventId(),
                event.getEventType(),
                event.getAggregateId(),
                event.getAggregateType(),
                payload,
                event.getOccurredAt()
        );

        eventStoreRepository.save(entry);
        log.info("Event stored: {} [aggregateId={}, type={}]",
                event.getEventType(), event.getAggregateId(), event.getAggregateType());
    }
}
