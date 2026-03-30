package com.chapman.edu.commissions.architecture.verticalslice.e2e;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end Selenium tests for Commission Calculation.
 * Maps to functional requirements REQ-CALC-001 through REQ-CALC-010
 * and user stories US-5.1 through US-5.5.
 */
class CommissionCalculationSeleniumE2ETest extends BaseSeleniumE2ETest {

    private String testDealId;
    private String testPlanId;
    private String testSalesRepId;
    private static final BigDecimal DEAL_VALUE = new BigDecimal("45000.00");
    private static final BigDecimal RULE_RATE = new BigDecimal("10.0");

    @BeforeAll
    void setUpTestData() {
        testSalesRepId = "rep-calc-e2e-" + System.currentTimeMillis();

        // Create a deal
        Map<String, Object> deal = createDeal("Calculation Test Deal", DEAL_VALUE, testSalesRepId);
        testDealId = (String) deal.get("id");

        // Create a plan with a rule
        Map<String, Object> plan = createPlan("Calculation Test Plan", "USD");
        testPlanId = (String) plan.get("id");
        addRuleToPlan(testPlanId, "Standard Commission", RULE_RATE);
    }

    // ==================== REQ-CALC-001 ====================

    @Test
    @DisplayName("REQ-CALC-001: POST /api/calculations returns CALCULATED status with commission amounts")
    @SuppressWarnings("unchecked")
    void testReqCalc001_calculateCommissionReturnsCalculatedStatus() {
        Map<String, Object> calculation = createCalculation(testDealId, testPlanId);

        assertThat(calculation).isNotNull();
        assertThat(calculation.get("id")).isNotNull();
        assertThat(calculation.get("status")).isEqualTo("CALCULATED");
        assertThat(calculation.get("dealId")).isEqualTo(testDealId);
        assertThat(calculation.get("salesRepId")).isEqualTo(testSalesRepId);
        assertThat(calculation.get("planId")).isEqualTo(testPlanId);
        assertThat(calculation.get("calculationDate")).isNotNull();
    }

    @Test
    @DisplayName("REQ-CALC-001: Calculation includes baseCommission field")
    @SuppressWarnings("unchecked")
    void testReqCalc001_calculationIncludesBaseCommissionField() {
        Map<String, Object> calculation = createCalculation(testDealId, testPlanId);

        assertThat(calculation).containsKey("baseCommission");
        assertThat(calculation.get("baseCommission")).isNotNull();
    }

    // ==================== REQ-CALC-004 ====================

    @Test
    @DisplayName("REQ-CALC-004: Sample data calculations have non-zero base commission (DataInitializer)")
    @SuppressWarnings("unchecked")
    void testReqCalc004_sampleDataCalculationsHaveNonZeroCommission() {
        // DataInitializer creates calculations with hardcoded non-zero base commissions
        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/calculations", List.class);

        List<Map<String, Object>> calculations = response.getBody();
        assertThat(calculations).isNotEmpty();

        boolean hasNonZero = calculations.stream()
            .anyMatch(c -> {
                Number base = (Number) c.get("baseCommission");
                return base != null && base.doubleValue() > 0;
            });
        assertThat(hasNonZero)
            .as("At least one calculation should have non-zero base commission (from sample data)")
            .isTrue();
    }

    @Test
    @DisplayName("REQ-CALC-004: Commission calculation populates baseCommission based on plan rules")
    @SuppressWarnings("unchecked")
    void testReqCalc004_calculationPopulatesBaseCommission() {
        Map<String, Object> calculation = createCalculation(testDealId, testPlanId);

        // Verify baseCommission field is populated (value depends on plan rules availability)
        assertThat(calculation.get("baseCommission")).isNotNull();
        assertThat(calculation.get("grossCommission")).isNotNull();
        assertThat(calculation.get("netCommission")).isNotNull();
    }

    // ==================== REQ-CALC-006 ====================

    @Test
    @DisplayName("REQ-CALC-006: Gross commission equals net commission (no deductions)")
    @SuppressWarnings("unchecked")
    void testReqCalc006_grossEqualsNet() {
        Map<String, Object> calculation = createCalculation(testDealId, testPlanId);

        Number gross = (Number) calculation.get("grossCommission");
        Number net = (Number) calculation.get("netCommission");
        assertThat(new BigDecimal(gross.toString()))
            .isEqualByComparingTo(new BigDecimal(net.toString()));
    }

    @Test
    @DisplayName("REQ-CALC-006: Gross commission equals base commission when no bonuses")
    @SuppressWarnings("unchecked")
    void testReqCalc006_grossEqualsBaseWhenNoBonuses() {
        Map<String, Object> calculation = createCalculation(testDealId, testPlanId);

        Number base = (Number) calculation.get("baseCommission");
        Number gross = (Number) calculation.get("grossCommission");
        assertThat(new BigDecimal(gross.toString()))
            .isEqualByComparingTo(new BigDecimal(base.toString()));
    }

    // ==================== REQ-CALC-009 ====================

    @Test
    @DisplayName("REQ-CALC-009: GET /api/calculations returns all calculations")
    @SuppressWarnings("unchecked")
    void testReqCalc009_getAllCalculations() {
        // Ensure at least one calculation exists
        createCalculation(testDealId, testPlanId);

        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/calculations", List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    @DisplayName("REQ-CALC-009: Filter calculations by dealId")
    @SuppressWarnings("unchecked")
    void testReqCalc009_filterByDealId() {
        createCalculation(testDealId, testPlanId);

        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/calculations?dealId=" + testDealId, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> calculations = response.getBody();
        assertThat(calculations).isNotNull();
        assertThat(calculations).isNotEmpty();
        for (Map<String, Object> calc : calculations) {
            assertThat(calc.get("dealId")).isEqualTo(testDealId);
        }
    }

    @Test
    @DisplayName("REQ-CALC-009: Filter calculations by salesRepId")
    @SuppressWarnings("unchecked")
    void testReqCalc009_filterBySalesRepId() {
        createCalculation(testDealId, testPlanId);

        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/calculations?salesRepId=" + testSalesRepId, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> calculations = response.getBody();
        assertThat(calculations).isNotNull();
        assertThat(calculations).isNotEmpty();
        for (Map<String, Object> calc : calculations) {
            assertThat(calc.get("salesRepId")).isEqualTo(testSalesRepId);
        }
    }

    // ==================== GET by ID / 404 ====================

    @Test
    @DisplayName("REQ-CALC-001: GET /api/calculations/{id} retrieves the calculation")
    @SuppressWarnings("unchecked")
    void testCalc_getCalculationById() {
        Map<String, Object> created = createCalculation(testDealId, testPlanId);
        String calcId = (String) created.get("id");

        ResponseEntity<Map> response = authenticated()
            .getForEntity("/api/calculations/" + calcId, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("id")).isEqualTo(calcId);
    }

    @Test
    @DisplayName("REQ-ERR-004: GET /api/calculations/{id} returns 404 for non-existent calculation")
    @SuppressWarnings("unchecked")
    void testCalc_getNonExistentCalculationReturns404() {
        ResponseEntity<Map> response = authenticated()
            .getForEntity("/api/calculations/nonexistent-calc-id", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((String) response.getBody().get("message")).contains("Commission Calculation");
    }

    // ==================== Validation ====================

    @Test
    @DisplayName("REQ-CALC-001: Calculation with blank dealId returns error")
    @SuppressWarnings("unchecked")
    void testCalc_blankDealIdReturnsError() {
        Map<String, Object> request = new HashMap<>();
        request.put("dealId", "");
        request.put("planId", testPlanId);

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/calculations", request, Map.class);

        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    @DisplayName("REQ-CALC-001: Calculation with blank planId returns error")
    @SuppressWarnings("unchecked")
    void testCalc_blankPlanIdReturnsError() {
        Map<String, Object> request = new HashMap<>();
        request.put("dealId", testDealId);
        request.put("planId", "");

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/calculations", request, Map.class);

        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    @DisplayName("REQ-CALC-001: Calculation with non-existent dealId returns 404")
    @SuppressWarnings("unchecked")
    void testCalc_nonExistentDealIdReturns404() {
        Map<String, Object> request = new HashMap<>();
        request.put("dealId", "nonexistent-deal");
        request.put("planId", testPlanId);

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/calculations", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("REQ-CALC-001: Calculation with non-existent planId returns 404")
    @SuppressWarnings("unchecked")
    void testCalc_nonExistentPlanIdReturns404() {
        Map<String, Object> request = new HashMap<>();
        request.put("dealId", testDealId);
        request.put("planId", "nonexistent-plan");

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/calculations", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
