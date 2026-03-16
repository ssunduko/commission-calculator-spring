package com.chapman.edu.commissions.architecture.verticalslice.features.deals;

import com.chapman.edu.commissions.architecture.verticalslice.domain.Deal;
import com.chapman.edu.commissions.architecture.verticalslice.domain.DealStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for deal information.
 */
public record DealResponse(
    String id,
    String title,
    BigDecimal value,
    DealStatus status,
    String salesRepId,
    LocalDate closeDate,
    LocalDate createdDate
) {
    public static DealResponse from(Deal deal) {
        return new DealResponse(
            deal.getId(),
            deal.getTitle(),
            deal.getValue(),
            deal.getStatus(),
            deal.getSalesRepId(),
            deal.getCloseDate(),
            deal.getCreatedDate()
        );
    }
}
