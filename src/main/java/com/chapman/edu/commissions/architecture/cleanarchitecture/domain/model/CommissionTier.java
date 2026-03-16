package com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Domain entity representing a tier within a commission plan.
 */
@Entity
@Table(name = "commission_tiers")
public class CommissionTier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "lower_bound", nullable = false, precision = 19, scale = 2)
    private BigDecimal minValue;

    @Column(name = "upper_bound", precision = 19, scale = 2)
    private BigDecimal maxValue;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal rate;

    @Column(name = "plan_id")
    private String planId;

    public CommissionTier() {
    }

    public CommissionTier(String name, BigDecimal minValue, BigDecimal maxValue, BigDecimal rate) {
        this.name = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.rate = rate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getMinValue() { return minValue; }
    public void setMinValue(BigDecimal minValue) { this.minValue = minValue; }
    public BigDecimal getMaxValue() { return maxValue; }
    public void setMaxValue(BigDecimal maxValue) { this.maxValue = maxValue; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
}
