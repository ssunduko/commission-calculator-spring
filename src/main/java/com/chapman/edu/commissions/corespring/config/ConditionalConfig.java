package com.chapman.edu.commissions.corespring.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Demonstrates Spring Conditional Bean Registration.
 *
 * CONDITIONAL ANNOTATIONS:
 * - @ConditionalOnProperty: Create bean if property has specific value
 * - @ConditionalOnMissingBean: Create bean only if another bean doesn't exist (fallback)
 * - @ConditionalOnClass: Create bean if specific class is on classpath
 * - @ConditionalOnBean: Create bean only if another bean exists
 * - @Conditional: Custom condition logic
 *
 * USE CASES:
 * - Feature flags (enable/disable features via properties)
 * - Auto-configuration (Spring Boot heavily uses these)
 * - Fallback beans (provide default if custom not defined)
 * - Classpath-dependent configuration (different setup based on available libraries)
 */
@Configuration
public class ConditionalConfig {

    /**
     * Bean only created if property "feature.advanced-calculations" = "true"
     * Demonstrates feature toggle pattern
     */
    @Bean
    @ConditionalOnProperty(name = "feature.advanced-calculations", havingValue = "true", matchIfMissing = false)
    public AdvancedCalculationEngine advancedCalculationEngine() {
        System.out.println("CONDITIONAL: Creating AdvancedCalculationEngine (feature enabled)");
        return new AdvancedCalculationEngine();
    }

    /**
     * Fallback bean - only created if AdvancedCalculationEngine bean doesn't exist
     * Provides basic functionality when advanced features are disabled
     */
    @Bean
    @ConditionalOnMissingBean(AdvancedCalculationEngine.class)
    public BasicCalculationEngine basicCalculationEngine() {
        System.out.println("CONDITIONAL: Creating BasicCalculationEngine (fallback - advanced feature disabled)");
        return new BasicCalculationEngine();
    }

    /**
     * Bean created when property exists and has value "enabled"
     */
    @Bean
    @ConditionalOnProperty(name = "feature.caching", havingValue = "enabled")
    public CacheManager cacheManager() {
        System.out.println("CONDITIONAL: Creating CacheManager (caching enabled)");
        return new CacheManager();
    }

    /**
     * Advanced calculation engine (when feature enabled)
     */
    public static class AdvancedCalculationEngine {
        public String calculate() {
            return "Advanced calculation with ML predictions and complex rules";
        }
    }

    /**
     * Basic calculation engine (fallback)
     */
    public static class BasicCalculationEngine {
        public String calculate() {
            return "Basic calculation with simple percentage rules";
        }
    }

    /**
     * Cache manager for optimization
     */
    public static class CacheManager {
        public void enableCache() {
            System.out.println("Cache enabled for calculations");
        }
    }
}
