package com.chapman.edu.commissions.architecture.eventdriven.features.calculations;

import com.chapman.edu.commissions.architecture.eventdriven.domain.CommissionCalculation;
import com.chapman.edu.commissions.architecture.eventdriven.domain.CommissionPlan;
import com.chapman.edu.commissions.architecture.eventdriven.domain.CommissionRule;
import com.chapman.edu.commissions.architecture.eventdriven.domain.Deal;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.CommissionCalculatedEvent;
import com.chapman.edu.commissions.architecture.eventdriven.features.deals.DealRepository;
import com.chapman.edu.commissions.architecture.eventdriven.features.plans.CommissionPlanRepository;
import com.chapman.edu.commissions.architecture.eventdriven.infrastructure.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CONCEPT: Event-Publishing Calculation Service
 *
 * This service performs the same tier-based commission calculation as the
 * vertical-slice version but publishes a {@link CommissionCalculatedEvent}
 * after each calculation. This is a cross-aggregate event — it bridges
 * the Deal and CommissionCalculation aggregates.
 *
 * Downstream listeners can react to the calculation event to:
 * - Notify the sales rep of their commission amount
 * - Trigger approval workflows for large commissions
 * - Update running totals and reporting dashboards
 * - Feed data into forecasting models
 */
@Service
public class CommissionCalculationService {

    private static final Logger log = LoggerFactory.getLogger(CommissionCalculationService.class);

    private final CommissionCalculationRepository calculationRepository;
    private final DealRepository dealRepository;
    private final CommissionPlanRepository planRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CommissionCalculationService(
            CommissionCalculationRepository calculationRepository,
            DealRepository dealRepository,
            CommissionPlanRepository planRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.calculationRepository = calculationRepository;
        this.dealRepository = dealRepository;
        this.planRepository = planRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Calculates commission for a deal under a plan and publishes a
     * {@link CommissionCalculatedEvent}.
     *
     * CONCEPT: The event carries the calculated amounts so that listeners
     * do not need to query the database again — they receive all the
     * information they need to act.
     */
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

        log.info("Commission calculated: id={}, dealId={}, amount={}",
                savedCalculation.getId(), deal.getId(), savedCalculation.getNetCommission());

        eventPublisher.publishEvent(new CommissionCalculatedEvent(
                savedCalculation.getId(),
                deal.getId(),
                deal.getSalesRepId(),
                savedCalculation.getBaseCommission(),
                savedCalculation.getNetCommission()
        ));

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
