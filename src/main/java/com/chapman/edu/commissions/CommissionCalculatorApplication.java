package com.chapman.edu.commissions;

import com.chapman.edu.commissions.verticalslice.features.calculations.CommissionCalculationService;
import com.chapman.edu.commissions.verticalslice.features.deals.DealService;
import com.chapman.edu.commissions.verticalslice.features.disputes.DisputeService;
import com.chapman.edu.commissions.verticalslice.features.plans.CommissionPlanService;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
     * Register all MCP tools from services
     * These tools can be invoked by AI agents through the MCP protocol
     */
    @Bean
    public List<ToolCallback> tools(
            DealService dealService,
            CommissionPlanService commissionPlanService,
            DisputeService disputeService,
            CommissionCalculationService calculationService) {

        // Collect all tool callbacks from all services
        // Each ToolCallbacks.from() returns an array of ToolCallback[]
        return java.util.stream.Stream.of(
                // Deal Management Tools (7 tools)
                ToolCallbacks.from(dealService),

                // Commission Plan Management Tools (7 tools)
                ToolCallbacks.from(commissionPlanService),

                // Dispute Management Tools (8 tools)
                ToolCallbacks.from(disputeService),

                // Commission Calculation Tools (5 tools)
                ToolCallbacks.from(calculationService)
        )
        .flatMap(java.util.Arrays::stream)
        .toList();
    }
}
