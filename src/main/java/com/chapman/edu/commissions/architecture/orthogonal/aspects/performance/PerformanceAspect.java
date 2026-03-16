package com.chapman.edu.commissions.architecture.orthogonal.aspects.performance;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * CONCEPT: Performance Monitoring as an Orthogonal Concern
 *
 * This aspect monitors handler execution time and flags slow operations.
 * It demonstrates how a new concern can be added without touching
 * any existing handler or decorator — true orthogonality.
 *
 * @Order(4) means this is the innermost aspect, closest to the actual
 * handler execution. It measures only the handler's own time, not
 * the time spent in logging/validation/auditing decorators.
 */
@Aspect
@Component
@Order(4)
public class PerformanceAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceAspect.class);
    private static final long SLOW_THRESHOLD_MS = 500;

    @Around("execution(* com.chapman.edu.commissions.architecture.orthogonal.features..*.handle(..))")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTime;

        if (duration > SLOW_THRESHOLD_MS) {
            log.warn("[PERF] SLOW operation: {} took {}ms (threshold: {}ms)",
                    joinPoint.getTarget().getClass().getSimpleName(), duration, SLOW_THRESHOLD_MS);
        }

        return result;
    }
}
