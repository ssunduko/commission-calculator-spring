package com.chapman.edu.commissions.architecture.eventdriven.integration;

import com.chapman.edu.commissions.architecture.eventdriven.EventDrivenCommissionApplication;
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
 * Integration test for the Event-Driven Architecture module.
 *
 * Tests verify that:
 * 1. CRUD operations work correctly (same as vertical-slice)
 * 2. Domain events are captured in the Event Store
 * 3. Event Store API returns the correct events
 */
@SpringBootTest(classes = EventDrivenCommissionApplication.class, properties = {
    "spring.datasource.url=jdbc:h2:mem:eventdrivenintdb"
})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Event-Driven Architecture — Integration Tests")
class EventDrivenIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String seededSalesRepId;
    private static String newDealId;
    private static String newPlanId;

    // ============================================================
    // STEP 1: VERIFY SEEDED DATA
    // ============================================================

    @Test
    @Order(1)
    @DisplayName("Step 1: GET /api/events/deals -- seeded deals should be present")
    void getDeals_shouldReturnSeededData() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/events/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(6)))
                .andReturn();

        JsonNode deals = objectMapper.readTree(result.getResponse().getContentAsString());
        seededSalesRepId = deals.get(0).path("salesRepId").asText();
        Assertions.assertFalse(seededSalesRepId.isBlank());
    }

    // ============================================================
    // STEP 2: CREATE A DEAL (triggers DealCreatedEvent)
    // ============================================================

    @Test
    @Order(2)
    @DisplayName("Step 2: POST /api/events/deals -- create deal and verify event is stored")
    void createDeal_shouldPublishEvent() throws Exception {
        String requestBody = """
                {
                    "title": "Event-Driven Test Deal",
                    "value": 75000,
                    "salesRepId": "%s"
                }
                """.formatted(seededSalesRepId);

        MvcResult result = mockMvc.perform(post("/api/events/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Event-Driven Test Deal"))
                .andReturn();

        newDealId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("id").asText();

        // Wait briefly for async event processing
        Thread.sleep(100);

        // Verify the event was stored in the Event Store
        mockMvc.perform(get("/api/events/event-store/aggregate/" + newDealId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].eventType").value("DealCreatedEvent"))
                .andExpect(jsonPath("$[0].aggregateType").value("Deal"));
    }

    // ============================================================
    // STEP 3: UPDATE THE DEAL (triggers DealUpdatedEvent)
    // ============================================================

    @Test
    @Order(3)
    @DisplayName("Step 3: PUT /api/events/deals/{id} -- update deal and verify event")
    void updateDeal_shouldPublishEvent() throws Exception {
        String requestBody = """
                {
                    "status": "WON"
                }
                """;

        mockMvc.perform(put("/api/events/deals/" + newDealId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WON"));

        Thread.sleep(100);

        // Should now have 2 events: DealCreatedEvent + DealUpdatedEvent
        mockMvc.perform(get("/api/events/event-store/aggregate/" + newDealId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ============================================================
    // STEP 4: CREATE A PLAN (triggers CommissionPlanCreatedEvent)
    // ============================================================

    @Test
    @Order(4)
    @DisplayName("Step 4: POST /api/events/plans -- create plan and verify event")
    void createPlan_shouldPublishEvent() throws Exception {
        String requestBody = """
                {
                    "name": "Event-Driven Test Plan",
                    "currencyCode": "USD"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/events/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        newPlanId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("id").asText();

        Thread.sleep(100);

        mockMvc.perform(get("/api/events/event-store/aggregate/" + newPlanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("CommissionPlanCreatedEvent"));
    }

    // ============================================================
    // STEP 5: ACTIVATE THE PLAN (triggers CommissionPlanActivatedEvent)
    // ============================================================

    @Test
    @Order(5)
    @DisplayName("Step 5: POST /api/events/plans/{id}/activate -- activate and verify event")
    void activatePlan_shouldPublishEvent() throws Exception {
        mockMvc.perform(post("/api/events/plans/" + newPlanId + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        Thread.sleep(100);

        // Should have CommissionPlanCreatedEvent + CommissionPlanActivatedEvent
        mockMvc.perform(get("/api/events/event-store/aggregate/" + newPlanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ============================================================
    // STEP 6: QUERY EVENT STORE
    // ============================================================

    @Test
    @Order(6)
    @DisplayName("Step 6: GET /api/events/event-store -- verify all events are recorded")
    void eventStore_shouldContainAllEvents() throws Exception {
        mockMvc.perform(get("/api/events/event-store"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(4)));
    }

    @Test
    @Order(7)
    @DisplayName("Step 7: GET /api/events/event-store/type/Deal -- filter by aggregate type")
    void eventStore_shouldFilterByAggregateType() throws Exception {
        mockMvc.perform(get("/api/events/event-store/type/Deal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)));
    }

    // ============================================================
    // STEP 8: DELETE DEAL (triggers DealDeletedEvent)
    // ============================================================

    @Test
    @Order(8)
    @DisplayName("Step 8: DELETE /api/events/deals/{id} -- delete and verify event")
    void deleteDeal_shouldPublishEvent() throws Exception {
        mockMvc.perform(delete("/api/events/deals/" + newDealId))
                .andExpect(status().isNoContent());

        Thread.sleep(100);

        // Should have DealCreatedEvent + DealUpdatedEvent + DealDeletedEvent = 3
        mockMvc.perform(get("/api/events/event-store/aggregate/" + newDealId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }
}
