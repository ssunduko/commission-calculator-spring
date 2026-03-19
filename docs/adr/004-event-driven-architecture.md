# ADR-004: Event-Driven Architecture

## Status
Accepted

## Date
2026-03-19

## Context
Commission processing requires an audit trail — regulatory and business stakeholders need to know when deals were created, when plans were activated, when calculations were performed, and when disputes were filed. Additionally, downstream processes (notifications, analytics, reporting) need to react to domain changes without tight coupling to the originating service.

A traditional request-response model would require the originating service to know about all consumers, creating coupling and making it difficult to add new reactions to events.

## Decision
Adopt an Event-Driven Architecture with the following components:

- **Domain Events:** Immutable records representing state changes (DealCreatedEvent, PlanActivatedEvent, CommissionCalculatedEvent, DisputeInitiatedEvent, etc.)
- **Event Store:** Append-only persistence of all domain events, providing a complete audit log
- **Event Publisher:** Services emit events after successful mutations
- **Event Listeners:** React to events independently using Spring's `@EventListener` with `@Async`

Event processing order:
1. `EventStoreListener` (synchronous, `@Order(1)`) — persists event to the store before any async processing
2. Feature listeners (asynchronous, `@Order(10)`) — handle downstream reactions (notifications, calculations, analytics)

Key design rules:
- Events are immutable and never modified after creation
- Publishers do not know about subscribers
- The event store is the single source of truth for what happened and when
- Async listeners operate with eventual consistency

## Consequences

### Positive
- Complete audit trail of all domain state changes with timestamps
- Loose coupling — new event consumers can be added without modifying publishers
- Temporal decoupling — async processing improves perceived responsiveness
- Event store enables event replay and temporal queries

### Negative
- Eventual consistency requires careful handling in the UI and API layers
- Debugging distributed async event flows is harder than synchronous call stacks
- Event schema evolution must be managed carefully to avoid breaking consumers
- Event store grows indefinitely and requires retention/archival policies

### Trade-offs
- Best for systems requiring audit trails, async workflows, and loose coupling between components
- The consistency trade-off is acceptable for commission processing where calculations can be eventually consistent
