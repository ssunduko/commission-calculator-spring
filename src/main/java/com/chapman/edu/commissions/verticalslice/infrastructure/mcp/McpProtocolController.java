package com.chapman.edu.commissions.verticalslice.infrastructure.mcp;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP Protocol Controller
 * Exposes MCP protocol endpoints for AI agent integration
 */
@RestController
@RequestMapping("/api/mcp")
public class McpProtocolController {

    private final List<ToolCallback> tools;

    @Value("${spring.ai.mcp.server.name}")
    private String serverName;

    @Value("${spring.ai.mcp.server.version}")
    private String serverVersion;

    public McpProtocolController(List<ToolCallback> tools) {
        this.tools = tools;
    }

    /**
     * Get server information
     * Returns basic information about the MCP server
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getServerInfo() {
        return ResponseEntity.ok(Map.of(
            "protocolVersion", "2024-11-05",
            "serverInfo", Map.of(
                "name", serverName,
                "version", serverVersion
            ),
            "capabilities", Map.of(
                "tools", Map.of("listChanged", true),
                "resources", Map.of("listChanged", true),
                "prompts", Map.of("listChanged", true)
            )
        ));
    }

    /**
     * List all available tools
     * Returns information about all registered MCP tools
     */
    @PostMapping("/tools/list")
    public ResponseEntity<Map<String, Object>> listTools() {
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

        return ResponseEntity.ok(Map.of(
            "tools", toolList
        ));
    }

    /**
     * Call a tool
     * Executes a specific tool with given arguments
     */
    @PostMapping("/tools/call")
    public ResponseEntity<Map<String, Object>> callTool(@RequestBody Map<String, Object> request) {
        String toolName = (String) request.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) request.get("arguments");

        // Find the tool
        ToolCallback tool = tools.stream()
            .filter(t -> t.getToolDefinition().name().equals(toolName))
            .findFirst()
            .orElse(null);

        if (tool == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", Map.of(
                    "code", -32602,
                    "message", "Tool not found: " + toolName
                )
            ));
        }

        try {
            // Execute the tool
            String result = tool.call(convertArgumentsToJson(arguments));

            return ResponseEntity.ok(Map.of(
                "content", List.of(
                    Map.of(
                        "type", "text",
                        "text", result
                    )
                ),
                "isError", false
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "content", List.of(
                    Map.of(
                        "type", "text",
                        "text", "Error executing tool: " + e.getMessage()
                    )
                ),
                "isError", true
            ));
        }
    }

    /**
     * Initialize MCP session
     * Handles the MCP initialize request
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initialize(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(Map.of(
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
        ));
    }

    /**
     * Get server capabilities
     * Returns what the MCP server is capable of
     */
    @GetMapping("/capabilities")
    public ResponseEntity<Map<String, Object>> getCapabilities() {
        return ResponseEntity.ok(Map.of(
            "tools", Map.of(
                "count", tools.size(),
                "listChanged", true
            ),
            "resources", Map.of(
                "count", 0,
                "listChanged", true
            ),
            "prompts", Map.of(
                "count", 0,
                "listChanged", true
            )
        ));
    }

    private String convertArgumentsToJson(Map<String, Object> arguments) {
        try {
            // Simple JSON conversion
            if (arguments == null || arguments.isEmpty()) {
                return "{}";
            }

            StringBuilder json = new StringBuilder("{");
            arguments.forEach((key, value) -> {
                if (json.length() > 1) {
                    json.append(",");
                }
                json.append("\"").append(key).append("\":");
                if (value instanceof String) {
                    json.append("\"").append(value).append("\"");
                } else if (value instanceof Number || value instanceof Boolean) {
                    json.append(value);
                } else {
                    json.append("\"").append(value).append("\"");
                }
            });
            json.append("}");
            return json.toString();
        } catch (Exception e) {
            return "{}";
        }
    }
}
