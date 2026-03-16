package com.chapman.edu.commissions.architecture.microservice.common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CalculationDto(
    String id, String dealId, String salesRepId, String planId,
    BigDecimal baseCommission, BigDecimal grossCommission, BigDecimal netCommission,
    String status, LocalDate calculationDate
) {}
