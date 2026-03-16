package com.chapman.edu.commissions.architecture.cleanarchitecture.adapter.in.web;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CalculateCommissionCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CalculationResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in.CommissionCalculationUseCase;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.EntityNotFoundException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.infrastructure.config.SecurityConfig;
import com.chapman.edu.commissions.architecture.cleanarchitecture.infrastructure.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes = {CommissionCalculationController.class, SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("Clean Architecture CommissionCalculationController — WebMvc Tests")
class CommissionCalculationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommissionCalculationUseCase commissionCalculationUseCase;

    private CalculationResult testCalculation;

    @BeforeEach
    void setUp() {
        testCalculation = new CalculationResult(
                "calc-001", "deal-001", "rep-001",
                new BigDecimal("5000.00"), new BigDecimal("5500.00"), new BigDecimal("4950.00"),
                "COMPLETED", LocalDate.of(2026, 3, 15), "plan-001"
        );
    }

    @Test
    @DisplayName("POST /api/clean/calculations should return 201 with calculation result")
    void calculateCommission_shouldReturn201() throws Exception {
        CalculateCommissionCommand command = new CalculateCommissionCommand("deal-001", "plan-001");
        when(commissionCalculationUseCase.calculateCommission(any(CalculateCommissionCommand.class)))
                .thenReturn(testCalculation);

        mockMvc.perform(post("/api/clean/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("calc-001"))
                .andExpect(jsonPath("$.dealId").value("deal-001"))
                .andExpect(jsonPath("$.salesRepId").value("rep-001"))
                .andExpect(jsonPath("$.baseCommission").value(5000.00))
                .andExpect(jsonPath("$.grossCommission").value(5500.00))
                .andExpect(jsonPath("$.netCommission").value(4950.00))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.planId").value("plan-001"));
    }

    @Test
    @DisplayName("GET /api/clean/calculations should return 200 with all calculations")
    void getAllCalculations_shouldReturn200() throws Exception {
        when(commissionCalculationUseCase.getAllCalculations()).thenReturn(List.of(testCalculation));

        mockMvc.perform(get("/api/clean/calculations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("calc-001"));
    }

    @Test
    @DisplayName("GET /api/clean/calculations?dealId=deal-001 should filter by deal")
    void getCalculationsByDeal_shouldReturn200() throws Exception {
        when(commissionCalculationUseCase.getCalculationsByDeal("deal-001"))
                .thenReturn(List.of(testCalculation));

        mockMvc.perform(get("/api/clean/calculations").param("dealId", "deal-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].dealId").value("deal-001"));
    }

    @Test
    @DisplayName("GET /api/clean/calculations?salesRepId=rep-001 should filter by sales rep")
    void getCalculationsBySalesRep_shouldReturn200() throws Exception {
        when(commissionCalculationUseCase.getCalculationsBySalesRep("rep-001"))
                .thenReturn(List.of(testCalculation));

        mockMvc.perform(get("/api/clean/calculations").param("salesRepId", "rep-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].salesRepId").value("rep-001"));
    }

    @Test
    @DisplayName("GET /api/clean/calculations/{id} should return 200 when calculation exists")
    void getCalculation_shouldReturn200() throws Exception {
        when(commissionCalculationUseCase.getCalculation("calc-001")).thenReturn(testCalculation);

        mockMvc.perform(get("/api/clean/calculations/calc-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("calc-001"))
                .andExpect(jsonPath("$.baseCommission").value(5000.00))
                .andExpect(jsonPath("$.netCommission").value(4950.00));
    }

    @Test
    @DisplayName("GET /api/clean/calculations/{id} should return 404 when not found")
    void getCalculation_shouldReturn404_whenNotFound() throws Exception {
        when(commissionCalculationUseCase.getCalculation("nonexistent"))
                .thenThrow(new EntityNotFoundException("CommissionCalculation", "nonexistent"));

        mockMvc.perform(get("/api/clean/calculations/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/clean/calculations should return 404 when deal not found")
    void calculateCommission_shouldReturn404_whenDealNotFound() throws Exception {
        CalculateCommissionCommand command = new CalculateCommissionCommand("bad-deal", "plan-001");
        when(commissionCalculationUseCase.calculateCommission(any(CalculateCommissionCommand.class)))
                .thenThrow(new EntityNotFoundException("Deal", "bad-deal"));

        mockMvc.perform(post("/api/clean/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
