package com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto;

import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.DomainException;

import java.math.BigDecimal;

/**
 * Command DTO for creating a new deal.
 */
public record CreateDealCommand(String title, BigDecimal value, String salesRepId) {

    public void validate() {
        if (title == null || title.isBlank()) {
            throw new DomainException("Deal title must not be blank");
        }
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Deal value must be greater than zero");
        }
        if (salesRepId == null || salesRepId.isBlank()) {
            throw new DomainException("Sales rep ID must not be blank");
        }
    }
}
