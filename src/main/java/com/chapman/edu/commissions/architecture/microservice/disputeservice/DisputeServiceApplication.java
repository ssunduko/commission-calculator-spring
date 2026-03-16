package com.chapman.edu.commissions.architecture.microservice.disputeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.chapman.edu.commissions.architecture.microservice.disputeservice")
public class DisputeServiceApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DisputeServiceApplication.class);
        app.setAdditionalProfiles("dispute-service");
        app.setDefaultProperties(java.util.Map.of(
            "server.port", "8094",
            "spring.application.name", "dispute-service",
            "spring.datasource.url", "jdbc:h2:mem:disputeservicedb",
            "spring.jpa.hibernate.ddl-auto", "update",
            "spring.flyway.enabled", "false",
            "spring.jpa.show-sql", "false"
        ));
        app.run(args);
    }
}
