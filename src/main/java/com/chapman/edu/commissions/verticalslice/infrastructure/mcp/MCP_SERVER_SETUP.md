# MCP Server Setup Guide

This document explains how to connect to the Commission Calculator MCP Server using various MCP clients.

## Overview

The Commission Calculator MCP Server exposes:
- **27 Tools**: Complete CRUD operations for deals, commission plans, disputes, and calculations
- **7 Prompts**: Pre-defined workflows for common tasks
- **10 Resources**: Real-time access to system data and schemas

## Server Configuration

### Running the Server

1. **Build the application:**
   ```bash
   mvn clean package
   ```

2. **Start the server:**
   ```bash
   java -jar target/commission-calculator-0.0.1-SNAPSHOT.jar
   ```

3. **Verify the server is running:**
   ```bash
   curl http://localhost:8081/api/mcp/info
   ```

### Server Endpoints

The MCP server exposes the following endpoints:

- **Base URL**: `http://localhost:8081/api/mcp`
- **Protocol Version**: `2024-11-05`
- **Port**: `8081` (configurable in application.properties)

### Transport Modes

The server supports multiple transport protocols:

1. **STDIO Transport** (for Claude Desktop)
   - Uses standard input/output for communication
   - Required for local MCP client integration
   - No web server needed

2. **HTTP/REST Transport** (for API testing)
   - RESTful HTTP endpoints
   - Requires authentication
   - Suitable for curl/Postman testing

3. **SSE Transport** (for real-time updates)
   - Server-Sent Events for persistent connections
   - Real-time notifications and updates
   - Bidirectional communication over HTTP
   - See [SSE_TESTING.md](../../../../../../../SSE_TESTING.md) for details

## Connecting with Claude Desktop

Claude Desktop requires MCP servers to use STDIO (standard input/output) transport for local communication.

### Configuration File Location

**Windows**: `%APPDATA%\Claude\claude_desktop_config.json`
**macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`

### STDIO Configuration (Required for Claude Desktop)

Add this configuration to your Claude Desktop config file:

```json
{
  "mcpServers": {
    "commission-calculator": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-Dspring.main.web-application-type=none",
        "-Dlogging.pattern.console=",
        "-Dspring.main.banner-mode=off",
        "-jar",
        "C:\\Commission Calculator\\commission-calculator-spring\\target\\commission-calculator-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

**Important Configuration Details:**
- **stdio=true**: Enables STDIO transport for Claude Desktop communication
- **web-application-type=none**: Disables the HTTP web server (not needed for STDIO)
- **logging.pattern.console=**: Disables console logging (required for STDIO)
- **banner-mode=off**: Disables Spring Boot banner (keeps STDIO clean)
- Use double backslashes (`\\`) in Windows paths, or forward slashes in macOS paths

### How to Use

1. Save the configuration file at the location above
2. **Restart Claude Desktop** completely (quit and relaunch)
3. The commission-calculator server will appear in Claude Desktop's MCP servers list
4. You can now use all 27 tools, 7 prompts, and 10 resources directly in conversations

### Troubleshooting Claude Desktop Connection

If the server doesn't appear or fails to connect:

1. **Check the JAR path**: Ensure the path in the config matches your actual JAR location
2. **Check Java version**: Run `java -version` - you need Java 21+
3. **View Claude Desktop logs**: Check Settings → Advanced → View Logs for error messages
4. **Test manually**: Try running the command from the config in a terminal to see errors:
   ```bash
   java -Dspring.ai.mcp.server.stdio=true -Dspring.main.web-application-type=none -Dlogging.pattern.console= -Dspring.main.banner-mode=off -jar "C:\Commission Calculator\commission-calculator-spring\target\commission-calculator-0.0.1-SNAPSHOT.jar"
   ```
5. **Rebuild the JAR**: Run `mvn clean package` to ensure you have the latest build

## Available Capabilities

### Tools (27 total)

#### Deal Management (7 tools)
- `createDeal` - Create a new deal
- `getDeal` - Get deal by ID
- `getAllDeals` - List all deals
- `getDealsBySalesRep` - Get deals by sales rep
- `getDealsByStatus` - Filter deals by status
- `updateDeal` - Update deal information
- `deleteDeal` - Delete a deal

#### Commission Plan Management (7 tools)
- `createCommissionPlan` - Create a new plan
- `getCommissionPlan` - Get plan by ID
- `getAllCommissionPlans` - List all plans
- `getCommissionPlansByStatus` - Filter plans by status
- `activateCommissionPlan` - Activate a plan
- `addRuleToPlan` - Add rules to a plan
- `deleteCommissionPlan` - Delete a plan

#### Dispute Management (8 tools)
- `createDispute` - Create a new dispute
- `getDispute` - Get dispute by ID
- `getAllDisputes` - List all disputes
- `getDisputesBySalesRep` - Get disputes by sales rep
- `getDisputesByStatus` - Filter disputes by status
- `resolveDispute` - Resolve a dispute
- `escalateDispute` - Escalate a dispute
- `deleteDispute` - Delete a dispute

#### Commission Calculation (5 tools)
- `calculateCommission` - Calculate commission for a deal
- `getCommissionCalculation` - Get calculation by ID
- `getAllCommissionCalculations` - List all calculations
- `getCalculationsBySalesRep` - Get calculations by sales rep
- `getCalculationsByDeal` - Get calculations by deal

### Prompts (7 pre-defined workflows)

1. **analyze-sales-performance**
   - Analyze sales performance metrics for a sales rep
   - Parameters: salesRepId, period

2. **create-commission-workflow**
   - Complete workflow to create deal and calculate commission
   - Parameters: dealTitle, dealValue, salesRepId, planId

3. **dispute-resolution**
   - Handle complete dispute resolution process
   - Parameters: calculationId, disputeReason, salesRepId

4. **setup-commission-plan**
   - Create and configure a new commission plan
   - Parameters: planName, currency, baseRate, ruleType

5. **monthly-commission-report**
   - Generate comprehensive monthly report
   - Parameters: salesRepId, month

6. **compare-plans**
   - Compare commission plans for a deal
   - Parameters: dealId, planIds

7. **audit-calculation**
   - Perform detailed audit of a calculation
   - Parameters: calculationId

### Resources (10 data sources)

#### Data Resources
- `deals://all` - All deals in the system
- `deals://active` - Active deals only
- `plans://all` - All commission plans
- `plans://active` - Active plans only
- `disputes://all` - All disputes
- `disputes://open` - Open disputes only
- `calculations://all` - All calculations

#### Schema Resources
- `schema://deal` - JSON schema for Deal entity
- `schema://commission-plan` - JSON schema for Commission Plan
- `schema://dispute` - JSON schema for Dispute

## Testing the Connection

### Using curl (HTTP Endpoints)

1. **Check server info:**
   ```bash
   curl -u admin:admin123 http://localhost:8081/api/mcp/info
   ```

2. **List available tools:**
   ```bash
   curl -u admin:admin123 -X POST http://localhost:8081/api/mcp/tools/list \
     -H "Content-Type: application/json" \
     -d '{}'
   ```

3. **List available prompts:**
   ```bash
   curl -u admin:admin123 -X POST http://localhost:8081/api/mcp/prompts/list \
     -H "Content-Type: application/json" \
     -d '{}'
   ```

4. **List available resources:**
   ```bash
   curl -u admin:admin123 -X POST http://localhost:8081/api/mcp/resources/list \
     -H "Content-Type: application/json" \
     -d '{}'
   ```

5. **Read a resource:**
   ```bash
   curl -u admin:admin123 -X POST http://localhost:8081/api/mcp/resources/read \
     -H "Content-Type: application/json" \
     -d '{"uri": "deals://all"}'
   ```

6. **Call a tool:**
   ```bash
   curl -u admin:admin123 -X POST http://localhost:8081/api/mcp/tools/call \
     -H "Content-Type: application/json" \
     -d '{
       "name": "getAllDeals",
       "arguments": {}
     }'
   ```

### Using SSE (Server-Sent Events)

For real-time communication with persistent connections:

1. **Connect to SSE stream:**
   ```bash
   curl -u admin:admin123 -N http://localhost:8081/api/mcp/sse
   ```

2. **Send MCP messages** (in another terminal):
   ```bash
   curl -u admin:admin123 -X POST http://localhost:8081/api/mcp/message \
     -H "Content-Type: application/json" \
     -d '{
       "jsonrpc": "2.0",
       "id": 1,
       "method": "tools/list",
       "params": {}
     }'
   ```

For complete SSE testing examples, see [SSE_TESTING.md](../../../../../../../SSE_TESTING.md)

### Authentication

The server uses HTTP Basic Authentication:
- **Username**: `admin`
- **Password**: `admin123`

For curl requests, add: `-u admin:admin123`

Example:
```bash
curl -u admin:admin123 -X POST http://localhost:8081/api/mcp/tools/list \
  -H "Content-Type: application/json" \
  -d '{}'
```

## Example Usage with Claude

Once connected, you can ask Claude to use the MCP server:

**Example 1: Using a Prompt**
```
Use the "analyze-sales-performance" prompt to analyze sales rep REP001 for last month
```

**Example 2: Using Tools**
```
Create a new deal titled "Enterprise Software License"
with value $50000 for sales rep REP001
```

**Example 3: Using Resources**
```
Show me all active commission plans
```

## Troubleshooting

### Server won't start
- Check that port 8081 is not in use
- Verify Java 21 is installed: `java -version`
- Check application logs in the console

### Connection refused
- Ensure the server is running
- Verify the port in the configuration matches `server.port` in application.properties
- Check firewall settings

### Authentication errors
- Verify credentials match those in application.properties
- Default: username=`admin`, password=`admin123`

## Configuration Options

### application.properties

```properties
# MCP Server Configuration
spring.ai.mcp.server.name=commission-calculator
spring.ai.mcp.server.version=1.0.0
spring.ai.mcp.server.enabled=true
spring.ai.mcp.server.protocol=SSE
server.port=8081

# Security
spring.security.user.name=admin
spring.security.user.password=admin123
```

## Additional Resources

- **Swagger UI**: http://localhost:8081/swagger-ui/
- **H2 Console**: http://localhost:8081/h2-console
- **Health Check**: http://localhost:8081/actuator/health
- **API Documentation**: http://localhost:8081/api-docs

## Support

For issues or questions:
1. Check the server logs
2. Review the MCP protocol documentation
3. Verify all endpoints are accessible via curl
