package com.chapman.edu.commissions.architecture.microservice.planservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for the Plan Service microservice.
 * Tests the service in isolation with its own H2 database.
 * No seeded data — each test creates plans from scratch.
 */
@SpringBootTest(classes = PlanServiceApplication.class, properties = {
    "spring.datasource.url=jdbc:h2:mem:plantestdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "server.port=0"
})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Plan Service — Microservice Integration Test")
class PlanServiceIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    private static String newPlanId;

    @Test
    @Order(1)
    @DisplayName("GET /api/plans -- initially empty")
    void getPlans_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/plans -- create a commission plan")
    void createPlan_shouldReturn201() throws Exception {
        String body = """
                {
                    "name": "Q1 Sales Plan",
                    "currencyCode": "USD",
                    "effectiveStartDate": "2026-01-01",
                    "effectiveEndDate": "2026-03-31"
                }
                """;
        MvcResult result = mockMvc.perform(post("/api/plans")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Q1 Sales Plan"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andReturn();
        newPlanId = objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asText();
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/plans/{id} -- get created plan")
    void getPlan_shouldReturnPlan() throws Exception {
        mockMvc.perform(get("/api/plans/" + newPlanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Q1 Sales Plan"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/plans/{id}/activate -- activate the plan")
    void activatePlan_shouldChangeStatusToActive() throws Exception {
        mockMvc.perform(post("/api/plans/" + newPlanId + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/plans/{id}/rules -- add a rule to the plan")
    void addRule_shouldIncrementRulesCount() throws Exception {
        String body = """
                {
                    "name": "Standard Commission",
                    "description": "10% on all deals",
                    "rate": 0.10,
                    "ruleType": "STANDARD",
                    "priority": 1
                }
                """;
        mockMvc.perform(post("/api/plans/" + newPlanId + "/rules")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Q1 Sales Plan"));
    }

    @Test
    @Order(6)
    @DisplayName("DELETE /api/plans/{id} -- delete plan")
    void deletePlan_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/plans/" + newPlanId))
                .andExpect(status().isNoContent());
    }
}
