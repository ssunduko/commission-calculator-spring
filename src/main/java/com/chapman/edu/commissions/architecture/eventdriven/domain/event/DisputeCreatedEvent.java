package com.chapman.edu.commissions.architecture.eventdriven.domain.event;

/**
 * Published when a sales rep files a dispute against a commission calculation.
 * Listeners may notify the manager or pause payout processing.
 */
public class DisputeCreatedEvent extends DomainEvent {

    private final String disputeId;
    private final String calculationId;
    private final String salesRepId;
    private final String title;

    public DisputeCreatedEvent(String disputeId, String calculationId, String salesRepId, String title) {
        super();
        this.disputeId = disputeId;
        this.calculationId = calculationId;
        this.salesRepId = salesRepId;
        this.title = title;
    }

    @Override public String getAggregateId() { return disputeId; }
    @Override public String getAggregateType() { return "Dispute"; }

    public String getDisputeId() { return disputeId; }
    public String getCalculationId() { return calculationId; }
    public String getSalesRepId() { return salesRepId; }
    public String getTitle() { return title; }
}
