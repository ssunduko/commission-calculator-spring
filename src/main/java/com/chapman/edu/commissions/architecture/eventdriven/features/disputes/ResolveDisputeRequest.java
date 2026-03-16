package com.chapman.edu.commissions.architecture.eventdriven.features.disputes;

/**
 * Request DTO for resolving a dispute.
 */
public record ResolveDisputeRequest(
    String resolution,
    String resolvedBy,
    boolean approved
) {
    public void validate() {
        if (resolution == null || resolution.isBlank()) {
            throw new IllegalArgumentException("Resolution is required");
        }
        if (resolvedBy == null || resolvedBy.isBlank()) {
            throw new IllegalArgumentException("Resolver ID is required");
        }
    }
}
