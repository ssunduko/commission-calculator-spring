package com.chapman.edu.commissions.architecture.verticalslice.features.currency;

import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ValidationException;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Currency Conversion Service — MCP Client Feature Slice
 *
 * This service acts as an MCP CLIENT that connects to the external
 * currency-conversion-mcp server (https://currency-mcp.wesbos.com)
 * to perform real-time currency conversions and exchange rate lookups.
 *
 * Available remote tools:
 *   - convert_currency: Convert an amount between currency pairs
 *   - get_latest_rates: Fetch current exchange rates
 *   - get_currencies: List all supported currencies
 *   - get_historical_rates: Get rates for a specific past date
 *
 * These are also exposed as local MCP tools via @Tool annotations,
 * so AI agents connected to THIS server can trigger currency lookups.
 */
@Service
public class CurrencyConversionService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyConversionService.class);

    private final McpSyncClient currencyMcpClient;

    public CurrencyConversionService(McpSyncClient currencyMcpClient) {
        this.currencyMcpClient = currencyMcpClient;
    }

    /**
     * Convert an amount from one currency to another.
     */
    @Tool(name = "convertCurrency",
          description = "Convert an amount from one currency to another using real-time exchange rates. " +
                        "Parameters: from (3-letter currency code), to (3-letter currency code), amount (number).")
    public CurrencyConversionResponse convertCurrency(ConvertCurrencyRequest request) {
        if (request == null) {
            throw new ValidationException("Request is required with 'from', 'to', and 'amount' fields");
        }
        request.validate();
        log.info("Converting {} {} to {}", request.amount(), request.from(), request.to());

        McpSchema.CallToolResult result = currencyMcpClient.callTool(new McpSchema.CallToolRequest(
                "convert_currency",
                Map.of(
                        "from", request.from().toUpperCase(),
                        "to", request.to().toUpperCase(),
                        "amount", request.amount()
                )
        ));

        String text = extractText(result);
        return new CurrencyConversionResponse(
                request.from().toUpperCase(),
                request.to().toUpperCase(),
                request.amount(),
                text
        );
    }

    /**
     * Get the latest exchange rates for a base currency.
     */
    @Tool(name = "getLatestRates",
          description = "Fetch the latest exchange rates. Optional base currency (default EUR) " +
                        "and optional comma-separated symbols to filter (e.g. 'USD,GBP,JPY').")
    public ExchangeRateResponse getLatestRates(GetLatestRatesRequest request) {
        if (request == null) {
            request = new GetLatestRatesRequest(null, null);
        }
        log.info("Fetching latest rates for base={}, symbols={}", request.base(), request.symbols());

        Map<String, Object> args = new java.util.HashMap<>();
        if (request.base() != null && !request.base().isBlank()) {
            args.put("base", request.base().toUpperCase());
        }
        if (request.symbols() != null && !request.symbols().isBlank()) {
            args.put("symbols", request.symbols().toUpperCase());
        }

        McpSchema.CallToolResult result = currencyMcpClient.callTool(new McpSchema.CallToolRequest(
                "get_latest_rates", args
        ));

        return new ExchangeRateResponse(
                request.base() != null ? request.base().toUpperCase() : "EUR",
                extractText(result)
        );
    }

    /**
     * List all supported currencies.
     */
    @Tool(name = "listSupportedCurrencies",
          description = "List all supported currencies with their full names.")
    public SupportedCurrenciesResponse listSupportedCurrencies() {
        log.info("Fetching supported currencies");

        McpSchema.CallToolResult result = currencyMcpClient.callTool(new McpSchema.CallToolRequest(
                "get_currencies", Map.of()
        ));

        return new SupportedCurrenciesResponse(extractText(result));
    }

    /**
     * Get historical exchange rates for a specific date.
     */
    @Tool(name = "getHistoricalRates",
          description = "Get historical exchange rates for a specific date. " +
                        "Parameters: date (YYYY-MM-DD format), optional base currency, optional symbols filter.")
    public ExchangeRateResponse getHistoricalRates(GetHistoricalRatesRequest request) {
        if (request == null || request.date() == null || request.date().isBlank()) {
            throw new ValidationException("Date is required (YYYY-MM-DD format)");
        }
        log.info("Fetching historical rates for date={}, base={}", request.date(), request.base());

        Map<String, Object> args = new java.util.HashMap<>();
        args.put("date", request.date());
        if (request.base() != null && !request.base().isBlank()) {
            args.put("base", request.base().toUpperCase());
        }
        if (request.symbols() != null && !request.symbols().isBlank()) {
            args.put("symbols", request.symbols().toUpperCase());
        }

        McpSchema.CallToolResult result = currencyMcpClient.callTool(new McpSchema.CallToolRequest(
                "get_historical_rates", args
        ));

        return new ExchangeRateResponse(
                request.base() != null ? request.base().toUpperCase() : "EUR",
                extractText(result)
        );
    }

    /**
     * List the tools available on the remote currency MCP server.
     */
    public List<McpSchema.Tool> listRemoteTools() {
        McpSchema.ListToolsResult result = currencyMcpClient.listTools();
        return result.tools();
    }

    private String extractText(McpSchema.CallToolResult result) {
        if (result.content() == null || result.content().isEmpty()) {
            return "No response from currency server";
        }
        return result.content().stream()
                .filter(c -> c instanceof McpSchema.TextContent)
                .map(c -> ((McpSchema.TextContent) c).text())
                .reduce("", (a, b) -> a.isEmpty() ? b : a + "\n" + b);
    }
}
