package com.chapman.edu.commissions.architecture.orthogonal.aspects.validation;

import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Command;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * CONCEPT: Validation as an Orthogonal Concern
 *
 * Validation is another independent dimension. This aspect automatically
 * calls validate() on any Command that defines it, BEFORE the handler
 * processes the command.
 *
 * This means:
 * - Handlers never need to call validate() themselves
 * - Validation is guaranteed to run for ALL commands
 * - Validation logic stays in the command object (where the data is)
 * - The validation concern is defined ONCE, not repeated in every handler
 *
 * @Order(2) means this runs AFTER logging but BEFORE auditing,
 * so invalid commands are rejected before being audited.
 */
@Aspect
@Component
@Order(2)
public class ValidationAspect {

    private static final Logger log = LoggerFactory.getLogger(ValidationAspect.class);

    @Before("execution(* com.chapman.edu.commissions.architecture.orthogonal.features..*.handle(..)) && args(command,..)")
    public void validateCommand(JoinPoint joinPoint, Object command) {
        if (command instanceof Command) {
            try {
                Method validateMethod = command.getClass().getMethod("validate");
                validateMethod.invoke(command);
                log.debug("[VALIDATE] Validated: {}", command.getClass().getSimpleName());
            } catch (NoSuchMethodException e) {
                // No validate() method — nothing to validate
            } catch (java.lang.reflect.InvocationTargetException e) {
                // Re-throw the actual validation exception
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                }
                throw new RuntimeException(e.getCause());
            } catch (IllegalAccessException e) {
                log.warn("[VALIDATE] Cannot access validate() on {}", command.getClass().getSimpleName());
            }
        }
    }
}
