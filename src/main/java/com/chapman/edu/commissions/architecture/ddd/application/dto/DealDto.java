package com.chapman.edu.commissions.architecture.ddd.application.dto;

import com.chapman.edu.commissions.architecture.ddd.domain.deal.Deal;
import com.chapman.edu.commissions.architecture.ddd.domain.deal.DealStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for deal information.
 */
public record DealDto(
    String id,
    String title,
    BigDecimal value,
    DealStatus status,
    String salesRepId,
    LocalDate closeDate,
    LocalDate createdDate
) {
    public static DealDto fromEntity(Deal deal) {
        return new DealDto(
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
