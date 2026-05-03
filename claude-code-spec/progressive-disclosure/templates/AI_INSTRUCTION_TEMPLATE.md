# AI Instruction Block — Per-Session Template

Open every implementation session with this block. Do not skip any section. If a section would be empty, the unit is not yet scoped — go fix the story README first.

---

## TASK

One sentence. What are we building in this session? One unit only. Not a whole story.

> Example: "Create the `OptOut` JPA entity and `OptOutRepository`."

## CONTEXT

Pull the minimum from the feature and story READMEs. Paste **only** the relevant sections, not the whole document.

**Feature vertical slice (from `features/{feature}/README.md`):**
{Paste the "Vertical Slice: Layers This Feature Touches" section, plus the API Shape and MCP Tool Shape if the unit interacts with them.}

**Story context (from `stories/{NN-slug}/README.md`):**
{Paste "What This Story Delivers" and the relevant "Vertical Layers Touched" rows for this unit.}

**Existing methods the new code must integrate with:**
{Paste each method excerpt with file path and line numbers. Strip Javadoc, blank lines, and irrelevant imports.}

```java
// src/main/java/.../{ExistingFile}.java -- lines NN to MM
public {ReturnType} {methodName}({Params}) {
    // current implementation
}
```

## FAILING TESTS

Paste only the `@Nested` block(s) from the test class that cover the current unit. Not the whole class.

```java
@Nested
@DisplayName("{unit description}")
class {UnitTestNest} {

    @Test
    @DisplayName("...")
    void test1() {
        // arrange
        // act
        // assert
    }
}
```

## SUCCESS CONDITION

Single command and observable outcome.

```
mvn test -Dtest={TestClass}#{nestedClass}
```

All listed tests pass. No other tests in the suite are broken.

## CONSTRAINTS

- Do not modify the test file.
- Create or modify only these files: {explicit list}
- Do not add new Maven dependencies.
- Use constructor injection. No `@Autowired` fields.
- DTOs are Java `record` types. Requests have a `validate()` method. Responses have a `static from(Entity)` factory.
- All data access through Spring Data JPA repositories.
- {Story-specific constraint}

## WHAT I ALREADY TRIED (only if not the first session on this unit)

If a previous attempt produced an incorrect result, describe what it produced and why it was wrong. This prevents the AI from repeating the same approach.

> Example: "Previous attempt placed the opt-out check inside an `@PrePersist` lifecycle callback on the `Message` entity. This fired during test factory creation and broke unrelated tests. Correct location is `MessageDispatcher.dispatch()`. See ADR-0002."

---

## Compact Summary (for the next session)

At the end of this session, ask the AI to produce a <200 word summary covering:
- Files created or modified, one sentence each
- Tests now passing
- Decisions made and why
- The next unit of work

Save the summary. Close the session. Open the next unit's session with the summary as the first message.
