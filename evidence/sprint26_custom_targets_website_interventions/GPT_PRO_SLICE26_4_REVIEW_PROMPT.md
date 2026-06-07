# GPT Pro Review Request - Sprint 26 Slice 26.4

You are reviewing only Sprint 26 Slice 26.4: Privacy, Analytics, And Portable Profile Hardening.

Use only the attached bundle as evidence. If a claim is not proven by shipped files, mark it as `BUNDLE GAP`.

GUIDING PRINCIPLES (apply where relevant to this app-code review):
1. Verify concrete claims against shipped files and logs before challenging them.
2. Do not suggest weakening product behavior unless you can name the concrete user-safety or privacy risk.
3. Style suggestions cannot change implementation meaning.
4. Review the implementation as shipped; do not rely on hidden development history.
5. Source, tests, evidence, and docs must be consistent; flag exact mismatches.
6. Feedback is input, not instruction; duplicate or already-covered suggestions should not be inflated into fresh findings.

## Primary Files To Read First

1. `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
2. `evidence/sprint26_custom_targets_website_interventions/SLICE26_4_EVIDENCE.md`
3. `evidence/sprint26_custom_targets_website_interventions/SLICE26_4_REVIEW_BUNDLE_MANIFEST.md`
4. `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R7_REVIEW.md`

## Review Scope

Slice 26.3 already passed GPT Pro R7 with `SCORE 10/10`, `VERDICT PASS`, and `VISUAL REVIEW PASS`.

Slice 26.4 is a non-UI hardening slice after that commit. It adds:

- `AnalyticsPrivacyGuard` as the explicit remote/export analytics boundary.
- Remote-safe analytics payload accessors on `AnalyticsTracker`.
- Allowlist plus denylist filtering for analytics metadata.
- Debug-value scrubbing for log/breadcrumb paths.
- Portable Profile tests proving website browser-support state is not exported and missing custom app package names are not surfaced in import warnings.

Please decide whether Slice 26.4 earns `SCORE 10/10` and can be committed before continuing to Slice 26.5.

Review these sections:

- Privacy / analytics boundary
- URL / host / package / title / rule-id leak prevention
- Local behavior preservation
- Portable Profile hardening
- Test / evidence
- Visual review applicability
- Bundle gaps
- Package hygiene

## Known Bug Classes To Actively Check

- Remote/export analytics accidentally carrying `targetAppPackage`, foreground package/class, browser package, URL-bar text, raw URL, observed host/domain, page title, path/query, or website rule ids.
- Sanitizer allowing package-like or host-like values through metadata.
- Local analytics losing package data needed for device-local intervention behavior.
- Portable Profile export carrying ephemeral browser support state or private package/host state.
- Portable Profile import warnings revealing missing custom app package names.
- Bundle claiming visual proof where no UI changed, or omitting a required visual proof for an actual UI change.

## Required Output Format

Return all sections exactly:

SCORE: n/10

VERDICT: PASS or FAIL

VISUAL REVIEW: PASS, FAIL, or NOT APPLICABLE

BLOCKERS

PRIVACY / ANALYTICS BOUNDARY

URL / HOST / PACKAGE / TITLE / RULE-ID LEAK PREVENTION

LOCAL BEHAVIOR PRESERVATION

PORTABLE PROFILE HARDENING

TEST / EVIDENCE

VISUAL REVIEW

BUNDLE GAPS

PACKAGE HYGIENE

Only return `SCORE 10/10`, `VERDICT PASS`, and `VISUAL REVIEW PASS` or `VISUAL REVIEW NOT APPLICABLE` if the bundle proves the implementation is safe enough to commit Slice 26.4 and continue to Slice 26.5. If you find any release-blocking issue or score gap, return the minimum fix needed.
