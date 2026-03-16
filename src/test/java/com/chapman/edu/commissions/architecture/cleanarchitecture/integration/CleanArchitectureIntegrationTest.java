package com.chapman.edu.commissions.architecture.cleanarchitecture.integration;

import com.chapman.edu.commissions.architecture.cleanarchitecture.CleanArchitectureApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ============================================================================
 * INTEGRATION TEST -- Clean Architecture Commission Workflow
 * ============================================================================
 *
 * Full-stack integration test that exercises the complete commission lifecycle
 * through the real application stack:
 *
 *   HTTP Request -> Controller -> Use Case Service -> Repository Port -> JPA Adapter -> H2 Database
 *
 * The DataInitializer seeds sample data on startup (3 users, 2 plans, 6 deals,
 * 2 calculations, 2 disputes). Flyway V2 also seeds data (4 users, 3 plans,
 * 6 deals, 4 calculations, 2 disputes). GET endpoints therefore return combined
 * results from both sources.
 *
 * Security permits all requests under /api/clean/** (no auth required).
 *
 * Tests are ordered to simulate a realistic workflow:
 *   1. Verify seeded deals exist
 *   2. Create a new deal (using a seeded user's ID to satisfy FK constraint)
 *   3. Retrieve the created deal
 *   4. Update the deal status to WON
 *   5. Verify seeded plans exist
 *   6. Create a new commission plan
 *   7. Activate the plan
 *   8. Add a rule to the plan
 *   9. Calculate commission for the deal + plan
 *  10. Verify the calculation
 *  11. Create a dispute for the calculation
 *  12. Verify the dispute
 *  13. Escalate the dispute
 *  14. Resolve the dispute
 *  15. Cleanup: delete the created deal
 */
@SpringBootTest(classes = CleanArchitectureApplication.class, properties = {
    "spring.datasource.url=jdbc:h2:mem:cleanarchdb"
})
@AutoConfigureMockMvc
@ActiveProfiles("cleanarchitecture")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Clean Architecture Commission Workflow -- Integration Tests")
class CleanArchitectureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Shared state across ordered tests
    private static String seededSalesRepId;
    private static String newDealId;
    private static String newPlanId;
    private static String newCalculationId;
    private static String newDisputeId;
    private static int initialDealCount;

    // ============================================================
    // STEP 1: VERIFY SEEDED DEALS
    // ============================================================

    @Test
    @Order(1)
    @DisplayName("Step 1: GET /api/clean/deals -- seeded deals should be present")
    void getDeals_shouldReturnSeededData() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/clean/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(6)))
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].title").isNotEmpty())
                .andExpect(jsonPath("$[0].salesRepId").isNotEmpty())
                .andReturn();

        // Extract a valid salesRepId from seeded data (needed for FK constraints)
        JsonNode deals = objectMapper.readTree(result.getResponse().getContentAsString());
        initialDealCount = deals.size();
        seededSalesRepId = deals.get(0).path("salesRepId").asText();
        Assertions.assertFalse(seededSalesRepId.isBlank(), "Seeded salesRepId should not be blank");
    }

    // ============================================================
    // STEP 2: CREATE A NEW DEAL
    // ============================================================

    @Test
    @Order(2)
    @DisplayName("Step 2: POST /api/clean/deals -- create a new deal")
    void createDeal_shouldReturn201() throws Exception {
        String requestBody = """
                {
                    "title": "Integration Test Deal",
                    "value": 100000,
                    "salesRepId": "%s"
                }
                """.formatted(seededSalesRepId);

        MvcResult result = mockMvc.perform(post("/api/clean/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Integration Test Deal"))
                .andExpect(jsonPath("$.value").value(100000))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.salesRepId").value(seededSalesRepId))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        newDealId = json.path("id").asText();
        Assertions.assertFalse(newDealId.isBlank(), "Deal ID should not be blank");
    }

    // ============================================================
    // STEP 3: GET THE CREATED DEAL
    // ============================================================

    @Test
    @Order(3)
    @DisplayName("Step 3: GET /api/clean/deals/{id} -- retrieve the created deal")
    void getDealById_shouldReturnCreatedDeal() throws Exception {
        mockMvc.perform(get("/api/clean/deals/" + newDealId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(newDealId))
                .andExpect(jsonPath("$.title").value("Integration Test Deal"))
                .andExpect(jsonPath("$.value").value(100000))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    // ============================================================
    // STEP 4: UPDATE DEAL STATUS TO WON
    // ============================================================

    @Test
    @Order(4)
    @DisplayName("Step 4: PUT /api/clean/deals/{id} -- update deal status to WON")
    void updateDeal_shouldSetStatusToWon() throws Exception {
        String requestBody = """
                {
                    "status": "WON",
                    "closeDate": "2026-03-15"
                }
                """;

        mockMvc.perform(put("/api/clean/deals/" + newDealId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(newDealId))
                .andExpect(jsonPath("$.status").value("WON"))
                .andExpect(jsonPath("$.closeDate").value("2026-03-15"));
    }

    // ============================================================
    // STEP 5: VERIFY SEEDED PLANS
    // ============================================================

    @Test
    @Order(5)
    @DisplayName("Step 5: GET /api/clean/plans -- seeded plans should be present")
    void getPlans_shouldReturnSeededData() throws Exception {
        mockMvc.perform(get("/api/clean/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].name").isNotEmpty());
    }

    // ============================================================
    // STEP 6: CREATE A NEW COMMISSION PLAN
    // ============================================================

    @Test
    @Order(6)
    @DisplayName("Step 6: POST /api/clean/plans -- create a new commission plan")
    void createPlan_shouldReturn201() throws Exception {
        String requestBody = """
                {
                    "name": "Integration Test Plan",
                    "currencyCode": "USD",
                    "effectiveStartDate": "2026-01-01",
                    "effectiveEndDate": "2026-12-31"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/clean/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Integration Test Plan"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        newPlanId = json.path("id").asText();
        Assertions.assertFalse(newPlanId.isBlank(), "Plan ID should not be blank");
    }

    // ============================================================
    // STEP 7: ACTIVATE THE PLAN
    // ============================================================

    @Test
    @Order(7)
    @DisplayName("Step 7: POST /api/clean/plans/{id}/activate -- activate the plan")
    void activatePlan_shouldSetStatusToActive() throws Exception {
        mockMvc.perform(post("/api/clean/plans/" + newPlanId + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(newPlanId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    // ============================================================
    // STEP 8: ADD A RULE TO THE PLAN
    // ============================================================

    @Test
    @Order(8)
    @DisplayName("Step 8: POST /api/clean/plans/{id}/rules -- add a commission rule")
    void addRuleToPlan_shouldReturnPlanWithRule() throws Exception {
        String requestBody = """
                {
                    "name": "Integration Test Rule",
                    "description": "15% commission for integration test",
                    "rate": 15.0,
                    "ruleType": "STANDARD",
                    "priority": 1
                }
                """;

        // The addRule service adds the rule to the in-memory plan and returns
        // the PlanResult with the updated rulesCount. Note: CommissionRule is
        // persisted in its own table (commission_rules), while the plan's rules
        // list is @Transient -- so rulesCount reflects the in-memory state.
        mockMvc.perform(post("/api/clean/plans/" + newPlanId + "/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(newPlanId))
                .andExpect(jsonPath("$.name").value("Integration Test Plan"));
    }

    // ============================================================
    // STEP 9: CALCULATE COMMISSION
    // ============================================================

    @Test
    @Order(9)
    @DisplayName("Step 9: POST /api/clean/calculations -- calculate commission for deal + plan")
    void calculateCommission_shouldReturn201() throws Exception {
        String requestBody = """
                {
                    "dealId": "%s",
                    "planId": "%s"
                }
                """.formatted(newDealId, newPlanId);

        MvcResult result = mockMvc.perform(post("/api/clean/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.dealId").value(newDealId))
                .andExpect(jsonPath("$.planId").value(newPlanId))
                .andExpect(jsonPath("$.salesRepId").value(seededSalesRepId))
                .andExpect(jsonPath("$.status").value("CALCULATED"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        newCalculationId = json.path("id").asText();
        Assertions.assertFalse(newCalculationId.isBlank(), "Calculation ID should not be blank");
    }

    // ============================================================
    // STEP 10: VERIFY THE CALCULATION
    // ============================================================

    @Test
    @Order(10)
    @DisplayName("Step 10: GET /api/clean/calculations/{id} -- verify the calculation")
    void getCalculationById_shouldReturnCreatedCalculation() throws Exception {
        mockMvc.perform(get("/api/clean/calculations/" + newCalculationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(newCalculationId))
                .andExpect(jsonPath("$.dealId").value(newDealId))
                .andExpect(jsonPath("$.planId").value(newPlanId))
                .andExpect(jsonPath("$.salesRepId").value(seededSalesRepId))
                .andExpect(jsonPath("$.status").value("CALCULATED"));
    }

    // ============================================================
    // STEP 11: CREATE A DISPUTE
    // ============================================================

    @Test
    @Order(11)
    @DisplayName("Step 11: POST /api/clean/disputes -- create a dispute for the calculation")
    void createDispute_shouldReturn201() throws Exception {
        String requestBody = """
                {
                    "calculationId": "%s",
                    "salesRepId": "%s",
                    "title": "Integration Test Dispute",
                    "description": "Commission rate appears incorrect for this deal size"
                }
                """.formatted(newCalculationId, seededSalesRepId);

        MvcResult result = mockMvc.perform(post("/api/clean/disputes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.calculationId").value(newCalculationId))
                .andExpect(jsonPath("$.salesRepId").value(seededSalesRepId))
                .andExpect(jsonPath("$.title").value("Integration Test Dispute"))
                .andExpect(jsonPath("$.status").value("INITIATED"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        newDisputeId = json.path("id").asText();
        Assertions.assertFalse(newDisputeId.isBlank(), "Dispute ID should not be blank");
    }

    // ============================================================
    // STEP 12: VERIFY THE DISPUTE
    // ============================================================

    @Test
    @Order(12)
    @DisplayName("Step 12: GET /api/clean/disputes/{id} -- verify the dispute")
    void getDisputeById_shouldReturnCreatedDispute() throws Exception {
        mockMvc.perform(get("/api/clean/disputes/" + newDisputeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(newDisputeId))
                .andExpect(jsonPath("$.calculationId").value(newCalculationId))
                .andExpect(jsonPath("$.title").value("Integration Test Dispute"))
                .andExpect(jsonPath("$.status").value("INITIATED"))
                .andExpect(jsonPath("$.isEscalated").value(false));
    }

    // ============================================================
    // STEP 13: ESCALATE THE DISPUTE
    // ============================================================

    @Test
    @Order(13)
    @DisplayName("Step 13: POST /api/clean/disputes/{id}/escalate -- escalate the dispute")
    void escalateDispute_shouldSetEscalatedFlag() throws Exception {
        mockMvc.perform(post("/api/clean/disputes/" + newDisputeId + "/escalate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(newDisputeId))
                .andExpect(jsonPath("$.isEscalated").value(true))
                .andExpect(jsonPath("$.status").value("ESCALATED"));
    }

    // ============================================================
    // STEP 14: RESOLVE THE DISPUTE
    // ============================================================

    @Test
    @Order(14)
    @DisplayName("Step 14: POST /api/clean/disputes/{id}/resolve -- resolve the dispute")
    void resolveDispute_shouldSetStatusToApproved() throws Exception {
        String requestBody = """
                {
                    "resolution": "Rate recalculated and adjusted per contract terms",
                    "resolvedBy": "manager_001",
                    "approved": true
                }
                """;

        mockMvc.perform(post("/api/clean/disputes/" + newDisputeId + "/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(newDisputeId))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.resolution").value("Rate recalculated and adjusted per contract terms"));
    }

    // ============================================================
    // STEP 15: CLEANUP -- DELETE THE CREATED DEAL
    // ============================================================

    @Test
    @Order(15)
    @DisplayName("Step 15: DELETE dependent data then the deal -- cleanup created resources")
    void deleteDeal_shouldReturn204() throws Exception {
        // Delete in reverse dependency order: dispute -> calculation -> deal
        mockMvc.perform(delete("/api/clean/disputes/" + newDisputeId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/clean/calculations/" + newCalculationId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/clean/deals/" + newDealId))
                .andExpect(status().isNoContent());

        // Verify the deal count is back to the original seeded count
        mockMvc.perform(get("/api/clean/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(initialDealCount));
    }
}
