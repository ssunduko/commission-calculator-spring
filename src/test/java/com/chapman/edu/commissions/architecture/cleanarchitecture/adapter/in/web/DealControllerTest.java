package com.chapman.edu.commissions.architecture.cleanarchitecture.adapter.in.web;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CreateDealCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.DealResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.UpdateDealCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in.DealUseCase;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.EntityNotFoundException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DealStatus;
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
@ContextConfiguration(classes = {DealController.class, SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("Clean Architecture DealController — WebMvc Tests")
class DealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DealUseCase dealUseCase;

    private DealResult testDeal;

    @BeforeEach
    void setUp() {
        testDeal = new DealResult(
                "deal-001", "Enterprise License", new BigDecimal("50000.00"),
                "OPEN", "rep-001", LocalDate.of(2026, 6, 15), LocalDate.of(2026, 3, 1)
        );
    }

    @Test
    @DisplayName("POST /api/clean/deals should return 201 with created deal")
    void createDeal_shouldReturn201() throws Exception {
        CreateDealCommand command = new CreateDealCommand("Enterprise License", new BigDecimal("50000.00"), "rep-001");
        when(dealUseCase.createDeal(any(CreateDealCommand.class))).thenReturn(testDeal);

        mockMvc.perform(post("/api/clean/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("deal-001"))
                .andExpect(jsonPath("$.title").value("Enterprise License"))
                .andExpect(jsonPath("$.value").value(50000.00))
                .andExpect(jsonPath("$.salesRepId").value("rep-001"));
    }

    @Test
    @DisplayName("GET /api/clean/deals should return 200 with all deals")
    void getAllDeals_shouldReturn200() throws Exception {
        when(dealUseCase.getAllDeals()).thenReturn(List.of(testDeal));

        mockMvc.perform(get("/api/clean/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("deal-001"))
                .andExpect(jsonPath("$[0].title").value("Enterprise License"));
    }

    @Test
    @DisplayName("GET /api/clean/deals?salesRepId=rep-001 should filter by sales rep")
    void getDealsBySalesRep_shouldReturn200() throws Exception {
        when(dealUseCase.getDealsBySalesRep("rep-001")).thenReturn(List.of(testDeal));

        mockMvc.perform(get("/api/clean/deals").param("salesRepId", "rep-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].salesRepId").value("rep-001"));
    }

    @Test
    @DisplayName("GET /api/clean/deals?status=OPEN should filter by status")
    void getDealsByStatus_shouldReturn200() throws Exception {
        when(dealUseCase.getDealsByStatus(DealStatus.OPEN)).thenReturn(List.of(testDeal));

        mockMvc.perform(get("/api/clean/deals").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("OPEN"));
    }

    @Test
    @DisplayName("GET /api/clean/deals/{id} should return 200 when deal exists")
    void getDeal_shouldReturn200() throws Exception {
        when(dealUseCase.getDeal("deal-001")).thenReturn(testDeal);

        mockMvc.perform(get("/api/clean/deals/deal-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("deal-001"))
                .andExpect(jsonPath("$.title").value("Enterprise License"));
    }

    @Test
    @DisplayName("GET /api/clean/deals/{id} should return 404 when deal not found")
    void getDeal_shouldReturn404_whenNotFound() throws Exception {
        when(dealUseCase.getDeal("nonexistent"))
                .thenThrow(new EntityNotFoundException("Deal", "nonexistent"));

        mockMvc.perform(get("/api/clean/deals/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/clean/deals/{id} should return 200 with updated deal")
    void updateDeal_shouldReturn200() throws Exception {
        UpdateDealCommand command = new UpdateDealCommand("Updated Title", new BigDecimal("75000.00"), DealStatus.WON, LocalDate.of(2026, 7, 1));
        DealResult updatedDeal = new DealResult(
                "deal-001", "Updated Title", new BigDecimal("75000.00"),
                "WON", "rep-001", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 3, 1)
        );
        when(dealUseCase.updateDeal(eq("deal-001"), any(UpdateDealCommand.class))).thenReturn(updatedDeal);

        mockMvc.perform(put("/api/clean/deals/deal-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.status").value("WON"));
    }

    @Test
    @DisplayName("PUT /api/clean/deals/{id} should return 404 when deal not found")
    void updateDeal_shouldReturn404_whenNotFound() throws Exception {
        UpdateDealCommand command = new UpdateDealCommand("Updated", null, null, null);
        when(dealUseCase.updateDeal(eq("nonexistent"), any(UpdateDealCommand.class)))
                .thenThrow(new EntityNotFoundException("Deal", "nonexistent"));

        mockMvc.perform(put("/api/clean/deals/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /api/clean/deals/{id} should return 204")
    void deleteDeal_shouldReturn204() throws Exception {
        doNothing().when(dealUseCase).deleteDeal("deal-001");

        mockMvc.perform(delete("/api/clean/deals/deal-001"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/clean/deals/{id} should return 404 when deal not found")
    void deleteDeal_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Deal", "nonexistent"))
                .when(dealUseCase).deleteDeal("nonexistent");

        mockMvc.perform(delete("/api/clean/deals/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
