package com.chapman.edu.commissions.verticalslice.mcp.client;

import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpResponse;
import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for MCP client functionality.
 * Note: These tests require the MCP server to be running on localhost:8080
 */
@Slf4j
@SpringBootTest
@Disabled("Requires MCP server to be running - enable manually for integration testing")
class McpClientTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testHttpClientInitialization() throws Exception {
        McpClient client = new McpHttpClient(
            "http://localhost:8080/api/mcp",
            "admin",
            "admin123",
            objectMapper
        );

        try {
            // Initialize
            McpResponse initResponse = client.initialize(new HashMap<>()).get();
            assertNotNull(initResponse);
            assertNull(initResponse.getError());
            assertTrue(client.isConnected());

            log.info("Initialization response: {}", initResponse.getResult());
        } finally {
            client.close();
        }
    }

    @Test
    void testHttpClientListTools() throws Exception {
        McpClient client = new McpHttpClient(
            "http://localhost:8080/api/mcp",
            "admin",
            "admin123",
            objectMapper
        );

        try {
            // Initialize
            client.initialize(new HashMap<>()).get();

            // List tools
            List<McpTool> tools = client.listTools().get();
            assertNotNull(tools);
            assertFalse(tools.isEmpty());

            log.info("Found {} tools", tools.size());
            tools.forEach(tool -> {
                log.info("Tool: {} - {}", tool.getName(), tool.getDescription());
                assertNotNull(tool.getName());
                assertNotNull(tool.getDescription());
                assertNotNull(tool.getInputSchema());
            });

            // Verify expected tools exist
            assertTrue(tools.stream().anyMatch(t -> t.getName().equals("createDeal")));
            assertTrue(tools.stream().anyMatch(t -> t.getName().equals("listDeals")));
            assertTrue(tools.stream().anyMatch(t -> t.getName().equals("calculateCommission")));

        } finally {
            client.close();
        }
    }

    @Test
    void testHttpClientCallTool() throws Exception {
        McpClient client = new McpHttpClient(
            "http://localhost:8080/api/mcp",
            "admin",
            "admin123",
            objectMapper
        );

        try {
            // Initialize
            client.initialize(new HashMap<>()).get();

            // Call listDeals tool
            Object result = client.callTool("listDeals", new HashMap<>()).get();
            assertNotNull(result);
            log.info("List deals result: {}", result);

            // Call listCommissionPlans tool
            result = client.callTool("listCommissionPlans", new HashMap<>()).get();
            assertNotNull(result);
            log.info("List plans result: {}", result);

        } finally {
            client.close();
        }
    }

    @Test
    void testHttpClientPing() throws Exception {
        McpClient client = new McpHttpClient(
            "http://localhost:8080/api/mcp",
            "admin",
            "admin123",
            objectMapper
        );

        try {
            // Ping without initialization should work
            McpResponse pingResponse = client.ping().get();
            assertNotNull(pingResponse);
            assertNull(pingResponse.getError());
            assertNotNull(pingResponse.getResult());

            log.info("Ping response: {}", pingResponse.getResult());
        } finally {
            client.close();
        }
    }

    @Test
    void testWebSocketClientInitialization() throws Exception {
        McpWebSocketClient client = new McpWebSocketClient(
            "ws://localhost:8080/mcp/ws",
            objectMapper
        );

        try {
            // Connect
            client.connect().get();

            // Initialize
            McpResponse initResponse = client.initialize(new HashMap<>()).get();
            assertNotNull(initResponse);
            assertNull(initResponse.getError());
            assertTrue(client.isConnected());

            log.info("WebSocket initialization response: {}", initResponse.getResult());
        } finally {
            client.close();
        }
    }

    @Test
    void testWebSocketClientListTools() throws Exception {
        McpWebSocketClient client = new McpWebSocketClient(
            "ws://localhost:8080/mcp/ws",
            objectMapper
        );

        try {
            // Connect and initialize
            client.connect().get();
            client.initialize(new HashMap<>()).get();

            // List tools
            List<McpTool> tools = client.listTools().get();
            assertNotNull(tools);
            assertFalse(tools.isEmpty());

            log.info("WebSocket - Found {} tools", tools.size());
            assertTrue(tools.stream().anyMatch(t -> t.getName().equals("createDeal")));

        } finally {
            client.close();
        }
    }

    @Test
    void testWebSocketClientCallTool() throws Exception {
        McpWebSocketClient client = new McpWebSocketClient(
            "ws://localhost:8080/mcp/ws",
            objectMapper
        );

        try {
            // Connect and initialize
            client.connect().get();
            client.initialize(new HashMap<>()).get();

            // Call tool
            Object result = client.callTool("listDeals", new HashMap<>()).get();
            assertNotNull(result);
            log.info("WebSocket tool call result: {}", result);

        } finally {
            client.close();
        }
    }

    @Test
    void testCreateDealViaMcpClient() throws Exception {
        McpClient client = new McpHttpClient(
            "http://localhost:8080/api/mcp",
            "admin",
            "admin123",
            objectMapper
        );

        try {
            // Initialize
            client.initialize(new HashMap<>()).get();

            // Create deal
            Map<String, Object> dealData = new HashMap<>();
            dealData.put("title", "Test Customer Deal");
            dealData.put("value", 25000.0);
            dealData.put("salesRepId", "rep-test-001");

            Object result = client.callTool("createDeal", dealData).get();
            assertNotNull(result);
            log.info("Created deal: {}", result);

            // Verify we can list deals and see our new deal
            Object deals = client.callTool("listDeals", new HashMap<>()).get();
            assertNotNull(deals);
            log.info("All deals: {}", deals);

        } finally {
            client.close();
        }
    }

    @Test
    void testAsyncMultipleToolCalls() throws Exception {
        McpClient client = new McpHttpClient(
            "http://localhost:8080/api/mcp",
            "admin",
            "admin123",
            objectMapper
        );

        try {
            // Initialize
            client.initialize(new HashMap<>()).get();

            // Make multiple async calls
            var dealsFuture = client.callTool("listDeals", new HashMap<>());
            var plansFuture = client.callTool("listCommissionPlans", new HashMap<>());
            var disputesFuture = client.callTool("listDisputes", new HashMap<>());

            // Wait for all to complete
            Object deals = dealsFuture.get();
            Object plans = plansFuture.get();
            Object disputes = disputesFuture.get();

            assertNotNull(deals);
            assertNotNull(plans);
            assertNotNull(disputes);

            log.info("Async results received for all calls");

        } finally {
            client.close();
        }
    }
}
