# Commission Calculator - Vertical Slice Architecture

## Overview

This is a Spring Boot application implementing a commission calculator system using **Vertical Slice Architecture**. The application manages deals, commission plans, calculations, and disputes with support for AI agent integration through MCP (Model Context Protocol) tools.

## Architecture

### Vertical Slice Architecture

This application follows the **Vertical Slice Architecture** pattern, where each feature is organized as a complete vertical slice containing all the layers needed for that feature:

```
verticalslice/
├── domain/              # Domain models and enums
├── features/            # Feature slices (vertical slices)
│   ├── deals/          # Deal management feature
│   ├── plans/          # Commission plan feature
│   ├── calculations/   # Commission calculation feature
│   └── disputes/       # Dispute management feature
└── infrastructure/      # Cross-cutting concerns
    ├── config/         # Configuration
    ├── data/           # Data initialization
    └── exceptions/     # Global exception handling
```

Each feature slice contains:
- **Controller**: REST API endpoints
- **Service**: Business logic and MCP tools
- **Repository**: Data access
- **DTOs**: Request/Response objects
- **Domain Models**: Related to that feature

### Benefits of Vertical Slice Architecture

1. **Feature Cohesion**: Everything related to a feature is in one place
2. **Easy Navigation**: Developers can quickly find all code for a feature
3. **Independent Development**: Features can be developed independently
4. **Clear Boundaries**: Reduces coupling between features
5. **Easier Testing**: Each slice can be tested in isolation

## Domain Model

### Core Entities

#### Deal
Represents a sales deal that will generate commissions.
- **Fields**: id, title, value, salesRepId, status, closeDate, products
- **Statuses**: OPEN, WON, LOST, CANCELLED

#### CommissionPlan
Defines commission rules and calculation methods.
- **Fields**: id, name, currency, status, effectiveStartDate, effectiveEndDate, rules, tiers
- **Statuses**: DRAFT, ACTIVE, INACTIVE, ARCHIVED

#### CommissionRule
Rules within a commission plan that define how commissions are calculated.
- **Fields**: id, name, description, type, rate, priority, conditions
- **Types**: PERCENTAGE, TIERED, FLAT, ACCELERATOR

#### CommissionCalculation
Result of calculating commission for a deal.
- **Fields**: id, dealId, salesRepId, planId, baseCommission, adjustments, finalAmount
- **Statuses**: DRAFT, PENDING, APPROVED, PAID, CANCELLED

#### Dispute
Dispute raised by sales reps regarding commission calculations.
- **Fields**: id, calculationId, salesRepId, title, description, status, escalated, resolution
- **Statuses**: INITIATED, UNDER_REVIEW, ADDITIONAL_INFO_REQUESTED, ESCALATED, APPROVED, REJECTED, RESOLVED, CANCELLED

## Features

### 1. Deal Management (`features/deals`)

Manages sales deals and their lifecycle.

**Services:**
- Create new deals
- Update deal status and information
- Retrieve deals by sales rep or status
- Delete deals

**MCP Tools Available:**
- `createDeal` - Create a new deal
- `getDeal` - Get deal by ID
- `getAllDeals` - Get all deals
- `getDealsBySalesRep` - Filter deals by sales rep
- `getDealsByStatus` - Filter deals by status
- `updateDeal` - Update deal information
- `deleteDeal` - Remove a deal

**REST Endpoints:**
- `POST /api/deals` - Create deal
- `GET /api/deals/{id}` - Get deal
- `GET /api/deals` - List all deals
- `GET /api/deals/rep/{salesRepId}` - Get deals by sales rep
- `GET /api/deals/status/{status}` - Get deals by status
- `PUT /api/deals/{id}` - Update deal
- `DELETE /api/deals/{id}` - Delete deal

### 2. Commission Plan Management (`features/plans`)

Manages commission plans and their rules.

**Services:**
- Create and activate commission plans
- Add rules to plans (percentage, tiered, accelerators)
- Retrieve plans by status
- Delete plans

**MCP Tools Available:**
- `createCommissionPlan` - Create a new commission plan
- `getCommissionPlan` - Get plan by ID
- `getAllCommissionPlans` - Get all plans
- `getCommissionPlansByStatus` - Filter plans by status
- `activateCommissionPlan` - Activate a plan
- `addRuleToPlan` - Add commission rule to plan
- `deleteCommissionPlan` - Remove a plan

**REST Endpoints:**
- `POST /api/plans` - Create plan
- `GET /api/plans/{id}` - Get plan
- `GET /api/plans` - List all plans
- `GET /api/plans/status/{status}` - Get plans by status
- `POST /api/plans/{id}/activate` - Activate plan
- `POST /api/plans/{id}/rules` - Add rule to plan
- `DELETE /api/plans/{id}` - Delete plan

### 3. Commission Calculation (`features/calculations`)

Calculates commissions based on deals and plans.

**Services:**
- Calculate commission for a deal using a plan
- Retrieve calculations by deal or sales rep
- Manage calculation lifecycle

**MCP Tools Available:**
- `calculateCommission` - Calculate commission for a deal
- `getCommissionCalculation` - Get calculation by ID
- `getAllCommissionCalculations` - Get all calculations
- `getCalculationsByDeal` - Filter by deal
- `getCalculationsBySalesRep` - Filter by sales rep

**REST Endpoints:**
- `POST /api/calculations` - Calculate commission
- `GET /api/calculations/{id}` - Get calculation
- `GET /api/calculations` - List all calculations
- `GET /api/calculations/deal/{dealId}` - Get calculations by deal
- `GET /api/calculations/rep/{salesRepId}` - Get calculations by sales rep

### 4. Dispute Management (`features/disputes`)

Manages disputes raised by sales representatives.

**Services:**
- Create disputes for commission calculations
- Resolve disputes (approve/reject)
- Escalate disputes to management
- Track dispute status

**MCP Tools Available:**
- `createDispute` - Create a new dispute
- `getDispute` - Get dispute by ID
- `getAllDisputes` - Get all disputes
- `getDisputesBySalesRep` - Filter by sales rep
- `getDisputesByStatus` - Filter by status
- `resolveDispute` - Resolve a dispute
- `escalateDispute` - Escalate dispute
- `deleteDispute` - Remove a dispute

**REST Endpoints:**
- `POST /api/disputes` - Create dispute
- `GET /api/disputes/{id}` - Get dispute
- `GET /api/disputes` - List all disputes
- `GET /api/disputes/rep/{salesRepId}` - Get disputes by sales rep
- `GET /api/disputes/status/{status}` - Get disputes by status
- `POST /api/disputes/{id}/resolve` - Resolve dispute
- `POST /api/disputes/{id}/escalate` - Escalate dispute
- `DELETE /api/disputes/{id}` - Delete dispute

## MCP Server Integration

### What is MCP?

**Model Context Protocol (MCP)** is a protocol that enables AI agents to interact with your application's services through well-defined tools. This application exposes **27 MCP tools** across all services.

### MCP Tools Architecture

All service methods are annotated with `@Tool` from Spring AI, making them automatically available as MCP tools:

```java
@Service
public class DealService {

    @Tool(name = "createDeal",
          description = "Create a new deal with title, value, and sales rep ID.")
    public DealResponse createDeal(CreateDealRequest request) {
        // Implementation
    }
}
```

### Total MCP Tools: 27

- **Deal Management**: 7 tools
- **Commission Plans**: 7 tools
- **Calculations**: 5 tools
- **Disputes**: 8 tools

### Using MCP Tools

MCP tools can be invoked by AI agents through the Spring AI MCP server. Each tool:

1. **Has a descriptive name** - Clear identification (e.g., "createDeal")
2. **Has a description** - Explains what the tool does and what parameters it needs
3. **Validates input** - Request objects validate before processing
4. **Returns structured data** - Response DTOs with consistent format
5. **Handles exceptions** - Global exception handling for errors

### Example MCP Tool Usage

An AI agent can:
1. Create a deal: `createDeal(title="Enterprise Deal", value=250000, salesRepId="REP001")`
2. Create a commission plan: `createCommissionPlan(name="Q1 2024 Plan", currency="USD")`
3. Add rules to the plan: `addRuleToPlan(planId="...", name="Base", rate=5.0)`
4. Calculate commission: `calculateCommission(dealId="...", planId="...")`
5. Create a dispute if needed: `createDispute(calculationId="...", title="Error", description="...")`

## Infrastructure

### Configuration (`infrastructure/config`)

- **OpenApiConfig**: Swagger/OpenAPI configuration
- **SecurityConfig**: Security and authentication setup

### Data Initialization (`infrastructure/data`)

- **DataInitializer**: Seeds initial data for development

### Exception Handling (`infrastructure/exceptions`)

Global exception handling with custom exceptions:
- **ResourceNotFoundException**: When entities are not found (404)
- **ValidationException**: When validation fails (400)
- **GlobalExceptionHandler**: Handles all exceptions globally

## Technology Stack

- **Spring Boot 3.4.5**
- **Spring Data JPA** - Data persistence
- **Spring Security** - Authentication/Authorization
- **Spring AI** - MCP server support
- **H2 Database** - In-memory database for development
- **Lombok** - Reduces boilerplate code
- **SpringDoc OpenAPI** - API documentation
- **JUnit 5 & Mockito** - Testing

## API Documentation

### Swagger UI

Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui/
```

### OpenAPI Specification

View the raw API specification at:
```
http://localhost:8080/api-docs
```

## Database

### H2 Console

Access the H2 database console at:
```
http://localhost:8080/h2-console
```

**Connection Details:**
- JDBC URL: `jdbc:h2:mem:commissiondbtwo`
- Username: `sa`
- Password: (empty)

## Security

Default credentials for development:
- **Username**: `admin`
- **Password**: `admin123`

## Running the Application

### Prerequisites
- Java 21+
- Maven 3.8+

### Build
```bash
mvn clean package
```

### Run
```bash
mvn spring-boot:run
```

The application will start on port 8080.

## Testing

### Unit Tests

Each service has comprehensive unit tests using Mockito:
```bash
mvn test -Dtest=*ServiceTest
```

### Integration Tests

MCP integration tests verify that MCP tools work correctly:
```bash
mvn test -Dtest=*McpIntegrationTest
```

**Note**: Integration tests require fixing compatibility issues with enums and response DTOs.

## Development Workflow

### Adding a New Feature

1. **Create feature package** under `features/`
2. **Define domain model** in `domain/`
3. **Create repository** interface extending JpaRepository
4. **Create service** with business logic
5. **Add @Tool annotations** for MCP support
6. **Create controller** with REST endpoints
7. **Define DTOs** for requests/responses
8. **Write tests** (unit and integration)

### Example: Adding a "Products" Feature

```
features/products/
├── Product.java (domain model)
├── ProductRepository.java
├── ProductService.java (with @Tool annotations)
├── ProductController.java
├── CreateProductRequest.java
├── UpdateProductRequest.java
└── ProductResponse.java
```

## Best Practices

### 1. Vertical Slice Organization
- Keep all feature code together
- Minimize dependencies between slices
- Use clear package naming

### 2. MCP Tool Design
- Use descriptive tool names
- Provide clear descriptions
- Validate all inputs
- Return structured responses
- Handle errors gracefully

### 3. DTOs
- Use records for immutability
- Include validation annotations
- Separate request/response DTOs
- Provide factory methods (from domain)

### 4. Exception Handling
- Use custom exceptions
- Provide meaningful error messages
- Return appropriate HTTP status codes
- Log errors appropriately

### 5. Testing
- Write unit tests for services
- Mock dependencies
- Test edge cases
- Integration tests for workflows

## Common Workflows

### 1. Commission Calculation Workflow

```
1. Create Deal → createDeal()
2. Create Commission Plan → createCommissionPlan()
3. Add Rules to Plan → addRuleToPlan()
4. Activate Plan → activateCommissionPlan()
5. Calculate Commission → calculateCommission()
6. Review Calculation → getCommissionCalculation()
```

### 2. Dispute Resolution Workflow

```
1. Identify Issue → getCommissionCalculation()
2. Create Dispute → createDispute()
3. Review Dispute → getDispute()
4. Escalate if Needed → escalateDispute()
5. Resolve Dispute → resolveDispute()
```

### 3. Sales Rep Commission Report

```
1. Get All Rep Deals → getDealsBySalesRep()
2. Get Rep Calculations → getCalculationsBySalesRep()
3. Check Disputes → getDisputesBySalesRep()
4. Generate Report
```

## Monitoring and Observability

### Health Endpoint
```
http://localhost:8080/actuator/health
```

### Metrics
Prometheus metrics available at:
```
http://localhost:8080/actuator/prometheus
```

## Future Enhancements

### Planned Features
- [ ] Real-time notifications for disputes
- [ ] Advanced commission rule engine
- [ ] Multi-currency support enhancements
- [ ] Bulk calculation processing
- [ ] Commission forecasting
- [ ] Integration with external CRM systems
- [ ] Role-based access control refinements
- [ ] Audit logging for all operations

### MCP Enhancements
- [ ] Streaming support for large datasets
- [ ] Batch operations via MCP tools
- [ ] Complex query tools
- [ ] Report generation tools
- [ ] Analytics and insights tools

## Contributing

### Code Style
- Follow Spring Boot best practices
- Use Lombok to reduce boilerplate
- Write descriptive method names
- Add JavaDoc for public APIs
- Keep methods small and focused

### Commit Messages
```
feat: Add new feature
fix: Bug fix
docs: Documentation update
test: Add or update tests
refactor: Code refactoring
```

## License

This project is for educational and demonstration purposes.

## Contact and Support

For questions or issues:
1. Check the Swagger documentation
2. Review the test cases for examples
3. Examine existing features as templates

---

**Built with Spring Boot 3.4.5 | Vertical Slice Architecture | MCP-Enabled**
