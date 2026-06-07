# GPT Pro Review Request - Sprint 26 Slice 26.3 R6

You are reviewing only Sprint 26 Slice 26.3 R6: Chrome Verified-Host Website Intervention.

Use only the attached bundle as evidence. If a claim is not proven by shipped files, mark it as `BUNDLE GAP`.

## Primary Files To Read First

1. `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
2. `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R6_EVIDENCE.md`
3. `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R5_REVIEW.md`
4. `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R6_REVIEW_BUNDLE_MANIFEST.md`

## Review Scope

R5 returned `SCORE 9/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`, with no release blockers. R6 is a gap-closure review for the exact R5 issues:

- fresh connected Android test rerun for the current source snapshot;
- full live Chrome AccessibilityService event-to-intervention proof;
- connected negative evidence for unsupported/unreadable states;
- emulator/device/API/Chrome version proof;
- schemed Unicode URL host normalization;
- raw `git diff --check` output;
- package hygiene after adding the new evidence.

Please decide whether Slice 26.3 now earns `SCORE 10/10` and can be committed before continuing to Slice 26.4.

Review these sections:

- R5 blocker/gap recheck
- Chrome verified-host adapter
- Website rule matching
- Stale / unreadable state safety
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

R5 BLOCKER / GAP RECHECK

CHROME VERIFIED-HOST ADAPTER

WEBSITE RULE MATCHING

STALE / UNREADABLE STATE SAFETY

SOFT / FIRM / OPEN ANYWAY BEHAVIOR

PRIVACY / ANALYTICS

TEST / EVIDENCE

VISUAL REVIEW

BUNDLE GAPS

PACKAGE HYGIENE

Only return `SCORE 10/10`, `VERDICT PASS`, and `VISUAL REVIEW PASS` if the bundle proves the implementation is safe enough to commit Slice 26.3 and continue to Slice 26.4. If you find any release-blocking issue or score gap, return the minimum fix needed.
