package com.chapman.edu.commissions.architecture.ddd.domain.shared;

/**
 * CONCEPT: Aggregate Root (DDD)
 *
 * An Aggregate Root is the entry point to an Aggregate — a cluster of
 * domain objects that are treated as a single unit for data changes.
 *
 * Rules for Aggregates:
 * 1. External objects may only reference the Aggregate Root, never internal entities
 * 2. All changes to the aggregate must go through the root
 * 3. Deleting the root deletes everything inside the aggregate
 * 4. The root enforces all invariants (business rules) for the aggregate
 *
 * This marker interface identifies which entities are aggregate roots
 * in the commission calculator domain.
 */
public interface AggregateRoot {
}
