package com.chapman.edu.commissions.architecture.orthogonal.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI orthogonalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Commission Calculator — Orthogonal Architecture")
                        .description("CQRS pipeline with orthogonal aspects (logging, validation, auditing, performance)")
                        .version("1.0.0"));
    }
}
