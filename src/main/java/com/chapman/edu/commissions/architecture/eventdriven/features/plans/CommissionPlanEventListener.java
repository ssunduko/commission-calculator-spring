package com.chapman.edu.commissions.architecture.eventdriven.features.plans;

import com.chapman.edu.commissions.architecture.eventdriven.domain.event.CommissionPlanActivatedEvent;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.CommissionPlanCreatedEvent;
import com.chapman.edu.commissions.architecture.eventdriven.domain.event.RuleAddedToPlanEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for commission plan lifecycle events.
 * Could trigger recalculations when plans are activated or rules are modified.
 */
@Component
public class CommissionPlanEventListener {

    private static final Logger log = LoggerFactory.getLogger(CommissionPlanEventListener.class);

    @Async
    @EventListener
    @Order(10)
    public void onPlanCreated(CommissionPlanCreatedEvent event) {
        log.info("[EVENT] Commission plan created: '{}' (currency={}) [eventId={}]",
                event.getPlanName(), event.getCurrency(), event.getEventId());
    }

    @Async
    @EventListener
    @Order(10)
    public void onPlanActivated(CommissionPlanActivatedEvent event) {
        log.info("[EVENT] Commission plan activated: '{}' [planId={}, eventId={}]",
                event.getPlanName(), event.getPlanId(), event.getEventId());
    }

    @Async
    @EventListener
    @Order(10)
    public void onRuleAdded(RuleAddedToPlanEvent event) {
        log.info("[EVENT] Rule added to plan: '{}' (rate={}, type={}) [planId={}, eventId={}]",
                event.getRuleName(), event.getRate(), event.getRuleType(),
                event.getPlanId(), event.getEventId());
    }
}
