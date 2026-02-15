package com.chapman.edu.commissions.springboot.exception;

/**
 * Exception thrown when a business rule validation fails.
 * For example: trying to calculate commission on a cancelled deal,
 * or creating a plan with an end date before the start date.
 *
 * Mapped to HTTP 422 (Unprocessable Entity) by GlobalExceptionHandler.
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }
}
