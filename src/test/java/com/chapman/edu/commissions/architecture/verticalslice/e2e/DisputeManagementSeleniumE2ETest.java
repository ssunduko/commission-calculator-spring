package com.chapman.edu.commissions.architecture.verticalslice.e2e;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end Selenium tests for Dispute Management.
 * Maps to functional requirements REQ-DISP-001 through REQ-DISP-008
 * and user stories US-6.1 through US-6.4.
 */
class DisputeManagementSeleniumE2ETest extends BaseSeleniumE2ETest {

    private String testCalculationId;
    private String testSalesRepId;

    @BeforeAll
    void setUpTestData() {
        testSalesRepId = "rep-dispute-e2e-" + System.currentTimeMillis();

        // Create a deal and plan for calculations
        Map<String, Object> deal = createDeal("Dispute Test Deal", new BigDecimal("30000.00"), testSalesRepId);
        String dealId = (String) deal.get("id");

        Map<String, Object> plan = createPlan("Dispute Test Plan", "USD");
        String planId = (String) plan.get("id");
        addRuleToPlan(planId, "Base Rate", new BigDecimal("10.0"));

        // Create a calculation for disputes
        Map<String, Object> calculation = createCalculation(dealId, planId);
        testCalculationId = (String) calculation.get("id");
    }

    // ==================== REQ-DISP-001 ====================

    @Test
    @DisplayName("REQ-DISP-001: POST /api/disputes creates dispute with INITIATED status")
    @SuppressWarnings("unchecked")
    void testReqDisp001_createDisputeReturnsInitiatedStatus() {
        Map<String, Object> dispute = createDispute(
            testCalculationId, testSalesRepId,
            "Commission Discrepancy", "The calculated amount does not match expected"
        );

        assertThat(dispute).isNotNull();
        assertThat(dispute.get("id")).isNotNull();
        assertThat(dispute.get("status")).isEqualTo("INITIATED");
        assertThat(dispute.get("calculationId")).isEqualTo(testCalculationId);
        assertThat(dispute.get("salesRepId")).isEqualTo(testSalesRepId);
        assertThat(dispute.get("title")).isEqualTo("Commission Discrepancy");
        assertThat(dispute.get("description")).isEqualTo("The calculated amount does not match expected");
    }

    @Test
    @DisplayName("REQ-DISP-001: New dispute has escalated=false")
    @SuppressWarnings("unchecked")
    void testReqDisp001_newDisputeNotEscalated() {
        Map<String, Object> dispute = createDispute(
            testCalculationId, testSalesRepId,
            "Escalation Check", "Verifying initial escalation state"
        );

        assertThat(dispute.get("isEscalated")).isEqualTo(false);
    }

    @Test
    @DisplayName("REQ-DISP-001: New dispute has createdDate set")
    @SuppressWarnings("unchecked")
    void testReqDisp001_newDisputeHasCreatedDate() {
        Map<String, Object> dispute = createDispute(
            testCalculationId, testSalesRepId,
            "Date Check Dispute", "Verifying created date is set"
        );

        assertThat(dispute.get("createdDate")).isNotNull();
    }

    // ==================== REQ-DISP-002 ====================

    @Test
    @DisplayName("REQ-DISP-002: Create dispute with blank calculationId returns error")
    @SuppressWarnings("unchecked")
    void testReqDisp002_blankCalculationIdReturnsError() {
        Map<String, Object> request = new HashMap<>();
        request.put("calculationId", "");
        request.put("salesRepId", testSalesRepId);
        request.put("title", "Valid Title");
        request.put("description", "Valid description text");

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/disputes", request, Map.class);

        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    @DisplayName("REQ-DISP-002: Create dispute with blank title returns error")
    @SuppressWarnings("unchecked")
    void testReqDisp002_blankTitleReturnsError() {
        Map<String, Object> request = new HashMap<>();
        request.put("calculationId", testCalculationId);
        request.put("salesRepId", testSalesRepId);
        request.put("title", "");
        request.put("description", "Valid description text");

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/disputes", request, Map.class);

        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    @DisplayName("REQ-DISP-002: Create dispute with blank description returns error")
    @SuppressWarnings("unchecked")
    void testReqDisp002_blankDescriptionReturnsError() {
        Map<String, Object> request = new HashMap<>();
        request.put("calculationId", testCalculationId);
        request.put("salesRepId", testSalesRepId);
        request.put("title", "Valid Title");
        request.put("description", "");

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/disputes", request, Map.class);

        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    @DisplayName("REQ-DISP-002: Create dispute with blank salesRepId returns error")
    @SuppressWarnings("unchecked")
    void testReqDisp002_blankSalesRepIdReturnsError() {
        Map<String, Object> request = new HashMap<>();
        request.put("calculationId", testCalculationId);
        request.put("salesRepId", "");
        request.put("title", "Valid Title");
        request.put("description", "Valid description text");

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/disputes", request, Map.class);

        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
    }

    // ==================== REQ-DISP-003 ====================

    @Test
    @DisplayName("REQ-DISP-003: POST /api/disputes/{id}/resolve with approved=true sets APPROVED status")
    @SuppressWarnings("unchecked")
    void testReqDisp003_resolveDisputeApproved() {
        Map<String, Object> dispute = createDispute(
            testCalculationId, testSalesRepId,
            "Resolve Approve Test", "Testing approval resolution flow"
        );
        String disputeId = (String) dispute.get("id");

        Map<String, Object> resolveRequest = new HashMap<>();
        resolveRequest.put("resolution", "Commission recalculated and adjusted");
        resolveRequest.put("resolvedBy", "manager-001");
        resolveRequest.put("approved", true);

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/disputes/" + disputeId + "/resolve", resolveRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body.get("status")).isEqualTo("APPROVED");
        assertThat(body.get("resolution")).isEqualTo("Commission recalculated and adjusted");
    }

    @Test
    @DisplayName("REQ-DISP-003: POST /api/disputes/{id}/resolve with approved=false sets REJECTED status")
    @SuppressWarnings("unchecked")
    void testReqDisp003_resolveDisputeRejected() {
        Map<String, Object> dispute = createDispute(
            testCalculationId, testSalesRepId,
            "Resolve Reject Test", "Testing rejection resolution flow"
        );
        String disputeId = (String) dispute.get("id");

        Map<String, Object> resolveRequest = new HashMap<>();
        resolveRequest.put("resolution", "Commission calculation verified as correct");
        resolveRequest.put("resolvedBy", "manager-002");
        resolveRequest.put("approved", false);

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/disputes/" + disputeId + "/resolve", resolveRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("status")).isEqualTo("REJECTED");
    }

    // ==================== REQ-DISP-004 ====================

    @Test
    @DisplayName("REQ-DISP-004: Cannot resolve an already APPROVED dispute (returns 400)")
    @SuppressWarnings("unchecked")
    void testReqDisp004_cannotResolveApprovedDispute() {
        Map<String, Object> dispute = createDispute(
            testCalculationId, testSalesRepId,
            "Double Resolve Test", "Testing re-resolution prevention"
        );
        String disputeId = (String) dispute.get("id");

        // First resolve
        Map<String, Object> resolveRequest = new HashMap<>();
        resolveRequest.put("resolution", "Approved");
        resolveRequest.put("resolvedBy", "manager-001");
        resolveRequest.put("approved", true);
        authenticated().postForEntity("/api/disputes/" + disputeId + "/resolve", resolveRequest, Map.class);

        // Attempt second resolve
        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/disputes/" + disputeId + "/resolve", resolveRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) response.getBody().get("message")).contains("already resolved");
    }

    @Test
    @DisplayName("REQ-DISP-004: Cannot resolve an already REJECTED dispute (returns 400)")
    @SuppressWarnings("unchecked")
    void testReqDisp004_cannotResolveRejectedDispute() {
        Map<String, Object> dispute = createDispute(
            testCalculationId, testSalesRepId,
            "Reject Then Resolve", "Testing rejected dispute re-resolution"
        );
        String disputeId = (String) dispute.get("id");

        // First reject
        Map<String, Object> resolveRequest = new HashMap<>();
        resolveRequest.put("resolution", "Rejected");
        resolveRequest.put("resolvedBy", "manager-001");
        resolveRequest.put("approved", false);
        authenticated().postForEntity("/api/disputes/" + disputeId + "/resolve", resolveRequest, Map.class);

        // Attempt to resolve again
        resolveRequest.put("approved", true);
        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/disputes/" + disputeId + "/resolve", resolveRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ==================== REQ-DISP-005 ====================

    @Test
    @DisplayName("REQ-DISP-005: POST /api/disputes/{id}/escalate sets escalated=true and ESCALATED status")
    @SuppressWarnings("unchecked")
    void testReqDisp005_escalateDispute() {
        Map<String, Object> dispute = createDispute(
            testCalculationId, testSalesRepId,
            "Escalation Test", "Testing escalation flow"
        );
        String disputeId = (String) dispute.get("id");

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/disputes/" + disputeId + "/escalate", null, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body.get("isEscalated")).isEqualTo(true);
        assertThat(body.get("status")).isEqualTo("ESCALATED");
    }

    @Test
    @DisplayName("REQ-DISP-005: Cannot re-escalate an already escalated dispute (returns 400)")
    @SuppressWarnings("unchecked")
    void testReqDisp005_cannotReEscalateDispute() {
        Map<String, Object> dispute = createDispute(
            testCalculationId, testSalesRepId,
            "Re-Escalation Test", "Testing double escalation prevention"
        );
        String disputeId = (String) dispute.get("id");

        // First escalation
        authenticated().postForEntity("/api/disputes/" + disputeId + "/escalate", null, Map.class);

        // Attempt second escalation
        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/disputes/" + disputeId + "/escalate", null, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) response.getBody().get("message")).contains("already escalated");
    }

    // ==================== REQ-DISP-007 ====================

    @Test
    @DisplayName("REQ-DISP-007: GET /api/disputes returns all disputes")
    @SuppressWarnings("unchecked")
    void testReqDisp007_getAllDisputes() {
        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/disputes", List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    @DisplayName("REQ-DISP-007: Filter disputes by status=INITIATED returns only INITIATED disputes")
    @SuppressWarnings("unchecked")
    void testReqDisp007_filterByStatusInitiated() {
        // Create a fresh INITIATED dispute
        createDispute(testCalculationId, testSalesRepId, "Filter Status Test", "For filtering by status");

        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/disputes?status=INITIATED", List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> disputes = response.getBody();
        assertThat(disputes).isNotNull();
        for (Map<String, Object> d : disputes) {
            assertThat(d.get("status")).isEqualTo("INITIATED");
        }
    }

    @Test
    @DisplayName("REQ-DISP-007: Filter disputes by salesRepId returns only that rep's disputes")
    @SuppressWarnings("unchecked")
    void testReqDisp007_filterBySalesRepId() {
        String uniqueRepId = "rep-filter-disp-" + System.currentTimeMillis();
        createDispute(testCalculationId, uniqueRepId, "Rep Filter Test", "For filtering by sales rep");

        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/disputes?salesRepId=" + uniqueRepId, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> disputes = response.getBody();
        assertThat(disputes).isNotNull();
        assertThat(disputes).hasSize(1);
        assertThat(disputes.get(0).get("salesRepId")).isEqualTo(uniqueRepId);
    }

    // ==================== REQ-DISP-008 ====================

    @Test
    @DisplayName("REQ-DISP-008: Dispute response includes commentsCount")
    @SuppressWarnings("unchecked")
    void testReqDisp008_disputeResponseIncludesCommentsCount() {
        Map<String, Object> dispute = createDispute(
            testCalculationId, testSalesRepId,
            "Comments Count Test", "Verify comments count in response"
        );

        // DisputeResponse record may use "commentsCount" key
        boolean hasCommentsField = dispute.containsKey("commentsCount");
        assertThat(hasCommentsField).isTrue();
    }

    @Test
    @DisplayName("REQ-DISP-008: Dispute response includes isEscalated field")
    @SuppressWarnings("unchecked")
    void testReqDisp008_disputeResponseIncludesEscalatedField() {
        Map<String, Object> dispute = createDispute(
            testCalculationId, testSalesRepId,
            "Escalated Field Test", "Verify isEscalated field in response"
        );

        assertThat(dispute).containsKey("isEscalated");
    }

    // ==================== GET by ID / 404 / DELETE ====================

    @Test
    @DisplayName("REQ-DISP-001: GET /api/disputes/{id} retrieves the dispute")
    @SuppressWarnings("unchecked")
    void testDisp_getDisputeById() {
        Map<String, Object> created = createDispute(
            testCalculationId, testSalesRepId,
            "Get By ID Test", "Testing retrieval by ID"
        );
        String disputeId = (String) created.get("id");

        ResponseEntity<Map> response = authenticated()
            .getForEntity("/api/disputes/" + disputeId, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("id")).isEqualTo(disputeId);
        assertThat(response.getBody().get("title")).isEqualTo("Get By ID Test");
    }

    @Test
    @DisplayName("REQ-ERR-004: GET /api/disputes/{id} returns 404 for non-existent dispute")
    @SuppressWarnings("unchecked")
    void testDisp_getNonExistentDisputeReturns404() {
        ResponseEntity<Map> response = authenticated()
            .getForEntity("/api/disputes/nonexistent-dispute-id", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((String) response.getBody().get("message")).contains("Dispute");
    }

    @Test
    @DisplayName("DELETE /api/disputes/{id} returns 204")
    void testDisp_deleteDisputeReturns204() {
        Map<String, Object> dispute = createDispute(
            testCalculationId, testSalesRepId,
            "Delete Test Dispute", "This dispute will be deleted"
        );
        String disputeId = (String) dispute.get("id");

        ResponseEntity<Void> response = authenticated()
            .exchange("/api/disputes/" + disputeId, HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("DELETE then GET returns 404")
    @SuppressWarnings("unchecked")
    void testDisp_deleteThenGetReturns404() {
        Map<String, Object> dispute = createDispute(
            testCalculationId, testSalesRepId,
            "Delete Verify Dispute", "Verify delete then 404"
        );
        String disputeId = (String) dispute.get("id");

        authenticated().exchange("/api/disputes/" + disputeId, HttpMethod.DELETE, null, Void.class);

        ResponseEntity<Map> getResponse = authenticated()
            .getForEntity("/api/disputes/" + disputeId, Map.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("DELETE non-existent dispute returns 404")
    void testDisp_deleteNonExistentDisputeReturns404() {
        ResponseEntity<Map> response = authenticated()
            .exchange("/api/disputes/nonexistent-id", HttpMethod.DELETE, null, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
