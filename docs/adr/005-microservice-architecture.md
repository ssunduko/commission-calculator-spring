# ADR-005: Microservice Architecture

## Status
Accepted

## Date
2026-03-19

## Context
In a production environment with multiple teams, the commission system's components have different scaling requirements and release cadences. The deal management team ships weekly, the calculation engine team needs to deploy hotfixes independently, and the dispute resolution team operates on a different sprint cycle. A monolithic deployment couples all these release schedules together.

Additionally, the calculation service experiences 10x more load during end-of-quarter processing and needs to scale independently.

## Decision
Decompose the commission system into five independently deployable services plus an API Gateway:

| Service              | Port | Responsibility                        | Database            |
|---------------------|------|---------------------------------------|---------------------|
| API Gateway         | 8090 | Request routing, cross-cutting concerns | None (stateless)    |
| Deal Service        | 8091 | Deal lifecycle management             | H2 (dealservicedb)  |
| Plan Service        | 8092 | Commission plan CRUD and activation   | H2 (planservicedb)  |
| Calculation Service | 8093 | Commission calculation engine         | H2 (calcservicedb)  |
| Dispute Service     | 8094 | Dispute filing and resolution         | H2 (disputeservicedb)|

Key design decisions:
- **Database per service:** Each service owns its data; no direct cross-service table access
- **REST inter-service communication:** Services call each other via `RestClient` using service URLs from configuration
- **Shared DTOs:** A `common.dto` package provides request/response contracts for inter-service communication
- **API Gateway pattern:** Single entry point routes `/api/ms/**` requests to the appropriate backend service
- **Service registry via configuration:** Service URLs injected via `@Value` properties (production would use Eureka/Consul)
- **All-in-one launcher:** `MicroserviceAllInOneApplication` starts all services in separate threads within a single JVM for development convenience

Each service has its own:
- `@SpringBootApplication` entry point
- Spring Security configuration
- Spring profile
- Component scan scope

## Consequences

### Positive
- Independent deployment and release cycles per service
- Independent scaling — calculation service can scale horizontally during peak periods
- Technology flexibility — each service can evolve its stack independently
- Fault isolation — a failure in dispute service does not affect deal processing
- Team autonomy — clear ownership boundaries

### Negative
- Operational complexity — five processes, five databases, five deployment pipelines
- Network latency on inter-service calls (calculation service calls both deal and plan services)
- Distributed debugging requires correlation IDs and centralized logging
- Data consistency across services requires eventual consistency patterns
- Shared DTO package creates a coupling point that must be versioned carefully

### Trade-offs
- The operational overhead is justified only when teams and scaling requirements truly differ
- For a single team, the microservice boundaries add complexity without proportional benefit
- The all-in-one launcher mitigates development friction but does not represent production topology
