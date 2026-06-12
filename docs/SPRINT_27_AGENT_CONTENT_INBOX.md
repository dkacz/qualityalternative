# Sprint 27 - Agent Content Inbox

## Goal

Let a user-controlled agent workflow place private Markdown or EPUB replacement content into a Google Drive Agent Inbox, then let the Android app review, validate, and import accepted packages through the existing user-document model. The sprint finishes only after visual E2E evidence, GPT Pro review at `SCORE 10/10`, and an internal alpha APK.

## Product Constraints

- `PRD.md` remains the source of truth.
- The feature is a private content-ingestion path, not a shared content marketplace.
- Agent-supplied content is `user_private` by default.
- Google Drive access must be explicit, revocable, and scoped to user-owned files.
- The app must not scan the user's whole Drive.
- The app must not silently import or silently prioritize agent-supplied content.
- The review surface must stay finite and must not become a discovery feed.
- The implementation must reuse the existing Markdown/EPUB document import, reader, progress, priority, ranking, and analytics paths where possible.

## PRD Mapping

- `FR3. Replacement Source Setup`: add user-reviewed Google Drive Agent Inbox packages as a replacement source.
- `FR3B. Google Drive Agent Inbox`: implement folder connection, package detection, validation, duplicate handling, visible priority review, and retryable Drive failures.
- `FR4. Content Item Model`: preserve private provenance while keeping raw Drive identifiers out of remote analytics and portable exports.
- `FR5. Recommendation Selection`: use confirmed manifest priority through the existing priority mechanism.
- `FR8. Replacement Session Experience`: imported Markdown and EPUB continue to open in the private reader.
- `FR12. Analytics Instrumentation`: log detection, acceptance, rejection, duplicates, and validation failures without leaking Drive ids or document text.
- `FR13. Portable Profile`: preserve safe metadata only; require reconnecting Drive on a new device.
- `NFR2. Calm Interaction Model`: keep Agent Inbox review finite and outside the intervention flow.
- `NFR4. Local-First Portability And Privacy`: keep cloud access explicit, scoped, and non-portable.

## Agent Package Contract

An Agent Inbox package contains exactly one manifest and one readable content file.

Recommended package layout:

```text
<package-folder>/
  manifest.json
  content.md
```

or:

```text
<package-folder>/
  manifest.json
  content.epub
```

Manifest fields:

| Field | Required | Meaning |
| --- | --- | --- |
| `schemaVersion` | yes | Current value: `1`. |
| `title` | yes | Reader/library title. |
| `topics` | yes | One or more app topic tag names. |
| `contentFile` | yes | Relative file name for the Markdown or EPUB file. |
| `format` | yes | `MARKDOWN` or `EPUB`. |
| `rightsClass` | yes | Must be `USER_PRIVATE` for MVP import. |
| `sourceLabel` | no | Human-readable agent/source label. |
| `description` | no | Short note shown before import. |
| `priority` | no | `normal` or `high`; `high` becomes user-reviewable priority intent. |
| `documentSha256` | no | Expected lowercase SHA-256 of the content file. |
| `createdAt` | no | Agent-created timestamp for display only. |

Rules:

- Unknown fields are ignored.
- Unsupported `schemaVersion`, blank title, missing topics, unsupported format, missing content file, non-`USER_PRIVATE` rights class, and SHA mismatch reject the package.
- Duplicate content by verified file fingerprint is skipped or mapped to the existing local document; it is never imported twice silently.
- Manifest priority is a request, not authority. The import screen must show it and allow the user to remove it before saving.

## Slice Plan

### Slice 27.0 - Contract And Plan

- Patch `PRD.md` with Agent Inbox scope, privacy, analytics, priority, and portability rules.
- Add this sprint plan.
- Confirm `drive.file` limitations from Google Drive documentation and design around app-created/shared files only.

Acceptance:

- `FR3B` exists in `PRD.md`.
- Sprint plan maps every feature slice to PRD requirements.
- Plan states that Google Drive access is explicit, narrow, and user-reviewed.

### Slice 27.1 - Manifest Model And Validation

- Add a parser and validator for `manifest.json`.
- Normalize topic tags, format, priority intent, source label, title, and description.
- Validate schema version, rights class, supported content file, and optional SHA.

Acceptance:

- Unit tests cover valid Markdown, valid EPUB, priority intent, unknown fields, bad schema, no topics, unsupported rights class, unsupported format, missing file, and SHA mismatch.

### Slice 27.2 - Drive Inbox Client

- Add a Drive client that creates or reuses the persisted Agent Inbox folder id, lists app-accessible packages below that explicit folder id, and downloads manifest/content blobs.
- Reuse the existing Google Drive token provider where possible.
- Keep raw folder ids and file ids inside the Drive layer and UI state only.

Acceptance:

- Tests cover folder creation, stored-folder reuse, package listing, manifest download, content download, pagination, transient failures, and no whole-Drive scan.
- The client uses Drive `files.list` with an explicit parent folder query and `trashed = false`.

### Slice 27.3 - Review State And Import

- Add ViewModel state for connected, scanning, review candidates, invalid packages, duplicates, priority intent, accept, reject, and retry.
- Import accepted Markdown/EPUB packages through `UserDocumentRepository.addDocument`.
- Apply confirmed high priority through existing priority content ids.

Acceptance:

- Tests prove accepted packages become user documents, rejected packages do not, duplicates do not create new content, and confirmed priority updates ranking input.

### Slice 27.4 - Settings And Library UI

- Add a finite Agent Inbox section in Settings or Library.
- Show Drive connection state, scan action, detected packages, validation problems, duplicate status, and priority review.
- Keep the intervention flow unchanged except that accepted content may later be recommended.

Acceptance:

- Compose tests cover disconnected, connecting, connected empty, detected package, invalid package, duplicate, priority checked, priority unchecked, import success, and failure states.

### Slice 27.5 - Analytics And Portable Profile

- Add analytics events for detected, accepted, rejected, duplicate, validation failure, scan success, and scan failure.
- Prove remote-safe analytics exclude raw Drive ids, file names, document text, and reversible fingerprints.
- Export only safe Agent Inbox metadata, if any; require reconnect on import.

Acceptance:

- Unit tests inspect remote-safe payloads and profile JSON for denied strings.

### Slice 27.6 - Visual E2E Evidence

- Add visual E2E screenshots for GPT Pro:
  - disconnected Agent Inbox state,
  - connected empty state,
  - detected Markdown package with priority intent,
  - priority accepted only after an explicit operator toggle,
  - invalid and duplicate package states,
  - package removal/rejection from the local review list,
  - library row for imported content,
  - intervention recommending imported content,
  - Markdown reader opened from imported content,
  - EPUB reader smoke for imported private content.

Acceptance:

- Screenshots are written under `evidence/sprint27_agent_content_inbox/`.
- The screenshot test is deterministic and does not require live Google Drive.

## GPT Pro R1 Fix Notes

R1 returned `SCORE: 6/10`, `VERDICT: BLOCK`, `VISUAL REVIEW: REVISE`. R2 addresses the blocking findings by:

- changing manifest `priority: high` from auto-selected to explicit operator opt-in,
- bounding Drive scans to a finite number of packages and files per package with UI truncation copy,
- adding an Agent Inbox disconnect action that clears connection state,
- removing raw private-document SHA-256 values from Portable Profile export,
- computing content SHA during review so duplicates and manifest SHA mismatch are visible before import,
- expanding visual E2E evidence to disconnected, connected-empty, review, accepted priority, invalid/duplicate, library, intervention, Markdown reader, and EPUB reader states.

R2 returned `SCORE: 6/10`, `VERDICT: BLOCK`, `VISUAL REVIEW: PASS`. R3 addresses the remaining implementation findings by:

- storing reviewed content SHA-256 and byte size on the review candidate and rejecting import if the Drive blob changes after review,
- reapplying the 10 MiB content cap immediately before writing imported bytes,
- using a neutral Agent Inbox document display name instead of exporting raw Drive content file names through `sourceLabel`,
- enforcing exactly one `manifest.json` and one Markdown/EPUB content file with no extra files inside the reviewed package,
- bounding `manifest.json` to 64 KiB before parsing, with metadata and downloaded-byte checks.

R3 returned `SCORE: 6/10`, `VERDICT: BLOCK`, `VISUAL REVIEW: PASS`. R4 addresses the remaining implementation findings by:

- making Agent Inbox local document storage content-addressed with the verified content SHA-256, so the same Drive package folder and content file name cannot overwrite or upsert a previous private document with changed bytes,
- deciding duplicate status only from the actual reviewed content SHA-256, never from an unverified manifest-declared SHA before the content file is downloaded and hashed,
- changing the Drive download API to require `maxBytes`, streaming into a bounded buffer, and returning a typed too-large exception for oversized manifest/content responses even when Drive metadata is missing or understated,
- adding a visible `Remove` action for every review candidate, privacy-safe `AGENT_INBOX_CANDIDATE_REJECTED` analytics, ViewModel coverage, and visual evidence of the post-rejection state,
- removing the initial unqualified Drive folder-name search; first connection now creates and persists an app-owned Agent Inbox folder id, while all package/file scans use explicit parent-folder queries,
- gating fixture target resolution with `BuildConfig.DEBUG`, moving fixture activity registration from `src/main` to `src/debug`, and preserving release merged/packaged manifest evidence proving fixture activities are not registered in release,
- adding a debug APK candidate and SHA-256 checksum to the evidence bundle for the internal alpha gate; final publication still waits for GPT Pro `10/10 PASS/PASS`.

R4 review bundle:

- Bundle: `GPT_PRO_REVIEW_BUNDLE_SPRINT27_AGENT_CONTENT_INBOX_R4_20260612.zip`
- Lane: `https://chatgpt.com/c/6a2bdd0d-b5e8-83eb-972d-813bd00130f7`
- Canonical screenshot run: `evidence/sprint27_agent_content_inbox/visual_e2e/sprint27-agent-content-inbox-1781258985204/`
- APK candidate: `evidence/sprint27_agent_content_inbox/apk/quality-alternative-sprint27-agent-content-inbox-debug.apk`
- APK SHA-256: `9eea8cb8be2cca75f34cb86adac894a0dccd6db09c4b8d8c67a24056887e4375`

R4 returned `SCORE: 7/10`, `VERDICT: REVISE`, `VISUAL REVIEW: REVISE`. R5 addresses the remaining findings by:

- converting non-size manifest/content Drive download failures into package-level `DOWNLOAD_UNAVAILABLE` invalid review candidates, while continuing to show other packages from the same scan,
- gating Agent Inbox scan/import on `UserDocumentRepository.observeReady()` so duplicate decisions are not made from an unhydrated local library,
- adding an authoritative `findDocumentByFingerprintSha256` repository/DAO lookup and using it before Agent Inbox import instead of relying on an in-memory document snapshot,
- moving the priority confirmation control to its own full-width row and adding visual assertions for `Accept priority` before toggle and `Priority accepted` after toggle,
- refreshing the canonical screenshot run after the layout fix,
- including connected Android test XML plus a standalone logcat copy in the evidence bundle.

R5 review bundle:

- Bundle: `GPT_PRO_REVIEW_BUNDLE_SPRINT27_AGENT_CONTENT_INBOX_R5_20260612.zip`
- Lane: `https://chatgpt.com/c/6a2be5be-1d60-83eb-b0ba-01b633e2bcd1`
- Canonical screenshot run: `evidence/sprint27_agent_content_inbox/visual_e2e/sprint27-agent-content-inbox-1781261395439/`
- APK candidate: `evidence/sprint27_agent_content_inbox/apk/quality-alternative-sprint27-agent-content-inbox-debug.apk`
- APK SHA-256: `6e14639bf6b4e20a8219f2e6e37f39778ffb6b5a47fdf982dfa72645b9a54802`

R5 returned `SCORE: 8/10`, `VERDICT: BLOCK`, `VISUAL REVIEW: PASS`. R6 addresses the single remaining blocker by:

- adding `UserDocumentRepository.addDocumentIfFingerprintAbsent`, an atomic add-or-duplicate operation for fingerprint-sensitive imports,
- serializing Room user-document writes with a repository-level `Mutex`, so fingerprint lookup and insert cannot race within the app process,
- passing the verified Agent Inbox content SHA-256 and byte size through `UserDocumentDraft` into the stored user document,
- switching `AgentInboxPackageImporter` from separate lookup plus `addDocument` to the atomic repository method while retaining the fast pre-write duplicate check,
- adding a ViewModel single-flight guard before Agent Inbox import work starts,
- converting import-time duplicate results into finite `DUPLICATE` review candidates with visible Remove action instead of leaving the row as `READY`,
- marking same-SHA sibling packages from the same scan as `DUPLICATE` immediately after the first successful import,
- adding tests for concurrent same-SHA package imports, same-scan sibling duplicate state, and atomic-add duplicate state.

R6 review bundle:

- Bundle: `GPT_PRO_REVIEW_BUNDLE_SPRINT27_AGENT_CONTENT_INBOX_R6_20260612.zip`
- Lane: `https://chatgpt.com/c/6a2bed8f-7c34-83ed-a148-9e749cf8a099`
- Canonical screenshot run: `evidence/sprint27_agent_content_inbox/visual_e2e/sprint27-agent-content-inbox-1781263521353/`
- APK candidate: `evidence/sprint27_agent_content_inbox/apk/quality-alternative-sprint27-agent-content-inbox-debug.apk`
- APK SHA-256: `dfd65408e56bf1a6bc5a0be28345b38c0f84cbe27c8486d63b0530981584f1a4`

R6 returned `SCORE: 8/10`, `VERDICT: BLOCK`, `VISUAL REVIEW: PASS`. R7 addresses the single remaining state-transition blocker by:

- converting import-time `INVALID` and `REJECTED` results into finite non-importable `INVALID` review candidates,
- clearing stale `reviewedContentSha256` and `reviewedContentSizeBytes` when import proves the reviewed blob is no longer authoritative,
- clearing any accepted priority state for the failed package,
- converting non-size import download failures into `DOWNLOAD_UNAVAILABLE` invalid candidates instead of leaving the row unchanged,
- adding `LOCAL_IMPORT_REJECTED` for repository/local save rejection so invalid rows have visible cleanup copy,
- preserving the visible Remove path while hiding Import for failed rows through `canImport == false`,
- adding ViewModel tests for changed-after-review, import-time oversize, import download failure, and repository rejection state.

R7 review bundle:

- Bundle: `GPT_PRO_REVIEW_BUNDLE_SPRINT27_AGENT_CONTENT_INBOX_R7_20260612.zip`
- Lane: `https://chatgpt.com/c/6a2bf46d-43bc-83eb-8125-c1002b3ea3dd`
- Canonical screenshot run: `evidence/sprint27_agent_content_inbox/visual_e2e/sprint27-agent-content-inbox-1781265259529/`
- APK candidate: `evidence/sprint27_agent_content_inbox/apk/quality-alternative-sprint27-agent-content-inbox-debug.apk`
- APK SHA-256: `74502b4ddb717e72f13c47b2590c00b510965fda687445129f8b093e9cb501af`

R7 returned `SCORE: 9/10`, `VERDICT: REVISE`, `VISUAL REVIEW: PASS`. R8 addresses the final storage-retention finding by:

- extending `AgentInboxDocumentStore` with `deleteDocument`,
- deleting the stored Agent Inbox file when the post-write repository operation returns `DUPLICATE` or `REJECTED`,
- deleting the stored Agent Inbox file when the post-write repository operation throws,
- implementing guarded deletion in `FileAgentInboxDocumentStore` only for files under the Agent Inbox storage root,
- adding tests proving concurrent same-SHA packages leave only one stored file, repository rejection leaves no stored file, and atomic-add exception leaves no stored file.

R8 review bundle:

- Bundle: `GPT_PRO_REVIEW_BUNDLE_SPRINT27_AGENT_CONTENT_INBOX_R8_20260612.zip`
- Lane: `https://chatgpt.com/c/6a2bfb46-2550-83eb-a06a-889265bc2c57`
- Canonical screenshot run: `evidence/sprint27_agent_content_inbox/visual_e2e/sprint27-agent-content-inbox-1781267009056/`
- APK candidate: `evidence/sprint27_agent_content_inbox/apk/quality-alternative-sprint27-agent-content-inbox-debug.apk`
- APK SHA-256: `3dd2d4be22c40c1bff5481a73c246e0d86cc5506a0cd0d344c82fbd34b0ccc33`

R8 returned `SCORE: 9/10`, `VERDICT: REVISE`, `VISUAL REVIEW: PASS`. R9 addresses the final storage atomicity finding by:

- changing `FileAgentInboxDocumentStore.writeDocument` to write into a scoped temporary file under the Agent Inbox storage root,
- verifying the temporary file SHA-256 before it can become the final stored document,
- moving the verified temporary file into place with `ATOMIC_MOVE`, falling back to `REPLACE_EXISTING` only when the filesystem does not support atomic moves,
- deleting a stale deterministic final file when its bytes do not match the verified reviewed content SHA-256,
- deleting any temporary file in `finally`,
- preserving the existing guarded `deleteDocument` cleanup path for duplicate/rejected/exception post-write results,
- adding a regression test proving a stale mismatching final file is replaced by verified bytes instead of blocking a later valid import.

R9 review bundle:

- Bundle: `GPT_PRO_REVIEW_BUNDLE_SPRINT27_AGENT_CONTENT_INBOX_R9_20260612.zip`
- Lane: `https://chatgpt.com/c/6a2c059d-9764-83ed-b808-4538ad6a3160`
- Canonical screenshot run: `evidence/sprint27_agent_content_inbox/visual_e2e/sprint27-agent-content-inbox-1781269357166/`
- APK candidate: `evidence/sprint27_agent_content_inbox/apk/quality-alternative-sprint27-agent-content-inbox-debug.apk`
- APK SHA-256: `6681e878e34514afde6320de99b4eed7208a10b206898d5b010e82538e2bbd4e`

R9 returned `SCORE: 9/10`, `VERDICT: REVISE`, `VISUAL REVIEW: REVISE`. R10 addresses the visual evidence mismatch by:

- changing the Sprint 27 visual test so screenshots `06` through `09` come from actual `scanAgentInboxDrive` and `importAgentInboxCandidate` calls against a debug-only fake `AgentInboxDriveClient`,
- installing that fake client through a `BuildConfig.DEBUG`-guarded `MainViewModel.setAgentInboxDriveClientForTests` hook instead of mutating production `AppContainer`,
- using Drive-like package fixtures that contain `manifest.json` plus private Markdown/EPUB bytes and verified `documentSha256`,
- clicking `Accept priority` before importing the Markdown package, then verifying the imported model has a content fingerprint and neutral `Agent Inbox document` source label,
- asserting raw Drive content file names and raw package ids are not visible in Library or intervention screenshots,
- capturing Library, intervention, Markdown reader, and EPUB reader states only after the imported `ContentItem`s exist in the user-document repository,
- refreshing connected visual evidence, contact sheet, unit/lint/build evidence, release manifest evidence, and APK checksum after the visual fix.

R10 review bundle:

- Bundle: `GPT_PRO_REVIEW_BUNDLE_SPRINT27_AGENT_CONTENT_INBOX_R10_20260612.zip`
- Lane: `https://chatgpt.com/c/6a2c0f4a-b3e0-83eb-97a2-4762aeb641b2`
- Canonical screenshot run: `evidence/sprint27_agent_content_inbox/visual_e2e/sprint27-agent-content-inbox-1781272063934/`
- APK candidate: `evidence/sprint27_agent_content_inbox/apk/quality-alternative-sprint27-agent-content-inbox-debug.apk`
- APK SHA-256: `9a51ec2a435c8cb8e8a0cdaa8e74212551127a04e57dda08d81115f59d3bf4e8`

R10 returned `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`, with no fresh findings, no bundle gaps, and package hygiene clean enough for release-gate audit.

Final release gate:

- Release tag: `v0.11.15-agent-content-inbox-alpha`
- Android version: `versionCode=31`, `versionName=0.11.15-alpha`
- Final Gradle gate: `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug` PASS
- Final connected Android gate: `connectedDebugAndroidTest` PASS, 137 tests, 0 failures on R2
- APK: `release_artifacts/quality-alternative-v0.11.15-agent-content-inbox-alpha-debug.apk`
- APK SHA-256: `10f2d54f7dc06c561afa32a83bcc7c5790c211f17cd320d469d93e6c957278f6`
- Release gate summary: `docs/release-gate-logs/2026-06-12-sprint27-agent-content-inbox/VALIDATION_SUMMARY.md`

### Slice 27.7 - GPT Pro Review And APK

- Build a scoped review bundle containing `PRD.md`, this plan, changed source, changed tests, validation logs, screenshots, and a manifest.
- Ask GPT Pro to review PRD alignment, privacy, Drive scope, priority semantics, rights policy, ranking behavior, UI boundedness, and visual evidence.
- Iterate until `SCORE 10/10`, `VERDICT PASS`, and visual review pass.
- Build the internal alpha APK as currently used by the repo.

Acceptance:

- GPT Pro output is saved in the repo evidence trail.
- APK path and commit SHA are recorded in the final summary.

## Out Of Scope

- Open-web crawling.
- Newsletter or RSS ingestion.
- Shared agent-generated editorial library.
- Silent background import.
- Scanning all Google Drive files.
- Storing Drive access tokens or raw Drive ids in Portable Profile.
- Replacing the existing manual Markdown/EPUB import flow.

## Release Gate

The sprint is not done until:

- unit tests pass,
- relevant Android tests pass,
- visual E2E screenshots exist,
- lint is reviewed or fixed,
- GPT Pro review reaches `SCORE 10/10`,
- an internal alpha APK is built,
- final summary records integration method and commit SHA.
