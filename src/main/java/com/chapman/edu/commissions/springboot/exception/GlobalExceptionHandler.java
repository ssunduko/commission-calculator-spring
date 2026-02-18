package com.chapman.edu.commissions.springboot.exception;

import com.chapman.edu.commissions.springboot.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * GLOBAL EXCEPTION HANDLER — @ControllerAdvice
 * ============================================================================
 *
 * CONCEPT: @ControllerAdvice
 * ----------------------------
 * @ControllerAdvice is a specialization of @Component that allows you to handle
 * exceptions across ALL controllers in one centralized place. Without it, each
 * controller would need its own try-catch blocks.
 *
 * Benefits:
 *   - Centralized error handling for the entire application
 *   - Consistent error response format across all endpoints
 *   - Separation of error handling logic from business logic
 *   - Ability to handle framework exceptions (validation, type mismatch, etc.)
 *
 * CONCEPT: @ExceptionHandler
 * ----------------------------
 * The @ExceptionHandler annotation marks a method that handles a specific
 * exception type. When that exception is thrown anywhere in a controller,
 * Spring routes it to the matching handler method.
 *
 * You can specify multiple exception types:
 *   @ExceptionHandler({IOException.class, TimeoutException.class})
 *
 * Handler methods can return:
 *   - ResponseEntity<T> — Full control over status code, headers, and body
 *   - Any object — Serialized as the response body with 200 OK
 *   - String — A view name (for Thymeleaf rendering)
 *
 * CONCEPT: Exception Handling Best Practices
 * --------------------------------------------
 *   1. Always return consistent error response structures (ApiErrorResponse)
 *   2. Log errors server-side for debugging, but don't expose internals to clients
 *   3. Map custom exceptions to appropriate HTTP status codes:
 *      - 400 Bad Request — Validation errors
 *      - 401 Unauthorized — Authentication failures
 *      - 403 Forbidden — Authorization failures
 *      - 404 Not Found — Resource not found
 *      - 422 Unprocessable Entity — Business rule violations
 *      - 500 Internal Server Error — Unexpected errors
 *   4. Include field-level errors for validation failures
 *   5. Include the request path for debugging
 *
 * @see org.springframework.web.bind.annotation.ControllerAdvice
 * @see org.springframework.web.bind.annotation.ExceptionHandler
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles Bean Validation failures (when @Valid fails on a request body).
     *
     * CONCEPT: MethodArgumentNotValidException
     * ------------------------------------------
     * This exception is thrown automatically by Spring when a @RequestBody
     * annotated with @Valid fails validation. It contains a BindingResult
     * with all field-level validation errors.
     *
     * The flow:
     *   1. Client sends: POST /api/deals with body {"title": "", "value": -5}
     *   2. Spring validates @Valid CreateDealRequest
     *   3. @NotBlank on title fails, @DecimalMin on value fails
     *   4. MethodArgumentNotValidException is thrown with both errors
     *   5. This handler catches it and formats the errors
     *   6. Client receives 400 with field-level error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Collect all field-level validation errors
        Map<String, List<String>> validationErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors
                .computeIfAbsent(fieldError.getField(), key -> new ArrayList<>())
                .add(fieldError.getDefaultMessage());
        }

        ApiErrorResponse error = new ApiErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "Validation failed for one or more fields",
            request.getRequestURI()
        );
        error.setValidationErrors(validationErrors);

        logger.warn("Validation failed on {}: {}", request.getRequestURI(), validationErrors);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles ResourceNotFoundException — resource not found in repository.
     * Returns HTTP 404 (Not Found).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getRequestURI()
        );

        logger.warn("Resource not found: {}", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles BusinessValidationException — business rule violation.
     * Returns HTTP 422 (Unprocessable Entity).
     */
    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessValidation(
            BusinessValidationException ex,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "Unprocessable Entity",
            ex.getMessage(),
            request.getRequestURI()
        );

        logger.warn("Business validation failed: {}", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Handles UnauthorizedException — authentication/authorization failure.
     * Returns HTTP 401 (Unauthorized).
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            ex.getMessage(),
            request.getRequestURI()
        );

        logger.warn("Unauthorized access attempt: {}", ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles Spring Security AuthenticationException — bad credentials, locked account, etc.
     * Returns HTTP 401 (Unauthorized).
     *
     * This catches BadCredentialsException (wrong password), DisabledException (inactive user),
     * and other AuthenticationException subclasses thrown by AuthenticationManager.authenticate().
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            "Invalid username or password",
            request.getRequestURI()
        );

        logger.warn("Authentication failed on {}: {}", request.getRequestURI(), ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles AccessDeniedException — authorization failure from @PreAuthorize.
     * Returns HTTP 403 (Forbidden).
     *
     * IMPORTANT: This handler must be defined BEFORE the generic Exception handler.
     * Without it, AccessDeniedException would be caught by the catch-all handler
     * and returned as a 500 Internal Server Error instead of 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "Forbidden",
            "You do not have permission to perform this action",
            request.getRequestURI()
        );

        logger.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    /**
     * Catches all unhandled exceptions — the safety net.
     * Returns HTTP 500 (Internal Server Error).
     *
     * IMPORTANT: We log the full stack trace server-side but only return
     * a generic message to the client. Never expose internal details
     * (stack traces, SQL queries, etc.) in API responses — this is a
     * security best practice.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred. Please try again later.",
            request.getRequestURI()
        );

        // Log the full exception for server-side debugging
        logger.error("Unhandled exception on {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
