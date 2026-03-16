package com.chapman.edu.commissions.architecture.microservice.common.dto;

public record ResolveDisputeRequest(String resolution, String resolvedBy, boolean approved) {
    public void validate() {
        if (resolution == null || resolution.isBlank()) throw new IllegalArgumentException("Resolution is required");
        if (resolvedBy == null || resolvedBy.isBlank()) throw new IllegalArgumentException("Resolved by is required");
    }
}
