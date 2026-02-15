package com.chapman.edu.commissions.springboot.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user authentication (login) requests.
 *
 * CONCEPT: Authentication Flow
 * ------------------------------
 *   1. Client POSTs username/password to /api/auth/login
 *   2. Server validates credentials against stored user data
 *   3. If valid, server generates a JWT token and returns it
 *   4. Client stores the JWT and includes it in subsequent requests:
 *        Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 */
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    // --- Getters and Setters ---

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
