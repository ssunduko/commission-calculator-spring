# Feature: {Name}

> Layer 1 of the progressive-disclosure system. This file is the full vertical-slice context for the feature. It is reviewed and approved before any story begins. Stories reference this README; AI sessions read it for the *why* behind any work in this feature.

## Problem

Two to four sentences. What is broken or missing today? What is the user (sales rep, manager, admin, or downstream system) experiencing? Be specific about the failure mode, not the solution.

## Who Is Affected

Specific user roles, account types, or system components. Quantify if possible. Avoid "all users" without supporting detail.

## Vertical Slice: Layers This Feature Touches

This is the most important section for AI sessions. It tells the AI exactly where in the stack this feature lives and what it is allowed to change.

**Database (JPA / H2):**
- New tables: `{table_name}` — purpose, key columns, indexes
- Modified tables: `{table_name}` — column additions, why
- Off limits: `{table_name}` — why this feature must not touch it

**Domain (`verticalslice/domain/`):**
- New entities: `{Entity}.java` — purpose
- New enums: `{Status}.java` — values and transitions
- Off limits: existing shared entities not in this slice

**Feature package (`verticalslice/features/{feature}/`):**
- New: `{Entity}Repository.java` — extends JpaRepository
- New: `Create{Entity}Request.java` — record + `validate()`
- New: `Update{Entity}Request.java` — record (nullable fields)
- New: `{Entity}Response.java` — record + `static from(Entity)`
- New: `{Entity}Service.java` — `@Service`, business logic
- New: `{Entity}Controller.java` — `@RestController`, `/api/{path}`
- Modified: `{path/to/file.java}` — which method, what change, why

**Off limits across the codebase:**
- `{path}` — reason (e.g., "owned by a different feature, do not modify")
- `{path}` — reason

**REST API Shape (define before any code is written):**

Specify each endpoint, request body, response body, status codes, and auth requirements. The entity, repository, service, and controller are built to produce this shape. Do not retrofit.

```
GET /api/{resource}/{id}
  Auth: any authenticated user with read access
  Response 200: {EntityResponse}
  Response 404: { "error": "...", "code": "RESOURCE_NOT_FOUND" }

POST /api/{resource}
  Auth: role required ({role})
  Body: Create{Entity}Request
  Response 201: {EntityResponse}
  Response 400: validation errors
  Response 403: insufficient role

PUT /api/{resource}/{id}
  ...

DELETE /api/{resource}/{id}
  ...
```

**MCP Tool Shape (`infrastructure/mcp/McpCommissionTools.java`):**

Each REST operation gets an `@Tool` wrapper. Define the tool name, description (what the LLM sees), and the request/response types here.

```
@Tool(name = "create{Entity}",
      description = "Create a new {entity} with {key fields}. Returns the created {entity} details.")
{Entity}Response create{Entity}({Create{Entity}Request} request)
```

## Success Criteria

Numbered list. Each item is testable and observable from outside the system, not an implementation task.

1. {Specific behavior — e.g., "A POST to /api/calculations with a valid dealId and planId returns a 201 with a CommissionCalculationResponse"}
2. ...

## Out of Scope

Explicit list. What this feature does **not** include. Anything ambiguous goes here as "deferred" rather than left implicit.

- {Excluded behavior 1}
- {Excluded behavior 2}

## Open Questions

Unresolved decisions. Do not begin a story whose acceptance criteria depend on an open question. Record resolutions here with date.

- `[OPEN]` {Question}
- `[RESOLVED YYYY-MM-DD]` {Question} — {decision summary}. See ADR-{NNNN}.

## Cross-Feature Dependencies

- {Feature X repository} — read-only access for {purpose}
- {Feature Y service} — write access for {purpose}
- None (fully self-contained)

## Stories in This Feature

| ID | Title | Status |
|----|-------|--------|
| {FEAT}-01 | {short title} | Draft / Ready / In Progress / Done |
| {FEAT}-02 | {short title} | ... |

Stories live under `stories/{NN-slug}/`. Each story has its own README, tests, and (optionally) ADRs.
