# GPT Pro Review Prompt - Sprint 19 Final Release Gate

You are reviewing Sprint 19 Slice 19.5 release readiness for the Android app in this bundle.

Read `BUNDLE_MANIFEST.md`, `docs/SPRINT_19_AI_NOTE_ASSIST.md`, `docs/release-gate-logs/2026-05-07-sprint19-reader-regression-form/VALIDATION_SUMMARY.md`, `docs/release-gate-logs/2026-05-07-sprint19-reader-regression-form/RELEASE_NOTES_v0.11.2-reader-regression-form-alpha.md`, `evidence/sprint19_reader_regression_form_intervention/GPT_PRO_REVIEW_R2.md`, `app/build.gradle.kts`, and `sprint19_release_candidate.diff` first. Then inspect the raw logs, APK metadata, and screenshot under `docs/release-gate-logs/2026-05-07-sprint19-reader-regression-form/`.

Review scope:

- Whether Slice 19.5 may be tagged and published as `v0.11.2-reader-regression-form-alpha`.
- Whether Android versioning is coherent: `versionCode=18`, `versionName=0.11.2-alpha`, release tag/name `v0.11.2-reader-regression-form-alpha`.
- Whether the release candidate has sufficient evidence: full unit tests, debug build, connected reader/progress/annotation E2E, connected form-intervention E2E, badging, signature verification, emulator install/version readback, launch smoke, and emulator shutdown.
- Whether the shipped APK boundary is correct: reader/progress/annotation/form-intervention fixes included; AI note-assist intentionally excluded.
- Whether release notes accurately describe the changelog versus `v0.11.1-reader-progress-hotfix-alpha`.
- Whether package hygiene is acceptable and the bundle has no stale/noisy/generated artifacts that would mislead the release gate.
- Whether there are any privacy or credential risks, especially accidental AI/OpenRouter/Gemini credential inclusion.

Do not review AI note-assist implementation; it is intentionally not included yet. Do not penalize the absence of AI code in this release gate. Do flag any accidental AI implementation, secret handling risk, or scope creep if present.

Required output format:

```
SCORE: x/10
VERDICT: PASS|FAIL
VISUAL REVIEW: PASS|FAIL

BLOCKERS:
- ...

RELEASE READINESS:
- ...

APK / SIGNATURE / INSTALL:
- ...

READER REGRESSION GATE:
- ...

AI BOUNDARY:
- ...

CHANGELOG:
- ...

BUNDLE GAPS:
- ...

PACKAGE HYGIENE:
- ...
```

Passing standard: return `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS` only if this bundle proves the release can be tagged, pushed, and published on GitHub with the installable debug APK. If anything remains unproven or broken, return a lower score and concrete blockers.
