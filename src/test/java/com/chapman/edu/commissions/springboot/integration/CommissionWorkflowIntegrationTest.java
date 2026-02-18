package com.chapman.edu.commissions.springboot.integration;

import com.chapman.edu.commissions.springboot.CommissionCalculatorSpringBootApplication;
import com.chapman.edu.commissions.springboot.dto.request.*;
import com.chapman.edu.commissions.springboot.service.CommissionPlanService;
import com.chapman.edu.commissions.springboot.service.DealService;
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
import java.time.LocalDate;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ============================================================================
 * INTEGRATION TEST — FULL COMMISSION WORKFLOW
 * ============================================================================
 *
 * CONCEPT: Cross-Service Integration Testing
 * ---------------------------------------------
 * This test exercises the complete commission calculation workflow across
 * multiple services working together:
 *
 *   1. Create a Commission Plan (CommissionPlanService)
 *   2. Activate the Plan
 *   3. Create a Deal (DealService)
 *   4. Mark the Deal as WON
 *   5. Calculate Commission (CommissionCalculationService — orchestrates Plan + Deal)
 *   6. Approve the Calculation (requires SALES_MANAGER role)
 *   7. Mark as Paid (requires FINANCE_ADMIN role)
 *
 * This demonstrates how integration tests verify that multiple services
 * collaborate correctly — something unit tests with mocks cannot verify.
 *
 * CONCEPT: Testing with Multiple Roles
 * ----------------------------------------
 * Different steps in the workflow require different roles:
 *   - SALES_REP: Can create deals and view calculations
 *   - SALES_MANAGER: Can approve calculations
 *   - FINANCE_ADMIN: Can process payments
 *
 * We create multiple users with different roles and obtain JWT tokens
 * for each, switching between them as the workflow progresses.
 */
@SpringBootTest(classes = CommissionCalculatorSpringBootApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("SpringBoot Commission Workflow — Integration Tests")
class CommissionWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private CommissionPlanService planService;

    @Autowired
    private DealService dealService;

    private static String repToken;
    private static String managerToken;
    private static String financeToken;
    private static String planId;
    private static String dealId;
    private static String calculationId;
    private static boolean dataSeeded = false;

    @BeforeEach
    void setUp() throws Exception {
        if (!dataSeeded) {
            // Create users with different roles for the workflow
            createTestUser("workflow_rep", "wrep@test.com", "Workflow", "Rep",
                    "wfrep123", Set.of("SALES_REP"));
            createTestUser("workflow_mgr", "wmgr@test.com", "Workflow", "Manager",
                    "wfmgr123", Set.of("SALES_MANAGER"));
            createTestUser("workflow_fin", "wfin@test.com", "Workflow", "Finance",
                    "wffin123", Set.of("FINANCE_ADMIN"));

            repToken = obtainJwtToken("workflow_rep", "wfrep123");
            managerToken = obtainJwtToken("workflow_mgr", "wfmgr123");
            financeToken = obtainJwtToken("workflow_fin", "wffin123");

            // Create a plan and deal via services (setup for the workflow)
            CreatePlanRequest planRequest = new CreatePlanRequest();
            planRequest.setName("Workflow Test Plan");
            planRequest.setCurrencyCode("USD");
            planRequest.setEffectiveStartDate(LocalDate.now().minusMonths(1));
            planRequest.setCreatedBy("workflow_mgr");
            var plan = planService.createPlan(planRequest);
            plan = planService.activatePlan(plan.getId());
            planId = plan.getId();

            CreateDealRequest dealRequest = new CreateDealRequest();
            dealRequest.setTitle("Workflow Test Deal");
            dealRequest.setValue(new BigDecimal("100000"));
            dealRequest.setSalesRepId("workflow_rep");
            var deal = dealService.createDeal(dealRequest);
            deal = dealService.updateDealStatus(deal.getId(),
                    com.chapman.edu.commissions.model.DealStatus.WON);
            dealId = deal.getId();

            dataSeeded = true;
        }
    }

    // ============================================================
    // STEP 1: CALCULATE COMMISSION
    // ============================================================

    @Test
    @Order(1)
    @DisplayName("Step 1: POST /api/calculations should calculate commission")
    void calculateCommission_shouldReturn201() throws Exception {
        CalculateCommissionRequest request = new CalculateCommissionRequest();
        request.setDealId(dealId);
        request.setPlanId(planId);
        request.setCalculatedBy("workflow_mgr");

        MvcResult result = mockMvc.perform(post("/api/calculations")
                        .header("Authorization", "Bearer " + repToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CALCULATED"))
                .andExpect(jsonPath("$.data.baseCommission").isNumber())
                .andReturn();

        calculationId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText();
    }

    // ============================================================
    // STEP 2: VERIFY CALCULATION IS VISIBLE
    // ============================================================

    @Test
    @Order(2)
    @DisplayName("Step 2: GET /api/calculations/{id} should return the calculation")
    void getCalculation_shouldReturnCalculation() throws Exception {
        mockMvc.perform(get("/api/calculations/" + calculationId)
                        .header("Authorization", "Bearer " + repToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(calculationId))
                .andExpect(jsonPath("$.data.dealId").value(dealId))
                .andExpect(jsonPath("$.data.status").value("CALCULATED"));
    }

    // ============================================================
    // STEP 3: SALES_REP CANNOT APPROVE (RBAC)
    // ============================================================

    @Test
    @Order(3)
    @DisplayName("Step 3: SALES_REP should NOT be able to approve a calculation (403)")
    void salesRep_cannotApproveCalculation() throws Exception {
        mockMvc.perform(patch("/api/calculations/" + calculationId + "/approve")
                        .header("Authorization", "Bearer " + repToken))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // STEP 4: SALES_MANAGER APPROVES
    // ============================================================

    @Test
    @Order(4)
    @DisplayName("Step 4: SALES_MANAGER should approve the calculation")
    void salesManager_canApproveCalculation() throws Exception {
        mockMvc.perform(patch("/api/calculations/" + calculationId + "/approve")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    // ============================================================
    // STEP 5: SALES_REP CANNOT PAY (RBAC)
    // ============================================================

    @Test
    @Order(5)
    @DisplayName("Step 5: SALES_REP should NOT be able to mark as paid (403)")
    void salesRep_cannotMarkAsPaid() throws Exception {
        mockMvc.perform(patch("/api/calculations/" + calculationId + "/pay")
                        .header("Authorization", "Bearer " + repToken))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // STEP 6: FINANCE_ADMIN PROCESSES PAYMENT
    // ============================================================

    @Test
    @Order(6)
    @DisplayName("Step 6: FINANCE_ADMIN should mark the calculation as paid")
    void financeAdmin_canMarkAsPaid() throws Exception {
        mockMvc.perform(patch("/api/calculations/" + calculationId + "/pay")
                        .header("Authorization", "Bearer " + financeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"));
    }

    // ============================================================
    // STEP 7: VERIFY FINAL STATE
    // ============================================================

    @Test
    @Order(7)
    @DisplayName("Step 7: Final calculation status should be PAID")
    void finalState_shouldBePaid() throws Exception {
        mockMvc.perform(get("/api/calculations/" + calculationId)
                        .header("Authorization", "Bearer " + repToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"));
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private void createTestUser(String username, String email, String firstName,
                                String lastName, String password, Set<String> roles) {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setPassword(password);
        request.setRoles(roles);
        userService.createUser(request);
    }

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
