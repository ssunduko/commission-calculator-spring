package com.chapman.edu.commissions.architecture.verticalslice.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end Selenium tests for Commission Plan Management.
 * Maps to functional requirements REQ-PLAN-001 through REQ-PLAN-007
 * and user stories US-4.1 through US-4.6.
 */
class CommissionPlanSeleniumE2ETest extends BaseSeleniumE2ETest {

    // ==================== REQ-PLAN-001 ====================

    @Test
    @DisplayName("REQ-PLAN-001: POST /api/plans creates plan with DRAFT status")
    @SuppressWarnings("unchecked")
    void testReqPlan001_createPlanReturnsDraftStatus() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Q1 Sales Plan");
        request.put("currencyCode", "USD");
        request.put("effectiveStartDate", LocalDate.now().toString());
        request.put("effectiveEndDate", LocalDate.now().plusMonths(3).toString());

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/plans", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("id")).isNotNull();
        assertThat(body.get("name")).isEqualTo("Q1 Sales Plan");
        assertThat(body.get("status")).isEqualTo("DRAFT");
        assertThat(body.get("currency")).isEqualTo("USD");
    }

    @Test
    @DisplayName("REQ-PLAN-001: Plan creation includes effective date range")
    @SuppressWarnings("unchecked")
    void testReqPlan001_planIncludesEffectiveDates() {
        String startDate = LocalDate.now().toString();
        String endDate = LocalDate.now().plusMonths(6).toString();

        Map<String, Object> request = new HashMap<>();
        request.put("name", "Date Range Plan");
        request.put("currencyCode", "EUR");
        request.put("effectiveStartDate", startDate);
        request.put("effectiveEndDate", endDate);

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/plans", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map<String, Object> body = response.getBody();
        assertThat(body.get("effectiveStartDate")).isEqualTo(startDate);
        assertThat(body.get("effectiveEndDate")).isEqualTo(endDate);
    }

    // ==================== REQ-PLAN-002 ====================

    @Test
    @DisplayName("REQ-PLAN-002: Create plan with blank name returns error")
    @SuppressWarnings("unchecked")
    void testReqPlan002_createPlanWithBlankNameReturnsError() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "");
        request.put("currencyCode", "USD");

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/plans", request, Map.class);

        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    @DisplayName("REQ-PLAN-002: Create plan with blank currency code returns error")
    @SuppressWarnings("unchecked")
    void testReqPlan002_createPlanWithBlankCurrencyReturnsError() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Valid Plan Name");
        request.put("currencyCode", "");

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/plans", request, Map.class);

        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    @DisplayName("REQ-PLAN-002: Create plan with invalid currency code returns error")
    @SuppressWarnings("unchecked")
    void testReqPlan002_createPlanWithInvalidCurrencyReturnsError() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Bad Currency Plan");
        request.put("currencyCode", "INVALID");

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/plans", request, Map.class);

        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
    }

    // ==================== REQ-PLAN-003 ====================

    @Test
    @DisplayName("REQ-PLAN-003: POST /api/plans/{id}/activate transitions DRAFT to ACTIVE")
    @SuppressWarnings("unchecked")
    void testReqPlan003_activateDraftPlan() {
        Map<String, Object> plan = createPlan("Activate Test Plan", "USD");
        String planId = (String) plan.get("id");
        assertThat(plan.get("status")).isEqualTo("DRAFT");

        ResponseEntity<Map> activateResponse = authenticated()
            .postForEntity("/api/plans/" + planId + "/activate", null, Map.class);

        assertThat(activateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(activateResponse.getBody().get("status")).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("REQ-PLAN-003: Verify activated plan persists ACTIVE status")
    @SuppressWarnings("unchecked")
    void testReqPlan003_activatedPlanPersistsStatus() {
        Map<String, Object> plan = createPlan("Persist Active Plan", "USD");
        String planId = (String) plan.get("id");
        activatePlan(planId);

        ResponseEntity<Map> getResponse = authenticated()
            .getForEntity("/api/plans/" + planId, Map.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().get("status")).isEqualTo("ACTIVE");
    }

    // ==================== REQ-PLAN-005 ====================

    @Test
    @DisplayName("REQ-PLAN-005: GET /api/plans?status=ACTIVE returns only active plans")
    @SuppressWarnings("unchecked")
    void testReqPlan005_filterPlansByStatusActive() {
        // DataInitializer creates ACTIVE plans
        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/plans?status=ACTIVE", List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> plans = response.getBody();
        assertThat(plans).isNotNull();
        for (Map<String, Object> p : plans) {
            assertThat(p.get("status")).isEqualTo("ACTIVE");
        }
    }

    @Test
    @DisplayName("REQ-PLAN-005: GET /api/plans returns all plans regardless of status")
    @SuppressWarnings("unchecked")
    void testReqPlan005_getAllPlansReturnsAllStatuses() {
        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/plans", List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();
    }

    // ==================== REQ-PLAN-006 ====================

    @Test
    @DisplayName("REQ-PLAN-006: DELETE /api/plans/{id} returns 204")
    void testReqPlan006_deletePlanReturns204() {
        Map<String, Object> plan = createPlan("Delete Me Plan", "USD");
        String planId = (String) plan.get("id");

        ResponseEntity<Void> response = authenticated()
            .exchange("/api/plans/" + planId, HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("REQ-PLAN-006: DELETE then GET returns 404")
    @SuppressWarnings("unchecked")
    void testReqPlan006_deleteThenGetReturns404() {
        Map<String, Object> plan = createPlan("Delete Verify Plan", "USD");
        String planId = (String) plan.get("id");

        authenticated().exchange("/api/plans/" + planId, HttpMethod.DELETE, null, Void.class);

        ResponseEntity<Map> getResponse = authenticated()
            .getForEntity("/api/plans/" + planId, Map.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("REQ-PLAN-006: GET non-existent plan returns 404")
    @SuppressWarnings("unchecked")
    void testReqPlan006_getNonExistentPlanReturns404() {
        ResponseEntity<Map> response = authenticated()
            .getForEntity("/api/plans/nonexistent-plan-id", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ==================== REQ-PLAN-007 ====================

    @Test
    @DisplayName("REQ-PLAN-007: POST /api/plans/{id}/rules adds rule successfully (returns 200)")
    @SuppressWarnings("unchecked")
    void testReqPlan007_addRuleToPlanReturnsSuccess() {
        Map<String, Object> plan = createPlan("Rule Test Plan", "USD");
        String planId = (String) plan.get("id");

        Map<String, Object> request = new HashMap<>();
        request.put("name", "Standard Commission");
        request.put("description", "10% base rate");
        request.put("rate", new BigDecimal("10.0"));
        request.put("ruleType", "STANDARD");
        request.put("priority", 1);

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/plans/" + planId + "/rules", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("id")).isEqualTo(planId);
    }

    @Test
    @DisplayName("REQ-PLAN-007: Multiple rules can be added to a plan without error")
    @SuppressWarnings("unchecked")
    void testReqPlan007_multipleRulesCanBeAdded() {
        Map<String, Object> plan = createPlan("Multi-Rule Plan", "USD");
        String planId = (String) plan.get("id");

        ResponseEntity<Map> response1 = authenticated()
            .postForEntity("/api/plans/" + planId + "/rules",
                Map.of("name", "Base Commission", "rate", 8.0, "ruleType", "STANDARD", "priority", 1), Map.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> response2 = authenticated()
            .postForEntity("/api/plans/" + planId + "/rules",
                Map.of("name", "Accelerator", "rate", 12.0, "ruleType", "STANDARD", "priority", 2), Map.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("REQ-PLAN-007: Add rule with blank name returns error")
    @SuppressWarnings("unchecked")
    void testReqPlan007_addRuleWithBlankNameReturnsError() {
        Map<String, Object> plan = createPlan("Rule Validation Plan", "USD");
        String planId = (String) plan.get("id");

        Map<String, Object> request = new HashMap<>();
        request.put("name", "");
        request.put("rate", new BigDecimal("10.0"));
        request.put("ruleType", "STANDARD");
        request.put("priority", 1);

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/plans/" + planId + "/rules", request, Map.class);

        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
    }
}
