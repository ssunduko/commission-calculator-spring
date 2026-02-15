package com.chapman.edu.commissions.springboot.processor;

import com.chapman.edu.commissions.model.*;
import com.chapman.edu.commissions.springboot.repository.*;
import com.chapman.edu.commissions.springboot.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * PROCESSOR: DEPENDENCY INJECTION & IoC CONTAINER DEMONSTRATION
 * ============================================================================
 *
 * This runnable demonstrates Spring's Dependency Injection (DI) and
 * Inversion of Control (IoC) Container concepts:
 *
 *   1. Constructor Injection — Dependencies passed via constructor
 *   2. Field Injection — Dependencies injected via @Autowired on fields
 *   3. ApplicationContext — The IoC Container that manages all beans
 *   4. Bean Discovery — How Spring finds and creates beans
 *   5. Component Scanning — @Component, @Service, @Repository discovery
 *
 * RUN: This processor runs automatically at startup (implements CommandLineRunner).
 *
 * CONCEPT: @Order
 * The @Order annotation defines the execution order when multiple
 * CommandLineRunner beans exist. Lower numbers run first.
 */
@Component
@Order(2)  // Runs after SampleDataLoader (which is Order 1 by default)
public class DependencyInjectionProcessor implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DependencyInjectionProcessor.class);

    // ===== CONCEPT: Constructor Injection (PREFERRED) =====
    // Dependencies are declared as final fields and set via the constructor.
    // Benefits: immutability, testability, clear dependencies
    private final DealService dealService;
    private final UserService userService;

    // ===== CONCEPT: Field Injection (via @Autowired) =====
    // Spring injects the dependency directly into the field.
    // Not recommended for production code (harder to test), but shown for education.
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Constructor Injection — Spring automatically provides matching beans.
     * No @Autowired annotation needed (single constructor).
     */
    public DependencyInjectionProcessor(DealService dealService, UserService userService) {
        this.dealService = dealService;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        logger.info("");
        logger.info("╔══════════════════════════════════════════════════════════════╗");
        logger.info("║   DEPENDENCY INJECTION & IoC CONTAINER DEMONSTRATION        ║");
        logger.info("╚══════════════════════════════════════════════════════════════╝");

        // ----- Demonstrate IoC Container (ApplicationContext) -----
        logger.info("");
        logger.info("--- IoC Container (ApplicationContext) ---");
        logger.info("Total beans in container: {}", applicationContext.getBeanDefinitionCount());

        // Show our custom beans registered in the container
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        int customBeanCount = 0;
        for (String name : beanNames) {
            if (name.contains("commission") || name.contains("deal") ||
                name.contains("user") || name.contains("dispute") ||
                name.contains("plan") || name.contains("jwt") ||
                name.contains("mapper") || name.contains("processor")) {
                logger.info("  Bean: {} -> {}", name, applicationContext.getBean(name).getClass().getSimpleName());
                customBeanCount++;
            }
        }
        logger.info("Custom beans found: {}", customBeanCount);

        // ----- Demonstrate Service Layer (injected via constructor) -----
        logger.info("");
        logger.info("--- Using Injected Services ---");
        logger.info("DealService class: {}", dealService.getClass().getSimpleName());
        logger.info("UserService class: {}", userService.getClass().getSimpleName());
        logger.info("Total deals (via DealService): {}", dealService.getDealCount());
        logger.info("Total users (via UserService): {}", userService.getUserCount());

        // ----- Demonstrate Bean Retrieval from Context -----
        logger.info("");
        logger.info("--- Bean Retrieval from ApplicationContext ---");
        DealRepository dealRepo = applicationContext.getBean(DealRepository.class);
        logger.info("DealRepository retrieved from context: {}", dealRepo.getClass().getSimpleName());
        logger.info("Deal count from repository: {}", dealRepo.count());

        // Verify same instance (singleton scope)
        DealRepository dealRepo2 = applicationContext.getBean(DealRepository.class);
        logger.info("Same bean instance (singleton)? {}", dealRepo == dealRepo2);

        logger.info("");
        logger.info("=== Dependency Injection Demo Complete ===");
        logger.info("");
    }
}
