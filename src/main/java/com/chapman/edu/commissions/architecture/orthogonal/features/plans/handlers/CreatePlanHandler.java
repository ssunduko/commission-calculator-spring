package com.chapman.edu.commissions.architecture.orthogonal.features.plans.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.domain.CommissionPlan;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.CommissionPlanRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.CommissionPlanResponse;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.commands.CreatePlanCommand;
import com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions.ValidationException;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.CommandHandler;
import org.springframework.stereotype.Component;
import java.util.Currency;

@Component
public class CreatePlanHandler implements CommandHandler<CreatePlanCommand, CommissionPlanResponse> {
    private final CommissionPlanRepository planRepository;
    public CreatePlanHandler(CommissionPlanRepository planRepository) { this.planRepository = planRepository; }

    @Override
    public CommissionPlanResponse handle(CreatePlanCommand command) {
        try {
            Currency currency = Currency.getInstance(command.currencyCode());
            CommissionPlan plan = new CommissionPlan(command.name(), currency);
            plan.setEffectiveStartDate(command.effectiveStartDate());
            plan.setEffectiveEndDate(command.effectiveEndDate());
            CommissionPlan saved = planRepository.save(plan);
            return CommissionPlanResponse.from(saved);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid currency code: " + command.currencyCode());
        }
    }
}
