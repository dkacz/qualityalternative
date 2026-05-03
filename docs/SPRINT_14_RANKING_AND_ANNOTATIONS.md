# Sprint 14: Fresh Content Ranking and Annotations

## Goal

Fix private EPUB/Markdown ranking so newly added, non-completed user documents are surfaced before older de-prioritized files, then add user annotations tied to exact reader fragments with a local annotation library and optional export/autosave to a user-selected Android document provider file, including Google Drive.

## Review Gate

Every slice must pass:

- targeted unit tests for changed logic
- Android E2E test for the user flow
- visual screenshot capture for the changed screens
- GPT Pro review with implementation and visual bundle
- GPT Pro SCORE `10/10`, VERDICT `PASS`, and VISUAL REVIEW `PASS`

Do not start the next implementation slice until the current slice has passed this gate. When waiting for GPT Pro, keep a 10-minute heartbeat harvest active.

## Slice 14.0: Fresh Private Document Ranking

Status: GPT Pro PASS 10/10 with VISUAL REVIEW PASS in R3 (`PRO_REVIEW_OUTPUT_SPRINT14_SLICE14_0_R3_20260502_195309/Sprint14_Slice14_0_R3_GPT_Pro.md`).

Scope:

- Add creation/freshness metadata to user-owned links/documents as rankable content metadata.
- Ensure removing priority from old EPUB/Markdown files actually lets a newly added EPUB surface.
- Preserve the existing absolute priority for unfinished reading progress.
- Preserve explicit priority picks over freshness.

Acceptance:

- A new user EPUB/Markdown beats older unprioritized user documents when score/duration are otherwise similar.
- An explicitly prioritized old item still beats the newest item.
- Unfinished content remains primary even over priority and freshness.
- Completed hidden content stays excluded unless manually reactivated.

Review:

- Unit tests: recommendation engine.
- E2E/visual: add or seed old documents, de-prioritize, add new EPUB, trigger intervention, verify new EPUB appears as primary.

## Slice 14.1: Fragment-Anchored Annotation Storage

Status: GPT Pro PASS 10/10 with VISUAL REVIEW PASS (`PRO_REVIEW_OUTPUT_SPRINT14_SLICE14_1_20260502_202031/Sprint14_Slice14_1_GPT_Pro.md`).

Scope:

- Add persisted annotation model with `contentId`, `paragraphIndex`, quoted fragment text, note text, and timestamps.
- Add Room DAO/repository and migration.
- Delete annotations when user-managed content is deleted.
- Emit analytics for annotation create/update/delete.

Acceptance:

- Annotation survives app restart.
- Annotation points back to the same content and paragraph index.
- Deleted content removes its annotations.
- Existing reader progress/completion remains unaffected.

Review:

- Unit tests: repository/view-model save, update, delete.
- Android test: save annotation in reader, relaunch, verify it remains attached to the same fragment.

## Slice 14.2: Reader Annotation UI

Status: GPT Pro PASS 10/10 with VISUAL REVIEW PASS (`PRO_REVIEW_OUTPUT_SPRINT14_SLICE14_2_20260502_212309/Sprint14_Slice14_2_GPT_Pro.md`).

Scope:

- Add an annotation action directly beside active reader text blocks.
- Add an editor that shows the quoted fragment and saves/updates the note.
- Show an unobtrusive note indicator/preview on annotated fragments.
- Keep reader focused on the current content, without unrelated recommendations.

Acceptance:

- User can add and edit a note from the active reader.
- Annotated fragment clearly shows that a note exists.
- The UI is usable on phone viewport in light and dark themes.

Review:

- E2E test: open reader, annotate a paragraph, verify preview.
- Visual QA: reader start, annotation editor, saved annotation preview.

## Slice 14.3: Annotation Library

Status: GPT Pro PASS 10/10 with VISUAL REVIEW PASS in R2 (`PRO_REVIEW_OUTPUT_SPRINT14_SLICE14_3_R2_20260502_221929/Sprint14_Slice14_3_R2_GPT_Pro.md`).

Scope:

- Add a Library/Progress-accessible annotation list.
- Each annotation row shows content title, quote, note, updated time, and source type.
- Tapping an annotation opens the original content in Reader near that paragraph.
- Keep the library finite and management-oriented, not a discovery feed.

Acceptance:

- User can browse all annotations.
- User can jump from annotation to exact content/fragment.
- Missing/deleted content is handled clearly.

Review:

- Unit tests: view-model opens annotation target with paragraph restore intent.
- Android E2E: save two notes, open annotation library, jump back to fragment.
- Visual QA: annotation library light/dark and fragment jump.

Local evidence:

- `./gradlew testDebugUnitTest` PASS.
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#annotationLibraryListsSavedNotesAndOpensSourceFragment` PASS.
- R1 GPT Pro output: `PRO_REVIEW_OUTPUT_SPRINT14_SLICE14_3_20260502_215709/GPT_Pro_Sprint_14_Review.md`.
- R2 GPT Pro output: `PRO_REVIEW_OUTPUT_SPRINT14_SLICE14_3_R2_20260502_221929/Sprint14_Slice14_3_R2_GPT_Pro.md`.
- R2 visual screenshots: `docs/visual-qa/2026-05-02-sprint14-annotation-library-r2/sprint14-annotation-library-1777753094209/`.

## Slice 14.4: Export and Google Drive Autosave

Status: R2 implemented locally and ready for GPT Pro review after R1 scored `9/10 PASS` with VISUAL REVIEW PASS.

Scope:

- Add Settings option to choose an annotation export document using Android document picker.
- Store the chosen URI with persistable write permission.
- On annotation save/update/delete, export all annotations to that file.
- Use a simple Markdown export format grouped by content title with quote, note, and local fragment reference.
- Present this as Android document-provider export/autosave, compatible with Google Drive when the user chooses a Drive file.

Acceptance:

- User can select an export destination.
- Annotation changes update the chosen file.
- Export failure is visible and non-destructive.
- Feature works without Drive login inside the app; Drive is handled by Android provider.

Review:

- Unit tests: export formatting and failure handling.
- Android test: choose test URI/provider, save note, verify export content.
- Visual QA: settings export row, successful autosave state, failure state if feasible.

Local evidence:

- `./gradlew testDebugUnitTest` PASS.
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#annotationAutosaveWritesMarkdownAndShowsSettingsStatus` PASS.
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.VisualQaScreenshotTest#captureCoreContentScreensInLightAndDark` PASS after visual harness state reset for unfinished-content priority.
- `./gradlew connectedDebugAndroidTest` PASS, 77/77.
- R1 GPT Pro output: `PRO_REVIEW_OUTPUT_SPRINT14_SLICE14_4_20260502_225749/Sprint14_Slice14_4_GPT_Pro.md`.
- R2 visual screenshots: `docs/visual-qa/2026-05-02-sprint14-annotation-export/sprint14-annotation-export-1777757272463/`.

R2 fixes:

- Persistable write-permission failures now stop the first export write and surface a recoverable permission error.
- Changing the export destination clears the previous success timestamp before any new write succeeds.
- Turning off autosave releases the persisted URI grant where Android allows it.
- Deleting one annotation from the reader triggers autosave and rewrites the Markdown file without the removed note.
- Visual regression harness now resets reader carryover before the dark repeat, so unfinished-content priority does not destabilize unrelated visual captures.

## Slice 14.5: Direct Reader Mode and Pagination

Status: R2 implemented after GPT Pro review, locally verified, ready for GPT Pro R2 review.

Scope:

- Make the app usable as a reader directly from Home/Library without opening a distracting app or triggering an intervention.
- Replace long reader scrolling with default page-by-page reading for all content.
- Keep annotation actions, fragment anchors, reading progress, continue reading, and completion intact under pagination.
- Preserve the finite reader experience; this must not become a feed or discovery surface.

Acceptance:

- User can open the reader intentionally without intervention.
- Long EPUB/Markdown content renders only the current page slice, avoiding full-document scroll lag.
- Next/previous page controls update visible progress and saved reading progress.
- Done/completion is available at the end of the paginated content.
- Annotation add/edit/delete works on paginated reader pages.

Evidence:

- Unit tests: `./gradlew testDebugUnitTest` passed.
- Android E2E: `./gradlew connectedDebugAndroidTest` passed, 78/78 tests.
- New E2E coverage: Home `Read now` opens Library and a paginated reader without an intervention trigger.
- Reader pagination coverage: page label, next/previous controls, page-based progress, restore-region semantics, and annotation actions remain present.
- Visual screenshots: `docs/visual-qa/2026-05-02-sprint14-reader-pagination/sprint14-reader-pagination-1777760549765/`.
- R2 targeted validation after GPT Pro blocker: `./gradlew testDebugUnitTest connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#homeReadNowOpensLibraryAndPaginatedReaderWithoutIntervention` passed.
- R2 full Android E2E: `./gradlew connectedDebugAndroidTest` passed, 78/78 tests.
- R2 visual screenshots: `docs/visual-qa/2026-05-02-sprint14-reader-pagination-r2/sprint14-reader-pagination-1777761795690/`.

Implementation notes:

- Home exposes a direct `Read now` library entry so the app can be used as a reader outside the intervention flow.
- Reader rendering now slices content into finite pages and renders only the current page, avoiding full-document long-scroll rendering for large EPUBs.
- Reader progress display is page-based (`50% read` on page 1/2, `100% read` on page 2/2), while saved restore anchors still preserve paragraph-level resume behavior.
- Moving backward through pages does not lower saved reading progress.

R2 fixes:

- Added a page-keyed `LazyListState` reset so Next/Previous always opens the target page at its label/top paragraphs even if the user scrolled inside the previous page.
- Expanded the direct-reader E2E to scroll within a reader page before Next and Previous, then assert that `Page 2/2` and `Page 1/2` are visible after navigation.

## Slice 14.6: Final UX/Copy Declutter Review

Status: GPT Pro R3 `10/10 PASS`, VISUAL REVIEW PASS. Ready for Slice 14.7 final hardening/release review.

Scope:

- Run a dedicated GPT Pro UX review across the final Sprint 14 screens before release.
- Audit whether screens contain unnecessary explanatory text, repeated helper copy, or crowded status language.
- Prefer tighter labels, clearer actions, and calmer screen density without removing needed state, error, or accessibility information.
- Include visual screenshots from intervention, reader, pagination, direct reader entry, annotation editor, annotation library, and settings/export states.

Acceptance:

- GPT Pro explicitly reviews visual presentation and copy density.
- Any release-blocking clutter or confusing copy is fixed and rerun until `10/10 PASS` with VISUAL REVIEW PASS.
- Remaining explanatory text is intentional: it either prevents user error, explains a non-obvious state, or labels a required action.

Evidence package:

- UX review screenshots: `docs/visual-qa/2026-05-03-sprint14-ux-review/`.
- Included states cover intervention, direct reader entry, paginated reader, annotation editor/preview, annotation library, settings/open-anyway unlock, annotation export success/failure, completed-content activation, meditation always-option, and fresh-ranking primary content.
- Prior local validation before this review: `./gradlew testDebugUnitTest` PASS and `./gradlew connectedDebugAndroidTest` PASS, 78/78.
- Prior gate: Slice 14.5 R2 GPT Pro `10/10 PASS`, VISUAL REVIEW PASS.
- R1 GPT Pro UX review: `8/10 FAIL`, VISUAL REVIEW FAIL.
- R2 targeted validation: unit tests plus Home/pagination, annotation export, reader annotation, and sprint13 visual capture tests passed.
- R2 full validation: `./gradlew testDebugUnitTest connectedDebugAndroidTest` PASS, 78/78.
- R2 full-run screenshots: `docs/visual-qa/2026-05-03-sprint14-ux-review-r2-full/`.
- R2 GPT Pro UX review: `9/10 FAIL`, VISUAL REVIEW FAIL.
- R3 targeted validation: `./gradlew testDebugUnitTest` PASS; Android autosave settings, direct reader pagination, and completed/unlock visual tests PASS.
- R3 full validation: `./gradlew testDebugUnitTest connectedDebugAndroidTest` PASS, 78/78.
- R3 full-run screenshots: `docs/visual-qa/2026-05-03-sprint14-ux-review-r3-full/`.
- R3 GPT Pro UX review: `10/10 PASS`, VISUAL REVIEW PASS, BLOCKERS None.
- R3 GPT Pro output: `PRO_REVIEW_OUTPUT_SPRINT14_SLICE14_6_UX_R3_20260503_022619/Sprint14_Slice14_6_UX_R3_GPT_Pro.md`.

R2 fixes:

- Permission-paused Home now says `Finish setup to intercept distracting apps.` instead of the ready-state headline.
- Annotation export failures now show `Autosave failed. Choose the file again or retry.` instead of raw provider/URI errors.
- Disabled terminal reader pagination now renders `Last page` as an outline disabled control without a forward arrow.

R3 fixes:

- Home and Settings visible count labels now use singular/plural copy (`1 app`, `2 apps`, `1 item`, `2 items`) instead of hardcoded plural nouns.
- Read-now and Library summaries reuse the same count-label helper for item/link/file/pick copy.
- Annotation autosave success now labels the manual export action `Save now`; only failure state labels the action `Retry`.
- Android E2E asserts `Save now` in successful autosave settings and `Retry` in failure settings.

## Slice 14.7: Final Sprint Hardening and Release

Scope:

- Full regression tests.
- Visual contact sheet for ranking, direct reader, pagination, reader annotation, annotation library, settings export, and end-to-end flow.
- GPT Pro final sprint review.
- Version bump and debug APK release only after final `10/10 PASS` with visual PASS.

Acceptance:

- Full Sprint 14 backlog passes locally and in GPT Pro review.
- Release notes state exactly what changed versus previous release.
- APK is the installable debug artifact, matching the current accepted release pattern.
