package com.chapman.edu.commissions.architecture.orthogonal.pipeline;

/**
 * CONCEPT: Command (CQRS Pattern)
 *
 * A Command represents an intent to change the system's state.
 * Commands are named imperatively: CreateDeal, UpdateDeal, DeleteDeal.
 *
 * In Orthogonal Architecture, commands are first-class objects that
 * flow through a pipeline of decorators before reaching the handler.
 * This allows cross-cutting concerns (logging, validation, auditing)
 * to be applied uniformly to ALL commands without modifying handlers.
 *
 * @param <R> the return type of the command
 */
public interface Command<R> {
}
