package com.chapman.edu.commissions.verticalslice.mcp.client;

import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpRequest;
import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpResponse;
import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpTool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * WebSocket-based implementation of the MCP client.
 * Provides persistent bidirectional communication with MCP servers.
 */
@Slf4j
public class McpWebSocketClient extends TextWebSocketHandler implements McpClient {

    private final String serverUrl;
    private final ObjectMapper objectMapper;
    private final AtomicLong requestIdCounter;
    private final Map<String, CompletableFuture<McpResponse>> pendingRequests;
    private final StandardWebSocketClient webSocketClient;

    private WebSocketSession session;
    private boolean initialized;

    public McpWebSocketClient(String serverUrl, ObjectMapper objectMapper) {
        this.serverUrl = serverUrl;
        this.objectMapper = objectMapper;
        this.requestIdCounter = new AtomicLong(1);
        this.pendingRequests = new ConcurrentHashMap<>();
        this.webSocketClient = new StandardWebSocketClient();
        this.initialized = false;
    }

    /**
     * Connect to the WebSocket server.
     */
    public CompletableFuture<Void> connect() {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Connecting to WebSocket server: {}", serverUrl);
                session = webSocketClient.execute(this, null, new URI(serverUrl)).get(10, TimeUnit.SECONDS);
                log.info("Connected to WebSocket server");
            } catch (Exception e) {
                log.error("Failed to connect to WebSocket server", e);
                throw new RuntimeException("Failed to connect to WebSocket server: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        this.session = session;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Received WebSocket message: {}", payload);

        try {
            McpResponse response = objectMapper.readValue(payload, McpResponse.class);
            String requestId = response.getId();

            CompletableFuture<McpResponse> future = pendingRequests.remove(requestId);
            if (future != null) {
                future.complete(response);
            } else {
                log.warn("Received response for unknown request ID: {}", requestId);
            }
        } catch (Exception e) {
            log.error("Error processing WebSocket message", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {} with status: {}", session.getId(), status);
        this.session = null;
        this.initialized = false;

        // Complete all pending requests with an error
        pendingRequests.values().forEach(future ->
            future.completeExceptionally(new RuntimeException("Connection closed"))
        );
        pendingRequests.clear();
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error", exception);
    }

    @Override
    public CompletableFuture<McpResponse> initialize(Map<String, Object> clientInfo) {
        return ensureConnected().thenCompose(v -> {
            try {
                Map<String, Object> params = new HashMap<>(clientInfo);
                params.putIfAbsent("protocolVersion", "1.0.0");
                params.putIfAbsent("clientInfo", Map.of(
                    "name", "Commission Calculator MCP WebSocket Client",
                    "version", "1.0.0"
                ));

                McpRequest request = McpRequest.builder()
                    .id(String.valueOf(requestIdCounter.getAndIncrement()))
                    .method("initialize")
                    .params(params)
                    .build();

                return sendRequest(request).thenApply(response -> {
                    if (response.getError() == null) {
                        initialized = true;
                        log.info("MCP WebSocket client initialized successfully");
                    } else {
                        log.error("Failed to initialize MCP WebSocket client: {}", response.getError().getMessage());
                    }
                    return response;
                });
            } catch (Exception e) {
                log.error("Error during initialization", e);
                return CompletableFuture.failedFuture(e);
            }
        });
    }

    @Override
    public CompletableFuture<List<McpTool>> listTools() {
        if (!initialized) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("Client must be initialized before listing tools")
            );
        }

        McpRequest request = McpRequest.builder()
            .id(String.valueOf(requestIdCounter.getAndIncrement()))
            .method("tools/list")
            .params(new HashMap<>())
            .build();

        return sendRequest(request).thenApply(response -> {
            if (response.getError() != null) {
                throw new RuntimeException("Failed to list tools: " + response.getError().getMessage());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) response.getResult();
            List<?> toolsList = (List<?>) result.get("tools");

            return objectMapper.convertValue(toolsList, new TypeReference<List<McpTool>>() {});
        });
    }

    @Override
    public CompletableFuture<Object> callTool(String toolName, Map<String, Object> arguments) {
        if (!initialized) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("Client must be initialized before calling tools")
            );
        }

        Map<String, Object> params = new HashMap<>();
        params.put("name", toolName);
        params.put("arguments", arguments != null ? arguments : new HashMap<>());

        McpRequest request = McpRequest.builder()
            .id(String.valueOf(requestIdCounter.getAndIncrement()))
            .method("tools/call")
            .params(params)
            .build();

        return sendRequest(request).thenApply(response -> {
            if (response.getError() != null) {
                throw new RuntimeException("Tool call failed: " + response.getError().getMessage());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) response.getResult();

            Boolean isError = (Boolean) result.get("isError");
            if (Boolean.TRUE.equals(isError)) {
                throw new RuntimeException("Tool execution error: " + result.get("content"));
            }

            return result.get("content");
        });
    }

    @Override
    public CompletableFuture<McpResponse> ping() {
        McpRequest request = McpRequest.builder()
            .id(String.valueOf(requestIdCounter.getAndIncrement()))
            .method("ping")
            .params(new HashMap<>())
            .build();

        return sendRequest(request);
    }

    @Override
    public CompletableFuture<McpResponse> sendRequest(McpRequest request) {
        return ensureConnected().thenCompose(v -> {
            try {
                CompletableFuture<McpResponse> future = new CompletableFuture<>();
                pendingRequests.put(request.getId(), future);

                String json = objectMapper.writeValueAsString(request);
                log.debug("Sending WebSocket request: method={}, id={}", request.getMethod(), request.getId());
                session.sendMessage(new TextMessage(json));

                // Set timeout for the request
                CompletableFuture.delayedExecutor(30, TimeUnit.SECONDS).execute(() -> {
                    if (!future.isDone()) {
                        pendingRequests.remove(request.getId());
                        future.completeExceptionally(new RuntimeException("Request timeout"));
                    }
                });

                return future;
            } catch (Exception e) {
                pendingRequests.remove(request.getId());
                log.error("Error sending WebSocket request", e);
                return CompletableFuture.failedFuture(e);
            }
        });
    }

    private CompletableFuture<Void> ensureConnected() {
        if (session != null && session.isOpen()) {
            return CompletableFuture.completedFuture(null);
        }
        return connect();
    }

    @Override
    public boolean isConnected() {
        return session != null && session.isOpen() && initialized;
    }

    @Override
    public void close() {
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
            initialized = false;
            log.info("MCP WebSocket client closed");
        } catch (Exception e) {
            log.error("Error closing WebSocket client", e);
        }
    }
}
