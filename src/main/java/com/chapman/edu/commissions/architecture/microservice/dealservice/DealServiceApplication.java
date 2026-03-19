package com.chapman.edu.commissions.architecture.microservice.dealservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CONCEPT: Independent Microservice
 *
 * Each microservice is a standalone Spring Boot application that can
 * be started, stopped, scaled, and deployed independently. The Deal
 * Service runs on port 8091 and owns all deal-related data and logic.
 *
 * To start: java -jar app.jar --spring.profiles.active=deal-service
 * Or: mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=deal-service"
 */
@SpringBootApplication(
    scanBasePackages = "com.chapman.edu.commissions.architecture.microservice.dealservice",
    exclude = {org.springframework.ai.model.anthropic.autoconfigure.AnthropicChatAutoConfiguration.class}
)
public class DealServiceApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DealServiceApplication.class);
        app.setAdditionalProfiles("deal-service");
        app.setDefaultProperties(java.util.Map.of(
            "server.port", "8091",
            "spring.application.name", "deal-service",
            "spring.datasource.url", "jdbc:h2:mem:dealservicedb",
            "spring.jpa.hibernate.ddl-auto", "update",
            "spring.flyway.enabled", "false",
            "spring.jpa.show-sql", "false",
            "spring.h2.console.enabled", "true"
        ));
        app.run(args);
    }
}
