package com.chapman.edu.commissions.springboot.controller;

import com.chapman.edu.commissions.model.Deal;
import com.chapman.edu.commissions.model.DealStatus;
import com.chapman.edu.commissions.springboot.config.SecurityConfig;
import com.chapman.edu.commissions.springboot.dto.request.CreateDealRequest;
import com.chapman.edu.commissions.springboot.dto.response.DealResponse;
import com.chapman.edu.commissions.springboot.mapper.DtoMapper;
import com.chapman.edu.commissions.springboot.security.CustomUserDetailsService;
import com.chapman.edu.commissions.springboot.security.JwtAuthenticationFilter;
import com.chapman.edu.commissions.springboot.security.JwtTokenProvider;
import com.chapman.edu.commissions.springboot.service.DealService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvc tests for Spring Boot DealController.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - @WebMvcTest with Spring Security integration
 * - @WithMockUser: Simulates an authenticated user with specified roles
 * - Testing API response wrapper (ApiResponse) structure
 * - Testing request validation (@Valid)
 * - Testing security rules (authenticated vs. unauthenticated access)
 *
 * WHY @Import(SecurityConfig.class)?
 * The springboot SecurityConfig defines JWT-based security with role-based access.
 * @WebMvcTest includes security auto-configuration, so we import the real config
 * and use @WithMockUser to bypass actual JWT authentication.
 */
@WebMvcTest(DealController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@DisplayName("SpringBoot DealController — WebMvc Tests")
class DealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DealService dealService;

    @MockitoBean
    private DtoMapper mapper;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private Deal testDeal;
    private DealResponse testDealResponse;

    @BeforeEach
    void setUp() {
        testDeal = new Deal("Enterprise License", new BigDecimal("100000"), "rep-001");
        testDeal.setId("deal-001");
        testDeal.setStatus(DealStatus.WON);

        testDealResponse = new DealResponse();
        testDealResponse.setId("deal-001");
        testDealResponse.setTitle("Enterprise License");
        testDealResponse.setValue(new BigDecimal("100000"));
        testDealResponse.setStatus("WON");
        testDealResponse.setSalesRepId("rep-001");
    }

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("GET /api/deals should return 200 with deal list")
    void getAllDeals_shouldReturn200_withDealList() throws Exception {
        when(dealService.getAllDeals()).thenReturn(List.of(testDeal));
        when(mapper.toDealResponse(any(Deal.class))).thenReturn(testDealResponse);

        mockMvc.perform(get("/api/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("Enterprise License"));
    }

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("GET /api/deals?status=WON should return filtered results")
    void getAllDeals_withStatusFilter_shouldReturnFiltered() throws Exception {
        when(dealService.getDealsByStatus(DealStatus.WON)).thenReturn(List.of(testDeal));
        when(mapper.toDealResponse(any(Deal.class))).thenReturn(testDealResponse);

        mockMvc.perform(get("/api/deals").param("status", "WON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("WON"));
    }

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("GET /api/deals/{id} should return 200")
    void getDealById_shouldReturn200() throws Exception {
        when(dealService.getDealById("deal-001")).thenReturn(testDeal);
        when(mapper.toDealResponse(testDeal)).thenReturn(testDealResponse);

        mockMvc.perform(get("/api/deals/deal-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("deal-001"))
                .andExpect(jsonPath("$.data.title").value("Enterprise License"));
    }

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("POST /api/deals should return 201 with valid request")
    void createDeal_shouldReturn201_withValidRequest() throws Exception {
        CreateDealRequest request = new CreateDealRequest();
        request.setTitle("New Deal");
        request.setValue(new BigDecimal("50000"));
        request.setSalesRepId("rep-002");

        when(dealService.createDeal(any(CreateDealRequest.class))).thenReturn(testDeal);
        when(mapper.toDealResponse(any(Deal.class))).thenReturn(testDealResponse);

        mockMvc.perform(post("/api/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("POST /api/deals should return 400 with invalid request")
    void createDeal_shouldReturn400_withInvalidRequest() throws Exception {
        // Missing required fields
        CreateDealRequest request = new CreateDealRequest();

        mockMvc.perform(post("/api/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/deals should return 401 without authentication")
    void getAllDeals_shouldReturn401_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/deals"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("DELETE /api/deals/{id} should return 204")
    void deleteDeal_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/deals/deal-001"))
                .andExpect(status().isNoContent());
    }
}
