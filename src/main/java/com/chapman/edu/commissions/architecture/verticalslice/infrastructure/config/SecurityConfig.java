package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.config;

import com.chapman.edu.commissions.architecture.verticalslice.features.authentication.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for the verticalslice application.
 *
 * Three auth paths are accepted:
 *   1. Bearer JWT issued by /api/auth/login or /api/register (browser flow)
 *   2. HTTP Basic (kept for the Selenium E2E test suite and external MCP
 *      clients that already authenticate as admin:admin123)
 *   3. Public endpoints — auth, register, subscription packages, MCP, docs
 *
 * The global authentication entry point is {@link HttpStatusEntryPoint} so a
 * 401 response never includes a {@code WWW-Authenticate: Basic} header,
 * which stops browsers from popping the native Sign In credential prompt
 * for failed fetch calls. BasicAuthenticationFilter still processes valid
 * Basic headers when they ARE sent (e.g. by curl / Selenium tests).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://*.ngrok-free.app",
            "https://*.ngrok-free.dev",
            "https://*.ngrok.io",
            "https://*.ngrok.app",
            "https://*.ngrok.dev",
            // Claude.ai browser artifact / iframe origins — lets the in-chat
            // wizard artifact call /api/** through the ngrok tunnel
            "https://claude.ai",
            "https://*.claude.ai",
            "https://*.claudeusercontent.com",
            "https://*.anthropic.com"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        // Spring AI's streamable-HTTP MCP transport returns an Mcp-Session-Id
        // header on initialize; the browser won't expose custom response
        // headers to JS unless we opt them in here. The claude.ai wizard
        // reads this value to thread it through subsequent tools/call posts.
        configuration.setExposedHeaders(List.of(
            "Mcp-Session-Id",
            "mcp-session-id",
            "Location",
            "Content-Disposition"
        ));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        // DelegatingPasswordEncoder understands the {bcrypt}/{noop}/etc prefix.
        // New passwords from our registration flow are encoded with BCrypt and
        // stored as "{bcrypt}$2a$10$…". Seed SQL uses an explicit {bcrypt}
        // prefix. But Spring Boot's auto-configured in-memory admin user
        // (admin/admin123, see spring.security.user.*) is stored WITHOUT a
        // prefix whenever a PasswordEncoder bean is present — so we set a
        // NoOp fallback for unprefixed values so that Basic-auth flow (used
        // by Selenium E2E tests and MCP Inspector) still authenticates cleanly
        // instead of throwing IllegalArgumentException on match.
        DelegatingPasswordEncoder encoder =
            (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
        encoder.setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder.getInstance());
        return encoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Allow public access to Swagger UI and API docs
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api-docs/**"
                ).permitAll()
                // Allow public access to H2 console and Togglz console
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/togglz-console/**").permitAll()
                // Allow public access to MCP endpoints (for MCP Inspector and clients)
                .requestMatchers("/api/mcp/**").permitAll()
                // Allow public access to Spring AI auto-configured Streamable HTTP MCP endpoint
                .requestMatchers("/mcp/**").permitAll()
                // Allow public access to A2A agent discovery + JSON-RPC endpoint
                .requestMatchers("/.well-known/agent-card.json").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/").permitAll()
                // Public authentication + registration endpoints (self-service signup + login)
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/register/**").permitAll()
                .requestMatchers("/api/subscription-packages/**").permitAll()
                // Require authentication for all other API endpoints
                .requestMatchers("/api/**").authenticated()
                // Allow all other requests
                .anyRequest().permitAll()
            )
            // 401 with no WWW-Authenticate so browsers never show their native Basic prompt
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            // Keep HTTP Basic available for Selenium / curl / MCP Inspector — the
            // global entry point above ensures no WWW-Authenticate header leaks
            // back to browsers, so the popup stays suppressed.
            .httpBasic(basic -> basic.authenticationEntryPoint(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // Disable CSRF for H2 console, API endpoints, and MCP server
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/h2-console/**",
                    "/api/**",
                    "/mcp/**",
                    "/togglz-console/**",
                    "/",
                    "/a2a-client/**"
                ))
            // Allow frames for H2 console
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
