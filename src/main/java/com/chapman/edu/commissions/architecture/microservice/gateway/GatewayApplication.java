package com.chapman.edu.commissions.architecture.microservice.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * CONCEPT: API Gateway (Microservice Architecture)
 *
 * The API Gateway is the single entry point for all client requests.
 * It routes requests to the appropriate microservice based on the URL path:
 *
 *   /api/ms/deals/**       -> Deal Service (port 8091)
 *   /api/ms/plans/**       -> Plan Service (port 8092)
 *   /api/ms/calculations/** -> Calculation Service (port 8093)
 *   /api/ms/disputes/**    -> Dispute Service (port 8094)
 *
 * Benefits:
 * - Clients have ONE URL to call, not four
 * - Cross-cutting concerns (auth, rate limiting) applied in one place
 * - Services can be moved/scaled without clients knowing
 *
 * The gateway has NO database -- it only proxies requests.
 */
@SpringBootApplication(
    scanBasePackages = "com.chapman.edu.commissions.architecture.microservice.gateway",
    exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class}
)
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(GatewayApplication.class);
        app.setAdditionalProfiles("gateway");
        app.setDefaultProperties(java.util.Map.of(
            "server.port", "8090",
            "spring.application.name", "api-gateway",
            "spring.flyway.enabled", "false",
            "spring.jpa.hibernate.ddl-auto", "none",
            "spring.autoconfigure.exclude", "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
        ));
        app.run(args);
    }
}
