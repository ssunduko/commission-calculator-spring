package com.chapman.edu.commissions.architecture.eventdriven.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * CONCEPT: Domain Event Base Class
 *
 * In Event-Driven Architecture, a Domain Event represents something
 * meaningful that happened in the domain. Every event captures:
 * - WHAT happened (event type/class name)
 * - WHEN it happened (timestamp)
 * - A unique identity (eventId) for idempotency and tracing
 *
 * Events are immutable facts — once something happened, it cannot un-happen.
 * This base class provides the common structure all events share.
 */
public abstract class DomainEvent {

    private final String eventId;
    private final Instant occurredAt;
    private final String eventType;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = Instant.now();
        this.eventType = this.getClass().getSimpleName();
    }

    public String getEventId() { return eventId; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getEventType() { return eventType; }

    /**
     * Returns the aggregate ID this event relates to.
     * Used for event store partitioning and lookup.
     */
    public abstract String getAggregateId();

    /**
     * Returns the aggregate type (e.g., "Deal", "CommissionPlan").
     * Used for filtering and routing events.
     */
    public abstract String getAggregateType();
}
