package com.chapman.edu.commissions.architecture.eventdriven.processor;

import com.chapman.edu.commissions.architecture.eventdriven.domain.event.EventStore;
import com.chapman.edu.commissions.architecture.eventdriven.features.deals.CreateDealRequest;
import com.chapman.edu.commissions.architecture.eventdriven.features.deals.DealService;
import com.chapman.edu.commissions.architecture.eventdriven.features.calculations.CommissionCalculationService;
import com.chapman.edu.commissions.architecture.eventdriven.features.plans.CommissionPlanService;
import com.chapman.edu.commissions.architecture.eventdriven.infrastructure.events.EventStoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ============================================================
 * PROCESSOR: Event-Driven Architecture Demonstration
 * ============================================================
 *
 * CONCEPT: Event-Driven Architecture (EDA)
 * ------------------------------------------------------------
 * Components communicate via immutable DOMAIN EVENTS. Services
 * publish events after state changes; listeners react independently.
 *
 * EVENT FLOW:
 *
 *   DealService.createDeal()
 *     → dealRepository.save(deal)
 *     → eventPublisher.publishEvent(DealCreatedEvent)
 *       ├─→ EventStoreListener (@Order(1), sync) → persists to event_store
 *       ├─→ DealEventListener (@Order(10), @Async) → logs event
 *       └─→ Other listeners (analytics, notifications, etc.)
 *
 * KEY CONCEPTS:
 * 1. DOMAIN EVENTS — Immutable records of what happened
 * 2. EVENT STORE — Append-only log for audit trail and replay
 * 3. ASYNC LISTENERS — Non-blocking event processing
 * 4. DECOUPLING — Publisher doesn't know who listens
 * 5. EVENT SOURCING — Can reconstruct state from event history
 *
 * COMPARISON TO SYNCHRONOUS APPROACH:
 *   Synchronous: DealService calls NotificationService directly (coupled)
 *   Event-Driven: DealService publishes event; NotificationListener reacts (decoupled)
 */
@Service("eventDrivenProcessor")
public class EventDrivenProcessor {

    private static final Logger log = LoggerFactory.getLogger(EventDrivenProcessor.class);

    private final DealService dealService;
    private final CommissionCalculationService calcService;
    private final CommissionPlanService planService;
    private final EventStoreRepository eventStoreRepository;

    public EventDrivenProcessor(DealService dealService,
                                 CommissionCalculationService calcService,
                                 CommissionPlanService planService,
                                 EventStoreRepository eventStoreRepository) {
        this.dealService = dealService;
        this.calcService = calcService;
        this.planService = planService;
        this.eventStoreRepository = eventStoreRepository;
    }

    // ============================================================
    // DEMO 1: Event Publishing — Create a Deal and Observe Events
    // ============================================================

    /**
     * Demonstrates how domain events are published automatically
     * when state changes occur.
     *
     * When DealService.createDeal() is called:
     * 1. Deal is saved to the database
     * 2. DealCreatedEvent is published via ApplicationEventPublisher
     * 3. EventStoreListener persists the event (synchronous, @Order(1))
     * 4. DealEventListener logs the event (async, @Order(10))
     *
     * The service does NOT call listeners directly — it just publishes.
     * This is the core of event-driven decoupling.
     */
    public Map<String, Object> demonstrateEventPublishing() {
        log.info("[Event-Driven] Demonstrating Event Publishing");

        Map<String, Object> results = new LinkedHashMap<>();

        // Count events before
        long eventsBefore = eventStoreRepository.count();

        // Create a deal — this triggers DealCreatedEvent
        var deal = dealService.createDeal(
                new CreateDealRequest("Event Demo Deal", new BigDecimal("85000"), "usr-001"));

        // Count events after
        long eventsAfter = eventStoreRepository.count();

        results.put("concept", "Event Publishing — state change triggers domain event automatically");
        results.put("deal_created", deal.title());
        results.put("deal_id", deal.id());
        results.put("events_before", eventsBefore);
        results.put("events_after", eventsAfter);
        results.put("new_events_published", eventsAfter - eventsBefore);
        results.put("event_type_published", "DealCreatedEvent");
        results.put("decoupling", "DealService publishes event; it does NOT know who listens");

        log.info("[Event-Driven] Created deal '{}' — {} new event(s) in store",
                deal.title(), eventsAfter - eventsBefore);

        return results;
    }

    // ============================================================
    // DEMO 2: Event Store — Append-Only Audit Trail
    // ============================================================

    /**
     * Demonstrates the Event Store — an append-only log of all
     * domain events that have occurred in the system.
     *
     * WHY AN EVENT STORE?
     * - AUDIT TRAIL: Complete history of every change
     * - EVENT REPLAY: Can reconstruct state at any point in time
     * - DEBUGGING: See exactly what happened and when
     * - COMPLIANCE: Immutable record for regulatory requirements
     *
     * EVENT STORE SCHEMA:
     *   eventId | eventType | aggregateId | aggregateType | payload (JSON) | occurredAt
     */
    public Map<String, Object> demonstrateEventStore() {
        log.info("[Event-Driven] Demonstrating Event Store");

        Map<String, Object> results = new LinkedHashMap<>();

        List<EventStore> allEvents = eventStoreRepository.findAllByOrderByOccurredAtDesc();

        results.put("concept", "Event Store — append-only audit log of all domain events");
        results.put("total_events_stored", allEvents.size());

        // Group events by type
        Map<String, Long> eventsByType = allEvents.stream()
                .collect(Collectors.groupingBy(EventStore::getEventType, Collectors.counting()));
        results.put("events_by_type", eventsByType);

        // Group events by aggregate type
        Map<String, Long> eventsByAggregate = allEvents.stream()
                .collect(Collectors.groupingBy(EventStore::getAggregateType, Collectors.counting()));
        results.put("events_by_aggregate", eventsByAggregate);

        // Show latest events
        List<Map<String, String>> recentEvents = allEvents.stream()
                .limit(5)
                .map(e -> Map.of(
                        "type", e.getEventType(),
                        "aggregate", e.getAggregateType() + ":" + e.getAggregateId(),
                        "when", e.getOccurredAt().toString()))
                .toList();
        results.put("recent_events", recentEvents);

        results.put("event_store_properties", Map.of(
                "append_only", "Events are never updated or deleted",
                "synchronous_write", "EventStoreListener runs at @Order(1) before async listeners",
                "json_payload", "Full event data serialized as JSON for replay"));

        return results;
    }

    // ============================================================
    // DEMO 3: Event Listeners — Sync vs Async
    // ============================================================

    /**
     * Demonstrates the two types of event listeners in the system.
     *
     * SYNCHRONOUS (@Order(1)):
     *   EventStoreListener — persists events to database
     *   Runs BEFORE async listeners to guarantee durability
     *   If this fails, the event is lost
     *
     * ASYNCHRONOUS (@Async @Order(10)):
     *   DealEventListener, CommissionCalculationEventListener, etc.
     *   Run in separate threads, don't block the main request
     *   Can fail without affecting the original transaction
     *
     * ORDERING:
     *   @Order(1)  → EventStoreListener (persist first — critical)
     *   @Order(10) → Feature listeners (process second — non-critical)
     */
    public Map<String, Object> demonstrateEventListeners() {
        log.info("[Event-Driven] Demonstrating Event Listeners");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "Event Listeners — synchronous durability + async processing");

        results.put("synchronous_listeners", Map.of(
                "EventStoreListener", Map.of(
                        "order", 1,
                        "purpose", "Persist ALL events to event_store table",
                        "why_sync", "Must complete before request returns — guarantees durability")));

        results.put("async_listeners", Map.of(
                "DealEventListener", Map.of(
                        "order", 10,
                        "events", "DealCreatedEvent, DealUpdatedEvent, DealDeletedEvent",
                        "why_async", "Logging is non-critical — don't block the response"),
                "CommissionCalculationEventListener", Map.of(
                        "order", 10,
                        "events", "CommissionCalculatedEvent",
                        "why_async", "Processing is non-critical")));

        results.put("execution_order", List.of(
                "1. Service publishes event",
                "2. EventStoreListener persists (sync, @Order(1))",
                "3. Feature listeners react (async, @Order(10))",
                "4. Response returned to client"));

        results.put("async_config", "@EnableAsync on AsyncConfig enables @Async listeners");

        return results;
    }

    // ============================================================
    // DEMO 4: Event Replay — Reconstructing History
    // ============================================================

    /**
     * Demonstrates event replay: querying the event store to
     * reconstruct the history of a specific aggregate.
     *
     * This is the foundation of Event Sourcing — instead of querying
     * current state, you replay events to build state at any point.
     */
    public Map<String, Object> demonstrateEventReplay() {
        log.info("[Event-Driven] Demonstrating Event Replay");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "Event Replay — reconstruct aggregate history from events");

        // Find an aggregate with events
        List<EventStore> allEvents = eventStoreRepository.findAllByOrderByOccurredAtDesc();
        if (allEvents.isEmpty()) {
            results.put("status", "SKIPPED");
            results.put("reason", "No events in store yet");
            return results;
        }

        String aggregateId = allEvents.get(0).getAggregateId();
        List<EventStore> aggregateHistory = eventStoreRepository
                .findByAggregateIdOrderByOccurredAtAsc(aggregateId);

        results.put("aggregate_id", aggregateId);
        results.put("total_events_for_aggregate", aggregateHistory.size());

        List<Map<String, String>> timeline = aggregateHistory.stream()
                .map(e -> Map.of(
                        "event", e.getEventType(),
                        "when", e.getOccurredAt().toString()))
                .toList();
        results.put("event_timeline", timeline);

        results.put("replay_capability", Map.of(
                "full_history", "All events for an aggregate, ordered by time",
                "point_in_time", "Filter by time range for historical state",
                "by_event_type", "Filter by event type (e.g., all DealCreatedEvents)"));

        return results;
    }

    // ============================================================
    // DEMO 5: Decoupling via Events
    // ============================================================

    /**
     * Demonstrates the decoupling benefits of event-driven architecture.
     *
     * COMPARISON:
     *
     * COUPLED (synchronous calls):
     *   DealService.createDeal() {
     *       dealRepo.save(deal);
     *       auditService.log(deal);          ← DealService KNOWS about AuditService
     *       notificationService.notify(deal); ← DealService KNOWS about NotificationService
     *       analyticsService.track(deal);     ← DealService KNOWS about AnalyticsService
     *   }
     *
     * DECOUPLED (event-driven):
     *   DealService.createDeal() {
     *       dealRepo.save(deal);
     *       eventPublisher.publishEvent(new DealCreatedEvent(...));
     *       // DealService is DONE — doesn't know or care who listens
     *   }
     */
    public Map<String, Object> demonstrateDecoupling() {
        log.info("[Event-Driven] Demonstrating Decoupling via Events");

        Map<String, Object> results = new LinkedHashMap<>();

        results.put("concept", "Decoupling — publisher doesn't know who listens or what they do");

        results.put("coupled_approach", Map.of(
                "problem", "Service directly calls every downstream dependency",
                "adding_new_feature", "Must modify DealService to add notification call",
                "testing", "Must mock every dependency"));

        results.put("event_driven_approach", Map.of(
                "benefit", "Service just publishes an event and moves on",
                "adding_new_feature", "Add a new @EventListener — DealService unchanged",
                "testing", "Test event publishing separately from handling"));

        // Show how many different event types exist
        List<EventStore> allEvents = eventStoreRepository.findAllByOrderByOccurredAtDesc();
        long distinctEventTypes = allEvents.stream()
                .map(EventStore::getEventType)
                .distinct()
                .count();

        results.put("event_types_in_system", distinctEventTypes);
        results.put("total_events_processed", allEvents.size());
        results.put("extensibility", "To add analytics: create AnalyticsEventListener. " +
                "Zero changes to existing services.");

        return results;
    }
}
