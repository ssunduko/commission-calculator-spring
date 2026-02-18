package com.chapman.edu.commissions.ai.controller;

import com.chapman.edu.commissions.ai.service.ml.AnomalyDetectionService;
import com.chapman.edu.commissions.ai.service.ml.CommissionExplainerService;
import com.chapman.edu.commissions.ai.service.ml.DisputeAnalysisService;
import com.chapman.edu.commissions.ai.service.ml.ForecastingService;
import com.chapman.edu.commissions.ai.service.rag.CommissionRagService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommissionController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CommissionController — Integration Tests")
class CommissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommissionRagService ragService;

    @MockitoBean
    private CommissionExplainerService explainerService;

    @MockitoBean
    private DisputeAnalysisService disputeAnalysisService;

    @MockitoBean
    private ForecastingService forecastingService;

    @MockitoBean
    private AnomalyDetectionService anomalyDetectionService;

    // ============================================================
    // RAG Endpoints
    // ============================================================

    @Nested
    @DisplayName("RAG Endpoints")
    class RagEndpoints {

        @Test
        @DisplayName("POST /api/ai/rag/ask should return AI answer for valid question")
        void askQuestion_shouldReturnAnswer() throws Exception {
            when(ragService.answerQuestion("What plans exist?"))
                    .thenReturn("The Standard Sales Plan is available.");

            mockMvc.perform(post("/api/ai/rag/ask")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"question\": \"What plans exist?\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.question").value("What plans exist?"))
                    .andExpect(jsonPath("$.response").value("The Standard Sales Plan is available."));
        }

        @Test
        @DisplayName("POST /api/ai/rag/ask should return 400 when question is missing")
        void askQuestion_shouldReturn400WhenQuestionMissing() throws Exception {
            mockMvc.perform(post("/api/ai/rag/ask")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"notAQuestion\": \"value\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Question is required"));

            verifyNoInteractions(ragService);
        }

        @Test
        @DisplayName("POST /api/ai/rag/ask should return 400 when question is blank")
        void askQuestion_shouldReturn400WhenQuestionBlank() throws Exception {
            mockMvc.perform(post("/api/ai/rag/ask")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"question\": \"   \"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Question is required"));
        }

        @Test
        @DisplayName("POST /api/ai/rag/ask/{type} should return filtered AI answer")
        void askTypedQuestion_shouldReturnFilteredAnswer() throws Exception {
            when(ragService.answerTypedQuestion("What deals are open?", "deal"))
                    .thenReturn("There are 3 open deals in the pipeline.");

            mockMvc.perform(post("/api/ai/rag/ask/deal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"question\": \"What deals are open?\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type").value("deal"))
                    .andExpect(jsonPath("$.response").value("There are 3 open deals in the pipeline."));
        }

        @Test
        @DisplayName("POST /api/ai/rag/ask/{type} should return 400 when question is missing")
        void askTypedQuestion_shouldReturn400WhenQuestionMissing() throws Exception {
            mockMvc.perform(post("/api/ai/rag/ask/deal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"other\": \"value\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Question is required"));
        }

        @Test
        @DisplayName("GET /api/ai/rag/report/{salesRepName} should return performance report")
        void generateReport_shouldReturnReport() throws Exception {
            when(ragService.generatePerformanceReport("Alice"))
                    .thenReturn("Alice is a top performer with $57,300 in commissions.");

            mockMvc.perform(get("/api/ai/rag/report/Alice"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.salesRep").value("Alice"))
                    .andExpect(jsonPath("$.report").value("Alice is a top performer with $57,300 in commissions."));
        }
    }

    // ============================================================
    // Explanation Endpoints
    // ============================================================

    @Nested
    @DisplayName("Explanation Endpoints")
    class ExplanationEndpoints {

        @Test
        @DisplayName("GET /api/ai/explain/calculation/{id} should return explanation")
        void explainCalculation_shouldReturnExplanation() throws Exception {
            when(explainerService.explainCalculation("calc-001"))
                    .thenReturn("The $18,000 commission was calculated at 12% of the $150,000 deal.");

            mockMvc.perform(get("/api/ai/explain/calculation/calc-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.calculationId").value("calc-001"))
                    .andExpect(jsonPath("$.explanation").exists());
        }

        @Test
        @DisplayName("GET /api/ai/explain/plan/{id} should return plan explanation")
        void explainPlan_shouldReturnExplanation() throws Exception {
            when(explainerService.explainPlan("plan-001"))
                    .thenReturn("The Standard Plan has 4 tiers ranging from 5% to 15%.");

            mockMvc.perform(get("/api/ai/explain/plan/plan-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.planId").value("plan-001"))
                    .andExpect(jsonPath("$.explanation").value("The Standard Plan has 4 tiers ranging from 5% to 15%."));
        }
    }

    // ============================================================
    // Dispute Analysis Endpoints
    // ============================================================

    @Nested
    @DisplayName("Dispute Analysis Endpoints")
    class DisputeEndpoints {

        @Test
        @DisplayName("GET /api/ai/disputes/analyze/{id} should return dispute analysis")
        void analyzeDispute_shouldReturnAnalysis() throws Exception {
            when(disputeAnalysisService.analyzeDispute("disp-001"))
                    .thenReturn("The dispute has merit. The Enterprise tier rate should have been applied.");

            mockMvc.perform(get("/api/ai/disputes/analyze/disp-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.disputeId").value("disp-001"))
                    .andExpect(jsonPath("$.analysis").exists());
        }

        @Test
        @DisplayName("GET /api/ai/disputes/triage/{id} should return triage result")
        void triageDispute_shouldReturnTriage() throws Exception {
            when(disputeAnalysisService.triageDispute("disp-001"))
                    .thenReturn("PRIORITY: HIGH - Significant amount at stake");

            mockMvc.perform(get("/api/ai/disputes/triage/disp-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.disputeId").value("disp-001"))
                    .andExpect(jsonPath("$.triage").value("PRIORITY: HIGH - Significant amount at stake"));
        }
    }

    // ============================================================
    // Forecasting Endpoints
    // ============================================================

    @Nested
    @DisplayName("Forecasting Endpoints")
    class ForecastingEndpoints {

        @Test
        @DisplayName("GET /api/ai/forecast/user/{id} should return user forecast")
        void forecastUser_shouldReturnForecast() throws Exception {
            when(forecastingService.forecastCommissions("user-001"))
                    .thenReturn("Projected Q2 commission: $45,000 based on historical trends.");

            mockMvc.perform(get("/api/ai/forecast/user/user-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value("user-001"))
                    .andExpect(jsonPath("$.forecast").exists());
        }

        @Test
        @DisplayName("GET /api/ai/forecast/team should return team forecast")
        void forecastTeam_shouldReturnForecast() throws Exception {
            when(forecastingService.forecastTeamCommissions())
                    .thenReturn("Team projected total: $120,000 for next quarter.");

            mockMvc.perform(get("/api/ai/forecast/team"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.forecast").value("Team projected total: $120,000 for next quarter."));
        }
    }

    // ============================================================
    // Anomaly Detection Endpoints
    // ============================================================

    @Nested
    @DisplayName("Anomaly Detection Endpoints")
    class AnomalyEndpoints {

        @Test
        @DisplayName("GET /api/ai/anomaly/detect should return anomaly analysis")
        void detectAnomalies_shouldReturnAnalysis() throws Exception {
            when(anomalyDetectionService.detectAnomalies())
                    .thenReturn("1 anomaly detected: calc-003 with $37,500 exceeds 2σ threshold.");

            mockMvc.perform(get("/api/ai/anomaly/detect"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.analysis").exists());
        }

        @Test
        @DisplayName("GET /api/ai/anomaly/check/{id} should return single calculation check")
        void checkAnomaly_shouldReturnResult() throws Exception {
            when(anomalyDetectionService.checkSingleCalculation("calc-001"))
                    .thenReturn("NORMAL - Within expected range.");

            mockMvc.perform(get("/api/ai/anomaly/check/calc-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.calculationId").value("calc-001"))
                    .andExpect(jsonPath("$.result").value("NORMAL - Within expected range."));
        }
    }
}
