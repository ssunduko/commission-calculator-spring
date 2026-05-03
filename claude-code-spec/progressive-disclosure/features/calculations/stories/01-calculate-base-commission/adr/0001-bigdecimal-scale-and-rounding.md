# ADR-0001: All Monetary BigDecimal Math Uses scale=2 and RoundingMode.HALF_UP

- **Date:** 2026-05-01
- **Status:** Accepted
- **Feature:** `calculations`
- **Story:** `COMM-01` (and feature-wide for any monetary computation)
- **Authors:** Engineering team

## Context

The commission calculation service multiplies and divides `BigDecimal` values: `deal.value * rule.rate / 100`, plus additive bonuses and multiplicative accelerators in later stories. Three forces converge here:

1. `BigDecimal.divide(...)` without an explicit scale and rounding mode throws `ArithmeticException` for non-terminating decimals (e.g. `1 / 3`). This is a runtime failure waiting for the wrong inputs.
2. `BigDecimal.multiply(...)` returns a value whose scale is the sum of the operands' scales. A deal value of `10000.00` (scale 2) multiplied by a rate of `3.333` (scale 3) yields `33330.00000` (scale 5). Carrying scale 5 through subsequent operations bloats database storage and breaks equality checks.
3. The downstream `commission_calculations.base_commission` column is `NUMERIC(19,2)`. Any value with scale > 2 is rounded by the JDBC driver implicitly, with a rounding mode that varies by driver. We have already had one defect where the JVM's banker's rounding produced a different result than PostgreSQL's `HALF_UP`, by one cent, on a customer's payout.

We need a single, codebase-wide convention so every monetary calculation produces the same answer regardless of who wrote it or which surface (REST, MCP) invoked it.

## Decision

Every monetary `BigDecimal` operation in the `calculations` feature (and any future feature touching money) ends with `.setScale(2, RoundingMode.HALF_UP)` immediately after the operation that could change scale (multiply, divide). The `2` matches the database column scale; `HALF_UP` matches finance and PostgreSQL's default.

```java
import java.math.BigDecimal;
import java.math.RoundingMode;

BigDecimal base = deal.getValue()
        .multiply(rule.getRate())
        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
// or, when applying an accelerator factor:
BigDecimal accelerated = base.multiply(factor).setScale(2, RoundingMode.HALF_UP);
```

Response DTOs return `BigDecimal` values that already have scale 2; controllers and MCP tools do not re-scale.

## Alternatives Considered

### `MathContext` with `DECIMAL64` precision
What it is: pass `MathContext.DECIMAL64` to every operation, letting precision (significant digits) drive truncation.
Why rejected: precision-based rounding is inappropriate for fixed-fraction-digit money. `DECIMAL64` would happily produce `0.001234` cents, which then rounds again at the column boundary with a different mode.

### `RoundingMode.HALF_EVEN` (banker's rounding)
What it is: round-half-to-even, the JVM default for `BigDecimal.setScale(scale)` without an explicit mode.
Why rejected: industry-standard finance arithmetic uses `HALF_UP`, and that is also what PostgreSQL's `numeric` type uses by default. Introducing `HALF_EVEN` at the JVM layer creates one-cent drift between Java-computed values and any SQL-side recomputation (e.g. analytics). We have already seen this defect once.

### Use `double` and round at the boundary
Rejected outright. `double` cannot represent `0.10` exactly; commission math on `double` accumulates error across bonus and accelerator chains. Banned for any monetary value in this codebase.

### Store the rate as a fraction (0.05) instead of percent (5.0) to avoid the `/100`
What it is: change the data model so `rule.rate` is `0.05` for 5%, removing one division.
Why rejected: existing data and the existing UI display percentages. Migrating the data model is out of scope; a hidden division is cheap, while a hidden representation change is a coordinated cross-feature change with rollback risk.

## Consequences

### Positive
- Single, mechanical rule. Reviewers and judges can spot violations easily ("multiply or divide without `.setScale(2, HALF_UP)` is wrong").
- No `ArithmeticException` from non-terminating decimals; explicit scale + rounding eliminates the failure mode.
- Java-computed values match PostgreSQL-computed values at the cent. No drift.
- The `commission_calculations.base_commission`, `gross_commission`, and `net_commission` columns can stay `NUMERIC(19,2)` with no implicit rounding by the JDBC driver.

### Negative
- Every monetary operation has a verbose tail (`.setScale(2, RoundingMode.HALF_UP)`). A small ergonomic cost.
- A future product requirement for sub-cent precision (e.g. tenths-of-a-cent for high-volume micro-transactions) would require revisiting this ADR. Unlikely, but the constraint is explicit.

### Neutral
- All existing tests assert via `isEqualByComparingTo("...")` to be insensitive to internal scale; this convention does not change them.

## Impact on AI Sessions

When working on any feature that performs monetary arithmetic, include this in the AI instruction block:

> "All monetary `BigDecimal` math uses `setScale(2, RoundingMode.HALF_UP)` immediately after any multiply or divide, per ADR-0001. Do not use `MathContext`, `HALF_EVEN`, or `double`. Response DTO fields are already at scale 2; controllers and MCP tools do not re-scale."

Judge sessions should treat any unscaled multiply or divide on a `BigDecimal` field whose name contains `commission`, `value`, `amount`, `rate`, `bonus`, `tier`, `payout`, or `tax` as a **Fail** under the Persistence and Query Performance criterion (sub-criterion: precision).

## References

- Story: COMM-01 — Calculate base commission from deal value and plan's STANDARD rule
- Java `BigDecimal` Javadoc — non-terminating decimal expansion
- PostgreSQL `numeric` type — default rounding mode is `ROUND_HALF_UP`
