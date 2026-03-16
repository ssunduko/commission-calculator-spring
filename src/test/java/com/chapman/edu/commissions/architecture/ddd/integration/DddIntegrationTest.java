package com.chapman.edu.commissions.architecture.ddd.integration;

import com.chapman.edu.commissions.architecture.ddd.DddCommissionApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for the DDD Architecture module.
 *
 * Tests verify that:
 * 1. Aggregate roots are properly persisted
 * 2. Application services orchestrate use cases correctly
 * 3. Domain services perform cross-aggregate calculations
 * 4. Repository abstractions work through JPA implementations
 */
@SpringBootTest(classes = DddCommissionApplication.class, properties = {
    "spring.datasource.url=jdbc:h2:mem:dddintdb"
})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Domain-Driven Design — Integration Tests")
class DddIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static String seededSalesRepId;
    private static String newDealId;
    private static String newPlanId;
    private static String newCalculationId;
    private static String newDisputeId;

    @Test
    @Order(1)
    @DisplayName("Step 1: GET /api/ddd/deals -- verify aggregate roots are seeded")
    void getDeals_shouldReturnSeededAggregates() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/ddd/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(6)))
                .andReturn();
        JsonNode deals = objectMapper.readTree(result.getResponse().getContentAsString());
        seededSalesRepId = deals.get(0).path("salesRepId").asText();
        Assertions.assertFalse(seededSalesRepId.isBlank());
    }

    @Test
    @Order(2)
    @DisplayName("Step 2: POST /api/ddd/deals -- create deal aggregate root")
    void createDeal_shouldPersistAggregateRoot() throws Exception {
        String requestBody = """
                {"title": "DDD Test Deal", "value": 95000, "salesRepId": "%s"}
                """.formatted(seededSalesRepId);
        MvcResult result = mockMvc.perform(post("/api/ddd/deals")
                        .contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("DDD Test Deal"))
                .andReturn();
        newDealId = objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asText();
    }

    @Test
    @Order(3)
    @DisplayName("Step 3: PUT /api/ddd/deals/{id} -- update deal status to WON")
    void updateDeal_shouldModifyAggregate() throws Exception {
        mockMvc.perform(put("/api/ddd/deals/" + newDealId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"WON\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WON"));
    }

    @Test
    @Order(4)
    @DisplayName("Step 4: POST /api/ddd/plans -- create commission plan aggregate")
    void createPlan_shouldPersistPlanAggregate() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/ddd/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"DDD Test Plan\", \"currencyCode\": \"USD\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        newPlanId = objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asText();
    }

    @Test
    @Order(5)
    @DisplayName("Step 5: POST /api/ddd/plans/{id}/activate -- transition plan status")
    void activatePlan_shouldTransitionStatus() throws Exception {
        mockMvc.perform(post("/api/ddd/plans/" + newPlanId + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @Order(6)
    @DisplayName("Step 6: POST /api/ddd/plans/{id}/rules -- add rule to plan aggregate")
    void addRuleToPlan_shouldAddToAggregate() throws Exception {
        String ruleBody = """
                {"name": "DDD Base Rule", "description": "10% commission", "rate": 10.0, "ruleType": "STANDARD", "priority": 1}
                """;
        mockMvc.perform(post("/api/ddd/plans/" + newPlanId + "/rules")
                        .contentType(MediaType.APPLICATION_JSON).content(ruleBody))
                .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    @DisplayName("Step 7: POST /api/ddd/calculations -- domain service calculates commission")
    void calculateCommission_shouldUseDomainService() throws Exception {
        String body = """
                {"dealId": "%s", "planId": "%s"}
                """.formatted(newDealId, newPlanId);
        MvcResult result = mockMvc.perform(post("/api/ddd/calculations")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dealId").value(newDealId))
                .andExpect(jsonPath("$.status").value("CALCULATED"))
                .andReturn();
        newCalculationId = objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asText();
    }

    @Test
    @Order(8)
    @DisplayName("Step 8: POST /api/ddd/disputes -- create dispute aggregate")
    void createDispute_shouldPersistDisputeAggregate() throws Exception {
        String body = """
                {"calculationId": "%s", "salesRepId": "%s", "title": "Rate Error", "description": "Wrong rate applied"}
                """.formatted(newCalculationId, seededSalesRepId);
        MvcResult result = mockMvc.perform(post("/api/ddd/disputes")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        newDisputeId = objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asText();
    }

    @Test
    @Order(9)
    @DisplayName("Step 9: POST /api/ddd/disputes/{id}/escalate -- escalate dispute")
    void escalateDispute_shouldUpdateAggregate() throws Exception {
        mockMvc.perform(post("/api/ddd/disputes/" + newDisputeId + "/escalate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isEscalated").value(true));
    }

    @Test
    @Order(10)
    @DisplayName("Step 10: DELETE /api/ddd/disputes/{id} -- delete dispute")
    void deleteDispute_shouldRemoveAggregate() throws Exception {
        mockMvc.perform(delete("/api/ddd/disputes/" + newDisputeId))
                .andExpect(status().isNoContent());
    }
}
