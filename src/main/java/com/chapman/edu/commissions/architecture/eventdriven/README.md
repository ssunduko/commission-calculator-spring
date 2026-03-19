# Commission Calculator - Event-Driven Architecture

## Overview

This module implements the commission calculator system using **Event-Driven Architecture (EDA)**. The application manages deals, commission plans, calculations, and disputes — the same domain as the vertical-slice module — but uses **domain events**, an **event store**, and **event listeners** for cross-cutting concerns instead of direct method calls.

## Architecture

### Event-Driven Architecture

In EDA, components communicate by producing and consuming **events** — immutable records of things that happened. Services publish events after state changes, and listeners react to those events independently.

```
eventdriven/
├── domain/                    # Domain models and enums
│   └── event/                # Domain events and Event Store entity
├── features/                  # Feature modules
│   ├── deals/                # Deal service + DealEventListener
│   ├── plans/                # Plan service + CommissionPlanEventListener
│   ├── calculations/         # Calculation service + CommissionCalculationEventListener
│   └── disputes/             # Dispute service + DisputeEventListener
├── processor/                 # STARTUP DEMOS — Showcases EDA concepts
│   ├── EventDrivenProcessor.java      # Demonstrates all EDA patterns
│   └── EventDrivenProcessorDemo.java  # CommandLineRunner for startup demos
└── infrastructure/
    ├── config/               # Security, OpenAPI, AsyncConfig
    ├── data/                 # Data initialization
    ├── events/               # Event Store (repository, listener, controller)
    └── exceptions/           # Global exception handling
```

### How Events Flow

```
    ┌─────────────┐     publishEvent()     ┌──────────────────┐
    │  DealService │ ───────────────────→   │ Spring Event Bus  │
    └─────────────┘                         └────────┬─────────┘
                                                     │
                           ┌─────────────────────────┼─────────────────────┐
                           │                         │                     │
                           ▼                         ▼                     ▼
                  ┌─────────────────┐    ┌───────────────────┐   ┌─────────────────┐
                  │ EventStore      │    │ DealEventListener │   │ Other Listeners  │
                  │ Listener        │    │ (async, logging)  │   │ (notifications,  │
                  │ (@Order(1),     │    │ (@Order(10),      │   │  analytics, etc) │
                  │  synchronous)   │    │  @Async)          │   │                  │
                  └────────┬────────┘    └───────────────────┘   └─────────────────┘
                           │
                           ▼
                  ┌─────────────────┐
                  │  event_store    │
                  │  table (H2)    │
                  └─────────────────┘
```

1. **Service** performs a state change (create, update, delete)
2. **Service** publishes a domain event via `ApplicationEventPublisher`
3. **EventStoreListener** (`@Order(1)`, synchronous) persists the event to the event store
4. **Feature EventListeners** (`@Order(10)`, `@Async`) handle cross-cutting concerns in background threads
5. **Event Store Controller** provides a REST API to query the audit log

### Key Concepts

#### 1. Domain Events

Immutable records of business-significant happenings:

```java
public class DealCreatedEvent extends DomainEvent {
    private final String dealId;
    private final String title;
    private final BigDecimal value;
    private final String salesRepId;
    // ...
}
```

Every event extends `DomainEvent`, which provides:
- **eventId** — UUID for idempotency and tracing
- **occurredAt** — Timestamp of when it happened
- **eventType** — Class name (e.g., "DealCreatedEvent")
- **aggregateId** — The entity this event relates to
- **aggregateType** — The entity type (e.g., "Deal")

#### 2. Event Publisher (Service Layer)

Services publish events after each state change:

```java
@Service
public class DealService {
    private final DealRepository dealRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DealResponse createDeal(CreateDealRequest request) {
        // 1. Validate and persist
        Deal saved = dealRepository.save(deal);

        // 2. Publish event — listeners react independently
        eventPublisher.publishEvent(new DealCreatedEvent(
            saved.getId(), saved.getTitle(),
            saved.getValue(), saved.getSalesRepId()
        ));

        return DealResponse.from(saved);
    }
}
```

#### 3. Event Listeners

Listeners react to events without the publisher knowing they exist:

```java
@Component
public class DealEventListener {

    @Async                    // Non-blocking — runs in background thread
    @EventListener            // Subscribes to Spring events
    @Order(10)                // Runs AFTER EventStoreListener (@Order(1))
    public void onDealCreated(DealCreatedEvent event) {
        log.info("[EVENT] Deal created: '{}' (value={})", event.getTitle(), event.getValue());
        // Could also: send notification, update dashboard, trigger workflow...
    }
}
```

#### 4. Event Store

An append-only log that persists every domain event:

```java
@Entity
@Table(name = "event_store")
public class EventStore {
    private String eventId;       // UUID
    private String eventType;     // "DealCreatedEvent"
    private String aggregateId;   // "deal-001"
    private String aggregateType; // "Deal"
    private String payload;       // Full event JSON
    private Instant occurredAt;   // When it happened
}
```

The `EventStoreListener` captures all events synchronously:

```java
@Component
public class EventStoreListener {

    @EventListener
    @Order(1)  // Runs FIRST — ensures persistence before other listeners
    public void onDomainEvent(DomainEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        eventStoreRepository.save(new EventStore(
            event.getEventId(), event.getEventType(),
            event.getAggregateId(), event.getAggregateType(),
            payload, event.getOccurredAt()
        ));
    }
}
```

#### 5. Async Processing

`@EnableAsync` activates Spring's async support. Event listeners annotated with `@Async` run in separate threads:

- **Benefit**: The HTTP response returns immediately; listeners process in the background
- **Trade-off**: Eventual consistency — the event store is written synchronously, but notifications and logging may lag slightly

### Benefits of Event-Driven Architecture

1. **Decoupling**: Publishers don't know about listeners — add new behaviors without modifying services
2. **Audit Trail**: The event store records every state change as an immutable fact
3. **Extensibility**: New listeners can be added for notifications, analytics, or integrations
4. **Debugging**: Complete event history shows exactly what happened and when
5. **Async Processing**: Background listeners keep the main request fast

## Domain Events

### Event Catalog

| Event | Published When | Key Data |
|-------|---------------|----------|
| `DealCreatedEvent` | Deal is created | dealId, title, value, salesRepId |
| `DealUpdatedEvent` | Deal is modified | dealId, updatedField, oldValue, newValue |
| `DealDeletedEvent` | Deal is deleted | dealId, salesRepId |
| `CommissionPlanCreatedEvent` | Plan is created | planId, planName, currency |
| `CommissionPlanActivatedEvent` | Plan becomes ACTIVE | planId, planName |
| `RuleAddedToPlanEvent` | Rule added to plan | planId, ruleName, rate, ruleType |
| `CommissionCalculatedEvent` | Commission calculated | calculationId, dealId, salesRepId, baseCommission, netCommission |
| `DisputeCreatedEvent` | Dispute filed | disputeId, calculationId, salesRepId, title |
| `DisputeResolvedEvent` | Dispute resolved | disputeId, approved, resolution |
| `DisputeEscalatedEvent` | Dispute escalated | disputeId, calculationId, salesRepId |

### Event Hierarchy

```
DomainEvent (abstract)
├── DealCreatedEvent
├── DealUpdatedEvent
├── DealDeletedEvent
├── CommissionPlanCreatedEvent
├── CommissionPlanActivatedEvent
├── RuleAddedToPlanEvent
├── CommissionCalculatedEvent
├── DisputeCreatedEvent
├── DisputeResolvedEvent
└── DisputeEscalatedEvent
```

## Features

### 1. Deal Management (`features/deals/`)

| Component | Role |
|-----------|------|
| `DealController` | REST endpoints at `/api/events/deals` |
| `DealService` | CRUD + publishes `DealCreated`, `DealUpdated`, `DealDeleted` events |
| `DealRepository` | JPA data access |
| `DealEventListener` | Async listener for deal events (logging, notifications) |
| `CreateDealRequest` / `UpdateDealRequest` / `DealResponse` | DTOs |

### 2. Commission Plan Management (`features/plans/`)

| Component | Role |
|-----------|------|
| `CommissionPlanController` | REST endpoints at `/api/events/plans` |
| `CommissionPlanService` | CRUD + publishes `PlanCreated`, `PlanActivated`, `RuleAdded` events |
| `CommissionPlanRepository` | JPA data access |
| `CommissionPlanEventListener` | Async listener for plan events |
| `CreateCommissionPlanRequest` / `AddRuleToPlanRequest` / `CommissionPlanResponse` | DTOs |

### 3. Commission Calculation (`features/calculations/`)

| Component | Role |
|-----------|------|
| `CommissionCalculationController` | REST endpoints at `/api/events/calculations` |
| `CommissionCalculationService` | Tier-based calculation + publishes `CommissionCalculated` event |
| `CommissionCalculationRepository` | JPA data access |
| `CommissionCalculationEventListener` | Async listener for calculation events |
| `CalculateCommissionRequest` / `CommissionCalculationResponse` | DTOs |

### 4. Dispute Management (`features/disputes/`)

| Component | Role |
|-----------|------|
| `DisputeController` | REST endpoints at `/api/events/disputes` |
| `DisputeService` | CRUD + publishes `DisputeCreated`, `DisputeResolved`, `DisputeEscalated` events |
| `DisputeRepository` | JPA data access |
| `DisputeEventListener` | Async listener for dispute events |
| `CreateDisputeRequest` / `ResolveDisputeRequest` / `DisputeResponse` | DTOs |

## Event Store API

The event store provides a read-only REST API to query the audit log:

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/events/event-store` | All events (newest first) |
| `GET` | `/api/events/event-store/aggregate/{id}` | Events for a specific entity (oldest first) |
| `GET` | `/api/events/event-store/type/{aggregateType}` | Events by aggregate type (e.g., "Deal") |
| `GET` | `/api/events/event-store/event-type/{eventType}` | Events by event class (e.g., "DealCreatedEvent") |

### Example: Viewing a Deal's Event History

```
GET /api/events/event-store/aggregate/deal-001

[
  {
    "eventId": "a1b2c3...",
    "eventType": "DealCreatedEvent",
    "aggregateId": "deal-001",
    "aggregateType": "Deal",
    "payload": "{\"dealId\":\"deal-001\",\"title\":\"Acme Corp\",\"value\":85000}",
    "occurredAt": "2024-03-15T10:30:00Z"
  },
  {
    "eventId": "d4e5f6...",
    "eventType": "DealUpdatedEvent",
    "aggregateId": "deal-001",
    "aggregateType": "Deal",
    "payload": "{\"dealId\":\"deal-001\",\"updatedField\":\"status\",\"oldValue\":\"OPEN\",\"newValue\":\"WON\"}",
    "occurredAt": "2024-03-15T11:00:00Z"
  }
]
```

## REST Endpoints

### Deal Management
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/events/deals` | Create a new deal |
| `GET` | `/api/events/deals` | List all deals |
| `GET` | `/api/events/deals/{id}` | Get deal by ID |
| `GET` | `/api/events/deals/rep/{salesRepId}` | Get deals by sales rep |
| `GET` | `/api/events/deals/status/{status}` | Get deals by status |
| `PUT` | `/api/events/deals/{id}` | Update deal |
| `DELETE` | `/api/events/deals/{id}` | Delete deal |

### Commission Plans
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/events/plans` | Create a new plan |
| `GET` | `/api/events/plans` | List all plans |
| `GET` | `/api/events/plans/{id}` | Get plan by ID |
| `POST` | `/api/events/plans/{id}/activate` | Activate a plan |
| `POST` | `/api/events/plans/{id}/rules` | Add rule to plan |
| `DELETE` | `/api/events/plans/{id}` | Delete plan |

### Commission Calculations
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/events/calculations` | Calculate commission |
| `GET` | `/api/events/calculations` | List all calculations |
| `GET` | `/api/events/calculations/{id}` | Get calculation by ID |

### Disputes
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/events/disputes` | Create a dispute |
| `GET` | `/api/events/disputes` | List all disputes |
| `GET` | `/api/events/disputes/{id}` | Get dispute by ID |
| `POST` | `/api/events/disputes/{id}/resolve` | Resolve dispute |
| `POST` | `/api/events/disputes/{id}/escalate` | Escalate dispute |
| `DELETE` | `/api/events/disputes/{id}` | Delete dispute |

## Database

### Event Store Table

Created by Flyway migration `V5__create_event_store.sql`:

```sql
CREATE TABLE event_store (
    event_id       VARCHAR(36)  PRIMARY KEY,
    event_type     VARCHAR(100) NOT NULL,
    aggregate_id   VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    payload        TEXT,
    occurred_at    TIMESTAMP    NOT NULL
);
```

Indexed on `aggregate_id`, `aggregate_type`, `event_type`, and `occurred_at` for common query patterns.

## Testing

### Unit Tests

Service-level tests that mock the repository and event publisher:

```bash
mvn test -Dtest="com.chapman.edu.commissions.architecture.eventdriven.features.**"
```

| Test Class | Tests | What It Verifies |
|-----------|-------|-----------------|
| `DealServiceTest` | CRUD + event publishing | Deal logic and DealCreatedEvent/DealUpdatedEvent/DealDeletedEvent |
| `CommissionPlanServiceTest` | Plan lifecycle + events | Plan logic and CommissionPlanCreatedEvent/CommissionPlanActivatedEvent |
| `DisputeServiceTest` | Dispute lifecycle + events | Dispute logic and DisputeCreatedEvent/DisputeResolvedEvent/DisputeEscalatedEvent |

### Integration Test

Full-stack test that exercises the complete event-driven workflow:

```bash
mvn test -Dtest=EventDrivenIntegrationTest
```

The integration test runs an 8-step ordered workflow:
1. Verify seeded deals exist
2. Create a deal → verify `DealCreatedEvent` in event store
3. Update the deal → verify `DealUpdatedEvent` in event store
4. Create a commission plan → verify `CommissionPlanCreatedEvent` in event store
5. Activate the plan → verify `CommissionPlanActivatedEvent` in event store
6. Query full event store → verify all events recorded
7. Filter events by aggregate type
8. Delete the deal → verify `DealDeletedEvent` in event store

## Comparing Event-Driven to Other Patterns

| Aspect | Event-Driven | Vertical Slice | Clean Architecture |
|--------|-------------|----------------|-------------------|
| **Communication** | Events (pub/sub) | Direct method calls | Through ports (interfaces) |
| **Audit trail** | Built-in via event store | Manual logging | Manual logging |
| **Adding behaviors** | Add a new `@EventListener` | Modify service | Modify use case implementation |
| **Async support** | Native via `@Async` | Requires manual setup | Requires manual setup |
| **Complexity** | Events + listeners + store | Simplest | Ports + adapters + layers |
| **Best for** | Systems needing audit trails, notifications, async processing | CRUD-heavy, rapid development | Complex domains, long-lived systems |

## Common Workflows

### Commission Calculation with Event Trail

```
1. POST /api/events/deals          → DealCreatedEvent stored
2. POST /api/events/plans          → CommissionPlanCreatedEvent stored
3. POST /api/events/plans/{id}/rules → RuleAddedToPlanEvent stored
4. POST /api/events/plans/{id}/activate → CommissionPlanActivatedEvent stored
5. POST /api/events/calculations    → CommissionCalculatedEvent stored
6. GET  /api/events/event-store     → View complete audit trail
```

### Dispute Resolution with Event Trail

```
1. POST /api/events/disputes        → DisputeCreatedEvent stored
2. POST /api/events/disputes/{id}/escalate → DisputeEscalatedEvent stored
3. POST /api/events/disputes/{id}/resolve  → DisputeResolvedEvent stored
4. GET  /api/events/event-store/aggregate/{disputeId} → Full dispute history
```

## Technology Stack

- **Spring Boot 3.4.5**
- **Spring Events** (`ApplicationEventPublisher`) — In-process event bus
- **Spring Async** (`@EnableAsync`, `@Async`) — Background event processing
- **Spring Data JPA** — Data persistence + event store
- **H2 Database** — In-memory for development
- **Flyway** — Schema migrations (V5 adds event_store table)
- **Jackson** — Event serialization to JSON for event store payload
- **JUnit 5 & Mockito** — Unit and integration testing

## Development Workflow

### Adding a New Event

1. **Create event class** extending `DomainEvent` in `domain/event/`
2. **Publish event** from the service using `eventPublisher.publishEvent()`
3. **Create listener** (optional) with `@EventListener` + `@Async` + `@Order(10)`
4. The `EventStoreListener` automatically persists it — no additional code needed

### Adding a New Feature

1. **Define domain model** in `domain/`
2. **Create events** in `domain/event/`
3. **Create repository** in the feature package
4. **Create service** with `ApplicationEventPublisher` injection
5. **Create controller** with REST endpoints
6. **Create event listener** for cross-cutting concerns
7. **Define DTOs** for requests/responses
8. **Write tests** — verify both logic and event publishing

---

## Processor Demos

The `EventDrivenProcessor` runs at startup to demonstrate key EDA concepts:

| Demo | Concept | What It Shows |
|------|---------|---------------|
| **Event Publishing** | Domain events | Creating a deal automatically publishes DealCreatedEvent |
| **Event Store** | Audit trail | Append-only log of all events with type, aggregate, payload, timestamp |
| **Event Listeners** | Sync vs Async | EventStoreListener (@Order(1), sync) vs feature listeners (@Order(10), @Async) |
| **Event Replay** | History reconstruction | Query event store to reconstruct aggregate timeline |
| **Decoupling** | Publisher independence | Services publish events without knowing who listens |

---

**Built with Spring Boot 3.4.5 | Event-Driven Architecture | Domain Events + Event Store**
