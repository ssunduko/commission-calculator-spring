package com.chapman.edu.commissions.ai.util;

import com.chapman.edu.commissions.ai.service.vectorstore.CommissionDocumentService;
import com.chapman.edu.commissions.orm.entity.*;
import com.chapman.edu.commissions.orm.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

/**
 * ============================================================
 * DATA INITIALIZER: AI Module Bootstrap Data
 * ============================================================
 *
 * CONCEPT: Application Startup Data Loading
 * ------------------------------------------------------------
 * This class initializes the H2 database with sample commission data
 * and loads it into the vector store for RAG queries.
 *
 * CommandLineRunner:
 * A Spring Boot interface whose run() method executes AFTER the
 * application context is fully initialized. This ensures all
 * beans (repositories, services, vector store) are available.
 *
 * INITIALIZATION ORDER:
 * 1. Spring Boot starts and creates all beans
 * 2. Flyway runs database migrations (creates tables)
 * 3. CommandLineRunner executes (inserts sample data)
 * 4. CommissionDocumentService loads data into vector store
 * 5. Application is ready to serve requests
 *
 * WHY SAMPLE DATA?
 * For an educational application, having pre-loaded data allows
 * students to immediately test AI features without manual setup.
 * The data represents a realistic commission scenario with:
 * - Multiple sales reps with different performance levels
 * - Various deal sizes and statuses
 * - Commission plans with tiered rates
 * - Calculated commissions for RAG queries
 */
@Configuration
public class SampleDataLoader {

    private static final Logger log = LoggerFactory.getLogger(SampleDataLoader.class);

    @Bean
    public CommandLineRunner initializeAiData(
            UserRepository userRepository,
            DealRepository dealRepository,
            CommissionPlanRepository planRepository,
            CommissionCalculationRepository calculationRepository,
            CommissionDocumentService documentService) {

        return args -> {
            // Only initialize if database is empty
            if (userRepository.count() > 0) {
                log.info("Database already contains data, skipping initialization");
                // Still load existing data into vector store
                documentService.loadAllDocuments();
                return;
            }

            log.info("Initializing sample commission data for AI module...");

            // ---- Create Users ----
            User alice = new User("alice.johnson", "alice@chapman.edu", "Alice", "Johnson");
            alice.addRole(UserRole.SALES_REP);
            alice.setDepartment("Enterprise Sales");
            alice.setTerritory("West Coast");

            User bob = new User("bob.smith", "bob@chapman.edu", "Bob", "Smith");
            bob.addRole(UserRole.SALES_REP);
            bob.setDepartment("SMB Sales");
            bob.setTerritory("East Coast");

            User carol = new User("carol.manager", "carol@chapman.edu", "Carol", "Williams");
            carol.addRole(UserRole.SALES_MANAGER);
            carol.setDepartment("Enterprise Sales");
            carol.setTerritory("National");

            User dave = new User("dave.admin", "dave@chapman.edu", "Dave", "Brown");
            dave.addRole(UserRole.FINANCE_ADMIN);
            dave.setDepartment("Finance");

            userRepository.saveAll(List.of(alice, bob, carol, dave));

            // Set manager relationships
            alice.setManager(carol);
            bob.setManager(carol);
            userRepository.saveAll(List.of(alice, bob));

            log.info("Created {} users", userRepository.count());

            // ---- Create Commission Plan ----
            CommissionPlan standardPlan = new CommissionPlan("Standard Sales Plan", Currency.getInstance("USD"));
            standardPlan.setStatus(PlanStatus.ACTIVE);
            standardPlan.setEffectiveStartDate(LocalDate.of(2025, 1, 1));
            standardPlan.setEffectiveEndDate(LocalDate.of(2026, 12, 31));
            standardPlan.setCreatedBy("system");

            // Add commission tiers
            CommissionTier tier1 = new CommissionTier("Starter", BigDecimal.ZERO,
                    new BigDecimal("25000"), new BigDecimal("5"));
            CommissionTier tier2 = new CommissionTier("Growth", new BigDecimal("25000"),
                    new BigDecimal("75000"), new BigDecimal("8"));
            CommissionTier tier3 = new CommissionTier("Enterprise", new BigDecimal("75000"),
                    new BigDecimal("200000"), new BigDecimal("12"));
            CommissionTier tier4 = new CommissionTier("Strategic", new BigDecimal("200000"),
                    null, new BigDecimal("15"));

            standardPlan.addTier(tier1);
            standardPlan.addTier(tier2);
            standardPlan.addTier(tier3);
            standardPlan.addTier(tier4);

            // Add a bonus rule
            BonusRule quarterlyBonus = new BonusRule("Q1 Accelerator", new BigDecimal("10"),
                    true, BonusType.ACCELERATOR);
            quarterlyBonus.setDescription("10% bonus on all commissions during Q1");
            quarterlyBonus.setStartDate(LocalDate.of(2026, 1, 1));
            quarterlyBonus.setEndDate(LocalDate.of(2026, 3, 31));
            standardPlan.addBonus(quarterlyBonus);

            planRepository.save(standardPlan);
            log.info("Created commission plan: {}", standardPlan.getName());

            // ---- Create Deals ----
            Deal deal1 = new Deal("Acme Corp Enterprise License", new BigDecimal("150000"), alice);
            deal1.setStatus(DealStatus.WON);
            deal1.setCloseDate(LocalDate.of(2026, 1, 15));

            Deal deal2 = new Deal("TechStart SaaS Subscription", new BigDecimal("35000"), bob);
            deal2.setStatus(DealStatus.WON);
            deal2.setCloseDate(LocalDate.of(2026, 1, 20));

            Deal deal3 = new Deal("Global Industries Platform", new BigDecimal("250000"), alice);
            deal3.setStatus(DealStatus.WON);
            deal3.setCloseDate(LocalDate.of(2026, 2, 1));

            Deal deal4 = new Deal("SmallBiz Pro Package", new BigDecimal("12000"), bob);
            deal4.setStatus(DealStatus.WON);
            deal4.setCloseDate(LocalDate.of(2026, 2, 5));

            Deal deal5 = new Deal("MegaCorp Digital Transformation", new BigDecimal("500000"), alice);
            deal5.setStatus(DealStatus.OPEN);

            Deal deal6 = new Deal("Startup Essentials Bundle", new BigDecimal("8000"), bob);
            deal6.setStatus(DealStatus.OPEN);

            Deal deal7 = new Deal("Regional Health Network", new BigDecimal("95000"), alice);
            deal7.setStatus(DealStatus.OPEN);

            dealRepository.saveAll(List.of(deal1, deal2, deal3, deal4, deal5, deal6, deal7));
            log.info("Created {} deals", dealRepository.count());

            // ---- Create Commission Calculations ----
            // Calculation for deal1: $150,000 at 12% (Enterprise tier) = $18,000
            CommissionCalculation calc1 = new CommissionCalculation(deal1, alice,
                    new BigDecimal("18000"));
            calc1.setPlan(standardPlan);
            calc1.setGrossCommission(new BigDecimal("19800")); // with 10% Q1 bonus
            calc1.setNetCommission(new BigDecimal("19800"));
            calc1.setCalculatedBy("system");
            calc1.setStatus(CommissionStatus.APPROVED);

            // Calculation for deal2: $35,000 at 8% (Growth tier) = $2,800
            CommissionCalculation calc2 = new CommissionCalculation(deal2, bob,
                    new BigDecimal("2800"));
            calc2.setPlan(standardPlan);
            calc2.setGrossCommission(new BigDecimal("3080")); // with 10% Q1 bonus
            calc2.setNetCommission(new BigDecimal("3080"));
            calc2.setCalculatedBy("system");
            calc2.setStatus(CommissionStatus.APPROVED);

            // Calculation for deal3: $250,000 at 15% (Strategic tier) = $37,500
            CommissionCalculation calc3 = new CommissionCalculation(deal3, alice,
                    new BigDecimal("37500"));
            calc3.setPlan(standardPlan);
            calc3.setGrossCommission(new BigDecimal("37500"));
            calc3.setNetCommission(new BigDecimal("37500"));
            calc3.setCalculatedBy("system");
            calc3.setStatus(CommissionStatus.CALCULATED);

            // Calculation for deal4: $12,000 at 5% (Starter tier) = $600
            CommissionCalculation calc4 = new CommissionCalculation(deal4, bob,
                    new BigDecimal("600"));
            calc4.setPlan(standardPlan);
            calc4.setGrossCommission(new BigDecimal("600"));
            calc4.setNetCommission(new BigDecimal("600"));
            calc4.setCalculatedBy("system");
            calc4.setStatus(CommissionStatus.CALCULATED);

            calculationRepository.saveAll(List.of(calc1, calc2, calc3, calc4));
            log.info("Created {} commission calculations", calculationRepository.count());

            // ---- Load all data into Vector Store for RAG ----
            log.info("Loading data into vector store for RAG...");
            documentService.loadAllDocuments();

            log.info("AI module data initialization complete!");
        };
    }
}
