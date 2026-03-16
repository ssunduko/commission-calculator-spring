package com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto;

import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.DomainException;

/**
 * Command DTO for resolving a dispute.
 */
public record ResolveDisputeCommand(String resolution, String resolvedBy, boolean approved) {

    public void validate() {
        if (resolution == null || resolution.isBlank()) {
            throw new DomainException("Resolution must not be blank");
        }
        if (resolvedBy == null || resolvedBy.isBlank()) {
            throw new DomainException("Resolved by must not be blank");
        }
    }
}
