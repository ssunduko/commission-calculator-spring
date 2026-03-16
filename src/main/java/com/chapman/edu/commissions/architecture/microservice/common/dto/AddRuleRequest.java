package com.chapman.edu.commissions.architecture.microservice.common.dto;

import java.math.BigDecimal;

public record AddRuleRequest(String name, String description, BigDecimal rate, String ruleType, int priority) {
    public void validate() {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Rule name is required");
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Rate must be >= 0");
    }
}
