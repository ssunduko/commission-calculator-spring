package com.chapman.edu.commissions.corespring.di;

import com.chapman.edu.commissions.model.Deal;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Rule engine for commission calculations.
 * Demonstrates @Component stereotype annotation.
 */
@Component
public class CommissionRuleEngine {

    public BigDecimal calculateBaseCommission(Deal deal, String planId) {
        // Simplified logic: 10% of deal value
        return deal.getValue().multiply(new BigDecimal("0.10"));
    }

    public BigDecimal calculateBonuses(Deal deal, String planId) {
        // Simplified logic: 5% bonus if deal value > $10,000
        if (deal.getValue().compareTo(new BigDecimal("10000")) > 0) {
            return deal.getValue().multiply(new BigDecimal("0.05"));
        }
        return BigDecimal.ZERO;
    }
}
