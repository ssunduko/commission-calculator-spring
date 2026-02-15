package com.chapman.edu.commissions.springboot.exception;

/**
 * Exception thrown when authentication or authorization fails.
 * Mapped to HTTP 401 (Unauthorized) by GlobalExceptionHandler.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
