package com.chapman.edu.commissions.architecture.cleanarchitecture.application.service;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.AddRuleCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CreatePlanCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.PlanResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in.CommissionPlanUseCase;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.CommissionPlanRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.EntityNotFoundException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionPlan;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionRule;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.PlanStatus;
import org.springframework.stereotype.Service;

import java.util.Currency;
import java.util.List;

/**
 * Application service implementing commission plan management use cases.
 */
@Service
public class CommissionPlanService implements CommissionPlanUseCase {

    private final CommissionPlanRepositoryPort planRepository;

    public CommissionPlanService(CommissionPlanRepositoryPort planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public PlanResult createPlan(CreatePlanCommand command) {
        command.validate();
        Currency currency = Currency.getInstance(command.currencyCode());
        CommissionPlan plan = new CommissionPlan(
                command.name(),
                currency,
                command.effectiveStartDate(),
                command.effectiveEndDate()
        );
        CommissionPlan saved = planRepository.save(plan);
        return PlanResult.from(saved);
    }

    @Override
    public PlanResult getPlan(String id) {
        CommissionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CommissionPlan", id));
        return PlanResult.from(plan);
    }

    @Override
    public List<PlanResult> getAllPlans() {
        return planRepository.findAll().stream()
                .map(PlanResult::from)
                .toList();
    }

    @Override
    public List<PlanResult> getPlansByStatus(PlanStatus status) {
        return planRepository.findByStatus(status).stream()
                .map(PlanResult::from)
                .toList();
    }

    @Override
    public PlanResult activatePlan(String id) {
        CommissionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CommissionPlan", id));
        plan.setStatus(PlanStatus.ACTIVE);
        CommissionPlan saved = planRepository.save(plan);
        return PlanResult.from(saved);
    }

    @Override
    public PlanResult addRuleToPlan(String planId, AddRuleCommand command) {
        command.validate();
        CommissionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("CommissionPlan", planId));
        CommissionRule rule = new CommissionRule(
                command.name(),
                command.rate(),
                command.ruleType()
        );
        rule.setDescription(command.description());
        rule.setPriority(command.priority());
        plan.addRule(rule);
        CommissionPlan saved = planRepository.save(plan);
        return PlanResult.from(saved);
    }

    @Override
    public void deletePlan(String id) {
        planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CommissionPlan", id));
        planRepository.deleteById(id);
    }
}
