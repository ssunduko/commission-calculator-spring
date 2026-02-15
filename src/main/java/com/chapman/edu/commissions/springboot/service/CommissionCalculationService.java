package com.chapman.edu.commissions.springboot.service;

import com.chapman.edu.commissions.model.*;
import com.chapman.edu.commissions.springboot.dto.request.CalculateCommissionRequest;
import com.chapman.edu.commissions.springboot.exception.BusinessValidationException;
import com.chapman.edu.commissions.springboot.exception.ResourceNotFoundException;
import com.chapman.edu.commissions.springboot.repository.CommissionCalculationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Service layer for Commission Calculation business logic.
 *
 * Demonstrates service-layer orchestration: this service depends on multiple
 * other services (DealService, CommissionPlanService) to perform its work.
 * Spring injects all dependencies via constructor injection.
 */
@Service
public class CommissionCalculationService {

    private static final Logger logger = LoggerFactory.getLogger(CommissionCalculationService.class);

    private final CommissionCalculationRepository calculationRepository;
    private final DealService dealService;
    private final CommissionPlanService planService;

    /**
     * Multiple dependencies injected via constructor.
     * This is a common pattern where a service orchestrates work across
     * multiple other services and repositories.
     */
    public CommissionCalculationService(CommissionCalculationRepository calculationRepository,
                                        DealService dealService,
                                        CommissionPlanService planService) {
        this.calculationRepository = calculationRepository;
        this.dealService = dealService;
        this.planService = planService;
    }

    /**
     * Calculate commission for a deal using a specific plan.
     *
     * Business rules:
     *   - Deal must exist and have WON status
     *   - Plan must exist and be ACTIVE
     *   - Commission = deal value * plan's standard rate / 100
     *   - Bonuses are added based on plan's bonus rules
     */
    public CommissionCalculation calculateCommission(CalculateCommissionRequest request) {
        // Validate and fetch the deal
        Deal deal = dealService.getDealById(request.getDealId());
        if (deal.getStatus() != DealStatus.WON) {
            throw new BusinessValidationException(
                "Can only calculate commission for WON deals. Current status: " + deal.getStatus());
        }

        // Validate and fetch the plan
        CommissionPlan plan = planService.getPlanById(request.getPlanId());
        if (plan.getStatus() != PlanStatus.ACTIVE) {
            throw new BusinessValidationException(
                "Commission plan must be ACTIVE. Current status: " + plan.getStatus());
        }

        // Calculate base commission using plan's standard rule rate
        BigDecimal baseRate = plan.getRules().stream()
                .filter(r -> r.getType() == CommissionRule.RuleType.STANDARD)
                .findFirst()
                .map(CommissionRule::getRate)
                .orElse(new BigDecimal("10")); // Default 10%

        BigDecimal baseCommission = deal.getValue()
                .multiply(baseRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        // Create calculation
        CommissionCalculation calculation = new CommissionCalculation(
            deal.getId(), deal.getSalesRepId(), baseCommission);
        calculation.setPlanId(plan.getId());
        calculation.setCalculatedBy(request.getCalculatedBy());

        // Apply bonuses from plan
        BigDecimal totalBonuses = BigDecimal.ZERO;
        for (BonusRule bonus : plan.getBonuses()) {
            if (bonus.isActiveOn(LocalDate.now())) {
                BigDecimal bonusAmount = bonus.calculateBonus(baseCommission);
                calculation.addBonus(new BonusCalculation(
                    bonus.getId(), bonus.getName(), bonusAmount));
                totalBonuses = totalBonuses.add(bonusAmount);
            }
        }

        // Calculate gross and net
        calculation.setGrossCommission(baseCommission.add(totalBonuses));
        calculation.setNetCommission(calculation.getGrossCommission());

        CommissionCalculation saved = calculationRepository.save(calculation);
        logger.info("Calculated commission for deal {}: base={}, gross={}, net={}",
            deal.getId(), baseCommission, saved.getGrossCommission(), saved.getNetCommission());
        return saved;
    }

    public CommissionCalculation getCalculationById(String id) {
        return calculationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommissionCalculation", "id", id));
    }

    public List<CommissionCalculation> getAllCalculations() {
        return calculationRepository.findAll();
    }

    public List<CommissionCalculation> getCalculationsByDeal(String dealId) {
        return calculationRepository.findByDealId(dealId);
    }

    public List<CommissionCalculation> getCalculationsBySalesRep(String salesRepId) {
        return calculationRepository.findBySalesRepId(salesRepId);
    }

    /**
     * Approve a commission calculation.
     */
    public CommissionCalculation approveCalculation(String id) {
        CommissionCalculation calc = getCalculationById(id);

        if (calc.getStatus() != CommissionCalculation.CommissionStatus.CALCULATED) {
            throw new BusinessValidationException(
                "Only CALCULATED commissions can be approved. Current status: " + calc.getStatus());
        }

        calc.setStatus(CommissionCalculation.CommissionStatus.APPROVED);
        logger.info("Approved commission calculation: {}", id);
        return calculationRepository.save(calc);
    }

    /**
     * Mark a commission as paid.
     */
    public CommissionCalculation markAsPaid(String id) {
        CommissionCalculation calc = getCalculationById(id);

        if (calc.getStatus() != CommissionCalculation.CommissionStatus.APPROVED) {
            throw new BusinessValidationException(
                "Only APPROVED commissions can be paid. Current status: " + calc.getStatus());
        }

        calc.setStatus(CommissionCalculation.CommissionStatus.PAID);
        calc.setPayoutDate(LocalDate.now());
        logger.info("Marked commission as paid: {}", id);
        return calculationRepository.save(calc);
    }

    public long getCalculationCount() {
        return calculationRepository.count();
    }
}
