# GPT Pro Review Prompt - Sprint 19 Regression Fix Gate R2

You are reviewing Sprint 19 slices 19.1-19.3 R2 for the Android app in this bundle. This review is a release-blocking gate for the regression-fix APK that must ship before any AI note-assist work starts.

Read `BUNDLE_MANIFEST.md`, `REGRESSION_FIX_EVIDENCE.md`, `GPT_PRO_REVIEW_R1.md`, `SPRINT_19_AI_NOTE_ASSIST.md`, `PRD.md`, and `sprint19_regression_fix_r2.diff` first. Then inspect the raw logs under `logs/` and screenshots under `screenshots/`.

Review scope:

- Annotation start-backward stability from a later EPUB chapter.
- Reader progress correctness, especially chapter 3 not showing a beginning-of-book `1%`.
- Reader font-size repagination preserving source-anchored progress instead of keeping only a stale display page number. The R2 visual evidence should show changed page count with stable percent.
- Portable Profile / profile-autosave persistence for corrected source-anchor progress fields.
- Annotation save/reopen persistence after moving the start backward across chapters.
- Form intervention `Open anyway` gating: visible 5-second wait, disabled open/close while waiting, unlocked state after the countdown, and analytics coverage.
- Test and evidence quality, including whether the bundle is sufficient for release gating.
- PRD alignment, especially the intentional FR7 update that the wait happens before `Open anyway` becomes available.

Do not review AI note-assist implementation; it is intentionally not included yet. Do not penalize the absence of AI code in this gate. Do flag any accidental AI implementation, secret handling risk, or scope creep if present.

Required output format:

```
SCORE: x/10
VERDICT: PASS|FAIL
VISUAL REVIEW: PASS|FAIL

BLOCKERS:
- ...

GOOGLE DRIVE / PROFILE PROGRESS:
- Say whether the shipped evidence is enough to show progress anchors are persisted through existing profile paths, or label BUNDLE GAP if not.

ANNOTATION SELECTION:
- ...

READER PAGINATION / PROGRESS:
- ...

FORM INTERVENTION:
- ...

RELEASE READINESS:
- ...

BUNDLE GAPS:
- ...

PACKAGE HYGIENE:
- ...
```

Passing standard: return `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS` only if this bundle proves the regression-fix APK can proceed to Slice 19.5. If anything remains unproven or broken, return a lower score and concrete blockers.
