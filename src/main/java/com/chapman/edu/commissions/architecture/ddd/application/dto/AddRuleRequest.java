package com.chapman.edu.commissions.architecture.ddd.application.dto;

import java.math.BigDecimal;

/**
 * Request DTO for adding a rule to a commission plan.
 */
public record AddRuleRequest(
    String name,
    String description,
    BigDecimal rate,
    String ruleType,
    int priority
) {
    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Rule name is required");
        }
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Rule rate must be non-negative");
        }
    }
}
