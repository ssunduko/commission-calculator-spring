# Story COMM-01: Calculate Base Commission from Deal Value and Plan's STANDARD Rule

> Layer 2 of the progressive-disclosure system. Primary AI context document for any session within this story.

**Feature:** [`calculations`](../../README.md)

## What This Story Delivers

After this story, the system can calculate the **base** commission for a single deal using the first `STANDARD` rule of a commission plan, persist a `CommissionCalculation` row with `status = CALCULATED`, and return it from the service layer. The REST controller and MCP tool wrappers are deferred to COMM-02 and COMM-03; this story stops at the service. Bonuses and accelerators are deferred to COMM-05.

A consumer of `CommissionCalculationService.calculate(...)` (which is what COMM-02's controller and COMM-03's MCP tool will both call) gets a fully formed, persisted, response-shaped result.

## Vertical Layers Touched in This Story

```
Database     New table commission_calculations (auto-DDL from @Entity)
Domain       New CommissionCalculation entity, new CommissionStatus enum
Repository   New CommissionCalculationRepository
DTOs         New CalculateCommissionRequest record + validate()
             New CommissionCalculationResponse record + static from(...)
Service      New CommissionCalculationService.calculate(CalculateCommissionRequest)
Controller   none — added in COMM-02
MCP Tool     none — added in COMM-03
```

The service is built to produce the exact `CommissionCalculationResponse` shape defined in the feature README. Do not retrofit later when COMM-02 wires the controller.

## Acceptance Criteria

Each maps to a `@Test` in `tests/CommissionCalculationServiceTest.java`.

1. **Happy path:** Given a deal with `value = 10000.00` and a plan whose first STANDARD rule has `rate = 5.0`, `calculate(...)` returns a response with `baseCommission = 500.00`, `grossCommission = 500.00`, `netCommission = 500.00`, `status = CALCULATED`, `calculationDate = today`.
2. **Persistence:** After `calculate(...)` returns, exactly one row is present in `commission_calculations` with the response's `id`, and `findById(id)` returns it.
3. **Append-only:** Calling `calculate(...)` twice for the same `dealId` + `planId` produces two distinct persisted rows with different `id`s.
4. **Deal not found:** `calculate(...)` with a `dealId` that does not exist throws `ResourceNotFoundException` with resource `"deal"`. No row is persisted.
5. **Plan not found:** `calculate(...)` with a `planId` that does not exist throws `ResourceNotFoundException` with resource `"plan"`. No row is persisted.
6. **Validation — null dealId:** `CalculateCommissionRequest.validate()` throws `ValidationException` when `dealId` is null or blank.
7. **Validation — null planId:** `CalculateCommissionRequest.validate()` throws `ValidationException` when `planId` is null or blank.
8. **Plan has no STANDARD rule:** `calculate(...)` throws `ValidationException` with a message naming the plan id. No row is persisted.
9. **Decimal precision:** Given `deal.value = 10000.00` and `rule.rate = 3.333`, the response's `baseCommission` equals `333.30` (scale 2, `RoundingMode.HALF_UP`).
10. **Rule priority:** Given a plan with two STANDARD rules — `priority = 1, rate = 5.0` and `priority = 2, rate = 10.0` — `calculate(...)` uses the rule with the lowest priority value (`5.0`), producing `baseCommission = 500.00` for a `10000.00` deal.

## Files Involved

### New files (create these)

- `src/main/java/com/chapman/edu/commissions/architecture/verticalslice/domain/CommissionCalculation.java` — JPA `@Entity`, UUID PK, fields per feature README, Lombok `@Data` + `@NoArgsConstructor`, plus a constructor that takes essential fields.
- `src/main/java/com/chapman/edu/commissions/architecture/verticalslice/domain/CommissionStatus.java` — enum with `CALCULATED`, `APPROVED`, `PAID`, `DISPUTED`, `ADJUSTED`, `CANCELLED`. Display name + `toString()`.
- `src/main/java/com/chapman/edu/commissions/architecture/verticalslice/features/calculations/CommissionCalculationRepository.java` — `extends JpaRepository<CommissionCalculation, String>`. No custom query methods needed for this story.
- `src/main/java/com/chapman/edu/commissions/architecture/verticalslice/features/calculations/CalculateCommissionRequest.java` — record `(String dealId, String planId)` with `public void validate()` throwing `ValidationException`.
- `src/main/java/com/chapman/edu/commissions/architecture/verticalslice/features/calculations/CommissionCalculationResponse.java` — record matching the shape in the feature README, with `public static CommissionCalculationResponse from(CommissionCalculation entity)`.
- `src/main/java/com/chapman/edu/commissions/architecture/verticalslice/features/calculations/CommissionCalculationService.java` — `@Service`, constructor-injected `DealRepository`, `CommissionPlanRepository`, `CommissionCalculationRepository`. Single public method: `CommissionCalculationResponse calculate(CalculateCommissionRequest request)`.

### Modified files

None. This story is purely additive.

### Do not touch

- `features/deals/**` — read via `DealRepository` only
- `features/plans/**` — read via `CommissionPlanRepository` only
- `infrastructure/mcp/McpCommissionTools.java` — owned by COMM-03
- `infrastructure/data/DataInitializer.java` — sample data, leave alone
- `infrastructure/exceptions/` — `ResourceNotFoundException` and `ValidationException` already exist; do not redefine

## Existing Code to Understand

```java
// src/main/java/com/chapman/edu/commissions/architecture/verticalslice/features/deals/DealRepository.java
public interface DealRepository extends JpaRepository<Deal, String> {
    List<Deal> findBySalesRepId(String salesRepId);
    List<Deal> findByStatus(DealStatus status);
}
```

```java
// src/main/java/com/chapman/edu/commissions/architecture/verticalslice/domain/Deal.java -- relevant getters
public class Deal {
    public String getId() { ... }
    public String getTitle() { ... }
    public BigDecimal getValue() { ... }       // column "deal_value"
    public DealStatus getStatus() { ... }
    public String getSalesRepId() { ... }
    // others omitted
}
```

```java
// src/main/java/com/chapman/edu/commissions/architecture/verticalslice/features/plans/CommissionPlanRepository.java
public interface CommissionPlanRepository extends JpaRepository<CommissionPlan, String> {
    List<CommissionPlan> findByStatus(PlanStatus status);
}
```

```java
// src/main/java/com/chapman/edu/commissions/architecture/verticalslice/domain/CommissionPlan.java -- relevant accessors
public class CommissionPlan {
    public String getId() { ... }
    public String getName() { ... }
    public List<CommissionRule> getRules() { ... }
}

// CommissionRule (in domain/) -- relevant fields
public class CommissionRule {
    public String getName() { ... }
    public BigDecimal getRate() { ... }        // percentage points, e.g. 5.0 = 5%
    public RuleType getType() { ... }          // STANDARD, ACCELERATOR, BONUS
    public Integer getPriority() { ... }       // lower = higher priority
}
```

```java
// src/main/java/com/chapman/edu/commissions/architecture/verticalslice/infrastructure/exceptions/ResourceNotFoundException.java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, String id) {
        super("%s not found: %s".formatted(resource, id));
    }
}
```

```java
// src/main/java/com/chapman/edu/commissions/architecture/verticalslice/infrastructure/exceptions/ValidationException.java
public class ValidationException extends RuntimeException {
    public ValidationException(String message) { super(message); }
}
```

## Hard Constraints

- Do not modify the test file.
- Do not modify any file outside the "New files" list above.
- Do not introduce new Maven dependencies.
- All monetary `BigDecimal` arithmetic uses `setScale(2, RoundingMode.HALF_UP)` immediately after multiply/divide. See ADR-0001.
- Constructor injection only on the service. No `@Autowired` field.
- DTOs are Java `record` types. `CalculateCommissionRequest.validate()` throws `ValidationException`. `CommissionCalculationResponse.from(...)` is a `public static` factory.
- `CommissionCalculationService.calculate(...)` is annotated `@Transactional` (the multi-step write-after-read pattern means the read must observe a consistent state).
- Use SLF4J. Log one INFO line at the start of `calculate(...)` (`"commission_calculation_started dealId={} planId={}"`) and one at the end with the resulting id.
- The first `STANDARD` rule means: filter `plan.getRules()` for `type == STANDARD`, sort by `priority` ascending, take the first. If the filtered list is empty, throw `ValidationException`.

## Tests That Must Pass

- File: [`tests/CommissionCalculationServiceTest.java`](tests/CommissionCalculationServiceTest.java)
- Run: `mvn test -Dtest=CommissionCalculationServiceTest`

All 10 acceptance criteria must pass. The full project test suite must continue to pass.

## Definition of Done

- All `@Test` methods in `CommissionCalculationServiceTest` pass.
- Full suite (`mvn test`) passes; no pre-existing tests broken (the known pre-existing failure in `CommissionCalculatorApplicationTests.contextLoads` is allowed).
- Judge session run with the prompt in [`templates/JUDGE_PROMPT.md`](../../../../templates/JUDGE_PROMPT.md) — no unresolved findings.
- ADR-0001 (BigDecimal scale + rounding) accepted; see `adr/0001-bigdecimal-scale-and-rounding.md`.
- This README's "Implementation Notes" filled in.

## Implementation Notes

> Filled in **after** the story is complete.

(empty until story is done)
