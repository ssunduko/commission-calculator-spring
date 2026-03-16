package com.chapman.edu.commissions.architecture.orthogonal.features.plans;

import com.chapman.edu.commissions.architecture.orthogonal.domain.CommissionPlan;
import com.chapman.edu.commissions.architecture.orthogonal.domain.PlanStatus;

import java.time.LocalDate;

/**
 * Response DTO for commission plan information.
 */
public record CommissionPlanResponse(
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
    public static CommissionPlanResponse from(CommissionPlan plan) {
        return new CommissionPlanResponse(
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
