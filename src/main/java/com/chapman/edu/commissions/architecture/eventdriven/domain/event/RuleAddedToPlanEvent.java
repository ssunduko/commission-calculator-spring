package com.chapman.edu.commissions.architecture.eventdriven.domain.event;

import java.math.BigDecimal;

/**
 * Published when a commission rule is added to a plan.
 * Listeners may need to recalculate existing commissions under this plan.
 */
public class RuleAddedToPlanEvent extends DomainEvent {

    private final String planId;
    private final String ruleName;
    private final BigDecimal rate;
    private final String ruleType;

    public RuleAddedToPlanEvent(String planId, String ruleName, BigDecimal rate, String ruleType) {
        super();
        this.planId = planId;
        this.ruleName = ruleName;
        this.rate = rate;
        this.ruleType = ruleType;
    }

    @Override public String getAggregateId() { return planId; }
    @Override public String getAggregateType() { return "CommissionPlan"; }

    public String getPlanId() { return planId; }
    public String getRuleName() { return ruleName; }
    public BigDecimal getRate() { return rate; }
    public String getRuleType() { return ruleType; }
}
