package com.chapman.edu.commissions.corespring.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Demonstrates Spring AOP with various advice types.
 *
 * KEY CONCEPTS:
 * - Aspect: A modularization of a concern that cuts across multiple classes
 * - Join Point: A point during execution (method call, exception thrown, etc.)
 * - Advice: Action taken at a join point (@Before, @After, @Around, etc.)
 * - Pointcut: Expression that matches join points
 * - Weaving: Process of applying aspects to target objects
 *
 * ADVICE TYPES:
 * - @Before: Runs before the method execution
 * - @After: Runs after method execution (finally block - regardless of outcome)
 * - @AfterReturning: Runs after successful method execution
 * - @AfterThrowing: Runs if method throws an exception
 * - @Around: Wraps the method execution (most powerful, can prevent execution)
 */
@Aspect
@Component
public class AuditingAspect {

    /**
     * Pointcut expression using custom annotation
     * Matches any method annotated with @Auditable
     */
    @Pointcut("@annotation(com.chapman.edu.commissions.corespring.annotations.Auditable)")
    public void auditableMethod() {}

    /**
     * Pointcut for all service layer methods
     * Matches: any return type, any class in .di package or subpackages,
     *         any method name, any parameters
     */
    @Pointcut("execution(* com.chapman.edu.commissions.corespring.di..*(..))")
    public void serviceLayer() {}

    /**
     * Pointcut for methods starting with "calculate"
     */
    @Pointcut("execution(* calculate*(..))")
    public void calculationMethods() {}

    /**
     * @Before advice - runs BEFORE the method executes
     * Cannot prevent method execution
     * No access to method return value
     */
    @Before("auditableMethod()")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("\n[AUDIT @Before] Method: " + joinPoint.getSignature().getName());
        System.out.println("[AUDIT @Before] Args: " + Arrays.toString(joinPoint.getArgs()));
        System.out.println("[AUDIT @Before] Time: " + LocalDateTime.now());
    }

    /**
     * @AfterReturning advice - runs AFTER successful method execution
     * Has access to the return value
     * Cannot modify the return value (use @Around for that)
     */
    @AfterReturning(pointcut = "calculationMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("\n[AUDIT @AfterReturning] Method: " + joinPoint.getSignature().getName());
        System.out.println("[AUDIT @AfterReturning] Returned: " + result);
    }

    /**
     * @AfterThrowing advice - runs if method throws an exception
     * Can access the thrown exception
     * Cannot suppress the exception (use @Around for that)
     */
    @AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
        System.out.println("\n[AUDIT @AfterThrowing] Method: " + joinPoint.getSignature().getName());
        System.out.println("[AUDIT @AfterThrowing] Exception: " + ex.getClass().getSimpleName());
        System.out.println("[AUDIT @AfterThrowing] Message: " + ex.getMessage());
    }

    /**
     * @After advice - runs AFTER method execution (like finally block)
     * Executes whether method succeeds or throws exception
     * No access to return value or exception
     */
    @After("auditableMethod()")
    public void logAfter(JoinPoint joinPoint) {
        System.out.println("[AUDIT @After] Completed: " + joinPoint.getSignature().getName() + "\n");
    }

    /**
     * @Around advice - most powerful advice type
     * - Wraps method execution
     * - Can prevent method execution
     * - Can modify parameters and return value
     * - Must call proceed() to execute the method
     * - Can catch and handle exceptions
     *
     * COMMON USES:
     * - Performance monitoring
     * - Transaction management
     * - Security checks
     * - Caching
     * - Retry logic
     */
    @Around("calculationMethods() && args(..)")
    public Object measurePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        System.out.println("\n[PERFORMANCE @Around] Starting: " + joinPoint.getSignature().getName());

        Object result = null;
        try {
            // Proceed with the actual method execution
            result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("[PERFORMANCE @Around] Completed in: " + duration + " ms");

            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("[PERFORMANCE @Around] Failed after: " + duration + " ms");
            throw ex;  // Re-throw the exception
        }
    }
}
