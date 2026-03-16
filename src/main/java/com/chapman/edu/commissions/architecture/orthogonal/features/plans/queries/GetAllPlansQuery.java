package com.chapman.edu.commissions.architecture.orthogonal.features.plans.queries;

import com.chapman.edu.commissions.architecture.orthogonal.domain.PlanStatus;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.CommissionPlanResponse;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Query;
import java.util.List;

public record GetAllPlansQuery(PlanStatus status) implements Query<List<CommissionPlanResponse>> {
    public static GetAllPlansQuery all() { return new GetAllPlansQuery(null); }
    public static GetAllPlansQuery byStatus(PlanStatus status) { return new GetAllPlansQuery(status); }
}
