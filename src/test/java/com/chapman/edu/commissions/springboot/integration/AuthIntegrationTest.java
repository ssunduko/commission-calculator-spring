package com.chapman.edu.commissions.springboot.integration;

import com.chapman.edu.commissions.springboot.CommissionCalculatorSpringBootApplication;
import com.chapman.edu.commissions.springboot.dto.request.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ============================================================================
 * INTEGRATION TEST — JWT AUTHENTICATION FLOW
 * ============================================================================
 *
 * CONCEPT: @SpringBootTest Integration Testing
 * -----------------------------------------------
 * Unlike @WebMvcTest (which loads ONLY the web layer), @SpringBootTest loads
 * the FULL application context — controllers, services, repositories, security
 * configuration, and all other beans. This allows testing the complete request
 * lifecycle end-to-end.
 *
 * CONCEPT: @AutoConfigureMockMvc
 * --------------------------------
 * Adds MockMvc to the test context so we can send simulated HTTP requests
 * without starting a real HTTP server. Combined with @SpringBootTest, this
 * gives us integration-level testing with the convenience of MockMvc.
 *
 * CONCEPT: @ActiveProfiles("test")
 * -----------------------------------
 * Activates the "test" Spring profile. This excludes beans annotated with
 * @Profile("!test"), such as RestApiProcessor — a CommandLineRunner that
 * makes real HTTP calls to localhost, which would fail since MockMvc does
 * not start a real HTTP server.
 *
 * CONCEPT: Integration Test vs Unit Test
 * -----------------------------------------
 * - Unit test: Tests a single class in isolation with mocked dependencies.
 *   Example: DealServiceTest mocks the DealRepository.
 * - Integration test: Tests multiple layers working together with real beans.
 *   Example: This test goes through Controller -> Service -> Repository -> Security.
 *
 * CONCEPT: Using Pre-Seeded Data
 * ---------------------------------
 * The SampleDataLoader (a CommandLineRunner) seeds the application with
 * sample users, deals, and plans at startup. Integration tests leverage
 * this pre-seeded data rather than creating their own, which:
 *   - Avoids "username already taken" conflicts
 *   - Tests against realistic data volumes
 *   - Mirrors production scenarios where data already exists
 *
 * Pre-seeded users used in this test:
 *   - "admin" / "admin123" (SYSTEM_ADMIN) — can access /api/users
 *   - "agarcia" / "password123" (SALES_REP) — cannot access /api/users
 *
 * This test verifies the complete JWT authentication flow:
 *   1. POST /api/auth/login with credentials → receive JWT token
 *   2. Use JWT token to access protected endpoints → 200 OK
 *   3. Access protected endpoints without JWT → 401 Unauthorized
 */
@SpringBootTest(classes = CommissionCalculatorSpringBootApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("SpringBoot Auth — Integration Tests")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ============================================================
    // SUCCESSFUL AUTHENTICATION
    // ============================================================

    @Test
    @DisplayName("POST /api/auth/login should return JWT token for valid credentials")
    void login_shouldReturnJwtToken_forValidCredentials() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setUsername("admin");
        login.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.roles").isArray());
    }

    // ============================================================
    // FAILED AUTHENTICATION
    // ============================================================

    @Test
    @DisplayName("POST /api/auth/login should return 401 for invalid credentials")
    void login_shouldReturn401_forInvalidCredentials() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setUsername("admin");
        login.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login should return 400 for missing username")
    void login_shouldReturn400_forMissingUsername() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // JWT TOKEN-BASED ACCESS
    // ============================================================

    @Test
    @DisplayName("GET /api/deals should return 200 with valid JWT token")
    void protectedEndpoint_shouldReturn200_withValidJwt() throws Exception {
        String jwt = obtainJwtToken("admin", "admin123");

        mockMvc.perform(get("/api/deals")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/deals should return 401 without JWT token")
    void protectedEndpoint_shouldReturn401_withoutJwt() throws Exception {
        mockMvc.perform(get("/api/deals"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/deals should return 401 with invalid JWT token")
    void protectedEndpoint_shouldReturn401_withInvalidJwt() throws Exception {
        mockMvc.perform(get("/api/deals")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // ROLE-BASED ACCESS CONTROL (RBAC)
    // ============================================================

    @Test
    @DisplayName("GET /api/users should return 200 for SYSTEM_ADMIN")
    void adminEndpoint_shouldReturn200_forSystemAdmin() throws Exception {
        String jwt = obtainJwtToken("admin", "admin123");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/users should return 403 for SALES_REP")
    void adminEndpoint_shouldReturn403_forSalesRep() throws Exception {
        // "agarcia" is a pre-seeded SALES_REP from SampleDataLoader
        String jwt = obtainJwtToken("agarcia", "password123");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/health should return 200 without authentication")
    void publicEndpoint_shouldReturn200_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Helper method that authenticates and extracts the JWT token.
     * This demonstrates how integration tests can chain API calls —
     * the output of one call (login) becomes the input for another.
     */
    private String obtainJwtToken(String username, String password) throws Exception {
        LoginRequest login = new LoginRequest();
        login.setUsername(username);
        login.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).path("data").path("token").asText();
        assertThat(token).isNotBlank();
        return token;
    }
}
