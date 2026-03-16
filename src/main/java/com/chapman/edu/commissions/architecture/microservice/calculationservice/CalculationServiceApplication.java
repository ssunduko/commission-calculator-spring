package com.chapman.edu.commissions.architecture.microservice.calculationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.chapman.edu.commissions.architecture.microservice.calculationservice",
        "com.chapman.edu.commissions.architecture.microservice.common"
})
public class CalculationServiceApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CalculationServiceApplication.class);
        app.setAdditionalProfiles("calculation-service");
        app.setDefaultProperties(java.util.Map.of(
                "server.port", "8093",
                "spring.application.name", "calculation-service",
                "spring.datasource.url", "jdbc:h2:mem:calcservicedb",
                "spring.jpa.hibernate.ddl-auto", "update",
                "spring.flyway.enabled", "false",
                "spring.jpa.show-sql", "false",
                "services.deal.url", "http://localhost:8091",
                "services.plan.url", "http://localhost:8092"
        ));
        app.run(args);
    }
}
