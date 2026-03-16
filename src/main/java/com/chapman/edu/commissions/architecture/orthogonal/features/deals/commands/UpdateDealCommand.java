package com.chapman.edu.commissions.architecture.orthogonal.features.deals.commands;

import com.chapman.edu.commissions.architecture.orthogonal.domain.DealStatus;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.DealResponse;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Command;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateDealCommand(
    String id,
    String title,
    BigDecimal value,
    DealStatus status,
    LocalDate closeDate
) implements Command<DealResponse> {
}
