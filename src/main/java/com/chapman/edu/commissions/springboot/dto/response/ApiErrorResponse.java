package com.chapman.edu.commissions.springboot.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * API ERROR RESPONSE — STRUCTURED ERROR REPORTING
 * ============================================================================
 *
 * CONCEPT: Custom Error Responses
 * ----------------------------------
 * Spring Boot's default error response (the "Whitelabel Error Page" for web,
 * or the default JSON error body) is not informative enough for API clients.
 *
 * A custom error response provides:
 *   - HTTP status code and name
 *   - Human-readable error message
 *   - Field-level validation errors (for 400 Bad Request)
 *   - Request path that caused the error
 *   - Timestamp for debugging and log correlation
 *
 * Example error response:
 *   {
 *     "status": 400,
 *     "error": "Bad Request",
 *     "message": "Validation failed",
 *     "validationErrors": {
 *       "title": ["Title is required", "Title must be at least 3 characters"],
 *       "value": ["Value must be greater than zero"]
 *     },
 *     "path": "/api/deals",
 *     "timestamp": "2024-01-15T10:30:00"
 *   }
 *
 * This class works with @ControllerAdvice (GlobalExceptionHandler) to provide
 * consistent error responses across all API endpoints.
 */
public class ApiErrorResponse {

    private int status;
    private String error;
    private String message;
    private Map<String, List<String>> validationErrors;
    private String path;
    private LocalDateTime timestamp;

    public ApiErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiErrorResponse(int status, String error, String message, String path) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    // --- Getters and Setters ---

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, List<String>> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, List<String>> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
