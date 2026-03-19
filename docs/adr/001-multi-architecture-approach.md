# ADR-001: Multi-Architecture Approach for Commission Calculator

## Status
Accepted

## Date
2026-03-19

## Context
The commission calculator system serves as both a production application and an educational reference for comparing software architecture patterns. The domain — commission plan management, deal tracking, calculation engines, and dispute resolution — is complex enough to highlight meaningful trade-offs between architectural styles, yet bounded enough to implement multiple times within a single codebase.

We needed a structure that allows side-by-side comparison of different architecture patterns applied to the same business domain, each independently runnable and self-contained.

## Decision
Implement six architecture patterns as parallel modules under `com.chapman.edu.commissions.architecture`, each containing a complete implementation of the commission domain:

1. **Clean Architecture** (`cleanarchitecture`)
2. **Domain-Driven Design** (`ddd`)
3. **Event-Driven Architecture** (`eventdriven`)
4. **Microservice Architecture** (`microservice`)
5. **Orthogonal Architecture / CQRS** (`orthogonal`)
6. **Vertical Slice Architecture** (`verticalslice`)

All modules operate on the same core entities (Deal, CommissionPlan, CommissionCalculation, Dispute) with consistent statuses and business rules, allowing direct comparison of how each pattern structures the same logic.

## Consequences

### Positive
- Direct apples-to-apples comparison of architecture patterns on the same domain
- Each module is self-contained and can be studied or run independently
- Developers can evaluate trade-offs (coupling, testability, complexity) empirically
- Shared domain concepts create a common vocabulary across modules

### Negative
- Significant code duplication across modules (intentional for independence)
- Larger overall codebase to maintain
- Risk of modules drifting apart in domain behavior over time
- Single repository may create merge contention if multiple modules are developed concurrently

### Risks
- Must ensure each module's domain model stays consistent when business rules evolve
- Build and test times increase linearly with each module added
