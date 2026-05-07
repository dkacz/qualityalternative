# Sprint 19 Regression Fix Review Bundle Manifest

This bundle is scoped to Sprint 19 regression-fix slices before AI work:

- 19.1 annotation start-backward stability
- 19.2 reader progress and font-size repagination stability
- 19.3 form intervention 5-second unlock
- 19.4 regression gate readiness

Included:

- `PRD.md` with the intentional FR7 update for the pre-open 5-second wait.
- `SPRINT_19_AI_NOTE_ASSIST.md` with sprint order and AI-last boundary.
- `REGRESSION_FIX_EVIDENCE.md` with validation commands and evidence index.
- `GPT_PRO_REVIEW_R1.md` with the failed 7/10 review and the blockers this R2 bundle closes.
- `sprint19_regression_fix_r2.diff` with the full implementation/test/documentation diff for the review scope.
- `logs/*.log` with raw Gradle output for targeted unit tests, assemble, and both connected visual E2E tests.
- `screenshots/reader/*.png` showing chapter 3 progress at 5/7 · 76%, large-font repagination at 8/10 · 76%, annotation before start-back movement, expanded cross-chapter selection, saved highlight, and reopened selector.
- `screenshots/form_intervention/*.png` showing the locked wait state with disabled open/close and the unlocked open-anyway state.

Excluded:

- Whole-repo generated build directories, Gradle caches, `.git`, and stale Sprint 16-18 review bundles.
- Release APK artifacts, because this is the gate before Slice 19.5 release packaging.
- Any AI note-assist implementation or OpenRouter/Gemini configuration, because AI work is explicitly scheduled after the regression-fix APK.

R2 closes the R1 bundle gaps by adding explicit Portable Profile progress-autosave assertions, raw validation logs, larger repagination evidence, saved/reopened annotation evidence, and form-intervention analytics for shown, unlock blocked, unlock enabled, unlock used, completed, and abandoned states.
