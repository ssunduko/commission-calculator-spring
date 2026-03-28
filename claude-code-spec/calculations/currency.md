# Feature Spec: Currency Conversion (MCP Client)

## Intent

Provide real-time currency conversion by acting as an MCP CLIENT that connects to an external MCP server. This feature demonstrates MCP server chaining — our server consumes another MCP server's tools and re-exposes them as local @Tool methods.

## Architecture Pattern: MCP Client → External MCP Server

```
AI Agent (Claude) ──→ Our MCP Server ──→ External Currency MCP Server
                      @Tool methods       currency-mcp.wesbos.com
                      (local)             (remote, SSE transport)
```

The CurrencyConversionService has a **dual role**:
1. **MCP Client** — calls tools on the external currency server via McpSyncClient
2. **@Tool Provider** — exposes those same capabilities as local tools for AI agents

## External MCP Server

- **URL:** https://currency-mcp.wesbos.com
- **Transport:** SSE (Server-Sent Events)
- **SSE Endpoint:** /sse
- **Remote Tools:** convert_currency, get_latest_rates, get_currencies, get_historical_rates

## Configuration

```properties
app.mcp.currency.base-url=https://currency-mcp.wesbos.com
app.mcp.currency.sse-endpoint=/sse
```

## Operations (Proxied from Remote)

| Local Operation | Remote Tool | Description |
|----------------|-------------|-------------|
| convertCurrency | convert_currency | Convert amount between currency pairs |
| getLatestRates | get_latest_rates | Fetch current exchange rates |
| listSupportedCurrencies | get_currencies | List all supported currencies |
| getHistoricalRates | get_historical_rates | Get rates for a past date |

## Request/Response DTOs

| DTO | Fields | Purpose |
|-----|--------|---------|
| ConvertCurrencyRequest | from(3-char), to(3-char), amount(double) | Currency conversion input |
| GetLatestRatesRequest | base(optional), symbols(optional) | Rate lookup input |
| GetHistoricalRatesRequest | date(YYYY-MM-DD), base, symbols | Historical rate input |
| CurrencyConversionResponse | from, to, amount, result(text) | Conversion result |
| ExchangeRateResponse | baseCurrency, rates(text) | Rate data |
| SupportedCurrenciesResponse | currencies(text) | Currency list |

## REST Endpoints

| Method | Path | Operation |
|--------|------|-----------|
| POST | /api/currency/convert | convertCurrency |
| GET | /api/currency/rates?base=USD&symbols=EUR,GBP | getLatestRates |
| GET | /api/currency/supported | listSupportedCurrencies |
| GET | /api/currency/historical?date=2025-01-15&base=USD | getHistoricalRates |

## MCP Tools (4)

| Tool Name | Description |
|-----------|-------------|
| convertCurrency | Convert amount from one currency to another using real-time rates |
| getLatestRates | Fetch latest exchange rates with optional base and symbols filter |
| listSupportedCurrencies | List all supported currencies with full names |
| getHistoricalRates | Get exchange rates for a specific past date |

## MCP Resources

| URI | Description |
|-----|-------------|
| currency://supported | List of supported currencies |
| currency://rates | Latest exchange rates (EUR base default) |
| currency-rates://{baseCurrency} | Rates for a specific base currency (template) |

## MCP Prompts

| Name | Parameters | Description |
|------|------------|-------------|
| convert-deal-currency | dealId, planId, targetCurrency | Calculate commission and convert to another currency |
| multi-currency-commission-report | salesRepId, targetCurrency | Commission report with currency conversion |
| currency-rate-check | baseCurrency, targetCurrencies, historicalDate | Compare current and historical rates |

## Files

```
features/currency/
  CurrencyMcpClientConfig.java       ← MCP client bean (SSE transport)
  CurrencyConversionService.java     ← Dual role: MCP client + @Tool provider
  CurrencyController.java            ← REST endpoints
  ConvertCurrencyRequest.java        ← Input DTO
  GetLatestRatesRequest.java         ← Input DTO
  GetHistoricalRatesRequest.java     ← Input DTO
  CurrencyConversionResponse.java    ← Output DTO
  ExchangeRateResponse.java          ← Output DTO
  SupportedCurrenciesResponse.java   ← Output DTO
```

## Key Implementation Detail

```java
// CurrencyMcpClientConfig — establishes MCP client connection
@Bean(destroyMethod = "close")
public McpSyncClient currencyMcpClient() {
    HttpClientSseClientTransport transport = HttpClientSseClientTransport
        .builder(baseUrl).sseEndpoint(sseEndpoint).build();
    McpSyncClient client = McpClient.sync(transport)
        .requestTimeout(Duration.ofSeconds(15))
        .clientInfo(new McpSchema.Implementation(
            "commission-calculator-currency-client", "1.0.0"))
        .build();
    client.initialize();
    return client;
}

// CurrencyConversionService — calls remote, exposes locally
@Tool(name = "convertCurrency", description = "...")
public CurrencyConversionResponse convertCurrency(ConvertCurrencyRequest req) {
    McpSchema.CallToolResult result = currencyMcpClient.callTool(
        new McpSchema.CallToolRequest("convert_currency",
            Map.of("from", req.from(), "to", req.to(), "amount", req.amount())));
    return new CurrencyConversionResponse(req.from(), req.to(), req.amount(),
        extractText(result));
}
```