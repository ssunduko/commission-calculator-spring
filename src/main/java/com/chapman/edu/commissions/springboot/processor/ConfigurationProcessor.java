package com.chapman.edu.commissions.springboot.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * ============================================================================
 * PROCESSOR: APPLICATION PROPERTIES, PROFILES & EXTERNALIZED CONFIGURATION
 * ============================================================================
 *
 * This runnable demonstrates:
 *
 *   1. @Value — Injecting property values from application.properties
 *   2. Profiles — dev, prod, and default configurations
 *   3. Environment — Programmatic access to properties
 *   4. Property Resolution — How Spring resolves property values
 *   5. Externalized Configuration — Properties, YAML, env vars, CLI args
 *
 * CONCEPT: Externalized Configuration Sources (Priority Order)
 * ---------------------------------------------------------------
 *   1. Command-line arguments: --server.port=9090
 *   2. JVM System Properties: -Dserver.port=9090
 *   3. OS Environment Variables: SERVER_PORT=9090
 *   4. Profile-specific properties: application-dev.properties
 *   5. Base properties: application.properties
 *   6. @PropertySource annotations on @Configuration classes
 *   7. Default properties via SpringApplication.setDefaultProperties()
 *
 * Higher-priority sources override lower-priority ones. This allows:
 *   - Development defaults in application-dev.properties
 *   - Production overrides via environment variables (no code change)
 *   - Quick testing overrides via command-line arguments
 *
 * CONCEPT: Spring Boot DevTools & Hot Reload
 * ---------------------------------------------
 * DevTools provides automatic application restart when classpath files change:
 *   - Java files → triggers restart (fast: reloads only changed classes)
 *   - Template files → triggers LiveReload (browser auto-refresh)
 *   - Properties files → triggers restart
 *
 * DevTools uses two classloaders for speed:
 *   - Base classloader: third-party JARs (cached, not reloaded)
 *   - Restart classloader: your code (reloaded on each change)
 *
 * To enable: Add spring-boot-devtools dependency and run with "dev" profile.
 */
@Component
@Order(6)
public class ConfigurationProcessor implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationProcessor.class);

    private final Environment environment;

    // ===== CONCEPT: @Value — Property Injection =====
    // Injects values from application.properties (or profile-specific files).
    // The ${property.name:defaultValue} syntax provides a fallback default.

    @Value("${spring.application.name:unknown}")
    private String appName;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${app.jwt.expirationMs:86400000}")
    private long jwtExpiration;

    @Value("${spring.thymeleaf.cache:true}")
    private boolean thymeleafCache;

    public ConfigurationProcessor(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        logger.info("");
        logger.info("╔══════════════════════════════════════════════════════════════╗");
        logger.info("║   APPLICATION PROPERTIES & CONFIGURATION DEMONSTRATION      ║");
        logger.info("╚══════════════════════════════════════════════════════════════╝");

        demonstrateValueInjection();
        demonstrateProfiles();
        demonstrateEnvironment();
        demonstrateDevTools();

        logger.info("");
        logger.info("=== Configuration Demo Complete ===");
        logger.info("");
    }

    /**
     * Demonstrates @Value property injection from application.properties.
     */
    private void demonstrateValueInjection() {
        logger.info("");
        logger.info("--- @Value Property Injection ---");
        logger.info("@Value(\"${{spring.application.name}}\")  → {}", appName);
        logger.info("@Value(\"${{server.port}}\")               → {}", serverPort);
        logger.info("@Value(\"${{app.jwt.expirationMs}}\")      → {} ms ({} hours)",
            jwtExpiration, jwtExpiration / 3600000);
        logger.info("@Value(\"${{spring.thymeleaf.cache}}\")     → {}", thymeleafCache);
    }

    /**
     * Demonstrates Spring profiles and active profile detection.
     */
    private void demonstrateProfiles() {
        logger.info("");
        logger.info("--- Application Profiles ---");

        String[] activeProfiles = environment.getActiveProfiles();
        String[] defaultProfiles = environment.getDefaultProfiles();

        logger.info("Active profiles: {}", Arrays.toString(
            activeProfiles.length > 0 ? activeProfiles : new String[]{"(none)"}));
        logger.info("Default profiles: {}", Arrays.toString(defaultProfiles));
        logger.info("");
        logger.info("Available profile configurations:");
        logger.info("  application.properties          → Base config (always loaded)");
        logger.info("  application-dev.properties      → Development (--spring.profiles.active=dev)");
        logger.info("  application-prod.properties     → Production (--spring.profiles.active=prod)");
        logger.info("");
        logger.info("To activate a profile:");
        logger.info("  CLI:   java -jar app.jar --spring.profiles.active=dev");
        logger.info("  Env:   SPRING_PROFILES_ACTIVE=prod");
        logger.info("  Props: spring.profiles.active=dev");
    }

    /**
     * Demonstrates programmatic access to the Environment.
     */
    private void demonstrateEnvironment() {
        logger.info("");
        logger.info("--- Environment (Programmatic Property Access) ---");
        logger.info("environment.getProperty(\"server.port\")    → {}",
            environment.getProperty("server.port"));
        logger.info("environment.getProperty(\"spring.application.name\") → {}",
            environment.getProperty("spring.application.name"));
        logger.info("environment.getProperty(\"JAVA_HOME\")      → {}",
            environment.getProperty("JAVA_HOME", "(not set)"));
        logger.info("environment.containsProperty(\"server.port\") → {}",
            environment.containsProperty("server.port"));
    }

    /**
     * Describes Spring Boot DevTools functionality.
     */
    private void demonstrateDevTools() {
        logger.info("");
        logger.info("--- Spring Boot DevTools & Hot Reload ---");
        logger.info("DevTools dependency: spring-boot-devtools (scope=runtime, optional=true)");
        logger.info("");
        logger.info("Features:");
        logger.info("  1. Automatic Restart:");
        logger.info("     - Monitors classpath for changes");
        logger.info("     - Uses two classloaders (base + restart) for speed");
        logger.info("     - Only reloads YOUR code, not third-party JARs");
        logger.info("");
        logger.info("  2. LiveReload:");
        logger.info("     - Built-in LiveReload server");
        logger.info("     - Browser auto-refreshes when templates change");
        logger.info("     - Install LiveReload browser extension to use");
        logger.info("");
        logger.info("  3. Property Defaults:");
        logger.info("     - Thymeleaf cache disabled (spring.thymeleaf.cache=false)");
        logger.info("     - Detailed error pages enabled");
        logger.info("     - H2 console enabled");
        logger.info("");
        logger.info("  4. Disabled in Production:");
        logger.info("     - DevTools is automatically disabled when running as JAR");
        logger.info("     - The optional=true in pom.xml prevents production inclusion");
    }
}
