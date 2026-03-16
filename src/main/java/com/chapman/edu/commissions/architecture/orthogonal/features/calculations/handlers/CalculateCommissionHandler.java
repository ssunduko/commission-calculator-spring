package com.chapman.edu.commissions.architecture.orthogonal.features.calculations.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.domain.*;
import com.chapman.edu.commissions.architecture.orthogonal.features.calculations.CommissionCalculationRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.calculations.CommissionCalculationResponse;
import com.chapman.edu.commissions.architecture.orthogonal.features.calculations.commands.CalculateCommissionCommand;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.DealRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.plans.CommissionPlanRepository;
import com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.CommandHandler;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CalculateCommissionHandler implements CommandHandler<CalculateCommissionCommand, CommissionCalculationResponse> {
    private final CommissionCalculationRepository calculationRepository;
    private final DealRepository dealRepository;
    private final CommissionPlanRepository planRepository;

    public CalculateCommissionHandler(CommissionCalculationRepository calculationRepository,
                                       DealRepository dealRepository, CommissionPlanRepository planRepository) {
        this.calculationRepository = calculationRepository;
        this.dealRepository = dealRepository;
        this.planRepository = planRepository;
    }

    @Override
    public CommissionCalculationResponse handle(CalculateCommissionCommand command) {
        Deal deal = dealRepository.findById(command.dealId())
                .orElseThrow(() -> new ResourceNotFoundException("Deal", command.dealId()));
        CommissionPlan plan = planRepository.findById(command.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Commission Plan", command.planId()));

        BigDecimal baseCommission = BigDecimal.ZERO;
        if (!plan.getRules().isEmpty()) {
            CommissionRule rule = plan.getRules().get(0);
            BigDecimal dealValue = deal.getValue() != null ? deal.getValue() : BigDecimal.ZERO;
            baseCommission = dealValue.multiply(rule.getRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        CommissionCalculation calculation = new CommissionCalculation(deal.getId(), deal.getSalesRepId(), baseCommission);
        calculation.setPlanId(plan.getId());
        calculation.recalculate();
        CommissionCalculation saved = calculationRepository.save(calculation);
        return CommissionCalculationResponse.from(saved);
    }
}
