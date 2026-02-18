package com.chapman.edu.commissions.springboot.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * PROCESSOR: RESTful API DEVELOPMENT DEMONSTRATION
 * ============================================================================
 *
 * This runnable demonstrates REST API concepts by making REAL HTTP calls
 * to the running REST controllers using RestTemplate:
 *
 *   1. AUTHENTICATION — POST /api/auth/login to obtain a JWT token
 *   2. CRUD Operations — Create, Read, Update, Delete via HTTP
 *   3. HTTP Method Mapping — GET (read), POST (create), PATCH (update), DELETE
 *   4. Path Variables — /api/deals/{id}
 *   5. Request Parameters — /api/deals?status=WON
 *   6. Response Entities — Status codes (200, 201, 204, 401, 404, 422)
 *   7. Authorization Header — Bearer token in every request
 *
 * CONCEPT: RestTemplate
 * ----------------------
 * RestTemplate is Spring's synchronous HTTP client for making REST API calls.
 * It provides methods that map to HTTP verbs:
 *   - getForEntity()    → GET
 *   - postForEntity()   → POST
 *   - exchange()        → Any HTTP method (GET, POST, PUT, PATCH, DELETE)
 *   - delete()          → DELETE
 *
 * Each method serializes request bodies to JSON and deserializes responses
 * automatically using Jackson (the same library that powers @RequestBody).
 *
 * CONCEPT: HttpHeaders & HttpEntity
 * -----------------------------------
 * HttpHeaders represents HTTP request/response headers.
 * HttpEntity combines headers + body into a single object for RestTemplate:
 *   HttpHeaders headers = new HttpHeaders();
 *   headers.setBearerAuth(jwtToken);
 *   HttpEntity<MyRequest> entity = new HttpEntity<>(requestBody, headers);
 *   restTemplate.exchange(url, HttpMethod.POST, entity, Response.class);
 */
@Component
@Order(3)
@Profile("!test")
public class RestApiProcessor implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RestApiProcessor.class);

    private final RestTemplate restTemplate;

    @Value("${server.port:8081}")
    private int serverPort;

    private String jwtToken;

    public RestApiProcessor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private String baseUrl() {
        return "http://localhost:" + serverPort;
    }

    /**
     * Creates an HttpHeaders object with the JWT Bearer token and JSON content type.
     * Every authenticated API call needs these headers.
     */
    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);
        return headers;
    }

    @Override
    public void run(String... args) {
        logger.info("");
        logger.info("╔══════════════════════════════════════════════════════════════╗");
        logger.info("║   RESTful API DEVELOPMENT DEMONSTRATION                     ║");
        logger.info("║   (Making REAL HTTP calls to REST controllers)              ║");
        logger.info("╚══════════════════════════════════════════════════════════════╝");

        authenticate();
        demonstrateCRUD();
        demonstrateQueryOperations();
        demonstrateBusinessLogic();

        logger.info("");
        logger.info("=== REST API Demo Complete ===");
        logger.info("");
    }

    /**
     * Authenticate via POST /api/auth/login to obtain a JWT token.
     * All subsequent API calls use this token in the Authorization header.
     */
    @SuppressWarnings("unchecked")
    private void authenticate() {
        logger.info("");
        logger.info("--- Authentication (POST /api/auth/login) ---");
        logger.info("");

        String loginUrl = baseUrl() + "/api/auth/login";
        String loginBody = "{\"username\":\"admin\",\"password\":\"admin123\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(loginBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, request, Map.class);

        Map<String, Object> body = response.getBody();
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        jwtToken = (String) data.get("token");
        String username = (String) data.get("username");

        logger.info("  POST {} → {} {}", loginUrl, response.getStatusCode().value(), response.getStatusCode());
        logger.info("  Authenticated as: {}", username);
        logger.info("  JWT Token: {}...", jwtToken.substring(0, Math.min(40, jwtToken.length())));
        logger.info("  (Token will be sent as 'Authorization: Bearer <token>' on all subsequent requests)");
    }

    /**
     * Demonstrates CRUD operations by making real HTTP calls:
     *   POST   /api/deals          — Create
     *   GET    /api/deals/{id}     — Read
     *   PATCH  /api/deals/{id}/status — Update
     *   DELETE /api/deals/{id}     — Delete
     */
    @SuppressWarnings("unchecked")
    private void demonstrateCRUD() {
        logger.info("");
        logger.info("--- CRUD Operations (via HTTP to @RestController) ---");

        // CREATE — POST /api/deals
        logger.info("");
        logger.info("[POST /api/deals] — Creating a new deal...");
        String createBody = "{\"title\":\"New Enterprise Deal\",\"value\":55000,\"salesRepId\":\"user-003\"}";
        HttpEntity<String> createEntity = new HttpEntity<>(createBody, authHeaders());

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                baseUrl() + "/api/deals", createEntity, Map.class);

        Map<String, Object> createData = (Map<String, Object>) createResponse.getBody().get("data");
        String newDealId = (String) createData.get("id");
        logger.info("  HTTP {} {} — Created deal:", createResponse.getStatusCode().value(), createResponse.getStatusCode());
        logger.info("    ID: {}, Title: {}, Status: {}", newDealId, createData.get("title"), createData.get("status"));

        // READ — GET /api/deals/{id}
        logger.info("");
        logger.info("[GET /api/deals/{}] — Reading deal by ID...", newDealId);
        HttpEntity<Void> getEntity = new HttpEntity<>(authHeaders());

        ResponseEntity<Map> getResponse = restTemplate.exchange(
                baseUrl() + "/api/deals/" + newDealId, HttpMethod.GET, getEntity, Map.class);

        Map<String, Object> getData = (Map<String, Object>) getResponse.getBody().get("data");
        logger.info("  HTTP {} {} — Found: {} — Value: ${}",
                getResponse.getStatusCode().value(), getResponse.getStatusCode(),
                getData.get("title"), getData.get("value"));

        // UPDATE — PATCH /api/deals/{id}/status?status=WON
        logger.info("");
        logger.info("[PATCH /api/deals/{}/status?status=WON] — Updating deal status...", newDealId);
        HttpEntity<Void> patchEntity = new HttpEntity<>(authHeaders());

        ResponseEntity<Map> patchResponse = restTemplate.exchange(
                baseUrl() + "/api/deals/" + newDealId + "/status?status=WON",
                HttpMethod.PATCH, patchEntity, Map.class);

        Map<String, Object> patchData = (Map<String, Object>) patchResponse.getBody().get("data");
        logger.info("  HTTP {} {} — Updated status: OPEN → {}",
                patchResponse.getStatusCode().value(), patchResponse.getStatusCode(),
                patchData.get("status"));

        // DELETE — DELETE /api/deals/{id}
        logger.info("");
        logger.info("[DELETE /api/deals/{}] — Deleting deal...", newDealId);
        HttpEntity<Void> deleteEntity = new HttpEntity<>(authHeaders());

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl() + "/api/deals/" + newDealId, HttpMethod.DELETE, deleteEntity, Void.class);

        logger.info("  HTTP {} {} — Deal deleted (no response body for 204)",
                deleteResponse.getStatusCode().value(), deleteResponse.getStatusCode());
    }

    /**
     * Demonstrates query operations with @RequestParam filtering:
     *   GET /api/deals              — All deals
     *   GET /api/deals?status=WON   — Filter by status
     *   GET /api/deals?salesRepId=user-003 — Filter by sales rep
     */
    @SuppressWarnings("unchecked")
    private void demonstrateQueryOperations() {
        logger.info("");
        logger.info("--- Query Operations (via HTTP with @RequestParam) ---");
        HttpEntity<Void> getEntity = new HttpEntity<>(authHeaders());

        // GET /api/deals (all)
        logger.info("");
        logger.info("[GET /api/deals] — All deals:");
        ResponseEntity<Map> allResponse = restTemplate.exchange(
                baseUrl() + "/api/deals", HttpMethod.GET, getEntity, Map.class);
        List<Map<String, Object>> allDeals = (List<Map<String, Object>>) allResponse.getBody().get("data");
        allDeals.forEach(d -> logger.info("  {} — ${} ({})", d.get("title"), d.get("value"), d.get("status")));

        // GET /api/deals?status=WON
        logger.info("");
        logger.info("[GET /api/deals?status=WON] — Won deals only:");
        ResponseEntity<Map> wonResponse = restTemplate.exchange(
                baseUrl() + "/api/deals?status=WON", HttpMethod.GET, getEntity, Map.class);
        List<Map<String, Object>> wonDeals = (List<Map<String, Object>>) wonResponse.getBody().get("data");
        wonDeals.forEach(d -> logger.info("  {} — ${}", d.get("title"), d.get("value")));

        // GET /api/deals?salesRepId=user-003
        logger.info("");
        logger.info("[GET /api/deals?salesRepId=user-003] — Deals for Ana Garcia:");
        ResponseEntity<Map> repResponse = restTemplate.exchange(
                baseUrl() + "/api/deals?salesRepId=user-003", HttpMethod.GET, getEntity, Map.class);
        List<Map<String, Object>> repDeals = (List<Map<String, Object>>) repResponse.getBody().get("data");
        repDeals.forEach(d -> logger.info("  {} — ${}", d.get("title"), d.get("value")));
    }

    /**
     * Demonstrates business logic and error handling through the API:
     *   POST /api/calculations — Calculate commission (success & failure)
     *   Demonstrates how HTTP status codes communicate errors (422, 404)
     */
    private void demonstrateBusinessLogic() {
        logger.info("");
        logger.info("--- Business Logic & Error Handling (via HTTP) ---");

        // POST /api/calculations — Calculate commission for a won deal
        logger.info("");
        logger.info("[POST /api/calculations] — Calculating commission for deal-001...");
        try {
            String calcBody = "{\"dealId\":\"deal-001\",\"planId\":\"plan-001\",\"calculatedBy\":\"user-002\"}";
            HttpEntity<String> calcEntity = new HttpEntity<>(calcBody, authHeaders());

            ResponseEntity<Map> calcResponse = restTemplate.postForEntity(
                    baseUrl() + "/api/calculations", calcEntity, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> calcData = (Map<String, Object>) calcResponse.getBody().get("data");
            logger.info("  HTTP {} {} — Commission calculated:", calcResponse.getStatusCode().value(), calcResponse.getStatusCode());
            logger.info("    Base Commission: ${}", calcData.get("baseCommission"));
            logger.info("    Gross Commission: ${}", calcData.get("grossCommission"));
            logger.info("    Net Commission: ${}", calcData.get("netCommission"));
        } catch (HttpClientErrorException e) {
            logger.info("  HTTP {} — {}", e.getStatusCode().value(), e.getResponseBodyAsString());
        }

        // POST /api/calculations — Try with an OPEN deal (should get HTTP 422)
        logger.info("");
        logger.info("[POST /api/calculations] — Attempting commission on OPEN deal (expect HTTP 422)...");
        try {
            String badCalcBody = "{\"dealId\":\"deal-004\",\"planId\":\"plan-001\",\"calculatedBy\":\"user-002\"}";
            HttpEntity<String> badEntity = new HttpEntity<>(badCalcBody, authHeaders());

            restTemplate.postForEntity(baseUrl() + "/api/calculations", badEntity, Map.class);
        } catch (HttpClientErrorException e) {
            logger.info("  HTTP {} — Business validation error caught!", e.getStatusCode().value());
            logger.info("  Response: {}", e.getResponseBodyAsString());
            logger.info("  (GlobalExceptionHandler mapped BusinessValidationException → HTTP 422)");
        }

        // GET /api/deals/non-existent — Expect HTTP 404
        logger.info("");
        logger.info("[GET /api/deals/non-existent] — Requesting a deal that doesn't exist (expect HTTP 404)...");
        try {
            HttpEntity<Void> getEntity = new HttpEntity<>(authHeaders());
            restTemplate.exchange(
                    baseUrl() + "/api/deals/non-existent", HttpMethod.GET, getEntity, Map.class);
        } catch (HttpClientErrorException e) {
            logger.info("  HTTP {} — Resource not found!", e.getStatusCode().value());
            logger.info("  Response: {}", e.getResponseBodyAsString());
            logger.info("  (GlobalExceptionHandler mapped ResourceNotFoundException → HTTP 404)");
        }

        // GET /api/plans — Without auth token (expect HTTP 401)
        logger.info("");
        logger.info("[GET /api/plans] — Calling WITHOUT JWT token (expect HTTP 401)...");
        try {
            HttpHeaders noAuthHeaders = new HttpHeaders();
            noAuthHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> noAuthEntity = new HttpEntity<>(noAuthHeaders);

            restTemplate.exchange(
                    baseUrl() + "/api/plans", HttpMethod.GET, noAuthEntity, Map.class);
        } catch (HttpClientErrorException e) {
            logger.info("  HTTP {} — Unauthorized! No JWT token provided.", e.getStatusCode().value());
            logger.info("  (Spring Security's AuthenticationEntryPoint returned 401)");
        }
    }
}
