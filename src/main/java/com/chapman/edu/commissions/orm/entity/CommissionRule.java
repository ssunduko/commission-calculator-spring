package com.chapman.edu.commissions.orm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * JPA ENTITY: CommissionRule
 * ============================================================
 *
 * ENTITY RELATIONSHIPS DEMONSTRATED:
 * - @ManyToOne: Many rules belong to one commission plan.
 *   This is the owning side of the CommissionPlan-CommissionRule relationship.
 *
 * - @OneToMany with CascadeType.ALL: A rule has many conditions.
 *   Conditions are fully managed by the rule (aggregate child pattern).
 *
 * MAPPING STRATEGY - FOREIGN KEY vs. JOIN TABLE:
 * There are two ways to map @OneToMany:
 * 1. Foreign Key (used here): The child table has a FK column (plan_id)
 *    This is more efficient and is the recommended approach.
 * 2. Join Table: A third table maps the relationship
 *    Used when you can't add a FK column to the child table.
 *
 * We use Foreign Key mapping (via @JoinColumn on @ManyToOne) here.
 */
@Entity
@Table(name = "commission_rules", indexes = {
        @Index(name = "idx_rule_plan_id", columnList = "plan_id"),
        @Index(name = "idx_rule_type", columnList = "type"),
        @Index(name = "idx_rule_priority", columnList = "priority")
})
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

    /**
     * @OneToMany: A rule can have many conditions.
     * CascadeType.ALL + orphanRemoval: Conditions are owned by this rule.
     */
    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<RuleCondition> conditions = new ArrayList<>();

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal rate = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType type = RuleType.STANDARD;

    @Column(nullable = false)
    private int priority = 0;

    /**
     * @ManyToOne: The owning side. This field controls the plan_id FK column.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    @JsonIgnore
    private CommissionPlan plan;

    public CommissionRule(String name, BigDecimal rate, RuleType type) {
        this.name = name;
        this.rate = rate;
        this.type = type;
        this.conditions = new ArrayList<>();
        this.priority = 0;
    }

    public void addCondition(RuleCondition condition) {
        conditions.add(condition);
        condition.setRule(this);
    }

    public void removeCondition(RuleCondition condition) {
        conditions.remove(condition);
        condition.setRule(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommissionRule that = (CommissionRule) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CommissionRule{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", rate=" + rate +
                ", type=" + type +
                ", priority=" + priority +
                '}';
    }
}
