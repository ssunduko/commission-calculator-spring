package com.chapman.edu.commissions.corespring.di;

import com.chapman.edu.commissions.model.CommissionCalculation;
import com.chapman.edu.commissions.model.Deal;

import java.math.BigDecimal;

/**
 * ANTI-PATTERN EXAMPLE: Commission calculation WITHOUT Dependency Injection.
 *
 * This class demonstrates the problems that occur when you DON'T use Dependency Injection:
 *
 * PROBLEMS DEMONSTRATED:
 * 1. TIGHT COUPLING - Directly depends on concrete implementations (EmailNotificationService)
 * 2. HARD TO TEST - Cannot inject mocks; must use real email service in tests
 * 3. VIOLATES SRP - Creates its own dependencies (not its responsibility)
 * 4. HIDDEN DEPENDENCIES - Can't see what this class needs without reading the code
 * 5. INFLEXIBLE - Cannot swap implementations without modifying this class
 * 6. DIFFICULT TO MAINTAIN - Changes to EmailNotificationService affect this class
 */
public class SimpleCalculationService {

    //PROBLEM 1: Tight Coupling - depends on concrete class, not interface
    private EmailNotificationService emailService;

    //PROBLEM 2: Tight Coupling - depends on concrete class
    private DatabaseAuditLogger auditLogger;

    //PROBLEM 3: Hidden dependencies - created in constructor, not obvious from signature
    public SimpleCalculationService() {
        //PROBLEM 4: Creates own dependencies - violates Single Responsibility Principle
        this.emailService = new EmailNotificationService();
        this.auditLogger = new DatabaseAuditLogger();

        //PROBLEM 5: What if EmailNotificationService constructor requires parameters?
        //This class would need to know about EmailNotificationService's dependencies too!
    }

    /**
     * Calculate commission for a deal.
     *
     * PROBLEMS WITH THIS METHOD:
     * - Cannot unit test without sending real emails
     * - Cannot verify audit logging without real database
     * - Cannot swap email service for SMS without modifying this class
     * - Any change to EmailNotificationService constructor breaks this class
     */
    public CommissionCalculation calculateCommission(Deal deal, String planId) {
        //Cannot mock this validation - it's hardcoded
        if (deal == null || deal.getValue() == null) {
            throw new IllegalArgumentException("Invalid deal");
        }

        // Perform calculation
        BigDecimal baseCommission = deal.getValue().multiply(new BigDecimal("0.10"));

        //PROBLEM 6: Direct method calls to concrete implementations
        //Cannot intercept or modify this behavior without changing this class
        auditLogger.log("Calculating commission for deal: " + deal.getId());

        CommissionCalculation calculation = new CommissionCalculation(
            deal.getId(),
            deal.getSalesRepId(),
            baseCommission
        );

        calculation.setGrossCommission(baseCommission);
        calculation.setNetCommission(baseCommission);

        //PROBLEM 7: Hardcoded notification logic
        //What if we want to send SMS instead? Must modify this class!
        emailService.notifyCommissionCalculated(calculation);

        return calculation;
    }

    /**
     * TESTING PROBLEMS:
     *
     * How do you test this class?
     *
     * @Test
     * public void testCalculateCommission() {
     *     SimpleCalculationService service = new SimpleCalculationService();
     *     //PROBLEM: Real EmailService will try to send emails during test!
     *     //PROBLEM: Real AuditLogger will try to write to database during test!
     *     //PROBLEM: Cannot verify that notify() was called
     *     //PROBLEM: Cannot verify what parameters were passed to notify()
     *
     *     Deal deal = new Deal("Test", new BigDecimal("1000"), "rep-1");
     *     CommissionCalculation result = service.calculateCommission(deal, "plan-1");
     *
     *     // Can only assert on the result, not on the interactions
     *     assertEquals(new BigDecimal("100"), result.getBaseCommission());
     * }
     */

    /**
     * MAINTENANCE PROBLEMS:
     *
     * Scenario 1: Need to change from Email to SMS notifications
     * Must modify this class (violates Open-Closed Principle)
     * Must test this class again even though logic didn't change
     *
     * Scenario 2: EmailNotificationService constructor changes
     * This class breaks and must be updated
     * All tests break even though this class's logic didn't change
     *
     * Scenario 3: Need different notification service in different environments
     * Must create separate classes or use if/else logic
     * Cannot configure externally
     */

    /**
     * COMPARISON WITH DEPENDENCY INJECTION:
     *
     * See CommissionCalculationService.java for the correct approach:
     *
     * WITH DI:
     * ✅ Depends on interfaces (NotificationService, AuditLogger)
     * ✅ Dependencies injected via constructor (explicit and testable)
     * ✅ Easy to test (inject mocks)
     * ✅ Easy to swap implementations (configure externally)
     * ✅ Follows Single Responsibility (doesn't create dependencies)
     * ✅ Flexible and maintainable
     *
     * @Component
     * public class CommissionCalculationService {
     *     private final NotificationService notificationService;  // Interface!
     *     private final AuditLogger auditLogger;                  // Interface!
     *
     *     @Autowired
     *     public CommissionCalculationService(
     *         NotificationService notificationService,  // Injected!
     *         AuditLogger auditLogger) {                // Injected!
     *
     *         this.notificationService = notificationService;
     *         this.auditLogger = auditLogger;
     *     }
     * }
     */
}
