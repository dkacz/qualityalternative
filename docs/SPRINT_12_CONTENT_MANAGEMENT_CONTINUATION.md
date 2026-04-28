# Sprint 12: Content Management and Continue Reading

Status: complete. Slice 12.0, Slice 12.1, Slice 12.2, Slice 12.3, and final Slice 12.4 R3 are approved with literal GPT Pro `SCORE: 10/10` and `VERDICT: PASS`.

Review history:

- 2026-04-27 GPT Pro review R1 returned `SCORE: 8/10` and `VERDICT: REVISE`; this revision tightens progress cleanup tests, analytics gates, URI permission tests, reading-time bounds, and future bundle completeness.
- 2026-04-27 GPT Pro review R2 returned `SCORE: 9/10` and `VERDICT: REVISE`; all contract issues were fixed, but the review bundle still was not canonical enough as a model for implementation-slice review. R3 uses a full Android source/test/schemas/build-file review bundle for Slice 12.0.
- 2026-04-27 GPT Pro review R3 returned `SCORE: 8/10` and `VERDICT: REVISE`; it found one remaining Slice 12.1 test gap for non-destructive editorial rows and one bundle-completeness issue around Android resources/assets/docs/wrapper files. R4 fixes both before implementation.
- 2026-04-27 GPT Pro review R4 returned `SCORE: 9/10` and `VERDICT: REVISE`; it found one remaining Android privacy gap: deleting imported documents must release persisted read URI permission. R5 fixes this before implementation.
- 2026-04-27 GPT Pro review R5 returned `SCORE: 10/10` and `VERDICT: PASS`; Slice 12.0 is approved and implementation moved to Slice 12.1.
- 2026-04-27 Slice 12.1 implementation added user-content deletion APIs, Library manage mode, user-only multi-select deletion, priority cleanup, document URI permission release, deletion analytics, unit tests, Android tests, and visual screenshots. The slice is awaiting GPT Pro review and is not accepted until GPT Pro returns literal `SCORE: 10/10` and `VERDICT: PASS`.
- 2026-04-27 Slice 12.1 GPT Pro review R1 returned `SCORE: 9/10` and `VERDICT: REVISE`; the only material blocker was the post-delete snackbar covering bottom navigation in the dark deleted-state screenshot. R2 pads snackbar placement above the tab bar on tabbed screens and ships regenerated screenshots plus raw test reports.
- 2026-04-27 Slice 12.1 GPT Pro review R2 returned `SCORE: 10/10` and `VERDICT: PASS`; implementation moved to Slice 12.2.
- 2026-04-27 Slice 12.2 implementation added content-first add copy, multi-file document import, Markdown/EPUB/PDF reading-time estimates, priority-at-add, distinct analytics, unit tests, full connected Android test coverage, and visual screenshots. The slice is awaiting GPT Pro review and is not accepted until GPT Pro returns literal `SCORE: 10/10` and `VERDICT: PASS`.
- 2026-04-27 Slice 12.2 GPT Pro review R1 returned `SCORE: 8/10` and `VERDICT: REVISE`; blockers were incomplete light/dark visual gate coverage and insufficient tests for the actual Markdown/EPUB import-estimation path. R2 adds the missing screenshot pairs and tests the shared import-estimation path for short, normal, and very long Markdown/EPUB content.
- 2026-04-27 Slice 12.2 GPT Pro review R2 returned `SCORE: 9/10` and `VERDICT: REVISE`; visual coverage passed, but the review required a testable URI-to-import-candidate layer used by both the picker and single-file helper. R3 adds `DocumentImportCandidateFactory`, routes both import entry points through it, and tests Markdown/EPUB/PDF/unsupported candidate construction directly.
- 2026-04-27 Slice 12.2 GPT Pro review R3 returned `SCORE: 10/10` and `VERDICT: PASS`; implementation moved to Slice 12.3.
- 2026-04-27 Slice 12.3 implementation added persisted per-content reading progress, restored-reader continue paths, Home continue card, Library unfinished filtering/manage presentation, unfinished-first primary ranking, delete cleanup for saved progress, no-intervention manual completion, analytics for progress/continue/unfinished-primary events, unit tests, Android E2E tests, and light/dark visual screenshots. The slice is awaiting GPT Pro review and is not accepted until GPT Pro returns literal `SCORE: 10/10` and `VERDICT: PASS`.
- 2026-04-27 Slice 12.3 GPT Pro review R1 returned `SCORE: 7/10` and `VERDICT: BLOCK`; blockers were a non-reproducible review packet, missing engine-level unavailable filtering, insufficient UI-level proof for Reader scroll progress/no first-render autosave, incomplete UI proof for Library manual completion, and missing dark Reader start/mid screenshots. R2 fixes each blocker with a reproducible Gradle bundle, raw logs/reports, engine filtering and test coverage, UI-driven Android tests, and expanded screenshots.
- 2026-04-27 Slice 12.3 GPT Pro review R2 returned `SCORE: 9/10` and `VERDICT: REVISE`; the remaining issue was automated restored-position coverage. R3 adds a debug-only Reader semantics marker for the actual first visible paragraph index and tightens the Android UI test to prove the continued Reader restores near the saved scroll position instead of only showing restored percentage text.
- 2026-04-28 Slice 12.3 GPT Pro review R3 returned `SCORE: 10/10` and `VERDICT: PASS`; implementation moved to Slice 12.4 final E2E and release-readiness.
- 2026-04-28 Slice 12.4 implementation added a final light/dark Sprint 12 journey screenshot harness, final release notes, full Android unit plus connected validation, and a release-readiness evidence bundle. The sprint is awaiting the final GPT Pro gate and is not ready until that review returns literal `SCORE: 10/10` and `VERDICT: PASS`.
- 2026-04-28 Slice 12.4 GPT Pro review R1 returned `SCORE: 8/10` and `VERDICT: REVISE`; blockers were incomplete final dark-mode add/import/manage proof, stale snackbar contamination in batch-import screenshots, and cached unit-test evidence. R2 fixes the snackbar clearing path, expands final screenshots to 25 PNGs covering the full light/dark journey, and ships a fresh `testDebugUnitTest --rerun-tasks` plus connected Android validation run.
- 2026-04-28 Slice 12.4 GPT Pro review R2 returned `SCORE: 9/10` and `VERDICT: REVISE`; the only remaining issue was missing final dark Reader start/mid screenshots. R3 adds `26_reader_start_dark.png` and `27_reader_mid_dark.png`, regenerates the final contact sheet, and ships another fresh `testDebugUnitTest --rerun-tasks` plus connected Android validation run.
- 2026-04-28 Slice 12.4 GPT Pro review R3 returned `SCORE: 10/10` and `VERDICT: PASS`; Sprint 12 is approved for release as `v0.6.0-content-management-alpha`.

## User Backlog Input

This sprint starts from the following requested backlog features:

- Delete content.
- Select more than one file.
- Auto-calculate reading time.
- Set priority while adding.
- Streamline adding content instead of the current split/link-first wording.
- Make the full content list easy to manage.
- Give absolute priority to unfinished content and save reading progress.
- Let the user read or continue content even without an intervention.

## Product Boundary

Sprint 12 stays inside the Android-first MVP thesis: quality replacement at the moment of impulse.

It may improve the local content queue because `PRD.md` already includes:

- `FR3 Replacement Source Setup`: editorial starter packs, user-added links, and user-owned PDF/Markdown/EPUB documents.
- `FR4 Content Item Model`: item status and availability.
- `FR5 Recommendation Selection`: priority and history-aware ranking.
- `FR8 Reader or Handoff Experience`: a simple reader/handoff path.
- `FR12 Analytics Instrumentation`: event logging for substitution behavior.

It must not become:

- a full read-later manager,
- an archive/annotation product,
- an infinite discovery feed,
- a background crawler or web reader,
- a cross-device sync project,
- a hard-block or shame-based progress system.

## Sprint Goal

Make the user's own replacement inventory manageable enough that unfinished high-quality content reliably comes back at the moment of impulse, while preserving the calm finite replacement flow.

## Acceptance Contract

### Content Management

- User can delete user-added links.
- User can delete user-imported documents.
- Deleting user content removes it from future recommendations.
- Deleting user content also clears its individual priority flag and saved reading progress.
- Deleting a user-imported document releases any persisted Android read permission for that document URI when such permission exists.
- Editorial starter-pack content is not destructively deleted in this sprint; it may only be opened or prioritized.
- The library surface makes all content easier to scan, filter, and manage without becoming a feed.

### Multi-File Import

- Android document import supports selecting more than one PDF, Markdown, or EPUB file.
- The app should not force one full form per file when the user selects multiple files.
- Each imported file becomes a private user document with local metadata.
- Unsupported files are rejected cleanly without aborting the whole batch.
- The app preserves Android read permission only for successfully saved files.

### Reading-Time Estimation

- Imported Markdown and EPUB files get an estimated reading time from extracted text.
- PDF files use file metadata/default duration because the app does not parse PDF text in MVP.
- Manually added links keep an editable estimate, but the form should provide a sane default.
- Reading-time estimates are session estimates, not raw full-document lengths.
- Markdown/EPUB estimation must align with PRD duration buckets: minimum 3 minutes, preferred bucket targets of 3-5, 5-10, and 10-20 minutes, and maximum intervention estimate of 20 minutes.
- Very long Markdown/EPUB files may still be continued across sessions through saved progress, but they must not create a 60-120 minute primary replacement estimate.
- If future work needs raw full-document length, it must be stored separately from the intervention session estimate.

### Priority While Adding

- Add-link and add-document flows allow the user to mark the new item as priority before saving.
- Priority-at-add is persisted in the same individual priority set used by recommendation ranking.
- Priority-at-add records analytics distinct from later priority toggles in Library.

### Continue Reading

- Reader progress is saved per content item.
- Reader progress survives leaving and reopening the reader.
- Library/Home exposes a clear "continue" path for unfinished content.
- Unfinished content has absolute ranking priority over normal content during interventions.
- Completed content still stays out of primary recommendation slots as before.
- Manual reading from Library can save progress and complete content even without an intervention.
- Deleting a user item with saved progress removes that progress and removes the item from Home/Library continue paths and unfinished-first ranking.

## Slice Plan and Review Gates

### Slice 12.0: Sprint Contract

Output:

- This sprint document.
- GPT Pro review bundle focused on product scope, PRD fit, and slice order.
- Future implementation review bundles must include canonical source and test dependencies for the slice, not only excerpts. At minimum, bundles that touch Android behavior must include the relevant domain models, analytics models and Room analytics files, validators, Room entities/DAOs, repository implementations, ViewModel/UI files, test fakes or fixtures, and any PRD-referenced policy documents needed to audit the claim.

Pro gate:

- GPT Pro must return `SCORE: 10/10` and `VERDICT: PASS`.
- Any lower score requires a fix before implementation proceeds.

### Slice 12.1: Delete and Manage Content List

Output:

- Repository deletion APIs for user links and user documents.
- ViewModel state/actions for single and multi-select management.
- Library UI for manage mode, selected count, delete selected, and safe empty states.
- Unit and Android tests for deletion, priority cleanup, recommendation exclusion, and delete analytics.
- If progress storage is not implemented yet in Slice 12.1, deletion-progress cleanup is explicitly deferred to Slice 12.3 and becomes release-blocking there.
- Unit and Android tests must also prove editorial starter-pack rows are not destructively selectable or deletable in manage mode.
- Attempted deletion of editorial content must not remove it from Library, inventory, or recommendations, and must not emit a user-content-deleted analytics event.
- Editorial open and priority behavior must remain available while destructive deletion remains user-content-only.
- Deleting a user-imported document must attempt to release persisted Android read permission for that document URI when such permission exists.
- Automated tests must use a fake URI-permission releaser to prove document deletion calls release for deleted documents, does not call release for user links or editorial rows, and still emits only the appropriate user-document deletion analytics.

Visual gate:

- Library manage mode light/dark screenshots.
- Delete confirmation or deletion state screenshot.

Pro gate:

- GPT Pro must return `SCORE: 10/10` and `VERDICT: PASS` for slice scope and screenshots.

### Slice 12.2: Streamlined Add, Multi-File Import, Reading Time, Priority-at-Add

Output:

- Add flow copy changed from link-first/import-instead to content-first.
- Multi-file document picker handling.
- Batch document import path with per-file validation.
- Reading-time estimator for Markdown/EPUB text with 3-20 minute bounded session estimates aligned to PRD buckets.
- Priority-at-add controls for links and documents.
- Unit and Android tests for batch import, estimate bounds on short/normal/very long Markdown and EPUB text, priority persistence, unsupported-file handling, and success-only read-permission persistence.
- Batch import tests must cover mixed valid and unsupported selections: valid PDF/Markdown/EPUB files continue to save, unsupported files are rejected without aborting the batch, and Android persistable read permission is requested only for successfully added files.
- Analytics tests must verify batch-import-attempted, batch-import-completed, reading-time-estimate-applied, and priority-set-during-add events, with priority-at-add distinct from Library priority toggles.

Visual gate:

- Add content screen light/dark.
- Batch-import result light/dark.
- Priority-at-add state light/dark.

Pro gate:

- GPT Pro must return `SCORE: 10/10` and `VERDICT: PASS`.

### Slice 12.3: Saved Progress, Continue Reading, Unfinished-First Ranking

Output:

- Persistent per-content reading progress.
- Reader restores progress and records progress updates.
- Home/Library continue-reading path.
- Recommendation engine gives absolute primary priority to unfinished content unless it is completed/unavailable.
- Manual reading sessions from Library can complete content without an intervention.
- Unit and Android tests for progress persistence, continue path, ranking priority, progress cleanup on delete, and manual no-intervention Library reading.
- Manual reading tests must prove that opening unfinished content from Library without an intervention saves progress, restores progress, and can complete the item without intervention/session provenance.
- Delete cleanup tests must prove that a user link and a user document with saved progress lose that progress after deletion and no longer appear in continue paths or unfinished-first ranking.
- Analytics tests must verify reading-progress-saved, manual-continue-started, and unfinished-content-recommended-as-primary events.

Visual gate:

- Reader start/mid/continued screenshots.
- Home continue card screenshot.
- Library unfinished filter/manage screenshot.
- Intervention showing unfinished-content priority.

Pro gate:

- GPT Pro must return `SCORE: 10/10` and `VERDICT: PASS`.

### Slice 12.4: Final E2E and Release Readiness

Output:

- Full Android unit test pass.
- Connected Android E2E pass on emulator.
- Visual screenshot set for the full Sprint 12 journey in light and dark mode, including add flow, priority-at-add, batch import, Library manage, reader start/mid, continue paths, unfinished Library state, and unfinished-first intervention.
- Final docs and validation results.

Final Pro gate:

- GPT Pro must return `SCORE: 10/10` and `VERDICT: PASS`.
- Sprint is not ready until this final visual/e2e review reaches 10/10.

## Analytics Requirements

Sprint 12 must add or reuse analytics for:

- user content deleted,
- batch document import attempted,
- batch document import completed,
- reading-time estimate applied,
- priority set during add,
- reading progress saved,
- manual continue started,
- unfinished content recommended as primary.

Analytics should remain local-first and should not add any network dependency.

## Non-Goals

- No RSS, newsletters, browser extension, cloud sync, or account system.
- No PDF text extraction in this sprint.
- No destructive deletion of shipped editorial starter-pack rows.
- No full archive, folders, tags beyond existing topic chips, annotations, highlights, or search.
- No open-web scraping or automatic web article text extraction.

## Definition of Done

The sprint is done only when:

- every slice above has a passing automated test set,
- every slice visual surface is captured and inspected,
- GPT Pro returns `SCORE: 10/10` and `VERDICT: PASS` after each slice,
- the final full visual/e2e review returns `SCORE: 10/10` and `VERDICT: PASS`,
- documentation and release notes accurately describe the shipped behavior.
