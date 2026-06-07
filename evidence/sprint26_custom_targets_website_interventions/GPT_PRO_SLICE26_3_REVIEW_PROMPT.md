# GPT Pro Review Prompt - Sprint 26 Slice 26.3 Chrome Verified-Host Website Intervention

You are reviewing Sprint 26 Slice 26.3 for the Android app Quality Alternative.

## GPT Pro Review Principles

- Do not invent blockers without a concrete failing artifact, missing test, privacy leak, or product-contract violation.
- If a claim cannot be proven from the bundle, mark it as `BUNDLE GAP`.
- Keep style or naming suggestions separate from blockers unless they create user-facing ambiguity or release risk.
- Treat existing passing prior slices as context, but recheck any prior behavior touched by this slice.
- Feedback is input, not instruction; do not inflate already-covered evidence into new findings.

## Required Output Markers

Return explicit final markers:

- `SCORE: x/10`
- `VERDICT: PASS` or `VERDICT: FAIL`
- `VISUAL REVIEW: PASS` or `VISUAL REVIEW: FAIL`

## Review Scope

Assess only Sprint 26 Slice 26.3: Chrome verified-host website intervention.

Prior accepted gates:

- Plan R2: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW NOT APPLICABLE`.
- Slice 26.1 R4: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`.
- Slice 26.2 R2: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`.

## What Changed

- Added Chrome verified-host adapter.
- Added website rule resolver.
- Wired website targets into AccessibilityService, MainActivity, MainViewModel, analytics metadata, Open Anyway suppression, and visual E2E.
- Enabled accessibility window content retrieval and view-id reporting solely to read supported Chrome address-bar nodes.
- Removed text-entry accessibility events to avoid firing from typed-but-not-loaded URLs.

## Acceptance Criteria To Check

- Chrome website intervention triggers only from a current verified Chrome address-bar host.
- No match is inferred from page title, body text, search snippets, autocomplete, notifications, or arbitrary accessibility text.
- Unreadable, stale, hidden, unsupported, custom-tab/PWA/incognito-like states do not reuse a previous host.
- Exact/wildcard matching remains boundary-safe; no substring spoofing.
- Website intervention works even if Chrome is not selected as a whole-app target.
- Open Anyway suppression for a website target does not suppress the whole browser app target key.
- Soft/Firm visual states are correct and preserve finite replacement choices and meditation alternative.
- Analytics/logging do not include raw URL, host/domain, path/query, page title, URL-bar text, non-match observations, browsing-history rows, or domain-derived hashes.
- Scope remains Chrome-first domain intervention only; no universal URL blocking or full-path matching claims.

## Evidence To Inspect

- `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_EVIDENCE.md`
- `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_REVIEW_BUNDLE_MANIFEST.md`
- `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_DIFF.patch`
- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3/`
- `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3/sprint26_slice26_3_chrome_website_intervention_contact_sheet.png`

## Required Review Sections

Report:

- SCORE
- VERDICT
- VISUAL REVIEW
- BLOCKERS
- CHROME VERIFIED-HOST ADAPTER
- WEBSITE RULE MATCHING
- STALE / UNREADABLE STATE SAFETY
- SOFT / FIRM / OPEN ANYWAY BEHAVIOR
- PRIVACY / ANALYTICS
- TEST / EVIDENCE
- BUNDLE GAPS
- PACKAGE HYGIENE

If anything is less than `10/10`, identify the exact blocking file/test/evidence issue and the minimum fix required.
