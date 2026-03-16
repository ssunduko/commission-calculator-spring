package com.chapman.edu.commissions.architecture.eventdriven.features.plans;

import com.chapman.edu.commissions.architecture.eventdriven.domain.CommissionPlan;
import com.chapman.edu.commissions.architecture.eventdriven.domain.CommissionRule;
import com.chapman.edu.commissions.architecture.eventdriven.domain.PlanStatus;
import com.chapman.edu.commissions.architecture.eventdriven.domain.RuleType;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.CommissionPlanActivatedEvent;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.CommissionPlanCreatedEvent;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.RuleAddedToPlanEvent;
import com.chapman.edu.commissions.architecture.eventdriven.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.eventdriven.infrastructure.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CONCEPT: Event-Publishing Plan Service
 *
 * This service manages commission plan lifecycle operations and publishes
 * domain events at each significant state transition:
 *
 * - {@link CommissionPlanCreatedEvent} — a new plan is born
 * - {@link CommissionPlanActivatedEvent} — a plan becomes usable for calculations
 * - {@link RuleAddedToPlanEvent} — a rule is attached, potentially changing
 *   how future (or existing) commissions are calculated
 *
 * Each event gives listeners the opportunity to react without the plan
 * service needing to know about downstream concerns like notifications,
 * recalculation triggers, or audit logging.
 */
@Service
public class CommissionPlanService {

    private static final Logger log = LoggerFactory.getLogger(CommissionPlanService.class);

    private final CommissionPlanRepository planRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CommissionPlanService(CommissionPlanRepository planRepository,
                                 ApplicationEventPublisher eventPublisher) {
        this.planRepository = planRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new commission plan and publishes a {@link CommissionPlanCreatedEvent}.
     *
     * CONCEPT: The creation event allows listeners to perform initialization
     * tasks like setting up default tiers or notifying administrators that
     * a new plan requires review before activation.
     */
    public CommissionPlanResponse createPlan(CreateCommissionPlanRequest request) {
        request.validate();

        try {
            Currency currency = Currency.getInstance(request.currencyCode());
            CommissionPlan plan = new CommissionPlan(request.name(), currency);
            plan.setEffectiveStartDate(request.effectiveStartDate());
            plan.setEffectiveEndDate(request.effectiveEndDate());

            CommissionPlan savedPlan = planRepository.save(plan);

            log.info("Commission plan created: id={}, name={}", savedPlan.getId(), savedPlan.getName());

            eventPublisher.publishEvent(new CommissionPlanCreatedEvent(
                    savedPlan.getId(),
                    savedPlan.getName(),
                    currency.getCurrencyCode()
            ));

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

    /**
     * Activates a commission plan and publishes a {@link CommissionPlanActivatedEvent}.
     *
     * CONCEPT: Plan activation is a significant state transition because only
     * ACTIVE plans can be used for commission calculations. The event allows
     * listeners to, for example, notify sales reps that their plan is now live
     * or trigger batch recalculation of pending deals.
     */
    public CommissionPlanResponse activatePlan(String id) {
        CommissionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commission Plan", id));

        plan.setStatus(PlanStatus.ACTIVE);
        CommissionPlan updatedPlan = planRepository.save(plan);

        log.info("Commission plan activated: id={}, name={}", updatedPlan.getId(), updatedPlan.getName());

        eventPublisher.publishEvent(new CommissionPlanActivatedEvent(
                updatedPlan.getId(),
                updatedPlan.getName()
        ));

        return CommissionPlanResponse.from(updatedPlan);
    }

    /**
     * Adds a rule to a commission plan and publishes a {@link RuleAddedToPlanEvent}.
     *
     * CONCEPT: When a new rule is added to an active plan, downstream listeners
     * may need to recalculate existing commissions under that plan to reflect
     * the updated rate structure.
     */
    public CommissionPlanResponse addRuleToPlan(String planId, AddRuleToPlanRequest request) {
        request.validate();

        CommissionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Commission Plan", planId));

        CommissionRule rule = new CommissionRule();
        rule.setName(request.name());
        rule.setDescription(request.description());
        rule.setRate(request.rate());
        rule.setPriority(request.priority());

        String resolvedRuleType = null;
        try {
            if (request.ruleType() != null) {
                resolvedRuleType = request.ruleType().toUpperCase();
                rule.setType(RuleType.valueOf(resolvedRuleType));
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid rule type: " + request.ruleType());
        }

        plan.addRule(rule);
        CommissionPlan updatedPlan = planRepository.save(plan);

        log.info("Rule added to plan: planId={}, ruleName={}", planId, request.name());

        eventPublisher.publishEvent(new RuleAddedToPlanEvent(
                updatedPlan.getId(),
                request.name(),
                request.rate(),
                resolvedRuleType
        ));

        return CommissionPlanResponse.from(updatedPlan);
    }

    public void deletePlan(String id) {
        if (!planRepository.existsById(id)) {
            throw new ResourceNotFoundException("Commission Plan", id);
        }
        planRepository.deleteById(id);
    }
}
