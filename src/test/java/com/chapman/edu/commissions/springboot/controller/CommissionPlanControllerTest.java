package com.chapman.edu.commissions.springboot.controller;

import com.chapman.edu.commissions.model.CommissionPlan;
import com.chapman.edu.commissions.model.PlanStatus;
import com.chapman.edu.commissions.springboot.config.SecurityConfig;
import com.chapman.edu.commissions.springboot.dto.response.CommissionPlanResponse;
import com.chapman.edu.commissions.springboot.mapper.DtoMapper;
import com.chapman.edu.commissions.springboot.security.CustomUserDetailsService;
import com.chapman.edu.commissions.springboot.security.JwtAuthenticationFilter;
import com.chapman.edu.commissions.springboot.security.JwtTokenProvider;
import com.chapman.edu.commissions.springboot.service.CommissionPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Currency;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvc tests for CommissionPlanController.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - Testing REST endpoints that return ApiResponse-wrapped data
 * - Mocking service + mapper layers
 */
@WebMvcTest(CommissionPlanController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@DisplayName("SpringBoot CommissionPlanController — WebMvc Tests")
class CommissionPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommissionPlanService planService;

    @MockitoBean
    private DtoMapper mapper;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private CommissionPlanResponse testPlanResponse;

    @BeforeEach
    void setUp() {
        testPlanResponse = new CommissionPlanResponse();
        testPlanResponse.setId("plan-001");
        testPlanResponse.setName("Standard Plan");
        testPlanResponse.setCurrency("USD");
        testPlanResponse.setStatus("ACTIVE");
    }

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("GET /api/plans should return 200 with plan list")
    void getAllPlans_shouldReturn200() throws Exception {
        CommissionPlan plan = new CommissionPlan("Standard Plan", Currency.getInstance("USD"));
        plan.setId("plan-001");

        when(planService.getAllPlans()).thenReturn(List.of(plan));
        when(mapper.toCommissionPlanResponse(any())).thenReturn(testPlanResponse);

        mockMvc.perform(get("/api/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Standard Plan"));
    }

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("GET /api/plans/{id} should return 200")
    void getPlanById_shouldReturn200() throws Exception {
        CommissionPlan plan = new CommissionPlan("Standard Plan", Currency.getInstance("USD"));
        plan.setId("plan-001");

        when(planService.getPlanById("plan-001")).thenReturn(plan);
        when(mapper.toCommissionPlanResponse(any())).thenReturn(testPlanResponse);

        mockMvc.perform(get("/api/plans/plan-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("plan-001"));
    }

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("GET /api/plans/active should return 200 with active plans")
    void getActivePlans_shouldReturn200() throws Exception {
        CommissionPlan plan = new CommissionPlan("Active Plan", Currency.getInstance("USD"));
        plan.setId("plan-001");

        when(planService.getActivePlans()).thenReturn(List.of(plan));
        when(mapper.toCommissionPlanResponse(any())).thenReturn(testPlanResponse);

        mockMvc.perform(get("/api/plans/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
