package com.chapman.edu.commissions.architecture.orthogonal.features.plans.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.domain.CommissionPlan;
import com.chapman.edu.commissions.architecture.orthogonal.domain.PlanStatus;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.CommissionPlanRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.CommissionPlanResponse;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.commands.ActivatePlanCommand;
import com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class ActivatePlanHandler implements CommandHandler<ActivatePlanCommand, CommissionPlanResponse> {
    private final CommissionPlanRepository planRepository;
    public ActivatePlanHandler(CommissionPlanRepository planRepository) { this.planRepository = planRepository; }

    @Override
    public CommissionPlanResponse handle(ActivatePlanCommand command) {
        CommissionPlan plan = planRepository.findById(command.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Commission Plan", command.planId()));
        plan.setStatus(PlanStatus.ACTIVE);
        CommissionPlan updated = planRepository.save(plan);
        return CommissionPlanResponse.from(updated);
    }
}
