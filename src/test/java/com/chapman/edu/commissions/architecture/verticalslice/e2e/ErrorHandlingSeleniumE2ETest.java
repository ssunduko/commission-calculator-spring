package com.chapman.edu.commissions.architecture.verticalslice.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end Selenium tests for Error Handling.
 * Maps to functional requirements REQ-ERR-001 through REQ-ERR-007
 * and user stories US-7.1 through US-7.3.
 */
class ErrorHandlingSeleniumE2ETest extends BaseSeleniumE2ETest {

    // ==================== REQ-ERR-004: Resource not found format ====================

    @Test
    @DisplayName("REQ-ERR-004: Non-existent deal returns 404 with 'Deal not found with id' message")
    @SuppressWarnings("unchecked")
    void testReqErr004_dealNotFoundFormat() {
        ResponseEntity<Map> response = authenticated()
            .getForEntity("/api/deals/nonexistent-123", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo(404);
        assertThat((String) body.get("message")).contains("Deal");
        assertThat((String) body.get("message")).contains("nonexistent-123");
        assertThat(body.get("timestamp")).isNotNull();
    }

    @Test
    @DisplayName("REQ-ERR-004: Non-existent plan returns 404 with resource type in message")
    @SuppressWarnings("unchecked")
    void testReqErr004_planNotFoundFormat() {
        ResponseEntity<Map> response = authenticated()
            .getForEntity("/api/plans/nonexistent-plan", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Map<String, Object> body = response.getBody();
        assertThat((String) body.get("message")).containsIgnoringCase("not found");
        assertThat((String) body.get("message")).contains("nonexistent-plan");
    }

    @Test
    @DisplayName("REQ-ERR-004: Non-existent calculation returns 404 with resource type in message")
    @SuppressWarnings("unchecked")
    void testReqErr004_calculationNotFoundFormat() {
        ResponseEntity<Map> response = authenticated()
            .getForEntity("/api/calculations/nonexistent-calc", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Map<String, Object> body = response.getBody();
        assertThat((String) body.get("message")).containsIgnoringCase("not found");
    }

    @Test
    @DisplayName("REQ-ERR-004: Non-existent dispute returns 404 with resource type in message")
    @SuppressWarnings("unchecked")
    void testReqErr004_disputeNotFoundFormat() {
        ResponseEntity<Map> response = authenticated()
            .getForEntity("/api/disputes/nonexistent-dispute", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Map<String, Object> body = response.getBody();
        assertThat((String) body.get("message")).contains("Dispute");
    }

    @Test
    @DisplayName("REQ-ERR-004: 404 response includes timestamp, message, and status fields")
    @SuppressWarnings("unchecked")
    void testReqErr004_errorResponseStructure() {
        ResponseEntity<Map> response = authenticated()
            .getForEntity("/api/deals/not-found-id", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Map<String, Object> body = response.getBody();
        assertThat(body).containsKeys("timestamp", "message", "status");
    }

    // ==================== REQ-ERR-005: Auth error messages ====================

    @Test
    @DisplayName("REQ-ERR-005: Authentication failure does not reveal if username or password was wrong")
    void testReqErr005_authFailureGenericMessage() {
        // Wrong password
        ResponseEntity<String> wrongPwd = restTemplate
            .withBasicAuth("admin", "wrongpassword")
            .getForEntity("/api/deals", String.class);
        assertThat(wrongPwd.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Wrong username
        ResponseEntity<String> wrongUser = restTemplate
            .withBasicAuth("nonexistent", "admin123")
            .getForEntity("/api/deals", String.class);
        assertThat(wrongUser.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Both should return the same status code
        assertThat(wrongPwd.getStatusCode()).isEqualTo(wrongUser.getStatusCode());
    }

    // ==================== REQ-ERR-007: No stack traces ====================

    @Test
    @DisplayName("REQ-ERR-007: Error responses do not contain stack traces or class names")
    @SuppressWarnings("unchecked")
    void testReqErr007_noStackTraceInErrorResponse() {
        ResponseEntity<Map> response = authenticated()
            .getForEntity("/api/deals/trigger-error-id", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        String responseBody = response.getBody().toString();

        assertThat(responseBody).doesNotContain("at com.");
        assertThat(responseBody).doesNotContain("java.lang.");
        assertThat(responseBody).doesNotContain("Exception");
        assertThat(responseBody).doesNotContain(".java:");
    }

    // ==================== Validation error handling ====================

    @Test
    @DisplayName("REQ-ERR-002: Validation error on deal creation returns error response")
    @SuppressWarnings("unchecked")
    void testErr_validationErrorOnDealCreation() {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "");
        request.put("value", 0);
        request.put("salesRepId", "rep-001");

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/deals", request, Map.class);

        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsKey("message");
    }

    @Test
    @DisplayName("REQ-ERR-003: Business validation error returns error with descriptive message")
    @SuppressWarnings("unchecked")
    void testErr_businessValidationReturnsDescriptiveMessage() {
        // Attempt to escalate a non-existent dispute returns 404
        // Create, escalate, then re-escalate for business validation
        Map<String, Object> deal = createDeal("Err Test Deal", new java.math.BigDecimal("5000.00"), "rep-err-001");
        Map<String, Object> plan = createPlan("Err Test Plan", "USD");
        addRuleToPlan((String) plan.get("id"), "Rule", new java.math.BigDecimal("10.0"));
        Map<String, Object> calc = createCalculation((String) deal.get("id"), (String) plan.get("id"));
        Map<String, Object> dispute = createDispute(
            (String) calc.get("id"), "rep-err-001",
            "Error Test Dispute", "Testing error message quality"
        );
        String disputeId = (String) dispute.get("id");

        // Escalate once
        authenticated().postForEntity("/api/disputes/" + disputeId + "/escalate", null, Map.class);

        // Attempt to escalate again → should fail with descriptive message
        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/disputes/" + disputeId + "/escalate", null, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) response.getBody().get("message")).isNotBlank();
    }
}
