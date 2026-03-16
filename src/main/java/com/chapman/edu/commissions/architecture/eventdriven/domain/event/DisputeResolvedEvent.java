package com.chapman.edu.commissions.architecture.eventdriven.domain.event;

/**
 * Published when a dispute is resolved (approved or rejected).
 * If approved, listeners may trigger commission recalculation.
 */
public class DisputeResolvedEvent extends DomainEvent {

    private final String disputeId;
    private final String calculationId;
    private final String salesRepId;
    private final boolean approved;
    private final String resolution;

    public DisputeResolvedEvent(String disputeId, String calculationId, String salesRepId,
                                 boolean approved, String resolution) {
        super();
        this.disputeId = disputeId;
        this.calculationId = calculationId;
        this.salesRepId = salesRepId;
        this.approved = approved;
        this.resolution = resolution;
    }

    @Override public String getAggregateId() { return disputeId; }
    @Override public String getAggregateType() { return "Dispute"; }

    public String getDisputeId() { return disputeId; }
    public String getCalculationId() { return calculationId; }
    public String getSalesRepId() { return salesRepId; }
    public boolean isApproved() { return approved; }
    public String getResolution() { return resolution; }
}
