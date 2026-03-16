package com.chapman.edu.commissions.architecture.microservice.calculationservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents an accelerator calculation within a commission calculation.
 * Accelerator calculations store the results of applying accelerator rules to deals.
 */
@Entity
@Table(name = "accelerator_calculations")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "commission_calculation_id")
    private String commissionCalculationId;

    @Column(length = 500)
    private String description;

    /**
     * Constructor with essential fields
     */
    public AcceleratorCalculation(String ruleId, String ruleName, BigDecimal multiplier) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.multiplier = multiplier;
    }

    /**
     * Apply this accelerator to a base amount
     * @param baseAmount the base amount to apply the accelerator to
     * @return the accelerated amount
     */
    public BigDecimal applyTo(BigDecimal baseAmount) {
        return baseAmount.multiply(multiplier);
    }
}
