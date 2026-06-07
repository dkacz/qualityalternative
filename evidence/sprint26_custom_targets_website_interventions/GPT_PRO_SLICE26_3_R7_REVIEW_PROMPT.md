# GPT Pro Review Request - Sprint 26 Slice 26.3 R7

You are reviewing only Sprint 26 Slice 26.3 R7: Chrome Verified-Host Website Intervention.

Use only the attached bundle as evidence. If a claim is not proven by shipped files, mark it as `BUNDLE GAP`.

## Primary Files To Read First

1. `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
2. `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R7_EVIDENCE.md`
3. `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R6_REVIEW.md`
4. `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R7_REVIEW_BUNDLE_MANIFEST.md`

## Review Scope

R6 returned `SCORE 9/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`.

The R6 blocker was:

- the adapter snapshot path did not carry root/node package identity,
- the bundle did not prove stale active-window/package-mismatch safety at the same evidentiary level as the positive live Chrome service path.

R7 fixes that by:

- adding `packageName` to `BrowserNodeSnapshot`,
- populating it from `AccessibilityNodeInfo.packageName`,
- requiring the root and address-bar node packages to match `com.android.chrome`,
- adding unit and connected negative evidence for package-mismatched stale root/address-node snapshots,
- rerunning full unit/lint, connected Chrome/visual tests, and external live-service E2E after the package-authentication change.

Please decide whether Slice 26.3 now earns `SCORE 10/10` and can be committed before continuing to Slice 26.4.

Review these sections:

- R6 blocker recheck
- Chrome verified-host adapter
- Website rule matching
- Stale / unreadable / package-mismatch safety
- Soft / Firm / Open Anyway behavior
- Privacy / analytics
- Test / evidence
- Visual review
- Bundle gaps
- Package hygiene

## Required Output Format

Return all sections exactly:

SCORE: n/10

VERDICT: PASS or FAIL

VISUAL REVIEW: PASS or FAIL

BLOCKERS

R6 BLOCKER RECHECK

CHROME VERIFIED-HOST ADAPTER

WEBSITE RULE MATCHING

STALE / UNREADABLE / PACKAGE-MISMATCH SAFETY

SOFT / FIRM / OPEN ANYWAY BEHAVIOR

PRIVACY / ANALYTICS

TEST / EVIDENCE

VISUAL REVIEW

BUNDLE GAPS

PACKAGE HYGIENE

Only return `SCORE 10/10`, `VERDICT PASS`, and `VISUAL REVIEW PASS` if the bundle proves the implementation is safe enough to commit Slice 26.3 and continue to Slice 26.4. If you find any release-blocking issue or score gap, return the minimum fix needed.
