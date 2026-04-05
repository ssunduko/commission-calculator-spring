package com.chapman.edu.commissions.architecture.verticalslice;

import com.chapman.edu.commissions.architecture.verticalslice.features.currency.CurrencyConversionService;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.mcp.McpCommissionTools;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.mcp.McpPrompts;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.mcp.McpResources;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.mcp.McpSamplingTools;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Main Spring Boot Application for the Vertical Slice module.
 * Registers MCP tool callbacks, prompts, and resources for AI agent integration.
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
@EnableScheduling
public class CommissionCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CommissionCalculatorApplication.class);
        app.setAdditionalProfiles("verticalslice");
        app.run(args);
    }

    /**
     * Register @Tool-annotated MCP tools for internal use (processors, controllers).
     * The annotation scanner (enabled in application.properties) handles MCP protocol registration.
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

    /**
     * Register MCP prompts so they appear in the MCP protocol's prompts/list response.
     * Each prompt is a predefined template that AI agents can use for common tasks.
     */
    @Bean
    @SuppressWarnings("unchecked")
    public List<McpServerFeatures.SyncPromptSpecification> mcpPromptSpecifications(McpPrompts mcpPrompts) {
        return mcpPrompts.getAllPrompts().stream()
            .map(p -> {
                String name = (String) p.get("name");
                String description = (String) p.get("description");
                String template = (String) p.get("template");
                List<Map<String, Object>> args = (List<Map<String, Object>>) p.get("arguments");

                List<McpSchema.PromptArgument> promptArgs = args.stream()
                    .map(a -> new McpSchema.PromptArgument(
                        (String) a.get("name"),
                        (String) a.get("description"),
                        (Boolean) a.get("required")
                    ))
                    .toList();

                McpSchema.Prompt prompt = new McpSchema.Prompt(name, description, promptArgs);

                return new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, request) -> {
                    String resolvedTemplate = template;
                    if (request.arguments() != null) {
                        for (var entry : request.arguments().entrySet()) {
                            resolvedTemplate = resolvedTemplate.replace(
                                "{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
                        }
                    }
                    return new McpSchema.GetPromptResult(
                        description,
                        List.of(new McpSchema.PromptMessage(
                            McpSchema.Role.USER,
                            new McpSchema.TextContent(resolvedTemplate)
                        ))
                    );
                });
            })
            .toList();
    }

    /**
     * Register MCP resources so they appear in the MCP protocol's resources/list response.
     * Resources provide read-only access to commission calculator data.
     */
    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> mcpResourceSpecifications(McpResources mcpResources) {
        return mcpResources.getAllResources().stream()
            .map(r -> {
                String uri = (String) r.get("uri");
                String name = (String) r.get("name");
                String description = (String) r.get("description");
                String mimeType = (String) r.get("mimeType");

                McpSchema.Resource resource = new McpSchema.Resource(uri, name, description, mimeType, null);

                return new McpServerFeatures.SyncResourceSpecification(resource, (exchange, request) -> {
                    Map<String, Object> content = mcpResources.getResourceContent(request.uri());
                    if (content.containsKey("error")) {
                        return new McpSchema.ReadResourceResult(List.of(
                            new McpSchema.TextResourceContents(request.uri(), "text/plain",
                                (String) content.get("error"))
                        ));
                    }
                    return new McpSchema.ReadResourceResult(List.of(
                        new McpSchema.TextResourceContents(
                            request.uri(),
                            (String) content.getOrDefault("mimeType", "application/json"),
                            (String) content.get("content"))
                    ));
                });
            })
            .toList();
    }
}
