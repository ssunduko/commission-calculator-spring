package com.chapman.edu.commissions.springboot.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for Commission Calculation data.
 */
@Data
public class CommissionCalculationResponse {
    private String id;
    private String dealId;
    private String salesRepId;
    private BigDecimal baseCommission;
    private BigDecimal grossCommission;
    private BigDecimal netCommission;
    private String status;
    private LocalDate calculationDate;
    private LocalDate payoutDate;
    private String planId;
    private String calculatedBy;
    private int bonusCount;
    private int acceleratorCount;
}

