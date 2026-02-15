# Spring Boot Fundamentals -- Commission Calculator

**Course:** Undergraduate Software Development Life Cycle (SDLC)
**Institution:** Chapman University
**Framework:** Spring Boot 3.4.5 / Java 21

---

## Overview

This package (`com.chapman.edu.commissions.springboot`) is a fully functional Commission Calculator application built with Spring Boot. It serves as a hands-on teaching implementation for an undergraduate SDLC class, demonstrating how modern enterprise Java applications are structured using the Spring ecosystem.

The application manages sales deals, commission plans, commission calculations, users, and disputes. It exposes both a RESTful JSON API (secured with JWT authentication) and a server-rendered web interface (using Thymeleaf with form-based login). All data is stored in-memory using `ConcurrentHashMap`-based repositories -- no external database is required -- so students can focus on Spring concepts rather than database setup.

At startup, the application loads sample data (6 users, 6 deals, 3 commission plans, 4 calculations, and 1 dispute) and runs several `CommandLineRunner` processors that print demonstrations of core Spring concepts to the console log.

---

## Project Structure

```
springboot/
|-- CommissionCalculatorSpringBootApplication.java   Main entry point (@SpringBootApplication)
|
|-- config/
|   |-- AppConfig.java              @Configuration with @Bean definitions (RestTemplate, IoC)
|   |-- SecurityConfig.java         Spring Security: JWT filter chain, form login, RBAC
|   |-- WebMvcConfig.java           Spring MVC and Thymeleaf configuration
|   |-- DevToolsConfig.java         DevTools hot-reload configuration
|
|-- controller/
|   |-- AuthController.java         POST /api/auth/login -- JWT authentication
|   |-- DealController.java         @RestController -- full CRUD for deals
|   |-- CommissionPlanController.java   @RestController -- plan management
|   |-- CommissionCalculationController.java   @RestController with @PreAuthorize (RBAC)
|   |-- UserController.java         @RestController -- user management (admin-restricted)
|   |-- DisputeController.java      @RestController -- dispute lifecycle
|   |-- DashboardController.java    @Controller -- Thymeleaf MVC web pages
|   |-- HealthController.java       GET /api/health -- public health check
|
|-- dto/
|   |-- request/
|   |   |-- CreateDealRequest.java          @Valid, @NotNull, @NotBlank, @Size, @DecimalMin
|   |   |-- CreatePlanRequest.java
|   |   |-- CreateUserRequest.java
|   |   |-- CalculateCommissionRequest.java
|   |   |-- CreateDisputeRequest.java
|   |   |-- LoginRequest.java
|   |-- response/
|       |-- ApiResponse<T>                  Generic success wrapper
|       |-- ApiErrorResponse.java           Structured error response with validation details
|       |-- DealResponse.java
|       |-- CommissionPlanResponse.java
|       |-- CommissionCalculationResponse.java
|       |-- UserResponse.java
|       |-- DisputeResponse.java
|       |-- AuthResponse.java              JWT token + roles
|
|-- exception/
|   |-- GlobalExceptionHandler.java       @ControllerAdvice with @ExceptionHandler methods
|   |-- ResourceNotFoundException.java    Maps to HTTP 404
|   |-- BusinessValidationException.java  Maps to HTTP 422
|   |-- UnauthorizedException.java        Maps to HTTP 401
|
|-- mapper/
|   |-- DtoMapper.java                    @Component -- converts domain models to DTOs
|
|-- repository/   (all HashMap-based, @Repository, ConcurrentHashMap)
|   |-- DealRepository.java
|   |-- CommissionPlanRepository.java
|   |-- CommissionCalculationRepository.java
|   |-- UserRepository.java
|   |-- DisputeRepository.java
|
|-- security/
|   |-- JwtTokenProvider.java             Token generation and validation (JJWT library)
|   |-- JwtAuthenticationFilter.java      OncePerRequestFilter -- extracts JWT from headers
|   |-- CustomUserDetailsService.java     UserDetailsService impl backed by UserRepository
|
|-- service/   (@Service layer with business logic)
|   |-- DealService.java
|   |-- CommissionPlanService.java
|   |-- CommissionCalculationService.java
|   |-- UserService.java
|   |-- DisputeService.java
|
|-- processor/   (CommandLineRunner demos -- print to console at startup)
|   |-- DependencyInjectionProcessor.java    IoC container, bean discovery, singleton scope
|   |-- RestApiProcessor.java                REST concepts, HTTP methods, ResponseEntity
|   |-- ValidationProcessor.java             Bean Validation annotations and error handling
|   |-- SecurityProcessor.java               Security filter chain, JWT flow, RBAC
|   |-- ConfigurationProcessor.java          Profiles, properties, externalized config
|
|-- util/
    |-- SampleDataLoader.java               CommandLineRunner -- loads seed data at startup
```

### Templates (Thymeleaf)

```
src/main/resources/templates/springboot/
|-- layout.html              Shared layout with navigation
|-- login.html               Login page (form-based auth)
|-- dashboard.html           Main dashboard with summary counts
|-- deals/
|   |-- list.html            Deal listing with table
|   |-- detail.html          Single deal view
|   |-- form.html            Create new deal form (with validation)
|-- plans/
|   |-- list.html            Commission plans listing
|   |-- detail.html          Plan detail with tiers and rules
|-- calculations/
    |-- list.html            Calculations listing
    |-- detail.html          Calculation detail with bonuses
```

### Configuration Files

```
src/main/resources/
|-- application.properties          Base config (port 8081, actuator, Swagger)
|-- application-dev.properties      Dev profile (port 8082, DEBUG logging, devtools, short JWT)
|-- application-prod.properties     Prod profile (port 8080, WARN logging, GZIP, strong JWT)
```

---

## Concepts Covered

### 1. Spring Boot Auto-Configuration and Starter Dependencies

Spring Boot auto-configures beans based on what is on the classpath. The `@SpringBootApplication` annotation on `CommissionCalculatorSpringBootApplication.java` combines `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`. Since this project uses in-memory repositories rather than a real database, `DataSourceAutoConfiguration` and `HibernateJpaAutoConfiguration` are explicitly excluded.

**Starter dependencies** used in `pom.xml`:
- `spring-boot-starter-web` -- embedded Tomcat + Spring MVC
- `spring-boot-starter-thymeleaf` -- server-side HTML templating
- `spring-boot-starter-security` -- authentication and authorization
- `spring-boot-starter-validation` -- Bean Validation (JSR 380)
- `spring-boot-starter-actuator` -- production monitoring endpoints
- `spring-boot-devtools` -- hot reload during development

**Key files:** `CommissionCalculatorSpringBootApplication.java`, `pom.xml`

### 2. Spring Initializr and Project Structure

The project follows the standard Spring Boot layout generated by [Spring Initializr](https://start.spring.io): a Maven `pom.xml` with starter dependencies, the main application class, `application.properties`, and the standard `src/main/java` / `src/main/resources` / `src/test` directory structure.

**Key files:** `pom.xml`, `CommissionCalculatorSpringBootApplication.java`

### 3. RESTful API Development with Spring MVC

The application exposes a full REST API following standard HTTP semantics:
- `GET` for reading resources
- `POST` for creating resources (returns 201 Created)
- `PATCH` for partial updates (e.g., status changes)
- `DELETE` for removing resources (returns 204 No Content)

`ResponseEntity<T>` is used throughout to control HTTP status codes, headers, and response bodies. All responses use the generic `ApiResponse<T>` wrapper for consistent structure.

**Key files:** `DealController.java`, `CommissionPlanController.java`, `CommissionCalculationController.java`, `UserController.java`, `DisputeController.java`

### 4. Dependency Injection and the IoC Container

The Spring IoC Container (`ApplicationContext`) manages all bean creation, dependency resolution, and lifecycle. This project demonstrates:

- **Constructor injection** (preferred) -- used in all controllers, services, and repositories
- **Field injection** (`@Autowired`) -- shown in `DependencyInjectionProcessor.java` for educational comparison
- **Stereotype annotations**: `@Component`, `@Service`, `@Repository`, `@Controller`, `@RestController`
- **`@Bean` methods** in `@Configuration` classes for manual bean registration (e.g., `RestTemplate` in `AppConfig.java`)
- **Singleton scope** -- demonstrated by verifying two `getBean()` calls return the same instance

**Key files:** `AppConfig.java`, `DependencyInjectionProcessor.java`, all service and repository classes

### 5. Building REST APIs -- Controllers, Path Variables, Request Parameters

- `@RestController` combines `@Controller` + `@ResponseBody` for JSON APIs
- `@RequestMapping` at class level sets the base path (e.g., `/api/deals`)
- `@GetMapping`, `@PostMapping`, `@PatchMapping`, `@DeleteMapping` map HTTP methods
- `@PathVariable` extracts URL path segments (e.g., `/api/deals/{id}`)
- `@RequestParam` extracts query string parameters (e.g., `?status=WON&salesRepId=user-003`)
- `@RequestBody` deserializes JSON request bodies into Java objects

**Key files:** `DealController.java` (extensively documented), `AuthController.java`

### 6. Application Properties, Profiles, and Externalized Configuration

Three property files demonstrate environment-specific configuration:

| Property | Base | Dev | Prod |
|---|---|---|---|
| `server.port` | 8081 | 8082 | 8080 |
| Logging level | (default) | DEBUG | WARN |
| Thymeleaf cache | (default) | false | true |
| Error details | (default) | always | never |
| JWT expiration | -- | 1 hour | 24 hours |
| DevTools | -- | enabled | disabled |

Profiles are activated via `--spring.profiles.active=dev` on the command line. Profile-specific properties override base properties. The production profile demonstrates externalized secrets using environment variables (`${APP_JWT_SECRET:default}`).

**Key files:** `application.properties`, `application-dev.properties`, `application-prod.properties`, `ConfigurationProcessor.java`

### 7. Spring Boot DevTools and Hot Reload

The `spring-boot-devtools` dependency enables automatic application restart when class files change and LiveReload for browser refresh. `DevToolsConfig.java` configures DevTools behavior, and the dev profile enables both restart and LiveReload while the prod profile disables them.

**Key files:** `DevToolsConfig.java`, `application-dev.properties`, `application-prod.properties`

### 8. Error Handling and Validation Best Practices

Request DTOs use Bean Validation annotations (`@Valid`, `@NotNull`, `@NotBlank`, `@Size`, `@DecimalMin`) to enforce input constraints. When validation fails, Spring throws `MethodArgumentNotValidException`, which is caught by the global exception handler.

**Key files:** `CreateDealRequest.java`, `CreatePlanRequest.java`, `CreateUserRequest.java`, `ValidationProcessor.java`

### 9. Exception Handling -- @ControllerAdvice and @ExceptionHandler

`GlobalExceptionHandler.java` uses `@ControllerAdvice` to centralize exception handling across all controllers. Each `@ExceptionHandler` method maps a specific exception type to an HTTP status code and structured error response:

| Exception | HTTP Status | Description |
|---|---|---|
| `MethodArgumentNotValidException` | 400 Bad Request | Bean Validation failures with field-level details |
| `UnauthorizedException` | 401 Unauthorized | Authentication or authorization failure |
| `ResourceNotFoundException` | 404 Not Found | Entity not found in repository |
| `BusinessValidationException` | 422 Unprocessable Entity | Business rule violation |
| `Exception` (catch-all) | 500 Internal Server Error | Unexpected errors (stack trace logged, not exposed) |

All error responses use the `ApiErrorResponse` structure with status code, error type, message, request path, and optional field-level validation errors.

**Key files:** `GlobalExceptionHandler.java`, `ResourceNotFoundException.java`, `BusinessValidationException.java`, `UnauthorizedException.java`, `ApiErrorResponse.java`

### 10. Spring Security -- Authentication, Authorization, JWT, and RBAC

Security is configured with two filter chains in `SecurityConfig.java`:

1. **API filter chain** (`/api/**`, Order 1) -- stateless, JWT-based, CSRF disabled
2. **Web filter chain** (everything else, Order 2) -- form-based login, session-based

**JWT authentication flow:**
1. Client sends `POST /api/auth/login` with `{ "username": "admin", "password": "admin123" }`
2. `AuthenticationManager` validates credentials via `CustomUserDetailsService`
3. `JwtTokenProvider` generates a signed JWT
4. Client sends the JWT in subsequent requests: `Authorization: Bearer <token>`
5. `JwtAuthenticationFilter` (extends `OncePerRequestFilter`) validates the token on each request

**Role-Based Access Control (RBAC):**

| Role | Permissions |
|---|---|
| `SYSTEM_ADMIN` | Full access to all endpoints |
| `SALES_MANAGER` | Manage team deals, approve calculations, view users |
| `SALES_REP` | View own deals and calculations, create disputes |
| `FINANCE_ADMIN` | View all calculations, process payments |

Method-level security is enabled via `@EnableMethodSecurity` and enforced with `@PreAuthorize` annotations (e.g., only `FINANCE_ADMIN` and `SYSTEM_ADMIN` can call `markAsPaid()`).

**Key files:** `SecurityConfig.java`, `JwtTokenProvider.java`, `JwtAuthenticationFilter.java`, `CustomUserDetailsService.java`, `AuthController.java`, `CommissionCalculationController.java`, `UserController.java`, `SecurityProcessor.java`

---

## How to Run

### Prerequisites

- Java 21 (JDK)
- Maven 3.9+ (or use the included Maven wrapper `./mvnw`)

### Running the Application

**Default (port 8081):**
```bash
./mvnw spring-boot:run
```

**With the dev profile (port 8082, DEBUG logging, DevTools):**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**With the prod profile (port 8080, minimal logging, GZIP compression):**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

**As a packaged JAR:**
```bash
./mvnw clean package -DskipTests
java -jar target/commission-calculator-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### What Happens at Startup

1. Spring Boot creates the ApplicationContext (IoC Container)
2. Component scanning discovers all `@Component`, `@Service`, `@Repository`, `@Controller` beans
3. Auto-configuration sets up Tomcat, Spring MVC, Thymeleaf, and Spring Security
4. `SampleDataLoader` runs and loads seed data into the in-memory repositories
5. `CommandLineRunner` processors print concept demonstrations to the console
6. The embedded Tomcat server starts and begins accepting requests

---

## Sample Credentials

All passwords are BCrypt-encoded at startup by `SampleDataLoader.java`.

| Username | Password | Role | User ID |
|---|---|---|---|
| `admin` | `admin123` | SYSTEM_ADMIN | user-001 |
| `jsmith` | `password123` | SALES_MANAGER | user-002 |
| `agarcia` | `password123` | SALES_REP | user-003 |
| `bwilson` | `password123` | SALES_REP | user-004 |
| `clee` | `password123` | SALES_REP | user-005 |
| `dfinance` | `password123` | FINANCE_ADMIN | user-006 |

---

## API Endpoints

All REST API endpoints are prefixed with `/api` and return JSON wrapped in `ApiResponse<T>`. Authentication is required unless noted otherwise. Include the JWT token in the `Authorization` header: `Authorization: Bearer <token>`.

### Authentication

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/login` | None | Authenticate and receive a JWT token |
| `GET` | `/api/health` | None | Application health check |

### Deals

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/deals` | JWT | List all deals (optional: `?status=WON`, `?salesRepId=user-003`) |
| `GET` | `/api/deals/{id}` | JWT | Get a deal by ID |
| `POST` | `/api/deals` | JWT | Create a new deal (validated request body) |
| `PATCH` | `/api/deals/{id}/status` | JWT | Update deal status (`?status=WON`) |
| `DELETE` | `/api/deals/{id}` | JWT | Delete a deal (returns 204) |

### Commission Plans

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/plans` | JWT | List all plans |
| `GET` | `/api/plans/{id}` | JWT | Get a plan by ID |
| `GET` | `/api/plans/active` | JWT | List active plans only |
| `POST` | `/api/plans` | JWT | Create a new plan (validated request body) |
| `PATCH` | `/api/plans/{id}/activate` | JWT | Activate a plan |
| `PATCH` | `/api/plans/{id}/archive` | JWT | Archive a plan |
| `DELETE` | `/api/plans/{id}` | JWT | Delete a plan (returns 204) |

### Commission Calculations

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/calculations` | JWT | List all calculations (optional: `?salesRepId=...`, `?dealId=...`) |
| `GET` | `/api/calculations/{id}` | JWT | Get a calculation by ID |
| `POST` | `/api/calculations` | JWT | Calculate commission for a deal |
| `PATCH` | `/api/calculations/{id}/approve` | JWT + SALES_MANAGER / FINANCE_ADMIN / SYSTEM_ADMIN | Approve a calculation |
| `PATCH` | `/api/calculations/{id}/pay` | JWT + FINANCE_ADMIN / SYSTEM_ADMIN | Mark a calculation as paid |

### Users

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/users` | JWT + SYSTEM_ADMIN / SALES_MANAGER | List all users (optional: `?role=SALES_REP`) |
| `GET` | `/api/users/{id}` | JWT + SYSTEM_ADMIN / SALES_MANAGER | Get a user by ID |
| `POST` | `/api/users` | JWT + SYSTEM_ADMIN | Create a new user |
| `PATCH` | `/api/users/{id}/deactivate` | JWT + SYSTEM_ADMIN | Deactivate a user |
| `DELETE` | `/api/users/{id}` | JWT + SYSTEM_ADMIN | Delete a user (returns 204) |

### Disputes

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/disputes` | JWT | List all disputes (optional: `?salesRepId=...`, `?status=...`) |
| `GET` | `/api/disputes/{id}` | JWT | Get a dispute by ID |
| `POST` | `/api/disputes` | JWT | Create a new dispute |
| `PATCH` | `/api/disputes/{id}/resolve` | JWT | Resolve a dispute (`?resolution=...&resolvedBy=...`) |
| `PATCH` | `/api/disputes/{id}/escalate` | JWT | Escalate a dispute |

### Example: Login and Query Deals

```bash
# 1. Authenticate
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'

# Response includes: { "data": { "token": "eyJhbG...", "username": "admin", "roles": ["ROLE_SYSTEM_ADMIN"] } }

# 2. Use the token to query deals
curl http://localhost:8081/api/deals \
  -H "Authorization: Bearer eyJhbG..."

# 3. Filter deals by status
curl "http://localhost:8081/api/deals?status=WON" \
  -H "Authorization: Bearer eyJhbG..."

# 4. Create a new deal
curl -X POST http://localhost:8081/api/deals \
  -H "Authorization: Bearer eyJhbG..." \
  -H "Content-Type: application/json" \
  -d '{"title": "New Corp Deal", "value": 50000, "salesRepId": "user-003"}'
```

---

## Web UI Pages

The Thymeleaf-based web interface uses form-based login (separate from the JWT-based API). Access any web page in a browser and you will be redirected to the login page if not authenticated.

| Path | Description |
|---|---|
| `/login` | Login page (form-based authentication) |
| `/springboot/dashboard` | Main dashboard with summary counts and recent activity |
| `/springboot/deals` | Deal listing table |
| `/springboot/deals/{id}` | Deal detail view |
| `/springboot/deals/new` | Create new deal form (with server-side validation) |
| `/springboot/plans` | Commission plans listing |
| `/springboot/plans/{id}` | Plan detail view with tiers and rules |
| `/springboot/calculations` | Commission calculations listing |
| `/springboot/calculations/{id}` | Calculation detail view with bonuses |

After a successful login, users are redirected to `/springboot/dashboard`. The dashboard shows counts for deals, plans, calculations, users, and disputes, along with recent deals and active plans.

The web interface demonstrates the `@Controller` pattern (as opposed to `@RestController`): handler methods return Thymeleaf template names (e.g., `"springboot/dashboard"`), and data is passed to templates via the `Model` object using `model.addAttribute()`. The create-deal form demonstrates form binding, `@Valid` server-side validation with `BindingResult`, and the POST-Redirect-GET pattern to prevent duplicate submissions.

---

## Additional Tools and Endpoints

| Tool | Path | Description |
|---|---|---|
| Swagger UI | `/swagger-ui/` | Interactive API documentation (OpenAPI / springdoc) |
| OpenAPI spec | `/api-docs` | Raw OpenAPI 3.0 JSON specification |
| Actuator Health | `/actuator/health` | Spring Boot Actuator health endpoint |
| Actuator Info | `/actuator/info` | Spring Boot Actuator info endpoint |

---

## Domain Model

The application reuses domain model classes from `com.chapman.edu.commissions.model`, including:

- `Deal`, `DealStatus`, `DealProduct`
- `CommissionPlan`, `PlanStatus`, `CommissionTier`, `CommissionRule`, `BonusRule`
- `CommissionCalculation`, `BonusCalculation`, `AcceleratorCalculation`
- `User`, `UserRole`
- `Dispute`, `DisputeStatus`

---

## Sample Data Summary

Loaded at startup by `SampleDataLoader.java`:

- **6 Users:** 1 admin, 1 manager, 3 sales reps, 1 finance admin
- **6 Deals:** 4 won, 1 open, 1 lost (spread across 3 sales reps)
- **3 Commission Plans:** 2 active (Standard with 4 tiers + Premium), 1 draft
- **4 Commission Calculations:** 1 approved, 1 calculated, 1 paid, 1 disputed
- **1 Dispute:** Opened by Christine Lee on calculation calc-004, assigned to manager John Smith

---

## Architecture Notes

- **No database required.** All repositories use `ConcurrentHashMap` for thread-safe in-memory storage. Data is reset on every application restart. In a production Spring Boot application, these would be replaced with Spring Data JPA interfaces (e.g., `JpaRepository<Deal, String>`).
- **Two security filter chains.** API endpoints (`/api/**`) use stateless JWT authentication. Web pages use stateful form-based login with sessions. This dual-chain pattern is common in applications that serve both an API and a web UI.
- **DTO pattern.** Request DTOs enforce validation constraints, and response DTOs control what data is exposed to clients. The `DtoMapper` component handles conversions between domain models and DTOs.
- **Layered architecture.** Controller -> Service -> Repository. Controllers handle HTTP concerns, services contain business logic, and repositories handle data access. Each layer is injected into the one above it via constructor injection.
