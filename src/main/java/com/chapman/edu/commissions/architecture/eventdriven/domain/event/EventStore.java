package com.chapman.edu.commissions.architecture.eventdriven.domain.event;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * CONCEPT: Event Store
 *
 * An Event Store is a persistence mechanism that records all domain events
 * as an append-only log. This provides:
 *
 * 1. AUDIT TRAIL — Every state change is recorded with who/what/when
 * 2. EVENT REPLAY — The system can reconstruct state by replaying events
 * 3. TEMPORAL QUERIES — "What was the state at time T?"
 * 4. DEBUGGING — Complete history of what happened and in what order
 *
 * Unlike traditional CRUD where we overwrite state, the Event Store
 * captures the full history of changes as immutable facts.
 */
@Entity
@Table(name = "event_store")
public class EventStore {

    @Id
    @Column(name = "event_id")
    private String eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected EventStore() {}

    public EventStore(String eventId, String eventType, String aggregateId,
                      String aggregateType, String payload, Instant occurredAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.payload = payload;
        this.occurredAt = occurredAt;
    }

    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getAggregateId() { return aggregateId; }
    public String getAggregateType() { return aggregateType; }
    public String getPayload() { return payload; }
    public Instant getOccurredAt() { return occurredAt; }
}
