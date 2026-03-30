package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for the application.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
                // Allow public access to MCP endpoints (for MCP Inspector and clients)
                .requestMatchers("/api/mcp/**").permitAll()
                // Allow public access to Spring AI auto-configured Streamable HTTP MCP endpoint
                .requestMatchers("/mcp/**").permitAll()
                // Require authentication for all other API endpoints
                .requestMatchers("/api/**").authenticated()
                // Allow all other requests
                .anyRequest().permitAll()
            )
            .httpBasic(basic -> {})
            // Disable CSRF for H2 console, API endpoints, and MCP server
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/h2-console/**",
                    "/api/**",
                    "/mcp/**"
                ))
            // Allow frames for H2 console
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
