# MCP Client Documentation

## Overview

The MCP (Model Context Protocol) Client provides a way to interact with MCP servers programmatically. This implementation supports both HTTP and WebSocket transports for flexible integration.

## Features

- **Multiple Transport Options**: HTTP and WebSocket support
- **Async Operations**: All operations return CompletableFuture for non-blocking execution
- **Authentication**: Built-in support for Basic Authentication
- **Type-Safe**: Strongly typed request/response handling
- **Configurable**: Spring Boot configuration properties support

## Quick Start

### 1. HTTP Client Example

```java
@Autowired
private McpClientFactory clientFactory;

public void useHttpClient() {
    // Create HTTP client
    McpClient client = clientFactory.createHttpClient(
        "http://localhost:8080/api/mcp",
        "admin",
        "admin123"
    );

    try {
        // Initialize the connection
        client.initialize(new HashMap<>()).get();

        // List available tools
        List<McpTool> tools = client.listTools().get();
        tools.forEach(tool ->
            System.out.println(tool.getName() + ": " + tool.getDescription())
        );

        // Call a tool
        Map<String, Object> params = new HashMap<>();
        params.put("status", "ACTIVE");
        Object result = client.callTool("listCommissionPlans", params).get();
        System.out.println("Result: " + result);

    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        client.close();
    }
}
```

### 2. WebSocket Client Example

```java
public void useWebSocketClient() {
    // Create WebSocket client
    McpWebSocketClient client = clientFactory.createWebSocketClient(
        "ws://localhost:8080/mcp/ws"
    );

    try {
        // Connect to WebSocket
        client.connect().get();

        // Initialize
        client.initialize(new HashMap<>()).get();

        // Call tools
        Object deals = client.callTool("listDeals", new HashMap<>()).get();
        System.out.println("Deals: " + deals);

    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        client.close();
    }
}
```

## Configuration

Add to `application.properties`:

```properties
# MCP Client Configuration
mcp.client.enabled=true
mcp.client.server-url=http://localhost:8080/api/mcp
mcp.client.web-socket-url=ws://localhost:8080/mcp/ws
mcp.client.transport-type=HTTP
mcp.client.auth.enabled=true
mcp.client.auth.username=admin
mcp.client.auth.password=admin123
mcp.client.connection-timeout=10000
mcp.client.request-timeout=30000
```

## Available Tools

The Commission Calculator MCP server exposes the following tools:

### Deal Management
- `createDeal` - Create a new sales deal
- `getDeal` - Get deal details by ID
- `listDeals` - List all deals (with optional filters)
- `updateDeal` - Update an existing deal

### Commission Plan Management
- `createCommissionPlan` - Create a new commission plan
- `getCommissionPlan` - Get plan details by ID
- `listCommissionPlans` - List all plans (with optional filters)
- `activateCommissionPlan` - Activate a plan
- `addRuleToPlan` - Add rules to a plan

### Commission Calculation
- `calculateCommission` - Calculate commission for a deal
- `getCalculation` - Get calculation details by ID
- `listCalculations` - List all calculations

### Dispute Management
- `createDispute` - Create a commission dispute
- `resolveDispute` - Resolve a dispute
- `getDispute` - Get dispute details by ID
- `listDisputes` - List all disputes

## Advanced Usage

### Async Multiple Operations

```java
public void asyncOperations() {
    McpClient client = clientFactory.createHttpClient(
        "http://localhost:8080/api/mcp",
        "admin",
        "admin123"
    );

    try {
        client.initialize(new HashMap<>()).get();

        // Execute multiple operations in parallel
        CompletableFuture<Object> dealsFuture =
            client.callTool("listDeals", new HashMap<>());
        CompletableFuture<Object> plansFuture =
            client.callTool("listCommissionPlans", new HashMap<>());
        CompletableFuture<Object> calcsFuture =
            client.callTool("listCalculations", new HashMap<>());

        // Wait for all to complete
        CompletableFuture.allOf(dealsFuture, plansFuture, calcsFuture).get();

        Object deals = dealsFuture.get();
        Object plans = plansFuture.get();
        Object calculations = calcsFuture.get();

        // Process results...

    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        client.close();
    }
}
```

### Creating a Deal

```java
public void createDeal() {
    McpClient client = clientFactory.createHttpClient(
        "http://localhost:8080/api/mcp",
        "admin",
        "admin123"
    );

    try {
        client.initialize(new HashMap<>()).get();

        Map<String, Object> dealData = new HashMap<>();
        dealData.put("customerName", "Acme Corporation");
        dealData.put("amount", 100000.0);
        dealData.put("salesRepId", "rep-001");
        dealData.put("closeDate", "2025-12-31");
        dealData.put("products", List.of("product-1", "product-2"));

        Object result = client.callTool("createDeal", dealData).get();
        System.out.println("Deal created: " + result);

    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        client.close();
    }
}
```

### Calculating Commission

```java
public void calculateCommission(String dealId, String planId) {
    McpClient client = clientFactory.createHttpClient(
        "http://localhost:8080/api/mcp",
        "admin",
        "admin123"
    );

    try {
        client.initialize(new HashMap<>()).get();

        Map<String, Object> params = new HashMap<>();
        params.put("dealId", dealId);
        params.put("planId", planId);

        Object result = client.callTool("calculateCommission", params).get();
        System.out.println("Commission: " + result);

    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        client.close();
    }
}
```

## Error Handling

The client provides comprehensive error handling:

```java
try {
    Object result = client.callTool("createDeal", params).get();
} catch (ExecutionException e) {
    if (e.getCause() instanceof RuntimeException) {
        RuntimeException re = (RuntimeException) e.getCause();
        System.err.println("Tool execution failed: " + re.getMessage());
    }
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    System.err.println("Operation interrupted");
}
```

## Testing

Integration tests are available in `McpClientTest.java`. To run them:

1. Start the MCP server: `mvn spring-boot:run`
2. Enable the tests by removing `@Disabled` annotation
3. Run tests: `mvn test -Dtest=McpClientTest`

## Example Application

See `McpClientExample.java` for complete working examples of:
- HTTP client usage
- WebSocket client usage
- Creating deals
- Calculating commissions
- Async multiple operations

## Transport Comparison

### HTTP Transport
- **Pros**: Simple, stateless, works through firewalls
- **Cons**: Higher latency for multiple requests
- **Use When**: Making occasional requests or stateless operations

### WebSocket Transport
- **Pros**: Lower latency, persistent connection, bidirectional
- **Cons**: More complex, requires persistent connection
- **Use When**: Making frequent requests or need real-time updates

## Best Practices

1. **Always close clients**: Use try-finally blocks to ensure connections are closed
2. **Handle timeouts**: Set appropriate timeout values for your use case
3. **Use async operations**: Leverage CompletableFuture for better performance
4. **Initialize once**: Initialize the client connection before making tool calls
5. **Reuse clients**: Create one client instance and reuse it for multiple operations
6. **Error handling**: Always handle potential exceptions from async operations

## Troubleshooting

### Connection Refused
- Ensure MCP server is running on the specified URL
- Check firewall settings
- Verify server URL and port

### Authentication Failed
- Verify username and password
- Check server security configuration
- Ensure Basic Auth is enabled on server

### Tool Not Found
- List available tools to see what's exposed
- Verify tool name spelling
- Check server logs for errors

### Timeout Errors
- Increase timeout values in configuration
- Check network latency
- Verify server is responsive

## API Reference

### McpClient Interface

```java
CompletableFuture<McpResponse> initialize(Map<String, Object> clientInfo)
CompletableFuture<List<McpTool>> listTools()
CompletableFuture<Object> callTool(String toolName, Map<String, Object> arguments)
CompletableFuture<McpResponse> ping()
CompletableFuture<McpResponse> sendRequest(McpRequest request)
boolean isConnected()
void close()
```

### McpClientFactory

```java
McpClient createHttpClient(String serverUrl)
McpClient createHttpClient(String serverUrl, String username, String password)
McpWebSocketClient createWebSocketClient(String serverUrl)
```

## License

This implementation follows the Model Context Protocol specification.
