package com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto;

import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionPlan;

import java.time.LocalDate;

/**
 * Result DTO representing a commission plan.
 */
public record PlanResult(
        String id,
        String name,
        String currency,
        String status,
        LocalDate effectiveStartDate,
        LocalDate effectiveEndDate,
        LocalDate createdDate,
        int rulesCount,
        int tiersCount
) {

    public static PlanResult from(CommissionPlan plan) {
        return new PlanResult(
                plan.getId(),
                plan.getName(),
                plan.getCurrency() != null ? plan.getCurrency().getCurrencyCode() : null,
                plan.getStatus() != null ? plan.getStatus().name() : null,
                plan.getEffectiveStartDate(),
                plan.getEffectiveEndDate(),
                plan.getCreatedDate(),
                plan.getRules() != null ? plan.getRules().size() : 0,
                plan.getTiers() != null ? plan.getTiers().size() : 0
        );
    }
}
