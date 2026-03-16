package com.chapman.edu.commissions.architecture.verticalslice.features.calculations;

import com.chapman.edu.commissions.architecture.verticalslice.domain.CommissionCalculation;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for commission calculation information.
 */
public record CommissionCalculationResponse(
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
    public static CommissionCalculationResponse from(CommissionCalculation calculation) {
        return new CommissionCalculationResponse(
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
