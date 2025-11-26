package com.chapman.edu.commissions.verticalslice.mcp.server;

import com.chapman.edu.commissions.verticalslice.mcp.protocol.*;
import com.chapman.edu.commissions.verticalslice.mcp.tools.McpToolExecutor;
import com.chapman.edu.commissions.verticalslice.mcp.tools.McpToolRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class McpServerHandler {

    private final McpToolRegistry toolRegistry;
    private final McpToolExecutor toolExecutor;
    private final ObjectMapper objectMapper;

    private boolean initialized = false;

    public McpServerHandler(
        McpToolRegistry toolRegistry,
        McpToolExecutor toolExecutor,
        ObjectMapper objectMapper
    ) {
        this.toolRegistry = toolRegistry;
        this.toolExecutor = toolExecutor;
        this.objectMapper = objectMapper;
    }

    public McpResponse handleRequest(McpRequest request) {
        log.info("Handling MCP request: method={}, id={}", request.getMethod(), request.getId());

        try {
            if (request.getJsonrpc() == null || !request.getJsonrpc().equals("2.0")) {
                return createErrorResponse(request.getId(), McpError.invalidRequest("Invalid JSON-RPC version"));
            }

            Object result = switch (request.getMethod()) {
                case "initialize" -> handleInitialize(request.getParams());
                case "tools/list" -> handleToolsList(request.getParams());
                case "tools/call" -> handleToolsCall(request.getParams());
                case "ping" -> handlePing();
                default -> throw new IllegalArgumentException("Method not found: " + request.getMethod());
            };

            return McpResponse.builder()
                .jsonrpc("2.0")
                .id(request.getId())
                .result(result)
                .build();

        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return createErrorResponse(request.getId(), McpError.methodNotFound(e.getMessage()));
        } catch (Exception e) {
            log.error("Error handling request", e);
            return createErrorResponse(request.getId(), McpError.internalError(e.getMessage()));
        }
    }

    private Object handleInitialize(Map<String, Object> params) {
        log.info("Initializing MCP server");
        initialized = true;

        Map<String, Object> result = new HashMap<>();
        result.put("protocolVersion", "1.0.0");
        result.put("serverInfo", McpServerInfo.builder()
            .name("Commission Calculator MCP Server")
            .version("1.0.0")
            .protocolVersion("1.0.0")
            .build());
        result.put("capabilities", Map.of(
            "tools", Map.of("supportsToolCalls", true),
            "resources", Map.of("supportsResourceAccess", false)
        ));

        return result;
    }

    private Object handleToolsList(Map<String, Object> params) {
        if (!initialized) {
            throw new IllegalStateException("Server not initialized");
        }

        log.info("Listing available tools");
        return Map.of("tools", toolRegistry.getAllTools());
    }

    private Object handleToolsCall(Map<String, Object> params) {
        if (!initialized) {
            throw new IllegalStateException("Server not initialized");
        }

        String toolName = (String) params.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");

        if (toolName == null) {
            throw new IllegalArgumentException("Tool name is required");
        }

        log.info("Calling tool: {} with arguments: {}", toolName, arguments);

        try {
            Object result = toolExecutor.executeTool(toolName, arguments != null ? arguments : new HashMap<>());
            return Map.of(
                "content", result,
                "isError", false
            );
        } catch (Exception e) {
            log.error("Error executing tool: {}", toolName, e);
            return Map.of(
                "content", Map.of("error", e.getMessage()),
                "isError", true
            );
        }
    }

    private Object handlePing() {
        return Map.of("status", "ok");
    }

    private McpResponse createErrorResponse(String id, McpError error) {
        return McpResponse.builder()
            .jsonrpc("2.0")
            .id(id)
            .error(error)
            .build();
    }

    public boolean isInitialized() {
        return initialized;
    }
}
