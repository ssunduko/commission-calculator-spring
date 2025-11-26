package com.chapman.edu.commissions.verticalslice.mcp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mcp.server")
public class McpServerProperties {
    private boolean enabled = true;
    private String version = "1.0.0";
    private String name = "Commission Calculator MCP Server";
    private String protocolVersion = "1.0.0";
    private WebSocketProperties websocket = new WebSocketProperties();
    private HttpProperties http = new HttpProperties();

    @Data
    public static class WebSocketProperties {
        private boolean enabled = true;
        private String path = "/mcp/ws";
        private String[] allowedOrigins = {"*"};
    }

    @Data
    public static class HttpProperties {
        private boolean enabled = true;
        private String path = "/api/mcp";
    }
}
