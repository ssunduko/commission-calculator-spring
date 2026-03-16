package com.chapman.edu.commissions.architecture.eventdriven.features.calculations;

import com.chapman.edu.commissions.architecture.eventdriven.domain.event.CommissionCalculatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for commission calculation events.
 * In a real system, this could trigger approval workflows or notifications.
 */
@Component
public class CommissionCalculationEventListener {

    private static final Logger log = LoggerFactory.getLogger(CommissionCalculationEventListener.class);

    @Async
    @EventListener
    @Order(10)
    public void onCommissionCalculated(CommissionCalculatedEvent event) {
        log.info("[EVENT] Commission calculated: calculationId={}, dealId={}, salesRep={}, net={} [eventId={}]",
                event.getCalculationId(), event.getDealId(), event.getSalesRepId(),
                event.getNetCommission(), event.getEventId());
    }
}
