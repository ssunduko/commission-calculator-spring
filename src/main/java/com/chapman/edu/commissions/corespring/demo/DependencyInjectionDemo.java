package com.chapman.edu.commissions.corespring.demo;

import com.chapman.edu.commissions.corespring.di.*;
import com.chapman.edu.commissions.model.Deal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Runnable demo showing Dependency Injection concepts.
 *
 * Run this class to see:
 * - Constructor, Setter, and Field injection
 * - @Qualifier resolving multiple implementations
 * - @Primary selecting default implementation
 * - Optional dependencies
 *
 * To run: Uncomment @Component annotation and start the application
 */
//@Component  // Uncomment to run this demo
public class DependencyInjectionDemo implements CommandLineRunner {

    private final ApplicationContext context;
    private final CommissionCalculationService service;

    public DependencyInjectionDemo(ApplicationContext context,
                                  CommissionCalculationService service) {
        this.context = context;
        this.service = service;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========================================");
        System.out.println("DEPENDENCY INJECTION DEMO");
        System.out.println("========================================\n");

        demonstrateConstructorInjection();
        demonstrateMultipleImplementations();
        demonstrateOptionalDependencies();
        demonstrateDependencyStatus();
    }

    private void demonstrateConstructorInjection() {
        System.out.println("--- Constructor Injection ---");
        System.out.println("CommissionCalculationService uses:");
        System.out.println("1. Constructor injection for required dependencies");
        System.out.println("2. Field injection for audit logger");
        System.out.println("3. Setter injection for optional validation service");
        System.out.println();

        // Create test deal
        Deal deal = new Deal("Enterprise Software License", new BigDecimal("15000"), "rep-001");
        deal.setId("deal-001");

        // Call service method - demonstrates injection working
        System.out.println("Calculating commission for deal: " + deal.getTitle());
        service.calculateCommission(deal, "standard-plan");

        System.out.println();
    }

    private void demonstrateMultipleImplementations() {
        System.out.println("--- Multiple Implementations (@Qualifier & @Primary) ---");

        // Get both notification service implementations
        NotificationService emailService = (NotificationService) context.getBean("defaultNotificationService");
        NotificationService smsService = (NotificationService) context.getBean("smsNotificationService");

        System.out.println("EmailNotificationService (marked with @Primary):");
        emailService.sendAlert("This is an email notification");

        System.out.println("\nSmsNotificationService:");
        smsService.sendAlert("This is an SMS notification");

        System.out.println("\nCommissionCalculationService uses @Qualifier to inject:");
        System.out.println("- 'defaultNotificationService' for notifications");
        System.out.println("- 'emailAuditLogger' for audit logging");

        System.out.println();
    }

    private void demonstrateOptionalDependencies() {
        System.out.println("--- Optional Dependencies ---");

        // ValidationService is injected with @Autowired(required = false)
        try {
            Deal invalidDeal = new Deal(null, null, null);
            service.calculateCommission(invalidDeal, "standard-plan");
        } catch (Exception e) {
            System.out.println("Validation caught invalid deal: " + e.getMessage());
        }

        System.out.println("ValidationService is optional - service works without it");
        System.out.println();
    }

    private void demonstrateDependencyStatus() {
        System.out.println("--- Dependency Injection Status ---");
        System.out.println(service.getDependencyStatus());

        System.out.println("\nKey Points:");
        System.out.println("✓ Constructor injection: Required dependencies (final fields)");
        System.out.println("✓ Setter injection: Optional dependencies");
        System.out.println("✓ Field injection: Works but not recommended (shown for education)");
        System.out.println();
    }
}
