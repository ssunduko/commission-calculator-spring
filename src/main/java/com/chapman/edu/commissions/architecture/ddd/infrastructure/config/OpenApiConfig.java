package com.chapman.edu.commissions.architecture.ddd.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dddOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Commission Calculator -- Domain-Driven Design")
                .description("DDD with aggregates, domain services, application services, and ubiquitous language")
                .version("1.0.0"));
    }
}
