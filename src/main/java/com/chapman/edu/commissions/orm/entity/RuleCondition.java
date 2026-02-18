package com.chapman.edu.commissions.orm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================
 * JPA ENTITY: RuleCondition
 * ============================================================
 *
 * ENTITY RELATIONSHIPS DEMONSTRATED:
 * - @ManyToOne: Many conditions belong to one rule.
 *   This entity is a leaf in the aggregate hierarchy:
 *   CommissionPlan -> CommissionRule -> RuleCondition
 *
 * ENUM MAPPING:
 * Both ConditionOperator and LogicalOperator are stored as strings
 * using @Enumerated(EnumType.STRING). This is critical for maintainability:
 *
 * EnumType.STRING:  Stores "GREATER_THAN" in the DB column
 * EnumType.ORDINAL: Stores 3 (the enum's position) in the DB column
 *
 * ORDINAL IS DANGEROUS because:
 *   1. Reordering enum constants changes all stored values
 *   2. Removing an enum constant shifts all subsequent ordinals
 *   3. Debugging is harder (what does "3" mean?)
 */
@Entity
@Table(name = "rule_conditions", indexes = {
        @Index(name = "idx_rc_rule_id", columnList = "rule_id")
})
@Data
@NoArgsConstructor
public class RuleCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String field;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionOperator operator;

    @Column(name = "condition_value", nullable = false)
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "logical_operator", nullable = false)
    private LogicalOperator logicalOperator = LogicalOperator.AND;

    /**
     * @ManyToOne: Many conditions belong to one CommissionRule.
     * This is the owning side (controls the FK column 'rule_id').
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    @JsonIgnore
    private CommissionRule rule;

    public RuleCondition(String field, ConditionOperator operator, String value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
        this.logicalOperator = LogicalOperator.AND;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleCondition that = (RuleCondition) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "RuleCondition{" +
                "field='" + field + '\'' +
                ", operator=" + operator +
                ", value='" + value + '\'' +
                ", logicalOperator=" + logicalOperator +
                '}';
    }
}
