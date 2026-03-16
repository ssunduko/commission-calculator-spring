package com.chapman.edu.commissions.architecture.ddd.application.plan;

import com.chapman.edu.commissions.architecture.ddd.application.dto.*;
import com.chapman.edu.commissions.architecture.ddd.domain.plan.CommissionPlan;
import com.chapman.edu.commissions.architecture.ddd.domain.plan.CommissionPlanRepository;
import com.chapman.edu.commissions.architecture.ddd.domain.plan.CommissionRule;
import com.chapman.edu.commissions.architecture.ddd.domain.plan.PlanStatus;
import com.chapman.edu.commissions.architecture.ddd.domain.plan.RuleType;
import com.chapman.edu.commissions.architecture.ddd.domain.shared.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;
import java.util.List;

/**
 * Application Service for commission plan use cases.
 *
 * Orchestrates plan creation, activation, rule management, and retrieval
 * by coordinating domain aggregates and repositories. Business rules
 * live in the domain layer; this service manages the workflow.
 */
@Service
@Transactional
public class CommissionPlanApplicationService {

    private static final Logger log = LoggerFactory.getLogger(CommissionPlanApplicationService.class);
    private final CommissionPlanRepository planRepository;

    public CommissionPlanApplicationService(CommissionPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public CommissionPlanDto createPlan(CreatePlanRequest request) {
        request.validate();

        try {
            Currency currency = Currency.getInstance(request.currencyCode());
            CommissionPlan plan = new CommissionPlan(request.name(), currency);
            plan.setEffectiveStartDate(request.effectiveStartDate());
            plan.setEffectiveEndDate(request.effectiveEndDate());

            CommissionPlan saved = planRepository.save(plan);
            log.info("Commission plan created: id={}, name={}", saved.getId(), saved.getName());
            return CommissionPlanDto.fromEntity(saved);
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid currency code: " + request.currencyCode());
        }
    }

    @Transactional(readOnly = true)
    public CommissionPlanDto getPlan(String id) {
        CommissionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new DomainException("Commission Plan not found: " + id));
        return CommissionPlanDto.fromEntity(plan);
    }

    @Transactional(readOnly = true)
    public List<CommissionPlanDto> getAllPlans() {
        return planRepository.findAll().stream().map(CommissionPlanDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<CommissionPlanDto> getPlansByStatus(PlanStatus status) {
        return planRepository.findByStatus(status).stream().map(CommissionPlanDto::fromEntity).toList();
    }

    public CommissionPlanDto activatePlan(String id) {
        CommissionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new DomainException("Commission Plan not found: " + id));

        plan.setStatus(PlanStatus.ACTIVE);
        CommissionPlan updated = planRepository.save(plan);
        log.info("Commission plan activated: id={}", updated.getId());
        return CommissionPlanDto.fromEntity(updated);
    }

    public CommissionPlanDto addRuleToPlan(String planId, AddRuleRequest request) {
        request.validate();

        CommissionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new DomainException("Commission Plan not found: " + planId));

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
            throw new DomainException("Invalid rule type: " + request.ruleType());
        }

        plan.addRule(rule);
        CommissionPlan updated = planRepository.save(plan);
        log.info("Rule added to plan: planId={}, ruleName={}", planId, request.name());
        return CommissionPlanDto.fromEntity(updated);
    }

    public void deletePlan(String id) {
        if (!planRepository.existsById(id)) {
            throw new DomainException("Commission Plan not found: " + id);
        }
        planRepository.deleteById(id);
    }
}
