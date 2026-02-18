package com.chapman.edu.commissions.corespring.processor;

import com.chapman.edu.commissions.corespring.aop.CachingAspect;
import com.chapman.edu.commissions.corespring.di.CommissionCalculationService;
import com.chapman.edu.commissions.corespring.di.CommissionRuleEngine;
import com.chapman.edu.commissions.model.Deal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;

/**
 * Runnable demo showing AOP Fundamentals.
 *
 * Run this class to see:
 * - @Before, @After, @AfterReturning, @Around advice in action
 * - Performance monitoring aspect
 * - Caching aspect
 * - Auditing aspect
 * - Security aspect (if enabled)
 *
 * To run: Uncomment @Component annotation and start the application
 */
//@Component  // Uncomment to run this demo
public class AopDemo implements CommandLineRunner {

    private final ApplicationContext context;
    private final CommissionCalculationService calculationService;
    private final CommissionRuleEngine ruleEngine;
    private final CachingAspect cachingAspect;

    public AopDemo(ApplicationContext context,
                  CommissionCalculationService calculationService,
                  CommissionRuleEngine ruleEngine,
                  CachingAspect cachingAspect) {
        this.context = context;
        this.calculationService = calculationService;
        this.ruleEngine = ruleEngine;
        this.cachingAspect = cachingAspect;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========================================");
        System.out.println("AOP (ASPECT-ORIENTED PROGRAMMING) DEMO");
        System.out.println("========================================\n");

        demonstrateAdviceTypes();
        demonstrateCachingAspect();
        demonstratePerformanceMonitoring();
        explainProxyMechanism();
    }

    private void demonstrateAdviceTypes() {
        System.out.println("--- AOP Advice Types ---");
        System.out.println("Watch the console output to see aspects in action!\n");

        // Create a deal for testing
        Deal deal = new Deal("Cloud Services Contract", new BigDecimal("25000"), "rep-002");
        deal.setId("deal-002");

        System.out.println("Calling calculateCommission() - watch for aspect output:\n");

        // This method has @Auditable annotation
        // Will trigger:
        // - @Before advice (logs parameters before execution)
        // - @Around advice (measures performance)
        // - @After advice (cleanup after execution)
        // - @AfterReturning advice (logs successful result)
        calculationService.calculateCommission(deal, "premium-plan");

        System.out.println("\nAspects that ran:");
        System.out.println("✓ @Before - AuditingAspect.logBefore()");
        System.out.println("✓ @Around - AuditingAspect.measurePerformance()");
        System.out.println("✓ @AfterReturning - AuditingAspect.logAfterReturning()");
        System.out.println("✓ @After - AuditingAspect.logAfter()");
        System.out.println();
    }

    private void demonstrateCachingAspect() {
        System.out.println("--- Caching Aspect (@Around Advice) ---");
        System.out.println("CachingAspect caches results of calculation methods\n");

        Deal deal = new Deal("Software License", new BigDecimal("10000"), "rep-003");
        deal.setId("deal-003");

        System.out.println("First call - cache MISS (will execute method):");
        BigDecimal result1 = ruleEngine.calculateBaseCommission(deal, "standard-plan");
        System.out.println("Result: $" + result1);

        System.out.println("\nSecond call with same parameters - cache HIT (won't execute method):");
        BigDecimal result2 = ruleEngine.calculateBaseCommission(deal, "standard-plan");
        System.out.println("Result: $" + result2);

        System.out.println("\nThird call with different parameters - cache MISS:");
        BigDecimal result3 = ruleEngine.calculateBonuses(deal, "premium-plan");
        System.out.println("Result: $" + result3);

        System.out.println("\nCache statistics:");
        System.out.println("Cached entries: " + cachingAspect.getCacheSize());

        System.out.println("\nClearing cache...");
        cachingAspect.clearCache();
        System.out.println("Cached entries: " + cachingAspect.getCacheSize());

        System.out.println("\nKey Concept:");
        System.out.println("@Around advice can:");
        System.out.println("- Execute code before and after method");
        System.out.println("- Skip method execution entirely (return cached value)");
        System.out.println("- Modify parameters or return value");
        System.out.println();
    }

    private void demonstratePerformanceMonitoring() {
        System.out.println("--- Performance Monitoring (@Around Advice) ---");

        Deal deal = new Deal("Enterprise Agreement", new BigDecimal("50000"), "rep-004");
        deal.setId("deal-004");

        System.out.println("Calling calculation method - performance will be measured:\n");

        // @Around advice measures execution time
        ruleEngine.calculateBaseCommission(deal, "enterprise-plan");

        System.out.println("\nPerformance aspect wraps method execution:");
        System.out.println("1. Record start time");
        System.out.println("2. Call joinPoint.proceed() to execute method");
        System.out.println("3. Record end time and calculate duration");
        System.out.println("4. Log execution time");
        System.out.println();
    }

    private void explainProxyMechanism() {
        System.out.println("--- Understanding AOP Proxies ---");

        Object serviceBean = context.getBean("commissionCalculationService");
        System.out.println("CommissionCalculationService bean class:");
        System.out.println(serviceBean.getClass().getName());

        if (serviceBean.getClass().getName().contains("CGLIB") ||
            serviceBean.getClass().getName().contains("Proxy")) {
            System.out.println("✓ This is a PROXY (not the original class)");
            System.out.println("✓ Spring AOP wrapped the bean to add aspect behavior");
        }

        System.out.println("\nHow AOP works:");
        System.out.println("1. Spring creates proxy for beans with aspects");
        System.out.println("2. Proxy wraps your original bean");
        System.out.println("3. Method calls go through proxy first");
        System.out.println("4. Proxy executes aspect advice");
        System.out.println("5. Proxy delegates to original bean");
        System.out.println();

        System.out.println("Call flow:");
        System.out.println("Client → Proxy → Aspect (@Before)");
        System.out.println("              ↓");
        System.out.println("         Original Bean Method");
        System.out.println("              ↓");
        System.out.println("         Aspect (@AfterReturning)");
        System.out.println("              ↓");
        System.out.println("         Return to Client");
        System.out.println();

        System.out.println("⚠️  Self-Invocation Pitfall:");
        System.out.println("   If a method calls another method in the SAME class,");
        System.out.println("   the call bypasses the proxy → aspects NOT applied!");
        System.out.println();
    }
}
