package com.chapman.edu.commissions.springboot.controller;

import com.chapman.edu.commissions.springboot.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple health check endpoint (public, no auth required).
 * Useful for load balancers, monitoring, and deployment verification.
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Health check — verify the application is running")
public class HealthController {

    @Operation(summary = "Health check", description = "Returns application health status, Java version, and current timestamp. No authentication required.")
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("application", "Commission Calculator - Spring Boot Module");
        healthInfo.put("timestamp", LocalDateTime.now().toString());
        healthInfo.put("javaVersion", System.getProperty("java.version"));

        return ResponseEntity.ok(ApiResponse.success("Application is running", healthInfo));
    }
}
