# Infrastructure Spec: MCP Server Layer

## Intent

Expose all commission calculator features to AI agents via the Model Context Protocol. The MCP layer is a thin facade that wraps feature services — it contains NO business logic of its own.

## Architecture

```
infrastructure/mcp/
  McpCommissionTools.java     ← @Tool facade (wraps all feature services)
  McpPrompts.java             ← 10 prompt templates for common workflows
  McpResources.java           ← 12 data resources + 8 resource templates
  McpProtocolController.java  ← REST-based MCP protocol endpoints
  McpSseController.java       ← SSE transport for MCP protocol
```

## Design Principle: Zero Business Logic in MCP Layer

```java
// CORRECT — MCP tool delegates to feature service
@Tool(name = "createDeal", description = "...")
public DealResponse createDeal(CreateDealRequest request) {
    return dealService.createDeal(request);  // Pure delegation
}

// WRONG — never put logic in the MCP layer
@Tool(name = "createDeal", description = "...")
public DealResponse createDeal(CreateDealRequest request) {
    if (request.value() > 1000000) { ... }  // ❌ Business logic belongs in DealService
    Deal deal = new Deal(...);               // ❌ Entity creation belongs in DealService
}
```

## McpCommissionTools — Tool Facade

Total: 27 @Tool methods (+ 4 currency tools on CurrencyConversionService = 31 total)

### Deal Management (7 tools)

| Tool | Delegates To | Return Type |
|------|-------------|-------------|
| createDeal | dealService.createDeal() | DealResponse |
| getDeal | dealService.getDeal() | DealResponse |
| getAllDeals | dealService.getAllDeals() | List\<DealResponse\> |
| getDealsBySalesRep | dealService.getDealsBySalesRep() | List\<DealResponse\> |
| getDealsByStatus | dealService.getDealsByStatus() | List\<DealResponse\> |
| updateDeal | dealService.updateDeal() | DealResponse |
| deleteDeal | dealService.deleteDeal() | void |

### Commission Plan Management (7 tools)

| Tool | Delegates To | Return Type |
|------|-------------|-------------|
| createCommissionPlan | planService.createPlan() | CommissionPlanResponse |
| getCommissionPlan | planService.getPlan() | CommissionPlanResponse |
| getAllCommissionPlans | planService.getAllPlans() | List\<CommissionPlanResponse\> |
| getCommissionPlansByStatus | planService.getPlansByStatus() | List\<CommissionPlanResponse\> |
| activateCommissionPlan | planService.activatePlan() | CommissionPlanResponse |
| addRuleToPlan | planService.addRuleToPlan() | CommissionPlanResponse |
| deleteCommissionPlan | planService.deletePlan() | void |

### Dispute Management (8 tools)

| Tool | Delegates To | Return Type |
|------|-------------|-------------|
| createDispute | disputeService.createDispute() | DisputeResponse |
| getDispute | disputeService.getDispute() | DisputeResponse |
| getAllDisputes | disputeService.getAllDisputes() | List\<DisputeResponse\> |
| getDisputesBySalesRep | disputeService.getDisputesBySalesRep() | List\<DisputeResponse\> |
| getDisputesByStatus | disputeService.getDisputesByStatus() | List\<DisputeResponse\> |
| resolveDispute | disputeService.resolveDispute() | DisputeResponse |
| escalateDispute | disputeService.escalateDispute() | DisputeResponse |
| deleteDispute | disputeService.deleteDispute() | void |

### Commission Calculation (5 tools)

| Tool | Delegates To | Return Type |
|------|-------------|-------------|
| calculateCommission | calcService.calculateCommission() | CommissionCalculationResponse |
| getCommissionCalculation | calcService.getCalculation() | CommissionCalculationResponse |
| getAllCommissionCalculations | calcService.getAllCalculations() | List\<CommissionCalculationResponse\> |
| getCalculationsBySalesRep | calcService.getCalculationsBySalesRep() | List\<CommissionCalculationResponse\> |
| getCalculationsByDeal | calcService.getCalculationsByDeal() | List\<CommissionCalculationResponse\> |

## Tool Registration (Application Wiring)

```java
// CommissionCalculatorApplication.java
@Bean
public ToolCallbackProvider commissionToolCallbackProvider(
        McpCommissionTools mcpCommissionTools,
        CurrencyConversionService currencyConversionService) {
    return MethodToolCallbackProvider.builder()
        .toolObjects(mcpCommissionTools, currencyConversionService)
        .build();
}
```

## McpPrompts — 10 Workflow Templates

| Prompt | Parameters | Description |
|--------|------------|-------------|
| analyze-sales-performance | salesRepId, period | Analyze rep metrics |
| create-commission-workflow | dealTitle, dealValue, salesRepId, planId | End-to-end commission flow |
| dispute-resolution | calculationId, disputeReason, salesRepId | Dispute workflow |
| setup-commission-plan | planName, currency, baseRate, ruleType | Plan creation workflow |
| monthly-commission-report | salesRepId, month | Monthly report |
| compare-plans | dealId, planIds | Compare plans side-by-side |
| audit-calculation | calculationId | Audit a calculation |
| convert-deal-currency | dealId, planId, targetCurrency | Commission + currency convert |
| multi-currency-commission-report | salesRepId, targetCurrency | Multi-currency report |
| currency-rate-check | baseCurrency, targetCurrencies, historicalDate | Rate comparison |

## McpResources — 12 Data Sources + 8 Templates

### Static Resources

| URI | Description | MIME |
|-----|-------------|------|
| deals://all | All deals | application/json |
| deals://active | Active deals | application/json |
| plans://all | All plans | application/json |
| plans://active | Active plans | application/json |
| disputes://all | All disputes | application/json |
| disputes://open | Open disputes | application/json |
| calculations://all | All calculations | application/json |
| currency://supported | Supported currencies | application/json |
| currency://rates | Latest exchange rates | application/json |
| schema://deal | Deal JSON Schema | application/schema+json |
| schema://commission-plan | Plan JSON Schema | application/schema+json |
| schema://dispute | Dispute JSON Schema | application/schema+json |

### Resource Templates (parameterized)

| URI Template | Parameters | Description |
|-------------|------------|-------------|
| deal://{dealId} | dealId | Single deal |
| plan://{planId} | planId | Single plan |
| dispute://{disputeId} | disputeId | Single dispute |
| calculation://{calculationId} | calculationId | Single calculation |
| deals-by-rep://{salesRepId} | salesRepId | Deals by rep |
| calculations-by-rep://{salesRepId} | salesRepId | Calculations by rep |
| disputes-by-rep://{salesRepId} | salesRepId | Disputes by rep |
| currency-rates://{baseCurrency} | baseCurrency | Rates for base |

## Transport Configuration

| Transport | Endpoint | Use Case |
|-----------|----------|----------|
| Streamable HTTP | /mcp | Claude Desktop (recommended) |
| STDIO | (process stdio) | Claude Desktop (alternative) |
| SSE | /api/mcp/sse | MCP Inspector, legacy clients |
| REST | /api/mcp/* | curl, Postman testing |

## Adding MCP Tools for a New Feature

1. Create the feature slice with its Service (see FEATURE_SPEC_TEMPLATE.md)
2. Add @Tool wrapper methods to McpCommissionTools.java:
   ```java
   @Tool(name = "createNewEntity",
         description = "Create a new entity. Describe all parameters clearly.")
   public NewEntityResponse createNewEntity(CreateNewEntityRequest request) {
       return newEntityService.create(request);
   }
   ```
3. If the new feature has data worth exposing, add entries to McpResources.java
4. If the new feature has common workflows, add entries to McpPrompts.java
5. No registration changes needed — MethodToolCallbackProvider discovers @Tool annotations automatically