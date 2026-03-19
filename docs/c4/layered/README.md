# C4 Architecture Diagrams — Commission Calculator (Layered Architecture)

This directory contains [C4 model](https://c4model.com/) architecture diagrams for the Commission Calculator's two layered architecture implementations: the **SpringBoot module** and the **ORM module**. All diagrams are in PlantUML format.

## Two Modules, One Domain

The project contains two layered architecture packages that implement the same commission domain at different levels of infrastructure maturity:

| Aspect | SpringBoot Module | ORM Module |
|--------|-------------------|------------|
| **Persistence** | ConcurrentHashMap | JPA/Hibernate + H2 |
| **Schema** | None (in-memory) | Flyway SQL migrations |
| **Security** | JWT + Form Login (RBAC) | Permit all (educational) |
| **Web UI** | Thymeleaf dashboard | REST API only |
| **DTOs** | Request + Response + Mapper | Direct entity return |
| **Caching** | None | Spring Cache (4 regions) |
| **Transactions** | None (HashMap) | @Transactional with isolation levels |
| **Query Strategies** | Stream/filter | 5 JPA strategies |

---

## Diagram Overview

### Level 0: System Landscape (`c4-level0-system-landscape.puml`)

**Question answered:** *What systems exist in the Commission Calculator platform and how do they fit together?*

The highest-level view showing all four modules (SpringBoot, ORM, AI, Vertical Slice), all actor types (Sales Rep, Manager, Finance Admin, System Admin, AI/MCP Client), and all external systems (Anthropic Claude API, H2 Database, Grafana OTEL, Swagger UI). Shows how the entire platform is a single deployable JAR with four distinct architectural styles.

**Key takeaways:**
- Four modules in one JAR: SpringBoot (layered + security), ORM (layered + JPA), AI (agent-based), Vertical Slice (feature-based)
- Each module has its own API prefix: `/api/**`, `/api/orm/**`, `/api/ai/**`, `/api/vertical/**`
- Only SpringBoot module enforces JWT + RBAC security; others permit all (educational)
- AI module connects to Anthropic Claude API for LLM-powered analysis

---

### Level 1: System Context (`c4-level1-system-context.puml`)

**Question answered:** *What are the two layered modules and who interacts with them?*

Shows both the SpringBoot and ORM modules as separate systems with shared actors (Sales Reps, Managers, Finance Admins, System Admins) and external dependencies (H2 Database, Swagger UI, Web Browser).

**Key takeaways:**
- Four actor types with role-based access (SpringBoot module enforces RBAC; ORM permits all)
- SpringBoot module serves both REST API and Thymeleaf web UI
- ORM module is the only one with a real database (H2)

---

### Level 2: Container — SpringBoot (`c4-level2-container-springboot.puml`)

**Question answered:** *What are the technical layers in the SpringBoot module?*

Shows the classic 5-layer stack: Presentation (8 controllers) → Security (JWT dual chains) → Service (5 services) → DTO (mapper + validation) → Repository (5 HashMap repos). Includes cross-cutting concerns: exception handling, configuration, data initialization.

**Key takeaways:**
- Dual SecurityFilterChain: `@Order(1)` for API (JWT stateless), `@Order(2)` for web (form login)
- ConcurrentHashMap repositories — no database, data re-seeded each startup
- DtoMapper converts entities to responses, excluding sensitive fields (passwordHash)

---

### Level 2: Container — ORM (`c4-level2-container-orm.puml`)

**Question answered:** *What are the technical layers in the ORM module?*

Shows the data-access-focused stack: Controllers → Services (@Transactional) → Cache Layer → JPA Repositories → JPA Entities → H2 Database (Flyway-managed). Emphasizes the query, transaction, and caching patterns.

**Key takeaways:**
- Spring Cache with 4 regions (users, deals, calculations, commissionPlans)
- @Transactional with isolation (REPEATABLE_READ for calculations) and propagation (REQUIRES_NEW for audit)
- Flyway manages 14+ tables with 40+ indexes
- 6 JPA repositories using 5 different query strategies

---

### Level 3: Component — SpringBoot (`c4-level3-component-springboot.puml`)

**Question answered:** *What Spring components exist in each layer of the SpringBoot module?*

Shows every controller, service, repository, security component, DTO, and cross-cutting concern. Highlights the CommissionCalculationService as the only "orchestrating" service (depends on DealService + PlanService).

**Key takeaways:**
- JWT auth flow: JwtAuthenticationFilter → JwtTokenProvider → CustomUserDetailsService → UserRepository
- 7 REST controllers + 1 MVC web controller (DashboardController)
- GlobalExceptionHandler maps 7 exception types to HTTP statuses
- SampleDataLoader creates 6 users, 3 plans, 6 deals, 4 calculations, 1 dispute

---

### Level 3: Component — ORM (`c4-level3-component-orm.puml`)

**Question answered:** *What Spring components exist in each layer of the ORM module?*

Shows all controllers, services (with transaction annotations), cache configuration, JPA repositories (with query strategy annotations), entity classes, and Flyway migrations. DealSpecifications shown as a separate component for dynamic queries.

**Key takeaways:**
- Services use class-level `@Transactional(readOnly=true)` with method-level overrides for writes
- DealRepository implements `JpaSpecificationExecutor` for dynamic search
- CommissionPlanRepository uses split fetches to avoid MultipleBagFetchException
- CommissionCalculationRepository has `@Modifying` bulk update operations

---

### Level 4: Code — SpringBoot (`c4-level4-code-springboot.puml`)

**Question answered:** *What do the DTOs, security classes, and exception hierarchy look like?*

Shows class-level detail for the SpringBoot module's unique components: 6 request DTOs with Bean Validation annotations, 8 response DTOs, JWT security classes (provider, filter, config), DtoMapper, and the exception hierarchy.

**Key takeaways:**
- Request DTOs use Jakarta Validation (@NotBlank, @Email, @DecimalMin, @Size)
- Response DTOs exclude sensitive data (passwordHash) and include computed fields (bonusCount)
- JWT tokens signed with HMAC-SHA256, 24-hour expiration
- Nested DTO pattern: DealResponse contains List\<ProductInfo\>

---

### Level 4: Code — ORM (`c4-level4-code-orm.puml`)

**Question answered:** *What do the JPA entities look like and how are they related via FK annotations?*

Shows all 29 JPA entity classes with their fields, JPA annotations (@ManyToOne, @OneToMany, @ManyToMany, @OneToOne, @ElementCollection), cascade rules, fetch strategies, and database indexes. The aggregate root pattern is highlighted for CommissionPlan.

**Key takeaways:**
- True JPA FK relationships (unlike vertical slice's String ID references)
- CommissionPlan as aggregate root: owns Rules, Tiers, Bonuses via cascade ALL + orphan removal
- Self-referential relationship: User → User (manager/directReports)
- @ManyToMany with join table: User ↔ Company (user_companies)
- All @ManyToOne/@OneToMany use LAZY by default

---

### Supplementary: SpringBoot vs ORM Comparison (`c4-supplementary-springboot-vs-orm.puml`)

**Question answered:** *What are the differences between the two modules and why do both exist?*

Side-by-side comparison with a feature matrix and evolution narrative explaining how an application progresses from simple HashMap persistence (SpringBoot) to full JPA/Hibernate (ORM). Includes directory structure for both modules.

---

### Supplementary: JWT Security Flow (`c4-supplementary-security-flow.puml`)

**Question answered:** *How does JWT authentication work end-to-end in the SpringBoot module?*

Sequence diagram covering: login (credentials → JWT), authorized request (JWT → SecurityContext → controller), unauthorized request (no token → 401), role-based denial (insufficient role → 403), and web form login (session-based, separate chain). Includes RBAC matrix.

---

### Supplementary: Data Access Patterns (`c4-supplementary-data-access-patterns.puml`)

**Question answered:** *How does the ORM module query, cache, and manage transactions?*

Sequence diagram showing 7 patterns: derived queries, JPQL @Query, @EntityGraph (N+1 prevention), Specifications (dynamic search), @Cacheable hit/miss/eviction, REPEATABLE_READ isolation for calculations, and REQUIRES_NEW propagation for audit comments.

---

### Supplementary: Entity-Relationship Diagram (`c4-supplementary-erd.puml`)

**Question answered:** *What does the database schema look like and how are the tables related?*

Full ERD showing all 15+ tables managed by Flyway migrations, including columns, primary/foreign keys, data types, enum values, and cardinality relationships. Organized by domain: User Management, Deal Management, Commission Plans, Calculations, and Disputes.

**Key takeaways:**
- All PKs are UUID (VARCHAR(36)), monetary values use DECIMAL(15,2), rates use DECIMAL(5,4)
- CommissionPlan is an aggregate root owning Tiers, Rules, and Bonuses (cascade ALL + orphan removal)
- User self-referential FK (manager_id) for org hierarchy
- Many-to-many User-Company via join table (user_companies)
- 40+ indexes including composite indexes on (sales_rep_id, status)

---

### Supplementary: Deployment Diagram (`c4-supplementary-deployment.puml`)

**Question answered:** *How is the application deployed, what processes run, and how do they communicate?*

C4 deployment diagram showing the runtime topology: Spring Boot application (embedded Tomcat on Java 21), H2 embedded database, local vector store, external Anthropic Claude API, Grafana OTEL observability stack (Docker Compose), and client access points (Swagger UI, Thymeleaf dashboard, REST clients, MCP clients).

**Key takeaways:**
- Single deployable JAR containing all modules (SpringBoot, ORM, AI, Vertical Slice)
- Three Spring profiles: Default (8081), Dev (8082), Prod (8080)
- Observability via Grafana OTEL LGTM container (ports 3000, 4317, 4318)
- AI integration with Anthropic Claude API (Claude Sonnet, temp 0.3)
- MCP server exposed via SSE protocol for AI tool integration

---

## How to Render

These diagrams use [PlantUML](https://plantuml.com/) syntax with the C4 PlantUML library.

**Options:**

1. **VS Code**: Install the [PlantUML extension](https://marketplace.visualstudio.com/items?itemName=jebbs.plantuml) and preview with `Alt+D`
2. **IntelliJ IDEA**: Install the PlantUML integration plugin
3. **Online**: Paste into [PlantUML Web Server](https://www.plantuml.com/plantuml/uml)
4. **CLI**: `java -jar plantuml.jar docs/architecture/layered/*.puml`
