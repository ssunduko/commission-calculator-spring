package com.chapman.edu.commissions.springboot.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ============================================================================
 * OPENAPI / SWAGGER CONFIGURATION
 * ============================================================================
 *
 * CONCEPT: API Documentation with OpenAPI (Swagger)
 * ---------------------------------------------------
 * OpenAPI (formerly Swagger) provides interactive API documentation that:
 *   - Auto-generates from your controller annotations
 *   - Provides a "Try it out" UI to test endpoints in the browser
 *   - Documents request/response schemas from your DTOs
 *   - Shows authentication requirements
 *
 * springdoc-openapi scans @RestController classes and generates the spec.
 * Annotations like @Tag, @Operation, @ApiResponse enrich the documentation.
 *
 * Access:
 *   Swagger UI:  http://localhost:8081/swagger-ui/
 *   OpenAPI JSON: http://localhost:8081/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI commissionCalculatorOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Commission Calculator API")
                .description("Spring Boot Fundamentals — Commission Calculator REST API\n\n"
                    + "This API provides endpoints for managing deals, commission plans, "
                    + "commission calculations, users, and disputes.\n\n"
                    + "**Authentication:** Use the `/api/auth/login` endpoint to obtain a JWT token, "
                    + "then click the 'Authorize' button above and enter: `Bearer <your-token>`")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Chapman University SDLC Course")
                    .email("sundukovskiy@chapman.edu"))
                .license(new License()
                    .name("Educational Use")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter the JWT token obtained from POST /api/auth/login")));
    }
}
