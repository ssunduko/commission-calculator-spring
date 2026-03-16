package com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto;

import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.DomainException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.RuleType;

import java.math.BigDecimal;

/**
 * Command DTO for adding a rule to a commission plan.
 */
public record AddRuleCommand(
        String name,
        String description,
        BigDecimal rate,
        RuleType ruleType,
        int priority
) {

    public void validate() {
        if (name == null || name.isBlank()) {
            throw new DomainException("Rule name must not be blank");
        }
        if (rate == null) {
            throw new DomainException("Rule rate must not be null");
        }
    }
}
