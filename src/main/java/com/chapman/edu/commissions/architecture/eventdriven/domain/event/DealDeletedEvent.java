package com.chapman.edu.commissions.architecture.eventdriven.domain.event;

/**
 * Published when a deal is deleted. Listeners may need to
 * cancel related commission calculations or clean up references.
 */
public class DealDeletedEvent extends DomainEvent {

    private final String dealId;
    private final String salesRepId;

    public DealDeletedEvent(String dealId, String salesRepId) {
        super();
        this.dealId = dealId;
        this.salesRepId = salesRepId;
    }

    @Override public String getAggregateId() { return dealId; }
    @Override public String getAggregateType() { return "Deal"; }

    public String getDealId() { return dealId; }
    public String getSalesRepId() { return salesRepId; }
}
