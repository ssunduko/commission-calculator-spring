package com.chapman.edu.commissions.architecture.orthogonal.features.plans.commands;

import com.chapman.edu.commissions.architecture.orthogonal.features.plans.CommissionPlanResponse;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Command;
import java.time.LocalDate;

public record CreatePlanCommand(
    String name,
    String currencyCode,
    LocalDate effectiveStartDate,
    LocalDate effectiveEndDate
) implements Command<CommissionPlanResponse> {
    public void validate() {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Plan name is required");
        if (currencyCode == null || currencyCode.isBlank()) throw new IllegalArgumentException("Currency code is required");
    }
}
