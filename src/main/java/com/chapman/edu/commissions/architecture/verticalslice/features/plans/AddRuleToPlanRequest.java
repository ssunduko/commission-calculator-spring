package com.chapman.edu.commissions.architecture.verticalslice.features.plans;

import java.math.BigDecimal;

/**
 * Request DTO for adding a rule to a commission plan.
 *
 * @param rate commission rate as a whole-number percentage (e.g. {@code 5} for 5%,
 *             {@code 10.5} for 10.5%). The calculation engine divides by 100 internally;
 *             do not pass decimal multipliers like {@code 0.05}.
 */
public record AddRuleToPlanRequest(
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
