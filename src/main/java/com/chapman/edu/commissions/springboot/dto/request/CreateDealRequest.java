package com.chapman.edu.commissions.springboot.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * ============================================================================
 * DATA TRANSFER OBJECT (DTO) — CREATE DEAL REQUEST
 * ============================================================================
 *
 * CONCEPT: DTOs and Bean Validation
 * -----------------------------------
 * A DTO (Data Transfer Object) carries data between processes. In REST APIs,
 * DTOs define the shape of request/response JSON bodies, decoupling the API
 * contract from internal domain models.
 *
 * CONCEPT: Bean Validation (JSR 380)
 * -------------------------------------
 * Bean Validation provides a declarative way to validate data using annotations.
 * Spring Boot integrates it with the spring-boot-starter-validation dependency.
 *
 * Key validation annotations:
 *   @NotNull   — Field must not be null
 *   @NotBlank  — String must not be null, empty, or whitespace
 *   @Size      — String/collection size must be within bounds
 *   @Min/@Max  — Numeric value must be >= min or <= max
 *   @DecimalMin/@DecimalMax — For BigDecimal/double validation
 *   @Email     — Must be a valid email format
 *   @Pattern   — Must match a regex pattern
 *   @Valid     — Triggers cascaded validation on nested objects
 *   @Past/@Future — Date must be in the past/future
 *
 * HOW VALIDATION WORKS:
 *   1. Add validation annotations to DTO fields (like below)
 *   2. Add @Valid to the controller method parameter:
 *        public ResponseEntity<?> createDeal(@Valid @RequestBody CreateDealRequest request)
 *   3. Spring automatically validates before the method body executes
 *   4. If validation fails, a MethodArgumentNotValidException is thrown
 *   5. Our GlobalExceptionHandler catches it and returns a structured error response
 *
 * @see jakarta.validation.constraints
 */
public class CreateDealRequest {

    @NotBlank(message = "Deal title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @NotNull(message = "Deal value is required")
    @DecimalMin(value = "0.01", message = "Deal value must be greater than zero")
    private BigDecimal value;

    @NotBlank(message = "Sales representative ID is required")
    private String salesRepId;

    // --- Getters and Setters ---

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getSalesRepId() {
        return salesRepId;
    }

    public void setSalesRepId(String salesRepId) {
        this.salesRepId = salesRepId;
    }
}
