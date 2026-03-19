package com.chapman.edu.commissions.architecture.orthogonal.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * ============================================================
 * STARTUP DEMO: Orthogonal Architecture Processor Runner
 * ============================================================
 *
 * Executes Orthogonal (CQRS + AOP) processor demos at startup
 * to showcase Command/Query separation, Pipeline Bus, AOP Aspects,
 * Audit Trail, and Command Validation.
 */
@Configuration("orthogonalProcessorDemo")
public class OrthogonalProcessorDemo {

    private static final Logger log = LoggerFactory.getLogger(OrthogonalProcessorDemo.class);
    private static final String BANNER = "═".repeat(60);
    private static final String SECTION = "─".repeat(40);

    @Bean
    @Order(3)
    public CommandLineRunner runOrthogonalDemos(OrthogonalProcessor processor) {
        return args -> {
            log.info("\n\n{}", BANNER);
            log.info("  ORTHOGONAL ARCHITECTURE PROCESSOR DEMO");
            log.info("  CQRS | AOP Aspects | Pipeline Bus | Audit Trail");
            log.info("{}\n", BANNER);

            demo("[1/5] CQRS — Command/Query Separation", () -> processor.demonstrateCqrs());
            demo("[2/5] Pipeline Bus — Auto-Discovery", () -> processor.demonstratePipelineBus());
            demo("[3/5] AOP Aspect Chain", () -> processor.demonstrateAopAspectChain());
            demo("[4/5] Automatic Audit Trail", () -> processor.demonstrateAuditTrail());
            demo("[5/5] Command Validation via Aspect", () -> processor.demonstrateCommandValidation());

            log.info("\n{}", BANNER);
            log.info("  ORTHOGONAL DEMO COMPLETE");
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
