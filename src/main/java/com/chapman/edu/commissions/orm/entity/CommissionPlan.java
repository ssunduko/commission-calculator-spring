package com.chapman.edu.commissions.orm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

/**
 * ============================================================
 * JPA ENTITY: CommissionPlan
 * ============================================================
 *
 * ENTITY RELATIONSHIPS DEMONSTRATED:
 * - @OneToMany with CascadeType.ALL: A plan owns its rules, tiers, and bonuses.
 *   When a plan is saved, all associated rules, tiers, and bonuses are automatically
 *   saved too. When a plan is deleted, they are all deleted.
 *
 * - orphanRemoval = true: If you remove a rule from the plan's rules list,
 *   that rule is automatically deleted from the database.
 *
 * AGGREGATE ROOT PATTERN:
 * CommissionPlan is an "aggregate root" - the entry point for a cluster of entities
 * (CommissionPlan, CommissionRule, CommissionTier, BonusRule) that form a logical unit.
 * All modifications to the aggregate go through the root entity.
 *
 * DATABASE DESIGN:
 * - commission_plans: Parent table
 * - commission_rules: Child table with FK plan_id -> commission_plans.id
 * - commission_tiers: Child table with FK plan_id -> commission_plans.id
 * - bonus_rules: Child table with FK plan_id -> commission_plans.id
 *
 * CURRENCY HANDLING:
 * The Currency field demonstrates custom type conversion. JPA stores the
 * 3-letter ISO currency code (e.g., "USD") as a VARCHAR, and the getter/setter
 * convert between String and java.util.Currency.
 */
@Entity
@Table(name = "commission_plans", indexes = {
        @Index(name = "idx_plan_status", columnList = "status"),
        @Index(name = "idx_plan_effective_dates", columnList = "effective_start_date, effective_end_date"),
        @Index(name = "idx_plan_created_by", columnList = "created_by")
})
@Data
@NoArgsConstructor
public class CommissionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    /**
     * @OneToMany with full cascade: Rules are owned by this plan.
     * All CRUD operations on the plan cascade to rules.
     *
     * ORDERING: @OrderBy sorts the collection by priority when loaded.
     * This translates to an ORDER BY clause in the SQL query.
     */
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("priority ASC")
    @JsonIgnore
    private List<CommissionRule> rules = new ArrayList<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("lowerBound ASC")
    @JsonIgnore
    private List<CommissionTier> tiers = new ArrayList<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<BonusRule> bonuses = new ArrayList<>();

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanStatus status = PlanStatus.DRAFT;

    @Column(name = "effective_start_date")
    private LocalDate effectiveStartDate;

    @Column(name = "effective_end_date")
    private LocalDate effectiveEndDate;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate = LocalDate.now();

    @Column(name = "last_modified_date")
    private LocalDate lastModifiedDate = LocalDate.now();

    @Column(name = "created_by")
    private String createdBy;

    public CommissionPlan(String name, Currency currency) {
        this.name = name;
        this.currency = currency.getCurrencyCode();
        this.rules = new ArrayList<>();
        this.tiers = new ArrayList<>();
        this.bonuses = new ArrayList<>();
        this.createdDate = LocalDate.now();
        this.lastModifiedDate = LocalDate.now();
        this.status = PlanStatus.DRAFT;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency.getCurrencyCode();
    }

    public Currency getCurrencyObject() {
        return Currency.getInstance(this.currency);
    }

    /**
     * Bidirectional relationship helper methods.
     * Always use these instead of directly modifying the collection.
     */
    public void addRule(CommissionRule rule) {
        rules.add(rule);
        rule.setPlan(this);
    }

    public void removeRule(CommissionRule rule) {
        rules.remove(rule);
        rule.setPlan(null);
    }

    public void addTier(CommissionTier tier) {
        tiers.add(tier);
        tier.setPlan(this);
    }

    public void removeTier(CommissionTier tier) {
        tiers.remove(tier);
        tier.setPlan(null);
    }

    public void addBonus(BonusRule bonus) {
        bonuses.add(bonus);
        bonus.setPlan(this);
    }

    public void removeBonus(BonusRule bonus) {
        bonuses.remove(bonus);
        bonus.setPlan(null);
    }

    public void setStatus(PlanStatus status) {
        this.status = status;
        this.lastModifiedDate = LocalDate.now();
    }

    public boolean isActiveOn(LocalDate date) {
        if (status != PlanStatus.ACTIVE) {
            return false;
        }
        boolean afterStart = effectiveStartDate == null || !date.isBefore(effectiveStartDate);
        boolean beforeEnd = effectiveEndDate == null || !date.isAfter(effectiveEndDate);
        return afterStart && beforeEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommissionPlan that = (CommissionPlan) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CommissionPlan{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", effectiveStartDate=" + effectiveStartDate +
                ", effectiveEndDate=" + effectiveEndDate +
                '}';
    }
}
