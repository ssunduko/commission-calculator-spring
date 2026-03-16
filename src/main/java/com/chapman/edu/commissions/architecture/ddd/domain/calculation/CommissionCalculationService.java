package com.chapman.edu.commissions.architecture.ddd.domain.calculation;

import com.chapman.edu.commissions.architecture.ddd.domain.deal.Deal;
import com.chapman.edu.commissions.architecture.ddd.domain.plan.CommissionPlan;
import com.chapman.edu.commissions.architecture.ddd.domain.plan.CommissionRule;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * CONCEPT: Domain Service (DDD)
 *
 * A Domain Service contains business logic that doesn't naturally belong
 * to any single aggregate. Commission calculation spans the Deal and
 * CommissionPlan aggregates — neither should know about the other's internals.
 *
 * The Domain Service coordinates between aggregates while keeping
 * the calculation logic in the domain layer (not the application layer).
 *
 * Domain Services are STATELESS — they have no fields or injected
 * dependencies. They operate purely on domain objects passed as arguments.
 */
public class CommissionCalculationService {

    private CommissionCalculationService() {} // Utility class

    /**
     * Calculates the base commission for a deal using a plan's rules.
     * This logic spans two aggregates (Deal + CommissionPlan) so it
     * lives in a Domain Service rather than in either aggregate.
     */
    public static BigDecimal calculateBaseCommission(Deal deal, CommissionPlan plan) {
        if (plan.getRules().isEmpty()) {
            return BigDecimal.ZERO;
        }

        CommissionRule rule = plan.getRules().get(0);
        BigDecimal dealValue = deal.getValue() != null ? deal.getValue() : BigDecimal.ZERO;

        return dealValue
                .multiply(rule.getRate())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
