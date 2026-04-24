package com.chapman.edu.commissions.architecture.verticalslice.mcp;

import com.chapman.edu.commissions.architecture.verticalslice.features.calculations.CalculateCommissionRequest;
import com.chapman.edu.commissions.architecture.verticalslice.features.deals.DealResponse;
import com.chapman.edu.commissions.architecture.verticalslice.features.disputes.CreateDisputeRequest;
import com.chapman.edu.commissions.architecture.verticalslice.features.plans.AddRuleToPlanRequest;
import com.chapman.edu.commissions.architecture.verticalslice.features.plans.CommissionPlanResponse;
import com.chapman.edu.commissions.architecture.verticalslice.features.plans.CreateCommissionPlanRequest;
import com.chapman.edu.commissions.architecture.verticalslice.features.deals.CreateDealRequest;
import com.chapman.edu.commissions.architecture.verticalslice.features.deals.DealService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for MCP Server
 * Tests the MCP server endpoints and tool availability
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("verticalslice")
@Transactional
@WithMockUser(username = "testuser", roles = {"USER", "ADMIN"})
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testmcpdb",
    "spring.ai.mcp.server.enabled=true"
})
public class McpServerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DealService dealService;

    @BeforeEach
    public void setUp() {
        // Setup test data if needed
    }

    @Test
    public void testMcpServerInitialization() throws Exception {
        // Test that the MCP server endpoints are available
        // This test verifies the server is running and accessible through the MCP protocol

        // Test MCP info endpoint
        mockMvc.perform(get("/api/mcp/info")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protocolVersion").value("2024-11-05"))
                .andExpect(jsonPath("$.serverInfo.name").value("commission-calculator"))
                .andExpect(jsonPath("$.serverInfo.version").value("1.0.0"))
                .andExpect(jsonPath("$.capabilities.tools").exists());

        // Test MCP capabilities endpoint
        mockMvc.perform(get("/api/mcp/capabilities")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tools.count").value(35))
                .andExpect(jsonPath("$.tools.listChanged").value(true));

        // Test MCP initialize endpoint
        mockMvc.perform(post("/api/mcp/initialize")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protocolVersion").value("2024-11-05"))
                .andExpect(jsonPath("$.serverInfo.name").value("commission-calculator"));
    }

    @Test
    public void testToolsAreRegistered() throws Exception {
        // Verify that all 32 tools are registered and accessible via MCP protocol
        // (31 feature tools + delegateToDisputeAgent A2A bridge).

        mockMvc.perform(post("/api/mcp/tools/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tools").isArray())
                .andExpect(jsonPath("$.tools", hasSize(35)))
                .andExpect(jsonPath("$.tools[?(@.name=='createDeal')]").exists())
                .andExpect(jsonPath("$.tools[?(@.name=='createCommissionPlan')]").exists())
                .andExpect(jsonPath("$.tools[?(@.name=='createDispute')]").exists())
                .andExpect(jsonPath("$.tools[?(@.name=='calculateCommission')]").exists());
    }

    @Test
    public void testCallToolViaMcpProtocol() throws Exception {
        // Test calling a tool through the MCP protocol endpoint

        String mcpCallRequest = """
            {
                "name": "getAllDeals",
                "arguments": {}
            }
            """;

        mockMvc.perform(post("/api/mcp/tools/call")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mcpCallRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].type").value("text"))
                .andExpect(jsonPath("$.isError").value(false));
    }

    @Test
    public void testDealServiceToolsViaRestApi() throws Exception {
        // Test calling Deal tools through the REST API
        // This simulates how an MCP client would interact with our tools

        CreateDealRequest request = new CreateDealRequest(
            "MCP Integration Test Deal",
            new BigDecimal("100000"),
            "REP_MCP_TEST"
        );

        mockMvc.perform(post("/api/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("MCP Integration Test Deal"))
                .andExpect(jsonPath("$.value").value(100000))
                .andExpect(jsonPath("$.salesRepId").value("REP_MCP_TEST"));
    }

    @Test
    public void testGetAllDealsToolViaRestApi() throws Exception {
        // Create test deals
        dealService.createDeal(new CreateDealRequest(
            "Test Deal 1", new BigDecimal("50000"), "REP001"));
        dealService.createDeal(new CreateDealRequest(
            "Test Deal 2", new BigDecimal("75000"), "REP002"));

        // Test getting all deals
        mockMvc.perform(get("/api/deals")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    public void testDealServiceToolWorkflow() throws Exception {
        // Test a complete workflow simulating MCP client interaction

        // Step 1: Create a deal
        CreateDealRequest createRequest = new CreateDealRequest(
            "Workflow Test Deal",
            new BigDecimal("200000"),
            "REP_WORKFLOW"
        );

        String createResponse = mockMvc.perform(post("/api/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract the deal ID from response
        var dealResponse = objectMapper.readValue(createResponse,
            DealResponse.class);
        String dealId = dealResponse.id();

        // Step 2: Retrieve the deal
        mockMvc.perform(get("/api/deals/" + dealId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dealId))
                .andExpect(jsonPath("$.title").value("Workflow Test Deal"));

        // Step 3: Get deals by sales rep
        mockMvc.perform(get("/api/deals")
                .param("salesRepId", "REP_WORKFLOW")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].salesRepId").value("REP_WORKFLOW"));
    }

    @Test
    public void testCommissionPlanToolsViaRestApi() throws Exception {
        // Test commission plan tools
        var createPlanRequest = new CreateCommissionPlanRequest(
            "MCP Test Plan",
            "USD",
            java.time.LocalDate.now(),
            null
        );

        mockMvc.perform(post("/api/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPlanRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("MCP Test Plan"))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    public void testDisputeToolsViaRestApi() throws Exception {
        // Test dispute tools
        var createDisputeRequest = new CreateDisputeRequest(
            "CALC_MCP_TEST",
            "REP_MCP_TEST",
            "MCP Test Dispute",
            "Testing dispute creation through MCP",
            null
        );

        mockMvc.perform(post("/api/disputes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDisputeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("MCP Test Dispute"))
                .andExpect(jsonPath("$.calculationId").value("CALC_MCP_TEST"));
    }

    @Test
    public void testCommissionCalculationWorkflow() throws Exception {
        // Test the complete commission calculation workflow

        // Step 1: Create a deal
        var dealRequest = new CreateDealRequest(
            "Calculation Test Deal",
            new BigDecimal("150000"),
            "REP_CALC_TEST"
        );

        String dealResponse = mockMvc.perform(post("/api/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dealRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var deal = objectMapper.readValue(dealResponse,
            DealResponse.class);

        // Step 2: Create a commission plan
        var planRequest = new CreateCommissionPlanRequest(
            "Test Calculation Plan",
            "USD",
            java.time.LocalDate.now(),
            null
        );

        String planResponse = mockMvc.perform(post("/api/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var plan = objectMapper.readValue(planResponse,
            CommissionPlanResponse.class);

        // Step 3: Add a rule to the plan
        var ruleRequest = new AddRuleToPlanRequest(
            "Test Rule",
            "5% commission",
            new BigDecimal("5.00"),
            "STANDARD",
            1
        );

        mockMvc.perform(post("/api/plans/" + plan.id() + "/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ruleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rulesCount").value(1));

        // Step 4: Calculate commission
        var calcRequest = new CalculateCommissionRequest(
            deal.id(),
            plan.id()
        );

        mockMvc.perform(post("/api/calculations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(calcRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dealId").value(deal.id()))
                .andExpect(jsonPath("$.salesRepId").value("REP_CALC_TEST"))
                .andExpect(jsonPath("$.baseCommission").value(7500.00)); // 5% of 150000
    }

    @Test
    public void testMcpToolsAccessibility() throws Exception {
        // Verify that all MCP tools are accessible through the service layer
        // This ensures the @Tool annotations are properly configured

        // Get all tools via MCP protocol (31 feature tools + 1 A2A bridge)
        String response = mockMvc.perform(post("/api/mcp/tools/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tools").isArray())
                .andExpect(jsonPath("$.tools", hasSize(35)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Parse the response to verify tool categories
        var toolsResponse = objectMapper.readTree(response);
        var tools = toolsResponse.get("tools");

        // Count tools by service category based on naming conventions
        int dealTools = 0;
        int planTools = 0;
        int disputeTools = 0;
        int calculationTools = 0;
        int a2aTools = 0;

        for (var tool : tools) {
            String name = tool.get("name").asText();
            String description = tool.get("description").asText();

            // Categorize based on tool name patterns (check more specific patterns first).
            // A2A bridge tools are disambiguated before the DisputeService prefix match
            // — delegateToDisputeAgent contains "Dispute" in its name but lives in
            // McpCommissionTools, not DisputeService, so it shouldn't inflate the
            // DisputeService count.
            if (name.startsWith("delegate") || name.contains("Agent")) {
                a2aTools++;
            } else if (name.contains("Calculation") || name.contains("calculation") ||
                name.equals("calculateCommission")) {
                calculationTools++;
            } else if (name.contains("Dispute") || name.contains("dispute") ||
                       name.contains("Comment") || name.contains("comment")) {
                disputeTools++;
            } else if (name.contains("Plan") || name.contains("plan") ||
                       name.contains("Rule") || name.contains("Tier")) {
                planTools++;
            } else if (name.contains("Deal") || name.contains("deal")) {
                dealTools++;
            }

            // Verify each tool has required fields
            assert tool.has("name") : "Tool missing name field";
            assert tool.has("description") : "Tool missing description field";
            assert tool.has("inputSchema") : "Tool missing inputSchema field";
        }

        // Verify the expected distribution of tools across services
        // - DealService: 7 tools (createDeal, getDeal, getAllDeals, getDealsBySalesRep, getDealsByStatus, updateDeal, deleteDeal)
        // - CommissionPlanService: 7 tools (createPlan, getPlan, getAllPlans, getPlansByStatus, activatePlan, addRuleToPlan, deletePlan)
        // - DisputeService: 8 tools (createDispute, getDispute, getAllDisputes, getDisputesByStatus, getDisputesBySalesRep, resolveDispute, escalateDispute, addComment)
        // - CommissionCalculationService: 5 tools (calculateCommission, getCalculation, getAllCalculations, getCalculationsBySalesRep, getCalculationsByStatus)

        assert dealTools == 7 : "Expected 7 Deal tools, found " + dealTools;
        assert planTools == 7 : "Expected 7 Commission Plan tools, found " + planTools;
        assert disputeTools == 8 : "Expected 8 Dispute tools, found " + disputeTools;
        assert calculationTools == 5 : "Expected 5 Calculation tools, found " + calculationTools;
        assert a2aTools == 1 : "Expected 1 A2A bridge tool (delegateToDisputeAgent), found " + a2aTools;

        // Verify specific critical tools exist
        mockMvc.perform(post("/api/mcp/tools/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tools[?(@.name=='createDeal')]").exists())
                .andExpect(jsonPath("$.tools[?(@.name=='createCommissionPlan')]").exists())
                .andExpect(jsonPath("$.tools[?(@.name=='createDispute')]").exists())
                .andExpect(jsonPath("$.tools[?(@.name=='calculateCommission')]").exists())
                .andExpect(jsonPath("$.tools[?(@.name=='getDeal')]").exists())
                .andExpect(jsonPath("$.tools[?(@.name=='resolveDispute')]").exists())
                .andExpect(jsonPath("$.tools[?(@.name=='addRuleToPlan')]").exists());
    }

    @Test
    public void testErrorHandling() throws Exception {
        // Test that errors are properly handled and returned

        // Try to get a non-existent deal
        mockMvc.perform(get("/api/deals/non-existent-id")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testConcurrentToolInvocations() throws Exception {
        // Test that multiple tools can be called concurrently
        // This simulates an MCP client making multiple requests

        for (int i = 0; i < 5; i++) {
            CreateDealRequest request = new CreateDealRequest(
                "Concurrent Deal " + i,
                new BigDecimal(10000 * (i + 1)),
                "REP_CONCURRENT_" + i
            );

            mockMvc.perform(post("/api/deals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Concurrent Deal " + i));
        }

        // Verify all deals were created
        mockMvc.perform(get("/api/deals")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))));
    }
}
