package com.chapman.edu.commissions.verticalslice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents a commission tier in the system.
 * Commission tiers define different commission rates based on deal value thresholds.
 */
@Entity
@Table(name = "commission_tiers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommissionTier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "lower_bound", nullable = false, precision = 19, scale = 2)
    private BigDecimal lowerBound = BigDecimal.ZERO;

    @Column(name = "upper_bound", precision = 19, scale = 2)
    private BigDecimal upperBound;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal rate = BigDecimal.ZERO;

    @Column(name = "is_percentage", nullable = false)
    private boolean isPercentage = true;

    @Column(name = "plan_id")
    private String planId;

    /**
     * Constructor with essential fields
     */
    public CommissionTier(String name, BigDecimal lowerBound, BigDecimal upperBound, BigDecimal rate) {
        this.name = name;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.rate = rate;
        this.isPercentage = true;
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
}