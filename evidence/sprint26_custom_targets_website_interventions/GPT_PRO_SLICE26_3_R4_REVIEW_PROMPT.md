# GPT Pro Review Request - Sprint 26 Slice 26.3 R4

You are reviewing only Sprint 26 Slice 26.3 R4: Chrome Verified-Host Website Intervention.

Use only the attached bundle as evidence. If a claim is not proven by shipped files, mark it as `BUNDLE GAP`. Please do not infer from prior conversations.

## Primary Files To Read First

1. `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
2. `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R4_EVIDENCE.md`
3. `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R3_REVIEW.md`
4. `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R4_DIFF.patch`
5. `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R4_REVIEW_BUNDLE_MANIFEST.md`

## Review Scope

R3 already returned `SCORE 9/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`, with no blockers. R4 is a package/evidence completeness re-review to determine whether Slice 26.3 can receive `SCORE 10/10` and be committed.

Please review:

- R3 privacy blocker closure
- Chrome verified-host adapter
- Website rule matching
- Stale / unreadable state safety
- Soft / Firm / Open Anyway behavior
- Privacy / analytics, especially replacement URL leakage
- Test / evidence adequacy
- Bundle gaps
- Package hygiene

## R4 Additions Since R3

- Full `testDebugUnitTest` XML directory is included, including `WebsiteRuleNormalizerTest`.
- Standalone `MainActivity.kt` is included.
- Broader production analytics and repository source files are included:
  - analytics tracker contracts and Room analytics storage,
  - local analytics DAO/entity,
  - content/domain models,
  - production user-link repository and local user-link DAO/entity,
  - user-link validator,
  - database/application wiring.
- R2 Chrome visual evidence is still reused because R3/R4 changed analytics metadata and bundle completeness only.

## Required Output Format

Return all sections exactly:

SCORE: n/10

VERDICT: PASS or FAIL

VISUAL REVIEW: PASS or FAIL

BLOCKERS

R3 / R2 BLOCKER RECHECK

CHROME VERIFIED-HOST ADAPTER

WEBSITE RULE MATCHING

STALE / UNREADABLE STATE SAFETY

SOFT / FIRM / OPEN ANYWAY BEHAVIOR

PRIVACY / ANALYTICS

TEST / EVIDENCE

BUNDLE GAPS

PACKAGE HYGIENE

Only return `PASS` if the bundle proves the implementation is safe enough to commit Slice 26.3 and continue to Slice 26.4. If you find any release-blocking issue, return `FAIL` with the minimum fix.
