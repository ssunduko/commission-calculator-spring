package com.chapman.edu.commissions.architecture.orthogonal.features.deals.commands;

import com.chapman.edu.commissions.architecture.orthogonal.features.deals.DealResponse;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Command;

import java.math.BigDecimal;

/**
 * Command to create a new deal.
 * Commands are simple data carriers — they describe WHAT to do, not HOW.
 */
public record CreateDealCommand(
    String title,
    BigDecimal value,
    String salesRepId
) implements Command<DealResponse> {

    public void validate() {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Value must be greater than zero");
        }
        if (salesRepId == null || salesRepId.isBlank()) {
            throw new IllegalArgumentException("Sales Rep ID is required");
        }
    }
}
