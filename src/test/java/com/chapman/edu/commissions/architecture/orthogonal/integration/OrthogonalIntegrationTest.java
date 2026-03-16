package com.chapman.edu.commissions.architecture.orthogonal.integration;

import com.chapman.edu.commissions.architecture.orthogonal.OrthogonalCommissionApplication;
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
 * Integration test for the Orthogonal Architecture module.
 *
 * Tests verify that:
 * 1. Commands flow through the pipeline and reach handlers
 * 2. Queries return correct data
 * 3. Aspects (logging, auditing) are applied automatically
 * 4. Audit log records command executions
 */
@SpringBootTest(classes = OrthogonalCommissionApplication.class, properties = {
    "spring.datasource.url=jdbc:h2:mem:orthogonalintdb"
})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Orthogonal Architecture — Integration Tests")
class OrthogonalIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static String seededSalesRepId;
    private static String newDealId;
    private static String newPlanId;

    @Test
    @Order(1)
    @DisplayName("Step 1: GET /api/orthogonal/deals -- seeded deals should be present")
    void getDeals_shouldReturnSeededData() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/orthogonal/deals"))
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
    @DisplayName("Step 2: POST /api/orthogonal/deals -- create deal via command pipeline")
    void createDeal_shouldDispatchCommand() throws Exception {
        String requestBody = """
                {"title": "Orthogonal Test Deal", "value": 80000, "salesRepId": "%s"}
                """.formatted(seededSalesRepId);
        MvcResult result = mockMvc.perform(post("/api/orthogonal/deals")
                        .contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Orthogonal Test Deal"))
                .andReturn();
        newDealId = objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asText();
    }

    @Test
    @Order(3)
    @DisplayName("Step 3: PUT /api/orthogonal/deals/{id} -- update deal via command pipeline")
    void updateDeal_shouldDispatchCommand() throws Exception {
        mockMvc.perform(put("/api/orthogonal/deals/" + newDealId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"WON\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WON"));
    }

    @Test
    @Order(4)
    @DisplayName("Step 4: POST /api/orthogonal/plans -- create plan via command pipeline")
    void createPlan_shouldDispatchCommand() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/orthogonal/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Orthogonal Test Plan\", \"currencyCode\": \"USD\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        newPlanId = objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asText();
    }

    @Test
    @Order(5)
    @DisplayName("Step 5: POST /api/orthogonal/plans/{id}/activate -- activate via command")
    void activatePlan_shouldDispatchCommand() throws Exception {
        mockMvc.perform(post("/api/orthogonal/plans/" + newPlanId + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @Order(6)
    @DisplayName("Step 6: GET /api/orthogonal/audit-log -- verify commands were audited")
    void auditLog_shouldContainCommandExecutions() throws Exception {
        mockMvc.perform(get("/api/orthogonal/audit-log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(3)));
    }

    @Test
    @Order(7)
    @DisplayName("Step 7: GET /api/orthogonal/audit-log/operation/CreateDealCommand -- filter by operation")
    void auditLog_shouldFilterByOperation() throws Exception {
        mockMvc.perform(get("/api/orthogonal/audit-log/operation/CreateDealCommand"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }

    @Test
    @Order(8)
    @DisplayName("Step 8: DELETE /api/orthogonal/deals/{id} -- delete via command pipeline")
    void deleteDeal_shouldDispatchCommand() throws Exception {
        mockMvc.perform(delete("/api/orthogonal/deals/" + newDealId))
                .andExpect(status().isNoContent());
    }
}
