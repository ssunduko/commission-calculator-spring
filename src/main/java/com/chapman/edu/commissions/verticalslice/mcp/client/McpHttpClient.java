package com.chapman.edu.commissions.verticalslice.mcp.client;

import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpRequest;
import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpResponse;
import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpTool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * HTTP-based implementation of the MCP client.
 * Uses RestTemplate to communicate with MCP servers over HTTP.
 */
@Slf4j
public class McpHttpClient implements McpClient {

    private final String serverUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AtomicLong requestIdCounter;
    private boolean initialized;
    private String username;
    private String password;

    public McpHttpClient(String serverUrl, ObjectMapper objectMapper) {
        this.serverUrl = serverUrl.endsWith("/") ? serverUrl.substring(0, serverUrl.length() - 1) : serverUrl;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.requestIdCounter = new AtomicLong(1);
        this.initialized = false;
    }

    public McpHttpClient(String serverUrl, String username, String password, ObjectMapper objectMapper) {
        this(serverUrl, objectMapper);
        this.username = username;
        this.password = password;
    }

    @Override
    public CompletableFuture<McpResponse> initialize(Map<String, Object> clientInfo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> params = new HashMap<>(clientInfo);
                params.putIfAbsent("protocolVersion", "1.0.0");
                params.putIfAbsent("clientInfo", Map.of(
                    "name", "Commission Calculator MCP Client",
                    "version", "1.0.0"
                ));

                McpRequest request = McpRequest.builder()
                    .id(String.valueOf(requestIdCounter.getAndIncrement()))
                    .method("initialize")
                    .params(params)
                    .build();

                McpResponse response = sendRequestSync(request);

                if (response.getError() == null) {
                    initialized = true;
                    log.info("MCP client initialized successfully");
                } else {
                    log.error("Failed to initialize MCP client: {}", response.getError().getMessage());
                }

                return response;
            } catch (Exception e) {
                log.error("Error during initialization", e);
                throw new RuntimeException("Failed to initialize MCP client", e);
            }
        });
    }

    @Override
    public CompletableFuture<List<McpTool>> listTools() {
        return CompletableFuture.supplyAsync(() -> {
            if (!initialized) {
                throw new IllegalStateException("Client must be initialized before listing tools");
            }

            try {
                McpRequest request = McpRequest.builder()
                    .id(String.valueOf(requestIdCounter.getAndIncrement()))
                    .method("tools/list")
                    .params(new HashMap<>())
                    .build();

                McpResponse response = sendRequestSync(request);

                if (response.getError() != null) {
                    throw new RuntimeException("Failed to list tools: " + response.getError().getMessage());
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) response.getResult();
                List<?> toolsList = (List<?>) result.get("tools");

                return objectMapper.convertValue(toolsList, new TypeReference<List<McpTool>>() {});
            } catch (Exception e) {
                log.error("Error listing tools", e);
                throw new RuntimeException("Failed to list tools", e);
            }
        });
    }

    @Override
    public CompletableFuture<Object> callTool(String toolName, Map<String, Object> arguments) {
        return CompletableFuture.supplyAsync(() -> {
            if (!initialized) {
                throw new IllegalStateException("Client must be initialized before calling tools");
            }

            try {
                Map<String, Object> params = new HashMap<>();
                params.put("name", toolName);
                params.put("arguments", arguments != null ? arguments : new HashMap<>());

                McpRequest request = McpRequest.builder()
                    .id(String.valueOf(requestIdCounter.getAndIncrement()))
                    .method("tools/call")
                    .params(params)
                    .build();

                McpResponse response = sendRequestSync(request);

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
            } catch (Exception e) {
                log.error("Error calling tool: {}", toolName, e);
                throw new RuntimeException("Failed to call tool: " + toolName, e);
            }
        });
    }

    @Override
    public CompletableFuture<McpResponse> ping() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                McpRequest request = McpRequest.builder()
                    .id(String.valueOf(requestIdCounter.getAndIncrement()))
                    .method("ping")
                    .params(new HashMap<>())
                    .build();

                return sendRequestSync(request);
            } catch (Exception e) {
                log.error("Error sending ping", e);
                throw new RuntimeException("Failed to ping server", e);
            }
        });
    }

    @Override
    public CompletableFuture<McpResponse> sendRequest(McpRequest request) {
        return CompletableFuture.supplyAsync(() -> sendRequestSync(request));
    }

    private McpResponse sendRequestSync(McpRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (username != null && password != null) {
                String auth = username + ":" + password;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                headers.set("Authorization", "Basic " + encodedAuth);
            }

            HttpEntity<McpRequest> entity = new HttpEntity<>(request, headers);

            log.debug("Sending MCP request to {}: method={}, id={}", serverUrl, request.getMethod(), request.getId());

            ResponseEntity<McpResponse> responseEntity = restTemplate.exchange(
                serverUrl,
                HttpMethod.POST,
                entity,
                McpResponse.class
            );

            McpResponse response = responseEntity.getBody();
            log.debug("Received MCP response: id={}, hasError={}",
                response != null ? response.getId() : null,
                response != null && response.getError() != null);

            return response;
        } catch (Exception e) {
            log.error("Error sending request to MCP server", e);
            throw new RuntimeException("Failed to communicate with MCP server: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isConnected() {
        return initialized;
    }

    @Override
    public void close() {
        initialized = false;
        log.info("MCP HTTP client closed");
    }
}
