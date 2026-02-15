package com.chapman.edu.commissions.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * ============================================================================
 * SPRING BOOT APPLICATION — MAIN ENTRY POINT
 * ============================================================================
 *
 * CONCEPT: Spring Boot Auto-Configuration
 * ----------------------------------------
 * The @SpringBootApplication annotation is a convenience annotation that combines:
 *
 *   1. @Configuration    — Marks this class as a source of bean definitions
 *   2. @EnableAutoConfiguration — Tells Spring Boot to automatically configure
 *      beans based on the classpath, other beans, and property settings.
 *      For example, if spring-boot-starter-web is on the classpath, Spring Boot
 *      automatically configures Tomcat and Spring MVC.
 *   3. @ComponentScan    — Tells Spring to scan for @Component, @Service,
 *      @Repository, @Controller classes in this package and its sub-packages.
 *
 * CONCEPT: Spring Initializr & Project Structure
 * -----------------------------------------------
 * This project was scaffolded using Spring Initializr (https://start.spring.io),
 * which generates a standard Maven/Gradle project with:
 *   - pom.xml / build.gradle with selected starter dependencies
 *   - This main application class
 *   - application.properties for configuration
 *   - Standard directory structure (src/main/java, src/main/resources, src/test)
 *
 * CONCEPT: Starter Dependencies
 * ------------------------------
 * Spring Boot "starters" are curated dependency descriptors. Including a single
 * starter pulls in all related libraries you need. For this project we use:
 *   - spring-boot-starter-web       — Tomcat + Spring MVC for REST APIs
 *   - spring-boot-starter-thymeleaf — Server-side HTML templating
 *   - spring-boot-starter-security  — Authentication & authorization
 *   - spring-boot-starter-validation — Bean Validation (JSR 380)
 *   - spring-boot-starter-actuator  — Production monitoring endpoints
 *   - spring-boot-devtools          — Hot reload during development
 *
 * CONCEPT: Excluding Auto-Configuration
 * --------------------------------------
 * Since we are using HashMap-based repositories (no real database), we exclude
 * DataSourceAutoConfiguration and HibernateJpaAutoConfiguration. This tells
 * Spring Boot NOT to auto-configure a DataSource or JPA EntityManager.
 *
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.SpringApplication
 */
@SpringBootApplication(
    // Scan only the springboot sub-package so we don't conflict with other modules
    scanBasePackages = "com.chapman.edu.commissions.springboot",
    // Exclude database auto-config since we use HashMap-based repositories
    exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
    }
)
public class CommissionCalculatorSpringBootApplication {

    /**
     * The main() method uses SpringApplication.run() to launch the application.
     *
     * What happens when run() is called:
     *   1. Creates an ApplicationContext (the IoC Container)
     *   2. Performs component scanning to find beans
     *   3. Runs auto-configuration based on classpath
     *   4. Starts the embedded Tomcat server
     *   5. Initializes all beans and dependency injection
     *   6. Calls CommandLineRunner / ApplicationRunner beans
     *
     * @param args command-line arguments (can include --spring.profiles.active=dev)
     */
    public static void main(String[] args) {
        SpringApplication.run(CommissionCalculatorSpringBootApplication.class, args);
    }
}
