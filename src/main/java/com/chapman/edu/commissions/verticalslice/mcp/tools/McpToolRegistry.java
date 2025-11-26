package com.chapman.edu.commissions.verticalslice.mcp.tools;

import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpTool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class McpToolRegistry {

    public List<McpTool> getAllTools() {
        List<McpTool> tools = new ArrayList<>();

        tools.add(createDealTool());
        tools.add(getDealTool());
        tools.add(listDealsTool());
        tools.add(updateDealTool());

        tools.add(createPlanTool());
        tools.add(getPlanTool());
        tools.add(listPlansTool());
        tools.add(activatePlanTool());
        tools.add(addRuleToPlanTool());

        tools.add(calculateCommissionTool());
        tools.add(getCalculationTool());
        tools.add(listCalculationsTool());

        tools.add(createDisputeTool());
        tools.add(resolveDisputeTool());
        tools.add(getDisputeTool());
        tools.add(listDisputesTool());

        return tools;
    }

    private McpTool createDealTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("required", List.of("title", "value", "salesRepId"));

        Map<String, Object> properties = new HashMap<>();
        properties.put("title", Map.of("type", "string", "description", "Deal title"));
        properties.put("value", Map.of("type", "number", "description", "Deal value (must be greater than zero)"));
        properties.put("salesRepId", Map.of("type", "string", "description", "Sales representative ID"));

        schema.put("properties", properties);

        return McpTool.builder()
            .name("createDeal")
            .description("Create a new sales deal with title, value, and sales rep")
            .inputSchema(schema)
            .build();
    }

    private McpTool getDealTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("required", List.of("dealId"));

        Map<String, Object> properties = new HashMap<>();
        properties.put("dealId", Map.of("type", "string", "description", "The ID of the deal to retrieve"));
        schema.put("properties", properties);

        return McpTool.builder()
            .name("getDeal")
            .description("Retrieve details of a specific deal by ID")
            .inputSchema(schema)
            .build();
    }

    private McpTool listDealsTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("salesRepId", Map.of("type", "string", "description", "Filter by sales representative ID"));
        properties.put("status", Map.of("type", "string", "description", "Filter by deal status (OPEN, WON, LOST, PENDING)"));
        schema.put("properties", properties);

        return McpTool.builder()
            .name("listDeals")
            .description("List all deals with optional filters for sales rep or status")
            .inputSchema(schema)
            .build();
    }

    private McpTool updateDealTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("required", List.of("dealId"));

        Map<String, Object> properties = new HashMap<>();
        properties.put("dealId", Map.of("type", "string", "description", "The ID of the deal to update"));
        properties.put("title", Map.of("type", "string", "description", "New deal title"));
        properties.put("value", Map.of("type", "number", "description", "New deal value"));
        properties.put("status", Map.of("type", "string", "description", "New status (OPEN, WON, LOST, CANCELLED)"));
        properties.put("closeDate", Map.of("type", "string", "format", "date", "description", "Close date (YYYY-MM-DD)"));
        schema.put("properties", properties);

        return McpTool.builder()
            .name("updateDeal")
            .description("Update an existing deal's title, value, status, or close date")
            .inputSchema(schema)
            .build();
    }

    private McpTool createPlanTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("required", List.of("name", "currencyCode"));

        Map<String, Object> properties = new HashMap<>();
        properties.put("name", Map.of("type", "string", "description", "Commission plan name"));
        properties.put("currencyCode", Map.of("type", "string", "description", "Currency code (e.g., USD, EUR)"));
        properties.put("effectiveStartDate", Map.of("type", "string", "format", "date", "description", "Effective start date (YYYY-MM-DD)"));
        properties.put("effectiveEndDate", Map.of("type", "string", "format", "date", "description", "Effective end date (YYYY-MM-DD)"));
        schema.put("properties", properties);

        return McpTool.builder()
            .name("createCommissionPlan")
            .description("Create a new commission plan with name and currency")
            .inputSchema(schema)
            .build();
    }

    private McpTool getPlanTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("required", List.of("planId"));

        Map<String, Object> properties = new HashMap<>();
        properties.put("planId", Map.of("type", "string", "description", "The ID of the commission plan to retrieve"));
        schema.put("properties", properties);

        return McpTool.builder()
            .name("getCommissionPlan")
            .description("Retrieve details of a specific commission plan by ID")
            .inputSchema(schema)
            .build();
    }

    private McpTool listPlansTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("status", Map.of("type", "string", "description", "Filter by status (DRAFT, ACTIVE, ARCHIVED)"));
        schema.put("properties", properties);

        return McpTool.builder()
            .name("listCommissionPlans")
            .description("List all commission plans with optional status filter")
            .inputSchema(schema)
            .build();
    }

    private McpTool activatePlanTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("required", List.of("planId"));

        Map<String, Object> properties = new HashMap<>();
        properties.put("planId", Map.of("type", "string", "description", "The ID of the plan to activate"));
        schema.put("properties", properties);

        return McpTool.builder()
            .name("activateCommissionPlan")
            .description("Activate a commission plan, making it available for calculations")
            .inputSchema(schema)
            .build();
    }

    private McpTool addRuleToPlanTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("required", List.of("planId", "name", "rate"));

        Map<String, Object> properties = new HashMap<>();
        properties.put("planId", Map.of("type", "string", "description", "The ID of the plan"));
        properties.put("name", Map.of("type", "string", "description", "Rule name"));
        properties.put("description", Map.of("type", "string", "description", "Rule description"));
        properties.put("rate", Map.of("type", "number", "description", "Commission rate (must be non-negative)"));
        properties.put("ruleType", Map.of("type", "string", "description", "Type of rule (STANDARD, ACCELERATOR, BONUS, DECELERATOR, SPECIAL)"));
        properties.put("priority", Map.of("type", "integer", "description", "Rule priority for evaluation order"));
        schema.put("properties", properties);

        return McpTool.builder()
            .name("addRuleToPlan")
            .description("Add a commission rule to an existing plan")
            .inputSchema(schema)
            .build();
    }

    private McpTool calculateCommissionTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("required", List.of("dealId", "planId"));

        Map<String, Object> properties = new HashMap<>();
        properties.put("dealId", Map.of("type", "string", "description", "The ID of the deal to calculate commission for"));
        properties.put("planId", Map.of("type", "string", "description", "The commission plan to use for calculation"));
        schema.put("properties", properties);

        return McpTool.builder()
            .name("calculateCommission")
            .description("Calculate commission for a deal using a specific commission plan")
            .inputSchema(schema)
            .build();
    }

    private McpTool getCalculationTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("required", List.of("calculationId"));

        Map<String, Object> properties = new HashMap<>();
        properties.put("calculationId", Map.of("type", "string", "description", "The ID of the calculation to retrieve"));
        schema.put("properties", properties);

        return McpTool.builder()
            .name("getCalculation")
            .description("Retrieve details of a specific commission calculation by ID")
            .inputSchema(schema)
            .build();
    }

    private McpTool listCalculationsTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("dealId", Map.of("type", "string", "description", "Filter by deal ID"));
        properties.put("salesRepId", Map.of("type", "string", "description", "Filter by sales representative ID"));
        schema.put("properties", properties);

        return McpTool.builder()
            .name("listCalculations")
            .description("List all commission calculations with optional filters")
            .inputSchema(schema)
            .build();
    }

    private McpTool createDisputeTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("required", List.of("calculationId", "salesRepId", "title", "description"));

        Map<String, Object> properties = new HashMap<>();
        properties.put("calculationId", Map.of("type", "string", "description", "The calculation ID being disputed"));
        properties.put("salesRepId", Map.of("type", "string", "description", "Sales representative ID"));
        properties.put("title", Map.of("type", "string", "description", "Dispute title"));
        properties.put("description", Map.of("type", "string", "description", "Detailed description of the dispute"));
        schema.put("properties", properties);

        return McpTool.builder()
            .name("createDispute")
            .description("Create a dispute for a commission calculation")
            .inputSchema(schema)
            .build();
    }

    private McpTool resolveDisputeTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("required", List.of("disputeId", "resolution", "resolvedBy"));

        Map<String, Object> properties = new HashMap<>();
        properties.put("disputeId", Map.of("type", "string", "description", "The dispute ID to resolve"));
        properties.put("resolution", Map.of("type", "string", "description", "Resolution details"));
        properties.put("resolvedBy", Map.of("type", "string", "description", "ID of person resolving the dispute"));
        properties.put("approved", Map.of("type", "boolean", "description", "Whether the dispute is approved (default: false)"));
        schema.put("properties", properties);

        return McpTool.builder()
            .name("resolveDispute")
            .description("Resolve a commission dispute with resolution details")
            .inputSchema(schema)
            .build();
    }

    private McpTool getDisputeTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("required", List.of("disputeId"));

        Map<String, Object> properties = new HashMap<>();
        properties.put("disputeId", Map.of("type", "string", "description", "The dispute ID to retrieve"));
        schema.put("properties", properties);

        return McpTool.builder()
            .name("getDispute")
            .description("Retrieve details of a specific dispute by ID")
            .inputSchema(schema)
            .build();
    }

    private McpTool listDisputesTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("status", Map.of("type", "string", "description", "Filter by status (OPEN, UNDER_REVIEW, RESOLVED, REJECTED)"));
        properties.put("salesRepId", Map.of("type", "string", "description", "Filter by sales representative ID"));
        schema.put("properties", properties);

        return McpTool.builder()
            .name("listDisputes")
            .description("List all disputes with optional status or sales rep filter")
            .inputSchema(schema)
            .build();
    }
}
