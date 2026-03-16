package com.chapman.edu.commissions.architecture.orthogonal.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents a bonus calculation within a commission calculation.
 * Bonus calculations store the results of applying bonus rules to deals.
 */
@Entity
@Table(name = "bonus_calculations")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "commission_calculation_id")
    private String commissionCalculationId;

    @Column(length = 500)
    private String description;

    /**
     * Constructor with essential fields
     */
    public BonusCalculation(String bonusRuleId, String bonusName, BigDecimal amount) {
        this.bonusRuleId = bonusRuleId;
        this.bonusName = bonusName;
        this.amount = amount;
    }
}
