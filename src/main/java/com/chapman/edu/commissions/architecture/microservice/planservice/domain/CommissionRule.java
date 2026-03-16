package com.chapman.edu.commissions.architecture.microservice.planservice.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a commission rule in the system.
 * Commission rules define the conditions and rates for calculating commissions.
 */
@Entity
@Table(name = "commission_rules")
@Data
@NoArgsConstructor
public class CommissionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Transient
    private List<RuleCondition> conditions = new ArrayList<>();

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal rate = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType type = RuleType.STANDARD;

    @Column(nullable = false)
    private int priority = 0;

    @Column(name = "plan_id")
    private String planId;

    /**
     * Constructor with essential fields
     */
    public CommissionRule(String name, BigDecimal rate, RuleType type) {
        this.name = name;
        this.rate = rate;
        this.type = type;
        this.conditions = new ArrayList<>();
        this.priority = 0;
    }

    public void addCondition(RuleCondition condition) {
        this.conditions.add(condition);
    }
}
