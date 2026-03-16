package com.chapman.edu.commissions.architecture.verticalslice;

import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.mcp.McpCommissionTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
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
    scanBasePackages = "com.chapman.edu.commissions.architecture.verticalslice"
)
public class CommissionCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CommissionCalculatorApplication.class);
        app.setAdditionalProfiles("verticalslice");
        app.run(args);
    }

    /**
     * Register all MCP tools from McpCommissionTools facade
     * These tools can be invoked by AI agents through the MCP protocol
     *
     * Total: 27 tools
     * - Deal Management Tools: 7 tools
     * - Commission Plan Management Tools: 7 tools
     * - Dispute Management Tools: 8 tools
     * - Commission Calculation Tools: 5 tools
     */
    @Bean
    public List<ToolCallback> tools(McpCommissionTools mcpCommissionTools) {
        return Arrays.asList(ToolCallbacks.from(mcpCommissionTools));
    }
}
