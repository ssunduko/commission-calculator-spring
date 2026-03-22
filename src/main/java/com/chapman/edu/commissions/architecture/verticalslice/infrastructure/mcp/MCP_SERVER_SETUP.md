# MCP Server Setup Guide

This document explains how to connect to the Commission Calculator MCP Server using various MCP clients.

## Overview

The Commission Calculator MCP Server exposes:
- **31 Tools**: Complete CRUD operations for deals, commission plans, disputes, calculations, and currency conversion
- **10 Prompts**: Pre-defined workflows for common tasks
- **12 Resources**: Real-time access to system data and schemas

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

1. **Streamable HTTP Transport** (recommended for Claude Desktop)
   - Modern HTTP-based transport using JSON-RPC over POST
   - Server runs independently — start once, connect from any client
   - Endpoint: `http://localhost:8081/mcp`

2. **STDIO Transport** (alternative for Claude Desktop)
   - Uses standard input/output for communication
   - Claude Desktop launches and manages the server process
   - No web server needed

3. **SSE Transport** (for MCP Inspector and legacy clients)
   - Server-Sent Events for persistent connections
   - Endpoint: `http://localhost:8081/api/mcp/sse`
   - See [SSE_TESTING.md](../../../../../../../SSE_TESTING.md) for details

4. **HTTP/REST Transport** (for API testing)
   - RESTful HTTP endpoints at `/api/mcp/*`
   - Suitable for curl/Postman testing

## Connecting with Claude Desktop

Claude Desktop supports both **Streamable HTTP** (recommended) and **STDIO** transports.

### Step 1: Open the Config File

In Claude Desktop go to **Settings → Developer → Edit Config**.

Or open the file directly:

| OS      | Path                                                        |
|---------|-------------------------------------------------------------|
| Windows | `%APPDATA%\Claude\claude_desktop_config.json`               |
| macOS   | `~/Library/Application Support/Claude/claude_desktop_config.json` |

### Step 2: Add the Server Configuration

#### Option A: Streamable HTTP (Recommended)

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

**Benefits**: Server runs independently, supports multiple clients, no classpath issues, easier debugging.

#### Option B: STDIO (Alternative)

Claude Desktop launches the server process directly:

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

> If you already have other MCP servers configured, merge this entry into the existing `mcpServers` object.

**STDIO flags explained:**
- **stdio=true**: Enables STDIO transport
- **web-application-type=none**: Disables the HTTP web server (not needed for STDIO)
- **logging.pattern.console=**: Disables console logging (required — any stray output breaks the JSON-RPC stream)
- **banner-mode=off**: Disables Spring Boot banner (keeps STDIO clean)
- Use double backslashes (`\\`) in Windows paths, or forward slashes in macOS paths

### Step 3: Fully Quit and Restart Claude Desktop

Don't just close the window — fully quit Claude Desktop (system tray → Quit on Windows, or Cmd+Q on macOS) and relaunch. Claude Desktop only reads the config on startup.

> **Note**: If using Streamable HTTP, make sure the server is running before restarting Claude Desktop.

### Step 4: Verify

After restart, look for the **MCP tools icon** (hammer/wrench) in the bottom-right of the chat input box. Click it to see the 31 available tools, 10 prompts, and 12 resources.

### Troubleshooting Claude Desktop Connection

If the server doesn't appear or fails to connect:

1. **For Streamable HTTP**: Verify the server is running — `curl http://localhost:8081/api/mcp/info`
2. **For STDIO**: Check the JAR path in the config matches your actual JAR location
3. **Check Java version**: Run `java -version` — you need Java 21+
4. **View Claude Desktop logs**: Settings → Developer → View Logs
5. **Test STDIO manually**: Run the command from the config in a terminal to see errors:
   ```bash
   java -Dspring.ai.mcp.server.stdio=true -Dspring.main.web-application-type=none -Dlogging.pattern.console= -Dspring.main.banner-mode=off -jar "C:\Commission Calculator\commission-calculator-spring\target\commission-calculator-0.0.1-SNAPSHOT.jar"
   ```
5. **Rebuild the JAR**: Run `mvn clean package -DskipTests` to ensure you have the latest build

## Available Capabilities

### Tools (31 total)

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

#### Currency Conversion (4 tools — proxied from external MCP server)
- `convertCurrency` - Convert an amount between two currencies using real-time rates
- `getLatestRates` - Fetch latest exchange rates with optional base/symbols filter
- `listSupportedCurrencies` - List all supported currencies with their names
- `getHistoricalRates` - Get historical exchange rates for a specific date

### Prompts (10 pre-defined workflows)

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

8. **convert-deal-currency**
   - Calculate commission for a deal and convert the result to a different currency
   - Parameters: dealId, planId, targetCurrency

9. **multi-currency-commission-report**
   - Generate a commission report with amounts converted to a target currency
   - Parameters: salesRepId, targetCurrency

10. **currency-rate-check**
    - Check current and historical exchange rates for commission planning
    - Parameters: baseCurrency, targetCurrencies, historicalDate (optional)

### Resources (12 data sources)

#### Data Resources
- `deals://all` - All deals in the system
- `deals://active` - Active deals only
- `plans://all` - All commission plans
- `plans://active` - Active plans only
- `disputes://all` - All disputes
- `disputes://open` - Open disputes only
- `calculations://all` - All calculations

#### Currency Resources
- `currency://supported` - List of all supported currencies for conversion
- `currency://rates` - Latest exchange rates (EUR base by default)
- `currency-rates://{baseCurrency}` - Exchange rates for a specific base currency (template)

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
   curl -u admin:admin123 -X POST http://localhost:8081/mcp \
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
