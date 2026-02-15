package com.chapman.edu.commissions.springboot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ============================================================================
 * JWT AUTHENTICATION FILTER — SECURITY FILTER CHAIN
 * ============================================================================
 *
 * CONCEPT: Security Filters
 * ---------------------------
 * Spring Security operates as a chain of servlet filters. Each HTTP request
 * passes through these filters in order. Our JwtAuthenticationFilter is a
 * custom filter that:
 *
 *   1. Extracts the JWT token from the Authorization header
 *   2. Validates the token signature and expiration
 *   3. Loads the user details from our UserDetailsService
 *   4. Sets the authentication in Spring's SecurityContext
 *
 * CONCEPT: OncePerRequestFilter
 * --------------------------------
 * OncePerRequestFilter guarantees that the filter is executed exactly once
 * per request. This prevents the filter from running multiple times if the
 * request is forwarded internally (e.g., error handling, request dispatching).
 *
 * CONCEPT: SecurityContext
 * --------------------------
 * The SecurityContext holds the currently authenticated user's information.
 * It is stored in a ThreadLocal variable, meaning each thread (HTTP request)
 * has its own SecurityContext.
 *
 * Setting the SecurityContext tells Spring Security:
 *   "This request is authenticated as user X with roles Y"
 *
 * After setting it, the authorization layer (@PreAuthorize, URL rules) can
 * check if the user has the required roles/permissions.
 *
 * @see org.springframework.web.filter.OncePerRequestFilter
 * @see org.springframework.security.core.context.SecurityContextHolder
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * This method is called for every HTTP request. It checks for a JWT token
     * and authenticates the request if a valid token is found.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Step 1: Extract JWT from the Authorization header
            String jwt = extractJwtFromRequest(request);

            // Step 2: Validate the token
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {

                // Step 3: Extract username from token
                String username = tokenProvider.getUsernameFromToken(jwt);

                // Step 4: Load user details (roles, permissions)
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Step 5: Create an Authentication object
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,           // Principal (the user)
                        null,                  // Credentials (not needed after authentication)
                        userDetails.getAuthorities()  // Granted authorities (roles)
                    );

                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Step 6: Set the authentication in the SecurityContext
                // This tells Spring Security "this request is authenticated"
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("Authenticated user '{}' via JWT", username);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context: {}", ex.getMessage());
        }

        // Continue the filter chain (pass to next filter)
        filterChain.doFilter(request, response);
    }

    /**
     * Extract the JWT token from the HTTP request's Authorization header.
     *
     * Expected format: "Bearer eyJhbGciOiJIUzI1NiJ9..."
     * We strip the "Bearer " prefix and return the raw token.
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
