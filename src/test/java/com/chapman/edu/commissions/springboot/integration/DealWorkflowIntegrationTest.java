package com.chapman.edu.commissions.springboot.integration;

import com.chapman.edu.commissions.springboot.CommissionCalculatorSpringBootApplication;
import com.chapman.edu.commissions.springboot.dto.request.CreateDealRequest;
import com.chapman.edu.commissions.springboot.dto.request.CreateUserRequest;
import com.chapman.edu.commissions.springboot.dto.request.LoginRequest;
import com.chapman.edu.commissions.springboot.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ============================================================================
 * INTEGRATION TEST — DEAL CRUD WORKFLOW
 * ============================================================================
 *
 * CONCEPT: End-to-End Workflow Testing
 * ----------------------------------------
 * This integration test verifies the complete Deal lifecycle through
 * the real application stack:
 *
 *   HTTP Request → Security Filter → Controller → Service → Repository
 *
 * Unlike unit tests that mock dependencies, this test uses:
 *   - Real SecurityConfig with JWT authentication
 *   - Real DealService with business validation
 *   - Real DealRepository (HashMap-based)
 *   - Real DtoMapper for response transformation
 *
 * CONCEPT: @TestMethodOrder
 * ---------------------------
 * By default, JUnit 5 does not guarantee test execution order.
 * @TestMethodOrder(OrderAnnotation.class) with @Order(n) lets us
 * run tests in a specific sequence. This is useful for workflow tests
 * where later steps depend on earlier ones (e.g., can't GET a deal
 * that hasn't been POSTed yet).
 *
 * Note: In unit tests, tests should be independent. But in workflow
 * integration tests, ordered execution demonstrates the full lifecycle.
 */
@SpringBootTest(classes = CommissionCalculatorSpringBootApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("SpringBoot Deal Workflow — Integration Tests")
class DealWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    private static String jwtToken;
    private static String createdDealId;
    private static boolean dataSeeded = false;

    @BeforeEach
    void setUp() throws Exception {
        if (!dataSeeded) {
            CreateUserRequest request = new CreateUserRequest();
            request.setUsername("dealuser");
            request.setEmail("deal@test.com");
            request.setFirstName("Deal");
            request.setLastName("Tester");
            request.setPassword("deal1234");
            request.setRoles(Set.of("SALES_REP"));
            userService.createUser(request);

            jwtToken = obtainJwtToken("dealuser", "deal1234");
            dataSeeded = true;
        }
    }

    // ============================================================
    // STEP 1: CREATE A DEAL
    // ============================================================

    @Test
    @Order(1)
    @DisplayName("Step 1: POST /api/deals should create a deal and return 201")
    void createDeal_shouldReturn201_andReturnCreatedDeal() throws Exception {
        CreateDealRequest request = new CreateDealRequest();
        request.setTitle("Integration Test Deal");
        request.setValue(new BigDecimal("75000"));
        request.setSalesRepId("rep-integration");

        MvcResult result = mockMvc.perform(post("/api/deals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Integration Test Deal"))
                .andExpect(jsonPath("$.data.value").value(75000))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andReturn();

        // Extract the ID for subsequent tests
        createdDealId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText();
        assertThat(createdDealId).isNotBlank();
    }

    // ============================================================
    // STEP 2: READ THE DEAL
    // ============================================================

    @Test
    @Order(2)
    @DisplayName("Step 2: GET /api/deals/{id} should return the created deal")
    void getDealById_shouldReturnCreatedDeal() throws Exception {
        mockMvc.perform(get("/api/deals/" + createdDealId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(createdDealId))
                .andExpect(jsonPath("$.data.title").value("Integration Test Deal"));
    }

    @Test
    @Order(3)
    @DisplayName("Step 3: GET /api/deals should include the created deal in the list")
    void getAllDeals_shouldIncludeCreatedDeal() throws Exception {
        // SampleDataLoader pre-seeds deals, so the list will contain more than just our deal.
        // We verify the list is non-empty and includes the deal we created (by salesRepId).
        mockMvc.perform(get("/api/deals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("salesRepId", "rep-integration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Integration Test Deal"));
    }

    // ============================================================
    // STEP 4: UPDATE DEAL STATUS
    // ============================================================

    @Test
    @Order(4)
    @DisplayName("Step 4: PATCH /api/deals/{id}/status should update deal to WON")
    void updateDealStatus_shouldUpdateToWon() throws Exception {
        mockMvc.perform(patch("/api/deals/" + createdDealId + "/status")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("status", "WON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("WON"));
    }

    @Test
    @Order(5)
    @DisplayName("Step 5: GET /api/deals?status=WON should return the won deal")
    void getDealsByStatus_shouldReturnWonDeals() throws Exception {
        mockMvc.perform(get("/api/deals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("status", "WON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("WON"));
    }

    // ============================================================
    // STEP 6: DELETE THE DEAL
    // ============================================================

    @Test
    @Order(6)
    @DisplayName("Step 6: DELETE /api/deals/{id} should return 204")
    void deleteDeal_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/deals/" + createdDealId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(7)
    @DisplayName("Step 7: GET /api/deals/{id} should return 404 after deletion")
    void getDealById_shouldReturn404_afterDeletion() throws Exception {
        mockMvc.perform(get("/api/deals/" + createdDealId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // VALIDATION TESTS
    // ============================================================

    @Test
    @Order(8)
    @DisplayName("POST /api/deals with missing fields should return 400")
    void createDeal_shouldReturn400_withMissingFields() throws Exception {
        CreateDealRequest request = new CreateDealRequest();
        // Missing all required fields

        mockMvc.perform(post("/api/deals")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private String obtainJwtToken(String username, String password) throws Exception {
        LoginRequest login = new LoginRequest();
        login.setUsername(username);
        login.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }
}
