package com.chapman.edu.commissions.springboot.controller;

import com.chapman.edu.commissions.springboot.dto.request.LoginRequest;
import com.chapman.edu.commissions.springboot.dto.response.ApiResponse;
import com.chapman.edu.commissions.springboot.dto.response.AuthResponse;
import com.chapman.edu.commissions.springboot.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * AUTHENTICATION CONTROLLER — JWT LOGIN ENDPOINT
 * ============================================================================
 *
 * CONCEPT: Authentication Flow
 * --------------------------------
 * 1. Client sends POST /api/auth/login with username and password
 * 2. AuthenticationManager validates credentials against UserDetailsService
 * 3. If valid, JwtTokenProvider generates a signed JWT
 * 4. JWT is returned to the client in the response body
 * 5. Client stores the JWT and sends it in subsequent requests:
 *      Authorization: Bearer <jwt-token>
 *
 * This endpoint is configured as permitAll() in SecurityConfig,
 * so no authentication is needed to access it.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication — login to obtain a JWT token for API access")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * POST /api/auth/login — Authenticate and get JWT token
     *
     * Request body:
     *   { "username": "admin", "password": "admin123" }
     *
     * Response:
     *   { "token": "eyJhbG...", "tokenType": "Bearer", "username": "admin", "roles": ["SYSTEM_ADMIN"] }
     */
    @Operation(summary = "Login", description = "Authenticate with username and password to receive a JWT token. Use the token in the Authorization header as: Bearer <token>")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        // AuthenticationManager delegates to UserDetailsService to validate credentials
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        // Generate JWT token for the authenticated user
        String jwt = tokenProvider.generateToken(request.getUsername());

        // Extract roles from the authentication object
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        AuthResponse authResponse = new AuthResponse(jwt, request.getUsername(), roles);

        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }
}
