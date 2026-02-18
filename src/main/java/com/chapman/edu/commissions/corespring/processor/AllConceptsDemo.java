package com.chapman.edu.commissions.corespring.processor;

import com.chapman.edu.commissions.corespring.aop.CachingAspect;
import com.chapman.edu.commissions.corespring.core.*;
import com.chapman.edu.commissions.corespring.di.*;
import com.chapman.edu.commissions.model.Deal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Master demo that demonstrates ALL Spring Core concepts together.
 *
 * This demo shows how DI, IoC, Bean Lifecycle, Scopes, AOP, and Configuration
 * all work together in a real application.
 *
 * To run: Uncomment @Component annotation and start the application
 */
@Component  // ACTIVE BY DEFAULT - comment out to disable
public class AllConceptsDemo implements CommandLineRunner {

    // --- Dependency Injection Examples ---
    private final ApplicationContext context;
    private final CommissionCalculationService calculationService;
    private final CommissionRuleEngine ruleEngine;

    // --- Bean Scope Examples ---
    private final SingletonBean singletonBean;
    private final LifecycleBean lifecycleBean;

    // --- AOP Examples ---
    private final CachingAspect cachingAspect;

    // --- Configuration Examples ---
    private final Environment environment;

    public AllConceptsDemo(ApplicationContext context,
                          CommissionCalculationService calculationService,
                          CommissionRuleEngine ruleEngine,
                          SingletonBean singletonBean,
                          LifecycleBean lifecycleBean,
                          CachingAspect cachingAspect,
                          Environment environment) {
        this.context = context;
        this.calculationService = calculationService;
        this.ruleEngine = ruleEngine;
        this.singletonBean = singletonBean;
        this.lifecycleBean = lifecycleBean;
        this.cachingAspect = cachingAspect;
        this.environment = environment;
    }

    @Override
    public void run(String... args) throws Exception {
        printHeader();
        demonstrateAllConcepts();
        printFooter();
    }

    private void printHeader() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SPRING CORE CONCEPTS - COMPREHENSIVE DEMONSTRATION");
        System.out.println("=".repeat(80));
        System.out.println("This demo shows ALL concepts working together:");
        System.out.println("• Dependency Injection & IoC");
        System.out.println("• Bean Lifecycle & Scopes");
        System.out.println("• Aspect-Oriented Programming (AOP)");
        System.out.println("• Configuration Strategies");
        System.out.println("=".repeat(80) + "\n");
    }

    private void demonstrateAllConcepts() throws InterruptedException {
        // 1. Dependency Injection
        section("1. DEPENDENCY INJECTION (DI) & INVERSION OF CONTROL (IoC)");
        demonstrateDependencyInjection();

        // 2. Bean Scopes
        section("2. BEAN SCOPES");
        demonstrateBeanScopes();

        // 3. Bean Lifecycle
        section("3. BEAN LIFECYCLE");
        demonstrateBeanLifecycle();

        // 4. Aspect-Oriented Programming
        section("4. ASPECT-ORIENTED PROGRAMMING (AOP)");
        demonstrateAOP();

        // 5. Complete Workflow
        section("5. COMPLETE WORKFLOW - ALL CONCEPTS TOGETHER");
        demonstrateCompleteWorkflow();
    }

    private void demonstrateDependencyInjection() {
        System.out.println("CommissionCalculationService demonstrates three injection types:\n");

        System.out.println("✓ Constructor Injection (RECOMMENDED):");
        System.out.println("  - CommissionRuleEngine (required dependency)");
        System.out.println("  - NotificationService with @Qualifier (choosing specific implementation)");
        System.out.println();

        System.out.println("✓ Field Injection:");
        System.out.println("  - AuditLogger with @Qualifier(\"emailAuditLogger\")");
        System.out.println("  - Works but not recommended for production");
        System.out.println();

        System.out.println("✓ Setter Injection:");
        System.out.println("  - ValidationService with @Autowired(required = false)");
        System.out.println("  - Optional dependency - service works without it");
        System.out.println();

        System.out.println("Dependency Status: " + calculationService.getDependencyStatus());
        System.out.println();

        System.out.println("Key Principles:");
        System.out.println("• IoC: Spring controls object creation and lifecycle");
        System.out.println("• DI: Dependencies are injected, not created");
        System.out.println("• DIP: Depend on NotificationService interface, not concrete EmailService");
        System.out.println();
    }

    private void demonstrateBeanScopes() {
        System.out.println("Demonstrating Singleton vs Prototype scopes:\n");

        // Singleton
        System.out.println("Singleton Bean (default scope):");
        SingletonBean bean1 = context.getBean(SingletonBean.class);
        SingletonBean bean2 = context.getBean(SingletonBean.class);
        System.out.println("  Request 1: " + bean1.getCreatedAt());
        System.out.println("  Request 2: " + bean2.getCreatedAt());
        System.out.println("  Same instance? " + (bean1 == bean2));
        System.out.println("  ✓ One instance per container - shared by all");
        System.out.println();

        // Prototype
        System.out.println("Prototype Bean:");
        PrototypeBean proto1 = context.getBean(PrototypeBean.class);
        PrototypeBean proto2 = context.getBean(PrototypeBean.class);
        System.out.println("  Instance 1 ID: " + proto1.getInstanceId());
        System.out.println("  Instance 2 ID: " + proto2.getInstanceId());
        System.out.println("  Same instance? " + (proto1 == proto2));
        System.out.println("  ✓ New instance per request - stateful objects");
        System.out.println();

        System.out.println("Other scopes available:");
        System.out.println("  • request: One per HTTP request");
        System.out.println("  • session: One per HTTP session");
        System.out.println("  • application: One per ServletContext");
        System.out.println();
    }

    private void demonstrateBeanLifecycle() {
        System.out.println("Complete Bean Lifecycle (11 steps):\n");

        System.out.println("Initialization phase (check startup logs):");
        System.out.println("  1. Constructor");
        System.out.println("  2. Dependency Injection");
        System.out.println("  3. BeanPostProcessor.postProcessBeforeInitialization()");
        System.out.println("  4. @PostConstruct");
        System.out.println("  5. InitializingBean.afterPropertiesSet()");
        System.out.println("  6. Custom init-method");
        System.out.println("  7. BeanPostProcessor.postProcessAfterInitialization()");
        System.out.println("  8. ✓ Bean ready for use");
        System.out.println();

        System.out.println("Using the bean:");
        lifecycleBean.doWork();
        System.out.println();

        System.out.println("Destruction phase (watch at shutdown):");
        System.out.println("  9. @PreDestroy");
        System.out.println("  10. DisposableBean.destroy()");
        System.out.println("  11. Custom destroy-method");
        System.out.println();

        System.out.println("⚠️  Important: @PreDestroy NOT called for prototype beans!");
        System.out.println();
    }

    private void demonstrateAOP() {
        System.out.println("Aspect-Oriented Programming in action:\n");

        // Clear cache first
        cachingAspect.clearCache();

        Deal deal = new Deal("Demo Deal", new BigDecimal("10000"), "rep-demo");
        deal.setId("deal-demo");

        System.out.println("Calling calculateCommission() - watch for aspect output:");
        System.out.println("Expected aspects:");
        System.out.println("  • @Before: Log method entry");
        System.out.println("  • @Around: Measure performance");
        System.out.println("  • @AfterReturning: Log result");
        System.out.println("  • @After: Final cleanup");
        System.out.println();

        calculationService.calculateCommission(deal, "standard-plan");

        System.out.println("\nCaching aspect (@Around advice):");
        System.out.println("First call - cache MISS:");
        ruleEngine.calculateBaseCommission(deal, "plan-1");

        System.out.println("Second call - cache HIT (method not executed):");
        ruleEngine.calculateBaseCommission(deal, "plan-1");
        System.out.println();

        System.out.println("Cross-cutting concerns handled by AOP:");
        System.out.println("  ✓ Logging and auditing");
        System.out.println("  ✓ Performance monitoring");
        System.out.println("  ✓ Caching");
        System.out.println("  ✓ Security (can be added)");
        System.out.println("  ✓ Transaction management (can be added)");
        System.out.println();
    }

    private void demonstrateCompleteWorkflow() {
        System.out.println("Complete workflow showing all concepts:\n");

        // Create realistic scenario
        Deal enterpriseDeal = new Deal(
            "Enterprise License Agreement",
            new BigDecimal("50000"),
            "sales-rep-001"
        );
        enterpriseDeal.setId("deal-enterprise-001");

        System.out.println("Processing enterprise deal:");
        System.out.println("  Deal: " + enterpriseDeal.getTitle());
        System.out.println("  Value: $" + enterpriseDeal.getValue());
        System.out.println("  Sales Rep: " + enterpriseDeal.getSalesRepId());
        System.out.println();

        System.out.println("What happens behind the scenes:");
        System.out.println();

        System.out.println("1. DEPENDENCY INJECTION:");
        System.out.println("   Spring injects:");
        System.out.println("   • CommissionRuleEngine (constructor injection)");
        System.out.println("   • EmailNotificationService (@Primary)");
        System.out.println("   • EmailAuditLogger (@Qualifier)");
        System.out.println("   • ValidationService (optional)");
        System.out.println();

        System.out.println("2. BEAN SCOPES:");
        System.out.println("   • CommissionCalculationService: Singleton (shared)");
        System.out.println("   • Rule engines and services: Singletons (stateless)");
        System.out.println();

        System.out.println("3. AOP PROXIES:");
        System.out.println("   Call goes through proxy:");
        System.out.println("   Client → Proxy → Aspects → Target Method");
        System.out.println();

        System.out.println("4. ASPECT EXECUTION:");
        System.out.println("   • AuditingAspect: Logs operation");
        System.out.println("   • PerformanceAspect: Measures execution time");
        System.out.println("   • CachingAspect: Checks/stores cache");
        System.out.println();

        System.out.println("Executing...\n");
        var result = calculationService.calculateCommission(enterpriseDeal, "enterprise-plan");

        System.out.println("\nResult:");
        System.out.println("  Commission calculated: $" + result.getNetCommission());
        System.out.println();

        System.out.println("5. LIFECYCLE EVENTS:");
        System.out.println("   • Beans already initialized (@PostConstruct called at startup)");
        System.out.println("   • BeanPostProcessor added behavior");
        System.out.println("   • At shutdown: @PreDestroy will be called");
        System.out.println();
    }

    private void section(String title) {
        System.out.println("-".repeat(80));
        System.out.println(title);
        System.out.println("-".repeat(80));
        System.out.println();
    }

    private void printFooter() {
        System.out.println("=".repeat(80));
        System.out.println("DEMO COMPLETE");
        System.out.println("=".repeat(80));
        System.out.println();
        System.out.println("Individual demos available:");
        System.out.println("  • DependencyInjectionDemo - DI concepts in depth");
        System.out.println("  • BeanLifecycleDemo - Lifecycle and scopes");
        System.out.println("  • AopDemo - Aspect-Oriented Programming");
        System.out.println("  • ConfigurationDemo - Configuration strategies");
        System.out.println();
        System.out.println("To run individual demos:");
        System.out.println("  1. Comment out @Component on AllConceptsDemo");
        System.out.println("  2. Uncomment @Component on the specific demo");
        System.out.println("  3. Restart the application");
        System.out.println();
        System.out.println("Watch the console during application shutdown to see:");
        System.out.println("  • @PreDestroy methods being called");
        System.out.println("  • Bean destruction lifecycle");
        System.out.println();
        System.out.println("=".repeat(80) + "\n");
    }
}
