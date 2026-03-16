package com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto;

import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.DomainException;

/**
 * Command DTO for calculating commission on a deal.
 */
public record CalculateCommissionCommand(String dealId, String planId) {

    public void validate() {
        if (dealId == null || dealId.isBlank()) {
            throw new DomainException("Deal ID must not be blank");
        }
        if (planId == null || planId.isBlank()) {
            throw new DomainException("Plan ID must not be blank");
        }
    }
}
