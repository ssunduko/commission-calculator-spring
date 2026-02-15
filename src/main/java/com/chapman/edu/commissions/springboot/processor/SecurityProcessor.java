package com.chapman.edu.commissions.springboot.processor;

import com.chapman.edu.commissions.springboot.security.JwtTokenProvider;
import com.chapman.edu.commissions.springboot.security.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * PROCESSOR: SPRING SECURITY BASICS DEMONSTRATION
 * ============================================================================
 *
 * This runnable demonstrates Spring Security concepts:
 *
 *   1. Authentication — Verifying user identity (JWT token generation)
 *   2. Authorization — Role-based access control (RBAC)
 *   3. JWT Token — Generation, validation, and claim extraction
 *   4. UserDetailsService — Loading user data for authentication
 *   5. Security Filter Chain — How requests are intercepted and validated
 *   6. Password Encoding — BCrypt hashing
 *
 * SECURITY FILTER CHAIN FLOW (for API requests):
 *   Client Request → JwtAuthenticationFilter → SecurityContext →
 *   Authorization Check → Controller → Service → Response
 *
 *   1. JwtAuthenticationFilter extracts JWT from Authorization header
 *   2. JwtTokenProvider validates the token signature and expiration
 *   3. CustomUserDetailsService loads user roles from repository
 *   4. SecurityContext is populated with the authenticated user
 *   5. Authorization rules (@PreAuthorize, URL rules) are checked
 *   6. If authorized, the request proceeds to the controller
 */
@Component
@Order(5)
public class SecurityProcessor implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SecurityProcessor.class);

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public SecurityProcessor(JwtTokenProvider tokenProvider,
                             CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void run(String... args) {
        logger.info("");
        logger.info("╔══════════════════════════════════════════════════════════════╗");
        logger.info("║   SPRING SECURITY BASICS DEMONSTRATION                      ║");
        logger.info("╚══════════════════════════════════════════════════════════════╝");

        demonstrateUserDetails();
        demonstrateJwtTokens();
        demonstrateRoleBasedAccess();

        logger.info("");
        logger.info("=== Security Demo Complete ===");
        logger.info("");
    }

    /**
     * Demonstrates UserDetailsService — loading user data for authentication.
     */
    private void demonstrateUserDetails() {
        logger.info("");
        logger.info("--- UserDetailsService: Loading User Details ---");

        // Load admin user
        UserDetails admin = userDetailsService.loadUserByUsername("admin");
        logger.info("User: {}", admin.getUsername());
        logger.info("  Authorities: {}", admin.getAuthorities());
        logger.info("  Enabled: {}", admin.isEnabled());
        logger.info("  Password (BCrypt hash): {}...", admin.getPassword().substring(0, 20));

        // Load sales rep
        UserDetails salesRep = userDetailsService.loadUserByUsername("agarcia");
        logger.info("");
        logger.info("User: {}", salesRep.getUsername());
        logger.info("  Authorities: {}", salesRep.getAuthorities());

        // Load manager
        UserDetails manager = userDetailsService.loadUserByUsername("jsmith");
        logger.info("");
        logger.info("User: {}", manager.getUsername());
        logger.info("  Authorities: {}", manager.getAuthorities());
    }

    /**
     * Demonstrates JWT token generation, validation, and claim extraction.
     */
    private void demonstrateJwtTokens() {
        logger.info("");
        logger.info("--- JWT Token Operations ---");

        // Generate token for admin
        String adminToken = tokenProvider.generateToken("admin");
        logger.info("Generated JWT for 'admin':");
        logger.info("  Token: {}...", adminToken.substring(0, Math.min(50, adminToken.length())));
        logger.info("  Token length: {} characters", adminToken.length());

        // Validate token
        boolean isValid = tokenProvider.validateToken(adminToken);
        logger.info("  Token valid: {}", isValid);

        // Extract username from token
        String extractedUsername = tokenProvider.getUsernameFromToken(adminToken);
        logger.info("  Extracted username: {}", extractedUsername);

        // Generate token for different user
        String repToken = tokenProvider.generateToken("agarcia");
        logger.info("");
        logger.info("Generated JWT for 'agarcia':");
        logger.info("  Token: {}...", repToken.substring(0, Math.min(50, repToken.length())));
        logger.info("  Extracted username: {}", tokenProvider.getUsernameFromToken(repToken));

        // Validate an invalid token
        logger.info("");
        logger.info("Validating an invalid token:");
        boolean invalidResult = tokenProvider.validateToken("this.is.not.a.valid.token");
        logger.info("  Result: {} (expected false)", invalidResult);
    }

    /**
     * Demonstrates role-based access control concepts.
     */
    private void demonstrateRoleBasedAccess() {
        logger.info("");
        logger.info("--- Role-Based Access Control (RBAC) ---");
        logger.info("");
        logger.info("URL-based security rules (from SecurityConfig):");
        logger.info("  /api/auth/**        → permitAll()   (anyone can login)");
        logger.info("  /api/health         → permitAll()   (public health check)");
        logger.info("  /api/admin/**       → SYSTEM_ADMIN  (admin only)");
        logger.info("  /api/users/**       → SYSTEM_ADMIN or SALES_MANAGER");
        logger.info("  /api/** (other)     → authenticated (any logged-in user)");
        logger.info("");
        logger.info("Method-level security (@PreAuthorize):");
        logger.info("  approveCalculation  → SALES_MANAGER, FINANCE_ADMIN, SYSTEM_ADMIN");
        logger.info("  markAsPaid          → FINANCE_ADMIN, SYSTEM_ADMIN");
        logger.info("  createUser          → SYSTEM_ADMIN");
        logger.info("  deleteUser          → SYSTEM_ADMIN");
        logger.info("");
        logger.info("Sample API calls:");
        logger.info("  1. POST /api/auth/login {username: 'admin', password: 'admin123'}");
        logger.info("     → Returns JWT token");
        logger.info("  2. GET /api/deals -H 'Authorization: Bearer <token>'");
        logger.info("     → Returns all deals (authenticated)");
        logger.info("  3. PATCH /api/calculations/calc-002/approve -H 'Authorization: Bearer <token>'");
        logger.info("     → Requires SALES_MANAGER or higher role");
    }
}
