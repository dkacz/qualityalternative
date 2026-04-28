# Sprint 12 Release Notes

Status: final. Slice 12.4 R3 final validation passed, and GPT Pro release-readiness review returned literal `SCORE: 10/10` and `VERDICT: PASS`.

## Scope

Sprint 12 improves local content management and unfinished-reading continuity inside the Android-first MVP. It stays inside the PRD boundary of finite replacement at the moment of impulse and does not turn the app into a general read-later manager or browsing feed.

## Shipped Behavior

### Manage and delete user content

- Library now supports manage mode for user-added links and imported documents.
- Users can multi-select their own content and delete it from the local inventory.
- Editorial starter-pack rows remain read-only for destructive actions.
- Deleting a user item removes it from future recommendations.
- Deleting a user item also clears any saved per-item priority and saved reading progress.
- Deleting an imported document attempts to release persisted Android read permission for that document URI.

### Streamlined add flow

- Add flow copy is content-first instead of link-first.
- Users can import more than one document in a single batch.
- Unsupported files are rejected without aborting the whole batch.
- Persisted Android read permission is kept only for successfully added documents.

### Reading-time defaults

- Imported Markdown and EPUB files now receive bounded session estimates derived from extracted text.
- PDF imports keep metadata/default duration behavior for MVP.
- Link add flow continues to allow an editable duration with a sane default.
- Session estimates stay constrained to the product's 3-20 minute intervention range.

### Priority at add

- Add-link and add-document flows both allow marking the item as priority before save.
- Priority-at-add uses the same stored priority set as later Library prioritization.
- Priority-at-add analytics remain distinct from later Library priority toggles.

### Continue reading and unfinished-first ranking

- Reader progress is saved per content item.
- Home and Library expose continue paths for unfinished content.
- Reader restore brings the user back near the saved paragraph position.
- Manual reading from Library can save progress and complete content without an intervention session.
- Unfinished content now has absolute primary priority during interventions unless completed or unavailable.

## Analytics

Sprint 12 adds or verifies local-first analytics for:

- `USER_CONTENT_DELETED`
- `BATCH_DOCUMENT_IMPORT_ATTEMPTED`
- `BATCH_DOCUMENT_IMPORT_COMPLETED`
- `READING_TIME_ESTIMATE_APPLIED`
- `PRIORITY_SET_DURING_ADD`
- `READING_PROGRESS_SAVED`
- `MANUAL_CONTINUE_STARTED`
- `UNFINISHED_CONTENT_RECOMMENDED_AS_PRIMARY`

## Validation Expectations

Sprint 12 is only release-ready when all of the following are true:

- Android unit tests pass: 188 tests, 0 failures, 0 errors, 0 skipped.
- Connected Android tests pass on the emulator: 64 tests, 0 failures, 0 errors, 0 skipped.
- Final Sprint 12 light/dark journey screenshots are captured and visually inspected across add, import, manage, reader, continue, Library unfinished, and intervention states: 27 PNGs plus contact sheet.
- GPT Pro returned literal `SCORE: 10/10` and `VERDICT: PASS` for the final Slice 12.4 R3 review bundle.

## Release

- App version: `0.6.0-alpha`
- Release tag: `v0.6.0-content-management-alpha`
- Final GPT Pro audit: `docs/visual-qa/2026-04-28-sprint12-content-management/test-evidence/gpt_pro_final_audit.md`
- Final validation summary: `docs/visual-qa/2026-04-28-sprint12-content-management/VALIDATION_RESULTS.md`
