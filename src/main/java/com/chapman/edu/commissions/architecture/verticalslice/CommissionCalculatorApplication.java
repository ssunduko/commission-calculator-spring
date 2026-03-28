package com.chapman.edu.commissions.architecture.verticalslice;

import com.chapman.edu.commissions.architecture.verticalslice.features.currency.CurrencyConversionService;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.mcp.McpCommissionTools;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.mcp.McpSamplingTools;
import io.modelcontextprotocol.server.McpServerFeatures;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;

/**
 * Main Spring Boot Application for the Vertical Slice module.
 * Registers MCP tool callbacks for AI agent integration.
 *
 * Uses scanBasePackages to restrict component scanning to only this module,
 * preventing interference from other @SpringBootApplication classes
 * (ORM, SpringBoot, AI) that coexist in the same JAR.
 */
@SpringBootApplication(
    scanBasePackages = "com.chapman.edu.commissions.architecture.verticalslice",
    exclude = {
        org.springframework.ai.model.anthropic.autoconfigure.AnthropicChatAutoConfiguration.class
    }
)
public class CommissionCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CommissionCalculatorApplication.class);
        app.setAdditionalProfiles("verticalslice");
        app.run(args);
    }

    /**
     * Register @Tool-annotated MCP tools (31 tools).
     * These are standard tools where the client calls the server.
     */
    @Bean
    public ToolCallbackProvider commissionToolCallbackProvider(McpCommissionTools mcpCommissionTools,
                                                               CurrencyConversionService currencyConversionService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcpCommissionTools, currencyConversionService)
                .build();
    }

    /**
     * Register sampling-enabled MCP tools (3 tools).
     * These tools use McpSyncServerExchange.createMessage() to request
     * LLM completions from the connected AI client (MCP sampling).
     */
    @Bean
    public List<McpServerFeatures.SyncToolSpecification> samplingTools(McpSamplingTools samplingTools) {
        return samplingTools.getToolSpecifications();
    }

    /**
     * Expose tools as List<ToolCallback> for internal use (processors, custom controllers).
     */
    @Bean
    public List<ToolCallback> tools(ToolCallbackProvider commissionToolCallbackProvider) {
        return Arrays.asList(commissionToolCallbackProvider.getToolCallbacks());
    }
}
