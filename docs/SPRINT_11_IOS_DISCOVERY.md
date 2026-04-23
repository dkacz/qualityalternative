# Sprint 11: iOS Feasibility Discovery

Status: `in_progress`

## Goal

Decide whether Quality Alternative should start an iOS path now, and if so which path is realistic: full Screen Time / Family Controls, a lightweight Shortcuts-assisted prototype, or continued Android-only focus.

This sprint is discovery and decision work. It should not add iOS implementation code to the Android repository.

## PRD Mapping

- `Platform Scope and Constraints`: Android remains the MVP launch platform; iOS is explicitly not part of MVP launch.
- `iOS Constraints`: Screen Time may support a deeper system product, but entitlement access, shield UX, and routing constraints increase execution risk.
- `Platform Risks`: iOS parity pressure should not pull the product away from proving the Android wedge.

## Key Questions

- Can an iOS app reliably interrupt selected distracting apps without private APIs?
- What does Apple allow through Screen Time, Family Controls, DeviceActivity, ManagedSettings, and App Groups?
- What entitlement path is required, and what evidence or product framing improves approval odds?
- Can the shield UX route users into our replacement flow, or only display constrained shield actions?
- Is a Shortcuts-based prototype useful enough for tester learning despite weaker reliability?
- What parts of the Android product can be shared conceptually across platforms: content inventory, ranking logic, analytics taxonomy, and reader UX?
- What would make iOS worth building before more Android pilot data?

## Slice Sequence

### Slice 11.1: Platform Capability Research

Status: `implemented_pending_pro_review`

Deliverables:

- Document current Apple framework options: Family Controls, DeviceActivity, ManagedSettings, Screen Time API, Shortcuts/App Intents.
- Identify what each option can and cannot do for app-level interruption.
- Capture entitlement requirements and likely review constraints.

Validation:

- Use current Apple developer documentation as primary source.
- Include source links and date checked.

### Slice 11.2: UX and Flow Feasibility

Deliverables:

- Compare possible iOS flows against Android alpha behavior:
  - selected app detected
  - user sees pause/intervention
  - user chooses replacement
  - user can consciously continue
  - delay/suppression behavior
- Identify which flows are first-class, awkward, or impossible on iOS.
- Note user trust and permission-copy implications.

Validation:

- Produce a flow matrix with `works`, `limited`, `blocked`, or `unknown` status.

### Slice 11.3: Prototype Options and Cost

Deliverables:

- Define two possible prototype tracks:
  - `full_ios_spike`: native SwiftUI app using Screen Time stack, assuming entitlement path is plausible.
  - `lightweight_ios_spike`: Shortcuts/App Intents assisted test for interruption intent and replacement acceptance.
- Estimate implementation complexity, testing needs, policy risk, and expected learning value.

Validation:

- Each track must have a concrete 3-5 day spike plan and explicit non-goals.

### Slice 11.4: Decision Memo

Deliverables:

- `docs/IOS_DISCOVERY.md` with final recommendation:
  - `build_full_ios_spike`
  - `build_lightweight_ios_spike`
  - `defer_ios_until_android_pilot`
- State the decision criteria and what would change the recommendation.
- Add a proposed Sprint 12 only if the recommendation is to build a spike.

Validation:

- Pro review focused on platform correctness, policy risk, scope discipline, and whether the recommendation follows from the evidence.

## Acceptance Criteria

- No iOS implementation code is added in this sprint.
- Apple platform claims are sourced from current official documentation where possible.
- The sprint produces a clear decision memo, not an open-ended research dump.
- The recommendation explicitly accounts for entitlement risk, shield UX constraints, tester value, and opportunity cost versus Android pilot work.
- The recommendation does not promise iOS parity before the Android product loop is proven.

## Explicit Non-Goals

- No Swift/SwiftUI app implementation.
- No Apple developer account or entitlement application submission.
- No App Store listing work.
- No iOS design system buildout.
- No cross-platform rewrite.
- No backend or sync architecture changes.
- No changes to Android behavior.

## Pro Review Prompt Shape

Ask GPT Pro to review:

- whether Apple framework capabilities are accurately represented,
- whether entitlement and policy risks are understated,
- whether the proposed path preserves the Android-first product strategy,
- whether the final recommendation is decision-grade.

## Open Inputs

- Apple Developer account/team status.
- Whether the project can credibly apply for Family Controls entitlement soon.
- Whether a lightweight prototype is acceptable if it cannot replicate Android's full interception loop.
