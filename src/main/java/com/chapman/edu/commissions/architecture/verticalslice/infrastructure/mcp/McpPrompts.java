package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.mcp;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * MCP Prompts Service
 * Provides predefined prompts that AI agents can use for common commission calculation tasks.
 */
@Service
public class McpPrompts {

    /**
     * Get all available prompts
     */
    public List<Map<String, Object>> getAllPrompts() {
        return List.of(
            createPrompt(
                "analyze-sales-performance",
                "Analyze Sales Performance",
                "Analyze the sales performance of a specific sales representative. Includes total deals, win rate, and commission calculations.",
                List.of(
                    createArgument("salesRepId", "Sales representative ID to analyze", true),
                    createArgument("period", "Time period (e.g., 'last-month', 'last-quarter', 'ytd')", false)
                ),
                "Analyze sales performance for sales rep {{salesRepId}}. " +
                "Get all deals for this rep, calculate total value, count won vs lost deals, " +
                "and retrieve all commission calculations. Summarize findings with key metrics."
            ),

            createPrompt(
                "create-commission-workflow",
                "Create Commission Workflow",
                "Complete workflow to create a deal, apply a commission plan, and calculate commission.",
                List.of(
                    createArgument("dealTitle", "Title of the deal", true),
                    createArgument("dealValue", "Value of the deal in USD", true),
                    createArgument("salesRepId", "Sales representative ID", true),
                    createArgument("planId", "Commission plan ID to apply", true)
                ),
                "Create a new deal titled '{{dealTitle}}' with value ${{dealValue}} for rep {{salesRepId}}. " +
                "Then calculate commission using plan {{planId}}. Return the commission calculation details."
            ),

            createPrompt(
                "dispute-resolution",
                "Dispute Resolution Workflow",
                "Handle the complete dispute resolution process for a commission calculation.",
                List.of(
                    createArgument("calculationId", "Commission calculation ID", true),
                    createArgument("disputeReason", "Reason for the dispute", true),
                    createArgument("salesRepId", "Sales representative ID", true)
                ),
                "Create a dispute for calculation {{calculationId}} on behalf of rep {{salesRepId}}. " +
                "Reason: {{disputeReason}}. Then retrieve the dispute details and provide resolution recommendations " +
                "based on the calculation breakdown."
            ),

            createPrompt(
                "setup-commission-plan",
                "Setup Commission Plan",
                "Create a new commission plan with rules and activate it.",
                List.of(
                    createArgument("planName", "Name of the commission plan", true),
                    createArgument("currency", "Currency code (e.g., USD)", true),
                    createArgument("baseRate", "Base commission rate percentage", true),
                    createArgument("ruleType", "Type of rule (STANDARD, ACCELERATOR, BONUS)", false)
                ),
                "Create a commission plan named '{{planName}}' in {{currency}}. " +
                "Add a {{ruleType}} rule with {{baseRate}}% commission rate. " +
                "Then activate the plan and return its details."
            ),

            createPrompt(
                "monthly-commission-report",
                "Monthly Commission Report",
                "Generate a comprehensive monthly commission report for a sales rep.",
                List.of(
                    createArgument("salesRepId", "Sales representative ID", true),
                    createArgument("month", "Month in YYYY-MM format", true)
                ),
                "For sales rep {{salesRepId}} in {{month}}: " +
                "1. Get all deals closed in this period\n" +
                "2. Get all commission calculations\n" +
                "3. Get any disputes raised\n" +
                "4. Calculate total commissions (base, adjusted, final)\n" +
                "5. Summarize in a formatted report with key metrics and insights."
            ),

            createPrompt(
                "compare-plans",
                "Compare Commission Plans",
                "Compare multiple commission plans to help choose the best one for a deal.",
                List.of(
                    createArgument("dealId", "Deal ID to calculate commission for", true),
                    createArgument("planIds", "Comma-separated list of plan IDs to compare", true)
                ),
                "Calculate commission for deal {{dealId}} using each plan in {{planIds}}. " +
                "Compare the results side-by-side showing base commission, adjustments, and final amounts. " +
                "Recommend the best plan and explain why."
            ),

            createPrompt(
                "audit-calculation",
                "Audit Commission Calculation",
                "Perform a detailed audit of a commission calculation.",
                List.of(
                    createArgument("calculationId", "Commission calculation ID to audit", true)
                ),
                "Retrieve calculation {{calculationId}} and perform a detailed audit:\n" +
                "1. Verify the deal details and status\n" +
                "2. Check the commission plan and rules applied\n" +
                "3. Validate the calculation logic and breakdown\n" +
                "4. Check for any disputes or adjustments\n" +
                "5. Provide an audit report with findings and recommendations."
            ),

            createPrompt(
                "convert-deal-currency",
                "Convert Deal Commission to Another Currency",
                "Calculate commission for a deal and convert the result to a different currency using real-time exchange rates. " +
                "Uses tools: calculateCommission, convertCurrency. Resources: deal://{dealId}, plan://{planId}.",
                List.of(
                    createArgument("dealId", "Deal ID to calculate commission for", true),
                    createArgument("planId", "Commission plan ID to use", true),
                    createArgument("targetCurrency", "Target currency code (e.g., EUR, GBP, JPY)", true)
                ),
                "1. Use tool getDeal to retrieve deal {{dealId}} details\n" +
                "2. Use tool getCommissionPlan to retrieve plan {{planId}} details\n" +
                "3. Use tool calculateCommission with dealId={{dealId}} and planId={{planId}}\n" +
                "4. Use tool convertCurrency with from=USD, to={{targetCurrency}}, and the commission amount\n" +
                "5. Present both the original and converted amounts with the exchange rate used."
            ),

            createPrompt(
                "multi-currency-commission-report",
                "Multi-Currency Commission Report",
                "Generate a commission report for a sales rep with amounts converted to a target currency. " +
                "Uses tools: getCalculationsBySalesRep, getDealsBySalesRep, getLatestRates, convertCurrency. " +
                "Resources: calculations-by-rep://{salesRepId}, deals-by-rep://{salesRepId}, currency-rates://{targetCurrency}.",
                List.of(
                    createArgument("salesRepId", "Sales representative ID", true),
                    createArgument("targetCurrency", "Currency to convert all amounts to (e.g., EUR, GBP)", true)
                ),
                "For sales rep {{salesRepId}}:\n" +
                "1. Use tool getCalculationsBySalesRep to get all commission calculations\n" +
                "2. Use tool getDealsBySalesRep to get associated deal details\n" +
                "3. Use tool getLatestRates with base=USD and symbols={{targetCurrency}} to get the exchange rate\n" +
                "4. Use tool convertCurrency for each commission amount from USD to {{targetCurrency}}\n" +
                "5. Present a formatted table with deal name, original USD amounts, converted {{targetCurrency}} amounts, and totals in both currencies."
            ),

            createPrompt(
                "currency-rate-check",
                "Currency Rate Check",
                "Check current and historical exchange rates for commission planning purposes. " +
                "Uses tools: getLatestRates, getHistoricalRates, listSupportedCurrencies. " +
                "Resources: currency://supported, currency://rates.",
                List.of(
                    createArgument("baseCurrency", "Base currency code (e.g., USD)", true),
                    createArgument("targetCurrencies", "Comma-separated target currencies (e.g., EUR,GBP,JPY)", true),
                    createArgument("historicalDate", "Past date for comparison in YYYY-MM-DD format", false)
                ),
                "1. Use tool listSupportedCurrencies to verify the requested currencies are valid\n" +
                "2. Use tool getLatestRates with base={{baseCurrency}} and symbols={{targetCurrencies}} for current rates\n" +
                "3. If historicalDate is provided, use tool getHistoricalRates with date={{historicalDate}}, base={{baseCurrency}}, symbols={{targetCurrencies}}\n" +
                "4. Present the rates side-by-side and highlight any significant changes between historical and current rates\n" +
                "5. Provide insights on how rate changes might affect international commission payments."
            )
        );
    }

    /**
     * Get a specific prompt by name
     */
    public Map<String, Object> getPrompt(String name) {
        return getAllPrompts().stream()
            .filter(p -> p.get("name").equals(name))
            .findFirst()
            .orElse(null);
    }

    private Map<String, Object> createPrompt(
            String name,
            String displayName,
            String description,
            List<Map<String, Object>> arguments,
            String template) {

        return Map.of(
            "name", name,
            "description", description,
            "arguments", arguments,
            "template", template
        );
    }

    private Map<String, Object> createArgument(String name, String description, boolean required) {
        return Map.of(
            "name", name,
            "description", description,
            "required", required
        );
    }
}
