package com.chapman.edu.commissions.orm.service;

import com.chapman.edu.commissions.orm.entity.*;
import com.chapman.edu.commissions.orm.repository.CommissionCalculationRepository;
import com.chapman.edu.commissions.orm.repository.CommissionPlanRepository;
import com.chapman.edu.commissions.orm.repository.DealRepository;
import com.chapman.edu.commissions.orm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * SERVICE LAYER: CommissionService
 * ============================================================
 *
 * CORE BUSINESS LOGIC: Commission Calculation Engine
 *
 * This service implements the full commission calculation workflow:
 * 1. Load the active commission plan
 * 2. Find the applicable tier based on deal value
 * 3. Calculate base commission from the tier rate
 * 4. Apply any active bonus rules
 * 5. Save the calculation result
 *
 * TRANSACTION ISOLATION: REPEATABLE_READ
 * Commission calculations use REPEATABLE_READ isolation to ensure
 * consistent reads during the calculation. If another transaction
 * modifies the deal or plan while we're calculating, we still see
 * the original values. This prevents:
 * - Non-repeatable reads (deal value changes mid-calculation)
 * - Incorrect calculations from inconsistent data
 *
 * CACHING STRATEGY:
 * - Commission plans are cached (they change infrequently)
 * - Calculation results are cached by ID
 * - Cache is evicted when calculations are updated
 *
 * @Caching ANNOTATION:
 * Combines multiple cache operations on a single method.
 * Useful when one operation affects multiple caches.
 */
@Service
@Transactional(readOnly = true)
public class CommissionService {

    private static final Logger log = LoggerFactory.getLogger(CommissionService.class);

    private final CommissionCalculationRepository calculationRepository;
    private final CommissionPlanRepository planRepository;
    private final DealRepository dealRepository;
    private final UserRepository userRepository;

    public CommissionService(CommissionCalculationRepository calculationRepository,
                             CommissionPlanRepository planRepository,
                             DealRepository dealRepository,
                             UserRepository userRepository) {
        this.calculationRepository = calculationRepository;
        this.planRepository = planRepository;
        this.dealRepository = dealRepository;
        this.userRepository = userRepository;
    }

    @Cacheable(value = "calculations", key = "#id", unless = "#result == null")
    public Optional<CommissionCalculation> findById(String id) {
        log.info("Cache MISS - Loading calculation: {}", id);
        return calculationRepository.findById(id);
    }

    @Cacheable(value = "commissionPlans", key = "#planId", unless = "#result == null")
    public Optional<CommissionPlan> findPlanById(String planId) {
        log.info("Cache MISS - Loading plan: {}", planId);
        return planRepository.findById(planId);
    }

    public List<CommissionCalculation> findBySalesRep(String salesRepId) {
        return calculationRepository.findBySalesRepId(salesRepId);
    }

    public List<CommissionPlan> findActivePlans() {
        return planRepository.findActivePlansForDate(LocalDate.now());
    }

    /**
     * CORE METHOD: Calculate commission for a deal.
     *
     * TRANSACTION: Uses REPEATABLE_READ isolation to ensure consistent
     * data throughout the calculation process.
     *
     * BUSINESS LOGIC FLOW:
     * 1. Validate the deal exists and is WON
     * 2. Find the active commission plan
     * 3. Load plan tiers (for rate lookup)
     * 4. Find the applicable tier based on deal value
     * 5. Calculate base commission = deal value * tier rate / 100
     * 6. Apply active bonuses
     * 7. Save and return the calculation
     *
     * ALL-OR-NOTHING: If any step fails (e.g., no active plan found),
     * the entire transaction rolls back. No partial calculations saved.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = false)
    @CacheEvict(value = "calculations", allEntries = true)
    public CommissionCalculation calculateCommission(String dealId, String planId) {
        log.info("Calculating commission for deal: {} with plan: {}", dealId, planId);

        // Step 1: Load the deal
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("Deal not found: " + dealId));

        if (deal.getStatus() != DealStatus.WON) {
            throw new IllegalStateException("Cannot calculate commission for a deal that is not WON. Status: " + deal.getStatus());
        }

        // Step 2: Load the plan with tiers
        CommissionPlan plan = planRepository.findByIdWithTiers(planId)
                .orElseThrow(() -> new IllegalArgumentException("Commission plan not found: " + planId));

        // Step 3: Find the applicable tier
        BigDecimal dealValue = deal.getValue();
        BigDecimal commissionRate = BigDecimal.ZERO;

        for (CommissionTier tier : plan.getTiers()) {
            if (tier.containsValue(dealValue)) {
                commissionRate = tier.getRate();
                log.info("Deal value {} falls in tier '{}' with rate {}%",
                        dealValue, tier.getName(), commissionRate);
                break;
            }
        }

        // Step 4: Calculate base commission
        BigDecimal baseCommission = dealValue
                .multiply(commissionRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        // Step 5: Create the calculation entity
        User salesRep = deal.getSalesRep();
        CommissionCalculation calculation = new CommissionCalculation(deal, salesRep, baseCommission);
        calculation.setPlan(plan);
        calculation.setCalculatedBy("system");

        // Step 6: Apply active bonuses
        CommissionPlan planWithBonuses = planRepository.findByIdWithBonuses(planId)
                .orElse(plan);

        for (BonusRule bonusRule : planWithBonuses.getBonuses()) {
            if (bonusRule.isActiveOn(LocalDate.now())) {
                BigDecimal bonusAmount = bonusRule.calculateBonus(baseCommission);
                BonusCalculation bonusCalc = new BonusCalculation(
                        bonusRule.getId(), bonusRule.getName(), bonusAmount);
                bonusCalc.setDescription("Applied bonus: " + bonusRule.getName());
                calculation.addBonus(bonusCalc);
                log.info("Applied bonus '{}': {}", bonusRule.getName(), bonusAmount);
            }
        }

        // Step 7: Recalculate totals and save
        calculation.recalculate();
        CommissionCalculation saved = calculationRepository.save(calculation);
        log.info("Commission calculated: base={}, gross={}, net={}",
                saved.getBaseCommission(), saved.getGrossCommission(), saved.getNetCommission());

        return saved;
    }

    /**
     * Approve a commission calculation.
     *
     * @Caching: Combines multiple cache operations.
     * Here we evict from the "calculations" cache when approving.
     */
    @Transactional(readOnly = false)
    @Caching(evict = {
            @CacheEvict(value = "calculations", key = "#calculationId")
    })
    public CommissionCalculation approveCalculation(String calculationId, String approvedBy) {
        log.info("Approving calculation: {} by: {}", calculationId, approvedBy);

        CommissionCalculation calc = calculationRepository.findById(calculationId)
                .orElseThrow(() -> new IllegalArgumentException("Calculation not found: " + calculationId));

        if (calc.getStatus() != CommissionStatus.CALCULATED) {
            throw new IllegalStateException(
                    "Can only approve CALCULATED commissions. Current status: " + calc.getStatus());
        }

        calc.setStatus(CommissionStatus.APPROVED);
        return calculationRepository.save(calc);
    }

    /**
     * Bulk status update using @Modifying query.
     * Demonstrates efficient bulk operations vs. loading each entity individually.
     *
     * Returns the count of updated records.
     */
    @Transactional(readOnly = false)
    @CacheEvict(value = "calculations", allEntries = true)
    public int bulkApproveCalculations(LocalDate beforeDate) {
        log.info("Bulk approving calculations before: {}", beforeDate);
        return calculationRepository.bulkUpdateStatus(
                CommissionStatus.CALCULATED,
                CommissionStatus.APPROVED,
                beforeDate);
    }

    public BigDecimal getTotalCommissionsForSalesRep(String salesRepId) {
        return calculationRepository.calculateTotalCommissionBySalesRep(
                salesRepId,
                List.of(CommissionStatus.APPROVED, CommissionStatus.PAID));
    }

    public List<Object[]> getCommissionSummary() {
        return calculationRepository.getCommissionSummaryBySalesRep();
    }

    /**
     * Create a new commission plan.
     */
    @Transactional(readOnly = false)
    @CacheEvict(value = "commissionPlans", allEntries = true)
    public CommissionPlan createPlan(CommissionPlan plan) {
        log.info("Creating commission plan: {}", plan.getName());
        return planRepository.save(plan);
    }

    /**
     * Activate a commission plan.
     */
    @Transactional(readOnly = false)
    @CacheEvict(value = "commissionPlans", key = "#planId")
    public CommissionPlan activatePlan(String planId, LocalDate startDate, LocalDate endDate) {
        CommissionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        plan.setStatus(PlanStatus.ACTIVE);
        plan.setEffectiveStartDate(startDate);
        plan.setEffectiveEndDate(endDate);
        return planRepository.save(plan);
    }
}
