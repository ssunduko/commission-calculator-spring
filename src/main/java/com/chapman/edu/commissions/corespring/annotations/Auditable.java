package com.chapman.edu.commissions.corespring.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to mark methods that should be audited.
 * Demonstrates creating custom annotations for AOP pointcuts.
 *
 * Example usage in lecture: Creating custom annotations for cross-cutting concerns
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String action() default "";
    boolean logParams() default true;
}
