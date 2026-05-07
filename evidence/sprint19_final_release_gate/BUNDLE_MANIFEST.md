# Sprint 19 Final Release Gate Bundle Manifest

This bundle is scoped to Slice 19.5 release readiness for `v0.11.2-reader-regression-form-alpha`.

Included:

- `docs/SPRINT_19_AI_NOTE_ASSIST.md` with Sprint 19 ordering and AI-last boundary.
- `docs/release-gate-logs/2026-05-07-sprint19-reader-regression-form/RELEASE_NOTES_v0.11.2-reader-regression-form-alpha.md`.
- `docs/release-gate-logs/2026-05-07-sprint19-reader-regression-form/VALIDATION_SUMMARY.md`.
- Release logs and metadata: unit tests, build, connected E2E, badging, signature, install, installed package, launch focus, shutdown devices, and launch smoke screenshot.
- `evidence/sprint19_reader_regression_form_intervention/GPT_PRO_REVIEW_R2.md` with the `10/10 PASS` visual regression gate.
- `app/build.gradle.kts` for Android versionCode/versionName verification.
- `release_artifacts/quality-alternative-v0.11.2-reader-regression-form-alpha-debug.apk.sha256`.
- `sprint19_release_candidate.diff` with release-candidate changes since the regression-fix implementation commit.

Excluded:

- The APK binary itself, because release readiness is proven by badging, SHA-256, signature verification, install/version readback, and launch smoke in the included logs. The binary remains available locally at `release_artifacts/quality-alternative-v0.11.2-reader-regression-form-alpha-debug.apk` and will be attached to the GitHub release.
- AI note-assist implementation, OpenRouter configuration, provider credentials, and model integration, because AI work remains explicitly deferred until after this regression APK release.
