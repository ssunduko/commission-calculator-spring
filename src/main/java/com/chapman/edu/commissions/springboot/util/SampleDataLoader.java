package com.chapman.edu.commissions.springboot.util;

import com.chapman.edu.commissions.model.*;
import com.chapman.edu.commissions.springboot.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

/**
 * ============================================================================
 * SAMPLE DATA LOADER — CommandLineRunner
 * ============================================================================
 *
 * CONCEPT: CommandLineRunner
 * ----------------------------
 * CommandLineRunner is a Spring Boot interface that allows you to run code
 * after the application context is fully initialized. Any bean implementing
 * this interface has its run() method called after all beans are created.
 *
 * Common uses:
 *   - Loading sample/seed data into the database
 *   - Running initialization logic
 *   - Performing health checks
 *   - Warming up caches
 *
 * If you need access to the raw command-line arguments as ApplicationArguments,
 * use ApplicationRunner instead.
 *
 * CONCEPT: Constructor Injection with Multiple Dependencies
 * -----------------------------------------------------------
 * This class demonstrates constructor injection with five repository beans
 * and a PasswordEncoder. Spring resolves ALL dependencies and injects them:
 *
 *   1. Spring scans for beans matching each constructor parameter type
 *   2. If exactly one bean of that type exists, it's injected
 *   3. If multiple beans exist, use @Qualifier to specify which one
 *   4. If no bean exists, Spring fails fast at startup with a clear error
 *
 * @see org.springframework.boot.CommandLineRunner
 */
@Component
@Order(1)  // Run FIRST — before all processor demos that depend on sample data
public class SampleDataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SampleDataLoader.class);

    private final UserRepository userRepository;
    private final DealRepository dealRepository;
    private final CommissionPlanRepository planRepository;
    private final CommissionCalculationRepository calculationRepository;
    private final DisputeRepository disputeRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor injection — Spring injects all repository beans automatically.
     * The @Autowired annotation is optional with a single constructor.
     */
    public SampleDataLoader(UserRepository userRepository,
                            DealRepository dealRepository,
                            CommissionPlanRepository planRepository,
                            CommissionCalculationRepository calculationRepository,
                            DisputeRepository disputeRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.dealRepository = dealRepository;
        this.planRepository = planRepository;
        this.calculationRepository = calculationRepository;
        this.disputeRepository = disputeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        logger.info("=== Loading Sample Data ===");

        loadUsers();
        loadCommissionPlans();
        loadDeals();
        loadCommissionCalculations();
        loadDisputes();

        logger.info("=== Sample Data Loaded Successfully ===");
        logger.info("  Users: {}", userRepository.count());
        logger.info("  Deals: {}", dealRepository.count());
        logger.info("  Plans: {}", planRepository.count());
        logger.info("  Calculations: {}", calculationRepository.count());
        logger.info("  Disputes: {}", disputeRepository.count());
    }

    private void loadUsers() {
        // System Admin
        User admin = new User("admin", "admin@chapman.edu", "System", "Administrator");
        admin.setId("user-001");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.addRole(UserRole.SYSTEM_ADMIN);
        admin.setDepartment("IT");
        userRepository.save(admin);

        // Sales Manager
        User manager = new User("jsmith", "jsmith@chapman.edu", "John", "Smith");
        manager.setId("user-002");
        manager.setPasswordHash(passwordEncoder.encode("password123"));
        manager.addRole(UserRole.SALES_MANAGER);
        manager.setDepartment("Sales");
        manager.setTerritory("West Coast");
        userRepository.save(manager);

        // Sales Representatives
        User rep1 = new User("agarcia", "agarcia@chapman.edu", "Ana", "Garcia");
        rep1.setId("user-003");
        rep1.setPasswordHash(passwordEncoder.encode("password123"));
        rep1.addRole(UserRole.SALES_REP);
        rep1.setDepartment("Sales");
        rep1.setTerritory("California");
        rep1.setManagerId("user-002");
        userRepository.save(rep1);

        User rep2 = new User("bwilson", "bwilson@chapman.edu", "Brian", "Wilson");
        rep2.setId("user-004");
        rep2.setPasswordHash(passwordEncoder.encode("password123"));
        rep2.addRole(UserRole.SALES_REP);
        rep2.setDepartment("Sales");
        rep2.setTerritory("Oregon");
        rep2.setManagerId("user-002");
        userRepository.save(rep2);

        User rep3 = new User("clee", "clee@chapman.edu", "Christine", "Lee");
        rep3.setId("user-005");
        rep3.setPasswordHash(passwordEncoder.encode("password123"));
        rep3.addRole(UserRole.SALES_REP);
        rep3.setDepartment("Sales");
        rep3.setTerritory("Washington");
        rep3.setManagerId("user-002");
        userRepository.save(rep3);

        // Finance Admin
        User finance = new User("dfinance", "dfinance@chapman.edu", "Diana", "Finance");
        finance.setId("user-006");
        finance.setPasswordHash(passwordEncoder.encode("password123"));
        finance.addRole(UserRole.FINANCE_ADMIN);
        finance.setDepartment("Finance");
        userRepository.save(finance);

        logger.info("  Loaded {} users", userRepository.count());
    }

    private void loadCommissionPlans() {
        // Standard Commission Plan
        CommissionPlan standardPlan = new CommissionPlan("Standard Sales Plan Q1 2024", Currency.getInstance("USD"));
        standardPlan.setId("plan-001");
        standardPlan.setStatus(PlanStatus.ACTIVE);
        standardPlan.setEffectiveStartDate(LocalDate.of(2024, 1, 1));
        standardPlan.setEffectiveEndDate(LocalDate.of(2024, 12, 31));
        standardPlan.setCreatedBy("user-001");

        // Add tiers to standard plan
        CommissionTier tier1 = new CommissionTier("Bronze", BigDecimal.ZERO, new BigDecimal("10000"), new BigDecimal("5"));
        tier1.setId("tier-001");
        tier1.setPlanId("plan-001");
        standardPlan.addTier(tier1);

        CommissionTier tier2 = new CommissionTier("Silver", new BigDecimal("10000"), new BigDecimal("50000"), new BigDecimal("8"));
        tier2.setId("tier-002");
        tier2.setPlanId("plan-001");
        standardPlan.addTier(tier2);

        CommissionTier tier3 = new CommissionTier("Gold", new BigDecimal("50000"), new BigDecimal("100000"), new BigDecimal("12"));
        tier3.setId("tier-003");
        tier3.setPlanId("plan-001");
        standardPlan.addTier(tier3);

        CommissionTier tier4 = new CommissionTier("Platinum", new BigDecimal("100000"), null, new BigDecimal("15"));
        tier4.setId("tier-004");
        tier4.setPlanId("plan-001");
        standardPlan.addTier(tier4);

        // Add commission rules
        CommissionRule standardRule = new CommissionRule("Standard Rate", new BigDecimal("10"), CommissionRule.RuleType.STANDARD);
        standardRule.setId("rule-001");
        standardRule.setDescription("Standard 10% commission on all deals");
        standardRule.setPlanId("plan-001");
        standardPlan.addRule(standardRule);

        // Add bonus rules
        BonusRule spifBonus = new BonusRule("Q1 SPIF Bonus", new BigDecimal("500"), false, BonusRule.BonusType.SPIF);
        spifBonus.setId("bonus-001");
        spifBonus.setDescription("$500 bonus for every deal over $25,000");
        spifBonus.setPlanId("plan-001");
        spifBonus.setStartDate(LocalDate.of(2024, 1, 1));
        spifBonus.setEndDate(LocalDate.of(2024, 3, 31));
        standardPlan.addBonus(spifBonus);

        BonusRule quotaBonus = new BonusRule("Quota Achievement Bonus", new BigDecimal("10"), true, BonusRule.BonusType.QUOTA_ACHIEVEMENT);
        quotaBonus.setId("bonus-002");
        quotaBonus.setDescription("10% bonus on total commission when quarterly quota is met");
        quotaBonus.setPlanId("plan-001");
        standardPlan.addBonus(quotaBonus);

        planRepository.save(standardPlan);

        // Premium Commission Plan
        CommissionPlan premiumPlan = new CommissionPlan("Premium Enterprise Plan", Currency.getInstance("USD"));
        premiumPlan.setId("plan-002");
        premiumPlan.setStatus(PlanStatus.ACTIVE);
        premiumPlan.setEffectiveStartDate(LocalDate.of(2024, 1, 1));
        premiumPlan.setCreatedBy("user-001");

        CommissionRule premiumRule = new CommissionRule("Premium Rate", new BigDecimal("15"), CommissionRule.RuleType.STANDARD);
        premiumRule.setId("rule-002");
        premiumRule.setDescription("15% commission for enterprise deals");
        premiumRule.setPlanId("plan-002");
        premiumPlan.addRule(premiumRule);

        planRepository.save(premiumPlan);

        // Draft Plan
        CommissionPlan draftPlan = new CommissionPlan("2025 New Sales Plan", Currency.getInstance("USD"));
        draftPlan.setId("plan-003");
        draftPlan.setStatus(PlanStatus.DRAFT);
        draftPlan.setCreatedBy("user-002");
        planRepository.save(draftPlan);

        logger.info("  Loaded {} commission plans", planRepository.count());
    }

    private void loadDeals() {
        // Ana Garcia's deals
        Deal deal1 = new Deal("Acme Corp Software License", new BigDecimal("45000"), "user-003");
        deal1.setId("deal-001");
        deal1.setStatus(DealStatus.WON);
        deal1.setCloseDate(LocalDate.of(2024, 2, 15));
        deal1.addProduct(new DealProduct("prod-001", "Enterprise License", 3, new BigDecimal("10000")));
        deal1.addProduct(new DealProduct("prod-002", "Support Package", 3, new BigDecimal("5000")));
        dealRepository.save(deal1);

        Deal deal2 = new Deal("Beta Inc Cloud Migration", new BigDecimal("78000"), "user-003");
        deal2.setId("deal-002");
        deal2.setStatus(DealStatus.WON);
        deal2.setCloseDate(LocalDate.of(2024, 3, 20));
        deal2.addProduct(new DealProduct("prod-003", "Cloud Platform", 1, new BigDecimal("50000")));
        deal2.addProduct(new DealProduct("prod-004", "Migration Service", 1, new BigDecimal("28000")));
        dealRepository.save(deal2);

        // Brian Wilson's deals
        Deal deal3 = new Deal("Gamma Ltd Hardware Refresh", new BigDecimal("32000"), "user-004");
        deal3.setId("deal-003");
        deal3.setStatus(DealStatus.WON);
        deal3.setCloseDate(LocalDate.of(2024, 1, 30));
        deal3.addProduct(new DealProduct("prod-005", "Server Equipment", 4, new BigDecimal("8000")));
        dealRepository.save(deal3);

        Deal deal4 = new Deal("Delta Corp Security Suite", new BigDecimal("15000"), "user-004");
        deal4.setId("deal-004");
        deal4.setStatus(DealStatus.OPEN);
        deal4.addProduct(new DealProduct("prod-006", "Security License", 10, new BigDecimal("1500")));
        dealRepository.save(deal4);

        // Christine Lee's deals
        Deal deal5 = new Deal("Epsilon Systems Integration", new BigDecimal("120000"), "user-005");
        deal5.setId("deal-005");
        deal5.setStatus(DealStatus.WON);
        deal5.setCloseDate(LocalDate.of(2024, 3, 1));
        deal5.addProduct(new DealProduct("prod-007", "Integration Platform", 1, new BigDecimal("80000")));
        deal5.addProduct(new DealProduct("prod-008", "Professional Services", 1, new BigDecimal("40000")));
        dealRepository.save(deal5);

        Deal deal6 = new Deal("Zeta Inc Consulting", new BigDecimal("8500"), "user-005");
        deal6.setId("deal-006");
        deal6.setStatus(DealStatus.LOST);
        dealRepository.save(deal6);

        logger.info("  Loaded {} deals", dealRepository.count());
    }

    private void loadCommissionCalculations() {
        // Calculation for deal-001
        CommissionCalculation calc1 = new CommissionCalculation("deal-001", "user-003", new BigDecimal("4500"));
        calc1.setId("calc-001");
        calc1.setStatus(CommissionCalculation.CommissionStatus.APPROVED);
        calc1.setPlanId("plan-001");
        calc1.setCalculatedBy("user-002");
        calc1.setGrossCommission(new BigDecimal("5000"));
        calc1.setNetCommission(new BigDecimal("5000"));
        calc1.addBonus(new BonusCalculation("bonus-001", "Q1 SPIF Bonus", new BigDecimal("500")));
        calculationRepository.save(calc1);

        // Calculation for deal-002
        CommissionCalculation calc2 = new CommissionCalculation("deal-002", "user-003", new BigDecimal("7800"));
        calc2.setId("calc-002");
        calc2.setStatus(CommissionCalculation.CommissionStatus.CALCULATED);
        calc2.setPlanId("plan-001");
        calc2.setCalculatedBy("user-002");
        calc2.setGrossCommission(new BigDecimal("8300"));
        calc2.setNetCommission(new BigDecimal("8300"));
        calc2.addBonus(new BonusCalculation("bonus-001", "Q1 SPIF Bonus", new BigDecimal("500")));
        calculationRepository.save(calc2);

        // Calculation for deal-003
        CommissionCalculation calc3 = new CommissionCalculation("deal-003", "user-004", new BigDecimal("3200"));
        calc3.setId("calc-003");
        calc3.setStatus(CommissionCalculation.CommissionStatus.PAID);
        calc3.setPlanId("plan-001");
        calc3.setCalculatedBy("user-002");
        calc3.setGrossCommission(new BigDecimal("3700"));
        calc3.setNetCommission(new BigDecimal("3700"));
        calc3.setPayoutDate(LocalDate.of(2024, 2, 28));
        calc3.addBonus(new BonusCalculation("bonus-001", "Q1 SPIF Bonus", new BigDecimal("500")));
        calculationRepository.save(calc3);

        // Calculation for deal-005
        CommissionCalculation calc4 = new CommissionCalculation("deal-005", "user-005", new BigDecimal("18000"));
        calc4.setId("calc-004");
        calc4.setStatus(CommissionCalculation.CommissionStatus.DISPUTED);
        calc4.setPlanId("plan-002");
        calc4.setCalculatedBy("user-002");
        calc4.setGrossCommission(new BigDecimal("18000"));
        calc4.setNetCommission(new BigDecimal("18000"));
        calc4.addAccelerator(new AcceleratorCalculation("rule-002", "Premium Accelerator", new BigDecimal("1.0")));
        calculationRepository.save(calc4);

        logger.info("  Loaded {} commission calculations", calculationRepository.count());
    }

    private void loadDisputes() {
        // Dispute on calc-004
        Dispute dispute1 = new Dispute("calc-004", "user-005", "Incorrect Commission Rate Applied", "The premium enterprise rate of 15% should have been applied to the Epsilon Systems Integration deal, but it appears the standard 10% rate was used for the base calculation. The deal qualifies for the Premium Enterprise Plan as per our agreement.");
        dispute1.setId("dispute-001");
        dispute1.setManagerId("user-002");
        dispute1.addUserComment("user-005", "Christine Lee", "I've attached the signed enterprise agreement showing the 15% rate.");
        dispute1.addSystemComment("Dispute assigned to manager John Smith for review.");
        disputeRepository.save(dispute1);

        logger.info("  Loaded {} disputes", disputeRepository.count());
    }
}
