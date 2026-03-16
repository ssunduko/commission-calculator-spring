package com.chapman.edu.commissions.architecture.eventdriven.domain.event;

/**
 * Published when a dispute is escalated to management.
 * Listeners may send notifications to the manager or update SLA tracking.
 */
public class DisputeEscalatedEvent extends DomainEvent {

    private final String disputeId;
    private final String calculationId;
    private final String salesRepId;

    public DisputeEscalatedEvent(String disputeId, String calculationId, String salesRepId) {
        super();
        this.disputeId = disputeId;
        this.calculationId = calculationId;
        this.salesRepId = salesRepId;
    }

    @Override public String getAggregateId() { return disputeId; }
    @Override public String getAggregateType() { return "Dispute"; }

    public String getDisputeId() { return disputeId; }
    public String getCalculationId() { return calculationId; }
    public String getSalesRepId() { return salesRepId; }
}
