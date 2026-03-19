package com.chapman.edu.commissions.architecture.eventdriven.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * ============================================================
 * STARTUP DEMO: Event-Driven Architecture Processor Runner
 * ============================================================
 *
 * Executes Event-Driven processor demos at startup to showcase
 * Domain Events, Event Store, Async Listeners, Event Replay,
 * and Decoupling.
 */
@Configuration("eventDrivenProcessorDemo")
public class EventDrivenProcessorDemo {

    private static final Logger log = LoggerFactory.getLogger(EventDrivenProcessorDemo.class);
    private static final String BANNER = "═".repeat(60);
    private static final String SECTION = "─".repeat(40);

    @Bean
    @Order(3)
    public CommandLineRunner runEventDrivenDemos(EventDrivenProcessor processor) {
        return args -> {
            log.info("\n\n{}", BANNER);
            log.info("  EVENT-DRIVEN ARCHITECTURE PROCESSOR DEMO");
            log.info("  Domain Events | Event Store | Async Listeners");
            log.info("{}\n", BANNER);

            demo("[1/5] Event Publishing", () -> processor.demonstrateEventPublishing());
            demo("[2/5] Event Store — Audit Trail", () -> processor.demonstrateEventStore());
            demo("[3/5] Event Listeners — Sync vs Async", () -> processor.demonstrateEventListeners());
            demo("[4/5] Event Replay", () -> processor.demonstrateEventReplay());
            demo("[5/5] Decoupling via Events", () -> processor.demonstrateDecoupling());

            log.info("\n{}", BANNER);
            log.info("  EVENT-DRIVEN DEMO COMPLETE");
            log.info("{}\n", BANNER);
        };
    }

    private void demo(String title, java.util.function.Supplier<Map<String, Object>> fn) {
        log.info("\n{}", SECTION);
        log.info("  {}", title);
        log.info("{}", SECTION);
        try {
            fn.get().forEach((k, v) -> log.info("    {} = {}", k, v));
        } catch (Exception e) {
            log.warn("    Demo skipped: {}", e.getMessage());
        }
    }
}
