package com.chapman.edu.commissions.verticalslice.mcp.client;

import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Example demonstrating how to use the MCP client.
 * This can be used as a reference for integrating MCP client functionality.
 */
@Slf4j
@Component
public class McpClientExample {

    private final McpClientFactory clientFactory;

    public McpClientExample(McpClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    /**
     * Example: Connect to MCP server via HTTP and list tools.
     */
    public void httpClientExample() {
        log.info("=== HTTP MCP Client Example ===");

        // Create HTTP client
        McpClient client = clientFactory.createHttpClient(
            "http://localhost:8080/api/mcp",
            "admin",
            "admin123"
        );

        try {
            // Initialize the client
            log.info("Initializing client...");
            client.initialize(new HashMap<>()).get();

            // List available tools
            log.info("Listing available tools...");
            List<McpTool> tools = client.listTools().get();
            log.info("Found {} tools:", tools.size());
            tools.forEach(tool -> log.info("  - {}: {}", tool.getName(), tool.getDescription()));

            // Call a tool - example: listDeals
            log.info("Calling listDeals tool...");
            Object result = client.callTool("listDeals", new HashMap<>()).get();
            log.info("Result: {}", result);

            // Ping the server
            log.info("Pinging server...");
            client.ping().get();
            log.info("Ping successful");

        } catch (Exception e) {
            log.error("Error in HTTP client example", e);
        } finally {
            client.close();
        }
    }

    /**
     * Example: Connect to MCP server via WebSocket.
     */
    public void webSocketClientExample() {
        log.info("=== WebSocket MCP Client Example ===");

        // Create WebSocket client
        McpWebSocketClient client = clientFactory.createWebSocketClient("ws://localhost:8080/mcp/ws");

        try {
            // Connect to WebSocket
            log.info("Connecting to WebSocket...");
            client.connect().get();

            // Initialize the client
            log.info("Initializing client...");
            client.initialize(new HashMap<>()).get();

            // List available tools
            log.info("Listing available tools...");
            List<McpTool> tools = client.listTools().get();
            log.info("Found {} tools:", tools.size());
            tools.forEach(tool -> log.info("  - {}: {}", tool.getName(), tool.getDescription()));

            // Call a tool - example: getDeal
            log.info("Calling listCommissionPlans tool...");
            Object result = client.callTool("listCommissionPlans", new HashMap<>()).get();
            log.info("Result: {}", result);

        } catch (Exception e) {
            log.error("Error in WebSocket client example", e);
        } finally {
            client.close();
        }
    }

    /**
     * Example: Create a deal using MCP client.
     */
    public void createDealExample() {
        log.info("=== Create Deal Example ===");

        McpClient client = clientFactory.createHttpClient(
            "http://localhost:8080/api/mcp",
            "admin",
            "admin123"
        );

        try {
            // Initialize
            client.initialize(new HashMap<>()).get();

            // Prepare deal data
            Map<String, Object> dealData = new HashMap<>();
            dealData.put("title", "Acme Corp Deal");
            dealData.put("value", 50000.0);
            dealData.put("salesRepId", "rep-001");

            // Call createDeal tool
            log.info("Creating deal...");
            Object result = client.callTool("createDeal", dealData).get();
            log.info("Deal created: {}", result);

        } catch (Exception e) {
            log.error("Error creating deal", e);
        } finally {
            client.close();
        }
    }

    /**
     * Example: Calculate commission using MCP client.
     */
    public void calculateCommissionExample(String dealId, String planId) {
        log.info("=== Calculate Commission Example ===");

        McpClient client = clientFactory.createHttpClient(
            "http://localhost:8080/api/mcp",
            "admin",
            "admin123"
        );

        try {
            // Initialize
            client.initialize(new HashMap<>()).get();

            // Prepare calculation data
            Map<String, Object> calcData = new HashMap<>();
            calcData.put("dealId", dealId);
            calcData.put("planId", planId);

            // Call calculateCommission tool
            log.info("Calculating commission for deal {} with plan {}...", dealId, planId);
            Object result = client.callTool("calculateCommission", calcData).get();
            log.info("Commission calculated: {}", result);

        } catch (Exception e) {
            log.error("Error calculating commission", e);
        } finally {
            client.close();
        }
    }

    /**
     * Example: Async tool calling with multiple operations.
     */
    public void asyncMultipleOperationsExample() {
        log.info("=== Async Multiple Operations Example ===");

        McpClient client = clientFactory.createHttpClient(
            "http://localhost:8080/api/mcp",
            "admin",
            "admin123"
        );

        try {
            // Initialize
            client.initialize(new HashMap<>()).get();

            // Perform multiple async operations
            var listDealsFuture = client.callTool("listDeals", new HashMap<>());
            var listPlansFuture = client.callTool("listCommissionPlans", new HashMap<>());
            var listCalculationsFuture = client.callTool("listCalculations", new HashMap<>());

            // Wait for all to complete
            log.info("Waiting for all operations to complete...");
            Object deals = listDealsFuture.get();
            Object plans = listPlansFuture.get();
            Object calculations = listCalculationsFuture.get();

            log.info("Deals: {}", deals);
            log.info("Plans: {}", plans);
            log.info("Calculations: {}", calculations);

        } catch (Exception e) {
            log.error("Error in async operations", e);
        } finally {
            client.close();
        }
    }
}
