# Commission Calculator - Domain-Driven Design Architecture

## Overview

This is a Spring Boot application implementing a commission calculator system using **Domain-Driven Design (DDD)**. The application manages deals, commission plans, calculations, and disputes through well-defined aggregates, domain services, and a layered architecture where dependencies always point inward toward the domain.

DDD focuses on modeling the business domain explicitly in code. Every class, method, and package name in this module is chosen to reflect the **ubiquitous language** of commission management, making the codebase a living model of the business.

## Architecture

### Layered Architecture (Dependencies Point Inward)

```
┌─────────────────────────────────────────────────────────┐
│                    INTERFACES LAYER                     │
│         (REST Controllers — /api/ddd/*)                 │
│     DealController, PlanController, etc.                │
├─────────────────────────────────────────────────────────┤
│                   APPLICATION LAYER                     │
│       (Application Services — use-case orchestration)   │
│  DealApplicationService, CommissionPlanApplicationSvc   │
│  CommissionCalculationApplicationService                │
│  DisputeApplicationService                              │
│          DTOs: CreateDealRequest, DealDto, etc.         │
├─────────────────────────────────────────────────────────┤
│                     DOMAIN LAYER                        │
│    (Aggregates, Domain Services, Repository Interfaces) │
│  Deal, CommissionPlan, CommissionCalculation, Dispute   │
│  CommissionCalculationService (domain service)          │
│  DealRepository, PlanRepository (interfaces only)       │
│  AggregateRoot, DomainException (shared kernel)         │
├─────────────────────────────────────────────────────────┤
│                  INFRASTRUCTURE LAYER                   │
│      (JPA Repositories, Config, Data Seeding)           │
│  JpaDealRepository, JpaPlanRepository, etc.             │
│  SecurityConfig, OpenApiConfig, DataInitializer         │
└─────────────────────────────────────────────────────────┘
```

**Key rule:** Each layer may only depend on the layer directly below it. The domain layer has **zero** dependencies on Spring, JPA implementations, or HTTP concerns.

### Benefits of Domain-Driven Design

1. **Rich Domain Model** — Business logic lives inside aggregates, not in anemic services
2. **Enforced Invariants** — Aggregate roots guard all state changes through domain methods
3. **Persistence Ignorance** — Domain-level repository interfaces hide infrastructure details
4. **Ubiquitous Language** — Code reads like the business domain (Aggregate, DomainException, not generic terms)
5. **Testable Core** — The domain layer can be unit-tested without Spring context or database
6. **Clear Boundaries** — Aggregates define transactional consistency boundaries

## Key DDD Concepts

### 1. Aggregates & Aggregate Roots

An **Aggregate** is a cluster of domain objects treated as a single unit for data changes. The **Aggregate Root** is the only entry point — external code never reaches inside.

```java
// Marker interface identifying aggregate roots
public interface AggregateRoot { }

// Deal is an aggregate root — it owns DealProduct entities
@Entity
public class Deal implements AggregateRoot {
    private String id;
    private String title;
    private BigDecimal value;
    private DealStatus status;
    private List<DealProduct> products;  // owned by this aggregate

    // All mutations go through the root
    public void addProduct(DealProduct product) { ... }
    public void setStatus(DealStatus status) { ... }
    public BigDecimal calculateTotalValue() { ... }
}
```

**Aggregates in this module:**
| Aggregate Root | Owned Entities | Invariants |
|---|---|---|
| `Deal` | `DealProduct` | Value > 0, status transitions |
| `CommissionPlan` | `CommissionRule`, `CommissionTier`, `BonusRule` | Must be ACTIVE to calculate |
| `CommissionCalculation` | `BonusCalculation`, `AcceleratorCalculation` | Recalculate on changes |
| `Dispute` | `DisputeComment` | Escalation updates status |
| `User` | (roles via `@Transient`) | Username/email uniqueness |

### 2. Domain-Level Repository Interfaces vs Infrastructure Implementations

In DDD, the **domain layer** defines repository interfaces using domain language. The **infrastructure layer** provides concrete implementations using JPA.

```java
// Domain layer — no Spring imports, pure interface
package ...domain.user;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(String id);
}

// Infrastructure layer — Spring Data JPA implementation
package ...infrastructure.persistence;

@Repository
public interface JpaUserRepository extends JpaRepository<User, String>, UserRepository {
    // Spring Data auto-implements save() and findById()
}
```

This inversion means the domain never knows about JPA, Hibernate, or any persistence technology.

### 3. Domain Services

A **Domain Service** contains business logic that spans multiple aggregates. It belongs in the domain layer because it expresses a core business concept.

```java
// Domain service — calculates commission across Deal + CommissionPlan
public class CommissionCalculationService {

    public CommissionCalculation calculate(Deal deal, CommissionPlan plan) {
        // Apply plan rules to deal value
        BigDecimal base = deal.getValue()
            .multiply(findApplicableRate(plan))
            .divide(BigDecimal.valueOf(100));

        CommissionCalculation calc = new CommissionCalculation(
            deal.getId(), deal.getSalesRepId(), base);
        calc.setPlanId(plan.getId());
        calc.recalculate();
        return calc;
    }
}
```

### 4. Application Services with @Transactional

**Application Services** orchestrate use cases. They load aggregates via repositories, invoke domain logic, and persist results. They do NOT contain business rules.

```java
@Service
@Transactional
public class DealApplicationService {

    private final DealRepository dealRepository;

    public DealDto createDeal(CreateDealRequest request) {
        request.validate();                          // input validation
        Deal deal = new Deal(                        // domain object creation
            request.title(), request.value(), request.salesRepId());
        Deal saved = dealRepository.save(deal);      // persist via repository
        return DealDto.fromEntity(saved);            // map to DTO
    }

    public DealDto updateDeal(String id, UpdateDealRequest request) {
        Deal deal = dealRepository.findById(id)
            .orElseThrow(() -> new DomainException("Deal not found: " + id));
        if (request.status() != null) deal.setStatus(request.status());
        return DealDto.fromEntity(dealRepository.save(deal));
    }
}
```

### 5. Ubiquitous Language

Every class and method uses terminology from the commission management domain:

| DDD Term | Example in Code | Meaning |
|---|---|---|
| Aggregate Root | `Deal implements AggregateRoot` | Entry point enforcing invariants |
| Repository | `DealRepository` (interface in domain) | Persistence abstraction |
| Domain Service | `CommissionCalculationService` | Cross-aggregate business logic |
| Application Service | `DealApplicationService` | Use-case orchestration |
| Domain Exception | `DomainException` | Business rule violation |
| Value Object | `DealProduct`, `RuleCondition` | Immutable descriptors |

### 6. Shared Kernel

The `domain.shared` package contains building blocks used across all aggregates:

- **`AggregateRoot`** — Marker interface for aggregate roots
- **`DomainException`** — Base exception for domain rule violations
- **`ConditionOperator`** — Shared enum for rule conditions (EQUALS, GREATER_THAN, etc.)
- **`LogicalOperator`** — Shared enum for combining conditions (AND, OR)

## Package Structure

```
ddd/
├── DddCommissionApplication.java            # Spring Boot entry point
├── README.md                                 # This file
│
├── domain/                                   # DOMAIN LAYER (pure business logic)
│   ├── shared/                               # Shared Kernel
│   │   ├── AggregateRoot.java               # Marker interface
│   │   ├── DomainException.java             # Domain rule violations
│   │   ├── ConditionOperator.java           # Rule condition operators
│   │   └── LogicalOperator.java             # Logical combination operators
│   ├── deal/                                 # Deal Aggregate
│   │   ├── Deal.java                        # Aggregate Root
│   │   ├── DealProduct.java                 # Value Object
│   │   └── DealStatus.java                  # Enum
│   ├── plan/                                 # CommissionPlan Aggregate
│   │   ├── CommissionPlan.java              # Aggregate Root
│   │   ├── CommissionRule.java              # Entity
│   │   ├── CommissionTier.java              # Entity
│   │   ├── BonusRule.java                   # Entity
│   │   ├── RuleCondition.java              # Value Object
│   │   ├── PlanStatus.java                  # Enum
│   │   ├── RuleType.java                    # Enum
│   │   └── BonusType.java                   # Enum
│   ├── calculation/                          # CommissionCalculation Aggregate
│   │   ├── CommissionCalculation.java       # Aggregate Root
│   │   ├── BonusCalculation.java            # Value Object
│   │   ├── AcceleratorCalculation.java      # Value Object
│   │   └── CommissionStatus.java            # Enum
│   ├── dispute/                              # Dispute Aggregate
│   │   ├── Dispute.java                     # Aggregate Root
│   │   ├── DisputeComment.java              # Value Object
│   │   └── DisputeStatus.java               # Enum
│   └── user/                                 # User Aggregate
│       ├── User.java                        # Aggregate Root
│       ├── UserRepository.java              # Domain Repository Interface
│       └── UserRole.java                    # Enum
│
├── application/                              # APPLICATION LAYER (use-case orchestration)
│   ├── dto/                                  # Data Transfer Objects
│   │   ├── CreateDealRequest.java
│   │   ├── UpdateDealRequest.java
│   │   ├── DealDto.java
│   │   ├── CreatePlanRequest.java
│   │   ├── AddRuleRequest.java
│   │   ├── CommissionPlanDto.java
│   │   ├── CalculateCommissionRequest.java
│   │   ├── CommissionCalculationDto.java
│   │   ├── CreateDisputeRequest.java
│   │   └── DisputeDto.java
│   ├── deal/                                 # Deal Application Service
│   ├── plan/                                 # Commission Plan Application Service
│   ├── calculation/                          # Calculation Application Service
│   └── dispute/                              # Dispute Application Service
│
├── interfaces/                               # INTERFACES LAYER (REST controllers)
│   ├── rest/
│   │   ├── DealController.java
│   │   ├── CommissionPlanController.java
│   │   ├── CommissionCalculationController.java
│   │   └── DisputeController.java
│   └── GlobalExceptionHandler.java
│
├── processor/                               # STARTUP DEMOS — Showcases DDD concepts
│   ├── DddProcessor.java                   # Demonstrates all DDD patterns
│   └── DddProcessorDemo.java               # CommandLineRunner for startup demos
│
└── infrastructure/                           # INFRASTRUCTURE LAYER
    ├── persistence/                          # JPA Repository implementations
    │   ├── JpaDealRepository.java
    │   ├── JpaPlanRepository.java
    │   ├── JpaCalculationRepository.java
    │   ├── JpaDisputeRepository.java
    │   └── JpaUserRepository.java
    ├── config/                               # Spring configuration
    │   ├── SecurityConfig.java
    │   └── OpenApiConfig.java
    └── data/                                 # Seed data
        └── DataInitializer.java
```

## REST Endpoints

All endpoints are prefixed with `/api/ddd/` to distinguish from other architecture modules.

### Deal Management (`/api/ddd/deals`)

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/ddd/deals` | Create a new deal aggregate |
| `GET` | `/api/ddd/deals` | List all deals |
| `GET` | `/api/ddd/deals/{id}` | Get deal by ID |
| `GET` | `/api/ddd/deals/rep/{salesRepId}` | Get deals by sales rep |
| `GET` | `/api/ddd/deals/status/{status}` | Get deals by status |
| `PUT` | `/api/ddd/deals/{id}` | Update deal (status, value, etc.) |
| `DELETE` | `/api/ddd/deals/{id}` | Delete deal aggregate |

### Commission Plan Management (`/api/ddd/plans`)

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/ddd/plans` | Create a new commission plan aggregate |
| `GET` | `/api/ddd/plans` | List all plans |
| `GET` | `/api/ddd/plans/{id}` | Get plan by ID |
| `GET` | `/api/ddd/plans/status/{status}` | Get plans by status |
| `POST` | `/api/ddd/plans/{id}/activate` | Transition plan to ACTIVE status |
| `POST` | `/api/ddd/plans/{id}/rules` | Add commission rule to plan aggregate |
| `DELETE` | `/api/ddd/plans/{id}` | Delete plan aggregate |

### Commission Calculations (`/api/ddd/calculations`)

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/ddd/calculations` | Calculate commission (domain service) |
| `GET` | `/api/ddd/calculations` | List all calculations |
| `GET` | `/api/ddd/calculations/{id}` | Get calculation by ID |
| `GET` | `/api/ddd/calculations/deal/{dealId}` | Get calculations by deal |
| `GET` | `/api/ddd/calculations/rep/{salesRepId}` | Get calculations by sales rep |

### Dispute Management (`/api/ddd/disputes`)

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/ddd/disputes` | Create dispute aggregate |
| `GET` | `/api/ddd/disputes` | List all disputes |
| `GET` | `/api/ddd/disputes/{id}` | Get dispute by ID |
| `GET` | `/api/ddd/disputes/rep/{salesRepId}` | Get disputes by sales rep |
| `GET` | `/api/ddd/disputes/status/{status}` | Get disputes by status |
| `POST` | `/api/ddd/disputes/{id}/resolve` | Resolve a dispute |
| `POST` | `/api/ddd/disputes/{id}/escalate` | Escalate dispute to management |
| `DELETE` | `/api/ddd/disputes/{id}` | Delete dispute aggregate |

## Testing

### Context Test

Verifies the Spring application context loads with all DDD layers wired correctly:

```bash
mvn test -Dtest=DddCommissionApplicationTests
```

### Integration Tests

End-to-end tests that exercise the full DDD stack (controller -> application service -> domain -> repository):

```bash
mvn test -Dtest=DddIntegrationTest
```

The integration test walks through a complete commission workflow:
1. Verify seeded deal aggregates exist
2. Create a new deal aggregate root
3. Update deal status to WON
4. Create a commission plan aggregate
5. Activate the plan (status transition)
6. Add a commission rule to the plan
7. Calculate commission via domain service
8. Create a dispute aggregate
9. Escalate the dispute
10. Delete the dispute

### Unit Tests

Domain objects can be tested without Spring context:

```bash
mvn test -Dtest=*DomainTest
```

## Comparison with Other Architecture Patterns

| Aspect | DDD | Vertical Slice | Clean Architecture | Event-Driven | Orthogonal |
|---|---|---|---|---|---|
| **Primary organizing principle** | Business domain aggregates | Features / use cases | Dependency inversion layers | Events and reactions | Independent dimensions |
| **Where business logic lives** | Domain layer (aggregates + domain services) | Feature service | Use cases + entities | Event handlers | Aspect-specific modules |
| **Persistence abstraction** | Domain repository interfaces, infra implements | Repository per feature | Gateway interfaces | Event store / projections | Data access dimension |
| **Cross-cutting concerns** | Shared kernel | Infrastructure package | Outer rings | Middleware / interceptors | Orthogonal aspects |
| **Coupling direction** | Inward (infra -> domain) | Within slice | Inward (frameworks -> entities) | Through events (loose) | Between dimensions (loose) |
| **When to use** | Complex domains with rich business rules | Feature-focused teams, moderate complexity | Large systems needing strict decoupling | Async workflows, audit trails | Systems with many cross-cutting concerns |
| **Testing strategy** | Domain unit tests + integration tests | Slice-level tests | Use case tests | Event handler tests | Per-dimension tests |
| **Key strength** | Models the business explicitly in code | Simple navigation, feature cohesion | Maximum decoupling | Temporal decoupling, audit | Separation of independent concerns |

## Technology Stack

- **Spring Boot 3.4.5**
- **Spring Data JPA** — Infrastructure-layer repository implementations
- **Spring Security** — Authentication/Authorization
- **H2 Database** — In-memory database for development
- **Lombok** — Reduces boilerplate in domain entities
- **SpringDoc OpenAPI** — API documentation
- **JUnit 5 & Mockito** — Testing
- **AssertJ** — Fluent test assertions

## Development Workflow

### Adding a New Aggregate

Follow these steps to add a new aggregate (e.g., `Product`) to the DDD module:

1. **Define the Aggregate Root** in `domain/product/`
   ```java
   @Entity
   public class Product implements AggregateRoot { ... }
   ```

2. **Define the Domain Repository Interface** in `domain/product/`
   ```java
   public interface ProductRepository {
       Product save(Product product);
       Optional<Product> findById(String id);
       List<Product> findAll();
   }
   ```

3. **Create DTOs** in `application/dto/`
   ```java
   public record ProductDto(...) {
       public static ProductDto fromEntity(Product p) { ... }
   }
   public record CreateProductRequest(...) { void validate() { ... } }
   ```

4. **Create the Application Service** in `application/product/`
   ```java
   @Service @Transactional
   public class ProductApplicationService { ... }
   ```

5. **Implement the JPA Repository** in `infrastructure/persistence/`
   ```java
   @Repository
   public interface JpaProductRepository
       extends JpaRepository<Product, String>, ProductRepository { }
   ```

6. **Create the REST Controller** in `interfaces/rest/`
   ```java
   @RestController @RequestMapping("/api/ddd/products")
   public class ProductController { ... }
   ```

7. **Add entity scan** — Update `@EntityScan` in `DddCommissionApplication`

8. **Write tests** — Domain unit tests + integration tests

## Common Workflows

### 1. Commission Calculation Workflow

```
1. Create Deal         → POST /api/ddd/deals
2. Create Plan         → POST /api/ddd/plans
3. Add Rules to Plan   → POST /api/ddd/plans/{id}/rules
4. Activate Plan       → POST /api/ddd/plans/{id}/activate
5. Calculate           → POST /api/ddd/calculations
6. Review Result       → GET  /api/ddd/calculations/{id}
```

### 2. Dispute Resolution Workflow

```
1. Identify Issue      → GET  /api/ddd/calculations/{id}
2. Create Dispute      → POST /api/ddd/disputes
3. Escalate if Needed  → POST /api/ddd/disputes/{id}/escalate
4. Resolve Dispute     → POST /api/ddd/disputes/{id}/resolve
```

## Database

### H2 Console

Access the H2 database console at:
```
http://localhost:8080/h2-console
```

**Connection Details:**
- JDBC URL: `jdbc:h2:mem:commissiondbtwo`
- Username: `sa`
- Password: (empty)

## Running the Application

### Prerequisites
- Java 21+
- Maven 3.8+

### Build
```bash
mvn clean package
```

### Run
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=ddd
```

The application will start on port 8080.

---

## Processor Demos

The `DddProcessor` runs at startup to demonstrate key DDD concepts:

| Demo | Concept | What It Shows |
|------|---------|---------------|
| **Aggregate Roots** | Aggregate boundaries | Deal, CommissionPlan as roots; internal entities accessed only through root |
| **Domain Services** | Cross-aggregate logic | CommissionCalculationService — stateless, static, coordinates Deal + Plan |
| **App vs Domain Service** | Layer responsibilities | Application service orchestrates; domain service calculates |
| **Repository per Aggregate** | Persistence abstraction | One repo per root; no repo for DealProduct, CommissionRule, etc. |
| **Full DDD Flow** | End-to-end | Request → Application Service → Domain Service → Aggregate → Repository |

---

**Built with Spring Boot 3.4.5 | Domain-Driven Design Architecture | Layered with Inward Dependencies**
