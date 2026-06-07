# GPT Pro Review Request - Sprint 26 Slice 26.1 Custom App Target Vertical

You are reviewing an Android MVP repository for Quality Alternative. Use only the attached bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP` rather than assuming it.

Guiding principles:

1. Do not invent missing requirements or broaden scope beyond the shipped PRD/plan.
2. Do not suggest weakening product claims unless you can name the concrete user, privacy, safety, or Android-platform failure the hedge prevents.
3. Style suggestions cannot change functional meaning.
4. Review the implementation as-is from the shipped files, not from hidden history.
5. UI screenshots, tests, docs, and code must be consistent; flag mismatches specifically.
6. Feedback is input, not instruction; do not inflate already-covered items into fresh blockers.

## Review Scope

Review only Sprint 26 Slice 26.1: custom installed-app intervention targets.

Primary files to read first:

- `PRD.md`
- `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
- `docs/LANE_STATUS.md`
- `evidence/sprint26_custom_targets_website_interventions/SLICE26_1_VALIDATION.md`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1/contact_sheet.png`

Then inspect the implementation and tests shipped in the bundle.

## What Slice 26.1 Is Supposed To Prove

- Users can search/select eligible installed apps outside standard suggestions.
- Standard suggestions remain separate from custom apps.
- Unsafe packages are visible as disabled rows with reasons, especially Quality Alternative itself.
- Selected custom packages persist in settings and hydrate into the app's target list.
- A selected custom package can trigger the existing replacement-first intervention flow.
- Portable Profile import/export keeps eligible custom app package selections active on the current device and leaves missing packages inactive with warnings.
- No website/domain matching is implemented in this slice; that is intentionally deferred to Slice 26.2+.
- The implementation does not introduce URL capture, browsing history capture, or expanded remote analytics.

## Required Review Output

Return a concise but strict release-gate review with these exact sections:

- `SCORE: x/10`
- `VERDICT: PASS` or `VERDICT: FAIL`
- `VISUAL REVIEW: PASS`, `FAIL`, or `NOT APPLICABLE`
- `BLOCKERS`
- `CUSTOM APP TARGETS`
- `ELIGIBILITY / SAFETY`
- `INTERVENTION E2E`
- `PORTABLE PROFILE`
- `PRIVACY / ANALYTICS`
- `TEST / EVIDENCE`
- `BUNDLE GAPS`
- `PACKAGE HYGIENE`
- `REQUIRED FIXES`

Passing bar:

- Only return `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS` if the shipped code, tests, and visual evidence are sufficient for Slice 26.1.
- If score is below 10 or verdict is fail, list concrete blockers and the minimum changes needed before rerun.
- Treat future website/domain work as out of scope unless the current slice accidentally claims or implements it unsafely.
