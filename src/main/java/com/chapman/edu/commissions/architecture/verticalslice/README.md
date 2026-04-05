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
│   ├── disputes/       # Dispute management feature
│   └── currency/       # Currency conversion (MCP client to external server)
├── processor/           # STARTUP DEMOS — Showcases Vertical Slice concepts
│   ├── VerticalSliceProcessor.java       # Demonstrates all VS patterns
│   └── VerticalSliceProcessorDemo.java   # CommandLineRunner for startup demos
└── infrastructure/      # Cross-cutting concerns
    ├── config/         # Configuration
    ├── data/           # Data initialization
    ├── exceptions/     # Global exception handling
    └── mcp/            # MCP server (tools, prompts, resources, controllers)
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

### 5. Currency Conversion (`features/currency`)

Provides real-time currency conversion by acting as an **MCP client** that connects to an external MCP server ([currency-mcp.wesbos.com](https://github.com/wesbos/currency-conversion-mcp)).

**How It Works:**
- `CurrencyMcpClientConfig` creates an `McpSyncClient` bean that connects to the remote server via SSE transport
- `CurrencyConversionService` calls the remote MCP tools and also exposes them as local `@Tool` methods
- This means AI agents connected to THIS server can trigger currency conversions that internally call OUT to the external server

**MCP Tools Available (proxied from remote server):**
- `convertCurrency` - Convert an amount between two currencies
- `getLatestRates` - Fetch latest exchange rates with optional base/symbols filter
- `listSupportedCurrencies` - List all supported currencies
- `getHistoricalRates` - Get exchange rates for a specific past date

**REST Endpoints:**
- `POST /api/currency/convert` - Convert currency (`{ "from": "USD", "to": "EUR", "amount": 100 }`)
- `GET /api/currency/rates?base=USD&symbols=EUR,GBP,JPY` - Latest rates
- `GET /api/currency/supported` - List supported currencies
- `GET /api/currency/historical?date=2025-01-15&base=USD` - Historical rates

**Configuration** (`application.properties`):
```properties
app.mcp.currency.base-url=https://currency-mcp.wesbos.com
app.mcp.currency.sse-endpoint=/sse
```

## MCP Server Integration

### What is MCP?

**Model Context Protocol (MCP)** is a protocol that enables AI agents to interact with your application's services through well-defined tools. This application exposes **31 MCP tools** across all services.

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

### Total MCP Tools: 31

- **Deal Management**: 7 tools
- **Commission Plans**: 7 tools
- **Calculations**: 5 tools
- **Disputes**: 8 tools
- **Currency Conversion**: 4 tools (proxied from external MCP server)

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

## Testing the MCP Server

### Prerequisites

- **Java 21+** and **Maven 3.8+**
- **Node.js** (for MCP Inspector via `npx`)
- **Claude Desktop** (for Claude Desktop testing)

Build the application JAR before testing:

```bash
mvn clean package -DskipTests
```

---

### Testing with Claude Desktop

Claude Desktop supports both **Streamable HTTP** (recommended) and **STDIO** transports.

#### Step 1: Open the Claude Desktop Config File

In Claude Desktop go to **Settings → Developer → Edit Config**. This opens (or creates) the config file.

Alternatively, open the file directly:

| OS      | Path                                                        |
|---------|-------------------------------------------------------------|
| Windows | `%APPDATA%\Claude\claude_desktop_config.json`               |
| macOS   | `~/Library/Application Support/Claude/claude_desktop_config.json` |

#### Step 2: Add the Server Configuration

##### Option A: Streamable HTTP (Recommended)

Start the server first (`java -jar target/commission-calculator-0.0.1-SNAPSHOT.jar`), then configure Claude Desktop to connect over HTTP:

```json
{
  "mcpServers": {
    "commission-calculator": {
      "type": "streamable-http",
      "url": "http://localhost:8081/mcp"
    }
  }
}
```

**Benefits**: Server runs independently, supports multiple clients, no classpath issues.

##### Option B: STDIO (Alternative)

Claude Desktop launches the server process directly:

```json
{
  "mcpServers": {
    "commission-calculator": {
      "command": "java",
      "args": [
        "--enable-native-access=ALL-UNNAMED",
        "-Dspring.profiles.active=stdio",
        "-Dspring.main.web-application-type=none",
        "-jar",
        "C:\\Commission Calculator\\commission-calculator-spring\\target\\commission-calculator-0.0.1-SNAPSHOT.jar"
      ]
    }
  },
  "preferences": {
    "coworkScheduledTasksEnabled": true,
    "ccdScheduledTasksEnabled": true,
    "sidebarMode": "chat",
    "coworkWebSearchEnabled": true
  }
}
```

> **Note**: Update the JAR path to match your local project location. Use `\\` on Windows or `/` on macOS. If you already have other MCP servers configured, merge this entry into the existing `mcpServers` object.

**STDIO flags explained:**
- `stdio=true` — enables STDIO transport
- `web-application-type=none` — disables the HTTP web server (not needed for STDIO)
- `logging.pattern.console=` — suppresses console logging to keep STDIO clean
- `banner-mode=off` — disables Spring Boot banner output

#### Step 3: Fully Quit and Restart Claude Desktop

Don't just close the window — fully quit Claude Desktop (system tray → Quit on Windows, or Cmd+Q on macOS) and relaunch it. Claude Desktop only reads the config file on startup.

> **Note**: If using Streamable HTTP, make sure the server is running before restarting Claude Desktop.

#### Step 4: Verify and Use

After restart, look for the **MCP tools icon** (hammer/wrench) in the bottom-right of the chat input box. Click it to see the 31 available tools.

Once connected, all **31 tools**, **10 prompts**, and **12 resources** are available directly in conversation. Try these examples:

```
Use the "analyze-sales-performance" prompt to analyze sales rep REP001 for last month
```

```
Create a new deal titled "Enterprise Software License" with value $50000 for sales rep REP001
```

```
Show me all active commission plans
```

#### Troubleshooting Claude Desktop

| Problem                     | Solution                                                                 |
|-----------------------------|--------------------------------------------------------------------------|
| Server not appearing        | Verify the JAR path exists and matches the config                        |
| Connection fails            | Run the `java` command from config manually in a terminal to see errors  |
| Wrong Java version          | Run `java -version` — must be 21+                                        |
| Stale build                 | Rebuild: `mvn clean package -DskipTests`                                 |
| Check logs                  | Claude Desktop → Settings → Advanced → View Logs                         |

---

### Testing with MCP Inspector

The [MCP Inspector](https://github.com/modelcontextprotocol/inspector) provides a web-based UI for browsing tools, prompts, resources, and executing commands against your MCP server.

#### Method 1: STDIO Mode (No Server Required)

The inspector launches the server directly — no need to start it separately.

```bash
npx @modelcontextprotocol/inspector \
  java \
  -Dspring.ai.mcp.server.stdio=true \
  -Dspring.main.web-application-type=none \
  -Dspring.main.banner-mode=off \
  -Dlogging.level.root=OFF \
  -Dlogging.level.org.springframework=OFF \
  -Dlogging.level.org.hibernate=OFF \
  -Dspring.jpa.show-sql=false \
  -jar "C:\Commission Calculator\commission-calculator-spring\target\commission-calculator-0.0.1-SNAPSHOT.jar"
```

Open the URL printed in the terminal (typically http://localhost:5173).

#### Method 2: Streamable HTTP Mode (Recommended)

Connect the inspector to an already-running server instance.

**Terminal 1 — Start the server:**

```bash
java -jar target/commission-calculator-0.0.1-SNAPSHOT.jar
```

**Terminal 2 — Connect the inspector:**

```bash
npx @modelcontextprotocol/inspector --transport streamable-http http://admin:admin123@localhost:8081/api/mcp/message
```

> **Important**: You must include `--transport streamable-http`. Without it, the inspector treats the URL as a command to spawn and fails with `ENOENT`.

#### Method 3: SSE Mode

Alternative transport using Server-Sent Events (deprecated in favor of Streamable HTTP, but still functional).

**Terminal 1 — Start the server:**

```bash
java -jar target/commission-calculator-0.0.1-SNAPSHOT.jar
```

**Terminal 2 — Connect the inspector:**

```bash
npx @modelcontextprotocol/inspector --transport sse http://admin:admin123@localhost:8081/api/mcp/sse
```

#### What You Can Do in the Inspector

| Tab         | Capability                                               |
|-------------|----------------------------------------------------------|
| Tools       | Browse all 31 tools, view schemas, execute with test data |
| Prompts     | Browse 10 workflow prompts, execute with parameters       |
| Resources   | Read from 10 data resources and schema definitions        |
| Messages    | View raw JSON-RPC request/response traffic for debugging  |

#### Inspector Examples

**List all deals:**
1. Open the inspector UI
2. Navigate to Tools → `getAllDeals`
3. Click "Execute"

**Create a deal:**
1. Navigate to Tools → `createDeal`
2. Enter parameters:
   ```json
   {
     "title": "Test Deal",
     "value": 50000,
     "salesRepId": "REP001"
   }
   ```
3. Click "Execute"

**Run a workflow prompt:**
1. Navigate to Prompts → `create-commission-workflow`
2. Enter parameters:
   ```json
   {
     "dealTitle": "Q1 Enterprise License",
     "dealValue": "100000",
     "salesRepId": "REP001",
     "planId": "plan-1"
   }
   ```
3. Execute and review the multi-step workflow output

#### Inspector Troubleshooting

| Problem                      | Solution                                                  |
|------------------------------|-----------------------------------------------------------|
| `npx: command not found`     | Install Node.js from https://nodejs.org                   |
| Connection refused (HTTP/SSE)| Ensure the server is running on port 8081                 |
| JSON parse errors (STDIO)    | Verify all logging flags are set to suppress console output|
| Port 8081 in use             | Kill existing process or change `server.port` in application.properties |

---

### Quick Testing with curl

For quick command-line verification without the inspector:

```bash
# Check server info
curl -u admin:admin123 http://localhost:8081/api/mcp/info

# List all tools
curl -u admin:admin123 -X POST http://localhost:8081/api/mcp/tools/list \
  -H "Content-Type: application/json" -d '{}'

# Call a tool
curl -u admin:admin123 -X POST http://localhost:8081/api/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"name": "getAllDeals", "arguments": {}}'

# List prompts
curl -u admin:admin123 -X POST http://localhost:8081/api/mcp/prompts/list \
  -H "Content-Type: application/json" -d '{}'

# Read a resource
curl -u admin:admin123 -X POST http://localhost:8081/api/mcp/resources/read \
  -H "Content-Type: application/json" \
  -d '{"uri": "deals://all"}'
```

---

## Feature Flags (Togglz)

Runtime feature flags using [Togglz](https://www.togglz.org/) decouple **deployment from release** — ship code anytime, activate features independently.

### Registered Flags

| Flag | Default | Purpose |
|------|---------|---------|
| `CURRENCY_CONVERSION` | Enabled | Gates all `/api/currency/**` endpoints |
| `BETA_DASHBOARD` | Disabled | Beta dashboard for selected users |
| `ADVANCED_ANALYTICS` | Disabled | Advanced commission analytics |
| `BULK_IMPORT` | Disabled | Bulk deal import functionality |

### Togglz Console

Toggle flags at runtime via the web UI:
```
http://localhost:8081/togglz-console
```

### Activation Strategies

#### 1. Kill Switch (Default)
No strategy needed — simply enable or disable globally.
```
/togglz-console → CURRENCY_CONVERSION → Enabled: false
```
All `/api/currency/**` endpoints immediately return **503 Service Unavailable**.

#### 2. Gradual Rollout (Percentage)
Strategy: **Gradual rollout** (built-in `GradualActivationStrategy`).
Uses consistent hashing on user ID — the same user always gets the same result.
```
/togglz-console → ADVANCED_ANALYTICS → Strategy: Gradual rollout → percentage=10
```
Enables for 10% of users. Increase to 50, then 100 for full rollout.

#### 3. User Targeting (Username)
Strategy: **Username** (built-in `UsernameActivationStrategy`).
```
/togglz-console → BETA_DASHBOARD → Strategy: Username → users=alice,bob
```
Only `alice` and `bob` see the beta dashboard.

#### 4. Time Window (Release Date)
Strategy: **Release date** (built-in `ReleaseDateActivationStrategy`).
```
/togglz-console → BULK_IMPORT → Strategy: Release date → date=2026-12-01
```
Feature auto-enables after December 1, 2026.

#### 5. Server/Region (Custom Strategy)
Custom `RegionActivationStrategy` reads the current region from:
1. `X-Region` HTTP header (per-request override)
2. `APP_REGION` environment variable (server-level default)

```
/togglz-console → CURRENCY_CONVERSION → Strategy: Server/Region → regions=us-east,eu-west
```

**Implementation** (`RegionActivationStrategy.java`):
```java
@Component
public class RegionActivationStrategy implements ActivationStrategy {
    @Override
    public String getId() { return "region"; }

    @Override
    public boolean isActive(FeatureState state, FeatureUser user) {
        String enabledRegions = state.getParameter("regions");    // "us-east,eu-west"
        String currentRegion = resolveCurrentRegion();             // from header or env
        return Arrays.stream(enabledRegions.split(","))
                     .anyMatch(r -> r.trim().equalsIgnoreCase(currentRegion));
    }

    private String resolveCurrentRegion() {
        // 1. X-Region header (per-request)
        // 2. APP_REGION env var (server-level)
    }
}
```

To test: `curl -H "X-Region: eu-west" http://localhost:8081/api/currency/supported`

### Observability

Feature flag state is exposed as **Micrometer metrics** (scrapable by Prometheus):

| Metric | Type | Description |
|--------|------|-------------|
| `feature_flag_state{feature="X"}` | Gauge | 1=enabled, 0=disabled |
| `feature_flag_checked_total{feature="X"}` | Counter | Number of flag checks |

Logs:
- **State changes**: logged at `WARN` level immediately
- **Periodic summary**: logged at `INFO` every 60 seconds
- **Prometheus endpoint**: `http://localhost:8081/actuator/prometheus` → search `feature_flag_*`

### Architecture

```
CurrencyController                    FeatureManager (Togglz)
  │                                        │
  ├─ metrics.recordCheck(flag)             ├─ ActivationStrategy
  ├─ featureManager.isActive(flag)?        │   ├─ (none) = Kill Switch
  │   ├─ YES → call service normally       │   ├─ GradualActivation (%)
  │   └─ NO  → 503 Service Unavailable    │   ├─ UsernameActivation
  │                                        │   ├─ ReleaseDateActivation
  FeatureFlagMetrics                       │   └─ RegionActivation (custom)
  ├─ Gauge: feature_flag_state             │
  ├─ Counter: feature_flag_checked_total   StateRepository
  └─ Scheduled: log changes + summary     (in-memory, JDBC, file, Redis)
```

## Infrastructure

### Configuration (`infrastructure/config`)

- **OpenApiConfig**: Swagger/OpenAPI configuration
- **SecurityConfig**: Security and authentication setup
- **Features**: Feature flag enum with activation strategy configuration
- **RegionActivationStrategy**: Custom region-based feature activation
- **FeatureFlagMetrics**: Observability — Micrometer metrics and structured logging

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

## Processor Demos

The `VerticalSliceProcessor` runs at startup to demonstrate key Vertical Slice concepts:

| Demo | Concept | What It Shows |
|------|---------|---------------|
| **Feature-First Organization** | Package by feature | All code for a feature lives in one package — change one feature, touch one package |
| **Minimal Abstractions** | Concrete classes | No interfaces, no ports — DealController → DealService → DealRepository directly |
| **Cross-Feature Communication** | Direct dependencies | Calculation service injects deal and plan repositories — simple but coupled |
| **Rapid Development** | Low ceremony | 5-6 files to add a feature vs 10+ in Clean Architecture |
| **Full Feature Walkthrough** | End-to-end | Create deal → get plan → calculate commission — all through direct service calls |
| **MCP Server** | AI Agent Integration | 31 @Tool methods expose all features to AI agents via Model Context Protocol |
| **MCP Client — Currency Conversion** | External MCP consumption | Connects to currency-mcp.wesbos.com via SSE, calls remote tools, and re-exposes them as local @Tool methods — demonstrating MCP server chaining |
| **Feature Flags — Togglz** | Runtime feature toggles | Kill switch, gradual rollout (%), user targeting, time window, custom region strategy — with Micrometer metrics and /togglz-console |
| **MCP Sampling** | Server-initiated AI | Server gathers data, asks AI client to reason about it — reverse of normal tool calls |

---

**Built with Spring Boot 3.4.5 | Vertical Slice Architecture | MCP-Enabled | Togglz Feature Flags**
