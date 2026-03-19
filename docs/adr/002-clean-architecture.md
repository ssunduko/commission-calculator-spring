# ADR-002: Clean Architecture (Hexagonal / Ports and Adapters)

## Status
Accepted

## Date
2026-03-19

## Context
The commission domain involves complex business rules — tiered rate calculations, bonus eligibility, plan lifecycle management, and dispute workflows. These rules must remain testable and independent of delivery mechanisms (HTTP, messaging) and persistence technologies (JPA, file-based, external APIs).

We needed an architecture that enforces strict separation between business logic and infrastructure concerns.

## Decision
Adopt Clean Architecture (Hexagonal / Ports and Adapters) organized into concentric layers with inward-only dependencies:

- **Domain (center):** Entities, value objects, domain exceptions, and validation rules. No framework dependencies.
- **Application (use cases):** Orchestrates domain objects. Defines input ports (use case interfaces) and output ports (repository interfaces).
- **Adapters (outer ring):** REST controllers implement input ports; JPA repositories implement output ports.
- **Infrastructure:** Spring configuration, security, and framework wiring.

Key principles enforced:
- Inner layers have zero knowledge of outer layers
- All dependencies point inward via dependency inversion
- Business logic is completely framework-independent
- Ports define explicit system boundaries

## Consequences

### Positive
- Domain logic is fully testable without Spring, JPA, or HTTP
- Swapping infrastructure (e.g., changing persistence from JPA to MongoDB) requires only new adapter implementations
- Explicit port interfaces make system boundaries visible and documented
- High resilience to framework upgrades — core business logic is unaffected

### Negative
- More files per feature (port interfaces, adapter implementations, mappers)
- Developers must understand the layering discipline to avoid shortcuts
- Simple CRUD operations require the same ceremony as complex business logic
- Navigation through multiple layers can slow initial development

### Trade-offs
- Best suited for complex, long-lived systems where the cost of layering is offset by maintainability
- Overkill for simple CRUD features where a vertical slice would suffice
