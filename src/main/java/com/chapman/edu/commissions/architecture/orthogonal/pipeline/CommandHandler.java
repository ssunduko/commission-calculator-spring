package com.chapman.edu.commissions.architecture.orthogonal.pipeline;

/**
 * CONCEPT: Command Handler
 *
 * Each command has exactly ONE handler responsible for executing it.
 * This 1:1 mapping is a key principle of orthogonal architecture —
 * business logic is isolated in focused, single-responsibility handlers.
 *
 * @param <C> the command type
 * @param <R> the return type
 */
@FunctionalInterface
public interface CommandHandler<C extends Command<R>, R> {
    R handle(C command);
}
