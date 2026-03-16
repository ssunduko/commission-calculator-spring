package com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto;

import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionCalculation;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Result DTO representing a commission calculation.
 */
public record CalculationResult(
        String id,
        String dealId,
        String salesRepId,
        BigDecimal baseCommission,
        BigDecimal grossCommission,
        BigDecimal netCommission,
        String status,
        LocalDate calculationDate,
        String planId
) {

    public static CalculationResult from(CommissionCalculation calc) {
        return new CalculationResult(
                calc.getId(),
                calc.getDealId(),
                calc.getSalesRepId(),
                calc.getBaseCommission(),
                calc.getGrossCommission(),
                calc.getNetCommission(),
                calc.getStatus() != null ? calc.getStatus().name() : null,
                calc.getCalculationDate(),
                calc.getPlanId()
        );
    }
}
