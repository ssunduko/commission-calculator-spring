package com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto;

import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.DomainException;

import java.time.LocalDate;
import java.util.Currency;

/**
 * Command DTO for creating a new commission plan.
 */
public record CreatePlanCommand(
        String name,
        String currencyCode,
        LocalDate effectiveStartDate,
        LocalDate effectiveEndDate
) {

    public void validate() {
        if (name == null || name.isBlank()) {
            throw new DomainException("Plan name must not be blank");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new DomainException("Currency code must not be blank");
        }
        try {
            Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid currency code: " + currencyCode);
        }
    }
}
