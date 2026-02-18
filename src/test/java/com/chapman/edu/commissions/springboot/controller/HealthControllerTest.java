package com.chapman.edu.commissions.springboot.controller;

import com.chapman.edu.commissions.springboot.config.SecurityConfig;
import com.chapman.edu.commissions.springboot.security.CustomUserDetailsService;
import com.chapman.edu.commissions.springboot.security.JwtAuthenticationFilter;
import com.chapman.edu.commissions.springboot.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvc test for HealthController.
 *
 * KEY CONCEPTS DEMONSTRATED:
 * - Testing public endpoints (no authentication required)
 * - The /api/health endpoint is explicitly permitted in SecurityConfig
 * - No @WithMockUser needed since this endpoint is public
 */
@WebMvcTest(HealthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@DisplayName("SpringBoot HealthController — WebMvc Tests")
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/health should return 200 without authentication")
    void healthCheck_shouldReturn200_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.application").exists());
    }
}
