package com.chapman.edu.commissions.architecture.cleanarchitecture.application.service;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CalculateCommissionCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CalculationResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in.CommissionCalculationUseCase;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.CommissionCalculationRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.CommissionPlanRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.DealRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.DomainException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.EntityNotFoundException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionCalculation;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionPlan;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionRule;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.Deal;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DealStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Application service implementing commission calculation use cases.
 */
@Service
public class CommissionCalculationService implements CommissionCalculationUseCase {

    private final CommissionCalculationRepositoryPort calculationRepository;
    private final DealRepositoryPort dealRepository;
    private final CommissionPlanRepositoryPort planRepository;

    public CommissionCalculationService(
            CommissionCalculationRepositoryPort calculationRepository,
            DealRepositoryPort dealRepository,
            CommissionPlanRepositoryPort planRepository) {
        this.calculationRepository = calculationRepository;
        this.dealRepository = dealRepository;
        this.planRepository = planRepository;
    }

    @Override
    public CalculationResult calculateCommission(CalculateCommissionCommand command) {
        command.validate();

        Deal deal = dealRepository.findById(command.dealId())
                .orElseThrow(() -> new EntityNotFoundException("Deal", command.dealId()));

        if (deal.getStatus() != DealStatus.WON) {
            throw new DomainException("Commission can only be calculated for deals with status WON");
        }

        CommissionPlan plan = planRepository.findById(command.planId())
                .orElseThrow(() -> new EntityNotFoundException("CommissionPlan", command.planId()));

        // Calculate base commission using the first rule's rate as a percentage of deal value
        BigDecimal baseCommission = BigDecimal.ZERO;
        if (!plan.getRules().isEmpty()) {
            CommissionRule firstRule = plan.getRules().get(0);
            baseCommission = deal.getValue()
                    .multiply(firstRule.getRate())
                    .divide(BigDecimal.valueOf(100));
        }

        CommissionCalculation calculation = new CommissionCalculation(
                deal.getId(),
                deal.getSalesRepId(),
                baseCommission
        );
        calculation.setPlanId(plan.getId());

        calculation.recalculate();
        CommissionCalculation saved = calculationRepository.save(calculation);
        return CalculationResult.from(saved);
    }

    @Override
    public CalculationResult getCalculation(String id) {
        CommissionCalculation calculation = calculationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CommissionCalculation", id));
        return CalculationResult.from(calculation);
    }

    @Override
    public List<CalculationResult> getAllCalculations() {
        return calculationRepository.findAll().stream()
                .map(CalculationResult::from)
                .toList();
    }

    @Override
    public List<CalculationResult> getCalculationsByDeal(String dealId) {
        return calculationRepository.findByDealId(dealId).stream()
                .map(CalculationResult::from)
                .toList();
    }

    @Override
    public List<CalculationResult> getCalculationsBySalesRep(String salesRepId) {
        return calculationRepository.findBySalesRepId(salesRepId).stream()
                .map(CalculationResult::from)
                .toList();
    }

    @Override
    public void deleteCalculation(String id) {
        calculationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CommissionCalculation", id));
        calculationRepository.deleteById(id);
    }
}
