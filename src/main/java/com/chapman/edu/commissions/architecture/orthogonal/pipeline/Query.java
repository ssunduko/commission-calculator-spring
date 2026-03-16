package com.chapman.edu.commissions.architecture.orthogonal.pipeline;

/**
 * CONCEPT: Query (CQRS Pattern)
 *
 * A Query represents a request to read data without side effects.
 * Queries are named descriptively: GetDeal, GetAllDeals, GetDealsBySalesRep.
 *
 * Separating queries from commands allows different cross-cutting
 * concerns: queries might get caching decorators, while commands
 * get auditing decorators.
 *
 * @param <R> the return type of the query
 */
public interface Query<R> {
}
