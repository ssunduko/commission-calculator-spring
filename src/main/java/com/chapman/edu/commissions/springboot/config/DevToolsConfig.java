package com.chapman.edu.commissions.springboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * ============================================================================
 * SPRING BOOT DEVTOOLS & HOT RELOAD CONFIGURATION
 * ============================================================================
 *
 * CONCEPT: Spring Boot DevTools
 * -------------------------------
 * Spring Boot DevTools (spring-boot-devtools) provides development-time features
 * that improve the developer experience:
 *
 *   1. AUTOMATIC RESTART
 *      When files on the classpath change, DevTools automatically restarts the
 *      application. It uses two classloaders:
 *        - Base classloader: loads third-party JARs (doesn't restart)
 *        - Restart classloader: loads your project classes (restarts on change)
 *      This makes restarts much faster than a full cold start.
 *
 *   2. LIVERELOAD
 *      DevTools includes an embedded LiveReload server. When resources change
 *      (HTML, CSS, JS), it triggers the browser to refresh automatically.
 *      Install the LiveReload browser extension to use this feature.
 *
 *   3. PROPERTY DEFAULTS
 *      DevTools sets development-friendly defaults:
 *        - Template caching is disabled (see changes immediately)
 *        - Detailed error pages are shown
 *        - H2 console is enabled
 *
 *   4. REMOTE DEBUGGING
 *      DevTools supports remote application development with:
 *        - Remote restart
 *        - Remote update
 *
 * CONFIGURATION IN application-dev.properties:
 *   spring.devtools.restart.enabled=true
 *   spring.devtools.livereload.enabled=true
 *   spring.thymeleaf.cache=false
 *
 * HOW TO USE:
 *   1. Add spring-boot-devtools dependency in pom.xml (scope=runtime, optional=true)
 *   2. Run the application with the "dev" profile: --spring.profiles.active=dev
 *   3. Make changes to Java files → app auto-restarts
 *   4. Make changes to templates → browser auto-refreshes
 *
 * IMPORTANT: DevTools is automatically disabled when running a packaged
 * application (java -jar). The dependency is marked as optional=true so it
 * won't be included in production builds.
 *
 * @see org.springframework.boot.devtools.autoconfigure.DevToolsProperties
 */
@Configuration
@Profile("dev")  // Only active when the "dev" profile is enabled
public class DevToolsConfig {

    // DevTools is configured primarily through application-dev.properties.
    // This class serves as documentation and can hold additional dev-only beans.
    //
    // Examples of beans you might add here:
    //   - Mock external service clients for local development
    //   - Development-only data seeders
    //   - Debugging interceptors or loggers
}
