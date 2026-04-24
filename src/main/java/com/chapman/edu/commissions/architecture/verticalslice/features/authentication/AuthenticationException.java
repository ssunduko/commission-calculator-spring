package com.chapman.edu.commissions.architecture.verticalslice.features.authentication;

/**
 * Thrown when a login attempt fails — wrong password, missing user, or deactivated account.
 * Intentionally a generic message so we do not leak whether a username exists.
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
