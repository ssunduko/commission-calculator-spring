package com.chapman.edu.commissions.architecture.verticalslice;

import com.chapman.edu.commissions.architecture.verticalslice.features.currency.CurrencyConversionService;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.mcp.McpCommissionTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
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
     * Register all MCP tools for AI agent integration.
     *
     * Total: 31 tools
     * - Deal Management: 7 tools
     * - Commission Plan Management: 7 tools
     * - Dispute Management: 8 tools
     * - Commission Calculation: 5 tools
     * - Currency Conversion: 4 tools (proxied from external MCP server)
     */
    @Bean
    public ToolCallbackProvider commissionToolCallbackProvider(McpCommissionTools mcpCommissionTools,
                                                               CurrencyConversionService currencyConversionService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcpCommissionTools, currencyConversionService)
                .build();
    }

    /**
     * Also expose as List<ToolCallback> for internal use (processors, custom controllers).
     */
    @Bean
    public List<ToolCallback> tools(ToolCallbackProvider commissionToolCallbackProvider) {
        return Arrays.asList(commissionToolCallbackProvider.getToolCallbacks());
    }
}
