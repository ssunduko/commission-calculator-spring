package com.chapman.edu.commissions.architecture.verticalslice.features.authentication;

public record LoginResponse(
    String token,
    String tokenType,
    long expiresInSeconds,
    String userId,
    String username,
    String email,
    String fullName
) {
}
