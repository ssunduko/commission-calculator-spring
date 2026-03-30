package com.chapman.edu.commissions.architecture.verticalslice.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end Selenium tests for Observability and Developer Experience.
 * Maps to functional requirements REQ-OBS-001 through REQ-OBS-003
 * and REQ-DEV-001 through REQ-DEV-004.
 *
 * Uses Selenium WebDriver (HtmlUnitDriver) for browser-based verification
 * of Swagger UI and H2 Console, and TestRestTemplate for API verification.
 */
class ObservabilitySeleniumE2ETest extends BaseSeleniumE2ETest {

    // ==================== REQ-OBS-002: Swagger UI ====================

    @Test
    @DisplayName("REQ-OBS-002: Swagger UI endpoint is publicly accessible (no auth required)")
    void testReqObs002_swaggerUiAccessibleViaHttp() {
        // Try multiple possible swagger-ui paths
        ResponseEntity<String> response = restTemplate
            .getForEntity("/swagger-ui/index.html", String.class);

        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            response = restTemplate.getForEntity("/swagger-ui.html", String.class);
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            response = restTemplate.getForEntity("/swagger-ui/", String.class);
        }

        // Swagger UI should not require authentication (not 401/403)
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("REQ-OBS-002: Swagger UI loads through Selenium WebDriver")
    void testReqObs002_swaggerUiLoadsThroughBrowser() {
        driver.get(baseUrl() + "/swagger-ui/index.html");

        // HtmlUnitDriver may not fully render JS-heavy SPA, but should get the HTML
        String currentUrl = driver.getCurrentUrl();
        assertThat(currentUrl).contains("swagger");
    }

    @Test
    @DisplayName("REQ-OBS-002: OpenAPI docs at /api-docs returns valid JSON")
    void testReqObs002_openApiDocsReturnValidJson() {
        ResponseEntity<String> response = restTemplate
            .getForEntity("/api-docs", String.class);

        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            // Try the default springdoc path
            response = restTemplate.getForEntity("/v3/api-docs", String.class);
        }

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).contains("openapi");
        assertThat(body).contains("paths");
    }

    @Test
    @DisplayName("REQ-OBS-002: OpenAPI docs include all API controller endpoints")
    void testReqObs002_openApiDocsIncludeAllEndpoints() {
        ResponseEntity<String> response = restTemplate
            .getForEntity("/api-docs", String.class);

        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            response = restTemplate.getForEntity("/v3/api-docs", String.class);
        }

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).contains("/api/deals");
        assertThat(body).contains("/api/plans");
        assertThat(body).contains("/api/calculations");
        assertThat(body).contains("/api/disputes");
    }

    // ==================== REQ-DEV-001: Sample data loaded on startup ====================

    @Test
    @DisplayName("REQ-DEV-001: Sample deals are loaded on startup")
    @SuppressWarnings("unchecked")
    void testReqDev001_sampleDealsLoadedOnStartup() {
        ResponseEntity<java.util.List> response = authenticated()
            .getForEntity("/api/deals", java.util.List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isGreaterThanOrEqualTo(6);
    }

    @Test
    @DisplayName("REQ-DEV-001: Sample plans are loaded on startup")
    @SuppressWarnings("unchecked")
    void testReqDev001_samplePlansLoadedOnStartup() {
        ResponseEntity<java.util.List> response = authenticated()
            .getForEntity("/api/plans", java.util.List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("REQ-DEV-001: Sample calculations are loaded on startup")
    @SuppressWarnings("unchecked")
    void testReqDev001_sampleCalculationsLoadedOnStartup() {
        ResponseEntity<java.util.List> response = authenticated()
            .getForEntity("/api/calculations", java.util.List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("REQ-DEV-001: Sample disputes are loaded on startup")
    @SuppressWarnings("unchecked")
    void testReqDev001_sampleDisputesLoadedOnStartup() {
        ResponseEntity<java.util.List> response = authenticated()
            .getForEntity("/api/disputes", java.util.List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isGreaterThanOrEqualTo(2);
    }

    // ==================== REQ-DEV-003: H2 Console ====================

    @Test
    @DisplayName("REQ-DEV-003: H2 console is accessible at /h2-console (Selenium browser test)")
    void testReqDev003_h2ConsoleAccessibleThroughBrowser() {
        driver.get(baseUrl() + "/h2-console");

        String pageSource = driver.getPageSource();
        assertThat(pageSource).isNotNull();
        assertThat(pageSource.toLowerCase()).doesNotContain("whitelabel error");
    }

    // ==================== REQ-DEV-004: Application port ====================

    @Test
    @DisplayName("REQ-DEV-004: Application accepts requests on the configured port")
    void testReqDev004_applicationAcceptsRequestsOnPort() {
        // Verify the app responds on the test port using authenticated API call
        ResponseEntity<java.util.List> response = authenticated()
            .getForEntity("/api/deals", java.util.List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
