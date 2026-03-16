package com.chapman.edu.commissions.architecture.orthogonal.features.plans.queries;

import com.chapman.edu.commissions.architecture.orthogonal.features.plans.CommissionPlanResponse;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Query;

public record GetPlanQuery(String id) implements Query<CommissionPlanResponse> {}
