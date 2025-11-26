package com.chapman.edu.commissions.verticalslice.features.calculations;

import com.chapman.edu.commissions.verticalslice.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.verticalslice.features.deals.DealRepository;
import com.chapman.edu.commissions.verticalslice.features.plans.CommissionPlanRepository;
import com.chapman.edu.commissions.verticalslice.domain.CommissionCalculation;
import com.chapman.edu.commissions.verticalslice.domain.CommissionPlan;
import com.chapman.edu.commissions.verticalslice.domain.CommissionRule;
import com.chapman.edu.commissions.verticalslice.domain.Deal;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for calculating commissions.
 * Contains business logic for commission calculation operations.
 * Exposed as MCP tools for AI agent access.
 */
@Service
public class CommissionCalculationService {
    private final CommissionCalculationRepository calculationRepository;
    private final DealRepository dealRepository;
    private final CommissionPlanRepository planRepository;

    public CommissionCalculationService(
        CommissionCalculationRepository calculationRepository,
        DealRepository dealRepository,
        CommissionPlanRepository planRepository
    ) {
        this.calculationRepository = calculationRepository;
        this.dealRepository = dealRepository;
        this.planRepository = planRepository;
    }

    public CommissionCalculationResponse calculateCommission(CalculateCommissionRequest request) {
        request.validate();

        Deal deal = dealRepository.findById(request.dealId())
            .orElseThrow(() -> new ResourceNotFoundException("Deal", request.dealId()));

        CommissionPlan plan = planRepository.findById(request.planId())
            .orElseThrow(() -> new ResourceNotFoundException("Commission Plan", request.planId()));

        BigDecimal baseCommission = calculateBaseCommission(deal, plan);

        CommissionCalculation calculation = new CommissionCalculation(
            deal.getId(),
            deal.getSalesRepId(),
            baseCommission
        );
        calculation.setPlanId(plan.getId());
        calculation.recalculate();

        CommissionCalculation savedCalculation = calculationRepository.save(calculation);
        return CommissionCalculationResponse.from(savedCalculation);
    }

    private BigDecimal calculateBaseCommission(Deal deal, CommissionPlan plan) {
        if (plan.getRules().isEmpty()) {
            return BigDecimal.ZERO;
        }

        CommissionRule rule = plan.getRules().get(0);
        BigDecimal dealValue = deal.getValue() != null ? deal.getValue() : BigDecimal.ZERO;

        return dealValue
            .multiply(rule.getRate())
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public CommissionCalculationResponse getCalculation(String id) {
        CommissionCalculation calculation = calculationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Commission Calculation", id));
        return CommissionCalculationResponse.from(calculation);
    }

    public List<CommissionCalculationResponse> getAllCalculations() {
        return calculationRepository.findAll().stream()
            .map(CommissionCalculationResponse::from)
            .collect(Collectors.toList());
    }

    public List<CommissionCalculationResponse> getCalculationsByDeal(String dealId) {
        return calculationRepository.findByDealId(dealId).stream()
            .map(CommissionCalculationResponse::from)
            .collect(Collectors.toList());
    }

    public List<CommissionCalculationResponse> getCalculationsBySalesRep(String salesRepId) {
        return calculationRepository.findBySalesRepId(salesRepId).stream()
            .map(CommissionCalculationResponse::from)
            .collect(Collectors.toList());
    }
}
