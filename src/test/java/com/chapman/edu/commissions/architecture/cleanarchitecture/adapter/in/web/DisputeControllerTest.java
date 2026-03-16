package com.chapman.edu.commissions.architecture.cleanarchitecture.adapter.in.web;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CreateDisputeCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.DisputeResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.ResolveDisputeCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in.DisputeUseCase;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.exception.EntityNotFoundException;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DisputeStatus;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes = {DisputeController.class, SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("Clean Architecture DisputeController — WebMvc Tests")
class DisputeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DisputeUseCase disputeUseCase;

    private DisputeResult testDispute;

    @BeforeEach
    void setUp() {
        testDispute = new DisputeResult(
                "disp-001", "calc-001", "rep-001",
                "Incorrect Rate", "The commission rate applied was wrong",
                "INITIATED", false,
                LocalDateTime.of(2026, 3, 10, 9, 0), null, null, 0
        );
    }

    @Test
    @DisplayName("POST /api/clean/disputes should return 201 with created dispute")
    void createDispute_shouldReturn201() throws Exception {
        CreateDisputeCommand command = new CreateDisputeCommand(
                "calc-001", "rep-001", "Incorrect Rate", "The commission rate applied was wrong"
        );
        when(disputeUseCase.createDispute(any(CreateDisputeCommand.class))).thenReturn(testDispute);

        mockMvc.perform(post("/api/clean/disputes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("disp-001"))
                .andExpect(jsonPath("$.calculationId").value("calc-001"))
                .andExpect(jsonPath("$.salesRepId").value("rep-001"))
                .andExpect(jsonPath("$.title").value("Incorrect Rate"))
                .andExpect(jsonPath("$.status").value("INITIATED"))
                .andExpect(jsonPath("$.isEscalated").value(false));
    }

    @Test
    @DisplayName("GET /api/clean/disputes should return 200 with all disputes")
    void getAllDisputes_shouldReturn200() throws Exception {
        when(disputeUseCase.getAllDisputes()).thenReturn(List.of(testDispute));

        mockMvc.perform(get("/api/clean/disputes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("disp-001"))
                .andExpect(jsonPath("$[0].title").value("Incorrect Rate"));
    }

    @Test
    @DisplayName("GET /api/clean/disputes?salesRepId=rep-001 should filter by sales rep")
    void getDisputesBySalesRep_shouldReturn200() throws Exception {
        when(disputeUseCase.getDisputesBySalesRep("rep-001")).thenReturn(List.of(testDispute));

        mockMvc.perform(get("/api/clean/disputes").param("salesRepId", "rep-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].salesRepId").value("rep-001"));
    }

    @Test
    @DisplayName("GET /api/clean/disputes?status=INITIATED should filter by status")
    void getDisputesByStatus_shouldReturn200() throws Exception {
        when(disputeUseCase.getDisputesByStatus(DisputeStatus.INITIATED)).thenReturn(List.of(testDispute));

        mockMvc.perform(get("/api/clean/disputes").param("status", "INITIATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("INITIATED"));
    }

    @Test
    @DisplayName("GET /api/clean/disputes/{id} should return 200 when dispute exists")
    void getDispute_shouldReturn200() throws Exception {
        when(disputeUseCase.getDispute("disp-001")).thenReturn(testDispute);

        mockMvc.perform(get("/api/clean/disputes/disp-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("disp-001"))
                .andExpect(jsonPath("$.title").value("Incorrect Rate"))
                .andExpect(jsonPath("$.description").value("The commission rate applied was wrong"));
    }

    @Test
    @DisplayName("GET /api/clean/disputes/{id} should return 404 when dispute not found")
    void getDispute_shouldReturn404_whenNotFound() throws Exception {
        when(disputeUseCase.getDispute("nonexistent"))
                .thenThrow(new EntityNotFoundException("Dispute", "nonexistent"));

        mockMvc.perform(get("/api/clean/disputes/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/clean/disputes/{id}/resolve should return 200 with resolved dispute")
    void resolveDispute_shouldReturn200() throws Exception {
        ResolveDisputeCommand command = new ResolveDisputeCommand(
                "Rate corrected and recalculated", "admin-001", true
        );
        DisputeResult resolvedDispute = new DisputeResult(
                "disp-001", "calc-001", "rep-001",
                "Incorrect Rate", "The commission rate applied was wrong",
                "RESOLVED", false,
                LocalDateTime.of(2026, 3, 10, 9, 0),
                LocalDateTime.of(2026, 3, 15, 14, 30),
                "Rate corrected and recalculated", 0
        );
        when(disputeUseCase.resolveDispute(eq("disp-001"), any(ResolveDisputeCommand.class)))
                .thenReturn(resolvedDispute);

        mockMvc.perform(post("/api/clean/disputes/disp-001/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolution").value("Rate corrected and recalculated"));
    }

    @Test
    @DisplayName("POST /api/clean/disputes/{id}/escalate should return 200 with escalated dispute")
    void escalateDispute_shouldReturn200() throws Exception {
        DisputeResult escalatedDispute = new DisputeResult(
                "disp-001", "calc-001", "rep-001",
                "Incorrect Rate", "The commission rate applied was wrong",
                "ESCALATED", true,
                LocalDateTime.of(2026, 3, 10, 9, 0), null, null, 0
        );
        when(disputeUseCase.escalateDispute("disp-001")).thenReturn(escalatedDispute);

        mockMvc.perform(post("/api/clean/disputes/disp-001/escalate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ESCALATED"))
                .andExpect(jsonPath("$.isEscalated").value(true));
    }

    @Test
    @DisplayName("POST /api/clean/disputes/{id}/escalate should return 404 when not found")
    void escalateDispute_shouldReturn404_whenNotFound() throws Exception {
        when(disputeUseCase.escalateDispute("nonexistent"))
                .thenThrow(new EntityNotFoundException("Dispute", "nonexistent"));

        mockMvc.perform(post("/api/clean/disputes/nonexistent/escalate"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /api/clean/disputes/{id} should return 204")
    void deleteDispute_shouldReturn204() throws Exception {
        doNothing().when(disputeUseCase).deleteDispute("disp-001");

        mockMvc.perform(delete("/api/clean/disputes/disp-001"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/clean/disputes/{id} should return 404 when dispute not found")
    void deleteDispute_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Dispute", "nonexistent"))
                .when(disputeUseCase).deleteDispute("nonexistent");

        mockMvc.perform(delete("/api/clean/disputes/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
