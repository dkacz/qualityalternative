# Sprint 16: Account Light Local Profile

Status: started

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

- PRD Account Light requirement.
- Account Light profile schema document.
- Sprint plan and acceptance criteria.

Review gate:

- GPT Pro review of product contract, schema completeness, privacy constraints, and bundle hygiene.
- Required result before implementation slices: `SCORE: 10/10`, `VERDICT: PASS`.
- R1 GPT Pro review returned `SCORE: 7/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R2 hardens the schema with concrete field shapes, enum domains, document fingerprints, no raw URI export, explicit merge/replace semantics, and Drive/autosave reactivation rules before implementation proceeds.
- R2 GPT Pro review returned `SCORE: 8/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; R3 adds stable content-id format/generation/collision rules, formal `warnings[]` schema, deterministic field-level merge winners for links/documents/progress/settings, and exact text fingerprint normalization.
- R4 GPT Pro review returned `SCORE: 9/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`; Account Light contract blockers were closed, but the sprint order incorrectly placed final release before the newly required adaptive reader pagination slice. R5 moves adaptive pagination before final UX/hardening/release.

### Slice 16.1: Local Profile Identity And Settings Export

Deliverables:

- Local profile id persisted on device.
- Serializable profile model.
- Settings-only export to JSON.
- Unit tests for schema, exclusions, and version metadata.
- No runtime model work begins until Slice 16.0 schema review passes at 10/10.

Review gate:

- GPT Pro code review and Settings visual review where applicable.
- If Settings UI is touched, screenshot Account Light entry, export action, export success, and export failure.

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

### Slice 16.3: Portable Library And Reading State

Deliverables:

- User links in profile export/import.
- User document metadata in profile export/import.
- Reading progress and completed/reactivated state in profile export/import.
- Missing document files clearly marked unavailable until reattached.

Review gate:

- GPT Pro code, E2E, and visual review.

### Slice 16.4: Account Light Autosave Destination

Deliverables:

- Optional profile autosave destination in Settings.
- Local or Android document-provider export path.
- Optional Google Drive file sync if it can reuse the existing explicit authorization model safely.
- Profile autosave failures do not block app use.

Review gate:

- GPT Pro code, Drive/privacy, and visual review.

### Slice 16.5: Adaptive Reader Pagination

Context:

- User reported that current paginated reader can leave too much empty space on a real phone.
- The reader should remain Kindle-like: no active vertical scrolling, no persistent Previous/Next/Done controls, tap/swipe navigation still works.
- Reader font size must be adjustable in Quality Alternative settings and exported/imported through Account Light. The user must not have to change Android system font size just to tune the reader.

Deliverables:

- Replace fixed paragraph/weight pagination with viewport-aware pagination that measures usable reader height.
- Add an app-level reader font-size setting backed by the Account Light `readerFontScale` field.
- Account for actual device dimensions and the app-level reader font-size setting when calculating pages; system font scale may be observed as an accessibility input but cannot be the only control.
- Keep pages full enough to avoid large avoidable blank regions while preventing clipping/overflow.
- Preserve TOC jumps, reading progress restoration, annotations, tap-to-next, swipe left/right, and Android Back-to-previous-page behavior.
- Add tests for at least small/large viewport and small/default/large app-level reader-font behavior.
- Add visual evidence for multiple viewport/font-size combinations.

Review gate:

- GPT Pro code and visual review before final release gate.
- Required result before release: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`.

### Slice 16.6: Final UX, Hardening, Release

Deliverables:

- Minimal Settings UX for Account Light.
- E2E export/import on clean app state.
- Visual screenshots for Settings, export success, import success, invalid import, missing-document state, and adaptive reader pagination on representative viewport/font-size combinations.
- Full release validation.
- GitHub APK release.

Review gate:

- GPT Pro final release gate with visual review after all Account Light and adaptive reader pagination slices pass.
- Required result: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`.

## Definition Of Done

- Account Light works without a server.
- Exported profile is JSON and human-auditable.
- Import validates before mutation.
- No credentials or authorization grants are exported.
- Local-first behavior remains usable without Drive.
- Tests cover schema, validation, merge behavior, and E2E restore.
- Adaptive reader pagination fills real device viewports without clipping text or reintroducing vertical active reading scroll.
- GPT Pro passes every slice gate to 10/10 before the next slice.
- Final APK is published to GitHub Releases with a changelog.
