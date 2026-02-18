package com.chapman.edu.commissions.orm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ============================================================
 * JPA ENTITY: BonusCalculation
 * ============================================================
 *
 * ENTITY RELATIONSHIPS DEMONSTRATED:
 * - @ManyToOne to CommissionCalculation: Each bonus is part of one calculation.
 *
 * This is a child entity in the CommissionCalculation aggregate.
 * Its lifecycle is fully managed by the parent (CascadeType.ALL + orphanRemoval).
 */
@Entity
@Table(name = "bonus_calculations", indexes = {
        @Index(name = "idx_bc_calc_id", columnList = "commission_calculation_id"),
        @Index(name = "idx_bc_rule_id", columnList = "bonus_rule_id")
})
@Data
@NoArgsConstructor
public class BonusCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "bonus_rule_id")
    private String bonusRuleId;

    @Column(name = "bonus_name")
    private String bonusName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commission_calculation_id")
    @JsonIgnore
    private CommissionCalculation commissionCalculation;

    @Column(length = 500)
    private String description;

    public BonusCalculation(String bonusRuleId, String bonusName, BigDecimal amount) {
        this.bonusRuleId = bonusRuleId;
        this.bonusName = bonusName;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BonusCalculation that = (BonusCalculation) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "BonusCalculation{" +
                "id='" + id + '\'' +
                ", bonusName='" + bonusName + '\'' +
                ", amount=" + amount +
                '}';
    }
}
