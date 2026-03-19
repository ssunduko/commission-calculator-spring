package com.chapman.edu.commissions.architecture.ddd.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * ============================================================
 * STARTUP DEMO: DDD Processor Runner
 * ============================================================
 *
 * Executes DDD processor demos at startup to showcase Aggregate
 * Roots, Domain Services, Repository per Aggregate, and the
 * Application vs Domain Service distinction.
 */
@Configuration("dddProcessorDemo")
public class DddProcessorDemo {

    private static final Logger log = LoggerFactory.getLogger(DddProcessorDemo.class);
    private static final String BANNER = "═".repeat(60);
    private static final String SECTION = "─".repeat(40);

    @Bean
    @Order(3)
    public CommandLineRunner runDddDemos(DddProcessor processor) {
        return args -> {
            log.info("\n\n{}", BANNER);
            log.info("  DDD PROCESSOR DEMO");
            log.info("  Aggregates | Domain Services | Ubiquitous Language");
            log.info("{}\n", BANNER);

            demo("[1/5] Aggregate Roots & Boundaries", () -> processor.demonstrateAggregateRoots());
            demo("[2/5] Domain Services — Cross-Aggregate Logic", () -> processor.demonstrateDomainServices());
            demo("[3/5] Application Service vs Domain Service", () -> processor.demonstrateApplicationVsDomainService());
            demo("[4/5] Repository per Aggregate", () -> processor.demonstrateRepositoryPerAggregate());
            demo("[5/5] Full DDD Flow — Commission Calculation", () -> processor.demonstrateFullDddFlow());

            log.info("\n{}", BANNER);
            log.info("  DDD DEMO COMPLETE");
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
