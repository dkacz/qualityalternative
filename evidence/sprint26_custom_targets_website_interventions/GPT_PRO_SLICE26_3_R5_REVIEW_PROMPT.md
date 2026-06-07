# GPT Pro Review Request - Sprint 26 Slice 26.3 R5

You are reviewing only Sprint 26 Slice 26.3 R5: Chrome Verified-Host Website Intervention.

Use only the attached bundle as evidence. If a claim is not proven by shipped files, mark it as `BUNDLE GAP`.

## Primary Files To Read First

1. `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
2. `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R5_EVIDENCE.md`
3. `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R4_REVIEW.md`
4. `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R5_REVIEW_BUNDLE_MANIFEST.md`

## Review Scope

R4 returned `SCORE 9/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`, with no blockers. R5 is a package-completeness re-review:

- all `app/src` source and test files are now shipped;
- full R4 unit XML and lint evidence are shipped;
- R2 Chrome/visual evidence is reused because R3/R4/R5 did not change UI/accessibility matching behavior after R2.

Please determine whether Slice 26.3 now earns `SCORE 10/10` and can be committed.

Review these sections:

- R4 / R3 / R2 blocker recheck
- Chrome verified-host adapter
- Website rule matching
- Stale / unreadable state safety
- Soft / Firm / Open Anyway behavior
- Privacy / analytics
- Test / evidence
- Bundle gaps
- Package hygiene

## Required Output Format

Return all sections exactly:

SCORE: n/10

VERDICT: PASS or FAIL

VISUAL REVIEW: PASS or FAIL

BLOCKERS

R4 / R3 / R2 BLOCKER RECHECK

CHROME VERIFIED-HOST ADAPTER

WEBSITE RULE MATCHING

STALE / UNREADABLE STATE SAFETY

SOFT / FIRM / OPEN ANYWAY BEHAVIOR

PRIVACY / ANALYTICS

TEST / EVIDENCE

BUNDLE GAPS

PACKAGE HYGIENE

Only return `PASS` if the bundle proves the implementation is safe enough to commit Slice 26.3 and continue to Slice 26.4. If you find any release-blocking issue, return `FAIL` with the minimum fix.
