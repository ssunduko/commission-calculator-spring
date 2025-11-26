package com.chapman.edu.commissions.verticalslice.mcp.config;

import com.chapman.edu.commissions.verticalslice.mcp.transport.McpWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class McpWebSocketConfig implements WebSocketConfigurer {

    private final McpWebSocketHandler mcpWebSocketHandler;

    public McpWebSocketConfig(McpWebSocketHandler mcpWebSocketHandler) {
        this.mcpWebSocketHandler = mcpWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(mcpWebSocketHandler, "/mcp/ws")
            .setAllowedOrigins("*");
    }
}
