package com.chapman.edu.commissions.architecture.orthogonal.pipeline;

/**
 * CONCEPT: Query Bus (Mediator)
 *
 * The Query Bus routes read-only queries to their handlers.
 * Like the Command Bus, it applies decorators (e.g., logging,
 * caching) uniformly to all queries.
 */
public interface QueryBus {
    <R, Q extends Query<R>> R dispatch(Q query);
}
