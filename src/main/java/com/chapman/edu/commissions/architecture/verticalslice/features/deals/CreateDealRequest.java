package com.chapman.edu.commissions.architecture.verticalslice.features.deals;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new deal.
 */
public record CreateDealRequest(
    String title,
    BigDecimal value,
    String salesRepId
) {
    public void validate() {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Deal title is required");
        }
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deal value must be greater than zero");
        }
        if (salesRepId == null || salesRepId.isBlank()) {
            throw new IllegalArgumentException("Sales rep ID is required");
        }
    }
}
