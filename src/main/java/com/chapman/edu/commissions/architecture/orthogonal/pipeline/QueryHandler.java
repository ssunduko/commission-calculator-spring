package com.chapman.edu.commissions.architecture.orthogonal.pipeline;

/**
 * CONCEPT: Query Handler
 *
 * Each query has exactly ONE handler that retrieves the requested data.
 * Query handlers are pure read operations with no side effects.
 *
 * @param <Q> the query type
 * @param <R> the return type
 */
@FunctionalInterface
public interface QueryHandler<Q extends Query<R>, R> {
    R handle(Q query);
}
