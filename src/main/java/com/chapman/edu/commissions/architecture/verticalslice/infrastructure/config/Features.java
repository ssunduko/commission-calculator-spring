package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.config;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feature flags for the Vertical Slice application.
 *
 * ACTIVATION STRATEGIES (configured at runtime via /togglz-console):
 *
 *   Kill Switch:     No strategy — just enable/disable globally
 *   Gradual Rollout: GradualActivationStrategy — percentage-based with consistent hashing
 *   User Targeting:  UsernameActivationStrategy — enable for specific users
 *   Time Window:     ReleaseDateActivationStrategy — enable after a specific date
 *   Region:          RegionActivationStrategy (custom) — enable per server/region via env var or header
 */
public enum Features implements Feature {

    @EnabledByDefault
    @Label("Currency Conversion Feature")
    CURRENCY_CONVERSION,

    @Label("Beta Dashboard Feature")
    BETA_DASHBOARD,

    @Label("Advanced Commission Analytics")
    ADVANCED_ANALYTICS,

    @Label("Bulk Deal Import")
    BULK_IMPORT;

    @Configuration
    static class TogglzConfig {
        @Bean
        public UserProvider togglzUserProvider() {
            return () -> new SimpleFeatureUser("admin", true);
        }
    }
}
