# Sprint 15: Kindle Reader, EPUB TOC, W3C Annotations, Drive Sync

## User Contract

This sprint corrects the Sprint 14 reader/annotation model and removes the remaining ranking trap around whole-document duration.

- Reading is paginated by default. There is no vertical scrolling in active reader pages.
- Tapping most of the active page advances to the next page. Previous-page navigation uses a reader gesture or the very small left-edge tap zone; Android back exits the reader after first dismissing open reader overlays.
- Active reading uses near-zero chrome: no persistent Previous, Next, or I'm done reading buttons. Title, progress, page number, and TOC access are small footer affordances so the text remains the screen.
- EPUBs expose table-of-contents navigation and TOC entries jump to the closest page or section.
- Reader annotations are created from long-press text selection, not margin icons.
- The initial selection is the sentence under the long press; the user can adjust the range before saving.
- The annotation popup is an overlay and must not increase page height or turn pagination into scrolling.
- Annotation files use W3C Web Annotation JSON-LD as the canonical portable format.
- EPUB annotations include an EPUB locator when available, with EPUB CFI as the preferred long-term locator.
- Google Drive sync is real authorization and API sync, not only Android document-provider export.
- Drive sync writes one annotation file per annotated source, with the source title included in the filename.
- Recommendation ranking must not exclude or penalize long readable materials because the whole source is longer than the user's session target.
- User-added/imported materials must not ask the user to store a manual reading-time estimate. Reading time is computed from available text and exposed as current-segment/remaining-time metadata.

## Standards And External Contracts

- W3C Web Annotation Data Model is the annotation export contract: `https://www.w3.org/TR/annotation-model/`
- EPUB Canonical Fragment Identifiers are the preferred EPUB locator contract: `https://w3c.github.io/epub-specs/epub33/epubcfi/`
- Google Drive authorization follows Android's separate authorization flow through AuthorizationClient: `https://developer.android.com/identity/authorization`
- Drive file creation/update follows Google Drive API upload/update behavior and the narrowest viable Drive scope: `https://developers.google.com/workspace/drive/api/guides/api-specific-auth`

## Slice 15.0: Contract, Ranking, Auto Time Foundation

Mapped PRD items: FR3, FR4, FR5, FR6, FR8, NFR analytics/testability.

Deliverables:

- PRD updated so preferred session length is no longer a whole-material recommendation filter.
- Recommendation engine stops using whole-source duration distance or shorter-than-primary backup gating.
- User document import stops requiring or validating manual duration input.
- Reading duration for user documents is computed from Markdown/EPUB/PDF estimator output.
- Long readable items remain eligible; their intervention labels use current progress and remaining/current segment estimates.
- Tests cover a long fresh EPUB/Markdown beating stale shorter items when priority/freshness/unfinished state says it should.

Review gate:

- Unit tests for recommendation and document validation.
- GPT Pro review with code evidence and visual contract evidence if UI copy changes.
- Must reach SCORE 10/10, VERDICT PASS, VISUAL REVIEW PASS before Slice 15.1.

## Slice 15.1: EPUB Document Model And TOC Extraction

Mapped PRD items: FR3, FR8, FR8A.

Deliverables:

- EPUB extraction returns a structured reader document: readable blocks, source hrefs, section anchors, and TOC entries.
- EPUB 3 nav documents and EPUB 2 NCX TOCs are parsed.
- TOC entries map to the nearest reader block/page even when anchor precision is unavailable.
- Existing plain-text extraction callers keep working during migration.

Review gate:

- Unit tests for EPUB 3 nav TOC, EPUB 2 NCX TOC, missing TOC fallback, and href-to-block mapping.
- GPT Pro review to 10/10 before Slice 15.2.

## Slice 15.2: No-Scroll Kindle Reader And TOC Navigation

Mapped PRD items: FR6, FR8.

Deliverables:

- Replace the active reader's vertical list with a fixed page viewport.
- Remove active-page vertical scrolling.
- Page tap advances.
- Previous-page navigation uses a reader gesture or the very small left-edge tap zone; Android/system back exits after closing reader overlays.
- Persistent Previous, Next page, and I'm done reading buttons are removed from the active reader.
- The title, page/progress metadata, and TOC affordance move into a small footer.
- Progress reflects page/document position.
- TOC navigation opens as a bounded overlay/sheet and jumps to the nearest page.
- Reader mode can be opened directly from library as a reader, not only via intervention.

Review gate:

- Compose tests for tap next, swipe/edge previous, Android Back exit, no scrollable active page, TOC jump, and direct reader entry.
- Emulator screenshots/light-dark visual bundle.
- GPT Pro visual review to 10/10 before Slice 15.3.

Implementation status:

- `MainUiState` now carries `currentReaderDocument` so the reader can use structured EPUB blocks and TOC entries instead of only a flattened body string.
- Library and intervention reader opens load `ContentRepository.readerDocument(content)` while preserving `currentContentBody` as a compatibility fallback.
- Reader pages are finite active pages; the active body uses `LazyColumn(userScrollEnabled = false)` and a fixed `reader-page-viewport`.
- Tap-to-next uses a tap-only pointer handler plus accessibility click semantics so vertical swipes do not advance or scroll.
- Pagination now uses conservative rendered-line weights instead of raw character buckets, and oversized single blocks are split at sentence boundaries before pagination so fixed no-scroll pages do not clip unread text.
- The old visible reader control bar is removed: there are no persistent Previous, Next page, or I'm done reading buttons in active reading.
- Tapping the last page finishes the reader session and opens feedback; no standalone Done button is needed.
- Reader title, page count, progress, and TOC access are reduced to a small footer.
- Margin annotation icon buttons are removed from active reading; long-press on a text block now opens the existing note editor without adding visible chrome.
- Android/system back exits the reader after closing TOC/annotation overlays; previous-page movement is handled by swipe-right or the small left-edge tap zone.
- TOC opens as a bounded overlay sheet and jumps to the page for the selected section.
- Section headings start a new page only after body content, avoiding blank heading-only pages for consecutive headings.

Validation evidence:

- `./gradlew testDebugUnitTest`
- `./gradlew compileDebugAndroidTestKotlin`
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#readerAnnotationEditorSavesEditsAndShowsPreview`
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#epubReaderUsesKindlePagingAndTableOfContentsNavigation`
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.VisualQaScreenshotTest#captureSprint15KindlePagingAndTocScreens`
- Visual evidence: `docs/visual-qa/2026-05-03-sprint15-slice15-2-r4-long-block-fit/contact_sheet.png`
- GPT Pro R2 review is preserved at `PRO_REVIEW_OUTPUT_SPRINT15_SLICE15_2_R2_20260503_101338/Sprint15_Slice15_2_R2_GPT_Pro.md`; it failed 7/10 because the previous fixed page could clip Chapter Two continuation text. The R3 correction adds the continuation screenshot and the line-weight pagination fix.
- GPT Pro R3 review is preserved at `PRO_REVIEW_OUTPUT_SPRINT15_SLICE15_2_R3_20260503_103358/Sprint15_Slice15_2_R3_GPT_Pro.md`; it passed functionally/visually but scored 9/10 because a very long single paragraph could still overflow. The R4 correction splits oversized single blocks and adds long-single-paragraph visual evidence.
- GPT Pro R4 review is preserved at `PRO_REVIEW_OUTPUT_SPRINT15_SLICE15_2_R4_20260503_105311/Sprint15_Slice15_2_R4_GPT_Pro.md`; SCORE 10/10, VERDICT PASS, VISUAL REVIEW PASS.

Known intentional carry-forward:

- Slice 15.2 R2 removes visible annotation controls and uses long-press to open the current editor. Slice 15.3 still owns sentence-level selection, range adjustment, and a non-resizing annotation popup.

## Slice 15.3: Long-Press Selection Annotation UX

Mapped PRD items: FR8, FR8A.

Deliverables:

- Remove margin annotation icons from the active reader page.
- Long press selects the sentence under the press.
- User can adjust the selected range before saving.
- Add/edit note popup overlays the page and does not resize page content or enable scrolling.
- Saved annotations highlight the selected text fragment without adding margin controls.

Review gate:

- Compose tests for long-press selection, range adjustment, note save/edit, and no scroll introduced by popup.
- Visual screenshots proving page geometry remains fixed.
- GPT Pro visual review to 10/10 before Slice 15.4.

Implementation status:

- Active reader annotation controls are text-first: long-press on a reader paragraph opens an overlay editor; no margin action icons or inline editor controls are rendered on the page.
- Initial selection uses the sentence under the press, with existing annotations reopening at their saved quote when possible.
- Range controls can expand or shrink the selected sentence span before save.
- The note editor is a bottom overlay with a scrim; it does not participate in the reader page layout and does not enable reader scrolling.
- Saved notes render as text highlights only, in light and dark mode, while the reader footer/page geometry remains fixed.
- Annotation library flows still list saved notes and jump back to the source fragment/page.

Validation evidence:

- `./gradlew testDebugUnitTest compileDebugAndroidTestKotlin`
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#readerAnnotationEditorSavesEditsAndShowsPreview`
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#annotationLibraryListsSavedNotesAndOpensSourceFragment,com.qualityalternative.app.MainActivityTest#epubReaderUsesKindlePagingAndTableOfContentsNavigation`
- Visual evidence: `docs/visual-qa/2026-05-03-sprint15-slice15-3-r1-annotation-selection/contact_sheet.png`
- Visual evidence: `docs/visual-qa/2026-05-03-sprint15-slice15-3-r1-annotation-library/contact_sheet.png`
- GPT Pro R1 review is preserved at `PRO_REVIEW_OUTPUT_SPRINT15_SLICE15_3_20260503_111821/Sprint15_Slice15_3_GPT_Pro.md`; it failed 8/10 with VISUAL REVIEW PASS because editing an existing multi-sentence annotation reopened only the first sentence and could shrink the saved quote.
- R2 correction preserves the full previously saved quote span when reopening an existing annotation and extends the instrumentation test to assert the expanded quote survives both first save and note-only edit.
- R2 visual evidence: `docs/visual-qa/2026-05-03-sprint15-slice15-3-r2-annotation-selection/contact_sheet.png`
- R2 visual evidence: `docs/visual-qa/2026-05-03-sprint15-slice15-3-r2-annotation-library/contact_sheet.png`
- GPT Pro R2 review is preserved at `PRO_REVIEW_OUTPUT_SPRINT15_SLICE15_3_R2_20260503_113804/Sprint15_Slice15_3_R2_GPT_Pro.md`; SCORE 10/10, VERDICT PASS, VISUAL REVIEW PASS.

## Slice 15.4: W3C Annotation Storage And Per-Source Export

Mapped PRD items: FR8A, NFR privacy, NFR data integrity.

Deliverables:

- Local annotation model stores source title, selector data, exact quote, text position, prefix/suffix, paragraph/block identity, and EPUB locator when available.
- Room migration preserves existing paragraph annotations.
- W3C Web Annotation JSON-LD formatter writes one file payload per source.
- Filenames include sanitized source titles and stable content identity.
- Annotation library returns to exact source fragment/page.

Review gate:

- Unit tests for migration, selector persistence, W3C JSON-LD shape, filename sanitization, and return-to-fragment.
- GPT Pro review to 10/10 before Slice 15.5.

Implementation status:

- `ReadingAnnotation` and `ReadingAnnotationDraft` now persist source title, label, source type, source format, and a structured selector.
- Selector data includes source href, source anchor, source block index, exact text position offsets, and prefix/suffix context for W3C quote targeting.
- Reader annotation saves now pass source locator metadata from structured reader blocks, including EPUB href/anchor data when available.
- Room database version 10 adds source and selector columns with defaults so existing paragraph annotations remain valid after migration.
- The 8 to 9 migration keeps the historical version-9 annotation table shape, and the 9 to 10 migration alone adds the source/selector columns so 8 to 10 chains do not duplicate columns.
- Room repository roundtrips source metadata and selector fields for newly saved notes while normalizing legacy rows.
- Minimal note-only updates preserve previously stored source metadata and selector fields instead of erasing annotation anchors.
- W3C JSON-LD export now produces one `AnnotationCollection` payload per annotated source.
- Export filenames include sanitized source title plus stable content identity.
- Local file export writes per-source `.annotations.jsonld` files and a JSON index for multi-source exports.
- Annotation autosave deletion removes stale per-source JSON-LD sidecars from the selected export directory.
- Annotation autosave settings copy now names W3C JSON-LD instead of the old Markdown export wording.
- Annotation library return-to-source behavior remains covered after the selector/export migration.
- R3 adds Room `MigrationTestHelper.runMigrationsAndValidate(...)` coverage against exported schema 10 for both 8 to 10 and 9 to 10.
- R3 changes annotation export picker MIME/default names/fallback names from Markdown to JSON-LD.
- R3 namespaces sidecar files with `quality-alternative-` and stale cleanup only removes Quality Alternative sidecars, not every `*.annotations.jsonld` in the directory.

Validation evidence:

- `./gradlew testDebugUnitTest compileDebugAndroidTestKotlin`
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.data.RoomReadingAnnotationRepositoryTest,com.qualityalternative.app.MainActivityTest#annotationAutosaveWritesW3cJsonLdAndShowsSettingsStatus,com.qualityalternative.app.MainActivityTest#annotationLibraryListsSavedNotesAndOpensSourceFragment,com.qualityalternative.app.MainActivityTest#readerAnnotationEditorSavesEditsAndShowsPreview,com.qualityalternative.app.MainActivityTest#epubReaderUsesKindlePagingAndTableOfContentsNavigation`
- R2 correction validation: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.data.local.QualityAlternativeDatabaseMigrationInstrumentedTest,com.qualityalternative.app.data.RoomReadingAnnotationRepositoryTest,com.qualityalternative.app.MainActivityTest#annotationAutosaveWritesW3cJsonLdAndShowsSettingsStatus,com.qualityalternative.app.MainActivityTest#annotationLibraryListsSavedNotesAndOpensSourceFragment`
- Visual evidence R1: `docs/visual-qa/2026-05-03-sprint15-slice15-4-w3c-export/contact_sheet.png`
- Visual evidence R2: `docs/visual-qa/2026-05-03-sprint15-slice15-4-r2-w3c-export/contact_sheet.png`
- GPT Pro R1 review is preserved at `PRO_REVIEW_OUTPUT_SPRINT15_SLICE15_4_20260503_121500/Sprint15_Slice15_4_GPT_Pro.md`; it failed 7/10 with VISUAL REVIEW PASS because the 8 to 10 migration chain could duplicate source/selector columns and because migration coverage was too shallow.
- R2 correction separates the historical v9 annotation table from v10 columns, adds real SQLite migration coverage for 8 to 10 and 9 to 10, preserves selector/source fields on minimal updates, and refreshes the settings copy/screenshot.
- GPT Pro R2 review is preserved at `PRO_REVIEW_OUTPUT_SPRINT15_SLICE15_4_R2_20260503_123000/Sprint15_Slice15_4_R2_GPT_Pro.md`; it passed 9/10 with VISUAL REVIEW PASS and no blockers, but requested Room schema validation, JSON-LD picker naming, and safer sidecar cleanup before a 10/10 gate.
- R3 correction adds Room testing dependency/schema assets, validates migrations with Room's exported-schema validator, resolves the Room testing serialization runtime to 1.8.1, updates picker paths to `.jsonld`, and narrows cleanup to `quality-alternative-*.annotations.jsonld`.
- Visual evidence R3: `docs/visual-qa/2026-05-03-sprint15-slice15-4-r3-w3c-export/contact_sheet.png`
- GPT Pro R3 review is preserved at `PRO_REVIEW_OUTPUT_SPRINT15_SLICE15_4_R3_20260503_155500/Sprint15_Slice15_4_R3_GPT_Pro.md`; it failed 9/10 with VISUAL REVIEW FAIL because the light success screenshot fixture still displayed `qa-annotations-e2e.json`.
- R4 correction changes the success screenshot fixture/export display name to `qa-annotations-e2e.jsonld` and refreshes the visual evidence.
- Visual evidence R4: `docs/visual-qa/2026-05-03-sprint15-slice15-4-r4-w3c-export/contact_sheet.png`
- GPT Pro R4 review is preserved at `PRO_REVIEW_OUTPUT_SPRINT15_SLICE15_4_R4_20260503_162500/Sprint15_Slice15_4_R4_GPT_Pro.md`; SCORE 10/10, VERDICT PASS, VISUAL REVIEW PASS.

## Slice 15.5: Real Google Drive Authorization And Sync

Mapped PRD items: FR8A, FR9 settings, NFR privacy, NFR reliability.

Deliverables:

- Settings includes Connect Google Drive, sync status, manual sync, and disconnect/revoke.
- Android Google authorization requests the narrowest viable Drive scope only when the user connects Drive sync.
- Drive sync creates or reuses a Quality Alternative annotations folder/file set.
- Each annotated source syncs to its own JSON-LD file.
- Offline and API failures surface status and keep local annotations safe.
- Tests use a fake Drive client; manual smoke uses a configured OAuth Android client where available.

Review gate:

- Unit tests for sync planning, file naming, create/update behavior, retry/error state, and disconnect.
- Settings visual screenshots.
- GPT Pro visual review to 10/10 before Slice 15.6.

Implementation status:

- Settings now includes a Google Drive sync card under annotation autosave with Connect, Save now/Retry, Disconnect, progress, and recoverable failure status.
- The Android UI uses Google Identity Services `AuthorizationClient` with only the `https://www.googleapis.com/auth/drive.file` scope when the user explicitly connects Drive sync.
- Drive disconnect calls `AuthorizationClient.revokeAccess(...)` for the same Drive scope and clears local sync state.
- `AndroidGoogleDriveAnnotationSyncClient` uses Drive REST APIs with the granted access token to create or reuse a `Quality Alternative annotations` folder.
- Drive sync writes `quality-alternative-annotations.index.json` plus one `.annotations.jsonld` W3C payload per annotated source.
- Per-source files are upserted by filename inside the Quality Alternative folder so repeated syncs update existing files instead of duplicating them.
- Drive API/auth failures surface in Settings and never block local annotation save/update/delete.
- Annotation saves autosync to Drive by first asking Google Identity for a fresh token when Drive consent already exists, including same-process autosync after the originally connected token may have gone stale.
- If Google Identity cannot refresh access silently, the app keeps local notes and asks the user to retry from Settings.
- Analytics now records Drive sync connected, success, failure, and disconnected events.

Validation evidence:

- `./gradlew testDebugUnitTest --tests com.qualityalternative.app.ui.MainViewModelTest --tests com.qualityalternative.app.data.PreferencesSettingsRepositoryTest --rerun-tasks`
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#annotationDriveSyncSettingsShowsConnectAndRecoverableAuthFailure,com.qualityalternative.app.MainActivityTest#annotationAutosaveWritesW3cJsonLdAndShowsSettingsStatus`
- R2 correction validation: `./gradlew testDebugUnitTest --tests com.qualityalternative.app.ui.MainViewModelTest.googleDriveAnnotationAutosaveRefreshesTokenSilentlyWhenCachedTokenIsStale --tests com.qualityalternative.app.ui.MainViewModelTest.googleDriveAnnotationAutosaveRefreshesTokenSilentlyAfterProcessRestart --tests com.qualityalternative.app.ui.MainViewModelTest.googleDriveAnnotationSyncConnectsAndAutosavesPerSourceJsonLd`
- Visual evidence: `docs/visual-qa/2026-05-03-sprint15-slice15-5-drive-sync/contact_sheet.png`
- GPT Pro R1 review is preserved at `PRO_REVIEW_OUTPUT_SPRINT15_SLICE15_5_20260503_1730/Sprint_15_Slice_155_Review.md`; it failed 9/10 with VISUAL REVIEW PASS because same-process autosync could reuse a cached Drive access token indefinitely.
- R2 correction makes connected autosync request a fresh Google Identity token first and adds a same-process stale-token regression test.
- GPT Pro R2 review is preserved at `PRO_REVIEW_OUTPUT_SPRINT15_SLICE15_5_R2_20260503_185800/Sprint15_Slice15_5_R2_GPT_Pro.md`; SCORE 10/10, VERDICT PASS, VISUAL REVIEW PASS.

## Slice 15.6: Final UX Declutter, Hardening, Release

Mapped PRD items: all changed FRs plus Definition of Done.

Deliverables:

- Final UX pass removes explanatory clutter from reader, annotations, TOC, Drive settings, and intervention labels.
- Full unit and connected Android tests pass.
- Final emulator visual evidence includes intervention, reader pagination, TOC, selection popup, annotation library, and Drive settings.
- GPT Pro final release gate reaches SCORE 10/10, VERDICT PASS, VISUAL REVIEW PASS.
- Version bump, installable debug APK, signature verification, install verification, GitHub release, and changelog versus `v0.7.0-ranking-annotations-alpha`.

Implementation status:

- Active reader semantics no longer expose `Next page` or `Finish reading`; the visible reader remains page-first with only a small footer for TOC, title, progress, page number, and percent.
- Reader footer spacing and progress width are reduced so content dominates the screen.
- Intervention copy is reduced: the extra detour line, backup-list helper sentence, and the large `Why this` explanation card are removed; the primary recommendation keeps only compact fit chips and progress metadata.
- Annotation editor removes the redundant `Selected fragment` label while keeping the quote preview, range controls, note field, and save/delete actions as an overlay that does not resize the page.
- Annotation autosave and Google Drive Settings cards remove long explanatory paragraphs and rely on status plus direct actions.
- E2E tests now assert the reader has no visible `Next page`, `Previous`, or `I'm done reading` controls, intervention helper text is absent, and the annotation editor still expands and persists a selected range.
- R2 correction changes local annotation export selection from a single JSON-LD file to a folder destination so Android SAF/tree exports can write one `.annotations.jsonld` file per source plus an index.
- R2 correction refuses multi-source export to legacy single-document URIs instead of writing a misleading index-only file.
- R2 correction makes the annotation library fall back to the stored annotation `sourceTitle` when the source is no longer available in Library.
- R2 correction refreshes final visual evidence from the post-fix emulator run, including the folder-based export card and the missing-source annotation row displaying `Deleted essay title`.
- R3 correction writes `quality-alternative-annotations.index.json` for local `file://` directory exports, matching SAF tree behavior, and adds instrumented regression coverage for per-source files plus index.

Validation evidence:

- `./gradlew testDebugUnitTest compileDebugAndroidTestKotlin`
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#homeReadNowOpensLibraryAndPaginatedReaderWithoutIntervention,com.qualityalternative.app.MainActivityTest#epubReaderUsesKindlePagingAndTableOfContentsNavigation,com.qualityalternative.app.MainActivityTest#systemInterventionShowsContinueProgressRemainingTimeAndScrollableOtherOptions,com.qualityalternative.app.MainActivityTest#readerAnnotationEditorSavesEditsAndShowsPreview,com.qualityalternative.app.MainActivityTest#annotationLibraryListsSavedNotesAndOpensSourceFragment,com.qualityalternative.app.MainActivityTest#annotationDriveSyncSettingsShowsConnectAndRecoverableAuthFailure`
- R2 correction validation: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#annotationLibraryListsSavedNotesAndOpensSourceFragment,com.qualityalternative.app.MainActivityTest#annotationAutosaveWritesW3cJsonLdAndShowsSettingsStatus,com.qualityalternative.app.MainActivityTest#annotationDriveSyncSettingsShowsConnectAndRecoverableAuthFailure`
- Visual evidence: `docs/visual-qa/2026-05-03-sprint15-slice15-6-final-ux/contact_sheet.png`
- R2 visual evidence: `docs/visual-qa/2026-05-03-sprint15-final-release-r2/contact_sheet.png`
- R2 Gradle log: `docs/release-gate-logs/2026-05-03-sprint15-r2/testDebugUnitTest_compileDebugAndroidTestKotlin.log`
- R2 connected Android log: `docs/release-gate-logs/2026-05-03-sprint15-r2/connectedDebugAndroidTest_release_gate.log`
- GPT Pro final release gate R1 is preserved at `PRO_REVIEW_OUTPUT_SPRINT15_FINAL_RELEASE_GATE_20260503_193048/Sprint15_Final_Release_Gate_GPT_Pro.md`; it failed 8/10 with VISUAL REVIEW PASS because the review bundle was not independently buildable, legacy SAF export could write index-only JSON-LD for multiple sources, and missing-source annotation rows displayed `Source no longer in Library` instead of the stored source title.
- GPT Pro final release gate R2 is preserved at `PRO_REVIEW_OUTPUT_SPRINT15_FINAL_RELEASE_GATE_R2_20260503_2008/Sprint15_Final_Release_Gate_R2_GPT_Pro.md`; it failed 9/10 with VISUAL REVIEW PASS because local `file://` directory exports wrote per-source JSON-LD files but omitted the index file.
- GPT Pro final release gate R3 is preserved at `PRO_REVIEW_OUTPUT_SPRINT15_FINAL_RELEASE_GATE_R3_20260503_2043/Sprint15_Final_Release_Gate_R3_GPT_Pro.md`; SCORE 10/10, VERDICT PASS, VISUAL REVIEW PASS.
- Final release validation after the version bump passed `./gradlew testDebugUnitTest connectedDebugAndroidTest` with 86/86 connected Android tests.
- Final debug APK validation passed build, badging, debug signature verification, emulator install smoke, and installed version checks for `versionCode=12`, `versionName=0.8.0-alpha`.

## Operating Rule

After each slice, create an auditable review bundle, send it to GPT Pro, set a 10-minute heartbeat harvest, and iterate until that slice receives SCORE 10/10, VERDICT PASS, and VISUAL REVIEW PASS. Do not publish the Sprint 15 APK until the final release gate passes the same standard.
