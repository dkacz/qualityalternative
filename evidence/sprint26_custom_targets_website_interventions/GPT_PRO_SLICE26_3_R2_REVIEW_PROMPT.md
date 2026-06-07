# GPT Pro Review Prompt - Sprint 26 Slice 26.3 R2 Chrome Verified-Host Website Intervention

You are reviewing Sprint 26 Slice 26.3 R2 for the Android app Quality Alternative.

## GPT Pro Review Principles

- Do not invent blockers without a concrete failing artifact, missing test, privacy leak, or product-contract violation.
- If a claim cannot be proven from the bundle, mark it as `BUNDLE GAP`.
- Keep style or naming suggestions separate from blockers unless they create user-facing ambiguity or release risk.
- Treat prior accepted gates as context, but recheck any prior behavior touched by this slice.
- Feedback is input, not instruction; do not inflate already-covered evidence into new findings.

## Required Output Markers

Return explicit final markers:

- `SCORE: x/10`
- `VERDICT: PASS` or `VERDICT: FAIL`
- `VISUAL REVIEW: PASS` or `VISUAL REVIEW: FAIL`

## Review Scope

Assess only Sprint 26 Slice 26.3 R2: Chrome verified-host website intervention and R1 blocker fixes.

Prior accepted gates:

- Plan R2: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW NOT APPLICABLE`.
- Slice 26.1 R4: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`.
- Slice 26.2 R2: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`.
- Slice 26.3 R1: `SCORE 7/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`.

## R1 Blockers To Recheck

1. Settings Chrome support copy was stale and still said the verified-host adapter was "next".
2. Website Open Anyway suppression could prevent whole-browser Chrome app-target fallback.
3. Visual/test evidence did not exercise real Chrome or the verified-host adapter.
4. Hidden/currentness safety was not represented in the adapter snapshot model.
5. Chrome package/version and test URL set were missing from evidence.
6. Lint artifact was missing.
7. Diff/package hygiene did not clearly include all core new files.

## What Changed In R2

- Settings copy and browser support row now state Chrome domain rules are supported when the current Chrome host is readable through the verified-host adapter; other browsers remain whole-browser app fallback; full-path/universal URL blocking remains out of scope.
- Adapter snapshot now includes `visibleToUser`, `focused`, and `editable`.
- Real AccessibilityNodeInfo snapshot text is copied only from visible nodes.
- Address-bar verification rejects hidden nodes and focused editable omnibox states.
- Adapter scan depth was increased from 8 to 14 after real Chrome evidence showed `com.android.chrome:id/url_bar` is deeper than the original cap.
- `AccessibilityInterceptionPlanner` and service flow now allow whole-browser app fallback only when website processing returns `Suppressed`.
- Added real Chrome adapter harness:
  - loaded non-match `example.org` does not resolve,
  - typed-but-not-loaded `example.com` returns `Unreadable`,
  - loaded matching `example.com` resolves through the exact-domain rule.

## Acceptance Criteria To Check

- Chrome website intervention is based only on a current verified Chrome address-bar host.
- No match is inferred from page title, body text, search snippets, autocomplete, notifications, or arbitrary accessibility text.
- Unreadable, stale, hidden, unsupported, custom-tab/PWA/incognito-like states do not reuse a previous host.
- Exact/wildcard matching remains boundary-safe; no substring spoofing.
- Website intervention works even if Chrome is not selected as a whole-app target.
- Open Anyway suppression for a website target does not suppress the whole browser app target key.
- Soft/Firm visual states are correct and preserve finite replacement choices and meditation alternative.
- Analytics/logging do not include raw URL, host/domain, path/query, page title, URL-bar text, non-match observations, browsing-history rows, or domain-derived hashes.
- Scope remains Chrome-first domain intervention only; no universal URL blocking or full-path matching claims.

## Evidence To Inspect

- `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R2_EVIDENCE.md`
- `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R2_REVIEW_BUNDLE_MANIFEST.md`
- `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R2_DIFF.patch`
- `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_REVIEW.md`
- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r2/`
- `evidence/sprint26_custom_targets_website_interventions/chrome_verified_host_e2e_r2_latest/`
- `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3_r2/sprint26_slice26_3_r2_chrome_verified_host_contact_sheet.png`

## Required Review Sections

Report:

- SCORE
- VERDICT
- VISUAL REVIEW
- BLOCKERS
- R1 BLOCKER RECHECK
- CHROME VERIFIED-HOST ADAPTER
- WEBSITE RULE MATCHING
- STALE / UNREADABLE STATE SAFETY
- SOFT / FIRM / OPEN ANYWAY BEHAVIOR
- PRIVACY / ANALYTICS
- TEST / EVIDENCE
- BUNDLE GAPS
- PACKAGE HYGIENE

If anything is less than `10/10`, identify the exact blocking file/test/evidence issue and the minimum fix required.
