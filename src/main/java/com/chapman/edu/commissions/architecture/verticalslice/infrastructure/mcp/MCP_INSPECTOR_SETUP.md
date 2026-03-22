# MCP Inspector Setup Guide

## Overview

The MCP Inspector is a developer tool that allows you to test and interact with your MCP server. It provides a web-based UI to explore tools, prompts, resources, and execute commands.

## Prerequisites

- Node.js installed (for `npx` command)
- MCP server built: `mvn clean package`

## Method 1: STDIO Mode (Local Testing)

Use this method to launch the server directly through the inspector.

### Command

```bash
npx @modelcontextprotocol/inspector java -Dspring.ai.mcp.server.stdio=true -Dspring.main.web-application-type=none -Dspring.main.banner-mode=off -Dlogging.level.root=OFF -Dlogging.level.org.springframework=OFF -Dlogging.level.org.hibernate=OFF -Dspring.jpa.show-sql=false -jar "C:\Commission Calculator\commission-calculator-spring\target\commission-calculator-0.0.1-SNAPSHOT.jar"
```

### What This Does

1. The inspector launches your MCP server in STDIO mode
2. Provides a web interface (usually at http://localhost:5173)
3. Allows you to:
   - Browse all 31 tools
   - Test tool execution
   - View prompts and resources
   - See real-time communication

### Usage

1. Run the command above
2. Open the URL provided (usually http://localhost:5173)
3. Interact with your MCP server through the web UI

## Method 2: Streamable HTTP Mode (Recommended for Running Server)

**Recommended**: Use this method for the best experience with a running server.

### Step 1: Start the Server

```bash
cd "C:\Commission Calculator\commission-calculator-spring"
java -jar target/commission-calculator-0.0.1-SNAPSHOT.jar
```

### Step 2: Connect Inspector via Streamable HTTP

```bash
# Include authentication in the URL
npx @modelcontextprotocol/inspector --transport streamable-http http://admin:admin123@localhost:8081/api/mcp/message
```

> **Important**: The `--transport streamable-http` flag is required. Without it, the inspector tries to spawn the URL as a command and fails with `ENOENT`.

### Benefits

- Modern HTTP-based transport
- Direct request/response model
- Full authentication support
- Better error handling than SSE
- Works with standard HTTP clients

## Method 3: SSE Mode (Alternative for Running Server)

Use this if you prefer Server-Sent Events transport.

### Step 1: Start the Server

```bash
cd "C:\Commission Calculator\commission-calculator-spring"
java -jar target/commission-calculator-0.0.1-SNAPSHOT.jar
```

### Step 2: Connect Inspector via SSE

```bash
# Include authentication in the URL
npx @modelcontextprotocol/inspector --transport sse http://admin:admin123@localhost:8081/api/mcp/sse
```

### Benefits

- Server runs independently
- Real-time streaming updates
- Multiple clients can connect simultaneously
- Server state persists between inspector sessions

**Note**: SSE transport is deprecated in favor of Streamable HTTP, but still functional.

## Method 4: Direct HTTP Testing (Command Line)

For quick command-line testing without the full inspector UI:

```bash
# Using curl with authentication
curl -u admin:admin123 -X POST http://localhost:8081/mcp \
  -H "Content-Type: application/json" \
  -d @test-tools-list.json
```

## Quick Start Scripts

### Windows PowerShell Script: `inspect-stdio.ps1`

```powershell
# Launch inspector with STDIO transport
npx @modelcontextprotocol/inspector `
  java `
  -Dspring.ai.mcp.server.stdio=true `
  -Dspring.main.web-application-type=none `
  -Dspring.main.banner-mode=off `
  -Dlogging.level.root=OFF `
  -Dlogging.level.org.springframework=OFF `
  -Dlogging.level.org.hibernate=OFF `
  -Dspring.jpa.show-sql=false `
  -jar "$PSScriptRoot\target\commission-calculator-0.0.1-SNAPSHOT.jar"
```

### Windows Batch Script: `inspect-stdio.bat`

```batch
@echo off
npx @modelcontextprotocol/inspector java -Dspring.ai.mcp.server.stdio=true -Dspring.main.web-application-type=none -Dspring.main.banner-mode=off -Dlogging.level.root=OFF -Dlogging.level.org.springframework=OFF -Dlogging.level.org.hibernate=OFF -Dspring.jpa.show-sql=false -jar "%~dp0target\commission-calculator-0.0.1-SNAPSHOT.jar"
```

### Streamable HTTP Connection Script: `inspect-http.bat`

```batch
@echo off
echo Starting MCP Inspector with Streamable HTTP transport...
echo Make sure the server is running on port 8081
echo.
npx @modelcontextprotocol/inspector --transport streamable-http http://admin:admin123@localhost:8081/api/mcp/message
```

### SSE Connection Script: `inspect-sse.bat`

```batch
@echo off
echo Starting MCP Inspector with SSE transport...
echo Make sure the server is running on port 8081
echo.
npx @modelcontextprotocol/inspector --transport sse http://admin:admin123@localhost:8081/api/mcp/sse
```

## Troubleshooting

### Inspector Not Starting

**Error**: `npx: command not found`
**Solution**: Install Node.js from https://nodejs.org

### Connection Refused

**Error**: Connection refused when using SSE mode
**Solution**:
1. Ensure server is running: `java -jar target/commission-calculator-0.0.1-SNAPSHOT.jar`
2. Check port 8081 is available: `netstat -ano | findstr :8081`

### STDIO Output Issues

**Error**: JSON parsing errors in STDIO mode
**Solution**: Ensure all console output is suppressed with the logging flags shown above

### Port Already in Use

**Error**: Port 8081 already in use
**Solution**:
```bash
# Find and kill the process
wmic process where "name='java.exe' and CommandLine like '%commission-calculator%'" delete
```

## What You Can Do with the Inspector

### Explore Tools

- View all 27 available tools
- See tool schemas and descriptions
- Test tool execution with sample data
- View tool responses in real-time

### Test Prompts

- Browse the 10 workflow prompts
- Execute prompts with parameters
- See prompt templates and expected arguments

### Access Resources

- Read from 12 data resources
- View deals, plans, disputes, calculations
- Inspect schema resources

### Debug Communication

- View JSON-RPC messages
- Monitor request/response flow
- Debug tool call issues
- Test error handling

## Examples

### Example 1: List All Deals

1. Open inspector
2. Navigate to Tools → `getAllDeals`
3. Click "Execute"
4. View JSON response with all deals

### Example 2: Create a Deal

1. Open inspector
2. Navigate to Tools → `createDeal`
3. Provide parameters:
   ```json
   {
     "title": "Test Deal",
     "value": 50000,
     "salesRepId": "REP001"
   }
   ```
4. Click "Execute"
5. View created deal response

### Example 3: Analyze Sales Performance

1. Open inspector
2. Navigate to Prompts → `analyze-sales-performance`
3. Provide parameters:
   ```json
   {
     "salesRepId": "REP001",
     "period": "last-month"
   }
   ```
4. Execute prompt
5. View analysis results

## Advanced Usage

### Custom Inspector Configuration

Create a `.mcp-inspector.json` file:

```json
{
  "serverName": "Commission Calculator",
  "transport": "sse",
  "url": "http://localhost:8081/api/mcp/sse",
  "autoConnect": true,
  "theme": "dark"
}
```

Then run:
```bash
npx @modelcontextprotocol/inspector --config .mcp-inspector.json
```

### Multiple Server Testing

Test multiple servers simultaneously:

```bash
# Terminal 1: Commission Calculator
npx @modelcontextprotocol/inspector --port 5173 --transport sse http://localhost:8081/api/mcp/sse

# Terminal 2: Another MCP Server
npx @modelcontextprotocol/inspector --port 5174 --transport sse http://localhost:8082/api/mcp/sse
```

## Integration with Development Workflow

### Pre-commit Testing

```bash
# Test script before committing
mvn clean package -DskipTests
npx @modelcontextprotocol/inspector java -Dspring.ai.mcp.server.stdio=true ... (STDIO command)
# Manual verification in inspector
# Commit if all tests pass
```

### CI/CD Testing

Use the inspector in headless mode for automated testing:

```bash
npx @modelcontextprotocol/inspector \
  --headless \
  --transport sse \
  --test-suite tests/mcp-integration.json \
  http://localhost:8081/api/mcp/sse
```

## Resources

- MCP Inspector Documentation: https://github.com/modelcontextprotocol/inspector
- MCP Protocol Specification: https://spec.modelcontextprotocol.io/
- Spring AI MCP Documentation: https://docs.spring.io/spring-ai/reference/api/mcp/

## Notes

- **STDIO Mode**: Best for development and testing (no authentication needed)
- **Streamable HTTP Mode**: Best for production-like testing (modern, recommended)
- **SSE Mode**: Alternative for real-time streaming (deprecated but functional)
- The inspector is read-only safe - it won't modify server configuration
- **Authentication Required**: HTTP and SSE endpoints require Basic Auth (admin:admin123)
- Include credentials in URL format: `http://admin:admin123@localhost:8081/api/mcp/...`
