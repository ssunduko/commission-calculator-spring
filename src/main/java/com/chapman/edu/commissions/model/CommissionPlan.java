package com.chapman.edu.commissions.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * Represents a commission plan in the system.
 * Commission plans define the rules and tiers for calculating commissions.
 */
public class CommissionPlan {
    private String id;
    private String name;
    private List<CommissionRule> rules;
    private List<CommissionTier> tiers;
    private List<BonusRule> bonuses;
    private Currency currency;
    private PlanStatus status;
    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
    private LocalDate createdDate;
    private LocalDate lastModifiedDate;
    private String createdBy;
    
    /**
     * Default constructor
     */
    public CommissionPlan() {
        this.rules = new ArrayList<>();
        this.tiers = new ArrayList<>();
        this.bonuses = new ArrayList<>();
        this.createdDate = LocalDate.now();
        this.lastModifiedDate = LocalDate.now();
        this.status = PlanStatus.DRAFT;
    }
    
    /**
     * Constructor with essential fields
     */
    public CommissionPlan(String name, Currency currency) {
        this();
        this.name = name;
        this.currency = currency;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<CommissionRule> getRules() {
        return rules;
    }
    
    public void setRules(List<CommissionRule> rules) {
        this.rules = rules;
    }
    
    public void addRule(CommissionRule rule) {
        this.rules.add(rule);
    }
    
    public List<CommissionTier> getTiers() {
        return tiers;
    }
    
    public void setTiers(List<CommissionTier> tiers) {
        this.tiers = tiers;
    }
    
    public void addTier(CommissionTier tier) {
        this.tiers.add(tier);
    }
    
    public List<BonusRule> getBonuses() {
        return bonuses;
    }
    
    public void setBonuses(List<BonusRule> bonuses) {
        this.bonuses = bonuses;
    }
    
    public void addBonus(BonusRule bonus) {
        this.bonuses.add(bonus);
    }
    
    public Currency getCurrency() {
        return currency;
    }
    
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
    
    public PlanStatus getStatus() {
        return status;
    }
    
    public void setStatus(PlanStatus status) {
        this.status = status;
        this.lastModifiedDate = LocalDate.now();
    }
    
    public LocalDate getEffectiveStartDate() {
        return effectiveStartDate;
    }
    
    public void setEffectiveStartDate(LocalDate effectiveStartDate) {
        this.effectiveStartDate = effectiveStartDate;
    }
    
    public LocalDate getEffectiveEndDate() {
        return effectiveEndDate;
    }
    
    public void setEffectiveEndDate(LocalDate effectiveEndDate) {
        this.effectiveEndDate = effectiveEndDate;
    }
    
    public LocalDate getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDate getLastModifiedDate() {
        return lastModifiedDate;
    }
    
    public void setLastModifiedDate(LocalDate lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    /**
     * Check if the plan is active on a given date
     * @param date the date to check
     * @return true if the plan is active on the given date
     */
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
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
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