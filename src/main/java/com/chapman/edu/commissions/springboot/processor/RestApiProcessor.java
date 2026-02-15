package com.chapman.edu.commissions.springboot.processor;

import com.chapman.edu.commissions.model.*;
import com.chapman.edu.commissions.springboot.service.*;
import com.chapman.edu.commissions.springboot.dto.request.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * ============================================================================
 * PROCESSOR: RESTful API DEVELOPMENT DEMONSTRATION
 * ============================================================================
 *
 * This runnable demonstrates REST API concepts by exercising the Service layer
 * (which mirrors what the REST controllers do):
 *
 *   1. CRUD Operations — Create, Read, Update, Delete
 *   2. HTTP Method Mapping — GET (read), POST (create), PATCH (update), DELETE
 *   3. Path Variables — /api/deals/{id}
 *   4. Request Parameters — /api/deals?status=WON
 *   5. Response Entities — Status codes (200, 201, 204, 404)
 *   6. Business Validation — Service-layer rules
 *
 * NOTE: In a real scenario, these operations would be triggered by HTTP
 * requests to the REST controllers. This processor simulates those operations
 * directly through the service layer.
 */
@Component
@Order(3)
public class RestApiProcessor implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RestApiProcessor.class);

    private final DealService dealService;
    private final CommissionPlanService planService;
    private final CommissionCalculationService calculationService;

    public RestApiProcessor(DealService dealService,
                            CommissionPlanService planService,
                            CommissionCalculationService calculationService) {
        this.dealService = dealService;
        this.planService = planService;
        this.calculationService = calculationService;
    }

    @Override
    public void run(String... args) {
        logger.info("");
        logger.info("╔══════════════════════════════════════════════════════════════╗");
        logger.info("║   RESTful API DEVELOPMENT DEMONSTRATION                     ║");
        logger.info("╚══════════════════════════════════════════════════════════════╝");

        demonstrateCRUD();
        demonstrateQueryOperations();
        demonstrateBusinessLogic();

        logger.info("");
        logger.info("=== REST API Demo Complete ===");
        logger.info("");
    }

    private void demonstrateCRUD() {
        logger.info("");
        logger.info("--- CRUD Operations (mirrors @RestController endpoints) ---");

        // CREATE (POST /api/deals)
        logger.info("");
        logger.info("[POST /api/deals] — Creating a new deal...");
        CreateDealRequest createRequest = new CreateDealRequest();
        createRequest.setTitle("New Enterprise Deal");
        createRequest.setValue(new BigDecimal("55000"));
        createRequest.setSalesRepId("user-003");
        Deal newDeal = dealService.createDeal(createRequest);
        logger.info("  Created: {} (ID: {}, Status: {})", newDeal.getTitle(), newDeal.getId(), newDeal.getStatus());

        // READ (GET /api/deals/{id})
        logger.info("");
        logger.info("[GET /api/deals/{}] — Reading deal by ID...", newDeal.getId());
        Deal fetched = dealService.getDealById(newDeal.getId());
        logger.info("  Found: {} — Value: ${}", fetched.getTitle(), fetched.getValue());

        // UPDATE (PATCH /api/deals/{id}/status)
        logger.info("");
        logger.info("[PATCH /api/deals/{}/status?status=WON] — Updating deal status...", newDeal.getId());
        Deal updated = dealService.updateDealStatus(newDeal.getId(), DealStatus.WON);
        logger.info("  Updated status: {} → {}", DealStatus.OPEN, updated.getStatus());

        // DELETE (DELETE /api/deals/{id})
        logger.info("");
        logger.info("[DELETE /api/deals/{}] — Deleting deal...", newDeal.getId());
        dealService.deleteDeal(newDeal.getId());
        logger.info("  Deleted successfully. Remaining deals: {}", dealService.getDealCount());
    }

    private void demonstrateQueryOperations() {
        logger.info("");
        logger.info("--- Query Operations (mirrors @RequestParam filtering) ---");

        // GET /api/deals (all)
        logger.info("");
        logger.info("[GET /api/deals] — All deals:");
        List<Deal> allDeals = dealService.getAllDeals();
        allDeals.forEach(d -> logger.info("  {} — ${} ({})", d.getTitle(), d.getValue(), d.getStatus()));

        // GET /api/deals?status=WON
        logger.info("");
        logger.info("[GET /api/deals?status=WON] — Won deals only:");
        List<Deal> wonDeals = dealService.getDealsByStatus(DealStatus.WON);
        wonDeals.forEach(d -> logger.info("  {} — ${}", d.getTitle(), d.getValue()));

        // GET /api/deals?salesRepId=user-003
        logger.info("");
        logger.info("[GET /api/deals?salesRepId=user-003] — Deals for Ana Garcia:");
        List<Deal> repDeals = dealService.getDealsBySalesRep("user-003");
        repDeals.forEach(d -> logger.info("  {} — ${}", d.getTitle(), d.getValue()));
    }

    private void demonstrateBusinessLogic() {
        logger.info("");
        logger.info("--- Business Logic & Commission Calculation ---");

        // Calculate commission for a won deal
        logger.info("");
        logger.info("[POST /api/calculations] — Calculating commission for deal-001...");
        try {
            CalculateCommissionRequest calcRequest = new CalculateCommissionRequest();
            calcRequest.setDealId("deal-001");
            calcRequest.setPlanId("plan-001");
            calcRequest.setCalculatedBy("user-002");
            CommissionCalculation calc = calculationService.calculateCommission(calcRequest);
            logger.info("  Base Commission: ${}", calc.getBaseCommission());
            logger.info("  Gross Commission: ${}", calc.getGrossCommission());
            logger.info("  Net Commission: ${}", calc.getNetCommission());
            logger.info("  Bonuses Applied: {}", calc.getBonuses().size());
        } catch (Exception e) {
            logger.info("  Note: {}", e.getMessage());
        }

        // Try to calculate commission for an OPEN deal (should fail)
        logger.info("");
        logger.info("[POST /api/calculations] — Attempting commission on OPEN deal (should fail)...");
        try {
            CalculateCommissionRequest badRequest = new CalculateCommissionRequest();
            badRequest.setDealId("deal-004");  // This deal is OPEN
            badRequest.setPlanId("plan-001");
            badRequest.setCalculatedBy("user-002");
            calculationService.calculateCommission(badRequest);
        } catch (Exception e) {
            logger.info("  BusinessValidationException: {}", e.getMessage());
            logger.info("  (This would return HTTP 422 via the GlobalExceptionHandler)");
        }
    }
}
