# ADR-009: Spring Boot as the Unified Application Framework

## Status
Accepted

## Date
2026-03-19

## Context
The commission calculator requires a web framework for REST APIs, dependency injection for wiring components, JPA for persistence, security for access control, and testing support. Each architecture module needs these capabilities while remaining independently configurable.

## Decision
Use Spring Boot 3.4+ as the unified framework across all architecture modules, leveraging:

- **Spring MVC:** REST controllers with JSON serialization
- **Spring Data JPA:** Repository interfaces with H2 in-memory databases
- **Spring Security:** Per-module security configurations (JWT-based for springboot module, permit-all for architecture demos)
- **Spring Profiles:** Each module/service activates its own profile for isolated configuration
- **Spring Boot DevTools:** Hot reload during development
- **Spring AI + MCP:** AI agent integration via Model Context Protocol in the vertical slice module
- **Flyway:** Database migration management for the ORM module

Framework-specific decisions:
- `@MockitoBean` (not deprecated `@MockBean`) for Spring Boot 3.4+ test mocking
- `spring.main.allow-bean-definition-overriding=true` for multi-module bean coexistence
- Each microservice gets its own `@SpringBootApplication` with scoped component scanning
- Architecture demo modules exclude `AnthropicChatAutoConfiguration` to avoid requiring an API key

## Consequences

### Positive
- Consistent programming model across all six architecture modules
- Rich ecosystem of starters reduces boilerplate (security, JPA, web, test)
- Spring profiles enable per-module configuration without file duplication
- Auto-configuration handles most infrastructure wiring

### Negative
- Modules that aim for framework independence (Clean Architecture, DDD) still depend on Spring at the adapter layer
- Spring Boot version upgrades affect all modules simultaneously
- Auto-configuration magic can cause unexpected bean conflicts in multi-module setups (e.g., multiple SecurityFilterChain beans)

### Mitigations
- Clean Architecture and DDD domain layers contain zero Spring imports — framework independence is preserved where it matters most
- Unique bean names on `@Configuration` classes prevent conflicts across modules
