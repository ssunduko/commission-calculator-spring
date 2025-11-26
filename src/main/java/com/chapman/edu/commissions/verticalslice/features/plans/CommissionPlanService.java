package com.chapman.edu.commissions.verticalslice.features.plans;

import com.chapman.edu.commissions.verticalslice.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.verticalslice.infrastructure.exceptions.ValidationException;
import com.chapman.edu.commissions.verticalslice.domain.CommissionPlan;
import com.chapman.edu.commissions.verticalslice.domain.CommissionRule;
import com.chapman.edu.commissions.verticalslice.domain.PlanStatus;
import com.chapman.edu.commissions.verticalslice.domain.RuleType;
import org.springframework.ai.tool.annotation.Tool;
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

    @Tool(name = "createCommissionPlan",
            description = "Create a new commission plan with name, currency code, and effective dates. Returns the created plan details.")
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

    @Tool(name = "getCommissionPlan",
            description = "Get a commission plan by its ID. Returns the plan details including rules and status.")
    public CommissionPlanResponse getPlan(String id) {
        CommissionPlan plan = planRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Commission Plan", id));
        return CommissionPlanResponse.from(plan);
    }

    @Tool(name = "getAllCommissionPlans",
            description = "Get all commission plans in the system. Returns a list of all plans with their details.")
    public List<CommissionPlanResponse> getAllPlans() {
        return planRepository.findAll().stream()
            .map(CommissionPlanResponse::from)
            .collect(Collectors.toList());
    }

    @Tool(name = "getCommissionPlansByStatus",
            description = "Get all commission plans with a specific status (DRAFT, ACTIVE, INACTIVE, ARCHIVED). Specify the status.")
    public List<CommissionPlanResponse> getPlansByStatus(PlanStatus status) {
        return planRepository.findByStatus(status).stream()
            .map(CommissionPlanResponse::from)
            .collect(Collectors.toList());
    }

    @Tool(name = "activateCommissionPlan",
            description = "Activate a commission plan by its ID. Changes the plan status to ACTIVE.")
    public CommissionPlanResponse activatePlan(String id) {
        CommissionPlan plan = planRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Commission Plan", id));

        plan.setStatus(PlanStatus.ACTIVE);
        CommissionPlan updatedPlan = planRepository.save(plan);
        return CommissionPlanResponse.from(updatedPlan);
    }

    @Tool(name = "addRuleToPlan",
            description = "Add a commission rule to a plan. Specify plan ID, rule name, description, rate, priority, and rule type.")
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

    @Tool(name = "deleteCommissionPlan",
            description = "Delete a commission plan by its ID. This permanently removes the plan from the system.")
    public void deletePlan(String id) {
        if (!planRepository.existsById(id)) {
            throw new ResourceNotFoundException("Commission Plan", id);
        }
        planRepository.deleteById(id);
    }
}
