package com.chapman.edu.commissions.verticalslice.mcp.client;

import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpRequest;
import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpResponse;
import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpTool;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for MCP (Model Context Protocol) client implementations.
 * Provides methods to connect to and interact with MCP servers.
 */
public interface McpClient {

    /**
     * Initialize the connection to the MCP server.
     *
     * @param clientInfo Client information to send during initialization
     * @return CompletableFuture with the server's initialization response
     */
    CompletableFuture<McpResponse> initialize(Map<String, Object> clientInfo);

    /**
     * List all available tools from the MCP server.
     *
     * @return CompletableFuture with list of available tools
     */
    CompletableFuture<List<McpTool>> listTools();

    /**
     * Call a tool on the MCP server with the given arguments.
     *
     * @param toolName Name of the tool to call
     * @param arguments Arguments to pass to the tool
     * @return CompletableFuture with the tool execution result
     */
    CompletableFuture<Object> callTool(String toolName, Map<String, Object> arguments);

    /**
     * Send a ping request to the MCP server.
     *
     * @return CompletableFuture with the ping response
     */
    CompletableFuture<McpResponse> ping();

    /**
     * Send a custom MCP request to the server.
     *
     * @param request The MCP request to send
     * @return CompletableFuture with the server response
     */
    CompletableFuture<McpResponse> sendRequest(McpRequest request);

    /**
     * Check if the client is connected to the server.
     *
     * @return true if connected, false otherwise
     */
    boolean isConnected();

    /**
     * Close the connection to the MCP server.
     */
    void close();
}
