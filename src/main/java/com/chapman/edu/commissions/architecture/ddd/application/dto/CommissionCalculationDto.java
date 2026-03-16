package com.chapman.edu.commissions.architecture.ddd.application.dto;

import com.chapman.edu.commissions.architecture.ddd.domain.calculation.CommissionCalculation;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for commission calculation information.
 */
public record CommissionCalculationDto(
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
    public static CommissionCalculationDto fromEntity(CommissionCalculation calculation) {
        return new CommissionCalculationDto(
            calculation.getId(),
            calculation.getDealId(),
            calculation.getSalesRepId(),
            calculation.getBaseCommission(),
            calculation.getGrossCommission(),
            calculation.getNetCommission(),
            calculation.getStatus().name(),
            calculation.getCalculationDate(),
            calculation.getPlanId()
        );
    }
}
