package com.chapman.edu.commissions.architecture.verticalslice.e2e;

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
 * End-to-end Selenium tests for Deal Management.
 * Maps to functional requirements REQ-DEAL-001 through REQ-DEAL-007
 * and user stories US-3.1 through US-3.4.
 */
class DealManagementSeleniumE2ETest extends BaseSeleniumE2ETest {

    // ==================== REQ-DEAL-001 ====================

    @Test
    @DisplayName("REQ-DEAL-001: POST /api/deals creates deal with OPEN status and generated ID")
    @SuppressWarnings("unchecked")
    void testReqDeal001_createDealReturnsOpenStatusAndGeneratedId() {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "Enterprise License Deal");
        request.put("value", new BigDecimal("50000.00"));
        request.put("salesRepId", "rep-e2e-001");

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/deals", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("id")).isNotNull();
        assertThat(body.get("title")).isEqualTo("Enterprise License Deal");
        assertThat(body.get("status")).isEqualTo("OPEN");
        assertThat(body.get("salesRepId")).isEqualTo("rep-e2e-001");
        assertThat(body.get("createdDate")).isNotNull();
    }

    @Test
    @DisplayName("REQ-DEAL-001: New deal value is persisted correctly")
    @SuppressWarnings("unchecked")
    void testReqDeal001_dealValuePersistedCorrectly() {
        Map<String, Object> deal = createDeal("Value Check Deal", new BigDecimal("12345.67"), "rep-val-001");

        String dealId = (String) deal.get("id");
        ResponseEntity<Map> getResponse = authenticated()
            .getForEntity("/api/deals/" + dealId, Map.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Number value = (Number) getResponse.getBody().get("value");
        assertThat(new BigDecimal(value.toString())).isEqualByComparingTo(new BigDecimal("12345.67"));
    }

    // ==================== REQ-DEAL-002 ====================

    @Test
    @DisplayName("REQ-DEAL-002: Create deal with blank title returns error")
    @SuppressWarnings("unchecked")
    void testReqDeal002_createDealWithBlankTitleReturnsError() {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "");
        request.put("value", new BigDecimal("1000.00"));
        request.put("salesRepId", "rep-001");

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/deals", request, Map.class);

        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    @DisplayName("REQ-DEAL-002: Create deal with null title returns error")
    @SuppressWarnings("unchecked")
    void testReqDeal002_createDealWithNullTitleReturnsError() {
        Map<String, Object> request = new HashMap<>();
        request.put("value", new BigDecimal("1000.00"));
        request.put("salesRepId", "rep-001");

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/deals", request, Map.class);

        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    @DisplayName("REQ-DEAL-002: Create deal with zero value returns error")
    @SuppressWarnings("unchecked")
    void testReqDeal002_createDealWithZeroValueReturnsError() {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "Zero Value Deal");
        request.put("value", BigDecimal.ZERO);
        request.put("salesRepId", "rep-001");

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/deals", request, Map.class);

        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    @DisplayName("REQ-DEAL-002: Create deal with negative value returns error")
    @SuppressWarnings("unchecked")
    void testReqDeal002_createDealWithNegativeValueReturnsError() {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "Negative Value Deal");
        request.put("value", new BigDecimal("-500.00"));
        request.put("salesRepId", "rep-001");

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/deals", request, Map.class);

        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    @DisplayName("REQ-DEAL-002: Create deal with blank salesRepId returns error")
    @SuppressWarnings("unchecked")
    void testReqDeal002_createDealWithBlankSalesRepIdReturnsError() {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "Missing Rep Deal");
        request.put("value", new BigDecimal("1000.00"));
        request.put("salesRepId", "");

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/deals", request, Map.class);

        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
    }

    // ==================== REQ-DEAL-005 ====================

    @Test
    @DisplayName("REQ-DEAL-005: GET /api/deals returns all deals")
    @SuppressWarnings("unchecked")
    void testReqDeal005_getAllDealsReturnsNonEmptyList() {
        // DataInitializer creates sample deals, so list should be non-empty
        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/deals", List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    @DisplayName("REQ-DEAL-005: Filter deals by status=WON returns only WON deals")
    @SuppressWarnings("unchecked")
    void testReqDeal005_filterDealsByStatusWon() {
        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/deals?status=WON", List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> deals = response.getBody();
        assertThat(deals).isNotNull();
        for (Map<String, Object> deal : deals) {
            assertThat(deal.get("status")).isEqualTo("WON");
        }
    }

    @Test
    @DisplayName("REQ-DEAL-005: Filter deals by salesRepId returns only that rep's deals")
    @SuppressWarnings("unchecked")
    void testReqDeal005_filterDealsBySalesRepId() {
        // Create a deal with a unique sales rep ID
        String uniqueRepId = "rep-filter-test-" + System.currentTimeMillis();
        createDeal("Filter Test Deal", new BigDecimal("5000.00"), uniqueRepId);

        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/deals?salesRepId=" + uniqueRepId, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> deals = response.getBody();
        assertThat(deals).isNotNull();
        assertThat(deals).hasSize(1);
        assertThat(deals.get(0).get("salesRepId")).isEqualTo(uniqueRepId);
    }

    // ==================== REQ-DEAL-006 ====================

    @Test
    @DisplayName("REQ-DEAL-006: GET /api/deals/{id} returns the deal with correct data")
    @SuppressWarnings("unchecked")
    void testReqDeal006_getDealByIdReturnsDeal() {
        Map<String, Object> created = createDeal("Get By ID Deal", new BigDecimal("25000.00"), "rep-get-001");
        String dealId = (String) created.get("id");

        ResponseEntity<Map> response = authenticated()
            .getForEntity("/api/deals/" + dealId, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body.get("id")).isEqualTo(dealId);
        assertThat(body.get("title")).isEqualTo("Get By ID Deal");
        assertThat(body.get("salesRepId")).isEqualTo("rep-get-001");
    }

    @Test
    @DisplayName("REQ-DEAL-006: GET /api/deals/{id} returns 404 for non-existent deal")
    @SuppressWarnings("unchecked")
    void testReqDeal006_getNonExistentDealReturns404() {
        ResponseEntity<Map> response = authenticated()
            .getForEntity("/api/deals/nonexistent-deal-id", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((String) body.get("message")).contains("Deal");
        assertThat((String) body.get("message")).contains("nonexistent-deal-id");
    }

    // ==================== REQ-DEAL-007 ====================

    @Test
    @DisplayName("REQ-DEAL-007: DELETE /api/deals/{id} returns 204 on success")
    void testReqDeal007_deleteDealReturns204() {
        Map<String, Object> created = createDeal("Delete Me Deal", new BigDecimal("1000.00"), "rep-del-001");
        String dealId = (String) created.get("id");

        ResponseEntity<Void> response = authenticated()
            .exchange("/api/deals/" + dealId, HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("REQ-DEAL-007: DELETE then GET returns 404")
    @SuppressWarnings("unchecked")
    void testReqDeal007_deleteThenGetReturns404() {
        Map<String, Object> created = createDeal("Delete Verify Deal", new BigDecimal("1000.00"), "rep-del-002");
        String dealId = (String) created.get("id");

        authenticated().exchange("/api/deals/" + dealId, HttpMethod.DELETE, null, Void.class);

        ResponseEntity<Map> getResponse = authenticated()
            .getForEntity("/api/deals/" + dealId, Map.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("REQ-DEAL-007: DELETE non-existent deal returns 404")
    void testReqDeal007_deleteNonExistentDealReturns404() {
        ResponseEntity<Map> response = authenticated()
            .exchange("/api/deals/nonexistent-id", HttpMethod.DELETE, null, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ==================== REQ-DEAL-003 / US-3.2: Update Deal Status ====================

    @Test
    @DisplayName("REQ-DEAL-003: PUT /api/deals/{id} can update deal status")
    @SuppressWarnings("unchecked")
    void testReqDeal003_updateDealStatus() {
        Map<String, Object> created = createDeal("Status Update Deal", new BigDecimal("30000.00"), "rep-upd-001");
        String dealId = (String) created.get("id");

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("status", "WON");

        ResponseEntity<Map> response = authenticated()
            .exchange("/api/deals/" + dealId, HttpMethod.PUT, jsonEntity(updateRequest), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("status")).isEqualTo("WON");
    }

    @Test
    @DisplayName("REQ-DEAL-003: PUT /api/deals/{id} can update deal title")
    @SuppressWarnings("unchecked")
    void testReqDeal003_updateDealTitle() {
        Map<String, Object> created = createDeal("Original Title", new BigDecimal("10000.00"), "rep-upd-002");
        String dealId = (String) created.get("id");

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("title", "Updated Title");

        ResponseEntity<Map> response = authenticated()
            .exchange("/api/deals/" + dealId, HttpMethod.PUT, jsonEntity(updateRequest), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("title")).isEqualTo("Updated Title");
    }
}
