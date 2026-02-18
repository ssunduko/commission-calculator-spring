package com.chapman.edu.commissions.springboot.controller;

import com.chapman.edu.commissions.model.CommissionCalculation;
import com.chapman.edu.commissions.springboot.config.SecurityConfig;
import com.chapman.edu.commissions.springboot.dto.response.CommissionCalculationResponse;
import com.chapman.edu.commissions.springboot.mapper.DtoMapper;
import com.chapman.edu.commissions.springboot.security.CustomUserDetailsService;
import com.chapman.edu.commissions.springboot.security.JwtAuthenticationFilter;
import com.chapman.edu.commissions.springboot.security.JwtTokenProvider;
import com.chapman.edu.commissions.springboot.service.CommissionCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvc tests for CommissionCalculationController.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - Testing @PreAuthorize method-level security
 * - SALES_MANAGER/FINANCE_ADMIN can approve; FINANCE_ADMIN/SYSTEM_ADMIN can pay
 * - SALES_REP cannot approve or pay
 */
@WebMvcTest(CommissionCalculationController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@DisplayName("SpringBoot CommissionCalculationController — WebMvc Tests")
class CommissionCalculationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommissionCalculationService calculationService;

    @MockitoBean
    private DtoMapper mapper;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private CommissionCalculationResponse testCalcResponse;

    @BeforeEach
    void setUp() {
        testCalcResponse = new CommissionCalculationResponse();
        testCalcResponse.setId("calc-001");
        testCalcResponse.setDealId("deal-001");
        testCalcResponse.setSalesRepId("rep-001");
        testCalcResponse.setBaseCommission(new BigDecimal("10000"));
        testCalcResponse.setGrossCommission(new BigDecimal("10500"));
        testCalcResponse.setStatus("CALCULATED");
    }

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("GET /api/calculations should return 200")
    void getAllCalculations_shouldReturn200() throws Exception {
        CommissionCalculation calc = new CommissionCalculation("deal-001", "rep-001", new BigDecimal("10000"));
        when(calculationService.getAllCalculations()).thenReturn(List.of(calc));
        when(mapper.toCommissionCalculationResponse(any())).thenReturn(testCalcResponse);

        mockMvc.perform(get("/api/calculations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("GET /api/calculations/{id} should return 200")
    void getCalculationById_shouldReturn200() throws Exception {
        CommissionCalculation calc = new CommissionCalculation("deal-001", "rep-001", new BigDecimal("10000"));
        calc.setId("calc-001");
        when(calculationService.getCalculationById("calc-001")).thenReturn(calc);
        when(mapper.toCommissionCalculationResponse(any())).thenReturn(testCalcResponse);

        mockMvc.perform(get("/api/calculations/calc-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("calc-001"));
    }

    @Test
    @WithMockUser(roles = "SALES_MANAGER")
    @DisplayName("PATCH /api/calculations/{id}/approve should return 200 for SALES_MANAGER")
    void approveCalculation_shouldReturn200_forSalesManager() throws Exception {
        CommissionCalculation calc = new CommissionCalculation("deal-001", "rep-001", new BigDecimal("10000"));
        calc.setId("calc-001");
        testCalcResponse.setStatus("APPROVED");

        when(calculationService.approveCalculation("calc-001")).thenReturn(calc);
        when(mapper.toCommissionCalculationResponse(any())).thenReturn(testCalcResponse);

        mockMvc.perform(patch("/api/calculations/calc-001/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("PATCH /api/calculations/{id}/approve should return 403 for SALES_REP")
    void approveCalculation_shouldReturn403_forSalesRep() throws Exception {
        mockMvc.perform(patch("/api/calculations/calc-001/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "FINANCE_ADMIN")
    @DisplayName("PATCH /api/calculations/{id}/pay should return 200 for FINANCE_ADMIN")
    void markAsPaid_shouldReturn200_forFinanceAdmin() throws Exception {
        CommissionCalculation calc = new CommissionCalculation("deal-001", "rep-001", new BigDecimal("10000"));
        calc.setId("calc-001");
        testCalcResponse.setStatus("PAID");

        when(calculationService.markAsPaid("calc-001")).thenReturn(calc);
        when(mapper.toCommissionCalculationResponse(any())).thenReturn(testCalcResponse);

        mockMvc.perform(patch("/api/calculations/calc-001/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"));
    }
}
