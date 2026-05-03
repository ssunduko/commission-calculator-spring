# Feature: Commission Calculation

> Layer 1 of the progressive-disclosure system. Reviewed and approved before any story begins.

## Problem

When a deal closes, the sales rep's commission must be derived from the deal value, the rep's assigned commission plan, and the rules in that plan (base rate, accelerators, bonuses). Today there is no consistent place this logic lives. Without a single calculation pipeline, the same deal can produce different commission amounts depending on which surface (REST API, MCP tool, or downstream payout job) requested the number, and there is no audit trail of which plan, rule set, and inputs produced a given amount.

## Who Is Affected

- **Sales reps** — receive payouts derived from these calculations; need confidence in the number and an audit trail when they dispute it.
- **Sales managers** — approve calculations and need consistent figures across reps and time periods.
- **Finance** — needs the calculation history to be immutable and reconcilable against payouts.
- **AI assistants (MCP clients)** — need a reliable `calculateCommission` tool with a stable input/output contract.

## Vertical Slice: Layers This Feature Touches

### Database (JPA / H2)

- New table: `commission_calculations`
  - Columns: `id` (UUID PK), `deal_id` (FK → deals), `sales_rep_id` (FK → users), `plan_id` (FK → commission_plans, nullable), `base_commission` (NUMERIC(19,2)), `gross_commission` (NUMERIC(19,2)), `net_commission` (NUMERIC(19,2)), `status` (VARCHAR), `calculation_date` (DATE), `payout_date` (DATE nullable)
  - Indexes: `(deal_id)`, `(sales_rep_id)`, `(status)`
- Off limits: `deals`, `commission_plans`, `users`, `disputes` (each owned by its own feature)

### Domain (`verticalslice/domain/`)

- New entity: `CommissionCalculation.java` — JPA `@Entity`, `@Data`, `@NoArgsConstructor`, UUID primary key
- New enum: `CommissionStatus.java` — `CALCULATED`, `APPROVED`, `PAID`, `DISPUTED`, `ADJUSTED`, `CANCELLED`
- Supporting transient entities (in-memory only, not persisted in this feature): `BonusCalculation`, `AcceleratorCalculation`
- Off limits: `Deal`, `CommissionPlan`, `User` — read-only access via their repositories

### Feature Package (`verticalslice/features/calculations/`)

- New: `CommissionCalculationRepository.java` — `extends JpaRepository<CommissionCalculation, String>`
- New: `CalculateCommissionRequest.java` — record `{ String dealId, String planId }` with `validate()`
- New: `CommissionCalculationResponse.java` — record + `static from(CommissionCalculation)`
- New: `CommissionCalculationService.java` — `@Service`, business logic, constructor-injected dependencies
- New: `CommissionCalculationController.java` — `@RestController`, `/api/calculations`

### Cross-feature reads

- `DealRepository` (from `features/deals/`) — fetch `Deal` by id; read `value`, `salesRepId`
- `CommissionPlanRepository` (from `features/plans/`) — fetch `CommissionPlan` by id; read `rules`, `tiers`, `bonuses`

### Off limits across the codebase

- `features/deals/**` — owned by deals feature; read via repository only
- `features/plans/**` — owned by plans feature; read via repository only
- `features/disputes/**` — disputes consume calculations, not the other way around
- `infrastructure/data/DataInitializer.java` — sample data only; do not add calculation seeding here
- Lombok-generated boilerplate is fine; do not hand-write getters/setters

### REST API Shape

```
POST /api/calculations
  Auth: any authenticated user with read access to the deal
  Body: CalculateCommissionRequest { dealId: string, planId: string }
  Response 201: CommissionCalculationResponse
  Response 400: { error, code: "VALIDATION_ERROR", details: [...] }
  Response 404: { error, code: "RESOURCE_NOT_FOUND", resource: "deal"|"plan" }

GET /api/calculations/{id}
  Auth: any authenticated user
  Response 200: CommissionCalculationResponse
  Response 404: ResourceNotFound

GET /api/calculations
  Auth: any authenticated user
  Response 200: List<CommissionCalculationResponse>

GET /api/calculations?dealId={dealId}
  Auth: any authenticated user
  Response 200: List<CommissionCalculationResponse>

GET /api/calculations?salesRepId={salesRepId}
  Auth: any authenticated user
  Response 200: List<CommissionCalculationResponse>
```

`CommissionCalculationResponse` shape:

```json
{
  "id": "string (uuid)",
  "dealId": "string",
  "salesRepId": "string",
  "planId": "string|null",
  "baseCommission": "number (decimal, 2 fraction digits)",
  "grossCommission": "number (decimal, 2 fraction digits)",
  "netCommission": "number (decimal, 2 fraction digits)",
  "status": "CALCULATED|APPROVED|PAID|DISPUTED|ADJUSTED|CANCELLED",
  "calculationDate": "string (ISO-8601 date)",
  "payoutDate": "string (ISO-8601 date) | null"
}
```

### MCP Tool Shape

Wrappers in `infrastructure/mcp/McpCommissionTools.java`:

```
@Tool(name = "calculateCommission",
      description = "Calculate the commission for a closed deal using a specific commission plan. Returns the calculated commission with base, gross, and net amounts, plus the calculation status.")
CommissionCalculationResponse calculateCommission(CalculateCommissionRequest request)

@Tool(name = "getCommissionCalculation",
      description = "Get a commission calculation by its ID. Returns the calculation details including amounts and status.")
CommissionCalculationResponse getCommissionCalculation(String id)

@Tool(name = "getAllCommissionCalculations",
      description = "List all commission calculations in the system.")
List<CommissionCalculationResponse> getAllCommissionCalculations()

@Tool(name = "getCalculationsBySalesRep",
      description = "Get all commission calculations for a specific sales representative by their user ID.")
List<CommissionCalculationResponse> getCalculationsBySalesRep(String salesRepId)

@Tool(name = "getCalculationsByDeal",
      description = "Get all commission calculations performed for a specific deal by deal ID.")
List<CommissionCalculationResponse> getCalculationsByDeal(String dealId)
```

## Status Lifecycle

```
CALCULATED ──► APPROVED ──► PAID
     │              │
     │              └──► ADJUSTED
     │
     └──► DISPUTED
              │
              └──► CANCELLED
```

Allowed transitions are enforced in `CommissionCalculationService`, not on the entity.

## Calculation Logic (canonical)

```
1. Fetch Deal by dealId.            ResourceNotFoundException if missing.
2. Fetch CommissionPlan by planId.  ResourceNotFoundException if missing.
3. Select the first STANDARD rule from plan.rules (sorted by priority).
4. baseCommission = deal.value * rule.rate / 100
   - BigDecimal math, scale 2, RoundingMode.HALF_UP
5. Sum applicable BONUS rules into bonusAmount; grossCommission = baseCommission + bonusAmount.
6. Apply ACCELERATOR rules (multipliers) to grossCommission as configured by plan.
7. netCommission = grossCommission (taxes are applied externally).
8. Persist with status = CALCULATED, calculationDate = today.
9. Return CommissionCalculationResponse.from(saved).
```

`BigDecimal` scale and rounding mode are mandatory on every monetary multiply/divide. See ADR-0001.

## Success Criteria

1. POST `/api/calculations` with a valid `dealId` and `planId` returns 201 with a populated `CommissionCalculationResponse` whose `baseCommission` equals `deal.value * rule.rate / 100`.
2. POST `/api/calculations` with a `dealId` that does not exist returns 404 with `code: RESOURCE_NOT_FOUND` and `resource: deal`.
3. POST `/api/calculations` with a `planId` that does not exist returns 404 with `resource: plan`.
4. POST `/api/calculations` with a plan that has no `STANDARD` rule returns 400 with `code: VALIDATION_ERROR`.
5. GET `/api/calculations/{id}` returns the persisted calculation.
6. GET `/api/calculations?dealId=...` returns only calculations for that deal.
7. The MCP `calculateCommission` tool produces the same `CommissionCalculationResponse` as the REST endpoint for the same input.
8. Two calculations of the same deal with the same plan produce two separate persisted rows (calculation history is append-only in this feature).
9. All monetary values are returned with exactly 2 fraction digits, regardless of input scale.

## Out of Scope

- Approval workflow (transition `CALCULATED → APPROVED`) — separate feature
- Payout scheduling and `PAID` transition — separate feature
- Dispute creation and resolution — handled by `features/disputes/`
- Tax calculation — `netCommission` equals `grossCommission` here; tax is external
- Currency conversion — handled by `features/currency/` if needed; this feature assumes deal and plan share a currency
- Bulk calculation across many deals at once — out of scope for v1
- Recalculation / amendment of an existing calculation row — append-only here

## Open Questions

- `[OPEN]` What should happen if the deal's `salesRepId` differs from the plan's owner? Today plans are not owned by reps, but if that changes, the calculation must reject the mismatch.
- `[OPEN]` Should `STANDARD` rule selection use the highest-priority rule, or the first one in declaration order when priorities tie? Story COMM-01 picks lowest priority value first; revisit if reps push back.
- `[RESOLVED 2026-05-01]` `BigDecimal` scale and rounding — scale 2, `RoundingMode.HALF_UP` everywhere. See ADR-0001.

## Cross-Feature Dependencies

- `DealRepository` from `features/deals/` — read-only
- `CommissionPlanRepository` from `features/plans/` — read-only
- None other

## Stories in This Feature

| ID | Title | Status |
|---|---|---|
| COMM-01 | Calculate base commission from deal value and plan's STANDARD rule | Ready |
| COMM-02 | REST controller exposes calculation endpoints | Draft |
| COMM-03 | MCP tool wrappers in `McpCommissionTools` | Draft |
| COMM-04 | Filter calculations by deal and by sales rep | Draft |
| COMM-05 | Apply BONUS and ACCELERATOR rules to gross commission | Draft |

Each story has its own README under `stories/{NN-slug}/`.
