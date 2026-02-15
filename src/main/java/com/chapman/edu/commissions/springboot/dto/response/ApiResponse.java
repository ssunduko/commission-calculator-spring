package com.chapman.edu.commissions.springboot.dto.response;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * GENERIC API RESPONSE WRAPPER
 * ============================================================================
 *
 * CONCEPT: Response Entities & Consistent API Design
 * ----------------------------------------------------
 * REST APIs should return consistent response structures. This generic wrapper
 * provides a uniform envelope for all API responses:
 *
 *   {
 *     "success": true,
 *     "message": "Deal created successfully",
 *     "data": { ... the actual payload ... },
 *     "timestamp": "2024-01-15T10:30:00"
 *   }
 *
 * Benefits:
 *   - Clients always know where to find the data
 *   - Error responses follow the same structure
 *   - Metadata (success flag, timestamp) aids debugging
 *   - Generic <T> allows type-safe payloads
 *
 * @param <T> the type of the data payload
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, String message, T data) {
        this();
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * Factory method for successful responses.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Factory method for error responses.
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    // --- Getters and Setters ---

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
