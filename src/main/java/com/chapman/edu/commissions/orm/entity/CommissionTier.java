package com.chapman.edu.commissions.orm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ============================================================
 * JPA ENTITY: CommissionTier
 * ============================================================
 *
 * ENTITY RELATIONSHIPS DEMONSTRATED:
 * - @ManyToOne: Many tiers belong to one commission plan.
 *
 * DATABASE DESIGN - TIERED RATE STRUCTURE:
 * Tiers implement a common business pattern: different rates for different
 * value ranges (similar to tax brackets). The database schema uses:
 *   - lower_bound: Minimum value for this tier
 *   - upper_bound: Maximum value (NULL means unlimited)
 *   - rate: The commission rate for deals in this range
 *   - is_percentage: Whether rate is a percentage or flat amount
 *
 * This design allows flexible tier configurations without code changes.
 */
@Entity
@Table(name = "commission_tiers", indexes = {
        @Index(name = "idx_tier_plan_id", columnList = "plan_id"),
        @Index(name = "idx_tier_bounds", columnList = "lower_bound, upper_bound")
})
@Data
@NoArgsConstructor
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    @JsonIgnore
    private CommissionPlan plan;

    public CommissionTier(String name, BigDecimal lowerBound, BigDecimal upperBound, BigDecimal rate) {
        this.name = name;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.rate = rate;
        this.isPercentage = true;
    }

    public boolean containsValue(BigDecimal value) {
        boolean aboveLower = lowerBound == null || value.compareTo(lowerBound) >= 0;
        boolean belowUpper = upperBound == null || value.compareTo(upperBound) < 0;
        return aboveLower && belowUpper;
    }

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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CommissionTier{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", lowerBound=" + lowerBound +
                ", upperBound=" + upperBound +
                ", rate=" + rate +
                '}';
    }
}
