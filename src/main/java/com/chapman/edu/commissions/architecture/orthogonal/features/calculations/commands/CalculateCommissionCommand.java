package com.chapman.edu.commissions.architecture.orthogonal.features.calculations.commands;

import com.chapman.edu.commissions.architecture.orthogonal.features.calculations.CommissionCalculationResponse;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Command;

public record CalculateCommissionCommand(String dealId, String planId) implements Command<CommissionCalculationResponse> {
    public void validate() {
        if (dealId == null || dealId.isBlank()) throw new IllegalArgumentException("Deal ID is required");
        if (planId == null || planId.isBlank()) throw new IllegalArgumentException("Plan ID is required");
    }
}
