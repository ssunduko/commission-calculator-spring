package com.chapman.edu.commissions.architecture.ddd.application.dto;

import com.chapman.edu.commissions.architecture.ddd.domain.plan.CommissionPlan;
import com.chapman.edu.commissions.architecture.ddd.domain.plan.PlanStatus;

import java.time.LocalDate;

/**
 * Response DTO for commission plan information.
 */
public record CommissionPlanDto(
    String id,
    String name,
    String currency,
    PlanStatus status,
    LocalDate effectiveStartDate,
    LocalDate effectiveEndDate,
    LocalDate createdDate,
    int rulesCount,
    int tiersCount
) {
    public static CommissionPlanDto fromEntity(CommissionPlan plan) {
        return new CommissionPlanDto(
            plan.getId(),
            plan.getName(),
            plan.getCurrency() != null ? plan.getCurrency().getCurrencyCode() : null,
            plan.getStatus(),
            plan.getEffectiveStartDate(),
            plan.getEffectiveEndDate(),
            plan.getCreatedDate(),
            plan.getRules() != null ? plan.getRules().size() : 0,
            plan.getTiers() != null ? plan.getTiers().size() : 0
        );
    }
}
