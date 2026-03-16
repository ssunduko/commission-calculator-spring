package com.chapman.edu.commissions.architecture.orthogonal.features.plans.commands;

import com.chapman.edu.commissions.architecture.orthogonal.features.plans.CommissionPlanResponse;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Command;

public record ActivatePlanCommand(String planId) implements Command<CommissionPlanResponse> {}
