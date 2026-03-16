package com.chapman.edu.commissions.architecture.microservice.common.dto;

import java.math.BigDecimal;

public record CreateDealRequest(String title, BigDecimal value, String salesRepId) {
    public void validate() {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title is required");
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Value must be > 0");
        if (salesRepId == null || salesRepId.isBlank()) throw new IllegalArgumentException("Sales Rep ID is required");
    }
}
