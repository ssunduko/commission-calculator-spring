package com.chapman.edu.commissions.verticalslice.mcp.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for MCP client connections.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mcp.client")
public class McpClientProperties {
    private boolean enabled = false;
    private String serverUrl = "http://localhost:8080/api/mcp";
    private String webSocketUrl = "ws://localhost:8080/mcp/ws";
    private TransportType transportType = TransportType.HTTP;
    private AuthProperties auth = new AuthProperties();
    private int connectionTimeout = 10000;
    private int requestTimeout = 30000;

    public enum TransportType {
        HTTP,
        WEBSOCKET
    }

    @Data
    public static class AuthProperties {
        private boolean enabled = false;
        private String username = "admin";
        private String password = "admin123";
    }
}
