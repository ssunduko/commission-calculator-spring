package com.chapman.edu.commissions.architecture.eventdriven.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Event-Driven module.
 * Permits all requests under /api/events/** for simplified testing.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain eventDrivenSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/events/**", "/h2-console/**", "/swagger-ui/**", "/api-docs/**")
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
