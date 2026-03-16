package com.chapman.edu.commissions.architecture.eventdriven.domain.event;

import java.math.BigDecimal;

/**
 * CONCEPT: Event Published After Deal Creation
 *
 * This event is published when a new deal is created. Listeners can
 * react to this event to perform cross-cutting concerns like:
 * - Logging the deal creation for audit
 * - Notifying the sales manager
 * - Triggering automatic commission calculation if a plan is assigned
 * - Updating analytics dashboards
 */
public class DealCreatedEvent extends DomainEvent {

    private final String dealId;
    private final String title;
    private final BigDecimal value;
    private final String salesRepId;

    public DealCreatedEvent(String dealId, String title, BigDecimal value, String salesRepId) {
        super();
        this.dealId = dealId;
        this.title = title;
        this.value = value;
        this.salesRepId = salesRepId;
    }

    @Override public String getAggregateId() { return dealId; }
    @Override public String getAggregateType() { return "Deal"; }

    public String getDealId() { return dealId; }
    public String getTitle() { return title; }
    public BigDecimal getValue() { return value; }
    public String getSalesRepId() { return salesRepId; }
}
