# Feature Spec: Dispute Management

## Intent

Allow sales representatives to formally challenge commission calculations they believe are incorrect. Disputes follow a structured workflow through review, optional escalation, and resolution.

## Domain Entity

**Entity:** `Dispute`
**Table:** `disputes`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | String (UUID) | @Id, auto-generated | Primary key |
| calculationId | String | not null | The disputed commission calculation |
| salesRepId | String | not null | The rep filing the dispute |
| managerId | String | nullable | Assigned reviewing manager |
| title | String | not null | Dispute title |
| description | String(2000) | not null | Detailed dispute explanation |
| status | DisputeStatus | @Enumerated(STRING), not null | Workflow state |
| comments | List\<DisputeComment\> | @Transient | Discussion thread |
| isEscalated | boolean | not null, default false | Escalation flag |
| resolution | String(2000) | nullable | Resolution notes |
| resolvedBy | String | nullable | Who resolved the dispute |
| createdDate | LocalDateTime | not null | Auto-set |
| lastUpdatedDate | LocalDateTime | nullable | Updated on any change |
| resolvedDate | LocalDateTime | nullable | Set when resolved |

## Status Lifecycle

```
INITIATED → UNDER_REVIEW → APPROVED
     ↓            ↓           
     ↓     ADDITIONAL_INFO_REQUESTED
     ↓            ↓
     ↓       ESCALATED → APPROVED
     ↓                     ↓
     ↓                  REJECTED
     ↓
  CANCELLED              RESOLVED
```

Enum: `INITIATED, UNDER_REVIEW, ADDITIONAL_INFO_REQUESTED, ESCALATED, APPROVED, REJECTED, RESOLVED, CANCELLED`

## Operations

| Operation | Input | Output | Description |
|-----------|-------|--------|-------------|
| createDispute | CreateDisputeRequest(calcId, repId, title, desc) | DisputeResponse | File a new dispute |
| getDispute | String id | DisputeResponse | Get dispute details |
| getAllDisputes | — | List\<DisputeResponse\> | List all disputes |
| getDisputesBySalesRep | String salesRepId | List\<DisputeResponse\> | Filter by rep |
| getDisputesByStatus | DisputeStatus | List\<DisputeResponse\> | Filter by status |
| resolveDispute | String id, ResolveDisputeRequest | DisputeResponse | Approve or reject |
| escalateDispute | String id | DisputeResponse | Escalate to management |
| deleteDispute | String id | void | Remove a dispute |

## Validation Rules

- calculationId: Required
- salesRepId: Required
- title: Required, must not be blank
- description: Required, must not be blank
- resolution (on resolve): Required
- resolvedBy (on resolve): Required
- Cannot resolve an already resolved dispute
- Cannot escalate an already escalated dispute

## MCP Tools (8)

| Tool Name | Description |
|-----------|-------------|
| createDispute | Create a new dispute for a commission calculation |
| getDispute | Get dispute by ID |
| getAllDisputes | List all disputes |
| getDisputesBySalesRep | Get disputes by sales rep |
| getDisputesByStatus | Filter disputes by status |
| resolveDispute | Approve or reject a dispute |
| escalateDispute | Escalate dispute to management |
| deleteDispute | Remove a dispute |

## MCP Resources

| URI | Description |
|-----|-------------|
| disputes://all | All disputes |
| disputes://open | Open (INITIATED status) disputes |

## MCP Prompts

| Name | Parameters | Description |
|------|------------|-------------|
| dispute-resolution | calculationId, disputeReason, salesRepId | Full dispute resolution workflow |
| audit-calculation | calculationId | Audit a calculation before/during dispute |

## Files

```
features/disputes/
  CreateDisputeRequest.java
  ResolveDisputeRequest.java
  DisputeResponse.java
  DisputeRepository.java
  DisputeService.java
  DisputeController.java

domain/
  Dispute.java
  DisputeStatus.java
  DisputeComment.java
```