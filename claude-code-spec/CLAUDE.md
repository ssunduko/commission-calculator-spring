# CLAUDE.md — Commission Calculator (Vertical Slice Architecture)

## Project Overview

Commission Calculator is a Spring Boot application for managing sales commissions. It uses **Vertical Slice Architecture** where each feature is a self-contained package. The system is both human-accessible (REST API) and AI-accessible (MCP Server with 31 tools, 10 prompts, 12 resources).

## Architecture: Vertical Slice

All code is organized by **feature**, not by technical layer. Each feature owns its controller, service, repository, and DTOs in a single package.

```
src/main/java/com/chapman/edu/commissions/architecture/verticalslice/
├── domain/                  # Shared domain entities and enums
├── features/                # Feature slices (one package per feature)
│   ├── deals/               # Deal management (7 files)
│   ├── plans/               # Commission plan management (7 files)
│   ├── calculations/        # Commission calculation (5 files)
│   ├── disputes/            # Dispute management (6 files)
│   └── currency/            # Currency conversion — MCP client (8 files)
├── infrastructure/          # Cross-cutting concerns
│   ├── config/              # OpenAPI, Security configs
│   ├── data/                # DataInitializer, UserRepository
│   ├── exceptions/          # Global exception handling
│   └── mcp/                 # MCP server (tools facade, prompts, resources, controllers)
└── processor/               # Startup demos (VerticalSliceProcessor)
```

## Golden Rules

1. **One feature = one package.** Never scatter a feature across multiple packages.
2. **No interfaces between layers.** Services are concrete classes — DealController → DealService → DealRepository.
3. **Java records for DTOs.** Request records include a `validate()` method. Response records include a `static from(Entity)` factory.
4. **Every service operation gets an @Tool.** All business logic is AI-accessible via MCP.
5. **Shared domain entities live in `domain/`.** Feature-specific logic stays in the feature package.
6. **Exceptions use ResourceNotFoundException (404) or ValidationException (400).**

## Adding a New Feature

Follow the pattern in `features/deals/`. Generate these files in `features/{feature-name}/`:

| File | Purpose | Pattern |
|------|---------|---------|
| `{Entity}.java` | JPA entity | `@Entity`, UUID ID, `@Data`, `@NoArgsConstructor` |
| `{Entity}Status.java` | Lifecycle enum (if needed) | Display name, `toString()` |
| `{Entity}Repository.java` | Data access | `extends JpaRepository<Entity, String>` |
| `Create{Entity}Request.java` | Input DTO | `record` with `validate()` |
| `Update{Entity}Request.java` | Update DTO (if needed) | `record` with nullable fields |
| `{Entity}Response.java` | Output DTO | `record` with `static from(Entity)` |
| `{Entity}Service.java` | Business logic | `@Service`, constructor injection |
| `{Entity}Controller.java` | REST API | `@RestController`, `/api/{entities}` |

Then add @Tool wrapper methods to `infrastructure/mcp/McpCommissionTools.java`.

## Conventions

### Entity Pattern
```java
@Entity
@Table(name = "entities")
@Data
@NoArgsConstructor
public class MyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    // Fields...
    
    public MyEntity(/* essential fields */) { /* constructor */ }
}
```

### Request DTO Pattern
```java
public record CreateMyEntityRequest(
    String name,
    BigDecimal value
) {
    public void validate() {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name is required");
    }
}
```

### Response DTO Pattern
```java
public record MyEntityResponse(
    String id,
    String name,
    BigDecimal value,
    MyStatus status
) {
    public static MyEntityResponse from(MyEntity entity) {
        return new MyEntityResponse(
            entity.getId(), entity.getName(),
            entity.getValue(), entity.getStatus());
    }
}
```

### Service Pattern
```java
@Service
public class MyEntityService {
    private final MyEntityRepository repository;
 
    public MyEntityService(MyEntityRepository repository) {
        this.repository = repository;
    }
 
    public MyEntityResponse create(CreateMyEntityRequest request) {
        request.validate();
        MyEntity entity = new MyEntity(request.name(), request.value());
        return MyEntityResponse.from(repository.save(entity));
    }
 
    public MyEntityResponse getById(String id) {
        MyEntity entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("MyEntity", id));
        return MyEntityResponse.from(entity);
    }
}
```

### Controller Pattern
```java
@RestController
@RequestMapping("/api/my-entities")
public class MyEntityController {
    private final MyEntityService service;
 
    public MyEntityController(MyEntityService service) {
        this.service = service;
    }
 
    @PostMapping
    public ResponseEntity<MyEntityResponse> create(@RequestBody CreateMyEntityRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<MyEntityResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.getById(id));
    }
}
```

### MCP Tool Pattern
```java
// Add to McpCommissionTools.java
@Tool(name = "createMyEntity",
      description = "Create a new entity with name and value. Returns the created entity details.")
public MyEntityResponse createMyEntity(CreateMyEntityRequest request) {
    return myEntityService.create(request);
}
```

## Technology Stack

- Java 21, Spring Boot 3.4.5, Spring Data JPA, Spring Security
- Spring AI (MCP server support, @Tool annotations)
- H2 Database (development), Lombok, SpringDoc OpenAPI
- MCP transports: stdio, Streamable HTTP, SSE

## Testing

- Unit tests: Mockito-based service tests per feature
- Integration tests: MCP tool invocation tests
- Run: `mvn test -Dtest=*ServiceTest`

## MCP Server

- 31 tools across 5 feature categories + currency
- 10 prompts for common workflows
- 12 resources for data access
- Transports: stdio (local), Streamable HTTP (remote), SSE (legacy)
- Auth: HTTP Basic (admin / admin123)

## Commands

```bash
mvn clean package -DskipTests          # Build
mvn spring-boot:run                     # Run (port 8081)
mvn test                                # Test
```

## Key URLs (when running)

- Swagger UI: http://localhost:8081/swagger-ui/
- H2 Console: http://localhost:8081/h2-console
- MCP Info: http://localhost:8081/api/mcp/info
- Health: http://localhost:8081/actuator/health