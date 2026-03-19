# ADR-007: Vertical Slice Architecture

## Status
Accepted

## Date
2026-03-19

## Context
For rapid feature development, the overhead of layered architectures (defining ports, implementing adapters, creating handler records) slows down delivery of straightforward CRUD features. The commission system's core operations — managing deals, plans, calculations, and disputes — follow predictable patterns where the primary complexity is in the data model, not in cross-cutting infrastructure.

Additionally, the system needs to integrate with AI agents via Model Context Protocol (MCP), which requires exposing operations as tool-annotated methods rather than through traditional REST endpoints.

## Decision
Adopt Vertical Slice Architecture, organized by feature rather than by technical layer:

```
verticalslice/
  deals/       -> DealController, DealService, DealRepository, Deal entity
  plans/       -> PlanController, PlanService, PlanRepository, Plan entity
  calculations/ -> CalcController, CalcService, CalcRepository, Calculation entity
  disputes/    -> DisputeController, DisputeService, DisputeRepository, Dispute entity
```

Key design decisions:
- **Feature-first organization:** Each slice contains all layers (controller, service, repository, entity) for a single business capability
- **Minimal abstractions:** Concrete classes with direct dependencies (DealController -> DealService -> DealRepository); no port interfaces or adapter indirection
- **Direct inter-slice dependencies:** Slices can reference each other directly when needed (e.g., calculation slice depends on deal and plan slices)
- **MCP integration:** 27 `@Tool`-annotated methods expose commission operations to AI agents via the Model Context Protocol, enabling natural language interaction with the system
- **Rapid development:** 5-6 files per feature vs. 10+ in layered patterns

## Consequences

### Positive
- Fastest time to working feature — minimal boilerplate and ceremony
- Feature ownership is clear — everything for deals is in the deals package
- Easy onboarding — developers can understand a slice without learning architectural abstractions
- MCP integration provides AI agent access without additional API layers
- Adding a new feature is a self-contained task within a new package

### Negative
- No enforced architectural boundaries — a developer can easily create cross-slice coupling
- Shared patterns (validation, error handling) may be duplicated across slices
- As slices grow, they can become mini-monoliths with entangled internal logic
- Harder to swap infrastructure (e.g., changing persistence) since there are no port abstractions

### Trade-offs
- Best for CRUD-heavy applications, rapid prototyping, and feature-focused teams
- The simplicity advantage diminishes as domain complexity grows
- MCP integration makes this the preferred module for AI-assisted commission management
