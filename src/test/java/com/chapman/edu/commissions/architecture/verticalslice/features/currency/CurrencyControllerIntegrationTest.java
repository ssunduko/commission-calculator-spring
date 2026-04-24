package com.chapman.edu.commissions.architecture.verticalslice.features.currency;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for Currency Conversion feature.
 * Mocks the external McpSyncClient to avoid hitting the real currency server.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("verticalslice")
@WithMockUser(username = "testuser", roles = {"USER", "ADMIN"})
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testcurrencydb",
    "spring.ai.mcp.server.enabled=true"
})
public class CurrencyControllerIntegrationTest {

    @TestConfiguration
    static class MockMcpClientConfig {
        @Bean
        @Primary
        public McpSyncClient currencyMcpClient() {
            McpSyncClient mockClient = mock(McpSyncClient.class);

            // Default: return a valid response for any tool call
            when(mockClient.callTool(any(McpSchema.CallToolRequest.class)))
                    .thenAnswer(invocation -> {
                        McpSchema.CallToolRequest req = invocation.getArgument(0);
                        String responseText = switch (req.name()) {
                            case "convert_currency" -> "{\"amount\":" + req.arguments().get("amount") +
                                    ",\"base\":\"" + req.arguments().get("from") +
                                    "\",\"date\":\"2026-03-20\",\"rates\":{\"" +
                                    req.arguments().get("to") + "\":0.92}}";
                            case "get_latest_rates" -> "{\"base\":\"" +
                                    req.arguments().getOrDefault("base", "EUR") +
                                    "\",\"date\":\"2026-03-20\",\"rates\":{\"USD\":1.09,\"GBP\":0.86,\"JPY\":161.5}}";
                            case "get_currencies" -> "{\"USD\":\"United States Dollar\",\"EUR\":\"Euro\"," +
                                    "\"GBP\":\"British Pound\",\"JPY\":\"Japanese Yen\",\"CAD\":\"Canadian Dollar\"}";
                            case "get_historical_rates" -> "{\"base\":\"" +
                                    req.arguments().getOrDefault("base", "EUR") +
                                    "\",\"date\":\"" + req.arguments().get("date") +
                                    "\",\"rates\":{\"USD\":1.08,\"GBP\":0.85}}";
                            default -> "{}";
                        };
                        List<McpSchema.Content> content = List.of(new McpSchema.TextContent(responseText));
                        return new McpSchema.CallToolResult(content, Boolean.FALSE);
                    });

            // listTools response
            when(mockClient.listTools()).thenReturn(new McpSchema.ListToolsResult(
                    List.of(
                            McpSchema.Tool.builder().name("convert_currency").description("Convert currency").build(),
                            McpSchema.Tool.builder().name("get_latest_rates").description("Get rates").build(),
                            McpSchema.Tool.builder().name("get_currencies").description("List currencies").build(),
                            McpSchema.Tool.builder().name("get_historical_rates").description("Historical rates").build()
                    ), null));

            return mockClient;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    // ==================== REST Endpoint Tests ====================

    @Test
    void convertCurrency_ShouldReturnConversion() throws Exception {
        mockMvc.perform(post("/api/currency/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"from\":\"USD\",\"to\":\"EUR\",\"amount\":5000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("USD"))
                .andExpect(jsonPath("$.to").value("EUR"))
                .andExpect(jsonPath("$.amount").value(5000))
                .andExpect(jsonPath("$.result").isNotEmpty());
    }

    @Test
    void convertCurrency_WithInvalidRequest_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/currency/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"from\":\"US\",\"to\":\"EUR\",\"amount\":100}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getLatestRates_ShouldReturnRates() throws Exception {
        mockMvc.perform(get("/api/currency/rates")
                .param("base", "USD")
                .param("symbols", "EUR,GBP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseCurrency").value("USD"))
                .andExpect(jsonPath("$.rates").isNotEmpty());
    }

    @Test
    void getLatestRates_WithNoParams_ShouldDefaultToEUR() throws Exception {
        mockMvc.perform(get("/api/currency/rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseCurrency").value("EUR"));
    }

    @Test
    void getSupportedCurrencies_ShouldReturnCurrencyList() throws Exception {
        mockMvc.perform(get("/api/currency/supported"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currencies").isNotEmpty())
                .andExpect(jsonPath("$.currencies", containsString("USD")))
                .andExpect(jsonPath("$.currencies", containsString("EUR")));
    }

    @Test
    void getHistoricalRates_ShouldReturnRatesForDate() throws Exception {
        mockMvc.perform(get("/api/currency/historical")
                .param("date", "2025-01-15")
                .param("base", "USD")
                .param("symbols", "EUR,GBP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseCurrency").value("USD"))
                .andExpect(jsonPath("$.rates", containsString("2025-01-15")));
    }

    @Test
    void getHistoricalRates_WithoutDate_ShouldReturnError() throws Exception {
        // Missing required @RequestParam "date" returns an error status
        mockMvc.perform(get("/api/currency/historical"))
                .andExpect(status().isInternalServerError());
    }

    // ==================== MCP Tool Registration Tests ====================

    @Test
    void currencyToolsShouldBeRegisteredInMcpServer() throws Exception {
        mockMvc.perform(post("/api/mcp/tools/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tools[?(@.name=='convertCurrency')]").exists())
                .andExpect(jsonPath("$.tools[?(@.name=='getLatestRates')]").exists())
                .andExpect(jsonPath("$.tools[?(@.name=='listSupportedCurrencies')]").exists())
                .andExpect(jsonPath("$.tools[?(@.name=='getHistoricalRates')]").exists());
    }

    @Test
    void totalToolCountShouldIncludeCurrencyTools() throws Exception {
        // 31 commerce tools + delegateToDisputeAgent A2A bridge + 3 auth
        // tools (listSubscriptionPackages, registerUser, login) = 35.
        mockMvc.perform(post("/api/mcp/tools/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tools", hasSize(35)));
    }

    // ==================== MCP Tool Invocation Tests ====================

    @Test
    void convertCurrencyViaMcpProtocol_ShouldWork() throws Exception {
        // MCP tool call wraps args in the request DTO structure
        mockMvc.perform(post("/api/mcp/tools/call")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "listSupportedCurrencies",
                        "arguments": {}
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isError").value(false))
                .andExpect(jsonPath("$.content[0].type").value("text"))
                .andExpect(jsonPath("$.content[0].text").isNotEmpty());
    }

    @Test
    void listSupportedCurrenciesViaMcpProtocol_ShouldWork() throws Exception {
        mockMvc.perform(post("/api/mcp/tools/call")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "listSupportedCurrencies",
                        "arguments": {}
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isError").value(false))
                .andExpect(jsonPath("$.content[0].text", containsString("USD")));
    }

    // ==================== MCP Resource Tests ====================

    @Test
    void currencyResourcesShouldBeRegistered() throws Exception {
        mockMvc.perform(post("/api/mcp/resources/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resources[?(@.uri=='currency://supported')]").exists())
                .andExpect(jsonPath("$.resources[?(@.uri=='currency://rates')]").exists());
    }

    @Test
    void readSupportedCurrenciesResource_ShouldReturnData() throws Exception {
        mockMvc.perform(post("/api/mcp/resources/read")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"uri\":\"currency://supported\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contents[0].content", containsString("USD")));
    }

    // ==================== MCP Prompt Tests ====================

    @Test
    void currencyPromptsShouldBeRegistered() throws Exception {
        mockMvc.perform(post("/api/mcp/prompts/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prompts[?(@.name=='convert-deal-currency')]").exists())
                .andExpect(jsonPath("$.prompts[?(@.name=='multi-currency-commission-report')]").exists())
                .andExpect(jsonPath("$.prompts[?(@.name=='currency-rate-check')]").exists());
    }

    @Test
    void convertDealCurrencyPrompt_ShouldHaveRequiredArguments() throws Exception {
        mockMvc.perform(post("/api/mcp/prompts/get")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"convert-deal-currency\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.arguments[?(@.name=='dealId')]").exists())
                .andExpect(jsonPath("$.arguments[?(@.name=='planId')]").exists())
                .andExpect(jsonPath("$.arguments[?(@.name=='targetCurrency')]").exists());
    }
}
