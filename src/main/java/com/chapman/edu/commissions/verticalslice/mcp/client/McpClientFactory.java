package com.chapman.edu.commissions.verticalslice.mcp.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Factory for creating MCP client instances.
 */
@Slf4j
@Component
public class McpClientFactory {

    private final ObjectMapper objectMapper;

    public McpClientFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Create an HTTP-based MCP client.
     *
     * @param serverUrl The URL of the MCP server (e.g., http://localhost:8080/api/mcp)
     * @return A new McpHttpClient instance
     */
    public McpClient createHttpClient(String serverUrl) {
        log.info("Creating HTTP MCP client for: {}", serverUrl);
        return new McpHttpClient(serverUrl, objectMapper);
    }

    /**
     * Create an HTTP-based MCP client with authentication.
     *
     * @param serverUrl The URL of the MCP server
     * @param username Basic auth username
     * @param password Basic auth password
     * @return A new McpHttpClient instance with authentication
     */
    public McpClient createHttpClient(String serverUrl, String username, String password) {
        log.info("Creating authenticated HTTP MCP client for: {}", serverUrl);
        return new McpHttpClient(serverUrl, username, password, objectMapper);
    }

    /**
     * Create a WebSocket-based MCP client.
     *
     * @param serverUrl The WebSocket URL of the MCP server (e.g., ws://localhost:8080/mcp/ws)
     * @return A new McpWebSocketClient instance
     */
    public McpWebSocketClient createWebSocketClient(String serverUrl) {
        log.info("Creating WebSocket MCP client for: {}", serverUrl);
        return new McpWebSocketClient(serverUrl, objectMapper);
    }
}
