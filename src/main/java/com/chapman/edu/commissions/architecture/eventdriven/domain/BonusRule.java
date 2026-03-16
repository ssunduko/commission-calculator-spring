package com.chapman.edu.commissions.architecture.eventdriven.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bonus rule in the system.
 * Bonus rules define special incentives that can be applied to commissions.
 */
@Entity
@Table(name = "bonus_rules")
@Data
@NoArgsConstructor
public class BonusRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "is_percentage", nullable = false)
    private boolean isPercentage = false;

    @Transient
    private List<RuleCondition> conditions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BonusType type = BonusType.FIXED;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "plan_id")
    private String planId;

    /**
     * Constructor with essential fields
     */
    public BonusRule(String name, BigDecimal amount, boolean isPercentage, BonusType type) {
        this.name = name;
        this.amount = amount;
        this.isPercentage = isPercentage;
        this.type = type;
        this.conditions = new ArrayList<>();
    }

    public void addCondition(RuleCondition condition) {
        this.conditions.add(condition);
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
}
