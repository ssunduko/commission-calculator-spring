# ADR-006: Orthogonal Architecture (CQRS + AOP)

## Status
Accepted

## Date
2026-03-19

## Context
The commission system has uniform cross-cutting requirements that apply to every operation: logging, input validation, audit trail persistence, and performance monitoring. In a traditional layered architecture, these concerns are either duplicated across every service method or tangled into base classes that create tight inheritance hierarchies.

Additionally, read and write operations have fundamentally different characteristics — writes must enforce invariants and produce audit entries, while reads need to be fast and flexible. Mixing both in the same service methods creates bloated classes.

## Decision
Adopt an Orthogonal Architecture combining CQRS (Command Query Responsibility Segregation) with AOP (Aspect-Oriented Programming):

**CQRS Pattern:**
- **Commands:** Records that represent state-changing intentions (CreateDealCommand, CalculateCommissionCommand, etc.)
- **Queries:** Records that represent data retrieval requests (GetDealQuery, ListCalculationsQuery, etc.)
- **Command Handlers:** Execute business logic for writes, return results
- **Query Handlers:** Execute read operations, return DTOs
- **Pipeline Bus:** Dispatches commands/queries to their handlers via automatic discovery at startup (`PipelineBus.init()`)
- **Thin Controllers:** Translate HTTP requests into commands/queries; contain no business logic

**AOP Cross-Cutting Concerns (applied orthogonally via aspects):**
1. `LoggingAspect` (`@Order(1)`) — logs entry/exit for all handler executions
2. `ValidationAspect` (`@Order(2)`) — calls `validate()` on commands before handler execution
3. `AuditAspect` (`@Order(3)`) — persists audit log entries for all command operations
4. `PerformanceAspect` (`@Order(4)`) — tracks and logs execution time

Aspect ordering ensures: log -> validate -> audit -> measure -> execute handler.

## Consequences

### Positive
- Cross-cutting concerns are defined once and applied uniformly — no duplication
- Handlers contain only pure business logic; infrastructure concerns are invisible to them
- Adding a new cross-cutting concern (e.g., rate limiting) requires only a new aspect, no handler changes
- CQRS separation allows read and write paths to be optimized independently
- Audit logging is automatic and cannot be accidentally omitted from new features

### Negative
- AOP can be opaque to developers unfamiliar with aspect weaving — behavior is not visible in the call site
- Debugging through proxy layers adds complexity to stack traces
- Command/Query record design requires discipline to keep contracts clean
- Pipeline bus discovery relies on convention; misconfigured handlers fail at runtime, not compile time

### Trade-offs
- Best for systems with many uniform policies that apply to all operations
- The AOP approach trades explicitness for consistency — every operation gets the same treatment
- CQRS adds value when read and write patterns diverge significantly; for simple CRUD it adds ceremony
