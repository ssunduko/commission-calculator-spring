# Microservice Architecture

## Overview

The microservice architecture splits the commission calculator into **5 independent services**, each with its own database, port, and deployment lifecycle. Every service is a standalone Spring Boot application that can be started, stopped, scaled, and deployed independently.

This contrasts with the monolithic vertical-slice and clean-architecture implementations in the same project, where all features share a single process and database.

## Architecture Diagram

```
                    ┌──────────────┐
      Client ──────>│ API Gateway  │
                    │  (port 8090) │
                    └──────┬───────┘
                           │
          ┌────────────────┼────────────────┐────────────────┐
          ▼                ▼                ▼                ▼
  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
  │ Deal Service │ │ Plan Service │ │ Calc Service │ │Dispute Service│
  │ (port 8091)  │ │ (port 8092)  │ │ (port 8093)  │ │ (port 8094)  │
  │  H2: deals   │ │  H2: plans   │ │  H2: calcs   │ │ H2: disputes │
  └──────────────┘ └──────────────┘ └──────┬───────┘ └──────────────┘
                                           │ REST calls
                                    ┌──────┴──────┐
                                    ▼             ▼
                              Deal Service  Plan Service
```

## Services

| Service              | Port | Database           | Owns                     |
|----------------------|------|--------------------|--------------------------|
| API Gateway          | 8090 | None               | Request routing           |
| Deal Service         | 8091 | `dealservicedb`    | Deals, DealProducts       |
| Plan Service         | 8092 | `planservicedb`    | Plans, Rules, Tiers       |
| Calculation Service  | 8093 | `calcservicedb`    | Calculations              |
| Dispute Service      | 8094 | `disputeservicedb` | Disputes, Comments        |

## Key Concepts

### 1. Independent Deployability

Each service has its own `main()` method and port. You can restart the Deal Service without affecting the Plan Service.

```java
// DealServiceApplication.java
@SpringBootApplication(scanBasePackages = "...microservice.dealservice")
public class DealServiceApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DealServiceApplication.class);
        app.setDefaultProperties(Map.of(
            "server.port", "8091",
            "spring.datasource.url", "jdbc:h2:mem:dealservicedb"
        ));
        app.run(args);
    }
}
```

### 2. Database per Service

Each service uses a separate H2 in-memory database. No service can directly query another service's tables.

| Service         | JDBC URL                        |
|-----------------|----------------------------------|
| Deal Service    | `jdbc:h2:mem:dealservicedb`      |
| Plan Service    | `jdbc:h2:mem:planservicedb`      |
| Calc Service    | `jdbc:h2:mem:calcservicedb`      |
| Dispute Service | `jdbc:h2:mem:disputeservicedb`   |

### 3. Inter-Service Communication via REST

When the Calculation Service needs deal or plan data, it calls the other services over HTTP using Spring's `RestClient`:

```java
// The Calculation Service fetches deal data via REST, not a shared database
DealDto deal = dealClient.get()
    .uri("/api/deals/{id}", dealId)
    .retrieve()
    .body(DealDto.class);
```

### 4. API Gateway Pattern

The `GatewayApplication` is the single entry point for all client requests. It routes based on URL prefix:

| Client calls              | Gateway routes to          |
|---------------------------|----------------------------|
| `GET /api/ms/deals`       | Deal Service `/api/deals`  |
| `POST /api/ms/plans`      | Plan Service `/api/plans`  |
| `GET /api/ms/calculations` | Calc Service `/api/calculations` |
| `POST /api/ms/disputes`   | Dispute Service `/api/disputes` |

The gateway has no database -- it only proxies requests.

### 5. Service Registry

A simplified `ServiceRegistry` component resolves service URLs from configuration properties. In production, this would use Eureka, Consul, or Kubernetes DNS.

```java
@Component
public class ServiceRegistry {
    @Value("${services.deal.url:http://localhost:8091}")
    private String dealServiceUrl;
    // ...
}
```

### 6. Shared DTOs

Services communicate using shared DTO records from the `common.dto` package. These DTOs have no dependency on any service's domain model:

```java
// common/dto/DealDto.java — used by Deal Service and Calculation Service
public record DealDto(
    String id, String title, BigDecimal value, String salesRepId,
    String status, LocalDate closeDate, LocalDate createdDate
) {}
```

## How to Run

Each service runs in its own terminal. Start them in any order (the gateway will return 502 for services that are not yet running).

```bash
# Terminal 1: Deal Service (port 8091)
mvn spring-boot:run -Dspring-boot.run.mainClass=com.chapman.edu.commissions.architecture.microservice.dealservice.DealServiceApplication

# Terminal 2: Plan Service (port 8092)
mvn spring-boot:run -Dspring-boot.run.mainClass=com.chapman.edu.commissions.architecture.microservice.planservice.PlanServiceApplication

# Terminal 3: Calculation Service (port 8093)
mvn spring-boot:run -Dspring-boot.run.mainClass=com.chapman.edu.commissions.architecture.microservice.calculationservice.CalculationServiceApplication

# Terminal 4: Dispute Service (port 8094)
mvn spring-boot:run -Dspring-boot.run.mainClass=com.chapman.edu.commissions.architecture.microservice.disputeservice.DisputeServiceApplication

# Terminal 5: API Gateway (port 8090)
mvn spring-boot:run -Dspring-boot.run.mainClass=com.chapman.edu.commissions.architecture.microservice.gateway.GatewayApplication
```

## REST Endpoints

### Deal Service (port 8091)

| Method | Path                         | Description             |
|--------|------------------------------|-------------------------|
| GET    | `/api/deals`                 | List all deals          |
| GET    | `/api/deals?salesRepId=X`    | Filter by sales rep     |
| GET    | `/api/deals?status=WON`      | Filter by status        |
| GET    | `/api/deals/{id}`            | Get deal by ID          |
| POST   | `/api/deals`                 | Create a deal           |
| PUT    | `/api/deals/{id}`            | Update a deal           |
| DELETE | `/api/deals/{id}`            | Delete a deal           |

### Plan Service (port 8092)

| Method | Path                         | Description             |
|--------|------------------------------|-------------------------|
| GET    | `/api/plans`                 | List all plans          |
| GET    | `/api/plans?status=ACTIVE`   | Filter by status        |
| GET    | `/api/plans/{id}`            | Get plan by ID          |
| POST   | `/api/plans`                 | Create a plan           |
| POST   | `/api/plans/{id}/activate`   | Activate a plan         |
| POST   | `/api/plans/{id}/rules`      | Add a rule to a plan    |
| DELETE | `/api/plans/{id}`            | Delete a plan           |

### Dispute Service (port 8094)

| Method | Path                            | Description             |
|--------|---------------------------------|-------------------------|
| GET    | `/api/disputes`                 | List all disputes       |
| GET    | `/api/disputes?salesRepId=X`    | Filter by sales rep     |
| GET    | `/api/disputes?status=INITIATED`| Filter by status        |
| GET    | `/api/disputes/{id}`            | Get dispute by ID       |
| POST   | `/api/disputes`                 | Create a dispute        |
| POST   | `/api/disputes/{id}/escalate`   | Escalate a dispute      |
| POST   | `/api/disputes/{id}/resolve`    | Resolve a dispute       |
| DELETE | `/api/disputes/{id}`            | Delete a dispute        |

### API Gateway (port 8090)

All of the above endpoints are accessible through the gateway by prefixing with `/api/ms/`:

| Gateway path                   | Routes to                     |
|--------------------------------|-------------------------------|
| `/api/ms/deals/**`             | Deal Service `/api/deals/**`  |
| `/api/ms/plans/**`             | Plan Service `/api/plans/**`  |
| `/api/ms/calculations/**`      | Calc Service `/api/calculations/**` |
| `/api/ms/disputes/**`          | Dispute Service `/api/disputes/**` |
| `GET /api/ms/health`           | Gateway health check          |

## Testing

Each microservice is tested independently since they run on different ports with different databases. Tests use `@SpringBootTest` with the specific service's `Application` class and an isolated H2 database:

```java
@SpringBootTest(classes = DealServiceApplication.class, properties = {
    "spring.datasource.url=jdbc:h2:mem:dealtestdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "server.port=0"
})
@AutoConfigureMockMvc
class DealServiceIntegrationTest { ... }
```

Test files:
- `DealServiceIntegrationTest` -- CRUD operations + seeded data verification
- `PlanServiceIntegrationTest` -- create, activate, add rules, delete
- `DisputeServiceIntegrationTest` -- create, escalate, resolve, delete

Run only the microservice tests:
```bash
mvn test -pl . -Dtest="com.chapman.edu.commissions.architecture.microservice.**"
```

## Architecture Comparison

| Aspect                  | Vertical Slice          | Clean Architecture       | Microservice             |
|-------------------------|-------------------------|--------------------------|--------------------------|
| **Deployment unit**     | Single JAR              | Single JAR               | Multiple JARs            |
| **Database**            | Shared                  | Shared                   | Database per service     |
| **Communication**       | In-process method calls | In-process method calls  | REST / HTTP              |
| **Scaling**             | Whole application       | Whole application        | Per service              |
| **Team ownership**      | Feature teams           | Layer teams              | Service teams            |
| **Complexity**          | Low                     | Medium                   | High                     |
| **Latency**             | Lowest (in-memory)      | Lowest (in-memory)       | Higher (network calls)   |
| **Data consistency**    | ACID transactions       | ACID transactions        | Eventual consistency     |
| **Failure isolation**   | Shared fate             | Shared fate              | Independent              |

## Technology Stack

| Technology         | Purpose                                      |
|--------------------|----------------------------------------------|
| Spring Boot 3.4+   | Application framework for each service       |
| Spring Web (MVC)   | REST controllers and `RestClient`            |
| Spring Data JPA    | Repository abstraction per service           |
| H2 Database        | In-memory database (one per service)         |
| Lombok             | Boilerplate reduction (`@Data`, `@NoArgsConstructor`) |
| Spring Security    | Per-service security (permit-all for demo)   |
| JUnit 5 + MockMvc  | Integration testing per service              |

## Trade-offs

### Advantages
- **Independent deployability** -- ship one service without redeploying others
- **Technology heterogeneity** -- each service can use a different stack (in theory)
- **Fault isolation** -- a crash in the Dispute Service does not take down deals
- **Scalability** -- scale the Calculation Service to 10 instances while keeping one Deal Service
- **Team autonomy** -- a team owns one service end-to-end

### Disadvantages
- **Operational complexity** -- five processes to monitor, deploy, and debug
- **Network latency** -- every cross-service call adds milliseconds
- **Data consistency** -- no cross-service ACID transactions; must use sagas or eventual consistency
- **Testing difficulty** -- integration tests require all dependent services to be running (or mocked)
- **Code duplication** -- shared DTOs must be kept in sync across services
- **Debugging** -- a single user request may span multiple services, requiring distributed tracing

## Package Structure

```
microservice/
├── common/dto/                  # Shared DTOs used across services
│   ├── DealDto.java
│   ├── PlanDto.java
│   ├── CalculationDto.java
│   ├── DisputeDto.java
│   ├── CreateDealRequest.java
│   ├── UpdateDealRequest.java
│   ├── CreatePlanRequest.java
│   ├── AddRuleRequest.java
│   ├── CalculateCommissionRequest.java
│   ├── CreateDisputeRequest.java
│   └── ResolveDisputeRequest.java
├── dealservice/                 # Deal Service (port 8091)
│   ├── DealServiceApplication.java
│   ├── DealController.java
│   ├── DealService.java
│   ├── DealRepository.java
│   ├── DealDataInitializer.java
│   ├── config/SecurityConfig.java
│   └── domain/ (Deal, DealProduct, DealStatus)
├── planservice/                 # Plan Service (port 8092)
│   ├── PlanServiceApplication.java
│   ├── PlanController.java
│   ├── PlanService.java
│   ├── PlanRepository.java
│   ├── config/SecurityConfig.java
│   └── domain/ (CommissionPlan, CommissionRule, CommissionTier, ...)
├── calculationservice/          # Calculation Service (port 8093)
│   └── domain/ (CommissionCalculation)
├── disputeservice/              # Dispute Service (port 8094)
│   ├── DisputeServiceApplication.java
│   ├── DisputeController.java
│   ├── DisputeService.java
│   ├── DisputeRepository.java
│   ├── config/SecurityConfig.java
│   └── domain/ (Dispute, DisputeComment, DisputeStatus)
├── gateway/                     # API Gateway (port 8090)
│   ├── GatewayApplication.java
│   ├── GatewayController.java
│   ├── ServiceRegistry.java
│   └── config/SecurityConfig.java
└── processor/                   # STARTUP DEMOS — Showcases Microservice concepts
    ├── MicroserviceProcessor.java       # Demonstrates all Microservice patterns
    └── MicroserviceProcessorDemo.java   # CommandLineRunner for startup demos
```

## Processor Demos

The `MicroserviceProcessor` runs at startup to demonstrate key Microservice concepts:

| Demo | Concept | What It Shows |
|------|---------|---------------|
| **Service Topology** | Service registry | Each service has own port, database, and deployment lifecycle |
| **Independent Services** | Service autonomy | Deal Service operates without knowledge of Plan or Calculation Service |
| **Inter-Service Communication** | REST clients | CalculationService calls DealServiceClient/PlanServiceClient via HTTP |
| **API Gateway** | Single entry point | Gateway routes /api/ms/** to appropriate service by URL path |
| **Database per Service** | Data isolation | No cross-service JOINs; data accessed only via REST APIs |
