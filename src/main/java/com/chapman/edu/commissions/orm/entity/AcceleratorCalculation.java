package com.chapman.edu.commissions.orm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ============================================================
 * JPA ENTITY: AcceleratorCalculation
 * ============================================================
 *
 * ENTITY RELATIONSHIPS DEMONSTRATED:
 * - @ManyToOne to CommissionCalculation: Each accelerator is part of one calculation.
 *
 * Accelerators apply multipliers to commission amounts.
 * For example, a 1.5x accelerator on a $1000 base commission
 * results in $1500.
 */
@Entity
@Table(name = "accelerator_calculations", indexes = {
        @Index(name = "idx_ac_calc_id", columnList = "commission_calculation_id"),
        @Index(name = "idx_ac_rule_id", columnList = "rule_id")
})
@Data
@NoArgsConstructor
public class AcceleratorCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "rule_id")
    private String ruleId;

    @Column(name = "rule_name")
    private String ruleName;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal multiplier = BigDecimal.ONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commission_calculation_id")
    @JsonIgnore
    private CommissionCalculation commissionCalculation;

    @Column(length = 500)
    private String description;

    public AcceleratorCalculation(String ruleId, String ruleName, BigDecimal multiplier) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.multiplier = multiplier;
    }

    public BigDecimal applyTo(BigDecimal baseAmount) {
        return baseAmount.multiply(multiplier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AcceleratorCalculation that = (AcceleratorCalculation) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "AcceleratorCalculation{" +
                "id='" + id + '\'' +
                ", ruleName='" + ruleName + '\'' +
                ", multiplier=" + multiplier +
                '}';
    }
}
