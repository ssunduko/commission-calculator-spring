package com.chapman.edu.commissions.verticalslice.infrastructure.mcp;

import com.chapman.edu.commissions.verticalslice.features.calculations.CommissionCalculationService;
import com.chapman.edu.commissions.verticalslice.features.deals.DealService;
import com.chapman.edu.commissions.verticalslice.features.disputes.DisputeService;
import com.chapman.edu.commissions.verticalslice.features.plans.CommissionPlanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * MCP Resources Service
 * Provides access to commission calculator data as MCP resources.
 */
@Service
public class McpResources {

    private final DealService dealService;
    private final CommissionPlanService planService;
    private final DisputeService disputeService;
    private final CommissionCalculationService calculationService;
    private final ObjectMapper objectMapper;

    public McpResources(
            DealService dealService,
            CommissionPlanService planService,
            DisputeService disputeService,
            CommissionCalculationService calculationService,
            ObjectMapper objectMapper) {
        this.dealService = dealService;
        this.planService = planService;
        this.disputeService = disputeService;
        this.calculationService = calculationService;
        this.objectMapper = objectMapper;
    }

    /**
     * Get all available resources
     */
    public List<Map<String, Object>> getAllResources() {
        return List.of(
            createResource(
                "deals://all",
                "All Deals",
                "Complete list of all deals in the system",
                "application/json",
                "deals"
            ),

            createResource(
                "deals://active",
                "Active Deals",
                "List of all active (OPEN or WON) deals",
                "application/json",
                "deals/active"
            ),

            createResource(
                "plans://all",
                "All Commission Plans",
                "Complete list of all commission plans",
                "application/json",
                "plans"
            ),

            createResource(
                "plans://active",
                "Active Commission Plans",
                "List of currently active commission plans",
                "application/json",
                "plans/active"
            ),

            createResource(
                "disputes://all",
                "All Disputes",
                "Complete list of all commission disputes",
                "application/json",
                "disputes"
            ),

            createResource(
                "disputes://open",
                "Open Disputes",
                "List of all open/unresolved disputes",
                "application/json",
                "disputes/open"
            ),

            createResource(
                "calculations://all",
                "All Commission Calculations",
                "Complete list of all commission calculations",
                "application/json",
                "calculations"
            ),

            createResource(
                "schema://deal",
                "Deal Schema",
                "JSON schema for Deal entity",
                "application/schema+json",
                "schema/deal"
            ),

            createResource(
                "schema://commission-plan",
                "Commission Plan Schema",
                "JSON schema for Commission Plan entity",
                "application/schema+json",
                "schema/commission-plan"
            ),

            createResource(
                "schema://dispute",
                "Dispute Schema",
                "JSON schema for Dispute entity",
                "application/schema+json",
                "schema/dispute"
            )
        );
    }

    /**
     * Get resource content by URI
     */
    public Map<String, Object> getResourceContent(String uri) {
        try {
            String content;
            String mimeType = "application/json";

            switch (uri) {
                case "deals://all":
                    content = objectMapper.writeValueAsString(dealService.getAllDeals());
                    break;

                case "plans://all":
                    content = objectMapper.writeValueAsString(planService.getAllPlans());
                    break;

                case "plans://active":
                    content = objectMapper.writeValueAsString(
                        planService.getPlansByStatus(
                            com.chapman.edu.commissions.verticalslice.domain.PlanStatus.ACTIVE
                        )
                    );
                    break;

                case "disputes://all":
                    content = objectMapper.writeValueAsString(disputeService.getAllDisputes());
                    break;

                case "disputes://open":
                    content = objectMapper.writeValueAsString(
                        disputeService.getDisputesByStatus(
                            com.chapman.edu.commissions.verticalslice.domain.DisputeStatus.INITIATED
                        )
                    );
                    break;

                case "calculations://all":
                    content = objectMapper.writeValueAsString(calculationService.getAllCalculations());
                    break;

                case "schema://deal":
                    content = getDealSchema();
                    mimeType = "application/schema+json";
                    break;

                case "schema://commission-plan":
                    content = getCommissionPlanSchema();
                    mimeType = "application/schema+json";
                    break;

                case "schema://dispute":
                    content = getDisputeSchema();
                    mimeType = "application/schema+json";
                    break;

                default:
                    return Map.of("error", "Resource not found: " + uri);
            }

            return Map.of(
                "uri", uri,
                "mimeType", mimeType,
                "content", content
            );

        } catch (Exception e) {
            return Map.of(
                "error", "Failed to retrieve resource: " + e.getMessage()
            );
        }
    }

    private Map<String, Object> createResource(
            String uri,
            String name,
            String description,
            String mimeType,
            String path) {

        return Map.of(
            "uri", uri,
            "name", name,
            "description", description,
            "mimeType", mimeType
        );
    }

    private String getDealSchema() {
        return """
            {
              "$schema": "http://json-schema.org/draft-07/schema#",
              "type": "object",
              "properties": {
                "id": { "type": "string" },
                "title": { "type": "string" },
                "value": { "type": "number" },
                "salesRepId": { "type": "string" },
                "status": {
                  "type": "string",
                  "enum": ["OPEN", "WON", "LOST", "CANCELLED"]
                },
                "closeDate": { "type": "string", "format": "date" },
                "createdDate": { "type": "string", "format": "date" }
              },
              "required": ["title", "value", "salesRepId"]
            }
            """;
    }

    private String getCommissionPlanSchema() {
        return """
            {
              "$schema": "http://json-schema.org/draft-07/schema#",
              "type": "object",
              "properties": {
                "id": { "type": "string" },
                "name": { "type": "string" },
                "currency": { "type": "string", "minLength": 3, "maxLength": 3 },
                "status": {
                  "type": "string",
                  "enum": ["DRAFT", "ACTIVE", "INACTIVE", "ARCHIVED"]
                },
                "effectiveStartDate": { "type": "string", "format": "date" },
                "effectiveEndDate": { "type": "string", "format": "date" },
                "rulesCount": { "type": "integer" }
              },
              "required": ["name", "currency", "effectiveStartDate"]
            }
            """;
    }

    private String getDisputeSchema() {
        return """
            {
              "$schema": "http://json-schema.org/draft-07/schema#",
              "type": "object",
              "properties": {
                "id": { "type": "string" },
                "calculationId": { "type": "string" },
                "salesRepId": { "type": "string" },
                "title": { "type": "string" },
                "description": { "type": "string" },
                "status": {
                  "type": "string",
                  "enum": ["INITIATED", "UNDER_REVIEW", "RESOLVED", "APPROVED", "REJECTED", "ESCALATED", "CANCELLED", "ADDITIONAL_INFO_REQUESTED"]
                },
                "isEscalated": { "type": "boolean" },
                "createdDate": { "type": "string", "format": "date-time" }
              },
              "required": ["calculationId", "salesRepId", "title", "description"]
            }
            """;
    }
}
