package com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception;

/**
 * Base exception for domain-level validation and business rule violations.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
