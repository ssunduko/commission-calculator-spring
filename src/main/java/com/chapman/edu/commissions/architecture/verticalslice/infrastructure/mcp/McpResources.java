package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.mcp;

import com.chapman.edu.commissions.architecture.verticalslice.domain.DealStatus;
import com.chapman.edu.commissions.architecture.verticalslice.domain.DisputeStatus;
import com.chapman.edu.commissions.architecture.verticalslice.domain.PlanStatus;
import com.chapman.edu.commissions.architecture.verticalslice.features.calculations.CommissionCalculationService;
import com.chapman.edu.commissions.architecture.verticalslice.features.currency.CurrencyConversionService;
import com.chapman.edu.commissions.architecture.verticalslice.features.currency.GetLatestRatesRequest;
import com.chapman.edu.commissions.architecture.verticalslice.features.deals.DealService;
import com.chapman.edu.commissions.architecture.verticalslice.features.disputes.DisputeService;
import com.chapman.edu.commissions.architecture.verticalslice.features.plans.CommissionPlanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * MCP Resources Service
 * Provides access to commission calculator data as MCP resources.
 */
@Service
public class McpResources {

    private static final Logger log = LoggerFactory.getLogger(McpResources.class);

    private final DealService dealService;
    private final CommissionPlanService planService;
    private final DisputeService disputeService;
    private final CommissionCalculationService calculationService;
    private final CurrencyConversionService currencyService;
    private final ObjectMapper objectMapper;

    public McpResources(
            DealService dealService,
            CommissionPlanService planService,
            DisputeService disputeService,
            CommissionCalculationService calculationService,
            CurrencyConversionService currencyService,
            ObjectMapper objectMapper) {
        this.dealService = dealService;
        this.planService = planService;
        this.disputeService = disputeService;
        this.calculationService = calculationService;
        this.currencyService = currencyService;
        this.objectMapper = objectMapper;
    }

    /**
     * Get all available resource templates
     */
    public List<Map<String, Object>> getAllResourceTemplates() {
        return List.of(
            createResourceTemplate(
                "deal://{dealId}",
                "Deal by ID",
                "Get a specific deal by its ID",
                "application/json",
                List.of("dealId")
            ),

            createResourceTemplate(
                "plan://{planId}",
                "Commission Plan by ID",
                "Get a specific commission plan by its ID",
                "application/json",
                List.of("planId")
            ),

            createResourceTemplate(
                "dispute://{disputeId}",
                "Dispute by ID",
                "Get a specific dispute by its ID",
                "application/json",
                List.of("disputeId")
            ),

            createResourceTemplate(
                "calculation://{calculationId}",
                "Calculation by ID",
                "Get a specific commission calculation by its ID",
                "application/json",
                List.of("calculationId")
            ),

            createResourceTemplate(
                "deals-by-rep://{salesRepId}",
                "Deals by Sales Rep",
                "Get all deals for a specific sales representative",
                "application/json",
                List.of("salesRepId")
            ),

            createResourceTemplate(
                "calculations-by-rep://{salesRepId}",
                "Calculations by Sales Rep",
                "Get all commission calculations for a specific sales representative",
                "application/json",
                List.of("salesRepId")
            ),

            createResourceTemplate(
                "disputes-by-rep://{salesRepId}",
                "Disputes by Sales Rep",
                "Get all disputes for a specific sales representative",
                "application/json",
                List.of("salesRepId")
            ),

            createResourceTemplate(
                "currency-rates://{baseCurrency}",
                "Exchange Rates by Base Currency",
                "Get latest exchange rates for a specific base currency (e.g., USD, EUR)",
                "application/json",
                List.of("baseCurrency")
            )
        );
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
                "currency://supported",
                "Supported Currencies",
                "List of all supported currencies for conversion",
                "application/json",
                "currency/supported"
            ),

            createResource(
                "currency://rates",
                "Latest Exchange Rates",
                "Latest exchange rates with EUR as default base currency",
                "application/json",
                "currency/rates"
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
     * Get resource content by URI (supports both static and template URIs)
     */
    public Map<String, Object> getResourceContent(String uri) {
        try {
            String content;
            String mimeType = "application/json";

            // Check static resources first to avoid conflicts with template URIs
            switch (uri) {
                case "deals://all":
                    content = objectMapper.writeValueAsString(dealService.getAllDeals());
                    break;

                case "deals://active":
                    content = objectMapper.writeValueAsString(
                        dealService.getAllDeals().stream()
                            .filter(deal -> deal.status() == DealStatus.OPEN ||
                                          deal.status() == DealStatus.WON)
                            .toList()
                    );
                    break;

                case "plans://all":
                    content = objectMapper.writeValueAsString(planService.getAllPlans());
                    break;

                case "plans://active":
                    content = objectMapper.writeValueAsString(
                        planService.getPlansByStatus(
                            PlanStatus.ACTIVE
                        )
                    );
                    break;

                case "disputes://all":
                    content = objectMapper.writeValueAsString(disputeService.getAllDisputes());
                    break;

                case "disputes://open":
                    content = objectMapper.writeValueAsString(
                        disputeService.getDisputesByStatus(
                            DisputeStatus.INITIATED
                        )
                    );
                    break;

                case "calculations://all":
                    content = objectMapper.writeValueAsString(calculationService.getAllCalculations());
                    break;

                case "currency://supported":
                    content = currencyService.listSupportedCurrencies().currencies();
                    break;

                case "currency://rates":
                    content = currencyService.getLatestRates(new GetLatestRatesRequest(null, null)).rates();
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

                // Check for template URIs if no static resource matches
                default:
                    if (uri.startsWith("deal://")) {
                        String dealId = uri.substring("deal://".length());
                        content = objectMapper.writeValueAsString(dealService.getDeal(dealId));
                    }
                    else if (uri.startsWith("plan://")) {
                        String planId = uri.substring("plan://".length());
                        content = objectMapper.writeValueAsString(planService.getPlan(planId));
                    }
                    else if (uri.startsWith("dispute://")) {
                        String disputeId = uri.substring("dispute://".length());
                        content = objectMapper.writeValueAsString(disputeService.getDispute(disputeId));
                    }
                    else if (uri.startsWith("calculation://")) {
                        String calculationId = uri.substring("calculation://".length());
                        content = objectMapper.writeValueAsString(calculationService.getCalculation(calculationId));
                    }
                    else if (uri.startsWith("deals-by-rep://")) {
                        String salesRepId = uri.substring("deals-by-rep://".length());
                        content = objectMapper.writeValueAsString(dealService.getDealsBySalesRep(salesRepId));
                    }
                    else if (uri.startsWith("calculations-by-rep://")) {
                        String salesRepId = uri.substring("calculations-by-rep://".length());
                        content = objectMapper.writeValueAsString(calculationService.getCalculationsBySalesRep(salesRepId));
                    }
                    else if (uri.startsWith("disputes-by-rep://")) {
                        String salesRepId = uri.substring("disputes-by-rep://".length());
                        content = objectMapper.writeValueAsString(disputeService.getDisputesBySalesRep(salesRepId));
                    }
                    else if (uri.startsWith("currency-rates://")) {
                        String baseCurrency = uri.substring("currency-rates://".length());
                        content = currencyService.getLatestRates(
                                new GetLatestRatesRequest(baseCurrency, null)).rates();
                    }
                    else {
                        return Map.of("error", "Resource not found: " + uri);
                    }
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

    private Map<String, Object> createResourceTemplate(
            String uriTemplate,
            String name,
            String description,
            String mimeType,
            List<String> parameters) {

        return Map.of(
            "uriTemplate", uriTemplate,
            "name", name,
            "description", description,
            "mimeType", mimeType,
            "parameters", parameters
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
