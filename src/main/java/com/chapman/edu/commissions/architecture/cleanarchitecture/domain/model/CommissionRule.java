package com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing a rule within a commission plan.
 */
@Entity
@Table(name = "commission_rules")
public class CommissionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal rate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType type = RuleType.STANDARD;

    @Column(nullable = false)
    private int priority;

    @Column(name = "plan_id")
    private String planId;

    @Transient
    private List<RuleCondition> conditions = new ArrayList<>();

    public CommissionRule() {
        this.conditions = new ArrayList<>();
    }

    public CommissionRule(String name, BigDecimal rate, RuleType type) {
        this();
        this.name = name;
        this.rate = rate;
        this.type = type;
        this.priority = 0;
    }

    public void addCondition(RuleCondition condition) {
        this.conditions.add(condition);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public RuleType getType() { return type; }
    public void setType(RuleType type) { this.type = type; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public List<RuleCondition> getConditions() { return conditions; }
    public void setConditions(List<RuleCondition> conditions) { this.conditions = conditions; }
}
