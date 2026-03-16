package com.chapman.edu.commissions.architecture.orthogonal.features.plans.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.domain.CommissionPlan;
import com.chapman.edu.commissions.architecture.orthogonal.domain.CommissionRule;
import com.chapman.edu.commissions.architecture.orthogonal.domain.RuleType;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.CommissionPlanRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.CommissionPlanResponse;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.commands.AddRuleToPlanCommand;
import com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions.ValidationException;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class AddRuleToPlanHandler implements CommandHandler<AddRuleToPlanCommand, CommissionPlanResponse> {
    private final CommissionPlanRepository planRepository;
    public AddRuleToPlanHandler(CommissionPlanRepository planRepository) { this.planRepository = planRepository; }

    @Override
    public CommissionPlanResponse handle(AddRuleToPlanCommand command) {
        CommissionPlan plan = planRepository.findById(command.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Commission Plan", command.planId()));
        CommissionRule rule = new CommissionRule();
        rule.setName(command.name());
        rule.setDescription(command.description());
        rule.setRate(command.rate());
        rule.setPriority(command.priority());
        try {
            if (command.ruleType() != null) rule.setType(RuleType.valueOf(command.ruleType().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid rule type: " + command.ruleType());
        }
        plan.addRule(rule);
        CommissionPlan updated = planRepository.save(plan);
        return CommissionPlanResponse.from(updated);
    }
}
