package com.chapman.edu.commissions.architecture.ddd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * CONCEPT: Domain-Driven Design Application
 *
 * This module demonstrates Domain-Driven Design (DDD) applied to the
 * commission calculator. Key DDD concepts demonstrated:
 *
 * 1. AGGREGATES — Domain objects clustered with an Aggregate Root
 *    (Deal, CommissionPlan, CommissionCalculation, Dispute, User)
 *
 * 2. AGGREGATE ROOTS — Entry points that enforce invariants
 *    (marked with the AggregateRoot interface)
 *
 * 3. REPOSITORIES — Domain-level interfaces for persistence
 *    (defined in domain layer, implemented in infrastructure)
 *
 * 4. DOMAIN SERVICES — Business logic spanning multiple aggregates
 *    (CommissionCalculationService calculates across Deal + Plan)
 *
 * 5. APPLICATION SERVICES — Use case orchestration with @Transactional
 *    (DealApplicationService, CommissionPlanApplicationService, etc.)
 *
 * 6. UBIQUITOUS LANGUAGE — Code uses domain terminology
 *    (Aggregate, Repository, DomainException, not generic terms)
 *
 * 7. LAYERED ARCHITECTURE — domain → application → interfaces → infrastructure
 *    with dependencies pointing inward
 */
@SpringBootApplication(
    scanBasePackages = "com.chapman.edu.commissions.architecture.ddd"
)
@EntityScan(basePackages = {
    "com.chapman.edu.commissions.architecture.ddd.domain.deal",
    "com.chapman.edu.commissions.architecture.ddd.domain.plan",
    "com.chapman.edu.commissions.architecture.ddd.domain.calculation",
    "com.chapman.edu.commissions.architecture.ddd.domain.dispute",
    "com.chapman.edu.commissions.architecture.ddd.domain.user"
})
@EnableJpaRepositories(basePackages = "com.chapman.edu.commissions.architecture.ddd.infrastructure.persistence")
public class DddCommissionApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DddCommissionApplication.class);
        app.setAdditionalProfiles("ddd");
        app.run(args);
    }
}
