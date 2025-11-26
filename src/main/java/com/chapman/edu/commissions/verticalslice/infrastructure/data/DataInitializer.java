package com.chapman.edu.commissions.verticalslice.infrastructure.data;

import com.chapman.edu.commissions.verticalslice.domain.*;
import com.chapman.edu.commissions.verticalslice.features.calculations.CommissionCalculationRepository;
import com.chapman.edu.commissions.verticalslice.features.deals.DealRepository;
import com.chapman.edu.commissions.verticalslice.features.disputes.DisputeRepository;
import com.chapman.edu.commissions.verticalslice.features.plans.CommissionPlanRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

/**
 * Initializes sample data in the database on application startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final DealRepository dealRepository;
    private final CommissionPlanRepository planRepository;
    private final CommissionCalculationRepository calculationRepository;
    private final DisputeRepository disputeRepository;

    public DataInitializer(
            DealRepository dealRepository,
            CommissionPlanRepository planRepository,
            CommissionCalculationRepository calculationRepository,
            DisputeRepository disputeRepository
    ) {
        this.dealRepository = dealRepository;
        this.planRepository = planRepository;
        this.calculationRepository = calculationRepository;
        this.disputeRepository = disputeRepository;
    }

    @Override
    public void run(String... args) {
        System.out.println("Initializing sample data...");

        // Create sample commission plans
        CommissionPlan standardPlan = createStandardPlan();
        CommissionPlan premiumPlan = createPremiumPlan();

        // Create sample deals
        Deal deal1 = createDeal("Enterprise Software License", new BigDecimal("150000"), "REP001", DealStatus.WON);
        Deal deal2 = createDeal("Cloud Services Contract", new BigDecimal("85000"), "REP001", DealStatus.WON);
        Deal deal3 = createDeal("Consulting Services", new BigDecimal("45000"), "REP002", DealStatus.WON);
        Deal deal4 = createDeal("Hardware Procurement", new BigDecimal("120000"), "REP002", DealStatus.OPEN);
        Deal deal5 = createDeal("Annual Support Renewal", new BigDecimal("25000"), "REP003", DealStatus.WON);
        Deal deal6 = createDeal("Training Package", new BigDecimal("15000"), "REP003", DealStatus.LOST);

        // Add products to deals
        addProductToDeal(deal1, "Software License - Enterprise", 10, new BigDecimal("15000"));
        addProductToDeal(deal2, "Cloud Storage", 100, new BigDecimal("500"));
        addProductToDeal(deal2, "Cloud Compute", 50, new BigDecimal("700"));
        addProductToDeal(deal3, "Consulting Hours", 300, new BigDecimal("150"));
        addProductToDeal(deal4, "Server Hardware", 5, new BigDecimal("24000"));

        // Create sample commission calculations
        CommissionCalculation calc1 = createCalculation(deal1, standardPlan, new BigDecimal("15000"));
        CommissionCalculation calc2 = createCalculation(deal2, standardPlan, new BigDecimal("8500"));
        CommissionCalculation calc3 = createCalculation(deal3, premiumPlan, new BigDecimal("5400"));
        CommissionCalculation calc5 = createCalculation(deal5, standardPlan, new BigDecimal("2500"));

        // Create sample disputes
        createDispute(calc1, "REP001", "Commission Rate Disagreement",
                "The commission rate applied does not match my contract terms for enterprise deals.");
        createDispute(calc2, "REP001", "Missing Bonus Calculation",
                "My Q4 performance bonus was not included in this calculation.");

        System.out.println("Sample data initialization completed!");
        System.out.println("Created:");
        System.out.println("  - 2 Commission Plans");
        System.out.println("  - 6 Deals");
        System.out.println("  - 4 Commission Calculations");
        System.out.println("  - 2 Disputes");
    }

    private CommissionPlan createStandardPlan() {
        CommissionPlan plan = new CommissionPlan("Standard Sales Plan", Currency.getInstance("USD"));
        plan.setStatus(PlanStatus.ACTIVE);
        plan.setEffectiveStartDate(LocalDate.now().minusMonths(6));
        plan.setCreatedBy("admin");

        // Add standard commission rule
        CommissionRule rule = new CommissionRule("Base Commission", new BigDecimal("10.0"), RuleType.STANDARD);
        rule.setDescription("10% commission on all deals");
        plan.addRule(rule);

        // Add commission tier
        CommissionTier tier1 = new CommissionTier("Tier 1", BigDecimal.ZERO, new BigDecimal("50000"), new BigDecimal("8"));
        CommissionTier tier2 = new CommissionTier("Tier 2", new BigDecimal("50000"), new BigDecimal("100000"), new BigDecimal("10"));
        CommissionTier tier3 = new CommissionTier("Tier 3", new BigDecimal("100000"), null, new BigDecimal("12"));
        plan.addTier(tier1);
        plan.addTier(tier2);
        plan.addTier(tier3);

        return planRepository.save(plan);
    }

    private CommissionPlan createPremiumPlan() {
        CommissionPlan plan = new CommissionPlan("Premium Sales Plan", Currency.getInstance("USD"));
        plan.setStatus(PlanStatus.ACTIVE);
        plan.setEffectiveStartDate(LocalDate.now().minusMonths(3));
        plan.setCreatedBy("admin");

        // Add premium commission rule
        CommissionRule rule = new CommissionRule("Premium Commission", new BigDecimal("12.0"), RuleType.STANDARD);
        rule.setDescription("12% commission on all deals");
        plan.addRule(rule);

        // Add bonus rule
        BonusRule bonus = new BonusRule("Q4 Performance Bonus", new BigDecimal("5000"), false, BonusType.QUOTA_ACHIEVEMENT);
        bonus.setDescription("$5000 bonus for quarterly quota achievement");
        bonus.setStartDate(LocalDate.now().minusMonths(3));
        bonus.setEndDate(LocalDate.now().plusMonths(3));
        plan.addBonus(bonus);

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

    private void addProductToDeal(Deal deal, String productName, int quantity, BigDecimal price) {
        DealProduct product = new DealProduct();
        product.setProductId("PROD-" + System.currentTimeMillis());
        product.setProductName(productName);
        product.setQuantity(quantity);
        product.setPrice(price);
        product.setDiscount(BigDecimal.ZERO);
        product.setDealId(deal.getId());

        deal.addProduct(product);
        dealRepository.save(deal);
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
