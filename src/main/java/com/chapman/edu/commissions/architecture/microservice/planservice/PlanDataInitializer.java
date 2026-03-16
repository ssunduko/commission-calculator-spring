package com.chapman.edu.commissions.architecture.microservice.planservice;

import com.chapman.edu.commissions.architecture.microservice.planservice.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

/**
 * Initializes sample commission plan data on application startup.
 */
@Component
public class PlanDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PlanDataInitializer.class);

    private final PlanRepository planRepository;

    public PlanDataInitializer(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public void run(String... args) {
        log.info("[PLAN-SERVICE] Initializing sample plan data...");

        // Create Standard Sales Plan
        CommissionPlan standardPlan = new CommissionPlan("Standard Sales Plan", Currency.getInstance("USD"));
        standardPlan.setStatus(PlanStatus.ACTIVE);
        standardPlan.setEffectiveStartDate(LocalDate.now().minusMonths(6));
        standardPlan.setCreatedBy("admin");

        CommissionRule standardRule = new CommissionRule("Base Commission", new BigDecimal("10.0"), RuleType.STANDARD);
        standardRule.setDescription("10% commission on all deals");
        standardPlan.addRule(standardRule);

        CommissionTier tier1 = new CommissionTier("Tier 1", BigDecimal.ZERO, new BigDecimal("50000"), new BigDecimal("8"));
        CommissionTier tier2 = new CommissionTier("Tier 2", new BigDecimal("50000"), new BigDecimal("100000"), new BigDecimal("10"));
        CommissionTier tier3 = new CommissionTier("Tier 3", new BigDecimal("100000"), null, new BigDecimal("12"));
        standardPlan.addTier(tier1);
        standardPlan.addTier(tier2);
        standardPlan.addTier(tier3);

        planRepository.save(standardPlan);

        // Create Premium Sales Plan
        CommissionPlan premiumPlan = new CommissionPlan("Premium Sales Plan", Currency.getInstance("USD"));
        premiumPlan.setStatus(PlanStatus.ACTIVE);
        premiumPlan.setEffectiveStartDate(LocalDate.now().minusMonths(3));
        premiumPlan.setCreatedBy("admin");

        CommissionRule premiumRule = new CommissionRule("Premium Commission", new BigDecimal("12.0"), RuleType.STANDARD);
        premiumRule.setDescription("12% commission on all deals");
        premiumPlan.addRule(premiumRule);

        BonusRule bonus = new BonusRule("Q4 Performance Bonus", new BigDecimal("5000"), false, BonusType.QUOTA_ACHIEVEMENT);
        bonus.setDescription("$5000 bonus for quarterly quota achievement");
        bonus.setStartDate(LocalDate.now().minusMonths(3));
        bonus.setEndDate(LocalDate.now().plusMonths(3));
        premiumPlan.addBonus(bonus);

        planRepository.save(premiumPlan);

        log.info("[PLAN-SERVICE] Sample data initialized: 2 Plans (Standard + Premium)");
    }
}
