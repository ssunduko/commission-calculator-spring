package com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
