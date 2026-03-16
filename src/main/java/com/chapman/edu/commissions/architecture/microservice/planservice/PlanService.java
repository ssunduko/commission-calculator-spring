package com.chapman.edu.commissions.architecture.microservice.planservice;

import com.chapman.edu.commissions.architecture.microservice.common.dto.AddRuleRequest;
import com.chapman.edu.commissions.architecture.microservice.common.dto.CreatePlanRequest;
import com.chapman.edu.commissions.architecture.microservice.common.dto.PlanDto;
import com.chapman.edu.commissions.architecture.microservice.planservice.domain.CommissionPlan;
import com.chapman.edu.commissions.architecture.microservice.planservice.domain.CommissionRule;
import com.chapman.edu.commissions.architecture.microservice.planservice.domain.PlanStatus;
import com.chapman.edu.commissions.architecture.microservice.planservice.domain.RuleType;
import org.springframework.stereotype.Service;

import java.util.Currency;
import java.util.List;

/**
 * Service for managing commission plans.
 * Contains business logic for plan operations in the Plan microservice.
 */
@Service
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public PlanDto createPlan(CreatePlanRequest request) {
        request.validate();

        try {
            Currency currency = Currency.getInstance(request.currencyCode());
            CommissionPlan plan = new CommissionPlan(request.name(), currency);
            plan.setEffectiveStartDate(request.effectiveStartDate());
            plan.setEffectiveEndDate(request.effectiveEndDate());

            CommissionPlan savedPlan = planRepository.save(plan);
            return toDto(savedPlan);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid currency code: " + request.currencyCode());
        }
    }

    public PlanDto getPlan(String id) {
        CommissionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission Plan not found: " + id));
        return toDto(plan);
    }

    public List<PlanDto> getAllPlans() {
        return planRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public List<PlanDto> getPlansByStatus(PlanStatus status) {
        return planRepository.findByStatus(status).stream()
                .map(this::toDto)
                .toList();
    }

    public PlanDto activatePlan(String id) {
        CommissionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission Plan not found: " + id));

        plan.setStatus(PlanStatus.ACTIVE);
        CommissionPlan updatedPlan = planRepository.save(plan);
        return toDto(updatedPlan);
    }

    public PlanDto addRuleToPlan(String planId, AddRuleRequest request) {
        request.validate();

        CommissionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Commission Plan not found: " + planId));

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
            throw new RuntimeException("Invalid rule type: " + request.ruleType());
        }

        plan.addRule(rule);
        CommissionPlan updatedPlan = planRepository.save(plan);
        return toDto(updatedPlan);
    }

    public void deletePlan(String id) {
        if (!planRepository.existsById(id)) {
            throw new RuntimeException("Commission Plan not found: " + id);
        }
        planRepository.deleteById(id);
    }

    private PlanDto toDto(CommissionPlan plan) {
        return new PlanDto(plan.getId(), plan.getName(),
                plan.getCurrency() != null ? plan.getCurrency().getCurrencyCode() : null,
                plan.getStatus().name(), plan.getEffectiveStartDate(), plan.getEffectiveEndDate(),
                plan.getRules() != null ? plan.getRules().size() : 0,
                plan.getTiers() != null ? plan.getTiers().size() : 0);
    }
}
