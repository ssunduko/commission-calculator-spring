package com.chapman.edu.commissions.architecture.eventdriven.infrastructure.events;

import com.chapman.edu.commissions.architecture.eventdriven.domain.event.EventStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for querying the Event Store.
 * Provides methods to retrieve events by aggregate, type, or time range.
 */
@Repository
public interface EventStoreRepository extends JpaRepository<EventStore, String> {

    List<EventStore> findByAggregateIdOrderByOccurredAtAsc(String aggregateId);

    List<EventStore> findByAggregateTypeOrderByOccurredAtDesc(String aggregateType);

    List<EventStore> findByEventType(String eventType);

    List<EventStore> findByOccurredAtBetweenOrderByOccurredAtAsc(Instant start, Instant end);

    List<EventStore> findAllByOrderByOccurredAtDesc();
}
