package com.chapman.edu.commissions.corespring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Demonstrates caching aspect using @Around advice.
 * Shows practical use of AOP for cross-cutting concerns.
 *
 * CROSS-CUTTING CONCERNS (perfect for AOP):
 * - Logging and auditing
 * - Security and authorization
 * - Transaction management
 * - Caching
 * - Error handling
 * - Performance monitoring
 * - Retry logic
 */
@Aspect
@Component
public class CachingAspect {

    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    /**
     * @Around advice for caching calculation results
     * Demonstrates:
     * - Building cache keys from method signature and parameters
     * - Returning cached values (skipping method execution)
     * - Storing results in cache
     */
    @Around("execution(* com.chapman.edu.commissions.corespring.di.CommissionRuleEngine.calculate*(..))")
    public Object cacheCalculationResults(ProceedingJoinPoint joinPoint) throws Throwable {
        // Build cache key from method name and arguments
        String cacheKey = buildCacheKey(joinPoint);

        // Check if result is in cache
        if (cache.containsKey(cacheKey)) {
            System.out.println("[CACHE] HIT - Returning cached result for: " + cacheKey);
            return cache.get(cacheKey);
        }

        System.out.println("[CACHE] MISS - Executing method: " + joinPoint.getSignature().getName());

        // Execute the method
        Object result = joinPoint.proceed();

        // Store result in cache
        cache.put(cacheKey, result);
        System.out.println("[CACHE] STORED - Cached result for: " + cacheKey);

        return result;
    }

    private String buildCacheKey(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String args = Arrays.toString(joinPoint.getArgs());
        return methodName + ":" + args;
    }

    public void clearCache() {
        cache.clear();
        System.out.println("[CACHE] CLEARED - All cached entries removed");
    }

    public int getCacheSize() {
        return cache.size();
    }
}
