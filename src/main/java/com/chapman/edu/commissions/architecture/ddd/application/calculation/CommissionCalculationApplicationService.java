package com.chapman.edu.commissions.architecture.ddd.application.calculation;

import com.chapman.edu.commissions.architecture.ddd.application.dto.*;
import com.chapman.edu.commissions.architecture.ddd.domain.calculation.CommissionCalculation;
import com.chapman.edu.commissions.architecture.ddd.domain.calculation.CommissionCalculationRepository;
import com.chapman.edu.commissions.architecture.ddd.domain.calculation.CommissionCalculationService;
import com.chapman.edu.commissions.architecture.ddd.domain.deal.Deal;
import com.chapman.edu.commissions.architecture.ddd.domain.deal.DealRepository;
import com.chapman.edu.commissions.architecture.ddd.domain.plan.CommissionPlan;
import com.chapman.edu.commissions.architecture.ddd.domain.plan.CommissionPlanRepository;
import com.chapman.edu.commissions.architecture.ddd.domain.shared.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Application Service for commission calculation use cases.
 *
 * Coordinates the calculation workflow: loads the Deal and Plan aggregates,
 * delegates the actual calculation logic to the domain service
 * ({@link CommissionCalculationService}), and persists the result.
 */
@Service
@Transactional
public class CommissionCalculationApplicationService {

    private static final Logger log = LoggerFactory.getLogger(CommissionCalculationApplicationService.class);
    private final CommissionCalculationRepository calculationRepository;
    private final DealRepository dealRepository;
    private final CommissionPlanRepository planRepository;

    public CommissionCalculationApplicationService(
            CommissionCalculationRepository calculationRepository,
            DealRepository dealRepository,
            CommissionPlanRepository planRepository
    ) {
        this.calculationRepository = calculationRepository;
        this.dealRepository = dealRepository;
        this.planRepository = planRepository;
    }

    public CommissionCalculationDto calculateCommission(CalculateCommissionRequest request) {
        request.validate();

        Deal deal = dealRepository.findById(request.dealId())
                .orElseThrow(() -> new DomainException("Deal not found: " + request.dealId()));

        CommissionPlan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new DomainException("Commission Plan not found: " + request.planId()));

        BigDecimal baseCommission = CommissionCalculationService.calculateBaseCommission(deal, plan);

        CommissionCalculation calculation = new CommissionCalculation(
                deal.getId(),
                deal.getSalesRepId(),
                baseCommission
        );
        calculation.setPlanId(plan.getId());
        calculation.recalculate();

        CommissionCalculation saved = calculationRepository.save(calculation);
        log.info("Commission calculated: id={}, dealId={}, amount={}",
                saved.getId(), saved.getDealId(), saved.getNetCommission());
        return CommissionCalculationDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public CommissionCalculationDto getCalculation(String id) {
        CommissionCalculation calculation = calculationRepository.findById(id)
                .orElseThrow(() -> new DomainException("Commission Calculation not found: " + id));
        return CommissionCalculationDto.fromEntity(calculation);
    }

    @Transactional(readOnly = true)
    public List<CommissionCalculationDto> getAllCalculations() {
        return calculationRepository.findAll().stream()
                .map(CommissionCalculationDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<CommissionCalculationDto> getCalculationsByDeal(String dealId) {
        return calculationRepository.findByDealId(dealId).stream()
                .map(CommissionCalculationDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<CommissionCalculationDto> getCalculationsBySalesRep(String salesRepId) {
        return calculationRepository.findBySalesRepId(salesRepId).stream()
                .map(CommissionCalculationDto::fromEntity).toList();
    }
}
