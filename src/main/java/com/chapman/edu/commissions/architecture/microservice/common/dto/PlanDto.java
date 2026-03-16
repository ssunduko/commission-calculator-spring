package com.chapman.edu.commissions.architecture.microservice.common.dto;

import java.time.LocalDate;

public record PlanDto(
    String id, String name, String currency, String status,
    LocalDate effectiveStartDate, LocalDate effectiveEndDate,
    int rulesCount, int tiersCount
) {}
