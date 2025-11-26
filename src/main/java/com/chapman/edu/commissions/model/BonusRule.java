package com.chapman.edu.commissions.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a bonus rule in the system.
 * Bonus rules define special incentives that can be applied to commissions.
 */
public class BonusRule {
    private String id;
    private String name;
    private String description;
    private BigDecimal amount;
    private boolean isPercentage;
    private List<RuleCondition> conditions;
    private BonusType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String planId;
    
    /**
     * Default constructor
     */
    public BonusRule() {
        this.conditions = new ArrayList<>();
        this.amount = BigDecimal.ZERO;
        this.isPercentage = false;
        this.type = BonusType.FIXED;
    }
    
    /**
     * Constructor with essential fields
     */
    public BonusRule(String name, BigDecimal amount, boolean isPercentage, BonusType type) {
        this();
        this.name = name;
        this.amount = amount;
        this.isPercentage = isPercentage;
        this.type = type;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public boolean isPercentage() {
        return isPercentage;
    }
    
    public void setPercentage(boolean percentage) {
        isPercentage = percentage;
    }
    
    public List<RuleCondition> getConditions() {
        return conditions;
    }
    
    public void setConditions(List<RuleCondition> conditions) {
        this.conditions = conditions;
    }
    
    public void addCondition(RuleCondition condition) {
        this.conditions.add(condition);
    }
    
    public BonusType getType() {
        return type;
    }
    
    public void setType(BonusType type) {
        this.type = type;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public String getPlanId() {
        return planId;
    }
    
    public void setPlanId(String planId) {
        this.planId = planId;
    }
    
    /**
     * Check if the bonus is active on a given date
     * @param date the date to check
     * @return true if the bonus is active on the given date
     */
    public boolean isActiveOn(LocalDate date) {
        boolean afterStart = startDate == null || !date.isBefore(startDate);
        boolean beforeEnd = endDate == null || !date.isAfter(endDate);
        
        return afterStart && beforeEnd;
    }
    
    /**
     * Calculate the bonus amount for a given base amount
     * @param baseAmount the base amount to calculate bonus for
     * @return the bonus amount
     */
    public BigDecimal calculateBonus(BigDecimal baseAmount) {
        if (isPercentage) {
            return baseAmount.multiply(amount.divide(new BigDecimal("100")));
        } else {
            return amount;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BonusRule bonusRule = (BonusRule) o;
        return Objects.equals(id, bonusRule.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "BonusRule{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", isPercentage=" + isPercentage +
                ", type=" + type +
                '}';
    }
    
    /**
     * Enum representing the types of bonuses.
     */
    public enum BonusType {
        FIXED("Fixed"),
        SPIF("SPIF"),
        ACCELERATOR("Accelerator"),
        QUOTA_ACHIEVEMENT("Quota Achievement"),
        TEAM_PERFORMANCE("Team Performance"),
        SPECIAL_INCENTIVE("Special Incentive");
        
        private final String displayName;
        
        BonusType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
}