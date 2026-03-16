package com.chapman.edu.commissions.architecture.microservice.dealservice;

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
 * Integration test for the Deal Service microservice.
 * Tests the service in isolation with its own H2 database.
 */
@SpringBootTest(classes = DealServiceApplication.class, properties = {
    "spring.datasource.url=jdbc:h2:mem:dealtestdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "server.port=0"
})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Deal Service — Microservice Integration Test")
class DealServiceIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    private static String newDealId;

    @Test
    @Order(1)
    @DisplayName("GET /api/deals -- seeded deals from DataInitializer")
    void getDeals_shouldReturnSeededData() throws Exception {
        mockMvc.perform(get("/api/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(6));
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/deals -- create a deal")
    void createDeal_shouldReturn201() throws Exception {
        String body = """
                {"title": "Microservice Test Deal", "value": 60000, "salesRepId": "ms_rep001"}
                """;
        MvcResult result = mockMvc.perform(post("/api/deals")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Microservice Test Deal"))
                .andReturn();
        newDealId = objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asText();
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/deals/{id} -- get created deal")
    void getDeal_shouldReturnDeal() throws Exception {
        mockMvc.perform(get("/api/deals/" + newDealId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Microservice Test Deal"));
    }

    @Test
    @Order(4)
    @DisplayName("PUT /api/deals/{id} -- update deal status")
    void updateDeal_shouldUpdateStatus() throws Exception {
        mockMvc.perform(put("/api/deals/" + newDealId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"WON\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WON"));
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /api/deals/{id} -- delete deal")
    void deleteDeal_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/deals/" + newDealId))
                .andExpect(status().isNoContent());
    }
}
