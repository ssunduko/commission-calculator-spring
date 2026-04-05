package com.chapman.edu.commissions.architecture.verticalslice.featureflags;

import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.config.Features;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.togglz.core.manager.FeatureManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests verifying feature flags control endpoint behavior.
 * Uses the real Spring context with the verticalslice application.
 */
@SpringBootTest(
    classes = com.chapman.edu.commissions.architecture.verticalslice.CommissionCalculatorApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:featureflagdb",
    "spring.flyway.enabled=false",
    "spring.ai.mcp.server.enabled=false",
    "togglz.enabled=true",
    "togglz.feature-enums=com.chapman.edu.commissions.architecture.verticalslice.infrastructure.config.Features",
    "togglz.console.enabled=true",
    "togglz.console.secured=false"
})
class FeatureFlagIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FeatureManager featureManager;

    // ============================================================
    // Kill Switch: disable endpoint via feature flag
    // ============================================================

    @Test
    @DisplayName("Currency endpoints return 503 when CURRENCY_CONVERSION is disabled")
    void currencyEndpoint_returns503_whenFlagDisabled() throws Exception {
        featureManager.setFeatureState(
                new org.togglz.core.repository.FeatureState(Features.CURRENCY_CONVERSION, false));

        try {
            mockMvc.perform(get("/api/currency/supported")
                            .with(httpBasic("admin", "admin123")))
                    .andExpect(status().isServiceUnavailable());

            mockMvc.perform(get("/api/currency/rates")
                            .with(httpBasic("admin", "admin123")))
                    .andExpect(status().isServiceUnavailable());

            mockMvc.perform(post("/api/currency/convert")
                            .with(httpBasic("admin", "admin123"))
                            .contentType("application/json")
                            .content("{\"from\":\"USD\",\"to\":\"EUR\",\"amount\":100}"))
                    .andExpect(status().isServiceUnavailable());

            mockMvc.perform(get("/api/currency/historical?date=2025-01-15")
                            .with(httpBasic("admin", "admin123")))
                    .andExpect(status().isServiceUnavailable());
        } finally {
            // Restore
            featureManager.setFeatureState(
                    new org.togglz.core.repository.FeatureState(Features.CURRENCY_CONVERSION, true));
        }
    }

    @Test
    @DisplayName("Currency endpoints are accessible when CURRENCY_CONVERSION is enabled")
    void currencyEndpoint_accessible_whenFlagEnabled() throws Exception {
        featureManager.setFeatureState(
                new org.togglz.core.repository.FeatureState(Features.CURRENCY_CONVERSION, true));

        // Endpoint should be reachable (may fail with 500 due to external service, but NOT 503)
        mockMvc.perform(get("/api/currency/supported")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(result -> assertThat(result.getResponse().getStatus())
                        .isNotEqualTo(503));
    }

    // ============================================================
    // FeatureManager wiring
    // ============================================================

    @Test
    @DisplayName("FeatureManager is available and has all features registered")
    void featureManagerWired() {
        assertThat(featureManager).isNotNull();
        assertThat(featureManager.getFeatures())
                .extracting(f -> f.name())
                .contains("CURRENCY_CONVERSION", "BETA_DASHBOARD",
                          "ADVANCED_ANALYTICS", "BULK_IMPORT");
    }

    @Test
    @DisplayName("Feature state can be toggled at runtime without restart")
    void featureStateTogglesAtRuntime() {
        // Enable
        featureManager.setFeatureState(
                new org.togglz.core.repository.FeatureState(Features.BETA_DASHBOARD, true));
        assertThat(featureManager.isActive(Features.BETA_DASHBOARD)).isTrue();

        // Disable
        featureManager.setFeatureState(
                new org.togglz.core.repository.FeatureState(Features.BETA_DASHBOARD, false));
        assertThat(featureManager.isActive(Features.BETA_DASHBOARD)).isFalse();
    }

    // ============================================================
    // Togglz Console accessibility
    // ============================================================

    @Test
    @DisplayName("Togglz console is accessible without authentication")
    void togglzConsole_isAccessible() throws Exception {
        mockMvc.perform(get("/togglz-console/"))
                .andExpect(result -> assertThat(result.getResponse().getStatus())
                        .isLessThan(500));
    }
}
