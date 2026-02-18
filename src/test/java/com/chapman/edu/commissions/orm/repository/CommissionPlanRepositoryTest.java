package com.chapman.edu.commissions.orm.repository;

import com.chapman.edu.commissions.orm.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for CommissionPlanRepository.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - Cascade persist: Saving a plan cascades to rules, tiers, and bonuses
 * - JOIN FETCH queries: Loading plans with collections in a single query
 * - TestEntityManager: Spring Boot test utility for JPA entity management
 * - Aggregate root pattern: Plan owns its child entities
 */
@DataJpaTest
@DisplayName("CommissionPlanRepository — Integration Tests")
class CommissionPlanRepositoryTest {

    @Autowired
    private CommissionPlanRepository planRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByStatus should return plans with matching status")
    void findByStatus_shouldReturnPlansWithStatus() {
        List<CommissionPlan> activePlans = planRepository.findByStatus(PlanStatus.ACTIVE);

        // V2 seed: plan-001 and plan-002 are ACTIVE
        assertThat(activePlans).hasSize(2);
        assertThat(activePlans).allMatch(p -> p.getStatus() == PlanStatus.ACTIVE);
    }

    @Test
    @DisplayName("findActivePlansForDate should return plans active on given date")
    void findActivePlansForDate_shouldReturnActivePlansForDate() {
        List<CommissionPlan> plans = planRepository.findActivePlansForDate(LocalDate.of(2024, 6, 15));

        // plan-001 and plan-002 are active from 2024-01-01 to 2024-12-31
        assertThat(plans).hasSize(2);
    }

    @Test
    @DisplayName("findByIdWithRules should load plan with rules eagerly")
    void findByIdWithRules_shouldLoadPlanWithRules() {
        Optional<CommissionPlan> plan = planRepository.findByIdWithRules("plan-001");

        assertThat(plan).isPresent();
        // plan-001 has rules: rule-001 and rule-003
        assertThat(plan.get().getRules()).hasSize(2);
    }

    @Test
    @DisplayName("findByIdWithTiers should load plan with tiers eagerly")
    void findByIdWithTiers_shouldLoadPlanWithTiers() {
        Optional<CommissionPlan> plan = planRepository.findByIdWithTiers("plan-001");

        assertThat(plan).isPresent();
        // plan-001 has 4 tiers: Bronze, Silver, Gold, Platinum
        assertThat(plan.get().getTiers()).hasSize(4);
    }

    @Test
    @DisplayName("findByIdWithBonuses should load plan with bonuses eagerly")
    void findByIdWithBonuses_shouldLoadPlanWithBonuses() {
        Optional<CommissionPlan> plan = planRepository.findByIdWithBonuses("plan-001");

        assertThat(plan).isPresent();
        // plan-001 has 2 bonuses: bonus-001, bonus-002
        assertThat(plan.get().getBonuses()).hasSize(2);
    }

    @Test
    @DisplayName("save should persist plan with cascaded children")
    void save_shouldPersistPlanWithCascadedChildren() {
        // Create a new plan with rules and tiers
        CommissionPlan plan = new CommissionPlan("Test Plan", Currency.getInstance("USD"));
        plan.setCreatedBy("test");

        CommissionRule rule = new CommissionRule("Test Rule", new BigDecimal("10"), RuleType.STANDARD);
        plan.addRule(rule);

        CommissionTier tier = new CommissionTier();
        tier.setName("Test Tier");
        tier.setLowerBound(BigDecimal.ZERO);
        tier.setUpperBound(new BigDecimal("50000"));
        tier.setRate(new BigDecimal("5"));
        tier.setPercentage(true);
        plan.addTier(tier);

        // Save — CascadeType.ALL should persist rule and tier too
        CommissionPlan saved = planRepository.save(plan);
        entityManager.flush();
        entityManager.clear();

        // Retrieve and verify cascaded children were persisted
        Optional<CommissionPlan> found = planRepository.findByIdWithRules(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getRules()).hasSize(1);
        assertThat(found.get().getRules().get(0).getName()).isEqualTo("Test Rule");
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase should find plans by partial name")
    void findByNameContainingIgnoreCase_shouldFindByPartialName() {
        List<CommissionPlan> plans = planRepository.findByNameContainingIgnoreCase("standard");

        assertThat(plans).hasSize(1);
        assertThat(plans.get(0).getName()).contains("Standard");
    }

    @Test
    @DisplayName("findByCreatedBy should return plans created by a specific user")
    void findByCreatedBy_shouldReturnPlansByCreator() {
        List<CommissionPlan> plans = planRepository.findByCreatedBy("admin");

        // V2 seed: all 3 plans were created by "admin"
        assertThat(plans).hasSize(3);
    }
}
