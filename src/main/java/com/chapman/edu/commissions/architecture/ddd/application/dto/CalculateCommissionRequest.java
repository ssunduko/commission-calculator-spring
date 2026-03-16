package com.chapman.edu.commissions.architecture.ddd.application.dto;

/**
 * Request DTO for calculating commission on a deal.
 */
public record CalculateCommissionRequest(
    String dealId,
    String planId
) {
    public void validate() {
        if (dealId == null || dealId.isBlank()) {
            throw new IllegalArgumentException("Deal ID is required");
        }
        if (planId == null || planId.isBlank()) {
            throw new IllegalArgumentException("Plan ID is required");
        }
    }
}
