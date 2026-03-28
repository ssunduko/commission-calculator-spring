# Feature Spec: Deal Management

## Intent

Manage sales deals and their lifecycle. Deals are the primary entities for which commissions are calculated. Sales reps create deals, managers approve them, and the system tracks their status through the pipeline.

## Domain Entity

**Entity:** `Deal`
**Table:** `deals`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | String (UUID) | @Id, auto-generated | Primary key |
| title | String | not null | Deal title/name |
| value | BigDecimal(19,2) | not null, column="deal_value" | Monetary value of the deal |
| status | DealStatus | @Enumerated(STRING), not null | Pipeline status |
| salesRepId | String | not null | Owning sales representative |
| products | List\<DealProduct\> | @Transient | Line items in the deal |
| closeDate | LocalDate | nullable | When the deal was closed |
| createdDate | LocalDate | not null | Auto-set on creation |
| lastModifiedDate | LocalDate | nullable | Updated on status change |

## Status Lifecycle

```
OPEN → WON
  ↓
LOST
  ↓
CANCELLED
```

Enum: `OPEN("Open"), WON("Won"), LOST("Lost"), CANCELLED("Cancelled")`

## Operations

| Operation | Input | Output | Description |
|-----------|-------|--------|-------------|
| createDeal | CreateDealRequest(title, value, salesRepId) | DealResponse | Create a new deal |
| getDeal | String id | DealResponse | Get deal by ID |
| getAllDeals | — | List\<DealResponse\> | List all deals |
| getDealsBySalesRep | String salesRepId | List\<DealResponse\> | Filter by sales rep |
| getDealsByStatus | DealStatus status | List\<DealResponse\> | Filter by status |
| updateDeal | String id, UpdateDealRequest | DealResponse | Update deal fields |
| deleteDeal | String id | void | Permanently remove deal |

## Validation Rules

- title: Required, must not be blank
- value: Required, must be greater than zero
- salesRepId: Required, must not be blank

## REST Endpoints

| Method | Path | Operation |
|--------|------|-----------|
| POST | /api/deals | createDeal |
| GET | /api/deals/{id} | getDeal |
| GET | /api/deals | getAllDeals |
| GET | /api/deals?salesRepId={id} | getDealsBySalesRep |
| GET | /api/deals?status={status} | getDealsByStatus |
| PUT | /api/deals/{id} | updateDeal |
| DELETE | /api/deals/{id} | deleteDeal |

## MCP Tools (7)

| Tool Name | Description |
|-----------|-------------|
| createDeal | Create a new deal with title, value, and sales rep ID |
| getDeal | Get a deal by its ID |
| getAllDeals | Get all deals in the system |
| getDealsBySalesRep | Get all deals for a specific sales representative |
| getDealsByStatus | Get all deals with a specific status (OPEN, WON, LOST) |
| updateDeal | Update an existing deal's title, value, status, or close date |
| deleteDeal | Delete a deal by its ID |

## MCP Resources

| URI | Description |
|-----|-------------|
| deals://all | Complete list of all deals |
| deals://active | Active deals (OPEN or WON status) |

## Cross-Feature Dependencies

- None. Deals is a root feature — other features depend on it.
- CommissionCalculation depends on DealRepository (cross-feature read).

## Files

```
features/deals/
  CreateDealRequest.java        ← Input DTO (record + validate)
  UpdateDealRequest.java        ← Update DTO (record, nullable fields)
  DealResponse.java             ← Output DTO (record + from factory)
  DealRepository.java           ← JPA repository interface
  DealService.java              ← Business logic
  DealController.java           ← REST controller

domain/
  Deal.java                     ← JPA entity (shared)
  DealStatus.java               ← Status enum (shared)
  DealProduct.java              ← Embedded product entity (shared)
```

## Sample Data (from DataInitializer)

| Title | Value | Sales Rep | Status |
|-------|-------|-----------|--------|
| Enterprise Software License | $150,000 | rep001 | WON |
| Cloud Services Contract | $85,000 | rep001 | WON |
| Consulting Services | $45,000 | rep002 | WON |
| Hardware Procurement | $120,000 | rep002 | OPEN |
| Annual Support Renewal | $25,000 | rep003 | WON |
| Training Package | $15,000 | rep003 | LOST |