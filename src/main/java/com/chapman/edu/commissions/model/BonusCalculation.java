package com.chapman.edu.commissions.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a bonus calculation within a commission calculation.
 * Bonus calculations store the results of applying bonus rules to deals.
 */
public class BonusCalculation {
    private String id;
    private String bonusRuleId;
    private String bonusName;
    private BigDecimal amount;
    private String commissionCalculationId;
    private String description;
    
    /**
     * Default constructor
     */
    public BonusCalculation() {
        this.amount = BigDecimal.ZERO;
    }
    
    /**
     * Constructor with essential fields
     */
    public BonusCalculation(String bonusRuleId, String bonusName, BigDecimal amount) {
        this();
        this.bonusRuleId = bonusRuleId;
        this.bonusName = bonusName;
        this.amount = amount;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getBonusRuleId() {
        return bonusRuleId;
    }
    
    public void setBonusRuleId(String bonusRuleId) {
        this.bonusRuleId = bonusRuleId;
    }
    
    public String getBonusName() {
        return bonusName;
    }
    
    public void setBonusName(String bonusName) {
        this.bonusName = bonusName;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCommissionCalculationId() {
        return commissionCalculationId;
    }
    
    public void setCommissionCalculationId(String commissionCalculationId) {
        this.commissionCalculationId = commissionCalculationId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BonusCalculation that = (BonusCalculation) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "BonusCalculation{" +
                "id='" + id + '\'' +
                ", bonusName='" + bonusName + '\'' +
                ", amount=" + amount +
                '}';
    }
}