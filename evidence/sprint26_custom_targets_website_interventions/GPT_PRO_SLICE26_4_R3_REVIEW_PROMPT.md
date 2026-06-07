# GPT Pro Review Request - Sprint 26 Slice 26.4 R3

You are reviewing only Sprint 26 Slice 26.4 R3: Privacy, Analytics, And Portable Profile Hardening.

Use only the attached bundle as evidence. If a claim is not proven by shipped files, mark it as `BUNDLE GAP`.

## Primary Files To Read First

1. `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_4_R2_REVIEW.md`
2. `evidence/sprint26_custom_targets_website_interventions/SLICE26_4_R3_EVIDENCE.md`
3. `evidence/sprint26_custom_targets_website_interventions/SLICE26_4_R3_REVIEW_BUNDLE_MANIFEST.md`
4. `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`

## Review Scope

R2 returned `SCORE 9/10`, `VERDICT FAIL`, `VISUAL REVIEW NOT APPLICABLE`.

The only R2 blocker was the remaining sanitizer gap:

- punycode/IDNA host-like values with punycode TLDs could pass,
- trailing-dot IPv4 literals could pass,
- those cases needed proof through both remote payload conversion and top-level `unsafeRemoteFields()` diagnostics.

R3 fixes only that blocker. Please decide whether Slice 26.4 R3 earns `SCORE 10/10` and can be committed before continuing to Slice 26.5.

Review these sections:

- R2 blocker recheck
- Privacy / analytics boundary
- URL / host / package / title / rule-id leak prevention
- Local behavior preservation
- Portable Profile hardening
- Test / evidence
- Visual review applicability
- Bundle gaps
- Package hygiene

## Required Output Format

Return all sections exactly:

SCORE: n/10

VERDICT: PASS or FAIL

VISUAL REVIEW: PASS, FAIL, or NOT APPLICABLE

BLOCKERS

R2 BLOCKER RECHECK

PRIVACY / ANALYTICS BOUNDARY

URL / HOST / PACKAGE / TITLE / RULE-ID LEAK PREVENTION

LOCAL BEHAVIOR PRESERVATION

PORTABLE PROFILE HARDENING

TEST / EVIDENCE

VISUAL REVIEW

BUNDLE GAPS

PACKAGE HYGIENE

Only return `SCORE 10/10`, `VERDICT PASS`, and `VISUAL REVIEW PASS` or `VISUAL REVIEW NOT APPLICABLE` if the bundle proves the implementation is safe enough to commit Slice 26.4 and continue to Slice 26.5. If you find any release-blocking issue or score gap, return the minimum fix needed.
