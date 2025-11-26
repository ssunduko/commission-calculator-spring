package com.chapman.edu.commissions.verticalslice.features.plans;

import java.time.LocalDate;

/**
 * Request DTO for creating a new commission plan.
 */
public record CreateCommissionPlanRequest(
    String name,
    String currencyCode,
    LocalDate effectiveStartDate,
    LocalDate effectiveEndDate
) {
    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Plan name is required");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new IllegalArgumentException("Currency code is required");
        }
    }
}
