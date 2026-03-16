# C4 Architecture Diagrams — Commission Calculator (Vertical Slice)

This directory contains [C4 model](https://c4model.com/) architecture diagrams for the Commission Calculator's vertical slice implementation, rendered in PlantUML.

## Diagram Overview

The C4 model describes software architecture at four levels of abstraction, each zooming deeper into the system.

### Level 1: System Context (`c4-level1-system-context.puml`)

**Question answered:** *What is the Commission Calculator and who interacts with it?*

Shows the system as a single box surrounded by the actors (Sales Reps, Sales Managers, Finance Admins, AI Agents) and external systems (H2 Database, Swagger UI) it interacts with. This is the starting point for anyone unfamiliar with the project.

**Key takeaways:**
- Four actor types with distinct responsibilities
- AI agents interact via MCP protocol (not just REST)
- H2 in-memory database (re-seeded on each startup)

---

### Level 2: Container (`c4-level2-container.puml`)

**Question answered:** *What are the major technical building blocks inside the system?*

Zooms into the system boundary to show logical containers: REST API Layer, MCP Server, Feature Slices, Domain Model, Infrastructure, and the H2 Database. Although deployed as a single Spring Boot JAR, these containers represent independently replaceable technical components.

**Key takeaways:**
- Feature Slices contain all business logic (4 vertical slices)
- MCP Server wraps feature slices for AI agent access
- Infrastructure provides cross-cutting concerns (security, exceptions, data seeding)

---

### Level 3: Component (`c4-level3-component.puml`)

**Question answered:** *What Spring components exist and how do they collaborate?*

The most detailed structural diagram. Shows every controller, service, repository, and infrastructure component with their relationships. Each vertical slice (Deals, Plans, Calculations, Disputes) is shown as a self-contained boundary with its own Controller → Service → Repository chain.

**Key takeaways:**
- Each feature slice is self-contained (Controller + Service + Repository + DTOs)
- Only one cross-slice dependency: CommissionCalculationService reads from DealRepo and PlanRepo
- McpCommissionTools is a facade wrapping all four feature services (27 tools total)
- GlobalExceptionHandler provides centralized error responses (404, 400, 500)

---

### Level 4: Code (`c4-level4-code.puml`)

**Question answered:** *What do the domain entities look like and how are they related?*

Shows all 23 entity classes with their key fields, 6 enums with all values, and the relationships between entities. This is the most granular view — useful for developers working with the data model.

**Key takeaways:**
- Entities reference each other by String ID fields (not JPA foreign keys), keeping slices loosely coupled
- All monetary values use BigDecimal(19,2) for financial precision
- All IDs are UUIDs generated in @PrePersist
- Status enums define clear lifecycles (e.g., Deal: OPEN → WON/LOST/CANCELLED)

---

### Supplementary: MCP Protocol Flow (`c4-supplementary-mcp-flow.puml`)

**Question answered:** *How do AI agents discover and invoke commission operations?*

A sequence diagram showing the MCP (Model Context Protocol) integration end-to-end: SSE connection establishment, session initialization, tool discovery, tool invocation, prompt template usage, and resource access. Includes the full catalog of 27 tools, 7 prompts, and 8+ resources.

**Key takeaways:**
- Two transport options: SSE (persistent streaming) and REST (request/response)
- Three MCP primitives: Tools (actions), Prompts (templates), Resources (data)
- Tool invocation flows: Agent → McpProtocolController → McpCommissionTools → Feature Service → Database

---

### Supplementary: Vertical Slice Pattern (`c4-supplementary-vertical-slice-pattern.puml`)

**Question answered:** *Why vertical slices instead of traditional layers, and how is code organized?*

A side-by-side comparison of traditional layered architecture vs. the vertical slice pattern used in this project. Includes the full directory structure mapping to show how the pattern manifests in the codebase.

**Key takeaways:**
- Code is organized by business capability (deals, plans, calculations, disputes), not technical layer
- Adding a new feature = adding one new package with its complete stack
- Shared concerns (domain entities, security, exceptions) live in dedicated packages
- The pattern maps naturally to MCP tools (each slice's operations become tool groups)

---

## How to Render

These diagrams use [PlantUML](https://plantuml.com/) syntax with the C4 PlantUML library.

**Options to render:**

1. **VS Code**: Install the [PlantUML extension](https://marketplace.visualstudio.com/items?itemName=jebbs.plantuml) and preview with `Alt+D`
2. **IntelliJ IDEA**: Install the PlantUML integration plugin
3. **Online**: Paste contents into [PlantUML Web Server](https://www.plantuml.com/plantuml/uml)
4. **CLI**: `java -jar plantuml.jar docs/architecture/*.puml`

## C4 Model Reference

| Level | Name | Shows | Audience |
|-------|------|-------|----------|
| 1 | System Context | System + actors + external systems | Everyone |
| 2 | Container | Major technical building blocks | Technical leads |
| 3 | Component | Spring beans and their relationships | Developers |
| 4 | Code | Classes, fields, relationships | Developers |

For more on the C4 model, see [c4model.com](https://c4model.com/).
