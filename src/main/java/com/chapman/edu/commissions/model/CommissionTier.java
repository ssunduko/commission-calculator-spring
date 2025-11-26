package com.chapman.edu.commissions.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a commission tier in the system.
 * Commission tiers define different commission rates based on deal value thresholds.
 */
public class CommissionTier {
    private String id;
    private String name;
    private BigDecimal lowerBound;
    private BigDecimal upperBound;
    private BigDecimal rate;
    private boolean isPercentage;
    private String planId;
    
    /**
     * Default constructor
     */
    public CommissionTier() {
        this.lowerBound = BigDecimal.ZERO;
        this.rate = BigDecimal.ZERO;
        this.isPercentage = true;
    }
    
    /**
     * Constructor with essential fields
     */
    public CommissionTier(String name, BigDecimal lowerBound, BigDecimal upperBound, BigDecimal rate) {
        this();
        this.name = name;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.rate = rate;
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
    
    public BigDecimal getLowerBound() {
        return lowerBound;
    }
    
    public void setLowerBound(BigDecimal lowerBound) {
        this.lowerBound = lowerBound;
    }
    
    public BigDecimal getUpperBound() {
        return upperBound;
    }
    
    public void setUpperBound(BigDecimal upperBound) {
        this.upperBound = upperBound;
    }
    
    public BigDecimal getRate() {
        return rate;
    }
    
    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
    
    public boolean isPercentage() {
        return isPercentage;
    }
    
    public void setPercentage(boolean percentage) {
        isPercentage = percentage;
    }
    
    public String getPlanId() {
        return planId;
    }
    
    public void setPlanId(String planId) {
        this.planId = planId;
    }
    
    /**
     * Check if a value falls within this tier's bounds
     * @param value the value to check
     * @return true if the value is within this tier's bounds
     */
    public boolean containsValue(BigDecimal value) {
        boolean aboveLower = lowerBound == null || value.compareTo(lowerBound) >= 0;
        boolean belowUpper = upperBound == null || value.compareTo(upperBound) < 0;
        
        return aboveLower && belowUpper;
    }
    
    /**
     * Calculate the commission amount for a given value within this tier
     * @param value the value to calculate commission for
     * @return the commission amount
     */
    public BigDecimal calculateCommission(BigDecimal value) {
        if (!containsValue(value)) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal effectiveValue = value.subtract(lowerBound);
        
        if (isPercentage) {
            return effectiveValue.multiply(rate.divide(new BigDecimal("100")));
        } else {
            return rate;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommissionTier that = (CommissionTier) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "CommissionTier{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", lowerBound=" + lowerBound +
                ", upperBound=" + upperBound +
                ", rate=" + rate +
                ", isPercentage=" + isPercentage +
                '}';
    }
}