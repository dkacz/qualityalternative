# Quality Alternative v0.6.0-content-management-alpha

Sprint 12 turns the app from a one-way content intake flow into a manageable local replacement library with deletion, batch import, reading-time estimates, priority-at-add, saved progress, continue reading, and unfinished-first recommendations.

## New Features

### Delete and manage user content

- Added Library manage mode for user-added links and imported documents.
- Added multi-select deletion for more than one user item at a time.
- Kept editorial starter-pack content read-only for destructive actions.
- Deleted user content is removed from future recommendations.
- Deleting user content also clears saved priority and reading progress for that item.
- Deleting an imported document attempts to release the persisted Android read permission for that document URI.

### Streamlined add flow

- Reworked the add screen to be content-first instead of link-first with an import fallback.
- Links and files now sit in one clearer add experience.
- Batch file import is supported from the document picker.
- Mixed batches are handled cleanly: supported PDF, Markdown, and EPUB files save; unsupported files are rejected without aborting the whole batch.
- Android read permission is retained only for files that were successfully added.

### Auto reading-time estimates

- Markdown imports now get bounded session reading-time estimates from extracted text.
- EPUB imports now get bounded session reading-time estimates from extracted text.
- PDF imports keep MVP metadata/default duration behavior; PDF text extraction is still intentionally out of scope.
- Estimates stay inside the PRD-friendly 3-20 minute replacement-session range.
- Link adds keep an editable default duration.

### Priority while adding

- Links can be marked priority before saving.
- Imported document batches can be marked priority before saving.
- Priority-at-add persists into the same priority set used by recommendation ranking.
- Priority-at-add analytics are distinct from later Library priority toggles.

### Continue reading without intervention

- Reader progress is saved per content item.
- The reader restores near the saved paragraph position.
- Home exposes a continue-reading card for unfinished content.
- Library exposes unfinished content and continue paths.
- Manual reading from Library can save progress and complete content without an intervention session.
- Deleting a user item removes it from continue paths.

### Unfinished-first recommendations

- Unfinished content now has absolute primary priority during interventions unless it is completed or unavailable.
- Completed content remains excluded from primary recommendation slots.
- Recommendation explanations now include unfinished/continue context where relevant.

## Validation

- GPT Pro final release-readiness review: `SCORE: 10/10`, `VERDICT: PASS`.
- Unit tests: 188 tests, 0 failures, 0 errors, 0 skipped.
- Connected Android tests: 64 tests, 0 failures, 0 errors, 0 skipped.
- Final visual QA: 27 light/dark Android screenshots plus contact sheet covering add, priority-at-add, batch import, Library manage, Reader start/mid, continue paths, unfinished Library state, and unfinished-first intervention.
- Release build after version bump: `BUILD SUCCESSFUL in 5m 23s` for `testDebugUnitTest connectedDebugAndroidTest assembleDebug assembleRelease`.

## APK Assets

- Installable alpha APK: `quality-alternative-v0.6.0-content-management-alpha-debug.apk`
- Release-variant APK for signing pipeline: `quality-alternative-v0.6.0-content-management-alpha-release-unsigned.apk`
- Both APKs are versionCode 7 / versionName `0.6.0-alpha`.

## Evidence

- Final GPT Pro audit: `docs/visual-qa/2026-04-28-sprint12-content-management/test-evidence/gpt_pro_final_audit.md`
- Final validation summary: `docs/visual-qa/2026-04-28-sprint12-content-management/VALIDATION_RESULTS.md`
- Final visual contact sheet: `docs/visual-qa/2026-04-28-sprint12-content-management/contact_sheet.png`
- Final Gradle and Android logs: `docs/visual-qa/2026-04-28-sprint12-content-management/test-evidence/`
- Post-version-bump release build validation: `docs/visual-qa/2026-04-28-sprint12-content-management/test-evidence/release_build_validation_20260428.md`
