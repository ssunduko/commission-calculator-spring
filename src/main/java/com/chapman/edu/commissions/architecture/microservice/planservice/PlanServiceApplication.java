package com.chapman.edu.commissions.architecture.microservice.planservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = "com.chapman.edu.commissions.architecture.microservice.planservice",
    exclude = {org.springframework.ai.model.anthropic.autoconfigure.AnthropicChatAutoConfiguration.class}
)
public class PlanServiceApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PlanServiceApplication.class);
        app.setAdditionalProfiles("plan-service");
        app.setDefaultProperties(java.util.Map.of(
                "server.port", "8092",
                "spring.application.name", "plan-service",
                "spring.datasource.url", "jdbc:h2:mem:planservicedb",
                "spring.jpa.hibernate.ddl-auto", "update",
                "spring.flyway.enabled", "false",
                "spring.jpa.show-sql", "false"
        ));
        app.run(args);
    }
}
