package com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception;

/**
 * Exception thrown when a requested entity cannot be found.
 */
public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String entityName, String id) {
        super(entityName + " not found with id: " + id);
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
