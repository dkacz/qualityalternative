# GPT Pro Review Request - Sprint 26 Slice 26.4 R2

You are reviewing only Sprint 26 Slice 26.4 R2: Privacy, Analytics, And Portable Profile Hardening.

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
2. `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_4_REVIEW.md`
3. `evidence/sprint26_custom_targets_website_interventions/SLICE26_4_R2_EVIDENCE.md`
4. `evidence/sprint26_custom_targets_website_interventions/SLICE26_4_R2_REVIEW_BUNDLE_MANIFEST.md`

## Review Scope

R1 returned `SCORE 8/10`, `VERDICT FAIL`, `VISUAL REVIEW NOT APPLICABLE`.

R1 blockers were:

- unknown `targetType` could be echoed into top-level `RemoteAnalyticsPayload.targetClass`,
- sanitizer missed IP literal, host-with-port, trailing-dot host, and Unicode/IDNA host variants,
- `unsafeRemoteFields()` did not inspect top-level remote payload fields.

R2 fixes those blockers and also adds a production remote-safe diagnostic summary path through `AnalyticsTracker.allRemoteSafeDebugSummaries()`.

Please decide whether Slice 26.4 R2 earns `SCORE 10/10` and can be committed before continuing to Slice 26.5.

Review these sections:

- R1 blocker recheck
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
- Sanitizer allowing package-like, IPv4, IPv6, host-with-port, trailing-dot host, or Unicode/IDNA host-like values through metadata or top-level fields.
- Unknown `targetType` values leaking through top-level `targetClass` or metadata.
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

R1 BLOCKER RECHECK

PRIVACY / ANALYTICS BOUNDARY

URL / HOST / PACKAGE / TITLE / RULE-ID LEAK PREVENTION

LOCAL BEHAVIOR PRESERVATION

PORTABLE PROFILE HARDENING

TEST / EVIDENCE

VISUAL REVIEW

BUNDLE GAPS

PACKAGE HYGIENE

Only return `SCORE 10/10`, `VERDICT PASS`, and `VISUAL REVIEW PASS` or `VISUAL REVIEW NOT APPLICABLE` if the bundle proves the implementation is safe enough to commit Slice 26.4 and continue to Slice 26.5. If you find any release-blocking issue or score gap, return the minimum fix needed.
