package com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

/**
 * Domain entity representing a commission plan with rules and tiers.
 */
@Entity
@Table(name = "commission_plans")
public class CommissionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanStatus status;

    @Column(name = "effective_start_date")
    private LocalDate effectiveStartDate;

    @Column(name = "effective_end_date")
    private LocalDate effectiveEndDate;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @Column(name = "last_modified_date")
    private LocalDate lastModifiedDate;

    @Column(name = "created_by")
    private String createdBy;

    @Transient
    private List<CommissionRule> rules;

    @Transient
    private List<CommissionTier> tiers;

    @Transient
    private List<BonusRule> bonusRules;

    public CommissionPlan() {
        this.status = PlanStatus.DRAFT;
        this.createdDate = LocalDate.now();
        this.lastModifiedDate = LocalDate.now();
        this.rules = new ArrayList<>();
        this.tiers = new ArrayList<>();
        this.bonusRules = new ArrayList<>();
    }

    public CommissionPlan(String name, Currency currency, LocalDate effectiveStartDate, LocalDate effectiveEndDate) {
        this();
        this.name = name;
        this.currency = currency.getCurrencyCode();
        this.effectiveStartDate = effectiveStartDate;
        this.effectiveEndDate = effectiveEndDate;
    }

    public void addRule(CommissionRule rule) {
        this.rules.add(rule);
    }

    public void addTier(CommissionTier tier) {
        this.tiers.add(tier);
    }

    public void addBonusRule(BonusRule bonusRule) {
        this.bonusRules.add(bonusRule);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Currency getCurrency() { return Currency.getInstance(this.currency); }
    public void setCurrency(Currency currency) { this.currency = currency.getCurrencyCode(); }
    public PlanStatus getStatus() { return status; }
    public void setStatus(PlanStatus status) { this.status = status; }
    public LocalDate getEffectiveStartDate() { return effectiveStartDate; }
    public void setEffectiveStartDate(LocalDate effectiveStartDate) { this.effectiveStartDate = effectiveStartDate; }
    public LocalDate getEffectiveEndDate() { return effectiveEndDate; }
    public void setEffectiveEndDate(LocalDate effectiveEndDate) { this.effectiveEndDate = effectiveEndDate; }
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    public LocalDate getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(LocalDate lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }
    public List<CommissionRule> getRules() { return rules; }
    public void setRules(List<CommissionRule> rules) { this.rules = rules; }
    public List<CommissionTier> getTiers() { return tiers; }
    public void setTiers(List<CommissionTier> tiers) { this.tiers = tiers; }
    public List<BonusRule> getBonusRules() { return bonusRules; }
    public void setBonusRules(List<BonusRule> bonusRules) { this.bonusRules = bonusRules; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
