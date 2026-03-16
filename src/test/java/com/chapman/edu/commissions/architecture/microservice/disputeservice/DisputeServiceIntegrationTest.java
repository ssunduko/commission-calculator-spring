package com.chapman.edu.commissions.architecture.microservice.disputeservice;

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
 * Integration test for the Dispute Service microservice.
 * Tests the service in isolation with its own H2 database.
 * Uses hardcoded calculationId and salesRepId since the dispute service
 * does not have access to other services' data.
 */
@SpringBootTest(classes = DisputeServiceApplication.class, properties = {
    "spring.datasource.url=jdbc:h2:mem:disputetestdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "server.port=0"
})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Dispute Service — Microservice Integration Test")
class DisputeServiceIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    private static String newDisputeId;

    private static final String TEST_CALCULATION_ID = "test-calc-001";
    private static final String TEST_SALES_REP_ID = "ms_rep001";

    @Test
    @Order(1)
    @DisplayName("POST /api/disputes -- create a dispute")
    void createDispute_shouldReturnDispute() throws Exception {
        String body = """
                {
                    "calculationId": "%s",
                    "salesRepId": "%s",
                    "title": "Incorrect commission rate",
                    "description": "The applied rate of 5%% should be 10%% per the Q1 plan."
                }
                """.formatted(TEST_CALCULATION_ID, TEST_SALES_REP_ID);

        MvcResult result = mockMvc.perform(post("/api/disputes")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Incorrect commission rate"))
                .andExpect(jsonPath("$.status").value("INITIATED"))
                .andExpect(jsonPath("$.calculationId").value(TEST_CALCULATION_ID))
                .andExpect(jsonPath("$.salesRepId").value(TEST_SALES_REP_ID))
                .andExpect(jsonPath("$.escalated").value(false))
                .andReturn();
        newDisputeId = objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asText();
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/disputes/{id} -- get created dispute")
    void getDispute_shouldReturnDispute() throws Exception {
        mockMvc.perform(get("/api/disputes/" + newDisputeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Incorrect commission rate"))
                .andExpect(jsonPath("$.status").value("INITIATED"));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/disputes -- list all disputes")
    void getAllDisputes_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/disputes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/disputes/{id}/escalate -- escalate a dispute")
    void escalateDispute_shouldChangeStatus() throws Exception {
        mockMvc.perform(post("/api/disputes/" + newDisputeId + "/escalate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escalated").value(true))
                .andExpect(jsonPath("$.status").value("ESCALATED"));
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/disputes/{id}/resolve -- resolve a dispute")
    void resolveDispute_shouldSetResolution() throws Exception {
        String body = """
                {
                    "resolution": "Rate corrected to 10%%. Recalculation issued.",
                    "resolvedBy": "manager_001",
                    "approved": true
                }
                """;
        mockMvc.perform(post("/api/disputes/" + newDisputeId + "/resolve")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @Order(6)
    @DisplayName("DELETE /api/disputes/{id} -- delete dispute")
    void deleteDispute_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/disputes/" + newDisputeId))
                .andExpect(status().isNoContent());
    }
}
