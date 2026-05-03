# ADR-{NNNN}: {Decision Title}

- **Date:** YYYY-MM-DD
- **Status:** Proposed | Accepted | Superseded by ADR-{NNNN} | Deprecated
- **Feature:** `{feature-name}`
- **Story:** `{FEAT}-{NN}` (or "feature-wide")
- **Authors:** {names}

## Context

What situation required this decision? What were the constraints and forces?

Describe the problem **as it existed before** the decision. Do not summarize the solution here. Include facts that future engineers and future AI sessions need to evaluate whether the decision still holds: stack constraints, performance limits, security requirements, integration boundaries, prior incidents.

## Decision

We will {specific action}. State it directly, in one or two sentences.

## Alternatives Considered

### {Alternative 1 Name}
What it is. Why it was rejected. Be specific about the failure mode — not just "it was worse" but **how** it failed (e.g. "the `@PrePersist` callback fires during test factory creation, which broke 14 unrelated tests").

### {Alternative 2 Name}
What it is. Why it was rejected.

### {Alternative N Name (the obvious one we rejected)}
If you rejected the obvious approach, document why explicitly. Without this, the next engineer (or AI session) will reach for it again.

## Consequences

### Positive
- {Specific benefit 1}
- {Specific benefit 2}

### Negative
- {Specific tradeoff or constraint this decision creates}
- {What becomes harder, slower, or more brittle}

### Neutral
- {Things that change but are neither good nor bad}

## Impact on AI Sessions

How does this decision constrain future AI sessions in this area? What text should future story READMEs and AI instruction blocks include when working in this part of the codebase?

> Example:
> "When working in any part of the codebase that sends outbound messages, include this in the AI instruction: 'All outbound messages go through `MessageDispatcher.dispatch()`. Never call an SMS provider directly. The opt-out check lives in `dispatch()` and is bypassed if you call the provider directly. See ADR-0002.'"

## References

- Related ADRs: {ADR-NNNN}
- Related stories: {FEAT}-{NN}
- External docs / RFCs / incident postmortems: {links or paths}
