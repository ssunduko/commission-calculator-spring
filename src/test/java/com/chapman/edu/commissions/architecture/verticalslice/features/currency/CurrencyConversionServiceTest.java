package com.chapman.edu.commissions.architecture.verticalslice.features.currency;

import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ValidationException;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionServiceTest {

    @Mock
    private McpSyncClient currencyMcpClient;

    @InjectMocks
    private CurrencyConversionService currencyService;

    private McpSchema.CallToolResult mockTextResult(String text) {
        List<McpSchema.Content> content = List.of(new McpSchema.TextContent(text));
        return new McpSchema.CallToolResult(content, Boolean.FALSE);
    }

    // ==================== convertCurrency ====================

    @Test
    void convertCurrency_WithValidRequest_ShouldReturnConversion() {
        when(currencyMcpClient.callTool(any(McpSchema.CallToolRequest.class)))
                .thenReturn(mockTextResult("{\"amount\":100,\"base\":\"USD\",\"date\":\"2026-03-20\",\"rates\":{\"EUR\":0.92}}"));

        var response = currencyService.convertCurrency(
                new ConvertCurrencyRequest("USD", "EUR", 100));

        assertThat(response.from()).isEqualTo("USD");
        assertThat(response.to()).isEqualTo("EUR");
        assertThat(response.amount()).isEqualTo(100);
        assertThat(response.result()).contains("EUR");

        verify(currencyMcpClient).callTool(argThat(req ->
                req.name().equals("convert_currency") &&
                req.arguments().get("from").equals("USD") &&
                req.arguments().get("to").equals("EUR") &&
                req.arguments().get("amount").equals(100.0)));
    }

    @Test
    void convertCurrency_ShouldUppercaseCurrencyCodes() {
        when(currencyMcpClient.callTool(any(McpSchema.CallToolRequest.class)))
                .thenReturn(mockTextResult("92.00 EUR"));

        currencyService.convertCurrency(new ConvertCurrencyRequest("usd", "eur", 100));

        verify(currencyMcpClient).callTool(argThat(req ->
                req.arguments().get("from").equals("USD") &&
                req.arguments().get("to").equals("EUR")));
    }

    @Test
    void convertCurrency_WithInvalidFrom_ShouldThrowValidation() {
        assertThatThrownBy(() ->
                currencyService.convertCurrency(new ConvertCurrencyRequest("US", "EUR", 100)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("from");
    }

    @Test
    void convertCurrency_WithInvalidTo_ShouldThrowValidation() {
        assertThatThrownBy(() ->
                currencyService.convertCurrency(new ConvertCurrencyRequest("USD", "", 100)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("to");
    }

    @Test
    void convertCurrency_WithNegativeAmount_ShouldThrowValidation() {
        assertThatThrownBy(() ->
                currencyService.convertCurrency(new ConvertCurrencyRequest("USD", "EUR", -50)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("amount");
    }

    @Test
    void convertCurrency_WithZeroAmount_ShouldThrowValidation() {
        assertThatThrownBy(() ->
                currencyService.convertCurrency(new ConvertCurrencyRequest("USD", "EUR", 0)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("amount");
    }

    // ==================== getLatestRates ====================

    @Test
    void getLatestRates_WithBaseAndSymbols_ShouldCallRemoteTool() {
        when(currencyMcpClient.callTool(any(McpSchema.CallToolRequest.class)))
                .thenReturn(mockTextResult("{\"base\":\"USD\",\"rates\":{\"EUR\":0.92,\"GBP\":0.79}}"));

        var response = currencyService.getLatestRates(
                new GetLatestRatesRequest("USD", "EUR,GBP"));

        assertThat(response.baseCurrency()).isEqualTo("USD");
        assertThat(response.rates()).contains("EUR");

        verify(currencyMcpClient).callTool(argThat(req ->
                req.name().equals("get_latest_rates") &&
                req.arguments().get("base").equals("USD") &&
                req.arguments().get("symbols").equals("EUR,GBP")));
    }

    @Test
    void getLatestRates_WithNoBase_ShouldDefaultToEUR() {
        when(currencyMcpClient.callTool(any(McpSchema.CallToolRequest.class)))
                .thenReturn(mockTextResult("{\"base\":\"EUR\",\"rates\":{\"USD\":1.09}}"));

        var response = currencyService.getLatestRates(
                new GetLatestRatesRequest(null, null));

        assertThat(response.baseCurrency()).isEqualTo("EUR");
    }

    @Test
    void getLatestRates_WithBlankBase_ShouldNotIncludeInArgs() {
        when(currencyMcpClient.callTool(any(McpSchema.CallToolRequest.class)))
                .thenReturn(mockTextResult("rates"));

        currencyService.getLatestRates(new GetLatestRatesRequest("", ""));

        verify(currencyMcpClient).callTool(argThat(req ->
                req.name().equals("get_latest_rates") &&
                !req.arguments().containsKey("base") &&
                !req.arguments().containsKey("symbols")));
    }

    // ==================== listSupportedCurrencies ====================

    @Test
    void listSupportedCurrencies_ShouldCallRemoteTool() {
        when(currencyMcpClient.callTool(any(McpSchema.CallToolRequest.class)))
                .thenReturn(mockTextResult("{\"USD\":\"United States Dollar\",\"EUR\":\"Euro\"}"));

        var response = currencyService.listSupportedCurrencies();

        assertThat(response.currencies()).contains("USD", "EUR");

        verify(currencyMcpClient).callTool(argThat(req ->
                req.name().equals("get_currencies") &&
                req.arguments().isEmpty()));
    }

    // ==================== getHistoricalRates ====================

    @Test
    void getHistoricalRates_WithValidDate_ShouldCallRemoteTool() {
        when(currencyMcpClient.callTool(any(McpSchema.CallToolRequest.class)))
                .thenReturn(mockTextResult("{\"base\":\"USD\",\"date\":\"2025-01-15\",\"rates\":{\"EUR\":0.91}}"));

        var response = currencyService.getHistoricalRates(
                new GetHistoricalRatesRequest("2025-01-15", "USD", "EUR"));

        assertThat(response.baseCurrency()).isEqualTo("USD");
        assertThat(response.rates()).contains("2025-01-15");

        verify(currencyMcpClient).callTool(argThat(req ->
                req.name().equals("get_historical_rates") &&
                req.arguments().get("date").equals("2025-01-15") &&
                req.arguments().get("base").equals("USD") &&
                req.arguments().get("symbols").equals("EUR")));
    }

    @Test
    void getHistoricalRates_WithNullDate_ShouldThrowValidation() {
        assertThatThrownBy(() ->
                currencyService.getHistoricalRates(
                        new GetHistoricalRatesRequest(null, "USD", null)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Date");
    }

    @Test
    void getHistoricalRates_WithBlankDate_ShouldThrowValidation() {
        assertThatThrownBy(() ->
                currencyService.getHistoricalRates(
                        new GetHistoricalRatesRequest("", "USD", null)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Date");
    }

    @Test
    void getHistoricalRates_WithOptionalParams_ShouldOmitNulls() {
        when(currencyMcpClient.callTool(any(McpSchema.CallToolRequest.class)))
                .thenReturn(mockTextResult("rates"));

        currencyService.getHistoricalRates(
                new GetHistoricalRatesRequest("2025-06-01", null, null));

        verify(currencyMcpClient).callTool(argThat(req ->
                req.arguments().get("date").equals("2025-06-01") &&
                !req.arguments().containsKey("base") &&
                !req.arguments().containsKey("symbols")));
    }

    // ==================== listRemoteTools ====================

    @Test
    void listRemoteTools_ShouldReturnToolList() {
        var tools = List.of(
                McpSchema.Tool.builder().name("convert_currency").description("Convert currency").build(),
                McpSchema.Tool.builder().name("get_latest_rates").description("Get rates").build(),
                McpSchema.Tool.builder().name("get_currencies").description("List currencies").build(),
                McpSchema.Tool.builder().name("get_historical_rates").description("Historical rates").build()
        );
        when(currencyMcpClient.listTools())
                .thenReturn(new McpSchema.ListToolsResult(tools, null));

        var result = currencyService.listRemoteTools();

        assertThat(result).hasSize(4);
        assertThat(result.stream().map(McpSchema.Tool::name).toList())
                .containsExactlyInAnyOrder(
                        "convert_currency", "get_latest_rates",
                        "get_currencies", "get_historical_rates");
    }

    // ==================== Edge cases ====================

    @Test
    void convertCurrency_WhenRemoteReturnsEmpty_ShouldReturnFallbackMessage() {
        List<McpSchema.Content> emptyContent = List.of();
        when(currencyMcpClient.callTool(any(McpSchema.CallToolRequest.class)))
                .thenReturn(new McpSchema.CallToolResult(emptyContent, Boolean.FALSE));

        var response = currencyService.convertCurrency(
                new ConvertCurrencyRequest("USD", "EUR", 100));

        assertThat(response.result()).isEqualTo("No response from currency server");
    }

    @Test
    void convertCurrency_WhenRemoteReturnsNull_ShouldReturnFallbackMessage() {
        List<McpSchema.Content> nullContent = null;
        when(currencyMcpClient.callTool(any(McpSchema.CallToolRequest.class)))
                .thenReturn(new McpSchema.CallToolResult(nullContent, Boolean.FALSE));

        var response = currencyService.convertCurrency(
                new ConvertCurrencyRequest("USD", "EUR", 100));

        assertThat(response.result()).isEqualTo("No response from currency server");
    }
}
