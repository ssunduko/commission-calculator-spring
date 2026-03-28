# Feature Spec: Commission Plan Management

## Intent

Define and manage commission plans that contain rules, tiers, and bonuses for calculating sales commissions. Plans are created in draft status, configured with rules, then activated for use.

## Domain Entities

**Primary Entity:** `CommissionPlan`
**Table:** `commission_plans`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | String (UUID) | @Id, auto-generated | Primary key |
| name | String | not null | Plan name |
| currency | String(3) | not null | ISO currency code (stored as string, accessed as Currency) |
| status | PlanStatus | @Enumerated(STRING), not null | Lifecycle state |
| rules | List\<CommissionRule\> | @Transient | Calculation rules |
| tiers | List\<CommissionTier\> | @Transient | Value-based tier brackets |
| bonuses | List\<BonusRule\> | @Transient | Bonus incentives |
| effectiveStartDate | LocalDate | nullable | When the plan takes effect |
| effectiveEndDate | LocalDate | nullable | When the plan expires |
| createdDate | LocalDate | not null | Auto-set |
| lastModifiedDate | LocalDate | nullable | Updated on changes |
| createdBy | String | nullable | Admin who created it |

**Supporting Entities (in domain/):**
- `CommissionRule` — rate, type (STANDARD/ACCELERATOR/BONUS), priority, conditions
- `CommissionTier` — lowerBound, upperBound, rate, isPercentage
- `BonusRule` — amount, isPercentage, type, date range, conditions

## Status Lifecycle

```
DRAFT → ACTIVE → INACTIVE
                    ↓
                 ARCHIVED
```

Enum: `DRAFT("Draft"), ACTIVE("Active"), INACTIVE("Inactive"), ARCHIVED("Archived")`

## Operations

| Operation | Input | Output | Description |
|-----------|-------|--------|-------------|
| createPlan | CreateCommissionPlanRequest | CommissionPlanResponse | Create a new plan in DRAFT |
| getPlan | String id | CommissionPlanResponse | Get plan by ID |
| getAllPlans | — | List\<CommissionPlanResponse\> | List all plans |
| getPlansByStatus | PlanStatus | List\<CommissionPlanResponse\> | Filter by status |
| activatePlan | String id | CommissionPlanResponse | Set plan to ACTIVE |
| addRuleToPlan | String planId, AddRuleToPlanRequest | CommissionPlanResponse | Add commission rule |
| deletePlan | String id | void | Remove a plan |

## Validation Rules

- name: Required, must not be blank
- currencyCode: Required, must be valid ISO currency code
- rule.name: Required, must not be blank
- rule.rate: Required, must be non-negative

## REST Endpoints

| Method | Path | Operation |
|--------|------|-----------|
| POST | /api/plans | createPlan |
| GET | /api/plans/{id} | getPlan |
| GET | /api/plans | getAllPlans |
| GET | /api/plans?status={status} | getPlansByStatus |
| POST | /api/plans/{id}/activate | activatePlan |
| POST | /api/plans/{id}/rules | addRuleToPlan |
| DELETE | /api/plans/{id} | deletePlan |

## MCP Tools (7)

| Tool Name | Description |
|-----------|-------------|
| createCommissionPlan | Create a new commission plan with name, currency, and dates |
| getCommissionPlan | Get plan by ID with rules and tiers |
| getAllCommissionPlans | List all plans |
| getCommissionPlansByStatus | Filter plans by status |
| activateCommissionPlan | Set plan to ACTIVE |
| addRuleToPlan | Add a commission rule (STANDARD, ACCELERATOR, BONUS) |
| deleteCommissionPlan | Remove a plan |

## MCP Resources

| URI | Description |
|-----|-------------|
| plans://all | All commission plans |
| plans://active | Active plans only |

## Files

```
features/plans/
  CreateCommissionPlanRequest.java
  AddRuleToPlanRequest.java
  CommissionPlanResponse.java
  CommissionPlanRepository.java
  CommissionPlanService.java
  CommissionPlanController.java

domain/
  CommissionPlan.java
  CommissionRule.java
  CommissionTier.java
  BonusRule.java
  PlanStatus.java
  RuleType.java
  BonusType.java
```