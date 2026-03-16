package com.chapman.edu.commissions.architecture.cleanarchitecture.infrastructure.data;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.CommissionCalculationRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.CommissionPlanRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.DealRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.DisputeRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.UserRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.BonusRule;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.BonusType;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionCalculation;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionPlan;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionRule;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionStatus;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionTier;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.Deal;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DealStatus;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.Dispute;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DisputeStatus;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.PlanStatus;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.RuleType;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

/**
 * Initializes sample data on startup using output ports (not Spring Data repos directly)
 * to demonstrate clean architecture dependency rules.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepositoryPort userRepository;
    private final DealRepositoryPort dealRepository;
    private final CommissionPlanRepositoryPort planRepository;
    private final CommissionCalculationRepositoryPort calculationRepository;
    private final DisputeRepositoryPort disputeRepository;

    public DataInitializer(
            UserRepositoryPort userRepository,
            DealRepositoryPort dealRepository,
            CommissionPlanRepositoryPort planRepository,
            CommissionCalculationRepositoryPort calculationRepository,
            DisputeRepositoryPort disputeRepository
    ) {
        this.userRepository = userRepository;
        this.dealRepository = dealRepository;
        this.planRepository = planRepository;
        this.calculationRepository = calculationRepository;
        this.disputeRepository = disputeRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing clean architecture sample data...");

        // Create users first (FK constraint)
        User rep1 = createUser("clean_rep001", "clean_rep001@company.com", "Alice", "Johnson");
        User rep2 = createUser("clean_rep002", "clean_rep002@company.com", "Bob", "Smith");
        User rep3 = createUser("clean_rep003", "clean_rep003@company.com", "Carol", "Williams");

        // Create plans
        CommissionPlan standardPlan = createStandardPlan();
        CommissionPlan premiumPlan = createPremiumPlan();

        // Create deals using actual user IDs
        Deal deal1 = createDeal("Enterprise Software License", new BigDecimal("150000"), rep1.getId(), DealStatus.WON);
        Deal deal2 = createDeal("Cloud Services Contract", new BigDecimal("85000"), rep1.getId(), DealStatus.WON);
        Deal deal3 = createDeal("Consulting Services", new BigDecimal("45000"), rep2.getId(), DealStatus.WON);
        Deal deal4 = createDeal("Hardware Procurement", new BigDecimal("120000"), rep2.getId(), DealStatus.OPEN);
        Deal deal5 = createDeal("Annual Support Renewal", new BigDecimal("25000"), rep3.getId(), DealStatus.WON);
        Deal deal6 = createDeal("Training Package", new BigDecimal("15000"), rep3.getId(), DealStatus.LOST);

        // Create calculations
        CommissionCalculation calc1 = createCalculation(deal1, standardPlan, new BigDecimal("15000"));
        CommissionCalculation calc2 = createCalculation(deal2, standardPlan, new BigDecimal("8500"));

        // Create disputes
        createDispute(calc1, rep1.getId(), "Commission Rate Disagreement",
                "Rate does not match contract terms.");
        createDispute(calc2, rep1.getId(), "Missing Bonus",
                "Q4 bonus not included.");

        log.info("Clean architecture sample data initialized! Created: 3 Users, 2 Plans, 6 Deals, 2 Calculations, 2 Disputes");
    }

    private User createUser(String username, String email, String firstName, String lastName) {
        User user = new User(username, email, firstName, lastName);
        return userRepository.save(user);
    }

    private CommissionPlan createStandardPlan() {
        CommissionPlan plan = new CommissionPlan(
                "Standard Sales Plan",
                Currency.getInstance("USD"),
                LocalDate.now().minusMonths(6),
                null
        );
        plan.setStatus(PlanStatus.ACTIVE);

        // Add standard commission rule
        CommissionRule rule = new CommissionRule(
                "Base Commission",
                new BigDecimal("10.0"),
                RuleType.STANDARD
        );
        rule.setDescription("10% commission on all deals");
        rule.setPriority(1);
        plan.addRule(rule);

        // Add commission tiers
        CommissionTier tier1 = new CommissionTier("Tier 1", BigDecimal.ZERO, new BigDecimal("50000"), new BigDecimal("8"));
        CommissionTier tier2 = new CommissionTier("Tier 2", new BigDecimal("50000"), new BigDecimal("100000"), new BigDecimal("10"));
        CommissionTier tier3 = new CommissionTier("Tier 3", new BigDecimal("100000"), null, new BigDecimal("12"));
        plan.addTier(tier1);
        plan.addTier(tier2);
        plan.addTier(tier3);

        return planRepository.save(plan);
    }

    private CommissionPlan createPremiumPlan() {
        CommissionPlan plan = new CommissionPlan(
                "Premium Sales Plan",
                Currency.getInstance("USD"),
                LocalDate.now().minusMonths(3),
                null
        );
        plan.setStatus(PlanStatus.ACTIVE);

        // Add premium commission rule
        CommissionRule rule = new CommissionRule(
                "Premium Commission",
                new BigDecimal("12.0"),
                RuleType.STANDARD
        );
        rule.setDescription("12% commission on all deals");
        rule.setPriority(1);
        plan.addRule(rule);

        // Add bonus rule
        BonusRule bonus = new BonusRule(
                "Q4 Performance Bonus",
                new BigDecimal("5000"),
                false,
                BonusType.QUOTA_ACHIEVEMENT
        );
        bonus.setDescription("$5000 bonus for quarterly quota achievement");
        plan.addBonusRule(bonus);

        return planRepository.save(plan);
    }

    private Deal createDeal(String title, BigDecimal value, String salesRepId, DealStatus status) {
        Deal deal = new Deal(title, value, salesRepId);
        deal.setStatus(status);

        if (status == DealStatus.WON) {
            deal.setCloseDate(LocalDate.now().minusDays((int) (Math.random() * 30)));
        }

        return dealRepository.save(deal);
    }

    private CommissionCalculation createCalculation(Deal deal, CommissionPlan plan, BigDecimal baseCommission) {
        CommissionCalculation calculation = new CommissionCalculation(
                deal.getId(),
                deal.getSalesRepId(),
                baseCommission
        );
        calculation.setPlanId(plan.getId());
        calculation.setStatus(CommissionStatus.CALCULATED);
        calculation.recalculate();

        return calculationRepository.save(calculation);
    }

    private Dispute createDispute(CommissionCalculation calculation, String salesRepId,
                                  String title, String description) {
        Dispute dispute = new Dispute(
                calculation.getId(),
                salesRepId,
                title,
                description
        );
        dispute.setStatus(DisputeStatus.INITIATED);

        return disputeRepository.save(dispute);
    }
}
