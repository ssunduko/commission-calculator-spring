package com.chapman.edu.commissions.architecture.verticalslice.features.currency;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
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
 * https://currency-mcp.wesbos.com using SSE transport.
 *
 * This demonstrates using Spring AI's MCP client to consume
 * tools from a third-party MCP server.
 */
@Configuration
public class CurrencyMcpClientConfig {

    private static final Logger log = LoggerFactory.getLogger(CurrencyMcpClientConfig.class);

    @Value("${app.mcp.currency.base-url:https://currency-mcp.wesbos.com}")
    private String baseUrl;

    @Value("${app.mcp.currency.sse-endpoint:/sse}")
    private String sseEndpoint;

    @Bean(destroyMethod = "close")
    public McpSyncClient currencyMcpClient() {
        log.info("Connecting to Currency MCP server at {}{}", baseUrl, sseEndpoint);

        HttpClientSseClientTransport transport = HttpClientSseClientTransport
                .builder(baseUrl)
                .sseEndpoint(sseEndpoint)
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
