package com.chapman.edu.commissions.springboot.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for requesting a commission calculation.
 *
 * CONCEPT: Minimal DTOs
 * -----------------------
 * Request DTOs should contain only the fields needed for the operation.
 * The server derives additional information (calculated fields, timestamps,
 * etc.) rather than trusting client-provided values. This follows the
 * principle of least privilege and prevents clients from manipulating
 * server-side state.
 */
public class CalculateCommissionRequest {

    @NotBlank(message = "Deal ID is required")
    private String dealId;

    @NotBlank(message = "Plan ID is required")
    private String planId;

    @NotBlank(message = "Calculated by user ID is required")
    private String calculatedBy;

    // --- Getters and Setters ---

    public String getDealId() {
        return dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getCalculatedBy() {
        return calculatedBy;
    }

    public void setCalculatedBy(String calculatedBy) {
        this.calculatedBy = calculatedBy;
    }
}
