# Commission Calculator — Vertical Slice Module

Spring Boot application implementing a commission calculator using **Vertical Slice
Architecture** with three integration surfaces for AI agents:

- **MCP** (Model Context Protocol) — 35 tools, 10 prompts, 12 resources.
- **A2A** (Agent-to-Agent) — a dedicated dispute-filing agent exposed over JSON-RPC.
- **REST** — conventional HTTP API for every feature.

Port: **8081** (override with `-Dserver.port=…`). Default credentials: `admin` / `admin123`.

---

## Table of Contents

1. [Architecture](#architecture)
2. [Domain Model](#domain-model)
3. [Features](#features)
4. [Running the Application](#running-the-application)
5. [MCP Server](#mcp-server)
6. [A2A — Agent-to-Agent](#a2a--agent-to-agent)
7. [Feature Flags (Togglz)](#feature-flags-togglz)
8. [Observability](#observability)
9. [Testing](#testing)
10. [Common Workflows](#common-workflows)

---

## Architecture

### Vertical Slice

Each feature is a complete vertical slice containing every layer needed to serve it:

```
verticalslice/
├── domain/                 # Shared enums + entities
├── features/
│   ├── deals/              # Controller + service + repository + DTOs
│   ├── plans/
│   ├── calculations/
│   ├── disputes/
│   └── currency/           # MCP client to currency-mcp.wesbos.com
├── processor/              # VerticalSliceProcessor — startup demos
└── infrastructure/
    ├── a2a/                # A2A dispute agent + client + REST trigger + CLI
    ├── cli/                # Interactive REPL for MCP tools
    ├── config/             # Security, OpenAPI, feature flags
    ├── data/               # DataInitializer
    ├── exceptions/         # GlobalExceptionHandler
    └── mcp/                # Hand-rolled SSE + REST MCP controllers, tools, prompts, resources
```

Benefits: feature cohesion, easy navigation, independent development, clear boundaries,
isolation-friendly testing.

---

## Domain Model

| Entity | Key fields | Statuses |
|---|---|---|
| **Deal** | id, title, value, salesRepId, closeDate, products | OPEN, WON, LOST, CANCELLED |
| **CommissionPlan** | id, name, currency, effective dates, rules, tiers | DRAFT, ACTIVE, INACTIVE, ARCHIVED |
| **CommissionRule** | id, name, type, rate, priority | PERCENTAGE, TIERED, FLAT, ACCELERATOR |
| **CommissionCalculation** | id, dealId, salesRepId, planId, baseCommission, finalAmount | DRAFT, PENDING, APPROVED, PAID, CANCELLED |
| **Dispute** | id, calculationId, salesRepId, title, description, **priority**, resolution | INITIATED, UNDER_REVIEW, ADDITIONAL_INFO_REQUESTED, ESCALATED, APPROVED, REJECTED, RESOLVED, CANCELLED |
| **DisputePriority** | LOW, MEDIUM, HIGH, URGENT | — |

---

## Features

### 1. Deal Management (`features/deals`)

**REST:** `POST/GET/PUT/DELETE /api/deals`, `/api/deals/rep/{id}`, `/api/deals/status/{s}`.
**MCP tools:** `createDeal`, `getDeal`, `getAllDeals`, `getDealsBySalesRep`, `getDealsByStatus`, `updateDeal`, `deleteDeal`.

### 2. Commission Plans (`features/plans`)

**REST:** `POST/GET /api/plans`, `POST /api/plans/{id}/activate`, `POST /api/plans/{id}/rules`, `DELETE /api/plans/{id}`.
**MCP tools:** `createCommissionPlan`, `getCommissionPlan`, `getAllCommissionPlans`, `getCommissionPlansByStatus`, `activateCommissionPlan`, `addRuleToPlan`, `deleteCommissionPlan`.

### 3. Commission Calculation (`features/calculations`)

**REST:** `POST /api/calculations`, `GET /api/calculations/{id|deal|rep}`.
**MCP tools:** `calculateCommission`, `getCommissionCalculation`, `getAllCommissionCalculations`, `getCalculationsByDeal`, `getCalculationsBySalesRep`.

### 4. Dispute Management (`features/disputes`)

**REST:** `POST/GET/DELETE /api/disputes`, `POST /api/disputes/{id}/{resolve|escalate|comments|documents}`, `?priority=LOW|MEDIUM|HIGH|URGENT` filter on `GET /api/disputes`.
**MCP tools:** `createDispute`, `getDispute`, `getAllDisputes`, `getDisputesBySalesRep`, `getDisputesByStatus`, `resolveDispute`, `escalateDispute`, `deleteDispute`, plus AI-powered `analyzeDispute`, `summarizeSalesPerformance`, `explainCommission`, and the A2A bridge `delegateToDisputeAgent` (see the A2A section).

### 5. Currency Conversion (`features/currency`)

Acts as an **MCP client** against [currency-mcp.wesbos.com](https://github.com/wesbos/currency-conversion-mcp) over SSE and re-exposes the remote tools locally — demonstrating MCP server chaining.

**REST:** `POST /api/currency/convert`, `GET /api/currency/rates`, `/api/currency/supported`, `/api/currency/historical`.
**MCP tools (proxied):** `convertCurrency`, `getLatestRates`, `listSupportedCurrencies`, `getHistoricalRates`.

---

## Running the Application

### Prerequisites

- Java 21+
- Maven 3.8+
- Node.js (for MCP Inspector and `mcp-remote`)

### Build

```bash
mvn clean package -DskipTests -Pverticalslice
```

### Run (development, with DevTools)

```bash
mvn -Pverticalslice spring-boot:run
```

`-Pverticalslice` activates the Maven profile that points `spring-boot:run` at `CommissionCalculatorApplication` — without it, `mvn spring-boot:run` defaults to the ORM module and the wrong context boots.

### Run from packaged JAR

```bash
java -jar target/commission-calculator-0.0.1-SNAPSHOT.jar
```

### Environment variables

| Variable | Purpose |
|---|---|
| `ANTHROPIC_API_KEY` | Required when the A2A agent or `analyzeDispute` / `explainCommission` / `summarizeSalesPerformance` tools are used. |
| `SPRING_DATASOURCE_PASSWORD` | Override default H2 password. |
| `APP_REGION` | Region value read by `RegionActivationStrategy` (feature flags). |

---

## MCP Server

### What is MCP?

**Model Context Protocol** lets AI agents call well-defined tools on an application. This
module exposes **35 tools**, **10 prompts**, and **12 resources** via Spring AI's MCP server.
All `@Tool`-annotated methods across services are registered automatically.

### Transports

Three ways to talk to the MCP server — pick one based on client:

| Transport | Endpoint | Who uses it |
|---|---|---|
| **Streamable HTTP** (recommended) | `POST http://localhost:8081/mcp` | Claude Desktop via `mcp-remote`, modern MCP clients |
| **SSE** | `GET http://localhost:8081/api/mcp/sse` + `POST http://localhost:8081/api/mcp/message` | Legacy MCP Inspector / clients without Streamable HTTP |
| **Plain REST shim** | `POST http://localhost:8081/api/mcp/{tools|prompts|resources}/{list|call|read}` | `curl` quick tests |
| **STDIO** | via `java -Dspring.ai.mcp.server.stdio=true -Dspring.ai.mcp.server.protocol=STDIO -jar …` | Claude Desktop launching the server as a subprocess |

`spring.ai.mcp.server.stdio=false` (default) must be set for the Streamable HTTP transport at `/mcp` to register. Flip to `true` **and** `protocol=STDIO` when running under a stdio-spawning client — the HTTP endpoint then goes away.

### Tool catalogue (35)

<details>
<summary>Click to expand</summary>

**Deals (7):** `createDeal`, `getDeal`, `getAllDeals`, `getDealsBySalesRep`, `getDealsByStatus`, `updateDeal`, `deleteDeal`.

**Commission plans (7):** `createCommissionPlan`, `getCommissionPlan`, `getAllCommissionPlans`, `getCommissionPlansByStatus`, `activateCommissionPlan`, `addRuleToPlan`, `deleteCommissionPlan`.

**Calculations (5):** `calculateCommission`, `getCommissionCalculation`, `getAllCommissionCalculations`, `getCalculationsByDeal`, `getCalculationsBySalesRep`.

**Disputes (8):** `createDispute`, `getDispute`, `getAllDisputes`, `getDisputesBySalesRep`, `getDisputesByStatus`, `resolveDispute`, `escalateDispute`, `deleteDispute`.

**Currency (4, proxied):** `convertCurrency`, `getLatestRates`, `listSupportedCurrencies`, `getHistoricalRates`.

**AI-powered (3, uses MCP sampling):** `analyzeDispute`, `explainCommission`, `summarizeSalesPerformance`.

**A2A bridge (1):** `delegateToDisputeAgent` — forwards a natural-language task to the dispute agent and returns its reply.

</details>

### Prompts (10 workflows)

`analyze-sales-performance`, `create-commission-workflow`, `dispute-resolution`,
`setup-commission-plan`, `monthly-commission-report`, `compare-plans`,
`audit-calculation`, `convert-deal-currency`, `multi-currency-commission-report`,
`currency-rate-check`.

### Resources (12)

Data: `deals://all`, `deals://active`, `plans://all`, `plans://active`,
`disputes://all`, `disputes://open`, `calculations://all`, `currency://supported`,
`currency://rates`, `currency-rates://{baseCurrency}`.
Schemas: `schema://deal`, `schema://commission-plan`, `schema://dispute`.

### Connecting Claude Desktop (Streamable HTTP via `mcp-remote`)

1. **Start the server:** `mvn -Pverticalslice spring-boot:run` (or from JAR).
2. **Edit the config file**:
   - Windows: `%APPDATA%\Claude\claude_desktop_config.json`
   - macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
3. **Add:**
   ```json
   {
     "mcpServers": {
       "commission-calculator": {
         "command": "npx",
         "args": ["-y", "mcp-remote", "http://localhost:8081/mcp"]
       }
     }
   }
   ```
4. **Fully quit** Claude Desktop (tray → Quit, not just close window) and relaunch.
5. Tools appear under the 🔨 icon in the chat input. Try: *"File an URGENT dispute for sales rep usr-002 about a wrong 12% rate."*

Some Claude Desktop builds support a native `streamable-http` type — if yours does, you can skip `mcp-remote`:
```json
{ "mcpServers": { "commission-calculator": { "type": "streamable-http", "url": "http://localhost:8081/mcp" } } }
```

### Connecting Claude Desktop (STDIO)

Claude Desktop launches the server as a child process. Requires a packaged JAR.

```json
{
  "mcpServers": {
    "commission-calculator": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-Dspring.ai.mcp.server.protocol=STDIO",
        "-Dspring.main.web-application-type=none",
        "-Dlogging.pattern.console=",
        "-Dspring.main.banner-mode=off",
        "-jar",
        "C:\\Commission Calculator\\commission-calculator-spring\\target\\commission-calculator-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

Flag explanations: `stdio=true` + `protocol=STDIO` selects STDIO transport; `web-application-type=none` drops Tomcat; `logging.pattern.console=` and `banner-mode=off` keep stderr/stdout clean so the JSON-RPC stream isn't corrupted.

### MCP Inspector

Web UI for browsing tools/prompts/resources and running them.

**Streamable HTTP against a running server:**
```bash
npx @modelcontextprotocol/inspector --transport streamable-http http://localhost:8081/mcp
```

**SSE (legacy):**
```bash
npx @modelcontextprotocol/inspector --transport sse http://admin:admin123@localhost:8081/api/mcp/sse
```

**STDIO (inspector spawns the server):**
```bash
npx @modelcontextprotocol/inspector java -Dspring.ai.mcp.server.stdio=true -Dspring.ai.mcp.server.protocol=STDIO -Dspring.main.web-application-type=none -Dspring.main.banner-mode=off -Dlogging.level.root=OFF -jar "target/commission-calculator-0.0.1-SNAPSHOT.jar"
```

Open the printed URL (usually `http://localhost:5173`). Remember `--transport <name>` on the URL form — without it, `npx` tries to `exec` the URL and errors with `ENOENT`.

### Interactive CLI (REPL, no external client needed)

```bash
mvn spring-boot:run -Pverticalslice -Dspring-boot.run.profiles=cli
```
Or from a packaged JAR: `java -Dspring.profiles.active=cli -jar target/commission-calculator-0.0.1-SNAPSHOT.jar`.

```
> tools                              # list all 35
> tool getAllDeals                   # invoke without args
> tool createDeal {"title":"X","value":1000,"salesRepId":"REP001"}
> prompts                            # list prompts
> prompt create-commission-workflow  # show prompt details
> resources                          # list resources
> resource deals://all               # read a resource
> search dispute                     # search by keyword
> exit
```

### curl quick tests

```bash
# Server info (custom REST shim)
curl -u admin:admin123 http://localhost:8081/api/mcp/info

# Streamable HTTP — initialize, then tools/list (needs Mcp-Session-Id cookie from first response)
curl -i -X POST http://localhost:8081/mcp -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" -d '{"jsonrpc":"2.0","id":"1","method":"initialize","params":{"protocolVersion":"2025-11-25","capabilities":{},"clientInfo":{"name":"probe","version":"0.1"}}}'
# ^ grab Mcp-Session-Id header, then:
curl -X POST http://localhost:8081/mcp -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" -H "Mcp-Session-Id: <paste>" -d '{"jsonrpc":"2.0","id":"2","method":"tools/list"}'

# Custom REST shim — no session needed
curl -u admin:admin123 -X POST http://localhost:8081/api/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"name":"getAllDeals","arguments":{}}'
```

---

## A2A — Agent-to-Agent

The `infrastructure/a2a` package implements a specialist **dispute-filing agent** using
the [Spring AI A2A starter](https://spring.io/blog/2026/01/29/spring-ai-agentic-patterns-a2a-integration).
Other agents (or Claude via MCP) hand it a natural-language task; it resolves ids,
picks the right internal tool, and returns a structured reply.

### Activation

Defaults in `application.properties`:
```properties
spring.ai.a2a.server.enabled=true
a2a.dispute-agent.url=http://localhost:8081
```
Plus **`ANTHROPIC_API_KEY`** must be in the environment — the agent needs a real
`ChatClient.Builder` to reason. Set to `false` (server) if you don't have a key and
don't want the autoconfigure to fail.

### Endpoints

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/.well-known/agent-card.json` | Agent discovery (AgentCard JSON) |
| `POST` | `/` | JSON-RPC 2.0 `message/send` — the A2A protocol |
| `POST` | `/a2a-client/send` | Plain-text client wrapper (posts through `DisputeClient` to `/`) |

All three are public (no Basic Auth) and exempt from CSRF — see `SecurityConfig.java`.

### Agent card preview

```json
{
  "name": "Dispute Filing Agent",
  "description": "Files commission disputes on behalf of a calling agent ...",
  "url": "http://localhost:8081",
  "protocolVersion": "0.3.0",
  "skills": [
    { "id": "file-dispute",   "name": "File a commission dispute", ... },
    { "id": "lookup-dispute", "name": "Look up commission disputes", ... }
  ]
}
```

Skill ids are advisory (for prompt planning) — A2A agents don't expose per-skill
endpoints. You always send a free-form `Message` with a `TextPart` to `POST /`.

### Internal tools (what the agent's LLM can pick)

Declared in `DisputeAgentTools.java`:

| Tool | Purpose |
|---|---|
| `listCalculations` | Returns every calculation so the agent can discover a valid `calculationId`/`salesRepId` pair |
| `listCalculationsForSalesRep(salesRepId)` | Narrower variant |
| `getCalculation(id)` | Verify a specific calc exists |
| **`createDispute(calculationId, salesRepId, title, description, priority)`** | **The core action — creates the dispute** |
| `listDisputes` | Verify filings |
| `getDispute(id)` | Fetch one |

### Calling from outside

**Plain REST wrapper (easiest for shell / testing):**
```bash
curl -X POST http://localhost:8081/a2a-client/send \
  -H "Content-Type: text/plain" \
  --data "File an URGENT dispute for rep usr-002 about a wrong 12% rate - expected 15%."
```
Response body is the agent's reply text (Markdown).

**Direct A2A JSON-RPC** (peers that speak the protocol):
```bash
curl -X POST http://localhost:8081/ \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc":"2.0","id":"1","method":"message/send",
    "params":{"message":{
      "role":"user",
      "parts":[{"kind":"text","text":"List all disputes."}],
      "messageId":"probe-001","kind":"message"
    }}
  }'
```
Response is a full `Task` result with `status.state=completed`, `artifacts[].parts[].text` carrying the agent's reply.

**From Claude Desktop via MCP:** call the `delegateToDisputeAgent` tool with
`{ "task": "…" }`. Under the hood this invokes `McpCommissionTools.delegateToDisputeAgent` →
`DisputeClient.sendTask` → JSON-RPC `POST /` → `DefaultAgentExecutor` → inner Claude →
back through all layers.

**From an embedded CLI at startup:** pass `a2a.client.cli.task=…` and the
`DisputeAgentCli` (fires on `ApplicationReadyEvent`) sends the task once, prints the
reply, and keeps the app running.

### End-to-end flow (Claude Desktop example)

```
user → Claude Desktop
       ↓ tools/call delegateToDisputeAgent
       mcp-remote → POST /mcp
       ↓ (in-process) McpCommissionTools.delegateToDisputeAgent
       DisputeClient.sendTask("File an URGENT dispute ...")
       ↓ JSON-RPC message/send
       POST /
       ↓ MessageController → RequestHandler → DefaultAgentExecutor
       ChatClient.prompt().call()  (inner Claude + DisputeAgentTools)
       ↓ LLM picks listCalculations, inspects calc-003, then calls createDispute(...)
       Task { status=completed, artifacts[...].text = "Dispute filed: id=..." }
       ↓ back up the stack
user sees the dispute id and status
```

### Design note — why `DisputeClient` has no `@Tool`

Earlier `DisputeClient.fileDisputeViaAgent` was `@Tool`-annotated. In the same JVM
that runs both the A2A server and its caller, the project-wide tool scanner (`spring.ai.mcp.server.annotation-scanner.enabled=true`)
exposed that tool to the **server agent's own ChatClient**, which picked it and
recursively POSTed to itself until the 60 s timeout tripped. The annotation was
removed; call `DisputeClient.sendTask(...)` directly (or via the REST wrapper /
`delegateToDisputeAgent` MCP bridge).

---

## Feature Flags (Togglz)

Runtime feature flags at `/togglz-console`.

| Flag | Default | Purpose |
|---|---|---|
| `CURRENCY_CONVERSION` | ON | Gates `/api/currency/**` |
| `BETA_DASHBOARD` | OFF | Beta UI for selected users |
| `ADVANCED_ANALYTICS` | OFF | Advanced commission analytics |
| `BULK_IMPORT` | OFF | Bulk deal import |

Activation strategies available: **kill switch** (default), **gradual rollout** by %, **username targeting**, **release date**, and a custom **`RegionActivationStrategy`** that reads the `X-Region` header or `APP_REGION` env var. Test with:
```
curl -H "X-Region: eu-west" http://localhost:8081/api/currency/supported
```

---

## Observability

- **Health:** `http://localhost:8081/actuator/health`
- **Prometheus:** `http://localhost:8081/actuator/prometheus` → search `feature_flag_*`
- **Swagger UI:** `http://localhost:8081/swagger-ui/`
- **OpenAPI JSON:** `http://localhost:8081/api-docs`
- **H2 console:** `http://localhost:8081/h2-console` (JDBC `jdbc:h2:mem:commissiondb`, user `sa`)
- **CORS:** allows `http://localhost:*`, `http://127.0.0.1:*`, and `*.ngrok-free.{app,dev}` / `*.ngrok.{io,app,dev}` (see `SecurityConfig.java`).

---

## Testing

### Unit tests
```bash
mvn test -Dtest=*ServiceTest
```

### MCP integration tests
```bash
mvn test -Dtest=McpServerIntegrationTest
```

### A2A smoke test (server running)
```bash
# Agent card
curl http://localhost:8081/.well-known/agent-card.json | jq .

# Full round-trip
curl -X POST http://localhost:8081/a2a-client/send \
  -H "Content-Type: text/plain" \
  --data "List all disputes."
```

---

## Common Workflows

### Commission calculation
```
createDeal → createCommissionPlan → addRuleToPlan → activateCommissionPlan →
calculateCommission → getCommissionCalculation
```

### Dispute resolution (manual)
```
getCommissionCalculation → createDispute → getDispute → escalateDispute → resolveDispute
```

### Dispute filing via the A2A agent
```
user prompt  →  delegateToDisputeAgent(task)  →  inner agent runs
listCalculations → createDispute(priority=URGENT) → returns new dispute id
```

### Sales rep report
```
getDealsBySalesRep → getCalculationsBySalesRep → getDisputesBySalesRep → compose report
```

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `NoResourceFoundException: No static resource mcp` on `POST /mcp` | Streamable HTTP not registered because `spring.ai.mcp.server.stdio=true` | Set `stdio=false` and restart |
| `Parameter 0 of method logAgentCard ... AgentCard ... could not be found` | Wrong main class (ORM instead of verticalslice) or missing ANTHROPIC_API_KEY | Use `mvn -Pverticalslice spring-boot:run` and export the key |
| A2A call times out at 60 s | Response came as `TaskUpdateEvent` not `MessageEvent` (fixed — consumer handles all three event kinds) | Pull latest; timeout is now 180 s |
| curl `mcp` returns 400 on `tools/list` | Missing `Mcp-Session-Id` header from `initialize` response | Capture the header and re-send |
| CORS 403 "Invalid CORS request" from a ngrok tunnel | Origin not in the allow-list patterns | Add the ngrok TLD pattern in `SecurityConfig` |

---

## Tech stack

Spring Boot 3.4.5 · Spring Data JPA · Spring Security · Spring AI 1.1.0 (MCP server webmvc, Anthropic starter, MCP client, Vector Store) · Spring AI A2A 0.2.0 + a2a-java-sdk 0.3.3 · H2 · Flyway · Togglz · Lombok · SpringDoc OpenAPI · JUnit 5 · Mockito · Micrometer.

---

**Built with Spring Boot 3.4.5 · Vertical Slice · MCP server + client · A2A agent · Togglz**
