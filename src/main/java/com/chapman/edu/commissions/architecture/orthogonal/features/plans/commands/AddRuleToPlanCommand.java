package com.chapman.edu.commissions.architecture.orthogonal.features.plans.commands;

import com.chapman.edu.commissions.architecture.orthogonal.features.plans.CommissionPlanResponse;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Command;
import java.math.BigDecimal;

public record AddRuleToPlanCommand(
    String planId,
    String name,
    String description,
    BigDecimal rate,
    String ruleType,
    int priority
) implements Command<CommissionPlanResponse> {
    public void validate() {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Rule name is required");
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Rate must be non-negative");
    }
}
