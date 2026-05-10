package com.chapman.edu.commissions.architecture.verticalslice.features.currency;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for the external Currency Conversion MCP client.
 *
 * Connects to Wes Bos's currency-conversion-mcp server at
 * https://currency-mcp.wesbos.com using the Streamable HTTP transport
 * (the SSE endpoint at /sse returns 404 — the server only exposes /mcp).
 */
@Configuration
public class CurrencyMcpClientConfig {

    private static final Logger log = LoggerFactory.getLogger(CurrencyMcpClientConfig.class);

    @Value("${app.mcp.currency.base-url:https://currency-mcp.wesbos.com}")
    private String baseUrl;

    @Value("${app.mcp.currency.mcp-endpoint:/mcp}")
    private String mcpEndpoint;

    @Bean(destroyMethod = "close")
    public McpSyncClient currencyMcpClient() {
        log.info("Connecting to Currency MCP server at {}{} (Streamable HTTP)", baseUrl, mcpEndpoint);

        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport
                .builder(baseUrl)
                .endpoint(mcpEndpoint)
                .build();

        McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(15))
                .clientInfo(new McpSchema.Implementation("commission-calculator-currency-client", "1.0.0"))
                .build();

        client.initialize();
        log.info("Currency MCP client initialized successfully");

        return client;
    }
}
