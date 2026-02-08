package com.chapman.edu.commissions.corespring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Demonstrates Spring Profiles for environment-specific configuration.
 *
 * SPRING PROFILES:
 * - Allows conditional bean registration based on active profile
 * - Common profiles: dev, test, staging, prod
 * - Activate via: -Dspring.profiles.active=dev or application.properties
 * - Multiple profiles can be active simultaneously
 *
 * USE CASES:
 * - Different datasources per environment
 * - Enable/disable features per environment
 * - Different logging configurations
 * - Mock services in testing
 */
@Configuration
public class ProfileConfig {

    /**
     * Bean only created when "dev" profile is active
     */
    @Bean
    @Profile("dev")
    public EnvironmentConfig devEnvironment() {
        System.out.println("PROFILE: Creating DEV environment configuration");
        EnvironmentConfig config = new EnvironmentConfig();
        config.setEnvironmentName("Development");
        config.setDebugEnabled(true);
        config.setCacheEnabled(false);
        config.setDatabaseUrl("jdbc:h2:mem:devdb");
        return config;
    }

    /**
     * Bean only created when "prod" profile is active
     */
    @Bean
    @Profile("prod")
    public EnvironmentConfig prodEnvironment() {
        System.out.println("PROFILE: Creating PROD environment configuration");
        EnvironmentConfig config = new EnvironmentConfig();
        config.setEnvironmentName("Production");
        config.setDebugEnabled(false);
        config.setCacheEnabled(true);
        config.setDatabaseUrl("jdbc:postgresql://prod-db:5432/commissions");
        return config;
    }

    /**
     * Bean only created when "test" profile is active
     */
    @Bean
    @Profile("test")
    public EnvironmentConfig testEnvironment() {
        System.out.println("PROFILE: Creating TEST environment configuration");
        EnvironmentConfig config = new EnvironmentConfig();
        config.setEnvironmentName("Testing");
        config.setDebugEnabled(true);
        config.setCacheEnabled(false);
        config.setDatabaseUrl("jdbc:h2:mem:testdb");
        return config;
    }

    /**
     * Default bean - created when no specific profile is active
     * Uses @Profile("default") or when profile-specific beans don't exist
     */
    @Bean
    @Profile("default")
    public EnvironmentConfig defaultEnvironment() {
        System.out.println("PROFILE: Creating DEFAULT environment configuration");
        EnvironmentConfig config = new EnvironmentConfig();
        config.setEnvironmentName("Default");
        config.setDebugEnabled(true);
        config.setCacheEnabled(false);
        config.setDatabaseUrl("jdbc:h2:mem:defaultdb");
        return config;
    }

    /**
     * Configuration properties object
     */
    public static class EnvironmentConfig {
        private String environmentName;
        private boolean debugEnabled;
        private boolean cacheEnabled;
        private String databaseUrl;

        // Getters and Setters
        public String getEnvironmentName() {
            return environmentName;
        }

        public void setEnvironmentName(String environmentName) {
            this.environmentName = environmentName;
        }

        public boolean isDebugEnabled() {
            return debugEnabled;
        }

        public void setDebugEnabled(boolean debugEnabled) {
            this.debugEnabled = debugEnabled;
        }

        public boolean isCacheEnabled() {
            return cacheEnabled;
        }

        public void setCacheEnabled(boolean cacheEnabled) {
            this.cacheEnabled = cacheEnabled;
        }

        public String getDatabaseUrl() {
            return databaseUrl;
        }

        public void setDatabaseUrl(String databaseUrl) {
            this.databaseUrl = databaseUrl;
        }

        @Override
        public String toString() {
            return "EnvironmentConfig{" +
                    "environmentName='" + environmentName + '\'' +
                    ", debugEnabled=" + debugEnabled +
                    ", cacheEnabled=" + cacheEnabled +
                    ", databaseUrl='" + databaseUrl + '\'' +
                    '}';
        }
    }
}
