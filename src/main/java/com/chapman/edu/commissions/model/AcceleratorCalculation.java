package com.chapman.edu.commissions.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents an accelerator calculation within a commission calculation.
 * Accelerator calculations store the results of applying accelerator rules to deals.
 */
public class AcceleratorCalculation {
    private String id;
    private String ruleId;
    private String ruleName;
    private BigDecimal multiplier;
    private String commissionCalculationId;
    private String description;
    
    /**
     * Default constructor
     */
    public AcceleratorCalculation() {
        this.multiplier = BigDecimal.ONE;
    }
    
    /**
     * Constructor with essential fields
     */
    public AcceleratorCalculation(String ruleId, String ruleName, BigDecimal multiplier) {
        this();
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.multiplier = multiplier;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getRuleId() {
        return ruleId;
    }
    
    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }
    
    public String getRuleName() {
        return ruleName;
    }
    
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
    
    public BigDecimal getMultiplier() {
        return multiplier;
    }
    
    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
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
    
    /**
     * Apply this accelerator to a base amount
     * @param baseAmount the base amount to apply the accelerator to
     * @return the accelerated amount
     */
    public BigDecimal applyTo(BigDecimal baseAmount) {
        return baseAmount.multiply(multiplier);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AcceleratorCalculation that = (AcceleratorCalculation) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "AcceleratorCalculation{" +
                "id='" + id + '\'' +
                ", ruleName='" + ruleName + '\'' +
                ", multiplier=" + multiplier +
                '}';
    }
}