package com.chapman.edu.commissions.architecture.verticalslice.features.currency;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@link CurrencyMcpClientConfig} wires up the Streamable HTTP
 * transport correctly. The test stands up an in-process HTTP server that
 * speaks just enough JSON-RPC for the client's initialize handshake to
 * succeed, then asserts:
 *
 *   1. The request lands on the configured /mcp endpoint (not /sse).
 *   2. The Spring bean is fully initialized (initialize() returned).
 *   3. listTools() round-trips through the streamable transport.
 *
 * This test exists because CurrencyControllerIntegrationTest's
 * @TestConfiguration overrides the production bean by name, so it can't
 * verify the real transport plumbing.
 */
class CurrencyMcpClientConfigTest {

    private HttpServer server;
    private final ConcurrentLinkedQueue<String> requestPaths = new ConcurrentLinkedQueue<>();

    @BeforeEach
    void startStubServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(Executors.newSingleThreadExecutor());

        // Single handler for the whole MCP endpoint. Replies to initialize,
        // notifications/initialized, and tools/list so the McpSyncClient
        // bean's initialize() handshake succeeds end-to-end.
        server.createContext("/mcp", exchange -> {
            String method = exchange.getRequestMethod();
            requestPaths.add(method + " " + exchange.getRequestURI().getPath());

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("[stub] " + method + " /mcp body=" + body
                    + " accept=" + exchange.getRequestHeaders().getFirst("Accept"));

            // Streamable HTTP supports an optional GET for server-pushed
            // streams. We don't push anything, so refuse it.
            if ("GET".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
                return;
            }

            if (body.contains("\"method\":\"notifications/initialized\"")) {
                exchange.sendResponseHeaders(202, -1);
                exchange.close();
                return;
            }

            // Pull out the JSON-RPC id so responses correlate with requests.
            String id = extractId(body);

            String response;
            if (body.contains("\"method\":\"initialize\"")) {
                response = "{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"result\":{"
                        + "\"protocolVersion\":\"2025-06-18\","
                        + "\"capabilities\":{\"tools\":{\"listChanged\":true}},"
                        + "\"serverInfo\":{\"name\":\"stub\",\"version\":\"0.0.1\"}}}";
                exchange.getResponseHeaders().add("Mcp-Session-Id", "test-session");
            } else if (body.contains("\"method\":\"tools/list\"")) {
                response = "{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"result\":{\"tools\":["
                        + "{\"name\":\"convert_currency\",\"description\":\"stub\","
                        + "\"inputSchema\":{\"type\":\"object\"}}]}}";
            } else {
                response = "{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"result\":{}}";
            }

            sendJson(exchange, response);
        });

        server.start();
    }

    @AfterEach
    void stopStubServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void currencyMcpClient_ShouldUseStreamableHttpAgainstMcpEndpoint() {
        CurrencyMcpClientConfig config = new CurrencyMcpClientConfig();
        ReflectionTestUtils.setField(config, "baseUrl",
                "http://127.0.0.1:" + server.getAddress().getPort());
        ReflectionTestUtils.setField(config, "mcpEndpoint", "/mcp");

        McpSyncClient client = config.currencyMcpClient();
        try {
            assertThat(client).isNotNull();
            assertThat(client.isInitialized())
                    .as("Streamable HTTP handshake should complete")
                    .isTrue();

            McpSchema.ListToolsResult tools = client.listTools();
            assertThat(tools.tools())
                    .extracting(McpSchema.Tool::name)
                    .containsExactly("convert_currency");

            // Every request that arrived hit /mcp — none hit /sse.
            assertThat(requestPaths)
                    .as("Transport should target /mcp, never the legacy /sse path")
                    .isNotEmpty()
                    .allSatisfy(entry -> assertThat(entry).endsWith(" /mcp"));
        } finally {
            client.close();
        }
    }

    private static String extractId(String body) {
        int idx = body.indexOf("\"id\":");
        if (idx < 0) {
            return "0";
        }
        int start = idx + 5;
        int end = start;
        while (end < body.length()
                && body.charAt(end) != ',' && body.charAt(end) != '}') {
            end++;
        }
        return body.substring(start, end).trim();
    }

    private static void sendJson(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
