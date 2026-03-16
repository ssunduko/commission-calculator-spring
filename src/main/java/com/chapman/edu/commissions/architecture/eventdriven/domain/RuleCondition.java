package com.chapman.edu.commissions.architecture.eventdriven.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a condition for a commission rule.
 * Rule conditions define when a commission rule should be applied.
 */
@Entity
@Table(name = "rule_conditions")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "rule_id")
    private String ruleId;

    /**
     * Constructor with essential fields
     */
    public RuleCondition(String field, ConditionOperator operator, String value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
        this.logicalOperator = LogicalOperator.AND;
    }
}
