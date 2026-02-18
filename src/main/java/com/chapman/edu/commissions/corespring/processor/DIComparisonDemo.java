package com.chapman.edu.commissions.corespring.processor;

import com.chapman.edu.commissions.corespring.di.CommissionCalculationService;
import com.chapman.edu.commissions.corespring.di.SimpleCalculationService;
import com.chapman.edu.commissions.model.CommissionCalculation;
import com.chapman.edu.commissions.model.Deal;
import org.springframework.boot.CommandLineRunner;

import java.math.BigDecimal;

/**
 * Demonstration comparing code WITH and WITHOUT Dependency Injection.
 *
 * This demo shows:
 * 1. Problems with SimpleCalculationService (NO DI) - tightly coupled, hard to test
 * 2. Benefits of CommissionCalculationService (WITH DI) - loose coupling, testable
 *
 * To run: Uncomment @Component annotation and start the application
 */
//@Component  // Uncomment to run this demo
public class DIComparisonDemo implements CommandLineRunner {

    private final CommissionCalculationService diService;

    public DIComparisonDemo(CommissionCalculationService diService) {
        this.diService = diService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEPENDENCY INJECTION COMPARISON DEMO");
        System.out.println("=".repeat(80));
        System.out.println("Comparing code WITHOUT DI vs WITH DI");
        System.out.println("=".repeat(80) + "\n");

        demonstrateWithoutDI();
        demonstrateWithDI();
        explainDifferences();
    }

    private void demonstrateWithoutDI() {
        System.out.println("--- WITHOUT Dependency Injection (Anti-Pattern) ---\n");

        System.out.println("Code: SimpleCalculationService");
        System.out.println("```java");
        System.out.println("public class SimpleCalculationService {");
        System.out.println("    private EmailNotificationService emailService;  // ❌ Concrete class");
        System.out.println("    private DatabaseAuditLogger auditLogger;        // ❌ Concrete class");
        System.out.println("");
        System.out.println("    public SimpleCalculationService() {");
        System.out.println("        this.emailService = new EmailNotificationService();  // ❌ Creates own deps");
        System.out.println("        this.auditLogger = new DatabaseAuditLogger();        // ❌ Creates own deps");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("```\n");

        System.out.println("Creating service WITHOUT DI:");
        SimpleCalculationService simpleService = new SimpleCalculationService();
        System.out.println("✓ Service created\n");

        // Create test deal
        Deal deal = new Deal("Software License", new BigDecimal("10000"), "rep-001");
        deal.setId("deal-without-di");

        System.out.println("Calling calculateCommission()...\n");
        CommissionCalculation result = simpleService.calculateCommission(deal, "standard-plan");
        System.out.println("Result: $" + result.getNetCommission() + "\n");

        System.out.println("PROBLEMS with this approach:");
        System.out.println("❌ 1. TIGHT COUPLING");
        System.out.println("   - Directly depends on EmailNotificationService (concrete class)");
        System.out.println("   - Cannot swap for SmsNotificationService without code changes");
        System.out.println("   - Changes to EmailNotificationService constructor break this class");
        System.out.println();

        System.out.println("❌ 2. HARD TO TEST");
        System.out.println("   - Cannot inject mock EmailService → sends real emails in tests!");
        System.out.println("   - Cannot inject mock AuditLogger → writes to real database in tests!");
        System.out.println("   - Cannot verify that notify() was called or with what parameters");
        System.out.println();

        System.out.println("❌ 3. VIOLATES SINGLE RESPONSIBILITY PRINCIPLE");
        System.out.println("   - Responsible for both commission calculation AND creating dependencies");
        System.out.println("   - Should focus only on business logic");
        System.out.println();

        System.out.println("❌ 4. HIDDEN DEPENDENCIES");
        System.out.println("   - Constructor signature: SimpleCalculationService()");
        System.out.println("   - Cannot see what dependencies are needed!");
        System.out.println("   - Must read implementation to understand requirements");
        System.out.println();

        System.out.println("❌ 5. INFLEXIBLE");
        System.out.println("   - Want SMS instead of Email? Must modify SimpleCalculationService code");
        System.out.println("   - Want different service in dev vs prod? Must use if/else logic");
        System.out.println("   - Cannot configure externally");
        System.out.println();

        System.out.println("❌ 6. DIFFICULT TO MAINTAIN");
        System.out.println("   - Any change to dependencies requires code changes here");
        System.out.println("   - Breaks Open-Closed Principle (open for extension, closed for modification)");
        System.out.println("   - Ripple effect: dependency changes break this class");
        System.out.println();
    }

    private void demonstrateWithDI() {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("--- WITH Dependency Injection (Best Practice) ---\n");

        System.out.println("Code: CommissionCalculationService");
        System.out.println("```java");
        System.out.println("@Service");
        System.out.println("public class CommissionCalculationService {");
        System.out.println("    private final NotificationService notificationService;  // ✅ Interface!");
        System.out.println("    private final AuditLogger auditLogger;                  // ✅ Interface!");
        System.out.println("");
        System.out.println("    @Autowired  // Dependencies INJECTED by Spring");
        System.out.println("    public CommissionCalculationService(");
        System.out.println("        NotificationService notificationService,  // ✅ Injected");
        System.out.println("        AuditLogger auditLogger) {                // ✅ Injected");
        System.out.println("        this.notificationService = notificationService;");
        System.out.println("        this.auditLogger = auditLogger;");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("```\n");

        System.out.println("Spring creates and injects dependencies:");
        System.out.println("✓ NotificationService → EmailNotificationService (@Primary)");
        System.out.println("✓ AuditLogger → EmailAuditLogger (@Qualifier)\n");

        // Create test deal
        Deal deal = new Deal("Enterprise License", new BigDecimal("10000"), "rep-002");
        deal.setId("deal-with-di");

        System.out.println("Calling calculateCommission()...\n");
        CommissionCalculation result = diService.calculateCommission(deal, "standard-plan");
        System.out.println("Result: $" + result.getNetCommission() + "\n");

        System.out.println("BENEFITS of Dependency Injection:");
        System.out.println("✅ 1. LOOSE COUPLING");
        System.out.println("   - Depends on interfaces (NotificationService, AuditLogger)");
        System.out.println("   - Can swap implementations without code changes (just configuration)");
        System.out.println("   - Changes to implementations don't affect this class");
        System.out.println();

        System.out.println("✅ 2. EASY TO TEST");
        System.out.println("   @Test");
        System.out.println("   public void testCalculate() {");
        System.out.println("       // Create mocks");
        System.out.println("       NotificationService mockNotif = mock(NotificationService.class);");
        System.out.println("       AuditLogger mockLogger = mock(AuditLogger.class);");
        System.out.println("       ");
        System.out.println("       // Inject mocks via constructor");
        System.out.println("       var service = new CommissionCalculationService(mockNotif, mockLogger);");
        System.out.println("       ");
        System.out.println("       // Test with mocks - no real emails/database!");
        System.out.println("       service.calculate(deal);");
        System.out.println("       ");
        System.out.println("       // Verify interactions");
        System.out.println("       verify(mockNotif).notify();  // ✅ Can verify calls!");
        System.out.println("   }");
        System.out.println();

        System.out.println("✅ 3. FOLLOWS SINGLE RESPONSIBILITY PRINCIPLE");
        System.out.println("   - Only responsible for commission calculation logic");
        System.out.println("   - Dependency creation is Spring's responsibility");
        System.out.println("   - Clear separation of concerns");
        System.out.println();

        System.out.println("✅ 4. EXPLICIT DEPENDENCIES");
        System.out.println("   - Constructor signature shows exactly what's needed:");
        System.out.println("     CommissionCalculationService(NotificationService, AuditLogger)");
        System.out.println("   - Dependencies are immediately visible");
        System.out.println("   - Self-documenting code");
        System.out.println();

        System.out.println("✅ 5. FLEXIBLE");
        System.out.println("   - Want SMS instead of Email?");
        System.out.println("     @Primary");
        System.out.println("     @Service");
        System.out.println("     public class SmsNotificationService implements NotificationService");
        System.out.println("   - No code changes to CommissionCalculationService!");
        System.out.println("   - Configure via Spring profiles (dev/prod)");
        System.out.println();

        System.out.println("✅ 6. EASY TO MAINTAIN");
        System.out.println("   - Changes to implementations don't affect this class");
        System.out.println("   - Follows Open-Closed Principle");
        System.out.println("   - Add new implementations without touching existing code");
        System.out.println();
    }

    private void explainDifferences() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SIDE-BY-SIDE COMPARISON");
        System.out.println("=".repeat(80) + "\n");

        System.out.println("┌─────────────────────────┬──────────────────────────┬──────────────────────────┐");
        System.out.println("│ Aspect                  │ WITHOUT DI               │ WITH DI                  │");
        System.out.println("├─────────────────────────┼──────────────────────────┼──────────────────────────┤");
        System.out.println("│ Dependency Type         │ Concrete class           │ Interface                │");
        System.out.println("│ Creation                │ new EmailService()       │ Spring injects           │");
        System.out.println("│ Testing                 │ Hard (real dependencies) │ Easy (inject mocks)      │");
        System.out.println("│ Flexibility             │ Hardcoded                │ Configurable             │");
        System.out.println("│ Coupling                │ Tight                    │ Loose                    │");
        System.out.println("│ Maintainability         │ Low                      │ High                     │");
        System.out.println("│ Change Impact           │ High                     │ Low                      │");
        System.out.println("│ Visibility              │ Hidden                   │ Explicit                 │");
        System.out.println("└─────────────────────────┴──────────────────────────┴──────────────────────────┘");
        System.out.println();

        System.out.println("TESTING COMPARISON:");
        System.out.println();

        System.out.println("WITHOUT DI:");
        System.out.println("❌ SimpleCalculationService service = new SimpleCalculationService();");
        System.out.println("   // Cannot inject mocks - stuck with real EmailService!");
        System.out.println("   // Tests will send real emails, write to real database");
        System.out.println("   // Cannot verify method calls or parameters");
        System.out.println();

        System.out.println("WITH DI:");
        System.out.println("✅ NotificationService mockService = mock(NotificationService.class);");
        System.out.println("✅ CommissionCalculationService service = new CommissionCalculationService(mockService);");
        System.out.println("   // Full control - inject mocks");
        System.out.println("   // No real side effects in tests");
        System.out.println("   // Can verify: verify(mockService).notify()");
        System.out.println();

        System.out.println("MAINTENANCE SCENARIO: Change from Email to SMS");
        System.out.println();

        System.out.println("WITHOUT DI:");
        System.out.println("❌ Must modify SimpleCalculationService code:");
        System.out.println("   1. Change field: EmailNotificationService → SmsNotificationService");
        System.out.println("   2. Change constructor: new EmailNotificationService() → new SmsNotificationService()");
        System.out.println("   3. Retest entire class even though logic didn't change");
        System.out.println("   4. Violates Open-Closed Principle");
        System.out.println();

        System.out.println("WITH DI:");
        System.out.println("✅ No changes to CommissionCalculationService!");
        System.out.println("   1. Create new SmsNotificationService implements NotificationService");
        System.out.println("   2. Add @Primary annotation");
        System.out.println("   3. Spring automatically injects new implementation");
        System.out.println("   4. Existing code unchanged and untouched");
        System.out.println();

        System.out.println("=".repeat(80));
        System.out.println("KEY TAKEAWAY:");
        System.out.println("=".repeat(80));
        System.out.println();
        System.out.println("Dependency Injection is NOT about Spring or frameworks.");
        System.out.println("It's a design principle that promotes:");
        System.out.println();
        System.out.println("  • Loose coupling between components");
        System.out.println("  • Testability through mock injection");
        System.out.println("  • Flexibility through configuration");
        System.out.println("  • Maintainability through separation of concerns");
        System.out.println();
        System.out.println("Spring (and other DI frameworks) just make it easier by:");
        System.out.println("  • Automatically creating objects");
        System.out.println("  • Resolving dependencies");
        System.out.println("  • Managing object lifecycle");
        System.out.println();
        System.out.println("You can (and should) apply DI principles even without a framework!");
        System.out.println();
        System.out.println("=".repeat(80) + "\n");
    }
}
