package com.chapman.edu.commissions.architecture.orthogonal.features.plans.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.features.plans.CommissionPlanRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.CommissionPlanResponse;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.queries.GetAllPlansQuery;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.QueryHandler;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetAllPlansHandler implements QueryHandler<GetAllPlansQuery, List<CommissionPlanResponse>> {
    private final CommissionPlanRepository planRepository;
    public GetAllPlansHandler(CommissionPlanRepository planRepository) { this.planRepository = planRepository; }

    @Override
    public List<CommissionPlanResponse> handle(GetAllPlansQuery query) {
        if (query.status() != null) {
            return planRepository.findByStatus(query.status()).stream().map(CommissionPlanResponse::from).collect(Collectors.toList());
        }
        return planRepository.findAll().stream().map(CommissionPlanResponse::from).collect(Collectors.toList());
    }
}
