package com.chapman.edu.commissions.springboot.config;

import com.chapman.edu.commissions.springboot.security.JwtAuthenticationFilter;
import com.chapman.edu.commissions.springboot.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;

/**
 * ============================================================================
 * SPRING SECURITY CONFIGURATION
 * ============================================================================
 *
 * CONCEPT: Spring Security Basics
 * --------------------------------
 * Spring Security provides comprehensive security services for Java applications.
 * Key concepts:
 *
 *   1. AUTHENTICATION — "Who are you?"
 *      Verifying the identity of a user. Spring Security supports:
 *      - Form-based login (for web apps)
 *      - HTTP Basic Authentication
 *      - JWT (JSON Web Tokens) for stateless APIs
 *      - OAuth2 / OpenID Connect
 *
 *   2. AUTHORIZATION — "What can you do?"
 *      Determining what an authenticated user is allowed to access.
 *      - URL-based security (e.g., /admin/** requires ADMIN role)
 *      - Method-level security (@PreAuthorize, @Secured)
 *      - Role-based access control (RBAC)
 *
 *   3. SECURITY FILTER CHAIN
 *      Spring Security operates as a chain of servlet filters. Each request
 *      passes through filters like:
 *      - SecurityContextPersistenceFilter — manages SecurityContext
 *      - UsernamePasswordAuthenticationFilter — processes form login
 *      - BasicAuthenticationFilter — processes HTTP Basic auth
 *      - FilterSecurityInterceptor — enforces authorization rules
 *
 *      We add our custom JwtAuthenticationFilter BEFORE the default
 *      UsernamePasswordAuthenticationFilter to intercept JWT tokens.
 *
 * CONCEPT: JWT (JSON Web Token) Integration
 * ------------------------------------------
 * JWT is a compact, URL-safe token format for stateless authentication:
 *   - Client sends credentials (username/password) to /api/auth/login
 *   - Server validates and returns a signed JWT
 *   - Client includes JWT in Authorization header: "Bearer <token>"
 *   - Server validates JWT on each request (no server-side session needed)
 *
 * CONCEPT: Role-Based Access Control (RBAC)
 * -------------------------------------------
 * Users are assigned roles (SALES_REP, SALES_MANAGER, FINANCE_ADMIN, SYSTEM_ADMIN).
 * Access rules map roles to allowed operations:
 *   - SALES_REP: View own deals, calculations, create disputes
 *   - SALES_MANAGER: Manage team deals, approve calculations
 *   - FINANCE_ADMIN: View all calculations, manage payouts
 *   - SYSTEM_ADMIN: Full access to all endpoints
 *
 * CONCEPT: @EnableWebSecurity
 * ----------------------------
 * Enables Spring Security's web security support and provides the Spring MVC
 * integration. It also extends WebSecurityConfigurerAdapter functionality
 * through the SecurityFilterChain bean approach (modern Spring Security 6.x).
 *
 * CONCEPT: @EnableMethodSecurity
 * -------------------------------
 * Enables method-level security annotations like @PreAuthorize("hasRole('ADMIN')")
 * on service or controller methods. This provides fine-grained access control
 * beyond URL-based rules.
 *
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 * @see org.springframework.security.web.SecurityFilterChain
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Enables @PreAuthorize, @PostAuthorize, @Secured annotations
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    /**
     * CONCEPT: Constructor Injection
     * --------------------------------
     * Spring automatically injects the JwtAuthenticationFilter and
     * CustomUserDetailsService beans. Constructor injection is preferred because:
     *   - Dependencies are clearly declared
     *   - Objects are always in a valid state (no null fields)
     *   - Easier to test (just pass mock dependencies)
     *   - Fields can be marked final (immutable)
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomUserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configures the Security Filter Chain for REST API endpoints (/api/**).
     *
     * CONCEPT: SecurityFilterChain
     * ------------------------------
     * In Spring Security 6.x, security configuration is done by defining
     * SecurityFilterChain beans. This replaces the older approach of extending
     * WebSecurityConfigurerAdapter.
     *
     * The filter chain defines:
     *   - Which URLs require authentication
     *   - Which roles can access which endpoints
     *   - Session management policy (stateless for APIs)
     *   - Custom filters (like our JWT filter)
     *   - CSRF protection settings
     */
    @Bean
    @Order(1)  // This filter chain takes priority for /api/** URLs
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // Apply this filter chain only to /api/** URLs
            .securityMatcher("/api/**")

            // CSRF: Disabled for REST APIs because:
            // 1. APIs use JWT tokens (not cookies), so CSRF attacks don't apply
            // 2. Each request carries the token in the Authorization header
            .csrf(csrf -> csrf.disable())

            // Authorization Rules — define who can access what
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no authentication required
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/health").permitAll()

                // Role-based access control for specific API paths
                // hasRole() automatically prepends "ROLE_" prefix
                .requestMatchers("/api/admin/**").hasRole("SYSTEM_ADMIN")
                .requestMatchers("/api/users/**").hasAnyRole("SYSTEM_ADMIN", "SALES_MANAGER")

                // All other /api/** endpoints require authentication
                .anyRequest().authenticated()
            )

            // Session Management — STATELESS means no HTTP session is created
            // Each request must carry its own authentication (JWT token)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Exception Handling — return 401 JSON instead of redirecting to login page
            // Without this, unauthenticated API requests get redirected to the
            // form login page, which returns HTML instead of a proper 401 response.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )

            // Add our custom JWT filter before the default authentication filter
            // This ensures JWT tokens are checked before Spring's default processing
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures the Security Filter Chain for web (Thymeleaf) pages.
     *
     * This separate chain handles browser-based access with form login,
     * while the API chain above handles REST API access with JWT.
     */
    @Bean
    @Order(2)  // Lower priority — handles non-API URLs
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public resources
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()

                // All web pages require authentication
                .anyRequest().authenticated()
            )

            // Form-based login for web pages
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/springboot/dashboard", true)
                .permitAll()
            )

            // Logout configuration
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )

            // Allow H2 console frames
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )

            // Disable CSRF for H2 console
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            );

        return http.build();
    }

    /**
     * CONCEPT: PasswordEncoder Bean
     * --------------------------------
     * BCrypt is the recommended password hashing algorithm. It:
     *   - Automatically generates a random salt
     *   - Is intentionally slow (configurable work factor)
     *   - Protects against rainbow table attacks
     *
     * Spring Security uses this bean to encode passwords during registration
     * and verify passwords during login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CONCEPT: AuthenticationManager
     * --------------------------------
     * The AuthenticationManager is the main strategy interface for authentication.
     * It processes Authentication requests. We expose it as a bean so our
     * AuthController can use it to authenticate login requests.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
