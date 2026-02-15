# Commission Calculator MCP Server

A comprehensive Model Context Protocol (MCP) server for managing sales commissions, deals, commission plans, disputes, and calculations.

## Features

- **27 MCP Tools**: Complete CRUD operations for deals, plans, disputes, and calculations
- **7 Workflow Prompts**: Pre-defined workflows for common commission management tasks
- **10 Data Resources**: Real-time access to system data and schemas
- **Multiple Transport Modes**: STDIO, HTTP/REST, and SSE support
- **HTTP Basic Authentication**: All API endpoints require authentication (admin:admin123)

## Quick Start

### 1. Build the Project

```bash
mvn clean package
```

### 2. Choose Your Access Method

#### Option A: Claude Desktop (STDIO Mode)

Best for: AI-powered commission management through Claude Desktop

1. Ensure Claude Desktop configuration is set at:
   ```
   C:\Users\ssund\AppData\Roaming\Claude\claude_desktop_config.json
   ```

2. Restart Claude Desktop

3. Use natural language to interact with your commission data

**Example**:
```
Show me all deals for sales rep REP001
Calculate commission for deal XYZ using the standard plan
Create a dispute for calculation ABC
```

#### Option B: MCP Inspector (Development & Testing)

Best for: Testing tools, debugging, and exploring the MCP server

**STDIO Mode** (Inspector launches server):
```bash
.\inspect-stdio.bat
```

**SSE Mode** (Connect to running server):
```bash
# Terminal 1: Start server
java -jar target\commission-calculator-0.0.1-SNAPSHOT.jar

# Terminal 2: Connect inspector
.\inspect-sse.bat
```

Opens web UI at http://localhost:5173 to explore and test all tools.

#### Option C: Direct HTTP/SSE (Integration)

Best for: Integrating with custom applications

**HTTP REST API**:
```bash
# Start server
java -jar target\commission-calculator-0.0.1-SNAPSHOT.jar

# Test endpoints (authentication required)
curl -u admin:admin123 -X POST http://localhost:8081/api/mcp/tools/list \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Server-Sent Events (SSE)**:
```bash
# Connect to SSE stream (authentication required)
curl -u admin:admin123 -N http://localhost:8081/api/mcp/sse

# Send messages (in another terminal)
curl -u admin:admin123 -X POST http://localhost:8081/api/mcp/message \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/list","params":{}}'
```

## Available Tools (27)

### Deal Management (7 tools)
- `createDeal` - Create a new deal
- `getDeal` - Get deal by ID
- `getAllDeals` - List all deals
- `getDealsBySalesRep` - Get deals by sales representative
- `getDealsByStatus` - Filter deals by status
- `updateDeal` - Update deal information
- `deleteDeal` - Delete a deal

### Commission Plan Management (7 tools)
- `createCommissionPlan` - Create a new commission plan
- `getCommissionPlan` - Get plan by ID
- `getAllCommissionPlans` - List all plans
- `getCommissionPlansByStatus` - Filter plans by status
- `activateCommissionPlan` - Activate a plan
- `addRuleToPlan` - Add commission rules to a plan
- `deleteCommissionPlan` - Delete a plan

### Dispute Management (8 tools)
- `createDispute` - Create a new commission dispute
- `getDispute` - Get dispute by ID
- `getAllDisputes` - List all disputes
- `getDisputesBySalesRep` - Get disputes by sales rep
- `getDisputesByStatus` - Filter disputes by status
- `resolveDispute` - Resolve a dispute
- `escalateDispute` - Escalate a dispute
- `deleteDispute` - Delete a dispute

### Commission Calculation (5 tools)
- `calculateCommission` - Calculate commission for a deal
- `getCommissionCalculation` - Get calculation by ID
- `getAllCommissionCalculations` - List all calculations
- `getCalculationsBySalesRep` - Get calculations by sales rep
- `getCalculationsByDeal` - Get calculations for a specific deal

## Workflow Prompts (7)

1. **analyze-sales-performance** - Analyze sales metrics for a representative
2. **create-commission-workflow** - Complete workflow from deal to commission
3. **dispute-resolution** - Handle dispute resolution process
4. **setup-commission-plan** - Create and configure a new plan
5. **monthly-commission-report** - Generate comprehensive monthly report
6. **compare-plans** - Compare commission plans for a deal
7. **audit-calculation** - Perform detailed calculation audit

## Data Resources (10)

### Data Access
- `deals://all` - All deals in the system
- `deals://active` - Active deals only
- `plans://all` - All commission plans
- `plans://active` - Active plans only
- `disputes://all` - All disputes
- `disputes://open` - Open disputes only
- `calculations://all` - All commission calculations

### Schema Information
- `schema://deal` - Deal entity JSON schema
- `schema://commission-plan` - Commission Plan schema
- `schema://dispute` - Dispute schema

## Documentation

- **[MCP_INSPECTOR_SETUP.md](infrastructure/mcp/MCP_INSPECTOR_SETUP.md)** - Complete guide to using npx inspector
- **[SSE_TESTING.md](SSE_TESTING.md)** - SSE endpoint testing guide
- **[CLAUDE_DESKTOP_SETUP.md](CLAUDE_DESKTOP_SETUP.md)** - Claude Desktop integration guide
- **[MCP_SERVER_SETUP.md](infrastructure/mcp/MCP_SERVER_SETUP.md)** - General MCP server setup

## Architecture

### Technology Stack
- **Framework**: Spring Boot 3.4.5
- **MCP**: Spring AI 1.1.0
- **Database**: H2 (in-memory)
- **ORM**: Hibernate 6.6.13
- **Java**: 21+

### Project Structure
```
commission-calculator-spring/
├── src/main/java/com/chapman/edu/commissions/
│   └── verticalslice/
│       ├── domain/              # Domain entities
│       ├── features/            # Feature-based organization
│       │   ├── deals/
│       │   ├── plans/
│       │   ├── disputes/
│       │   └── calculations/
│       └── infrastructure/
│           ├── mcp/             # MCP server implementation
│           │   ├── McpCommissionTools.java
│           │   ├── McpPrompts.java
│           │   ├── McpResources.java
│           │   ├── McpProtocolController.java
│           │   └── McpSseController.java
│           ├── config/          # Configuration
│           └── data/            # Data initialization
└── docs/                        # Documentation
```

## Sample Data

The server initializes with sample data:
- 2 Commission Plans (Standard and Premium)
- 6 Deals (various statuses and sales reps)
- 4 Commission Calculations
- 2 Disputes

## Configuration

### Server Ports
- **HTTP/SSE**: 8081
- **H2 Console**: 8081/h2-console

### Security
- All API endpoints (`/api/**`): Basic Auth (admin/admin123)
- MCP endpoints (`/api/mcp/**`): Basic Auth (admin/admin123)
- H2 Console: No authentication (development only)

### Database
- **Type**: H2 in-memory
- **JDBC URL**: `jdbc:h2:mem:commissiondb`
- **Username**: sa
- **Password**: (empty)

## Development

### Running Tests
```bash
mvn test
```

### Running in STDIO Mode (for Claude Desktop)
```bash
java -Dspring.ai.mcp.server.stdio=true \
     -Dspring.main.web-application-type=none \
     -Dspring.main.banner-mode=off \
     -Dlogging.level.root=OFF \
     -jar target/commission-calculator-0.0.1-SNAPSHOT.jar
```

### Running in HTTP Mode
```bash
java -jar target/commission-calculator-0.0.1-SNAPSHOT.jar
```

Access:
- Swagger UI: http://localhost:8081/swagger-ui/
- H2 Console: http://localhost:8081/h2-console
- API Docs: http://localhost:8081/api-docs
- Health: http://localhost:8081/actuator/health

## Troubleshooting

### Port Already in Use
```bash
# Windows
wmic process where "name='java.exe' and CommandLine like '%commission-calculator%'" delete

# Then restart the server
```

### Inspector Not Connecting
1. Ensure Node.js is installed: `node --version`
2. Verify server is running: `curl http://localhost:8081/actuator/health`
3. Check no firewall is blocking port 8081

### STDIO Mode Issues
- Ensure all logging is suppressed with the flags shown above
- No `System.out.println()` statements in the code
- Banner mode is disabled

## Contributing

This is a demonstration project for Spring AI MCP server implementation.

## License

Educational/Demonstration purposes.

## Resources

- [MCP Specification](https://spec.modelcontextprotocol.io/)
- [Spring AI MCP Documentation](https://docs.spring.io/spring-ai/reference/api/mcp/)
- [MCP Inspector](https://github.com/modelcontextprotocol/inspector)
- [Claude Desktop](https://claude.ai/download)
