package com.chapman.edu.commissions.architecture.microservice.common.dto;

import java.time.LocalDate;

public record CreatePlanRequest(String name, String currencyCode, LocalDate effectiveStartDate, LocalDate effectiveEndDate) {
    public void validate() {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required");
        if (currencyCode == null || currencyCode.isBlank()) throw new IllegalArgumentException("Currency code is required");
    }
}
