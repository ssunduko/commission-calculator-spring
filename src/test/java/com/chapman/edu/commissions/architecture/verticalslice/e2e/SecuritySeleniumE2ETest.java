package com.chapman.edu.commissions.architecture.verticalslice.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end Selenium tests for Security.
 * Maps to functional requirements REQ-AUTH-002, REQ-AUTH-006, REQ-AUTH-007
 * and user stories US-1.1, US-1.3.
 *
 * Note: The vertical slice architecture uses HTTP Basic Auth instead of JWT.
 * These tests verify the basic auth security configuration.
 */
class SecuritySeleniumE2ETest extends BaseSeleniumE2ETest {

    // ==================== REQ-AUTH-002: Protected endpoints require auth ====================

    @Test
    @DisplayName("REQ-AUTH-002: GET /api/deals without credentials returns 401")
    void testReqAuth002_dealsEndpointRequiresAuth() {
        ResponseEntity<String> response = restTemplate
            .getForEntity("/api/deals", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("REQ-AUTH-002: GET /api/plans without credentials returns 401")
    void testReqAuth002_plansEndpointRequiresAuth() {
        ResponseEntity<String> response = restTemplate
            .getForEntity("/api/plans", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("REQ-AUTH-002: GET /api/calculations without credentials returns 401")
    void testReqAuth002_calculationsEndpointRequiresAuth() {
        ResponseEntity<String> response = restTemplate
            .getForEntity("/api/calculations", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("REQ-AUTH-002: GET /api/disputes without credentials returns 401")
    void testReqAuth002_disputesEndpointRequiresAuth() {
        ResponseEntity<String> response = restTemplate
            .getForEntity("/api/disputes", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("REQ-AUTH-002: POST /api/deals without credentials returns 401")
    void testReqAuth002_postDealsRequiresAuth() {
        ResponseEntity<String> response = restTemplate
            .postForEntity("/api/deals", Map.of("title", "test"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ==================== REQ-AUTH-002: Valid credentials succeed ====================

    @Test
    @DisplayName("REQ-AUTH-002: GET /api/deals with valid Basic Auth returns 200")
    void testReqAuth002_validCredentialsAccessDeals() {
        ResponseEntity<List> response = authenticated()
            .getForEntity("/api/deals", List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("REQ-AUTH-002: Invalid credentials return 401")
    void testReqAuth002_invalidCredentialsReturn401() {
        ResponseEntity<String> response = restTemplate
            .withBasicAuth("admin", "wrongpassword")
            .getForEntity("/api/deals", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("REQ-AUTH-002: Wrong username returns 401")
    void testReqAuth002_wrongUsernameReturns401() {
        ResponseEntity<String> response = restTemplate
            .withBasicAuth("nonexistent", "admin123")
            .getForEntity("/api/deals", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ==================== REQ-AUTH-006: Public endpoints ====================

    @Test
    @DisplayName("REQ-AUTH-006: Swagger UI is publicly accessible without auth (not 401/403)")
    void testReqAuth006_swaggerUiPubliclyAccessible() {
        // Try multiple possible swagger-ui paths
        ResponseEntity<String> response = restTemplate
            .getForEntity("/swagger-ui/index.html", String.class);

        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            response = restTemplate.getForEntity("/swagger-ui.html", String.class);
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            response = restTemplate.getForEntity("/swagger-ui/", String.class);
        }

        // Key assertion: swagger endpoints do NOT require authentication
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("REQ-AUTH-006: OpenAPI docs are publicly accessible without auth")
    void testReqAuth006_openApiDocsPubliclyAccessible() {
        ResponseEntity<String> response = restTemplate
            .getForEntity("/api-docs", String.class);

        // api-docs may redirect; both 200 and 3xx are acceptable
        assertThat(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is3xxRedirection()).isTrue();
    }

    @Test
    @DisplayName("REQ-AUTH-006: H2 console is publicly accessible without auth")
    void testReqAuth006_h2ConsolePubliclyAccessible() {
        ResponseEntity<String> response = restTemplate
            .getForEntity("/h2-console", String.class);

        // H2 console typically redirects; not 401/403 is the key assertion
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("REQ-AUTH-006: MCP endpoints are publicly accessible without auth")
    void testReqAuth006_mcpEndpointPubliclyAccessible() {
        ResponseEntity<String> response = restTemplate
            .getForEntity("/api/mcp/info", String.class);

        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ==================== REQ-AUTH-007: CSRF disabled ====================

    @Test
    @DisplayName("REQ-AUTH-007: POST API requests succeed without CSRF token")
    @SuppressWarnings("unchecked")
    void testReqAuth007_postSucceedsWithoutCsrfToken() {
        Map<String, Object> request = Map.of(
            "title", "CSRF Test Deal",
            "value", 1000.00,
            "salesRepId", "rep-csrf-test"
        );

        ResponseEntity<Map> response = authenticated()
            .postForEntity("/api/deals", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("REQ-AUTH-007: DELETE API requests succeed without CSRF token")
    @SuppressWarnings("unchecked")
    void testReqAuth007_deleteSucceedsWithoutCsrfToken() {
        // Create then delete to verify CSRF is disabled for DELETE
        Map<String, Object> deal = createDeal("CSRF Delete Test", new java.math.BigDecimal("500.00"), "rep-csrf-del");
        String dealId = (String) deal.get("id");

        ResponseEntity<Void> response = authenticated()
            .exchange("/api/deals/" + dealId, org.springframework.http.HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
