# ADR-003: Domain-Driven Design (DDD)

## Status
Accepted

## Date
2026-03-19

## Context
The commission domain has rich business rules: deals transition through lifecycle states, commission plans contain tiered rate structures with bonus rules, calculations enforce invariants around approval and payment workflows, and disputes follow a multi-step resolution process. These rules are interrelated and change frequently based on business needs.

An anemic domain model (entities as data bags with logic in services) would scatter these rules across multiple service classes, making them harder to discover, test, and keep consistent.

## Decision
Adopt Domain-Driven Design organized around four aggregates, each with a root entity that enforces all invariants:

- **Deal Aggregate:** Manages deal lifecycle (OPEN, WON, LOST, CANCELLED) and product associations
- **CommissionPlan Aggregate:** Encapsulates plan activation rules, tier structures, and bonus criteria
- **CommissionCalculation Aggregate:** Guards calculation state transitions (CALCULATED -> APPROVED -> PAID) and recalculation logic
- **Dispute Aggregate:** Enforces dispute resolution workflow (INITIATED -> UNDER_REVIEW -> ESCALATED -> RESOLVED)

Layer structure with inward dependencies:
- **Interfaces:** REST controllers and DTOs
- **Application:** Application services orchestrating aggregate interactions
- **Domain:** Aggregates, entities, value objects, domain events, repository interfaces
- **Infrastructure:** JPA repository implementations, persistence adapters

Key principles:
- Rich domain model — business logic lives inside aggregates, not in services
- Aggregate roots are the only entry points for state changes
- Repository interfaces defined in the domain layer, implemented in infrastructure
- Ubiquitous language: code naming mirrors commission management terminology

## Consequences

### Positive
- Business rules are co-located with the data they protect
- Aggregate boundaries prevent invalid state transitions
- Domain model serves as living documentation of business rules
- Rich model is highly unit-testable without infrastructure

### Negative
- Requires upfront investment in identifying correct aggregate boundaries
- Developers unfamiliar with DDD patterns face a learning curve
- Cross-aggregate operations require application services or domain events
- Risk of aggregates becoming too large if boundaries are drawn incorrectly

### Trade-offs
- Best for complex business domains with many interacting rules
- The upfront design cost pays off as the domain evolves, since changes are localized to the relevant aggregate
