package com.chapman.edu.commissions.architecture.orthogonal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * CONCEPT: Orthogonal Architecture Application
 *
 * This module demonstrates Orthogonal Architecture applied to the
 * commission calculator domain. Key concepts demonstrated:
 *
 * 1. COMMANDS & QUERIES — Operations modeled as first-class objects
 * 2. HANDLERS — One handler per command/query (single responsibility)
 * 3. PIPELINE BUS — Routes commands/queries to handlers via a mediator
 * 4. ORTHOGONAL ASPECTS — Cross-cutting concerns applied via AOP:
 *    - LoggingAspect (@Order(1)) — Logs all handler executions
 *    - ValidationAspect (@Order(2)) — Auto-validates commands
 *    - AuditingAspect (@Order(3)) — Records command executions to audit_log
 *    - PerformanceAspect (@Order(4)) — Flags slow operations
 *
 * The "orthogonal" insight: each concern is an independent dimension.
 * Adding a new handler automatically gets logging, validation, auditing.
 * Adding a new aspect automatically applies to all existing handlers.
 * No concern knows about any other concern — true independence.
 */
@SpringBootApplication(
    scanBasePackages = "com.chapman.edu.commissions.architecture.orthogonal"
)
@EntityScan(basePackages = {
    "com.chapman.edu.commissions.architecture.orthogonal.domain",
    "com.chapman.edu.commissions.architecture.orthogonal.aspects.auditing"
})
@EnableJpaRepositories(basePackages = {
    "com.chapman.edu.commissions.architecture.orthogonal.features",
    "com.chapman.edu.commissions.architecture.orthogonal.infrastructure",
    "com.chapman.edu.commissions.architecture.orthogonal.aspects.auditing"
})
@EnableAspectJAutoProxy
public class OrthogonalCommissionApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(OrthogonalCommissionApplication.class);
        app.setAdditionalProfiles("orthogonal");
        app.run(args);
    }
}
