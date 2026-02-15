package com.chapman.edu.commissions.springboot.service;

import com.chapman.edu.commissions.model.CommissionPlan;
import com.chapman.edu.commissions.model.PlanStatus;
import com.chapman.edu.commissions.springboot.dto.request.CreatePlanRequest;
import com.chapman.edu.commissions.springboot.exception.BusinessValidationException;
import com.chapman.edu.commissions.springboot.exception.ResourceNotFoundException;
import com.chapman.edu.commissions.springboot.repository.CommissionPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

/**
 * Service layer for Commission Plan business logic.
 *
 * Demonstrates business validation rules that go beyond simple field validation:
 *   - End date must be after start date
 *   - Only DRAFT plans can be activated
 *   - ARCHIVED plans cannot be modified
 */
@Service
public class CommissionPlanService {

    private static final Logger logger = LoggerFactory.getLogger(CommissionPlanService.class);

    private final CommissionPlanRepository planRepository;

    public CommissionPlanService(CommissionPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public CommissionPlan createPlan(CreatePlanRequest request) {
        // Business validation: end date must be after start date
        if (request.getEffectiveEndDate() != null &&
            request.getEffectiveEndDate().isBefore(request.getEffectiveStartDate())) {
            throw new BusinessValidationException(
                "Effective end date must be after the start date");
        }

        CommissionPlan plan = new CommissionPlan(
            request.getName(),
            Currency.getInstance(request.getCurrencyCode())
        );
        plan.setEffectiveStartDate(request.getEffectiveStartDate());
        plan.setEffectiveEndDate(request.getEffectiveEndDate());
        plan.setCreatedBy(request.getCreatedBy());
        // New plans start as DRAFT
        plan.setStatus(PlanStatus.DRAFT);

        CommissionPlan saved = planRepository.save(plan);
        logger.info("Created commission plan: {} (ID: {})", saved.getName(), saved.getId());
        return saved;
    }

    public CommissionPlan getPlanById(String id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommissionPlan", "id", id));
    }

    public List<CommissionPlan> getAllPlans() {
        return planRepository.findAll();
    }

    public List<CommissionPlan> getPlansByStatus(PlanStatus status) {
        return planRepository.findByStatus(status);
    }

    public List<CommissionPlan> getActivePlans() {
        return planRepository.findActivePlansOnDate(LocalDate.now());
    }

    /**
     * Activate a plan. Only DRAFT plans can be activated.
     */
    public CommissionPlan activatePlan(String id) {
        CommissionPlan plan = getPlanById(id);

        if (plan.getStatus() != PlanStatus.DRAFT) {
            throw new BusinessValidationException(
                "Only DRAFT plans can be activated. Current status: " + plan.getStatus());
        }

        plan.setStatus(PlanStatus.ACTIVE);
        logger.info("Activated commission plan: {}", id);
        return planRepository.save(plan);
    }

    /**
     * Archive a plan.
     */
    public CommissionPlan archivePlan(String id) {
        CommissionPlan plan = getPlanById(id);
        plan.setStatus(PlanStatus.ARCHIVED);
        logger.info("Archived commission plan: {}", id);
        return planRepository.save(plan);
    }

    public void deletePlan(String id) {
        CommissionPlan plan = getPlanById(id);

        if (plan.getStatus() == PlanStatus.ACTIVE) {
            throw new BusinessValidationException(
                "Cannot delete an active plan. Archive it first.");
        }

        planRepository.deleteById(id);
        logger.info("Deleted commission plan: {}", id);
    }

    public long getPlanCount() {
        return planRepository.count();
    }
}
