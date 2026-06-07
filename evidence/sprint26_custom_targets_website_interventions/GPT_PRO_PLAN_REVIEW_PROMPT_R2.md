# GPT Pro Review Request R2 - Sprint 26 Custom Targets And Website Interventions

You are reviewing a corrected Android product sprint plan before implementation. Use only the attached bundle as your audit base. Read `PRD.md` first, then `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`, then the R1 review at `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_PLAN_REVIEW.md`, then `docs/LANE_STATUS.md` and `AGENTS.md`.

## Context

R1 scored `6/10` and failed because the plan had not yet patched PRD.md, under-specified browser URL detection, allowed unsafe path/full URL wording, lacked custom app safety exclusions, had insufficient privacy/analytics constraints, and did not fully specify Portable Profile import behavior.

R2 should focus on whether those blockers are actually fixed in the shipped files.

## Product Guardrail

Quality Alternative is a replacement-first Android app, not a generic blocker. Soft intervention remains default. Firm and Bedtime are opt-in. Bedtime can require emergency unlock but must preserve reading, meditation, and bounded alternatives.

Sprint 26 should add:

1. eligible custom installed-app targets outside standard suggestions,
2. supported-browser exact-domain and wildcard-subdomain website interventions,
3. truthful unsupported-browser status and whole-browser app fallback,
4. privacy-preserving local matching and analytics denylist,
5. visual and automated E2E evidence before APK release.

Sprint 26 must not add universal URL blocking, browser extensions, VPN/DNS/proxy filtering, browsing-history collection, open-web crawling, AI browsing categorization, parental-control scope, default hard-block behavior, or full URL/path rules.

## Review Criteria

Evaluate:

- Whether PRD.md now explicitly authorizes the new target classes without scope drift.
- Whether the corrected plan limits website matching to exact-domain and wildcard-subdomain rules.
- Whether browser support is realistic: Chrome-first verified-host only, no stale URL reuse, unsupported-browser fallback.
- Whether custom app eligibility safely excludes self, launchers, Settings/permissions, emergency/phone, installers, System UI, and system-critical packages.
- Whether Portable Profile import/export behavior is safe.
- Whether analytics/logging forbid raw URL, host, domain, path/query, page title, URL-bar text, non-match observations, browsing history, and domain-derived hashes.
- Whether the slice order is implementation-ready and demoable.
- Whether the future visual/E2E evidence plan is concrete enough for implementation reviews.
- Whether package hygiene is clean.

## Required Output Format

Return all sections below:

```
SCORE: x/10
VERDICT: PASS | FAIL
VISUAL REVIEW: PASS | FAIL | NOT APPLICABLE

BLOCKERS:
- ...

R1 BLOCKER RECHECK:
- ...

PRD / SCOPE FIT:
- ...

SLICE PLAN QUALITY:
- ...

ANDROID FEASIBILITY:
- ...

PRIVACY / ANALYTICS:
- ...

TEST AND VISUAL EVIDENCE PLAN:
- ...

BUNDLE GAPS:
- ...

PACKAGE HYGIENE:
- ...

REQUIRED PLAN CHANGES BEFORE IMPLEMENTATION:
- ...
```

Use `BUNDLE GAP` for claims that cannot be proven from shipped files. If you score below 10/10 or fail the verdict, make the required changes concrete enough for Codex to patch the sprint plan before implementation.
