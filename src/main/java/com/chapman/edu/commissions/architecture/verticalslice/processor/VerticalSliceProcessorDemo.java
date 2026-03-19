package com.chapman.edu.commissions.architecture.verticalslice.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * ============================================================
 * STARTUP DEMO: Vertical Slice Architecture Processor Runner
 * ============================================================
 *
 * Executes Vertical Slice processor demos at startup to showcase
 * Feature-First Organization, Minimal Abstractions, Cross-Feature
 * Communication, and Rapid Development.
 */
@Configuration("verticalSliceProcessorDemo")
public class VerticalSliceProcessorDemo {

    private static final Logger log = LoggerFactory.getLogger(VerticalSliceProcessorDemo.class);
    private static final String BANNER = "═".repeat(60);
    private static final String SECTION = "─".repeat(40);

    @Bean
    @Order(3)
    public CommandLineRunner runVerticalSliceDemos(VerticalSliceProcessor processor) {
        return args -> {
            log.info("\n\n{}", BANNER);
            log.info("  VERTICAL SLICE ARCHITECTURE PROCESSOR DEMO");
            log.info("  Feature-First | Minimal Abstractions | MCP Server | Rapid Development");
            log.info("{}\n", BANNER);

            demo("[1/6] Feature-First Organization", () -> processor.demonstrateFeatureFirstOrganization());
            demo("[2/6] Minimal Abstractions", () -> processor.demonstrateMinimalAbstractions());
            demo("[3/6] Cross-Feature Communication", () -> processor.demonstrateCrossFeatureCommunication());
            demo("[4/6] Rapid Development", () -> processor.demonstrateRapidDevelopment());
            demo("[5/6] Full Feature Walkthrough", () -> processor.demonstrateFullFeatureWalkthrough());
            demo("[6/6] MCP Server — AI Agent Integration", () -> processor.demonstrateMcpServer());

            log.info("\n{}", BANNER);
            log.info("  VERTICAL SLICE DEMO COMPLETE");
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
