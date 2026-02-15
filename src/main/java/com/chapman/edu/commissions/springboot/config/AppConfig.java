package com.chapman.edu.commissions.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

/**
 * ============================================================================
 * APPLICATION CONFIGURATION — BEAN DEFINITIONS & IoC CONTAINER
 * ============================================================================
 *
 * CONCEPT: Dependency Injection (DI) & Inversion of Control (IoC)
 * ----------------------------------------------------------------
 * Inversion of Control means the framework (Spring) controls the creation and
 * lifecycle of objects, rather than the developer manually using "new".
 *
 * Dependency Injection is the mechanism by which Spring provides (injects)
 * dependencies into objects. There are three types:
 *   1. Constructor Injection (PREFERRED) — dependencies passed via constructor
 *   2. Setter Injection — dependencies set via setter methods
 *   3. Field Injection (@Autowired on fields) — convenient but harder to test
 *
 * CONCEPT: @Configuration
 * ------------------------
 * The @Configuration annotation marks this class as a source of bean definitions.
 * Spring processes @Configuration classes at startup and registers the @Bean
 * methods as bean definitions in the ApplicationContext (the IoC Container).
 *
 * The ApplicationContext is Spring's IoC Container. It:
 *   - Creates and manages bean instances (singletons by default)
 *   - Resolves and injects dependencies between beans
 *   - Manages bean lifecycle (initialization, destruction)
 *   - Provides environment abstraction (properties, profiles)
 *
 * CONCEPT: @Bean
 * ---------------
 * The @Bean annotation on a method tells Spring that the return value should be
 * registered as a bean in the ApplicationContext. Other beans can then have this
 * dependency injected via @Autowired.
 *
 * CONCEPT: Component Scanning
 * ----------------------------
 * Spring automatically discovers beans through component scanning. Annotations
 * that mark a class as a Spring-managed bean:
 *   - @Component   — Generic Spring bean
 *   - @Service     — Business logic layer (semantic marker over @Component)
 *   - @Repository  — Data access layer (adds exception translation)
 *   - @Controller  — Web MVC controller (returns views)
 *   - @RestController — REST API controller (returns JSON/XML directly)
 *
 * All these are specializations of @Component. Spring's component scanner finds
 * them in packages specified by @ComponentScan (or @SpringBootApplication).
 *
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Bean
 */
@Configuration
public class AppConfig {

    /**
     * Registers a RestTemplate bean for making HTTP calls.
     *
     * CONCEPT: @Bean Method
     * ----------------------
     * This method is called by Spring during startup. The returned object is
     * stored in the ApplicationContext and can be injected into any other bean
     * using @Autowired or constructor injection.
     *
     * Example of injection in another class:
     *   @Service
     *   public class MyService {
     *       private final RestTemplate restTemplate;
     *
     *       // Constructor injection — Spring injects the RestTemplate bean
     *       public MyService(RestTemplate restTemplate) {
     *           this.restTemplate = restTemplate;
     *       }
     *   }
     *
     * @return a new RestTemplate instance managed by Spring
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
