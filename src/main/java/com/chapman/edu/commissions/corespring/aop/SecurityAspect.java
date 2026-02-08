package com.chapman.edu.commissions.corespring.aop;

import com.chapman.edu.commissions.corespring.annotations.RequiresPermission;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Demonstrates security aspect using @Around advice.
 * Shows how to access annotation values in aspects.
 *
 * IMPORTANT AOP CONCEPTS DEMONSTRATED:
 * - Accessing method annotations from joinPoint
 * - Using @Around to prevent method execution (security check)
 * - Throwing exceptions from aspects
 * - Best practices for authorization checks
 */
@Aspect
@Component
public class SecurityAspect {

    /**
     * @Around advice for methods annotated with @RequiresPermission
     * Demonstrates:
     * - How to extract annotation values
     * - How to prevent method execution based on conditions
     * - Practical use of @Around for security
     */
    @Around("@annotation(com.chapman.edu.commissions.corespring.annotations.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // Extract the annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresPermission annotation = method.getAnnotation(RequiresPermission.class);

        String requiredPermission = annotation.value();

        System.out.println("\n[SECURITY] Checking permission: " + requiredPermission);
        System.out.println("[SECURITY] Method: " + method.getName());

        // Simulate permission check (in real app, check against security context)
        boolean hasPermission = checkUserHasPermission(requiredPermission);

        if (!hasPermission) {
            System.out.println("[SECURITY] Access DENIED - missing permission: " + requiredPermission);
            throw new SecurityException("Access denied: requires " + requiredPermission);
        }

        System.out.println("[SECURITY] Access GRANTED - proceeding with method execution");

        // Permission granted - proceed with method execution
        return joinPoint.proceed();
    }

    /**
     * Simulated permission check
     * In a real application, this would check:
     * - Spring Security context
     * - JWT token claims
     * - Database permissions
     */
    private boolean checkUserHasPermission(String permission) {
        // Simplified: grant access to "VIEW_CALCULATIONS" only
        return "VIEW_CALCULATIONS".equals(permission);
    }

    /**
     * Demonstrates the SELF-INVOCATION PITFALL
     *
     * CRITICAL: Spring AOP uses proxies. If a method calls another method
     * in the SAME class, the aspect will NOT be applied!
     *
     * Example:
     * public void methodA() {
     *     methodB();  // <-- This call BYPASSES the proxy!
     * }
     *
     * @Around("methodB")
     * public void methodB() { ... }
     *
     * SOLUTION:
     * 1. Inject self and call through proxy: applicationContext.getBean(MyClass.class).methodB()
     * 2. Move methodB to different class
     * 3. Use AspectJ compile-time weaving instead of Spring AOP
     */
}
