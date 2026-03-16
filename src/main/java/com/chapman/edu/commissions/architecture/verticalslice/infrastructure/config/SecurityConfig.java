package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the application.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Allow public access to Swagger UI and API docs
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api-docs/**"
                ).permitAll()
                // Allow public access to H2 console
                .requestMatchers("/h2-console/**").permitAll()
                // Require authentication for all API endpoints (including MCP)
                .requestMatchers("/api/**").authenticated()
                // Allow all other requests
                .anyRequest().permitAll()
            )
            .httpBasic(basic -> {})
            // Disable CSRF for H2 console, API endpoints, and MCP server
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/h2-console/**",
                    "/api/**"
                ))
            // Allow frames for H2 console
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
