package com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto;

import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.Deal;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Result DTO representing a deal.
 */
public record DealResult(
        String id,
        String title,
        BigDecimal value,
        String status,
        String salesRepId,
        LocalDate closeDate,
        LocalDate createdDate
) {

    public static DealResult from(Deal deal) {
        return new DealResult(
                deal.getId(),
                deal.getTitle(),
                deal.getValue(),
                deal.getStatus() != null ? deal.getStatus().name() : null,
                deal.getSalesRepId(),
                deal.getCloseDate(),
                deal.getCreatedDate()
        );
    }
}
