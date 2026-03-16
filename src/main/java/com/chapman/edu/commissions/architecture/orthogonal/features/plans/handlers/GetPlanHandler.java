package com.chapman.edu.commissions.architecture.orthogonal.features.plans.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.domain.CommissionPlan;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.CommissionPlanRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.CommissionPlanResponse;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.queries.GetPlanQuery;
import com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetPlanHandler implements QueryHandler<GetPlanQuery, CommissionPlanResponse> {
    private final CommissionPlanRepository planRepository;
    public GetPlanHandler(CommissionPlanRepository planRepository) { this.planRepository = planRepository; }

    @Override
    public CommissionPlanResponse handle(GetPlanQuery query) {
        CommissionPlan plan = planRepository.findById(query.id())
                .orElseThrow(() -> new ResourceNotFoundException("Commission Plan", query.id()));
        return CommissionPlanResponse.from(plan);
    }
}
