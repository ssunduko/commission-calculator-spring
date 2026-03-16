package com.chapman.edu.commissions.verticalslice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for API documentation.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI commissionCalculatorOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Commission Calculator API")
                .description("REST API for managing sales commissions, deals, plans, and disputes")
                .version("1.0")
                .contact(new Contact()
                    .name("Sergey L. Sundukovskiy, Ph.D.")
                    .email("sundukovskiy@chapman.edu")))
            .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
            .components(new Components()
                .addSecuritySchemes("basicAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic")
                        .description("Basic Authentication with username and password")));
    }
}
