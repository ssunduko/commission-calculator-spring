package com.chapman.edu.commissions.architecture.orthogonal.pipeline;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * CONCEPT: Pipeline Bus (Mediator + Registry)
 *
 * This is the heart of the orthogonal architecture. The PipelineBus:
 *
 * 1. On startup, auto-discovers all CommandHandler and QueryHandler beans
 * 2. Maps each handler to its command/query type using GenericTypeResolver
 * 3. At runtime, dispatches commands/queries to the correct handler
 *
 * Cross-cutting concerns (logging, validation, auditing, performance)
 * are applied via AOP aspects — the bus itself is simple dispatch.
 */
@Component
public class PipelineBus implements CommandBus, QueryBus {

    private static final Logger log = LoggerFactory.getLogger(PipelineBus.class);

    private final ApplicationContext context;
    private final Map<Class<?>, CommandHandler<?, ?>> commandHandlers = new HashMap<>();
    private final Map<Class<?>, QueryHandler<?, ?>> queryHandlers = new HashMap<>();

    public PipelineBus(ApplicationContext context) {
        this.context = context;
    }

    @PostConstruct
    @SuppressWarnings("rawtypes")
    public void init() {
        // Register all CommandHandler beans
        Map<String, CommandHandler> cmdBeans = context.getBeansOfType(CommandHandler.class);
        for (CommandHandler handler : cmdBeans.values()) {
            Class<?>[] typeArgs = GenericTypeResolver.resolveTypeArguments(handler.getClass(), CommandHandler.class);
            if (typeArgs != null && typeArgs.length > 0) {
                commandHandlers.put(typeArgs[0], handler);
                log.info("Registered command handler: {} -> {}",
                        typeArgs[0].getSimpleName(), handler.getClass().getSimpleName());
            }
        }

        // Register all QueryHandler beans
        Map<String, QueryHandler> queryBeans = context.getBeansOfType(QueryHandler.class);
        for (QueryHandler handler : queryBeans.values()) {
            Class<?>[] typeArgs = GenericTypeResolver.resolveTypeArguments(handler.getClass(), QueryHandler.class);
            if (typeArgs != null && typeArgs.length > 0) {
                queryHandlers.put(typeArgs[0], handler);
                log.info("Registered query handler: {} -> {}",
                        typeArgs[0].getSimpleName(), handler.getClass().getSimpleName());
            }
        }

        log.info("Pipeline bus initialized: {} command handlers, {} query handlers",
                commandHandlers.size(), queryHandlers.size());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, C extends Command<R>> R dispatch(C command) {
        CommandHandler<C, R> handler = (CommandHandler<C, R>) commandHandlers.get(command.getClass());
        if (handler == null) {
            throw new IllegalStateException("No handler found for command: " + command.getClass().getSimpleName());
        }
        return handler.handle(command);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, Q extends Query<R>> R dispatch(Q query) {
        QueryHandler<Q, R> handler = (QueryHandler<Q, R>) queryHandlers.get(query.getClass());
        if (handler == null) {
            throw new IllegalStateException("No handler found for query: " + query.getClass().getSimpleName());
        }
        return handler.handle(query);
    }
}
