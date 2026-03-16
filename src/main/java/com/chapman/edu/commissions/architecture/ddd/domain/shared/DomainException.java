package com.chapman.edu.commissions.architecture.ddd.domain.shared;

/**
 * CONCEPT: Domain Exception
 *
 * Exceptions that originate from domain rule violations. These are
 * distinct from infrastructure exceptions (database errors, network
 * failures) and represent business rule violations.
 */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
