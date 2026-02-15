package com.chapman.edu.commissions.springboot.dto.response;

import java.util.Set;

/**
 * Response DTO for authentication (login) responses.
 * Returns the JWT token and basic user info upon successful authentication.
 */
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private String username;
    private Set<String> roles;

    public AuthResponse(String token, String username, Set<String> roles) {
        this.token = token;
        this.username = username;
        this.roles = roles;
    }

    // --- Getters and Setters ---

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}
