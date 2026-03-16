package com.chapman.edu.commissions.architecture.cleanarchitecture.adapter.in.web;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.AddRuleCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CreatePlanCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.PlanResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in.CommissionPlanUseCase;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.EntityNotFoundException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.PlanStatus;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.RuleType;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes = {CommissionPlanController.class, SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("Clean Architecture CommissionPlanController — WebMvc Tests")
class CommissionPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommissionPlanUseCase commissionPlanUseCase;

    private PlanResult testPlan;

    @BeforeEach
    void setUp() {
        testPlan = new PlanResult(
                "plan-001", "Q1 Sales Plan", "USD", "DRAFT",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31),
                LocalDate.of(2026, 1, 1), 2, 3
        );
    }

    @Test
    @DisplayName("POST /api/clean/plans should return 201 with created plan")
    void createPlan_shouldReturn201() throws Exception {
        CreatePlanCommand command = new CreatePlanCommand(
                "Q1 Sales Plan", "USD", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31)
        );
        when(commissionPlanUseCase.createPlan(any(CreatePlanCommand.class))).thenReturn(testPlan);

        mockMvc.perform(post("/api/clean/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("plan-001"))
                .andExpect(jsonPath("$.name").value("Q1 Sales Plan"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @DisplayName("GET /api/clean/plans should return 200 with all plans")
    void getAllPlans_shouldReturn200() throws Exception {
        when(commissionPlanUseCase.getAllPlans()).thenReturn(List.of(testPlan));

        mockMvc.perform(get("/api/clean/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("plan-001"))
                .andExpect(jsonPath("$[0].name").value("Q1 Sales Plan"));
    }

    @Test
    @DisplayName("GET /api/clean/plans?status=DRAFT should filter by status")
    void getPlansByStatus_shouldReturn200() throws Exception {
        when(commissionPlanUseCase.getPlansByStatus(PlanStatus.DRAFT)).thenReturn(List.of(testPlan));

        mockMvc.perform(get("/api/clean/plans").param("status", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("DRAFT"));
    }

    @Test
    @DisplayName("GET /api/clean/plans/{id} should return 200 when plan exists")
    void getPlan_shouldReturn200() throws Exception {
        when(commissionPlanUseCase.getPlan("plan-001")).thenReturn(testPlan);

        mockMvc.perform(get("/api/clean/plans/plan-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("plan-001"))
                .andExpect(jsonPath("$.name").value("Q1 Sales Plan"))
                .andExpect(jsonPath("$.rulesCount").value(2))
                .andExpect(jsonPath("$.tiersCount").value(3));
    }

    @Test
    @DisplayName("GET /api/clean/plans/{id} should return 404 when plan not found")
    void getPlan_shouldReturn404_whenNotFound() throws Exception {
        when(commissionPlanUseCase.getPlan("nonexistent"))
                .thenThrow(new EntityNotFoundException("CommissionPlan", "nonexistent"));

        mockMvc.perform(get("/api/clean/plans/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/clean/plans/{id}/activate should return 200 with activated plan")
    void activatePlan_shouldReturn200() throws Exception {
        PlanResult activatedPlan = new PlanResult(
                "plan-001", "Q1 Sales Plan", "USD", "ACTIVE",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31),
                LocalDate.of(2026, 1, 1), 2, 3
        );
        when(commissionPlanUseCase.activatePlan("plan-001")).thenReturn(activatedPlan);

        mockMvc.perform(post("/api/clean/plans/plan-001/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/clean/plans/{id}/rules should return 200 with updated plan")
    void addRuleToPlan_shouldReturn200() throws Exception {
        AddRuleCommand command = new AddRuleCommand(
                "Base Rate", "Standard base commission rate",
                new BigDecimal("0.10"), RuleType.STANDARD, 1
        );
        PlanResult updatedPlan = new PlanResult(
                "plan-001", "Q1 Sales Plan", "USD", "DRAFT",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31),
                LocalDate.of(2026, 1, 1), 3, 3
        );
        when(commissionPlanUseCase.addRuleToPlan(eq("plan-001"), any(AddRuleCommand.class))).thenReturn(updatedPlan);

        mockMvc.perform(post("/api/clean/plans/plan-001/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rulesCount").value(3));
    }

    @Test
    @DisplayName("DELETE /api/clean/plans/{id} should return 204")
    void deletePlan_shouldReturn204() throws Exception {
        doNothing().when(commissionPlanUseCase).deletePlan("plan-001");

        mockMvc.perform(delete("/api/clean/plans/plan-001"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/clean/plans/{id} should return 404 when plan not found")
    void deletePlan_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new EntityNotFoundException("CommissionPlan", "nonexistent"))
                .when(commissionPlanUseCase).deletePlan("nonexistent");

        mockMvc.perform(delete("/api/clean/plans/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
