package com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing a bonus rule within a commission plan.
 */
@Entity
@Table(name = "bonus_rules")
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

    public BonusRule() {
        this.conditions = new ArrayList<>();
    }

    public BonusRule(String name, BigDecimal amount, boolean isPercentage, BonusType type) {
        this();
        this.name = name;
        this.amount = amount;
        this.isPercentage = isPercentage;
        this.type = type;
    }

    public void addCondition(RuleCondition condition) {
        this.conditions.add(condition);
    }

    public boolean isActiveOn(LocalDate date) {
        boolean afterStart = startDate == null || !date.isBefore(startDate);
        boolean beforeEnd = endDate == null || !date.isAfter(endDate);
        return afterStart && beforeEnd;
    }

    public BigDecimal calculateBonus(BigDecimal baseAmount) {
        if (isPercentage) {
            return baseAmount.multiply(amount.divide(new BigDecimal("100")));
        } else {
            return amount;
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public boolean isPercentage() { return isPercentage; }
    public void setPercentage(boolean isPercentage) { this.isPercentage = isPercentage; }
    public List<RuleCondition> getConditions() { return conditions; }
    public void setConditions(List<RuleCondition> conditions) { this.conditions = conditions; }
    public BonusType getType() { return type; }
    public void setType(BonusType type) { this.type = type; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
}
