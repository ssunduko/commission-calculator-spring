package com.chapman.edu.commissions.architecture.ddd.application.dto;

import com.chapman.edu.commissions.architecture.ddd.domain.deal.DealStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for updating an existing deal.
 */
public record UpdateDealRequest(
    String title,
    BigDecimal value,
    DealStatus status,
    LocalDate closeDate
) {
}
