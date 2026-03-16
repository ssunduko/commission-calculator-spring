package com.chapman.edu.commissions.architecture.eventdriven;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * CONCEPT: Event-Driven Architecture Application
 *
 * This module demonstrates Event-Driven Architecture (EDA) applied to
 * the commission calculator domain. Key concepts demonstrated:
 *
 * 1. DOMAIN EVENTS — Immutable records of business-significant happenings
 *    (DealCreatedEvent, CommissionCalculatedEvent, DisputeResolvedEvent)
 *
 * 2. EVENT PUBLISHER — Services publish events via Spring's ApplicationEventPublisher.
 *    The publisher doesn't know or care who listens.
 *
 * 3. EVENT LISTENERS — Components annotated with @EventListener react to events.
 *    New behaviors can be added by creating new listeners, without modifying services.
 *
 * 4. EVENT STORE — An append-only log that persists every domain event,
 *    providing audit trail, debugging, and event replay capabilities.
 *
 * 5. ASYNC PROCESSING — @Async listeners process events in background threads,
 *    keeping the main request-response cycle fast.
 *
 * The REST API is identical to the vertical-slice module (same endpoints,
 * same business logic), but the internal plumbing uses events for
 * cross-cutting concerns instead of direct method calls.
 */
@SpringBootApplication(
    scanBasePackages = "com.chapman.edu.commissions.architecture.eventdriven"
)
@EntityScan("com.chapman.edu.commissions.architecture.eventdriven.domain")
@EnableJpaRepositories(basePackages = {
    "com.chapman.edu.commissions.architecture.eventdriven.features",
    "com.chapman.edu.commissions.architecture.eventdriven.infrastructure"
})
public class EventDrivenCommissionApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(EventDrivenCommissionApplication.class);
        app.setAdditionalProfiles("eventdriven");
        app.run(args);
    }
}
