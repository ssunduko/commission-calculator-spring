package com.chapman.edu.commissions.architecture.eventdriven.domain.event;

/**
 * Published when a commission plan transitions to ACTIVE status.
 * This is significant because active plans can be used for calculations.
 */
public class CommissionPlanActivatedEvent extends DomainEvent {

    private final String planId;
    private final String planName;

    public CommissionPlanActivatedEvent(String planId, String planName) {
        super();
        this.planId = planId;
        this.planName = planName;
    }

    @Override public String getAggregateId() { return planId; }
    @Override public String getAggregateType() { return "CommissionPlan"; }

    public String getPlanId() { return planId; }
    public String getPlanName() { return planName; }
}
