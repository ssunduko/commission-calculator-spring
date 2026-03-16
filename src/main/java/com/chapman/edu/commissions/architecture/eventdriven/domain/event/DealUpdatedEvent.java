package com.chapman.edu.commissions.architecture.eventdriven.domain.event;

/**
 * Published when a deal's details (title, value, status, etc.) are modified.
 * Listeners may need to recalculate commissions or update reports.
 */
public class DealUpdatedEvent extends DomainEvent {

    private final String dealId;
    private final String updatedField;
    private final String oldValue;
    private final String newValue;

    public DealUpdatedEvent(String dealId, String updatedField, String oldValue, String newValue) {
        super();
        this.dealId = dealId;
        this.updatedField = updatedField;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override public String getAggregateId() { return dealId; }
    @Override public String getAggregateType() { return "Deal"; }

    public String getDealId() { return dealId; }
    public String getUpdatedField() { return updatedField; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
}
