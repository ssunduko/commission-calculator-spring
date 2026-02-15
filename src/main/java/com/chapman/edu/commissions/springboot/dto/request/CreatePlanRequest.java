package com.chapman.edu.commissions.springboot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO for creating a new Commission Plan.
 *
 * CONCEPT: @NotBlank vs @NotNull
 * --------------------------------
 *   @NotNull — The value must not be null (works for any type)
 *   @NotBlank — The string must not be null AND must contain at least one
 *               non-whitespace character (only works for CharSequence)
 *   @NotEmpty — The string/collection must not be null AND must not be empty
 *
 * Use @NotBlank for String fields to catch both null and empty strings.
 * Use @NotNull for non-String types like dates, numbers, enums.
 */
public class CreatePlanRequest {

    @NotBlank(message = "Plan name is required")
    @Size(min = 3, max = 100, message = "Plan name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Currency code is required (e.g., USD)")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String currencyCode;

    @NotNull(message = "Effective start date is required")
    private LocalDate effectiveStartDate;

    private LocalDate effectiveEndDate;

    private String createdBy;

    // --- Getters and Setters ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public LocalDate getEffectiveStartDate() {
        return effectiveStartDate;
    }

    public void setEffectiveStartDate(LocalDate effectiveStartDate) {
        this.effectiveStartDate = effectiveStartDate;
    }

    public LocalDate getEffectiveEndDate() {
        return effectiveEndDate;
    }

    public void setEffectiveEndDate(LocalDate effectiveEndDate) {
        this.effectiveEndDate = effectiveEndDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
