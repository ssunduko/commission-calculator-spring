package com.chapman.edu.commissions.architecture.microservice.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * ============================================================
 * STARTUP DEMO: Microservice Architecture Processor Runner
 * ============================================================
 *
 * Executes Microservice processor demos at startup to showcase
 * Service Topology, Inter-Service Communication, API Gateway,
 * and Database-per-Service pattern.
 */
@Configuration("microserviceProcessorDemo")
public class MicroserviceProcessorDemo {

    private static final Logger log = LoggerFactory.getLogger(MicroserviceProcessorDemo.class);
    private static final String BANNER = "═".repeat(60);
    private static final String SECTION = "─".repeat(40);

    @Bean
    @Order(3)
    public CommandLineRunner runMicroserviceDemos(MicroserviceProcessor processor) {
        return args -> {
            log.info("\n\n{}", BANNER);
            log.info("  MICROSERVICE ARCHITECTURE PROCESSOR DEMO");
            log.info("  Service Topology | REST Clients | API Gateway | DB per Service");
            log.info("{}\n", BANNER);

            demo("[1/5] Service Topology & Registry", () -> processor.demonstrateServiceTopology());
            demo("[2/5] Independent Service Operations", () -> processor.demonstrateIndependentServices());
            demo("[3/5] Inter-Service Communication", () -> processor.demonstrateInterServiceCommunication());
            demo("[4/5] API Gateway Pattern", () -> processor.demonstrateApiGateway());
            demo("[5/5] Database per Service", () -> processor.demonstrateDatabasePerService());

            log.info("\n{}", BANNER);
            log.info("  MICROSERVICE DEMO COMPLETE");
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
