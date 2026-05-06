# GPT Pro Review Request: Sprint 17 Final Release Gate

You are reviewing the Android app repo for Sprint 17 Final Release Gate and APK readiness.

Use only the attached bundle as audit evidence. Read `PRD.md`, then `docs/SPRINT_17_READER_SETTINGS_SYNC_POLISH.md`, then review the final release candidate evidence.

Release candidate:

- Tag target: `v0.10.0-reader-settings-sync-polish-alpha`
- Previous release baseline: `v0.9.0-portable-profile-alpha`
- Android `versionCode`: `15`
- Android `versionName`: `0.10.0-alpha`
- APK candidate SHA-256: `581c3dfd69b54add1e74438b88a5ee20fdea180672bdf790a0ebcc0401ebfde9`

Scope to evaluate:

- Confirm every Sprint 17 slice has a preserved GPT Pro review artifact with `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS`.
- Confirm the final release candidate satisfies the Sprint 17 user-reported problems: Settings typography controls, local defaults and information architecture, Google Drive authorization repair, adaptive pagination fit, source-anchored cross-page annotation selection, compact controls, and annotation note surface sizing.
- Confirm the final local validation is sufficient: unit tests, connected Android tests, debug APK build, signature verification, emulator install, and installed version evidence.
- Confirm the APK metadata is internally consistent with the intended release.
- Confirm visual evidence is broad enough for this final gate.
- Check package hygiene: no stale debug artifacts, duplicate review bundles, APK binaries, unrelated generated outputs, or misleading superseded evidence in the bundle.

Primary evidence to inspect:

- `evidence/sprint17_final_release_gate/VALIDATION.md`
- `evidence/sprint17_final_release_gate/reviews/*.md`
- `evidence/sprint17_final_release_gate/logs/full_unit_validation.log`
- `evidence/sprint17_final_release_gate/logs/full_connected_validation.log`
- `evidence/sprint17_final_release_gate/logs/assemble_debug.log`
- `docs/release-gate-logs/2026-05-06-sprint17-reader-sync-polish/VALIDATION_SUMMARY.md`
- `docs/release-gate-logs/2026-05-06-sprint17-reader-sync-polish/RELEASE_NOTES_v0.10.0-reader-settings-sync-polish-alpha.md`
- `docs/release-gate-logs/2026-05-06-sprint17-reader-sync-polish/apk_badging.txt`
- `docs/release-gate-logs/2026-05-06-sprint17-reader-sync-polish/apksigner_verify.txt`
- `docs/release-gate-logs/2026-05-06-sprint17-reader-sync-polish/adb_install.txt`
- `docs/release-gate-logs/2026-05-06-sprint17-reader-sync-polish/adb_installed_version.txt`
- `evidence/sprint17_final_release_gate/sprint17_release_candidate.diff`

Visual evidence to inspect:

- `docs/visual-qa/sprint17-slice17-1-typography/sprint17-typography-settings-1777999457068/*.png`
- `docs/visual-qa/sprint17-slice17-2-default-destinations/sprint17-default-settings-1778004534886/*.png`
- `docs/visual-qa/sprint17-slice17-3-drive-auth/sprint17-drive-auth-1778007685417/*.png`
- `evidence/sprint17_slice17_4_adaptive_reader_pagination/screenshots/sprint17-adaptive-pagination-1778073394700/*.png`
- `evidence/sprint17_slice17_5_cross_page_annotation_selection/screenshots/sprint17-cross-page-annotation-1778079063962/*.png`
- `evidence/sprint17_slice17_6_annotation_surface_sizing/screenshots/sprint17-annotation-surface-1778083622313/*.png`
- `evidence/sprint17_slice17_6_annotation_surface_sizing/screenshots/sprint17-cross-page-annotation-1778083604958/*.png`

Please return exactly these sections:

SCORE: x/10

VERDICT: PASS or FAIL

VISUAL REVIEW: PASS or FAIL

BLOCKERS:

RELEASE READINESS:

APK / SIGNATURE / INSTALL:

PER-SLICE GATE CHECK:

BUNDLE GAPS:

PACKAGE HYGIENE:

If score is below 10/10, or if VERDICT/VISUAL REVIEW is not PASS, list the concrete blocker(s) that must be fixed before publishing the GitHub release.
