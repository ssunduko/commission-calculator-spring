# Story {ID}: {Title}

> Layer 2 of the progressive-disclosure system. This is the AI's primary context document for any session within this story. It is written and reviewed before any code or tests are written.

**Feature:** [`{feature-name}`](../../README.md)

## What This Story Delivers

Two to four sentences. What complete vertical behavior exists after this story that did not exist before? Name every layer that changes.

## Vertical Layers Touched in This Story

```
Database     {specific change, or "none"}
Domain       {specific entity/enum change, or "none"}
Repository   {specific change, or "none"}
DTOs         {Request/Response records added, or "none"}
Service      {specific class + method, or "none"}
Controller   {specific endpoint, or "none"}
MCP Tool     {tool name + signature, or "none — added in story {ID}"}
```

If a layer is "none" but is named in the feature README, explain why this story is correct without touching it. (Usually: another story owns that layer.)

## Acceptance Criteria

Each criterion must be testable from the outside. Not an implementation detail.

1. {Observable behavior 1}
2. {Observable behavior 2}
3. {Edge case behavior}

Each criterion must map to one or more tests in `tests/`.

## Files Involved

**New files (create these):**
- `src/main/java/com/chapman/edu/commissions/architecture/verticalslice/{path}/{NewFile}.java` — what this file is
- ...

**Modified files (change only these, only as described):**
- `src/main/java/.../{ExistingFile}.java` — which method to change and what the change is
- ...

**Do not touch:**
- `src/main/java/.../{OtherFile}.java` — reason it might seem relevant but must not change

## Existing Code to Understand

Paste only the specific methods the AI needs to write compatible code with. Not whole files. Label every excerpt with file path and line numbers.

```java
// src/main/java/.../{ExistingFile}.java -- lines NN to MM -- {methodName}
public {ReturnType} {methodName}({Params}) {
    // current implementation
}
```

## Hard Constraints

- Do not modify the test file.
- Do not modify any file not listed under "Modified files" above.
- Do not introduce new Maven dependencies.
- Do not catch `Exception` — catch the specific exception type. Always log on catch.
- Use constructor injection. No field `@Autowired`.
- All DTOs are Java `record` types. Requests have a `validate()` method. Responses have a `static from(Entity)` factory.
- All data access through Spring Data JPA repositories. No raw JDBC.
- {Story-specific constraint, e.g., "salesRepId must come from the authenticated principal, not from request body"}

## Tests That Must Pass

- File: `claude-code-spec/progressive-disclosure/features/{feature}/stories/{NN-slug}/tests/{TestClass}.java`
- Run: `mvn test -Dtest={TestClass}`

All listed tests must pass. No other tests in the suite may be broken.

## Definition of Done

- All acceptance criteria have passing tests in `{TestClass}`.
- Full test suite still passes (`mvn test`).
- Judge session completed; no unresolved findings.
- ADR written if a significant decision was made.
- This README updated with Implementation Notes (below).

## Implementation Notes

> Filled in **after** the story is complete. One short paragraph plus a bullet list of files added/modified. This is what the PR reviewer reads.

(empty until story is done)
