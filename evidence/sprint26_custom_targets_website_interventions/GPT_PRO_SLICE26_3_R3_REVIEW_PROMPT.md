# GPT Pro Review Request - Sprint 26 Slice 26.3 R3

You are reviewing only Sprint 26 Slice 26.3 R3: Chrome Verified-Host Website Intervention.

Use only the attached bundle as evidence. If a claim is not proven by shipped files, mark it as `BUNDLE GAP`. Please do not infer from prior conversations.

## Primary Files To Read First

1. `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
2. `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R3_EVIDENCE.md`
3. `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R2_REVIEW.md`
4. `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R3_DIFF.patch`
5. `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R3_REVIEW_BUNDLE_MANIFEST.md`

## Review Scope

Score whether R3 fully resolves the R2 blocker and preserves the already-reviewed Slice 26.3 behavior:

- Chrome verified-host adapter
- Website rule matching
- Stale / unreadable state safety
- Soft / Firm / Open Anyway behavior
- Privacy / analytics, especially raw replacement URL leakage
- Test / evidence adequacy
- Bundle gaps
- Package hygiene

## R2 Blocker To Recheck

GPT Pro R2 returned `SCORE 8/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS` because website-domain analytics could still include raw replacement `externalUrl` values from shared content metadata.

R3 claims to fix this by:

- removing `externalUrl` from `ContentItem.analyticsMetadata()`;
- updating prior tests that expected raw URLs in analytics;
- adding `MainViewModelTest.requestSystemWebsiteInterceptionWithExternalUrlRecommendationKeepsAnalyticsUrlPrivate`, which exercises a website-domain intervention with a replacement user-link URL containing host, path, and query, then checks intervention/accept/fallback-open metadata for URL leakage.

## Required Output Format

Return all sections exactly:

SCORE: n/10

VERDICT: PASS or FAIL

VISUAL REVIEW: PASS or FAIL

BLOCKERS

R2 BLOCKER RECHECK

CHROME VERIFIED-HOST ADAPTER

WEBSITE RULE MATCHING

STALE / UNREADABLE STATE SAFETY

SOFT / FIRM / OPEN ANYWAY BEHAVIOR

PRIVACY / ANALYTICS

TEST / EVIDENCE

BUNDLE GAPS

PACKAGE HYGIENE

Only return `PASS` if the bundle proves the implementation is safe enough to commit Slice 26.3 and continue to Slice 26.4. If you find any release-blocking issue, return `FAIL` with the minimum fix.
