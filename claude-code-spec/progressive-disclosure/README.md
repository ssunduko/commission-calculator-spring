# AI-Assisted Development Workflow — Commission Calculator (Java / Spring Boot / MCP)

This directory holds the progressive-disclosure documentation system for new features in the Commission Calculator project. It adapts the AI-Assisted Development How-To Guide to our stack: Java 21, Spring Boot 3.4, Spring Data JPA, Spring AI MCP server, JUnit 5 + Mockito + AssertJ for tests.

The system has four artifact layers. Each one feeds the next. Nothing skips ahead.

```
Layer 1  Feature README          full vertical-slice context (problem, layers, success criteria)
Layer 2  Story README            one thin vertical cut, files involved, hard constraints
Layer 3  Failing tests           one test per acceptance criterion, must fail before any AI session
Layer 4  AI instruction block    task + scoped context + failing tests + success condition
```

After the AI session: micro pivot loop until tests pass, then a fresh judge session, then an ADR if a decision was made, then a PR.

## Workflow at a Glance

```
FEATURE README            (written and approved before any story begins)
        |
        v
USER STORIES              (one story = one thin vertical cut through every layer it needs)
        |
        v
STORY README              (per story, before any code)
        |
        v
FAILING TESTS             (JUnit 5, written before implementation, must all fail)
        |
        v
AI INSTRUCTION BLOCK      (task + context + failing tests + success condition + constraints)
        |
        v
MICRO PIVOT LOOP          (scope unit -> implement -> review diff -> pivot or confirm -> compact)
        |
        v
AI JUDGE                  (fresh session, no implementation history, scored against criteria)
        |
        v
ADR                       (only if a significant decision was made)
        |
        v
PULL REQUEST              (reviewer reads story README + ADR, not the full diff)
        |
        v
NEXT STORY
```

## Vertical Slicing in This Codebase

A vertical slice is a feature that cuts through every layer of the stack in a single deliverable. For Commission Calculator the layers are:

```
MCP Tool wrapper           infrastructure/mcp/McpCommissionTools.java
        |
REST Controller            features/{feature}/{Entity}Controller.java
        |
Service (business logic)   features/{feature}/{Entity}Service.java
        |
Repository                 features/{feature}/{Entity}Repository.java
        |
JPA Entity                 domain/{Entity}.java
        |
H2 / PostgreSQL schema     auto-derived from @Entity
```

DTOs live alongside the service: `Create{Entity}Request`, `Update{Entity}Request`, `{Entity}Response` — all Java `record` types.

A horizontal slice (one layer at a time) is the wrong approach. When the AI is asked to "create the entity" in isolation it cannot make good shape decisions because it does not know what the controller will return or how the MCP tool will expose it. Define the API and MCP tool shape **first** in the Feature README, then build the entity, repository, and service to produce that shape.

## Layer Map per Story

Every story declares which layers it touches and which it does not. This keeps AI sessions scoped correctly.

```
Layer              File Locations                                              Example: COMM-01
-----------------  ----------------------------------------------------------  ----------------
Database           @Entity attributes (JPA auto-DDL) or src/main/resources/    yes
                   db/migration/ if Flyway is added
Domain Entity      verticalslice/domain/                                       yes
Repository         verticalslice/features/{feature}/{Entity}Repository.java   yes
Request DTO        verticalslice/features/{feature}/Create{Entity}Request.java yes
Response DTO       verticalslice/features/{feature}/{Entity}Response.java     yes
Service            verticalslice/features/{feature}/{Entity}Service.java      yes
Controller         verticalslice/features/{feature}/{Entity}Controller.java   no  (COMM-02)
MCP Tool           verticalslice/infrastructure/mcp/McpCommissionTools.java   no  (COMM-03)
```

A story that only touches one layer is a horizontal slice in disguise. Reject it.

## Session Types

```
Session Type        Opens With                              Closes With
------------------  -------------------------------------   ------------------------------------
Implementation      Story README + scoped failing tests +   Compact summary + passing tests
                    relevant existing methods (excerpts)
Pivot (correction)  Compact summary + specific error +      Corrected unit + passing tests
                    affected method
Judge               Code diff + review criteria +           Findings list (Pass/Fail per criterion)
                    architecture rules
```

The judge session must be fresh. A judge that helped write the code cannot objectively critique it.

## Directory Layout

```
claude-code-spec/progressive-disclosure/
  README.md                          this file
  templates/
    FEATURE_README_TEMPLATE.md       Layer 1 template
    STORY_README_TEMPLATE.md         Layer 2 template
    AI_INSTRUCTION_TEMPLATE.md       per-session instruction block
    JUDGE_PROMPT.md                  copy-paste judge prompt
    ADR_TEMPLATE.md                  decision record
  features/
    {feature-name}/
      README.md                      Layer 1: feature context
      stories/
        01-{slug}/
          README.md                  Layer 2: story context
          tests/
            {TestClass}.java         failing tests, written before code
          adr/
            0001-{slug}.md
        02-{slug}/
          ...
```

Code stays in the normal Spring Boot layout under `src/main/java/...`. This directory is documentation only.

## Stack Cheat Sheet

### Test stack
- **JUnit 5** with `@Test`, `@Nested`, `@DisplayName`
- **Mockito** with `@Mock`, `@InjectMocks`, `Mockito.verify`, `Mockito.when`
- **AssertJ** for fluent assertions: `assertThat(x).isEqualTo(y)`, `.hasSize(n)`, `.containsExactly(...)`
- **MockMvc** for controller tests via `@WebMvcTest`
- **`@DataJpaTest`** for repository tests
- **`@SpringBootTest`** for full integration tests
- **`@MockitoBean`** (NOT `@MockBean` — deprecated in Spring Boot 3.4+) from `org.springframework.test.context.bean.override.mockito.MockitoBean`

### Conventions enforced by the codebase
1. One feature = one package under `features/{name}/`. Never scatter.
2. No interfaces between layers. Concrete classes only.
3. Java `record` for all DTOs. Requests have a `validate()` method. Responses have a `static from(Entity)` factory.
4. Every service operation gets an `@Tool` wrapper in `McpCommissionTools`.
5. Shared domain entities live in `domain/`. Feature-specific logic stays in the feature package.
6. Exceptions: `ResourceNotFoundException` (404) or `ValidationException` (400). Do not catch `Exception`.
7. Constructor injection. No field `@Autowired`. No setter injection.
8. No raw JDBC. All data access through Spring Data JPA repositories.

## Pivot Triggers in Java/Spring Output

Stop and correct immediately on any of these:

```java
// Business logic on the entity -- move to service
@Entity
public class CommissionCalculation {
    public BigDecimal calculate(Deal deal) { ... }   // wrong
}
```

```java
// Field injection -- use constructor injection
@Service
public class CommissionCalculationService {
    @Autowired private DealRepository dealRepo;      // wrong
}
```

```java
// Catch-all exception swallow -- be specific, log
try {
    repository.save(entity);
} catch (Exception e) { }                            // wrong
```

```java
// Raw JDBC -- use repository
jdbcTemplate.execute("INSERT INTO commission_calculations ...");   // wrong
```

```java
// System.out -- use SLF4J
System.out.println("created: " + id);                // wrong
log.info("commission_calculation_created id={}", id); // correct
```

```java
// Missing @Transactional on multi-step writes
public void calculateAndPay(String dealId) {
    var calc = calculate(dealId);                    // two writes
    payoutService.schedulePayout(calc.id());
}                                                    // no @Transactional -- wrong
```

```java
// N+1 queries -- use fetch joins or batch
List<Deal> deals = dealRepo.findAll();
for (var d : deals) {
    var calcs = calcRepo.findByDealId(d.getId());    // N+1 -- wrong
}
```

```java
// principal.account_id from request -- pull from SecurityContext
String accountId = request.accountId();              // wrong
String accountId = SecurityContextHolder.getContext()
    .getAuthentication().getName();                  // correct (or principal claim)
```

## Token Economy Rules

The context window is finite. Every rule below either reduces tokens or improves the signal-to-noise ratio.

1. **Never paste a full file over ~100 lines.** Paste the relevant method excerpts with file path and line numbers.
2. **Strip before you paste.** Drop Javadoc blocks, blank lines beyond one separator, commented-out legacy, irrelevant imports.
3. **Summarize test output, do not paste it raw.** "Failing: `calculatesBaseCommission` -- expected 15000.00, got 0.00 at line 42. 4 other tests pass." beats 30 lines of Surefire output.
4. **Compact between sessions.** Ask the AI for a <200 word handoff summary; use it to open the next session.
5. **Reference established code by name.** Once `CommissionCalculationService.create(...)` is implemented, do not re-paste it in the next unit's instruction.
6. **Scope the test class to the unit.** Paste only the `@Nested` block for the current unit. Run the full class only at story end.
7. **One session per unit.** Close and start fresh after 15-20 exchanges or when corrections accumulate.
8. **Fresh session for the judge.** No implementation history.
9. **The Story README is the context budget.** If you need extra context, update the story README first, then add it to the session.
10. **State what you already tried.** Tell the next session what failed and why so it does not retry the dead end.
11. **Define the API and MCP tool shape in the Feature README first.** Every story references the shape by name; one definition, many references.

### Approximate token costs

```
Input                                       Tokens
-------------------------------------       --------------
Full Java file, 100 lines                   600 - 800
Full Java file, 500 lines                   3,000 - 4,000
Method excerpt, 30 lines                    150 - 200
Method excerpt, stripped                    50 - 80
Full Surefire output, 1 failure             200 - 400
Summarized test failure, 3 lines            20 - 40
Full session history, 20 messages           15,000 - 25,000
Compact summary, 200 words                  250 - 350
Story README, well-written                  500 - 800
Feature README, full                        800 - 1,200
```

A disciplined session opens with ~600-1,200 tokens. An undisciplined one opens with 30,000-50,000. Output quality is the same.

## Quick Reference: Per-Story Artifact Sequence

1. Story README written (human)
2. Failing tests written (human)
3. Tests run and all fail (human confirms)
4. AI instruction block written (human)
5. Unit 1 implementation session
6. Diff reviewed; pivot if needed
7. Compact summary; open Unit 2 session
8. Repeat through all units
9. All tests pass
10. Judge session (fresh, no history)
11. Fix judge findings
12. ADR if a significant decision was made
13. Story README updated with implementation notes
14. PR opened (reviewer reads story README + ADR)

## When to Write an ADR

Write one when you:
- Choose where in the stack a behavior lives (service vs entity lifecycle callback vs controller vs MCP tool)
- Define data scope (per-account, per-rep, global)
- Choose between two reasonable technical approaches
- Reject an approach that seems obvious
- Accept a known performance, security, or UX tradeoff
- Establish a pattern future stories must follow

ADRs live next to the story (`stories/01-.../adr/`) for story-specific decisions, or at the feature root (`features/{feature}/adr/`) when they affect multiple stories in the feature.

## Worked Example

See `features/calculations/` for an end-to-end example: feature README, one fully written story with a failing JUnit test class, and an ADR. Use it as a reference when writing your own.
