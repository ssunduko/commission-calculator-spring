package com.chapman.edu.commissions.architecture.eventdriven.domain.event;

/**
 * Published when a new commission plan is created.
 * Listeners may initialize default tiers or notify administrators.
 */
public class CommissionPlanCreatedEvent extends DomainEvent {

    private final String planId;
    private final String planName;
    private final String currency;

    public CommissionPlanCreatedEvent(String planId, String planName, String currency) {
        super();
        this.planId = planId;
        this.planName = planName;
        this.currency = currency;
    }

    @Override public String getAggregateId() { return planId; }
    @Override public String getAggregateType() { return "CommissionPlan"; }

    public String getPlanId() { return planId; }
    public String getPlanName() { return planName; }
    public String getCurrency() { return currency; }
}
