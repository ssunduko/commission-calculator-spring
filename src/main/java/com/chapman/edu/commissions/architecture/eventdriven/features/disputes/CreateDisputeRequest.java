package com.chapman.edu.commissions.architecture.eventdriven.features.disputes;

/**
 * Request DTO for creating a new dispute.
 */
public record CreateDisputeRequest(
    String calculationId,
    String salesRepId,
    String title,
    String description
) {
    public void validate() {
        if (calculationId == null || calculationId.isBlank()) {
            throw new IllegalArgumentException("Calculation ID is required");
        }
        if (salesRepId == null || salesRepId.isBlank()) {
            throw new IllegalArgumentException("Sales rep ID is required");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Dispute title is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Dispute description is required");
        }
    }
}
