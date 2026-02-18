package com.chapman.edu.commissions.orm.processor;

import com.chapman.edu.commissions.orm.entity.*;
import com.chapman.edu.commissions.orm.repository.CommissionPlanRepository;
import com.chapman.edu.commissions.orm.repository.DealRepository;
import com.chapman.edu.commissions.orm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ============================================================
 * PROCESSOR: Entity Relationships, Mapping Strategies & DB Design
 * ============================================================
 *
 * This processor demonstrates JPA entity relationship types and
 * their database mapping strategies using the Commission Calculator domain.
 *
 * ============================================================
 * JPA RELATIONSHIP TYPES:
 * ============================================================
 *
 * 1. @OneToOne    - One entity maps to exactly one other entity
 *                   Example: User <-> UserProfile (if we had one)
 *                   FK Strategy: Either table can hold the FK
 *
 * 2. @OneToMany   - One entity maps to many child entities
 *                   Example: CommissionPlan -> List<CommissionRule>
 *                   FK Strategy: Child table holds FK to parent
 *                   Always the INVERSE (non-owning) side in bidirectional
 *
 * 3. @ManyToOne   - Many entities map to one parent entity
 *                   Example: CommissionRule -> CommissionPlan
 *                   FK Strategy: This entity's table holds the FK
 *                   Always the OWNING side in bidirectional
 *
 * 4. @ManyToMany  - Many entities map to many other entities
 *                   Example: User <-> Project (if we had one)
 *                   FK Strategy: Requires a JOIN TABLE
 *
 * 5. @ElementCollection - Collection of value types (not entities)
 *                   Example: User -> Set<UserRole>
 *                   FK Strategy: Separate collection table
 *
 * ============================================================
 * OWNING vs. INVERSE SIDE:
 * ============================================================
 * - OWNING SIDE: Has @JoinColumn, controls the FK column in the DB
 * - INVERSE SIDE: Has mappedBy attribute, read-only mirror
 * - JPA ONLY persists changes made on the OWNING side
 * - Always set BOTH sides for in-memory consistency
 *
 * ============================================================
 * CASCADE TYPES:
 * ============================================================
 * - PERSIST: Save parent -> save children
 * - MERGE: Update parent -> update children
 * - REMOVE: Delete parent -> delete children
 * - REFRESH: Refresh parent -> refresh children
 * - DETACH: Detach parent -> detach children
 * - ALL: All of the above
 *
 * ============================================================
 * FETCH STRATEGIES:
 * ============================================================
 * - LAZY (default for collections): Load on first access
 * - EAGER (default for @ManyToOne/@OneToOne): Load immediately
 * - Best Practice: Use LAZY everywhere, then JOIN FETCH when needed
 */
@Component
@Order(2)
public class OrmProcessor implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(OrmProcessor.class);

    private final UserRepository userRepository;
    private final DealRepository dealRepository;
    private final CommissionPlanRepository planRepository;

    public OrmProcessor(UserRepository userRepository,
                        DealRepository dealRepository,
                        CommissionPlanRepository planRepository) {
        this.userRepository = userRepository;
        this.dealRepository = dealRepository;
        this.planRepository = planRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void run(String... args) {
        log.info("============================================================");
        log.info("ORM PROCESSOR: Entity Relationships & Mapping Strategies");
        log.info("============================================================");

        demonstrateElementCollection();
        demonstrateSelfReferentialRelationship();
        demonstrateOneToManyWithCascade();
        demonstrateAggregatePattern();

        log.info("============================================================");
        log.info("ORM PROCESSOR: Complete");
        log.info("============================================================");
    }

    private void demonstrateElementCollection() {
        log.info("");
        log.info("--- @ElementCollection: User -> Set<UserRole> ---");
        log.info("Stores value types (enums, strings) in a separate collection table.");
        log.info("Table 'user_roles' has columns: user_id (FK), role (enum as STRING).");

        userRepository.findByUsername("mgarcia").ifPresent(user -> {
            log.info("User: {} has roles: {}", user.getFullName(), user.getRoles());
            log.info("  isSalesManager: {}", user.isSalesManager());
            log.info("  isFinanceAdmin: {}", user.isFinanceAdmin());
            log.info("  Roles are fetched EAGERLY (loaded with the user query).");
        });
    }

    private void demonstrateSelfReferentialRelationship() {
        log.info("");
        log.info("--- Self-Referential Relationship: User -> manager (User) ---");
        log.info("A user's manager_id FK points back to the users table itself.");
        log.info("This models organizational hierarchies without a separate table.");

        userRepository.findByUsername("jsmith").ifPresent(user -> {
            log.info("User: {}", user.getFullName());
            User manager = user.getManager();
            if (manager != null) {
                log.info("  Manager: {} (loaded via LAZY @ManyToOne)", manager.getFullName());
            }
        });

        // Show the inverse side
        userRepository.findByUsername("mgarcia").ifPresent(manager -> {
            List<User> reports = userRepository.findDirectReportsByManagerId(manager.getId());
            log.info("Manager {} has {} direct reports:", manager.getFullName(), reports.size());
            reports.forEach(r -> log.info("  - {}", r.getFullName()));
        });
    }

    private void demonstrateOneToManyWithCascade() {
        log.info("");
        log.info("--- @OneToMany with CascadeType.ALL: Deal -> List<DealProduct> ---");
        log.info("CascadeType.ALL: All operations on Deal cascade to DealProducts.");
        log.info("orphanRemoval=true: Removing a product from the list deletes it from DB.");

        dealRepository.findWithProductsAndSalesRepById("deal-001").ifPresent(deal -> {
            log.info("Deal: '{}' (${}) has {} products:",
                    deal.getTitle(), deal.getValue(), deal.getProducts().size());
            deal.getProducts().forEach(p ->
                    log.info("  - {} x{} @ ${} each", p.getProductName(), p.getQuantity(), p.getPrice()));
            log.info("  Sales Rep: {} (loaded via @EntityGraph)", deal.getSalesRep().getFullName());
            log.info("  Total product value: ${}", deal.calculateTotalValue());
        });
    }

    private void demonstrateAggregatePattern() {
        log.info("");
        log.info("--- Aggregate Root Pattern: CommissionPlan ---");
        log.info("CommissionPlan is the aggregate root owning rules, tiers, and bonuses.");
        log.info("All modifications go through the root; children cascade automatically.");

        // Load plan with rules (step 1: fetch rules)
        planRepository.findByIdWithRules("plan-001").ifPresent(plan -> {
            log.info("Plan: '{}' (Status: {})", plan.getName(), plan.getStatus());
            log.info("  Rules ({}):", plan.getRules().size());
            plan.getRules().forEach(rule ->
                    log.info("    - {} (type={}, rate={}%, priority={})",
                            rule.getName(), rule.getType(), rule.getRate(), rule.getPriority()));
        });

        // Load rules with conditions separately (step 2: avoids MultipleBagFetchException)
        var rulesWithConditions = planRepository.findRulesWithConditionsByPlanId("plan-001");
        log.info("  Rules with conditions (fetched separately to avoid MultipleBagFetchException):");
        rulesWithConditions.forEach(rule -> {
            rule.getConditions().forEach(cond ->
                    log.info("    Rule '{}' condition: {} {} {}",
                            rule.getName(), cond.getField(), cond.getOperator(), cond.getValue()));
        });

        // Load plan with tiers
        planRepository.findByIdWithTiers("plan-001").ifPresent(plan -> {
            log.info("  Tiers ({}):", plan.getTiers().size());
            plan.getTiers().forEach(tier ->
                    log.info("    - {}: ${} - ${} @ {}%",
                            tier.getName(), tier.getLowerBound(),
                            tier.getUpperBound() != null ? tier.getUpperBound() : "unlimited",
                            tier.getRate()));
        });

        // Load plan with bonuses
        planRepository.findByIdWithBonuses("plan-001").ifPresent(plan -> {
            log.info("  Bonuses ({}):", plan.getBonuses().size());
            plan.getBonuses().forEach(bonus ->
                    log.info("    - {} (type={}, amount={}, percentage={})",
                            bonus.getName(), bonus.getType(), bonus.getAmount(), bonus.isPercentage()));
        });
    }
}
