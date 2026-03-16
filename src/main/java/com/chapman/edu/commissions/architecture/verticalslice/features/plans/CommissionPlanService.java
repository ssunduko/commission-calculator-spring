package com.chapman.edu.commissions.architecture.verticalslice.features.plans;

import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ValidationException;
import com.chapman.edu.commissions.architecture.verticalslice.domain.CommissionPlan;
import com.chapman.edu.commissions.architecture.verticalslice.domain.CommissionRule;
import com.chapman.edu.commissions.architecture.verticalslice.domain.PlanStatus;
import com.chapman.edu.commissions.architecture.verticalslice.domain.RuleType;
import org.springframework.stereotype.Service;

import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing commission plans.
 * Contains business logic for plan operations.
 * Exposed as MCP tools for AI agent access.
 */
@Service
public class CommissionPlanService {
    private final CommissionPlanRepository planRepository;

    public CommissionPlanService(CommissionPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public CommissionPlanResponse createPlan(CreateCommissionPlanRequest request) {
        request.validate();

        try {
            Currency currency = Currency.getInstance(request.currencyCode());
            CommissionPlan plan = new CommissionPlan(request.name(), currency);
            plan.setEffectiveStartDate(request.effectiveStartDate());
            plan.setEffectiveEndDate(request.effectiveEndDate());

            CommissionPlan savedPlan = planRepository.save(plan);
            return CommissionPlanResponse.from(savedPlan);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid currency code: " + request.currencyCode());
        }
    }

    public CommissionPlanResponse getPlan(String id) {
        CommissionPlan plan = planRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Commission Plan", id));
        return CommissionPlanResponse.from(plan);
    }

    public List<CommissionPlanResponse> getAllPlans() {
        return planRepository.findAll().stream()
            .map(CommissionPlanResponse::from)
            .collect(Collectors.toList());
    }

    public List<CommissionPlanResponse> getPlansByStatus(PlanStatus status) {
        return planRepository.findByStatus(status).stream()
            .map(CommissionPlanResponse::from)
            .collect(Collectors.toList());
    }

    public CommissionPlanResponse activatePlan(String id) {
        CommissionPlan plan = planRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Commission Plan", id));

        plan.setStatus(PlanStatus.ACTIVE);
        CommissionPlan updatedPlan = planRepository.save(plan);
        return CommissionPlanResponse.from(updatedPlan);
    }

    public CommissionPlanResponse addRuleToPlan(String planId, AddRuleToPlanRequest request) {
        request.validate();

        CommissionPlan plan = planRepository.findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("Commission Plan", planId));

        CommissionRule rule = new CommissionRule();
        rule.setName(request.name());
        rule.setDescription(request.description());
        rule.setRate(request.rate());
        rule.setPriority(request.priority());

        try {
            if (request.ruleType() != null) {
                rule.setType(RuleType.valueOf(request.ruleType().toUpperCase()));
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid rule type: " + request.ruleType());
        }

        plan.addRule(rule);
        CommissionPlan updatedPlan = planRepository.save(plan);
        return CommissionPlanResponse.from(updatedPlan);
    }

    public void deletePlan(String id) {
        if (!planRepository.existsById(id)) {
            throw new ResourceNotFoundException("Commission Plan", id);
        }
        planRepository.deleteById(id);
    }
}
