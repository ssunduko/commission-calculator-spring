package com.chapman.edu.commissions.architecture.orthogonal.aspects.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * CONCEPT: Logging as an Orthogonal Concern
 *
 * In orthogonal architecture, logging is a DIMENSION that is independent
 * of business logic. This aspect intercepts ALL command and query handler
 * executions, logging:
 * - What operation was requested (command/query class name)
 * - How long it took (execution time in ms)
 * - Whether it succeeded or failed
 *
 * This is "orthogonal" because:
 * - Adding new handlers automatically gets logging — no code changes needed
 * - Removing this aspect removes ALL logging — no handler changes needed
 * - Logging logic exists in exactly ONE place
 *
 * @Order(1) means this runs FIRST (outermost decorator), so it captures
 * the total time including all other decorators.
 */
@Aspect
@Component
@Order(1)
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.chapman.edu.commissions.architecture.orthogonal.features..*.handle(..))")
    public Object logHandlerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String handlerName = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();
        String operationName = args.length > 0 ? args[0].getClass().getSimpleName() : "unknown";

        log.info("[LOG] Executing: {} with {}", handlerName, operationName);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("[LOG] Completed: {} in {}ms", handlerName, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[LOG] Failed: {} after {}ms — {}", handlerName, duration, e.getMessage());
            throw e;
        }
    }
}
