package com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto;

import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.DomainException;

/**
 * Command DTO for creating a new dispute.
 */
public record CreateDisputeCommand(
        String calculationId,
        String salesRepId,
        String title,
        String description
) {

    public void validate() {
        if (calculationId == null || calculationId.isBlank()) {
            throw new DomainException("Calculation ID must not be blank");
        }
        if (salesRepId == null || salesRepId.isBlank()) {
            throw new DomainException("Sales rep ID must not be blank");
        }
        if (title == null || title.isBlank()) {
            throw new DomainException("Dispute title must not be blank");
        }
        if (description == null || description.isBlank()) {
            throw new DomainException("Dispute description must not be blank");
        }
    }
}
