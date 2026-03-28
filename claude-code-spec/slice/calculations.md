# Feature Spec: Commission Calculation

## Intent

Calculate commissions for closed deals using commission plan rules. This is the core domain logic — it reads deal values, applies plan rules and tiers, and produces a commission amount.

## Domain Entity

**Entity:** `CommissionCalculation`
**Table:** `commission_calculations`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | String (UUID) | @Id, auto-generated | Primary key |
| dealId | String | not null | The deal being commissioned |
| salesRepId | String | not null | The rep earning the commission |
| planId | String | nullable | Plan used for calculation |
| baseCommission | BigDecimal(19,2) | not null | Base calculated amount |
| grossCommission | BigDecimal(19,2) | not null | After bonuses and accelerators |
| netCommission | BigDecimal(19,2) | not null | Final payout amount |
| status | CommissionStatus | @Enumerated(STRING), not null | Calculation lifecycle |
| calculationDate | LocalDate | not null | When calculated |
| payoutDate | LocalDate | nullable | When paid |
| bonuses | List\<BonusCalculation\> | @Transient | Applied bonus amounts |
| accelerators | List\<AcceleratorCalculation\> | @Transient | Applied multipliers |

## Status Lifecycle

```
CALCULATED → APPROVED → PAID
     ↓           ↓
  DISPUTED    ADJUSTED
     ↓
  CANCELLED
```

## Operations

| Operation | Input | Output | Description |
|-----------|-------|--------|-------------|
| calculateCommission | CalculateCommissionRequest(dealId, planId) | CommissionCalculationResponse | Calculate commission for a deal |
| getCalculation | String id | CommissionCalculationResponse | Get calculation by ID |
| getAllCalculations | — | List\<CommissionCalculationResponse\> | List all calculations |
| getCalculationsByDeal | String dealId | List\<CommissionCalculationResponse\> | Filter by deal |
| getCalculationsBySalesRep | String salesRepId | List\<CommissionCalculationResponse\> | Filter by rep |

## Cross-Feature Dependencies

- **DealRepository** (from features/deals/) — reads deal value and rep ID
- **CommissionPlanRepository** (from features/plans/) — reads plan rules for calculation

## Calculation Logic

```
1. Fetch Deal by dealId → get deal.value, deal.salesRepId
2. Fetch CommissionPlan by planId → get plan.rules
3. Apply first rule: baseCommission = deal.value × rule.rate / 100
4. Add bonuses (if any) → grossCommission
5. Apply accelerators (if any) → grossCommission
6. Set netCommission = grossCommission (taxes applied externally)
7. Save and return CommissionCalculationResponse
```

## MCP Tools (5)

| Tool Name | Description |
|-----------|-------------|
| calculateCommission | Calculate commission for a deal using a plan |
| getCommissionCalculation | Get calculation by ID |
| getAllCommissionCalculations | List all calculations |
| getCalculationsBySalesRep | Get calculations by sales rep |
| getCalculationsByDeal | Get calculations by deal |

## Files

```
features/calculations/
  CalculateCommissionRequest.java
  CommissionCalculationResponse.java
  CommissionCalculationRepository.java
  CommissionCalculationService.java
  CommissionCalculationController.java

domain/
  CommissionCalculation.java
  CommissionStatus.java
  BonusCalculation.java
  AcceleratorCalculation.java
```