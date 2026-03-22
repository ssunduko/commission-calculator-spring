package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * MCP SSE (Server-Sent Events) Controller
 * Provides MCP protocol access via Server-Sent Events transport
 */
@RestController
@RequestMapping("/api/mcp")
public class McpSseController {

    private static final Logger log = LoggerFactory.getLogger(McpSseController.class);
    private static final long SSE_TIMEOUT = 30 * 60 * 1000; // 30 minutes

    private final List<ToolCallback> tools;
    private final McpPrompts mcpPrompts;
    private final McpResources mcpResources;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.mcp.server.name}")
    private String serverName;

    @Value("${spring.ai.mcp.server.version}")
    private String serverVersion;

    // Store active SSE connections
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, Object> sessionData = new ConcurrentHashMap<>();

    public McpSseController(
            List<ToolCallback> tools,
            McpPrompts mcpPrompts,
            McpResources mcpResources,
            ObjectMapper objectMapper) {
        this.tools = tools;
        this.mcpPrompts = mcpPrompts;
        this.mcpResources = mcpResources;
        this.objectMapper = objectMapper;
    }

    /**
     * SSE endpoint for MCP protocol
     * Establishes a Server-Sent Events connection for real-time MCP communication
     */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMcpEvents() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.add(emitter);

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.info("SSE connection completed");
        });

        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.info("SSE connection timed out");
        });

        emitter.onError((error) -> {
            emitters.remove(emitter);
            log.error("SSE connection error", error);
        });

        try {
            // MCP SSE spec: first event must be "endpoint" with the URL to POST messages to
            emitter.send(SseEmitter.event()
                .name("endpoint")
                .data("/api/mcp/message"));

            log.info("New SSE connection established — sent endpoint event");
        } catch (IOException e) {
            log.error("Error sending initial SSE event", e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * Handle MCP JSON-RPC requests via POST
     * Processes MCP protocol messages and returns response directly
     * Also broadcasts to all connected SSE clients
     */
    @PostMapping(value = "/message", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> handleMcpMessage(@RequestBody Map<String, Object> request) {
        String method = (String) request.get("method");
        Object params = request.get("params");
        Object id = request.get("id");

        log.info("Received MCP message: method={}, id={}", method, id);

        Map<String, Object> response;
        try {
            Object result = processMethod(method, params);
            response = Map.of(
                "jsonrpc", "2.0",
                "id", id != null ? id : "null",
                "result", result
            );
        } catch (Exception e) {
            log.error("Error processing MCP method: " + method, e);
            response = Map.of(
                "jsonrpc", "2.0",
                "id", id != null ? id : "null",
                "error", Map.of(
                    "code", -32603,
                    "message", "Internal error: " + e.getMessage()
                )
            );
        }

        // Send response to all connected SSE clients
        broadcastEvent("mcp-response", response);

        // Also return response directly in HTTP response
        return response;
    }

    /**
     * Process MCP method calls
     */
    private Object processMethod(String method, Object params) throws Exception {
        if (method == null) {
            throw new IllegalArgumentException("Method is required");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> paramsMap = params instanceof Map ? (Map<String, Object>) params : Map.of();

        return switch (method) {
            case "initialize" -> handleInitialize(paramsMap);
            case "tools/list" -> handleToolsList();
            case "tools/call" -> handleToolCall(paramsMap);
            case "prompts/list" -> handlePromptsList();
            case "prompts/get" -> handlePromptGet(paramsMap);
            case "resources/list" -> handleResourcesList();
            case "resources/read" -> handleResourceRead(paramsMap);
            case "resources/templates/list" -> handleResourceTemplatesList();
            case "ping" -> Map.of("status", "ok");
            default -> throw new IllegalArgumentException("Unknown method: " + method);
        };
    }

    private Map<String, Object> handleInitialize(Map<String, Object> params) {
        return Map.of(
            "protocolVersion", "2024-11-05",
            "capabilities", Map.of(
                "tools", Map.of("listChanged", true),
                "resources", Map.of("listChanged", true),
                "prompts", Map.of("listChanged", true)
            ),
            "serverInfo", Map.of(
                "name", serverName,
                "version", serverVersion
            )
        );
    }

    private Map<String, Object> handleToolsList() {
        List<Map<String, Object>> toolList = tools.stream()
            .map(tool -> {
                var toolDef = tool.getToolDefinition();
                return Map.<String, Object>of(
                    "name", toolDef.name(),
                    "description", toolDef.description(),
                    "inputSchema", Map.of(
                        "type", "object",
                        "properties", Map.of(),
                        "required", List.of()
                    )
                );
            })
            .collect(Collectors.toList());

        return Map.of("tools", toolList);
    }

    private Map<String, Object> handleToolCall(Map<String, Object> params) {
        String toolName = (String) params.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");

        ToolCallback tool = tools.stream()
            .filter(t -> t.getToolDefinition().name().equals(toolName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Tool not found: " + toolName));

        try {
            String result = tool.call(convertArgumentsToJson(arguments));
            return Map.of(
                "content", List.of(
                    Map.of(
                        "type", "text",
                        "text", result
                    )
                ),
                "isError", false
            );
        } catch (Exception e) {
            return Map.of(
                "content", List.of(
                    Map.of(
                        "type", "text",
                        "text", "Error executing tool: " + e.getMessage()
                    )
                ),
                "isError", true
            );
        }
    }

    private Map<String, Object> handlePromptsList() {
        return Map.of("prompts", mcpPrompts.getAllPrompts());
    }

    private Map<String, Object> handlePromptGet(Map<String, Object> params) {
        String name = (String) params.get("name");
        Map<String, Object> prompt = mcpPrompts.getPrompt(name);
        if (prompt == null) {
            throw new IllegalArgumentException("Prompt not found: " + name);
        }
        return prompt;
    }

    private Map<String, Object> handleResourcesList() {
        return Map.of("resources", mcpResources.getAllResources());
    }

    private Map<String, Object> handleResourceRead(Map<String, Object> params) {
        String uri = (String) params.get("uri");
        Map<String, Object> content = mcpResources.getResourceContent(uri);
        if (content.containsKey("error")) {
            throw new IllegalArgumentException((String) content.get("error"));
        }
        return Map.of("contents", List.of(content));
    }

    private Map<String, Object> handleResourceTemplatesList() {
        return Map.of("resourceTemplates", mcpResources.getAllResourceTemplates());
    }

    /**
     * Broadcast an event to all connected SSE clients
     */
    private void broadcastEvent(String eventName, Object data) {
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(objectMapper.writeValueAsString(data)));
            } catch (IOException e) {
                log.error("Error sending SSE event to client", e);
                deadEmitters.add(emitter);
            }
        });

        // Remove dead emitters
        emitters.removeAll(deadEmitters);
    }

    /**
     * Send notification to all connected clients
     * Can be used to notify about data changes
     */
    public void sendNotification(String type, Object data) {
        Map<String, Object> notification = Map.of(
            "type", type,
            "data", data,
            "timestamp", System.currentTimeMillis()
        );
        broadcastEvent("mcp-notification", notification);
    }

    private String convertArgumentsToJson(Map<String, Object> arguments) {
        try {
            if (arguments == null || arguments.isEmpty()) {
                return "{}";
            }
            return objectMapper.writeValueAsString(arguments);
        } catch (Exception e) {
            log.error("Error converting arguments to JSON", e);
            return "{}";
        }
    }

    /**
     * Get the count of active SSE connections
     */
    @GetMapping("/sse/connections")
    public Map<String, Object> getConnectionsInfo() {
        return Map.of(
            "activeConnections", emitters.size(),
            "serverInfo", Map.of(
                "name", serverName,
                "version", serverVersion
            )
        );
    }
}
