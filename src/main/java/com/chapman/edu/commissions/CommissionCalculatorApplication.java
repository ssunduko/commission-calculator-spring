package com.chapman.edu.commissions;

import com.chapman.edu.commissions.verticalslice.infrastructure.mcp.McpCommissionTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;

/**
 * Main Spring Boot Application
 * Registers MCP tool callbacks for AI agent integration
 */
@SpringBootApplication
public class CommissionCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommissionCalculatorApplication.class, args);
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
