package com.chapman.edu.commissions.architecture.eventdriven.infrastructure.events;

import com.chapman.edu.commissions.architecture.eventdriven.domain.event.EventStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONCEPT: Event Store Query API
 *
 * Exposes the event store as a read-only REST API, allowing clients to:
 * - View the complete event log (audit trail)
 * - Filter events by aggregate (e.g., all events for a specific deal)
 * - Filter events by type (e.g., all DealCreatedEvents)
 *
 * This is a key benefit of event-driven architecture: the event log
 * serves as both an audit trail and a debugging tool.
 */
@RestController
@RequestMapping("/api/events/event-store")
public class EventStoreController {

    private final EventStoreRepository eventStoreRepository;

    public EventStoreController(EventStoreRepository eventStoreRepository) {
        this.eventStoreRepository = eventStoreRepository;
    }

    @GetMapping
    public ResponseEntity<List<EventStore>> getAllEvents() {
        return ResponseEntity.ok(eventStoreRepository.findAllByOrderByOccurredAtDesc());
    }

    @GetMapping("/aggregate/{aggregateId}")
    public ResponseEntity<List<EventStore>> getEventsByAggregate(@PathVariable String aggregateId) {
        return ResponseEntity.ok(eventStoreRepository.findByAggregateIdOrderByOccurredAtAsc(aggregateId));
    }

    @GetMapping("/type/{aggregateType}")
    public ResponseEntity<List<EventStore>> getEventsByAggregateType(@PathVariable String aggregateType) {
        return ResponseEntity.ok(eventStoreRepository.findByAggregateTypeOrderByOccurredAtDesc(aggregateType));
    }

    @GetMapping("/event-type/{eventType}")
    public ResponseEntity<List<EventStore>> getEventsByEventType(@PathVariable String eventType) {
        return ResponseEntity.ok(eventStoreRepository.findByEventType(eventType));
    }
}
