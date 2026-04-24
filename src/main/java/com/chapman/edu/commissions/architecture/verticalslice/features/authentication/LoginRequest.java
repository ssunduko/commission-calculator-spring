package com.chapman.edu.commissions.architecture.verticalslice.features.authentication;

public record LoginRequest(String username, String password) {
    public void validate() {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
    }
}
