package com.chapman.edu.commissions.verticalslice.mcp.transport;

import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpRequest;
import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpResponse;
import com.chapman.edu.commissions.verticalslice.mcp.server.McpServerHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/mcp")
@Tag(name = "MCP Server", description = "Model Context Protocol Server Endpoints")
public class McpController {

    private final McpServerHandler mcpServerHandler;

    public McpController(McpServerHandler mcpServerHandler) {
        this.mcpServerHandler = mcpServerHandler;
    }

    @PostMapping
    @Operation(summary = "Execute MCP JSON-RPC request", description = "Process MCP protocol requests via HTTP POST")
    public ResponseEntity<McpResponse> handleMcpRequest(@RequestBody McpRequest request) {
        log.info("Received HTTP MCP request: method={}", request.getMethod());
        McpResponse response = mcpServerHandler.handleRequest(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Check MCP server health", description = "Returns server health status")
    public ResponseEntity<Object> health() {
        return ResponseEntity.ok(java.util.Map.of(
            "status", "ok",
            "initialized", mcpServerHandler.isInitialized()
        ));
    }
}
