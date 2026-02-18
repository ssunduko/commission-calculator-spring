package com.chapman.edu.commissions.orm.controller;

import com.chapman.edu.commissions.orm.config.SecurityConfig;
import com.chapman.edu.commissions.orm.entity.Deal;
import com.chapman.edu.commissions.orm.entity.DealStatus;
import com.chapman.edu.commissions.orm.entity.User;
import com.chapman.edu.commissions.orm.service.DealService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvc tests for ORM DealController.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - Testing paginated endpoints with MockMvc
 * - Testing query parameter handling
 * - @Import(SecurityConfig.class) for the ORM security config
 */
@WebMvcTest(DealController.class)
@Import(SecurityConfig.class)
@DisplayName("ORM DealController — WebMvc Tests")
class DealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DealService dealService;

    private Deal testDeal;

    @BeforeEach
    void setUp() {
        User salesRep = new User("jsmith", "john@test.com", "John", "Smith");
        salesRep.setId("usr-001");

        testDeal = new Deal("Test Deal", new BigDecimal("50000"), salesRep);
        testDeal.setId("deal-001");
    }

    @Test
    @DisplayName("GET /api/orm/deals/{id} should return 200 when deal exists")
    void getDealById_shouldReturn200_whenDealExists() throws Exception {
        when(dealService.findById("deal-001")).thenReturn(Optional.of(testDeal));

        mockMvc.perform(get("/api/orm/deals/deal-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Deal"))
                .andExpect(jsonPath("$.id").value("deal-001"));
    }

    @Test
    @DisplayName("GET /api/orm/deals/{id} should return 404 when deal not found")
    void getDealById_shouldReturn404_whenDealNotFound() throws Exception {
        when(dealService.findById("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orm/deals/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/orm/deals/status/{status} should return deals by status")
    void getDealsByStatus_shouldReturn200() throws Exception {
        when(dealService.findByStatus(DealStatus.WON)).thenReturn(List.of(testDeal));

        mockMvc.perform(get("/api/orm/deals/status/WON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Deal"));
    }

    @Test
    @DisplayName("GET /api/orm/deals/search should return paginated results")
    void searchDeals_shouldReturn200_withPaginatedResults() throws Exception {
        Page<Deal> page = new PageImpl<>(List.of(testDeal), PageRequest.of(0, 10), 1);
        when(dealService.searchDeals(any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/orm/deals/search")
                        .param("status", "WON")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Test Deal"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/orm/deals/sales-rep/{id} should return paginated deals")
    void getDealsBySalesRep_shouldReturn200() throws Exception {
        Page<Deal> page = new PageImpl<>(List.of(testDeal), PageRequest.of(0, 10), 1);
        when(dealService.findBySalesRep(eq("usr-001"), any())).thenReturn(page);

        mockMvc.perform(get("/api/orm/deals/sales-rep/usr-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Deal"));
    }

    @Test
    @DisplayName("GET /api/orm/deals/sales-rep/{id}/total-won should return total value")
    void getTotalWonValue_shouldReturn200() throws Exception {
        when(dealService.calculateTotalWonValue("usr-001")).thenReturn(new BigDecimal("117000.00"));

        mockMvc.perform(get("/api/orm/deals/sales-rep/usr-001/total-won"))
                .andExpect(status().isOk())
                .andExpect(content().string("117000.00"));
    }
}
