package com.chapman.edu.commissions.architecture.orthogonal.pipeline;

/**
 * CONCEPT: Command Bus (Mediator)
 *
 * The Command Bus is the central dispatcher that routes commands to
 * their handlers. It serves as the single entry point for all
 * state-changing operations.
 *
 * In orthogonal architecture, the bus doesn't just dispatch — it
 * wraps each handler with decorators (logging, validation, auditing)
 * forming a pipeline that processes every command uniformly.
 */
public interface CommandBus {
    <R, C extends Command<R>> R dispatch(C command);
}
