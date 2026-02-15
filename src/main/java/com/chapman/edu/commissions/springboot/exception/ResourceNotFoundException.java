package com.chapman.edu.commissions.springboot.exception;

/**
 * ============================================================================
 * CUSTOM EXCEPTION — RESOURCE NOT FOUND
 * ============================================================================
 *
 * CONCEPT: Custom Exceptions in Spring Boot
 * -------------------------------------------
 * Custom exceptions allow you to represent specific error conditions in your
 * application. When thrown, they are caught by the @ControllerAdvice global
 * exception handler which converts them into appropriate HTTP responses.
 *
 * This exception is thrown when a requested resource (Deal, Plan, User, etc.)
 * is not found in the repository. The GlobalExceptionHandler maps this to
 * an HTTP 404 (Not Found) response.
 *
 * CONCEPT: RuntimeException vs Checked Exception
 * -------------------------------------------------
 * We extend RuntimeException (unchecked) rather than Exception (checked) because:
 *   - Spring's @Transactional rolls back on unchecked exceptions by default
 *   - Controller methods don't need explicit throws declarations
 *   - The exception is handled globally by @ControllerAdvice
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final String fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }
}
