package com.chapman.edu.commissions.verticalslice.features.deals;

import com.chapman.edu.commissions.verticalslice.domain.DealStatus;
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
