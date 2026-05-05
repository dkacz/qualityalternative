# Sprint 16: Portable Profile

Status: final release gate in progress

## Goal

Implement account-like portability without a Quality Alternative server account. The user should be able to keep a local profile, export/import it as a versioned JSON file, and optionally autosave that profile to a user-owned destination.

## Product Constraints

- No Quality Alternative backend account.
- No server-side authentication.
- No exported Google tokens or OAuth grants.
- No opaque database backup as the product contract.
- Local-first behavior must work without Google Drive.
- Imported document metadata must not pretend to transfer document files.

## Slice Plan

### Slice 16.0: Contract And Schema

Deliverables:

- PRD Portable Profile requirement.
- Portable Profile schema document.
- Sprint plan and acceptance criteria.

Review gate:

- GPT Pro review of product contract, schema completeness, privacy constraints, and bundle hygiene.
- Required result before implementation slices: `SCORE: 10/10`, `VERDICT: PASS`.
- R1 GPT Pro review returned `SCORE: 7/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R2 hardens the schema with concrete field shapes, enum domains, document fingerprints, no raw URI export, explicit merge/replace semantics, and Drive/autosave reactivation rules before implementation proceeds.
- R2 GPT Pro review returned `SCORE: 8/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R3 adds stable content-id format/generation/collision rules, formal `warnings[]` schema, deterministic field-level merge winners for links/documents/progress/settings, and exact text fingerprint normalization.
- R4 GPT Pro review returned `SCORE: 9/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; Portable Profile contract blockers were closed, but the sprint order incorrectly placed final release before the newly required adaptive reader pagination slice. R5 moves adaptive pagination before final UX/hardening/release.

### Slice 16.1: Local Profile Identity And Settings Export

Deliverables:

- Local profile id persisted on device.
- Serializable profile model.
- Settings-only export to JSON.
- Unit tests for schema, exclusions, and version metadata.
- No runtime model work begins until Slice 16.0 schema review passes at 10/10.

Review gate:

- GPT Pro code review and Settings visual review where applicable.
- If Settings UI is touched, screenshot Portable profile entry, export action, export success, and export failure.

### Slice 16.2: Settings Import And Safe Restore

Deliverables:

- JSON import validation.
- Default merge mode.
- Explicit replace mode if UI allows it.
- No partial mutation on invalid import.
- User-visible import success/failure status.

Review gate:

- GPT Pro code, E2E, and visual review.
- Screenshot import entry, merge preview, replace confirmation, invalid import, unsupported future schema, and import success.
- R10 GPT Pro review returned `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`; Slice 16.2 is complete.

### Slice 16.3: Portable Library And Reading State

Deliverables:

- User links in profile export/import.
- User document metadata in profile export/import.
- Reading progress and completed/reactivated state in profile export/import.
- Missing document files clearly marked unavailable until reattached.

Review gate:

- GPT Pro code, E2E, and visual review.
- Current implementation exports and imports saved links, user-document metadata, active reading progress, and completed/reactivated state; imported documents are marked missing until the source file is reattached.
- R1 GPT Pro review returned `SCORE: 6/10`, `VERDICT: FAIL`, `VISUAL REVIEW: FAIL`; R2 removes the active open action from missing imported documents, retains their progress as dormant state, prevents merge from replacing local available library records with synthetic missing records, and adds rollback for mid-apply import failures.
- R2 GPT Pro review returned `SCORE: 9/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R3 applies unsafe-value filtering to portable link/document descriptions and proves raw URI/email/OAuth/token/provider text cannot be exported or imported through descriptions.
- R3 GPT Pro review returned `SCORE: 8/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R4 additionally rejects opaque provider/file IDs in portable descriptions, rejects unsafe portable link URLs, omits unsafe saved links from export, and proves available local documents are not overwritten by imported missing-document placeholders.
- R4 GPT Pro review returned `SCORE: 8/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R5 rejects encoded raw URI/provider payloads inside otherwise valid URLs, validates imported `mimeType` as portable data, and prevents settings references from pointing at unsafe library records omitted during export.
- R5 GPT Pro review returned `SCORE: 9/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R6 aligns portable user-link and user-document title export with the schema/import limit of 200 characters, while preserving the separate 240-character annotation sidecar source-title limit.
- R6 GPT Pro review returned `SCORE: 9/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R7 aligns saved-link `sourceLabel` export and DTO validation with the schema/import limit of 120 characters while preserving broader 240-character document source hints.
- R7 GPT Pro review returned `SCORE: 9/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R8 recursively decodes saved-link URL path/query/fragment surfaces before privacy scanning so nested encoded raw URI/account payloads cannot be exported or imported.
- R8 GPT Pro review returned `SCORE: 9/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R9 treats URL decode-depth cap exhaustion as non-portable and adds six-deep encoded raw-URI regression coverage.
- R9 GPT Pro review returned `SCORE: 9/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R10 rejects short Android SAF/provider document IDs and storage/download file payloads embedded inside saved-link `https://` URL surfaces.
- R10 GPT Pro review returned `SCORE: 9/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R11 rejects no-slash short provider-internal ids such as `image:3952`, `audio:1234`, and `msf:29` embedded inside saved-link `https://` URL surfaces.
- R11 GPT Pro review returned `SCORE: 9/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R12 rejects decoded `storage/` and `sdcard/` local-path payloads in saved-link URL surfaces regardless of file extension.
- R13 GPT Pro review returned `SCORE: 8/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R14 treats malformed nested URL-decoding failures as non-portable and adds malformed nested local-path/raw-URI regression coverage.
- R14 GPT Pro review returned `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`; Slice 16.3 is complete.

### Slice 16.4: Portable Profile Autosave Destination

Deliverables:

- Optional profile autosave destination in Settings.
- Local or Android document-provider export path.
- Optional Google Drive file sync if it can reuse the existing explicit authorization model safely.
- Profile autosave failures do not block app use.
- Current implementation uses explicit Android folder/document-provider selection and writes `quality-alternative-profile.json`; imported autosave metadata stays informational until the current device user selects a destination.
- Google Drive profile autosave is intentionally not activated in this slice because the existing authorization is annotation-specific and Portable Profile must not reuse Drive access without a separate explicit profile destination choice.

Review gate:

- GPT Pro code, Drive/privacy, and visual review.
- R5 GPT Pro review returned `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`; Slice 16.4 is complete.

### Slice 16.5: Adaptive Reader Pagination

Context:

- User reported that current paginated reader can leave too much empty space on a real phone.
- The reader should remain Kindle-like: no active vertical scrolling, no persistent Previous/Next/Done controls, tap/swipe navigation still works.
- Android Back should exit the active reader to the prior app screen, after closing TOC or annotation overlays. Previous-page navigation should be a reader gesture or a very small left-edge tap zone, not the system Back action.
- Reader font size must be adjustable in Quality Alternative settings and exported/imported through Portable Profile. The user must not have to change Android system font size just to tune the reader.
- Annotation range selection is part of this reader pass but must not be implemented during earlier Portable Profile slices: the current Start/End earlier/later concept is acceptable only if it becomes compact, icon-first, and works reliably.

Deliverables:

- Replace fixed paragraph/weight pagination with viewport-aware pagination that measures usable reader height.
- Add an app-level reader font-size setting backed by the Portable Profile `readerFontScale` field.
- Account for actual device dimensions and the app-level reader font-size setting when calculating pages; system font scale may be observed as an accessibility input but cannot be the only control.
- Keep pages full enough to avoid large avoidable blank regions while preventing clipping/overflow.
- Preserve TOC jumps, reading progress restoration, annotations, tap-to-next, swipe left/right, and Back-to-exit behavior.
- Previous-page navigation must remain available through reader-specific gestures and/or a very small left-edge tap zone, without preventing the user from exiting the reader through Android Back.
- Replace verbose annotation range buttons (`Start earlier`, `Start later`, `End earlier`, `End later`) with minimalist icon controls that still expose accessible labels and stable test tags.
- Fix annotation range adjustment so it works within a single selected sentence at token/phrase granularity; controls must not appear broken just because the initial long-press selected one sentence.
- Solve cross-page annotation selection explicitly: start/end anchors must be source-based, the selected quote must be allowed to span paginated display pages without enabling vertical scroll, and page navigation must not corrupt the selected range.
- Add tests for at least small/large viewport and small/default/large app-level reader-font behavior.
- Add tests for Back-to-exit, previous-page reader gesture/left-edge behavior, minimalist annotation controls, single-sentence range refinement, and cross-page annotation range persistence.
- Add visual evidence for multiple viewport/font-size combinations plus the minimalist annotation overlay and a cross-page selection case.

Review gate:

- GPT Pro code and visual review before final release gate.
- Required result before release: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`.
- R3 GPT Pro review returned `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`; Slice 16.5 is complete.

### Slice 16.6: Final UX, Hardening, Release

Deliverables:

- Minimal Settings UX for Portable Profile.
- E2E export/import on clean app state.
- Visual screenshots for Settings, export success, import success, invalid import, missing-document state, and adaptive reader pagination on representative viewport/font-size combinations.
- Full release validation.
- GitHub APK release.
- Current hardening minimizes Portable Profile copy, adds a live export-success visual state, adds clean-state export/import restore coverage, and assembles final release-gate evidence.
- R1 final release gate returned `SCORE: 8/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; blockers were merge import phase-two failure visibility and unsafe `settings.selectedPackIds` portability.
- R2 fixes wrap merge import apply failures into the same visible rollback/failure state as replace import and validate/filter `selectedPackIds` as safe portable pack ids before import, local settings restore, and export.
- R2 final release gate returned `SCORE: 8/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R1 blockers were accepted as fixed, but release remained blocked by the missing `DOCUMENT_FINGERPRINT_UNVERIFIED` export warning plus incomplete/dirty review bundle hygiene. R3 adds the warning regression and ships a clean, reproducible review packet.
- R3 final release gate returned `SCORE: 8/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R1/R2 blockers and package hygiene were accepted as fixed, but release remained blocked by deterministic portable user-link/document ids and missing content-id-versus-secondary-key conflict enforcement before mutation.
- R4 aligns generated user-link and user-document ids with random UUID v4 schema requirements, removes derived fallback portable ids from export, enforces `CONTENT_ID_SECONDARY_KEY_CONFLICT` before merge mutation, and only imports reading progress for actually accepted portable content ids.
- R4 final release gate returned `SCORE: 7/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R5 maps same-URL link imports and verified-document-fingerprint imports to existing local content ids, warns when local library values are retained, preflights links and documents together before any mutation, migrates stored user-document fingerprints in Room schema v11, and refreshes stale visual tests for UUID user content ids plus Android Back-to-exit reader behavior.
- R5 final release gate returned `SCORE: 8/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R6 exports verified `SHA256_BYTES` document fingerprints end to end by persisting local document byte size in Room schema v12, adds a cross-device export/import reconciliation test for document progress mapping, and fixes the review bundle to include `source/app/build.gradle.kts`.
- R6 final release gate returned `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`; Sprint 16 is approved for Android version bump, debug APK build/signature verification, and GitHub release.

Review gate:

- GPT Pro final release gate with visual review after all Portable Profile and adaptive reader pagination slices pass.
- Required result: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`.

## Definition Of Done

- Portable Profile works without a server.
- Exported profile is JSON and human-auditable.
- Import validates before mutation.
- No credentials or authorization grants are exported.
- Local-first behavior remains usable without Drive.
- Tests cover schema, validation, merge behavior, and E2E restore.
- Adaptive reader pagination fills real device viewports without clipping text or reintroducing vertical active reading scroll.
- GPT Pro passes every slice gate to 10/10 before the next slice.
- Final APK is published to GitHub Releases with a changelog.
