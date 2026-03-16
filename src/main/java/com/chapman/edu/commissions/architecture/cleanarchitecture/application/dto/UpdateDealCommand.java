package com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto;

import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DealStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command DTO for updating an existing deal. All fields are nullable.
 */
public record UpdateDealCommand(String title, BigDecimal value, DealStatus status, LocalDate closeDate) {
}
