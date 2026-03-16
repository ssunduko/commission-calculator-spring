# Commission Calculator - Clean Architecture

## Overview

This module implements the commission calculator system using **Clean Architecture** (also known as Hexagonal Architecture or Ports & Adapters). The application manages deals, commission plans, calculations, and disputes while enforcing strict dependency rules that keep the domain model independent of frameworks and infrastructure.

## Architecture

### Clean Architecture (Ports & Adapters)

Clean Architecture organizes code into concentric layers where dependencies point **inward** — outer layers depend on inner layers, never the reverse. The domain model sits at the center, completely independent of Spring, JPA, or any framework.

```
cleanarchitecture/
├── domain/                    # INNERMOST LAYER — Pure business logic
│   ├── model/                # JPA entities and enums
│   └── exception/            # Domain-specific exceptions
├── application/               # USE CASE LAYER — Orchestrates domain logic
│   ├── port/
│   │   ├── in/               # Input ports (use case interfaces)
│   │   └── out/              # Output ports (repository interfaces)
│   ├── service/              # Use case implementations
│   └── dto/                  # Commands and result objects
├── adapter/                   # OUTERMOST LAYER — Framework integrations
│   ├── in/web/               # Driving adapters (REST controllers)
│   └── out/persistence/      # Driven adapters (JPA repositories)
└── infrastructure/            # Cross-cutting concerns
    ├── config/               # Security, OpenAPI configuration
    ├── data/                 # Data initialization
    └── exception/            # Global exception handling
```

### The Dependency Rule

The fundamental rule of Clean Architecture: **source code dependencies must point inward**.

```
    ┌──────────────────────────────────────────┐
    │  Adapters (Controllers, JPA Repos)       │
    │  ┌──────────────────────────────────┐    │
    │  │  Application (Use Cases, Ports)  │    │
    │  │  ┌──────────────────────────┐    │    │
    │  │  │  Domain (Entities, Rules)│    │    │
    │  │  └──────────────────────────┘    │    │
    │  └──────────────────────────────────┘    │
    └──────────────────────────────────────────┘
         Dependencies point INWARD →→→
```

- **Domain** knows nothing about application, adapters, or infrastructure
- **Application** knows about domain but not about adapters
- **Adapters** know about application (through ports) but domain is accessed indirectly

### Key Concepts

#### Ports (Interfaces)

**Input Ports** define what the application can do (use cases):
```java
public interface DealUseCase {
    DealResult createDeal(CreateDealCommand command);
    DealResult getDeal(String id);
    List<DealResult> getAllDeals();
    void deleteDeal(String id);
}
```

**Output Ports** define what the application needs from the outside world:
```java
public interface DealRepositoryPort {
    Deal save(Deal deal);
    Optional<Deal> findById(String id);
    List<Deal> findAll();
    void deleteById(String id);
}
```

#### Adapters (Implementations)

**Driving Adapters** (input) trigger use cases — e.g., REST controllers:
```java
@RestController
@RequestMapping("/api/clean/deals")
public class DealController {
    private final DealUseCase dealUseCase;  // depends on INPUT PORT
}
```

**Driven Adapters** (output) implement ports — e.g., JPA repositories:
```java
@Repository
public interface SpringDataDealRepository
    extends JpaRepository<Deal, String>, DealRepositoryPort {
    // JPA implements the OUTPUT PORT
}
```

### Benefits of Clean Architecture

1. **Framework Independence**: Domain logic doesn't depend on Spring, JPA, or any framework
2. **Testability**: Use cases can be tested with mock ports — no database or web server needed
3. **Swappable Infrastructure**: Replace JPA with MongoDB by implementing new adapters — domain unchanged
4. **Clear Boundaries**: Ports explicitly define system boundaries
5. **Domain-Centric**: Business rules are protected from infrastructure changes

## Domain Model

### Core Entities (`domain/model/`)

#### Deal
Represents a sales deal that will generate commissions.
- **Fields**: id, title, value, salesRepId, status, closeDate, products
- **Statuses**: OPEN, WON, LOST, CANCELLED

#### CommissionPlan
Defines commission rules and calculation methods.
- **Fields**: id, name, currency, status, rules, tiers, bonusRules
- **Statuses**: DRAFT, ACTIVE, INACTIVE, ARCHIVED

#### CommissionCalculation
Result of calculating commission for a deal.
- **Fields**: id, dealId, salesRepId, planId, baseCommission, grossCommission, netCommission, status
- **Statuses**: CALCULATED, APPROVED, PAID, DISPUTED, ADJUSTED, CANCELLED

#### Dispute
Dispute raised by sales reps regarding commission calculations.
- **Fields**: id, calculationId, salesRepId, title, description, status, comments
- **Statuses**: INITIATED, UNDER_REVIEW, ESCALATED, APPROVED, REJECTED, RESOLVED, CANCELLED

### Domain Exceptions (`domain/exception/`)

- **EntityNotFoundException**: When an entity is not found by ID
- **DomainException**: General domain rule violations

## Application Layer

### Input Ports (`application/port/in/`)

Use case interfaces that define the application's capabilities:

| Port | Operations |
|------|-----------|
| `DealUseCase` | createDeal, getDeal, getAllDeals, getDealsBySalesRep, getDealsByStatus, updateDeal, deleteDeal |
| `CommissionPlanUseCase` | createPlan, getPlan, getAllPlans, getPlansByStatus, activatePlan, addRuleToPlan, deletePlan |
| `CommissionCalculationUseCase` | calculateCommission, getCalculation, getAllCalculations, getCalculationsByDeal, getCalculationsBySalesRep, deleteCalculation |
| `DisputeUseCase` | createDispute, getDispute, getAllDisputes, getDisputesBySalesRep, resolveDispute, escalateDispute, deleteDispute |

### Output Ports (`application/port/out/`)

Repository interfaces that the domain needs:

| Port | Operations |
|------|-----------|
| `DealRepositoryPort` | save, findById, findAll, findBySalesRepId, findByStatus, deleteById |
| `CommissionPlanRepositoryPort` | save, findById, findAll, findByStatus, deleteById |
| `CommissionCalculationRepositoryPort` | save, findById, findAll, findByDealId, findBySalesRepId, deleteById |
| `DisputeRepositoryPort` | save, findById, findAll, findBySalesRepId, findByStatus, deleteById |
| `UserRepositoryPort` | save, findById |

### Commands & Results (`application/dto/`)

**Commands** (input DTOs):
- `CreateDealCommand`, `UpdateDealCommand`
- `CreatePlanCommand`, `AddRuleCommand`
- `CalculateCommissionCommand`
- `CreateDisputeCommand`, `ResolveDisputeCommand`

**Results** (output DTOs):
- `DealResult`, `PlanResult`, `CalculationResult`, `DisputeResult`

### Services (`application/service/`)

Use case implementations that orchestrate domain logic:
- `DealService` implements `DealUseCase`
- `CommissionPlanService` implements `CommissionPlanUseCase`
- `CommissionCalculationService` implements `CommissionCalculationUseCase`
- `DisputeService` implements `DisputeUseCase`

## Adapters

### Web Adapters (`adapter/in/web/`)

REST controllers that drive the application through input ports:

| Controller | Base Path | Endpoints |
|-----------|-----------|-----------|
| `DealController` | `/api/clean/deals` | POST, GET, GET/{id}, GET/rep/{salesRepId}, GET/status/{status}, PUT/{id}, DELETE/{id} |
| `CommissionPlanController` | `/api/clean/plans` | POST, GET, GET/{id}, POST/{id}/activate, POST/{id}/rules, DELETE/{id} |
| `CommissionCalculationController` | `/api/clean/calculations` | POST, GET, GET/{id}, DELETE/{id} |
| `DisputeController` | `/api/clean/disputes` | POST, GET, GET/{id}, POST/{id}/resolve, POST/{id}/escalate, DELETE/{id} |

### Persistence Adapters (`adapter/out/persistence/`)

Spring Data JPA repositories that implement output ports:

| Adapter | Implements |
|---------|-----------|
| `SpringDataDealRepository` | `DealRepositoryPort` |
| `SpringDataCommissionPlanRepository` | `CommissionPlanRepositoryPort` |
| `SpringDataCommissionCalculationRepository` | `CommissionCalculationRepositoryPort` |
| `SpringDataDisputeRepository` | `DisputeRepositoryPort` |
| `SpringDataUserRepository` | `UserRepositoryPort` |

## Infrastructure

### Configuration (`infrastructure/config/`)

- **SecurityConfig**: Permits all requests under `/api/clean/**` for simplified testing
- **OpenApiConfig** *(inherited)*: Swagger/OpenAPI documentation

### Data Initialization (`infrastructure/data/`)

- **DataInitializer**: Seeds sample data on startup (3 users, 2 plans, 6 deals, 2 calculations, 2 disputes) using output ports — not Spring Data repos directly — to demonstrate clean architecture dependency rules

### Exception Handling (`infrastructure/exception/`)

- **GlobalExceptionHandler**: Maps domain exceptions to HTTP responses (EntityNotFoundException → 404, DomainException → 400)

## REST Endpoints

### Deal Management
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/clean/deals` | Create a new deal |
| `GET` | `/api/clean/deals` | List all deals |
| `GET` | `/api/clean/deals/{id}` | Get deal by ID |
| `GET` | `/api/clean/deals/rep/{salesRepId}` | Get deals by sales rep |
| `GET` | `/api/clean/deals/status/{status}` | Get deals by status |
| `PUT` | `/api/clean/deals/{id}` | Update deal |
| `DELETE` | `/api/clean/deals/{id}` | Delete deal |

### Commission Plans
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/clean/plans` | Create a new plan |
| `GET` | `/api/clean/plans` | List all plans |
| `GET` | `/api/clean/plans/{id}` | Get plan by ID |
| `POST` | `/api/clean/plans/{id}/activate` | Activate a plan |
| `POST` | `/api/clean/plans/{id}/rules` | Add rule to plan |
| `DELETE` | `/api/clean/plans/{id}` | Delete plan |

### Commission Calculations
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/clean/calculations` | Calculate commission |
| `GET` | `/api/clean/calculations` | List all calculations |
| `GET` | `/api/clean/calculations/{id}` | Get calculation by ID |
| `DELETE` | `/api/clean/calculations/{id}` | Delete calculation |

### Disputes
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/clean/disputes` | Create a dispute |
| `GET` | `/api/clean/disputes` | List all disputes |
| `GET` | `/api/clean/disputes/{id}` | Get dispute by ID |
| `POST` | `/api/clean/disputes/{id}/resolve` | Resolve dispute |
| `POST` | `/api/clean/disputes/{id}/escalate` | Escalate dispute |
| `DELETE` | `/api/clean/disputes/{id}` | Delete dispute |

## Testing

### Unit Tests

Service-level tests that mock ports to verify business logic in isolation:

```bash
mvn test -Dtest="com.chapman.edu.commissions.architecture.cleanarchitecture.**"
```

| Test Class | Tests | What It Verifies |
|-----------|-------|-----------------|
| `DealServiceTest` | CRUD operations, validation | Deal use case logic |
| `CommissionPlanServiceTest` | Plan lifecycle, rule addition | Plan use case logic |
| `CommissionCalculationServiceTest` | Calculation logic, tier-based rates | Calculation use case logic |
| `DisputeServiceTest` | Dispute lifecycle, escalation | Dispute use case logic |
| `DealControllerTest` | HTTP status codes, JSON structure | Web adapter behavior |
| `CommissionPlanControllerTest` | Plan endpoints | Web adapter behavior |
| `CommissionCalculationControllerTest` | Calculation endpoints | Web adapter behavior |
| `DisputeControllerTest` | Dispute endpoints | Web adapter behavior |

### Integration Tests

Full-stack integration test that exercises the complete workflow through all layers:

```bash
mvn test -Dtest=CleanArchitectureIntegrationTest
```

The integration test runs a 15-step ordered workflow:
1. Verify seeded deals exist
2. Create a new deal
3. Retrieve the created deal
4. Update the deal status to WON
5. Verify seeded plans exist
6. Create a new commission plan
7. Activate the plan
8. Add a rule to the plan
9. Calculate commission for the deal + plan
10. Verify the calculation
11. Create a dispute
12. Verify the dispute
13. Escalate the dispute
14. Resolve the dispute
15. Cleanup: delete dispute, calculation, and deal

## Comparing Clean Architecture to Other Patterns

| Aspect | Clean Architecture | Vertical Slice | Event-Driven |
|--------|-------------------|----------------|--------------|
| **Organization** | By layer (domain, application, adapter) | By feature | By feature + events |
| **Dependencies** | Strict inward-only rule | Feature-scoped | Feature-scoped + event bus |
| **Testability** | Mock ports | Mock repos | Mock repos + event publisher |
| **Adding features** | Touch multiple layers | Add one feature package | Add feature + events + listeners |
| **Best for** | Complex domains, long-lived systems | CRUD-heavy, rapid development | Systems needing audit trails, async processing |

## Technology Stack

- **Spring Boot 3.4.5**
- **Spring Data JPA** — Implements output port adapters
- **Spring Security** — Permits all for `/api/clean/**`
- **H2 Database** — In-memory for development
- **Flyway** — Schema migrations (shared with ORM module)
- **JUnit 5 & Mockito** — Unit and integration testing
- **SpringDoc OpenAPI** — API documentation

## Development Workflow

### Adding a New Feature

1. **Define domain model** in `domain/model/`
2. **Create input port** (use case interface) in `application/port/in/`
3. **Create output port** (repository interface) in `application/port/out/`
4. **Implement use case** in `application/service/`
5. **Define commands/results** in `application/dto/`
6. **Create web adapter** (controller) in `adapter/in/web/`
7. **Create persistence adapter** (JPA repo) in `adapter/out/persistence/`
8. **Write tests** — unit tests for services, controller tests, integration tests

### Dependency Verification

Always verify the dependency rule: search for imports in each layer to ensure:
- `domain/` has zero imports from `application/`, `adapter/`, or `infrastructure/`
- `application/` has zero imports from `adapter/` or `infrastructure/`
- Only `adapter/` and `infrastructure/` import Spring framework classes

---

**Built with Spring Boot 3.4.5 | Clean Architecture (Ports & Adapters)**
