# Judge Session Prompt — Commission Calculator (Java / Spring Boot / MCP)

Open a fresh session. Paste **no** prior conversation history. The judge must arrive cold.

---

## Prompt

You are a senior Java engineer doing an independent code review. You have no prior knowledge of how this code was written. Evaluate the code below against each criterion. For each criterion: state **Pass** or **Fail**. If Fail, state the specific issue and the exact correction needed. Cite file path and line numbers.

### Stack Context

- Java 21, Spring Boot 3.4.5, Spring Data JPA, Spring Security, Spring AI MCP
- JUnit 5 + Mockito + AssertJ for tests; MockMvc for controllers; `@DataJpaTest` for repositories
- H2 in development (auto-DDL from `@Entity`); PostgreSQL is the production target
- All DTOs are Java `record` types

### Architecture Rules This Codebase Enforces

1. One feature = one package under `features/{name}/`. Never scattered.
2. Concrete classes only between layers. No interfaces between Controller / Service / Repository.
3. Java `record` for all DTOs. Requests have `validate()`. Responses have `static from(Entity)`.
4. Every service operation has an `@Tool` wrapper in `infrastructure/mcp/McpCommissionTools.java`.
5. Shared domain entities live in `domain/`. Feature-specific logic stays in the feature package.
6. Exceptions: `ResourceNotFoundException` (404) or `ValidationException` (400). No `catch (Exception e)`.
7. Constructor injection only. No field `@Autowired`. No setter injection.
8. All data access through Spring Data JPA. No raw JDBC, no `EntityManager.createNativeQuery` for ordinary CRUD.
9. SLF4J for logging (`private static final Logger log = LoggerFactory.getLogger(...)`). No `System.out`, no `e.printStackTrace()`.

---

## Review Criteria

### 1. Correctness
Are there execution paths where the tests pass but production behavior is wrong? Look for off-by-one, null returns where Optional is expected, silent fallthrough, incorrect arithmetic precision (`BigDecimal` rounding mode), wrong status transitions.

### 2. Missing Edge Cases
What inputs or states would cause this to fail in production that the tests do not cover? Empty lists, null fields not asserted in `validate()`, concurrent modification, `BigDecimal` scale mismatches, currency mixing, status transitions from unexpected states.

### 3. Architecture Compliance
- Business logic in `@Service`, not on the entity, not in the controller?
- Entities clean of behavior beyond getters/setters/relationships and Lombok?
- Controllers thin — validate input, call service, return `ResponseEntity`?
- DTOs are records with `validate()` / `from()`?
- MCP `@Tool` wrapper added in `McpCommissionTools.java` for every new service operation?

### 4. Persistence and Query Performance
- N+1 queries from lazy associations? Should use a fetch join or `@EntityGraph`?
- Queries inside loops? Should be batched.
- Missing indexes on filter columns named in repository methods?
- Multi-step writes wrapped in `@Transactional` at the service method level?
- `BigDecimal` scale and `RoundingMode` set explicitly on all monetary calculations?

### 5. Error Handling and Observability
- Exceptions caught specifically (e.g. `DataIntegrityViolationException`), not `Exception`?
- Significant state changes logged via SLF4J with structured key=value context (entity id, action, actor)?
- `ResourceNotFoundException` thrown for missing entities, not silent null returns?
- Validation errors thrown via `ValidationException` from `record.validate()`?

### 6. Security
- All input validated by `Request.validate()` before use?
- Authorization checked before data is returned or modified (Spring Security `@PreAuthorize` or controller-level guard)?
- IDs that scope data (e.g. `salesRepId`, `accountId`) sourced from the authenticated principal, **never** from request body or URL parameter?
- No SQL injection surface (no string concatenation into JPQL or native queries)?

### 7. MCP Tool Surface
- `@Tool(name = "...", description = "...")` wraps the new service operation?
- Tool name in camelCase matching the service method?
- Description is a complete sentence describing inputs and the return value (the LLM uses this to decide when to call the tool)?
- DTOs used as tool params are Java `records` (Spring AI requires this for schema generation)?

---

## Code to Review

(Paste the diff, or the new and modified files in full. Strip Javadoc and blank-line padding before pasting.)

---

# Account / Rep Isolation Security Pass

Run this **separately**, in another fresh session, for every controller that handles rep- or account-scoped data.

## Prompt

Review this controller for rep / account isolation security only. Answer each question for each endpoint and operation.

1. Is `salesRepId` / `accountId` on every query sourced from the authenticated principal (`SecurityContextHolder.getContext().getAuthentication()` or method `Principal` parameter), and **never** from the request body, query string, or URL parameter?
2. Is every resource verified to belong to the caller's scope before it is returned or modified? (i.e. is there a `findByIdAndSalesRepId(...)` or equivalent guard, not a plain `findById(...)`?)
3. Is there any execution path where a user from rep A could read or modify data belonging to rep B?

For each finding, show the exact code path. Pass or fail per question per endpoint.

Code: (paste the controller)

---

## Handling Findings

| Outcome | Action |
|---|---|
| Pass | Continue to next criterion. |
| Fail (valid) | Fix it. Re-run tests. Re-run the judge on the corrected code if the fix was significant. |
| Fail (judge is wrong) | Document why in the PR description so the human reviewer is not confused by the same apparent issue. Do not silently ignore it. |
| Fail (style only, not correctness) | Note in the PR. Discuss as a team whether to standardize. Do not block the PR. |
