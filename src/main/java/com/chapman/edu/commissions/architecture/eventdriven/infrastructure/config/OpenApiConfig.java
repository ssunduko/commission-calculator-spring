package com.chapman.edu.commissions.architecture.eventdriven.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for the Event-Driven Architecture module.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eventDrivenOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Commission Calculator — Event-Driven Architecture")
                        .description("Event-driven commission system using domain events, event store, and event listeners")
                        .version("1.0.0"));
    }
}
