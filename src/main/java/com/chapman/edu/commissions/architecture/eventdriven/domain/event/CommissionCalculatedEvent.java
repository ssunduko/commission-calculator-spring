package com.chapman.edu.commissions.architecture.eventdriven.domain.event;

import java.math.BigDecimal;

/**
 * CONCEPT: Cross-Aggregate Event
 *
 * Published when a commission is calculated for a deal. This event
 * bridges the Deal and CommissionCalculation aggregates. Listeners can:
 * - Notify the sales rep of their commission amount
 * - Trigger approval workflows
 * - Update running totals and dashboards
 */
public class CommissionCalculatedEvent extends DomainEvent {

    private final String calculationId;
    private final String dealId;
    private final String salesRepId;
    private final BigDecimal baseCommission;
    private final BigDecimal netCommission;

    public CommissionCalculatedEvent(String calculationId, String dealId, String salesRepId,
                                      BigDecimal baseCommission, BigDecimal netCommission) {
        super();
        this.calculationId = calculationId;
        this.dealId = dealId;
        this.salesRepId = salesRepId;
        this.baseCommission = baseCommission;
        this.netCommission = netCommission;
    }

    @Override public String getAggregateId() { return calculationId; }
    @Override public String getAggregateType() { return "CommissionCalculation"; }

    public String getCalculationId() { return calculationId; }
    public String getDealId() { return dealId; }
    public String getSalesRepId() { return salesRepId; }
    public BigDecimal getBaseCommission() { return baseCommission; }
    public BigDecimal getNetCommission() { return netCommission; }
}
