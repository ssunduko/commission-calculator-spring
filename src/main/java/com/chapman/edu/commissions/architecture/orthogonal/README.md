# Commission Calculator - Orthogonal Architecture

## Overview

This module implements the commission calculator system using **Orthogonal Architecture**. The same domain (deals, plans, calculations, disputes) is restructured around two key ideas:

1. **CQRS** — Operations are modeled as **Command** and **Query** objects, each handled by exactly one handler
2. **Orthogonal Aspects** — Cross-cutting concerns (logging, validation, auditing, performance monitoring) are applied via AOP aspects that are completely independent of business logic

The word "orthogonal" means **independent dimensions**. Each concern is a dimension that can be added, removed, or modified without affecting any other concern or any handler.

## Architecture

```
orthogonal/
├── pipeline/                  # CQRS infrastructure
│   ├── Command.java          # Marker interface for write operations
│   ├── Query.java            # Marker interface for read operations
│   ├── CommandHandler.java   # Handler interface for commands
│   ├── QueryHandler.java     # Handler interface for queries
│   ├── CommandBus.java       # Bus interface for dispatching commands
│   ├── QueryBus.java         # Bus interface for dispatching queries
│   └── PipelineBus.java      # Implementation: auto-discovers & routes
├── aspects/                   # Orthogonal concerns (AOP)
│   ├── logging/              # @Order(1) — Logs all handler executions
│   ├── validation/           # @Order(2) — Auto-validates commands
│   ├── auditing/             # @Order(3) — Records commands to audit_log
│   └── performance/          # @Order(4) — Flags slow operations
├── features/                  # Business logic
│   ├── deals/
│   │   ├── commands/         # CreateDealCommand, UpdateDealCommand, DeleteDealCommand
│   │   ├── queries/          # GetDealQuery, GetAllDealsQuery
│   │   ├── handlers/         # One handler per command/query
│   │   ├── DealController.java
│   │   ├── DealRepository.java
│   │   └── DealResponse.java
│   ├── plans/                # Same pattern
│   ├── calculations/         # Same pattern
│   └── disputes/             # Same pattern
├── domain/                    # Entity classes and enums
└── infrastructure/            # Config, exceptions, data initialization
```

### How It Works

```
    HTTP Request
        │
        ▼
┌──────────────────┐
│   Controller     │   Translates HTTP → Command/Query
│   (thin layer)   │   No business logic
└────────┬─────────┘
         │  commandBus.dispatch(command)
         ▼
┌──────────────────┐
│   PipelineBus    │   Finds the right handler via registry
└────────┬─────────┘
         │
         ▼  handler.handle(command)
┌────────────────────────────────────────────────┐
│  AOP Aspect Chain (orthogonal dimensions)      │
│  ┌──────────────────────────────────────────┐  │
│  │ @Order(1) LoggingAspect                  │  │
│  │  ┌──────────────────────────────────┐    │  │
│  │  │ @Order(2) ValidationAspect       │    │  │
│  │  │  ┌──────────────────────────┐    │    │  │
│  │  │  │ @Order(3) AuditingAspect │    │    │  │
│  │  │  │  ┌──────────────────┐    │    │    │  │
│  │  │  │  │ @Order(4) Perf   │    │    │    │  │
│  │  │  │  │  ┌──────────┐   │    │    │    │  │
│  │  │  │  │  │ HANDLER  │   │    │    │    │  │
│  │  │  │  │  └──────────┘   │    │    │    │  │
│  │  │  │  └──────────────────┘    │    │    │  │
│  │  │  └──────────────────────────┘    │    │  │
│  │  └──────────────────────────────────┘    │  │
│  └──────────────────────────────────────────┘  │
└────────────────────────────────────────────────┘
         │
         ▼
    HTTP Response
```

### Why "Orthogonal"?

Each aspect is an **independent dimension** — it knows nothing about other aspects or handlers:

| Dimension | Applies To | What It Does | Can Be Removed? |
|-----------|-----------|--------------|-----------------|
| Logging | All handlers | Logs execution time and outcome | Yes — no handler changes |
| Validation | Commands only | Auto-calls `validate()` before handler | Yes — no handler changes |
| Auditing | Commands only | Persists to `audit_log` table | Yes — no handler changes |
| Performance | All handlers | Warns if execution > 500ms | Yes — no handler changes |

Adding a new handler? It automatically gets all 4 aspects.
Adding a new aspect? It automatically applies to all existing handlers.
This is true orthogonality — dimensions are independent.

## Key Concepts

### 1. Commands & Queries (CQRS)

**Commands** change state. **Queries** read state. They never mix.

```java
// Command — changes state, returns result
public record CreateDealCommand(
    String title, BigDecimal value, String salesRepId
) implements Command<DealResponse> {
    public void validate() { /* auto-called by ValidationAspect */ }
}

// Query — reads state, no side effects
public record GetAllDealsQuery(
    String salesRepId, DealStatus status
) implements Query<List<DealResponse>> {
    public static GetAllDealsQuery all() { return new GetAllDealsQuery(null, null); }
}
```

### 2. Handlers (Single Responsibility)

Each command/query has exactly ONE handler — pure business logic, no cross-cutting concerns:

```java
@Component
public class CreateDealHandler implements CommandHandler<CreateDealCommand, DealResponse> {
    private final DealRepository dealRepository;

    @Override
    public DealResponse handle(CreateDealCommand command) {
        // ONLY business logic — no logging, no validation, no auditing
        Deal deal = new Deal(command.title(), command.value(), command.salesRepId());
        Deal saved = dealRepository.save(deal);
        return DealResponse.from(saved);
    }
}
```

### 3. Pipeline Bus (Mediator)

The bus auto-discovers handlers at startup and routes commands/queries:

```java
@Component
public class PipelineBus implements CommandBus, QueryBus {
    @PostConstruct
    public void init() {
        // Scans ApplicationContext for all CommandHandler/QueryHandler beans
        // Maps each by its command/query type for O(1) lookup
    }

    public <R, C extends Command<R>> R dispatch(C command) {
        CommandHandler<C, R> handler = commandHandlers.get(command.getClass());
        return handler.handle(command);  // AOP aspects wrap this call
    }
}
```

### 4. Thin Controllers

Controllers have ZERO business logic — they only translate HTTP to commands/queries:

```java
@RestController
@RequestMapping("/api/orthogonal/deals")
public class DealController {
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    @PostMapping
    public ResponseEntity<DealResponse> createDeal(@RequestBody CreateDealCommand command) {
        return new ResponseEntity<>(commandBus.dispatch(command), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealResponse> getDeal(@PathVariable String id) {
        return ResponseEntity.ok(queryBus.dispatch(new GetDealQuery(id)));
    }
}
```

## Orthogonal Aspects

### LoggingAspect (`@Order(1)`)
Outermost aspect — captures total execution time including all other aspects:
```
[LOG] Executing: CreateDealHandler with CreateDealCommand
[LOG] Completed: CreateDealHandler in 12ms
```

### ValidationAspect (`@Order(2)`)
Auto-calls `validate()` on any command that defines it. Invalid commands are rejected before reaching the handler or audit log.

### AuditingAspect (`@Order(3)`)
Records every command execution to the `audit_log` table:
- Only audits **commands** (writes), not queries (reads)
- Records: operation, handler, input data, status (SUCCESS/FAILURE), duration
- Serializes command to JSON for the audit trail

### PerformanceAspect (`@Order(4)`)
Innermost aspect — measures only the handler's own execution time and warns if it exceeds 500ms.

## Audit Log API

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/orthogonal/audit-log` | All audit entries (newest first) |
| `GET` | `/api/orthogonal/audit-log/operation/{name}` | Filter by command name |
| `GET` | `/api/orthogonal/audit-log/status/{status}` | Filter by SUCCESS/FAILURE |

### Example Audit Entry
```json
{
  "id": "a1b2c3...",
  "operation": "CreateDealCommand",
  "handlerName": "CreateDealHandler",
  "inputData": "{\"title\":\"Enterprise Deal\",\"value\":150000,\"salesRepId\":\"rep001\"}",
  "status": "SUCCESS",
  "durationMs": 12,
  "occurredAt": "2024-03-15T10:30:00Z"
}
```

## REST Endpoints

### Deals (`/api/orthogonal/deals`)
| Method | Path | Command/Query |
|--------|------|---------------|
| `POST` | `/api/orthogonal/deals` | `CreateDealCommand` |
| `GET` | `/api/orthogonal/deals/{id}` | `GetDealQuery` |
| `GET` | `/api/orthogonal/deals` | `GetAllDealsQuery` |
| `PUT` | `/api/orthogonal/deals/{id}` | `UpdateDealCommand` |
| `DELETE` | `/api/orthogonal/deals/{id}` | `DeleteDealCommand` |

### Plans (`/api/orthogonal/plans`)
| Method | Path | Command/Query |
|--------|------|---------------|
| `POST` | `/api/orthogonal/plans` | `CreatePlanCommand` |
| `GET` | `/api/orthogonal/plans/{id}` | `GetPlanQuery` |
| `GET` | `/api/orthogonal/plans` | `GetAllPlansQuery` |
| `POST` | `/api/orthogonal/plans/{id}/activate` | `ActivatePlanCommand` |
| `POST` | `/api/orthogonal/plans/{id}/rules` | `AddRuleToPlanCommand` |
| `DELETE` | `/api/orthogonal/plans/{id}` | `DeletePlanCommand` |

### Calculations (`/api/orthogonal/calculations`)
| Method | Path | Command/Query |
|--------|------|---------------|
| `POST` | `/api/orthogonal/calculations` | `CalculateCommissionCommand` |
| `GET` | `/api/orthogonal/calculations/{id}` | `GetCalculationQuery` |
| `GET` | `/api/orthogonal/calculations` | `GetAllCalculationsQuery` |

### Disputes (`/api/orthogonal/disputes`)
| Method | Path | Command/Query |
|--------|------|---------------|
| `POST` | `/api/orthogonal/disputes` | `CreateDisputeCommand` |
| `GET` | `/api/orthogonal/disputes/{id}` | `GetDisputeQuery` |
| `GET` | `/api/orthogonal/disputes` | `GetAllDisputesQuery` |
| `POST` | `/api/orthogonal/disputes/{id}/resolve` | `ResolveDisputeCommand` |
| `POST` | `/api/orthogonal/disputes/{id}/escalate` | `EscalateDisputeCommand` |
| `DELETE` | `/api/orthogonal/disputes/{id}` | `DeleteDisputeCommand` |

## Testing

```bash
# Run all orthogonal tests
mvn test -Dtest="com.chapman.edu.commissions.architecture.orthogonal.**"

# Run just the integration test
mvn test -Dtest=OrthogonalIntegrationTest
```

The integration test verifies:
1. Seeded data is accessible via query bus
2. Deal creation flows through command pipeline
3. Deal updates flow through command pipeline
4. Plan creation and activation work
5. Audit log captures all command executions
6. Audit log can be filtered by operation
7. Deal deletion flows through command pipeline

## Comparing Orthogonal to Other Patterns

| Aspect | Orthogonal | Vertical Slice | Clean Architecture | Event-Driven |
|--------|-----------|----------------|-------------------|--------------|
| **Operations** | Command/Query objects | Service methods | Use case methods | Service methods + events |
| **Cross-cutting** | AOP aspects (automatic) | Manual in each service | Manual in each use case | Event listeners |
| **Adding concerns** | Add 1 aspect → applies everywhere | Modify every service | Modify every use case | Add listener (for events only) |
| **Handler focus** | Pure business logic only | Business + cross-cutting | Business + port wiring | Business + event publishing |
| **Audit trail** | Automatic via AuditingAspect | Manual | Manual | Event store |
| **Complexity** | Commands + handlers + aspects | Simplest | Ports + adapters | Events + listeners |
| **Best for** | Systems needing uniform cross-cutting policies | CRUD-heavy, rapid dev | Complex domains | Async processing, audit |

## Technology Stack

- **Spring Boot 3.4.5**
- **Spring AOP** (`@Aspect`, `@Around`, `@Before`) — Orthogonal concern implementation
- **Spring Data JPA** — Data persistence + audit log
- **H2 Database** — In-memory for development
- **Flyway** — V6 migration creates `audit_log` table
- **Jackson** — Command serialization for audit log
- **JUnit 5** — Integration testing

## Development Workflow

### Adding a New Command
1. Create a `record` implementing `Command<R>` with optional `validate()` method
2. Create a `@Component` implementing `CommandHandler<C, R>`
3. Done — the PipelineBus auto-discovers it, and all aspects apply automatically

### Adding a New Aspect
1. Create a `@Aspect @Component @Order(N)` class
2. Define pointcuts targeting handler methods
3. Done — applies to all existing and future handlers automatically

---

**Built with Spring Boot 3.4.5 | Orthogonal Architecture | CQRS + AOP Aspects**
