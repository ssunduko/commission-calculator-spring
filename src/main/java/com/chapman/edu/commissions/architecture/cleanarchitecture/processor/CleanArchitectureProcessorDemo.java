package com.chapman.edu.commissions.architecture.cleanarchitecture.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * ============================================================
 * STARTUP DEMO: Clean Architecture Processor Runner
 * ============================================================
 *
 * Executes the Clean Architecture processor demos at startup
 * to showcase Ports & Adapters, Dependency Inversion, Command
 * objects, and layer isolation.
 */
@Configuration("cleanArchProcessorDemo")
public class CleanArchitectureProcessorDemo {

    private static final Logger log = LoggerFactory.getLogger(CleanArchitectureProcessorDemo.class);
    private static final String BANNER = "═".repeat(60);
    private static final String SECTION = "─".repeat(40);

    @Bean
    @Order(3)
    public CommandLineRunner runCleanArchDemos(CleanArchitectureProcessor processor) {
        return args -> {
            log.info("\n\n{}", BANNER);
            log.info("  CLEAN ARCHITECTURE PROCESSOR DEMO");
            log.info("  Ports & Adapters | Dependency Inversion | Layer Isolation");
            log.info("{}\n", BANNER);

            // Demo 1: Dependency Inversion
            log.info("\n{}", SECTION);
            log.info("  [1/5] Dependency Inversion via Input Ports");
            log.info("{}", SECTION);
            try {
                Map<String, Object> result = processor.demonstrateDependencyInversion();
                result.forEach((k, v) -> log.info("    {} = {}", k, v));
            } catch (Exception e) {
                log.warn("    Demo skipped: {}", e.getMessage());
            }

            // Demo 2: Command Pattern
            log.info("\n{}", SECTION);
            log.info("  [2/5] Command Objects with Validation");
            log.info("{}", SECTION);
            try {
                Map<String, Object> result = processor.demonstrateCommandPattern();
                result.forEach((k, v) -> log.info("    {} = {}", k, v));
            } catch (Exception e) {
                log.warn("    Demo skipped: {}", e.getMessage());
            }

            // Demo 3: Port & Adapter Pattern
            log.info("\n{}", SECTION);
            log.info("  [3/5] Port & Adapter (Hexagonal) Pattern");
            log.info("{}", SECTION);
            try {
                Map<String, Object> result = processor.demonstratePortAdapterPattern();
                result.forEach((k, v) -> log.info("    {} = {}", k, v));
            } catch (Exception e) {
                log.warn("    Demo skipped: {}", e.getMessage());
            }

            // Demo 4: Full Use Case Flow
            log.info("\n{}", SECTION);
            log.info("  [4/5] Full Use Case Flow — Commission Calculation");
            log.info("{}", SECTION);
            try {
                Map<String, Object> result = processor.demonstrateFullUseCaseFlow();
                result.forEach((k, v) -> log.info("    {} = {}", k, v));
            } catch (Exception e) {
                log.warn("    Demo skipped: {}", e.getMessage());
            }

            // Demo 5: Layer Isolation
            log.info("\n{}", SECTION);
            log.info("  [5/5] Layer Isolation Verification");
            log.info("{}", SECTION);
            try {
                Map<String, Object> result = processor.demonstrateLayerIsolation();
                result.forEach((k, v) -> log.info("    {} = {}", k, v));
            } catch (Exception e) {
                log.warn("    Demo skipped: {}", e.getMessage());
            }

            log.info("\n{}", BANNER);
            log.info("  CLEAN ARCHITECTURE DEMO COMPLETE");
            log.info("{}\n", BANNER);
        };
    }
}
