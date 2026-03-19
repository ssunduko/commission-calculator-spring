# ADR-008: Shared Domain Model Across Architecture Modules

## Status
Accepted

## Date
2026-03-19

## Context
With six architecture modules implementing the same commission domain, we needed to decide whether to share a single domain model or allow each module to define its own entities independently.

A shared model reduces duplication but creates coupling between modules. Independent models allow each module to optimize its entities for its architectural style but risk divergence in business semantics.

## Decision
Each architecture module defines its own entity classes independently, but all adhere to a shared domain contract:

| Entity                  | Key Fields                                           | Statuses                                                    |
|------------------------|------------------------------------------------------|-------------------------------------------------------------|
| **Deal**               | id, title, value, salesRepId, products               | OPEN, WON, LOST, CANCELLED                                 |
| **CommissionPlan**     | id, name, currency, status, rules, tiers, bonusRules | DRAFT, ACTIVE, INACTIVE, ARCHIVED                           |
| **CommissionCalculation** | id, dealId, planId, baseCommission, netCommission  | CALCULATED, APPROVED, PAID, DISPUTED, ADJUSTED, CANCELLED  |
| **Dispute**            | id, calculationId, salesRepId, title, status         | INITIATED, UNDER_REVIEW, ESCALATED, APPROVED, REJECTED, RESOLVED, CANCELLED |

Key rules:
- Field names and types are consistent across modules
- Status enums have the same values and transition rules
- Business invariants (e.g., only WON deals generate commissions) apply uniformly
- Each module is free to add architectural-specific annotations, methods, and internal structure

## Consequences

### Positive
- Modules are fully independent — changing the DDD entity structure does not affect the Clean Architecture entity
- Each module can use entity patterns appropriate to its style (rich domain model in DDD, anemic in vertical slice)
- No shared dependency creates coupling between modules
- Direct comparison of the same business concept expressed in different architectural styles

### Negative
- Domain rule changes must be applied to six modules manually
- Risk of subtle divergence in business logic between modules
- More total code to maintain across the codebase

### Mitigations
- Each module's README documents the domain contract it implements
- Processor demo classes in each module exercise the same scenarios, serving as behavioral tests for consistency
