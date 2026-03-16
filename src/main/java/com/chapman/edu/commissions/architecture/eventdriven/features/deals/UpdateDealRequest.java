package com.chapman.edu.commissions.architecture.eventdriven.features.deals;

import com.chapman.edu.commissions.architecture.eventdriven.domain.DealStatus;
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
