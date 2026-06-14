# Sprint 28 - Agent Inbox Drive Access Fix

## Goal

Fix the post-release Agent Inbox access gap where packages uploaded by an external user-controlled tool such as rclone are not visible to the Android app under the current `drive.file` scope unless the app created those files or the user explicitly grants access through a Drive selection flow.

The sprint finishes only after the chosen access strategy is implemented, visual E2E evidence exists, GPT Pro reaches `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`, and an internal alpha APK is released.

## Decision

Default path: keep `drive.file` and add Google Picker folder selection for the Agent Inbox.

Rationale:

- Google's Drive scope guidance recommends `drive.file` with Google Picker for user-chosen per-file access instead of broad Drive access: `https://developers.google.com/workspace/drive/api/guides/api-specific-auth`
- Google's Android Picker guide documents `PICKER_OAUTH_TRIGGER` and `PICKER_ALLOW_FOLDER_SELECTION` for mobile folder selection: `https://developers.google.com/workspace/drive/picker/guides/desktop-mobile-picker`
- Google Play services release notes state that `play-services-auth` v21.6.0 adds `PICKER_ALLOW_FOLDER_SELECTION`: `https://developers.google.com/android/guides/releases`

Fallback path: request broader `drive.readonly` only if the Picker folder grant does not expose package children added after the folder selection. That fallback requires a PRD/privacy update, UX copy for broader Drive access, and GPT Pro review before release.

## PRD Mapping

- `FR3B. Google Drive Agent Inbox`: explicit folder authorization, no whole-Drive scan, no silent app-owned folder when the user intends an externally populated folder.
- `FR12. Analytics Instrumentation`: access errors and reconnect events must stay privacy-safe.
- `FR13. Portable Profile`: selected folder ids, tokens, and grants remain non-portable.
- `NFR4. Local-First Portability And Privacy`: prefer Picker-scoped `drive.file` over broader scopes.

## Slice Plan

Current status on 2026-06-14:

- Slice 28.0 implementation is in place: `play-services-auth` is bumped to `21.6.0`, the authorization builder has a pure testable spec, and Agent Inbox folder selection requests `drive.file` plus Picker folder parameters.
- Slice 28.1 implementation is in place for the app-side Picker result path: `picked_file_ids` is read, the selected folder id is persisted, and `AGENT_INBOX_CONNECTED` analytics records only `grantMode=pickerFolder`.
- Slice 28.2 implementation is in place for the no-auto-create contract: scans require a selected folder id, the Drive client rejects missing folder ids before any HTTP request, and selected-folder scans list only children of that folder.
- Slice 28.2 also handles selected-folder 401/403/404 scan failures as access-lost states that clear the local folder grant and return the UI to `Select folder`.
- GPT Pro R1 returned `SCORE 7/10`, `VERDICT BLOCK`, `VISUAL REVIEW REVISE`.
- R1 fixes are implemented locally: the app persists `agent_inbox_drive_grant_mode=picker_folder`, derives connected state from nonblank folder id plus that marker, treats legacy Sprint 27 app-created folder ids without the marker as disconnected, and no longer supports `enabled=true` with a missing folder id.
- Slice 28.4 has deterministic instrumented screenshot evidence for disconnected, selected-folder, access-lost, imported Agent Inbox Markdown image reader, and dark selected-folder states.
- Slice 28.6 is implemented locally for the added Markdown image scope: the manual document picker now supports adding image files to an already selected Markdown file, and Agent Inbox Markdown packages carry bounded sidecar image attachments through import and reader rendering.
- Current validation/evidence summary: `evidence/sprint28_agent_inbox_drive_access/VALIDATION_SUMMARY.md`.
- Live rclone/Picker spike checklist: `evidence/sprint28_agent_inbox_drive_access/device_spike/RCLONE_PICKER_FOLDER_SPIKE.md`.
- Slice 28.3 live device spike, GPT Pro R2, and the release gate are still pending.

### Slice 28.0 - Contract And Dependency Gate

- Patch `PRD.md` with the `drive.file`/Picker folder-grant rule and fallback condition.
- Add this sprint plan.
- Bump `play-services-auth` to a version that exposes `PICKER_ALLOW_FOLDER_SELECTION`.
- Add a small testable authorization-request builder for normal Drive authorization versus Agent Inbox folder selection.

Acceptance:

- Unit tests prove Agent Inbox folder selection requests `drive.file`, `PICKER_OAUTH_TRIGGER=true`, `PICKER_ALLOW_FOLDER_SELECTION=true`, `prompt=CONSENT`, and opt-out of previously granted broader scopes.
- Normal annotation sync, Agent Inbox scan, and Agent Inbox import authorization do not trigger Picker.

Validation:

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest --tests com.qualityalternative.app.ui.GoogleDriveAuthorizationTest --tests com.qualityalternative.app.ui.MainViewModelTest --tests com.qualityalternative.app.data.AndroidGoogleDriveAgentInboxClientTest` passed.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest` passed.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:lintDebug` passed.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:processReleaseManifestForPackage` passed.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:assembleDebug` passed.

### Slice 28.1 - Picker Folder Grant Connection

- Add an Agent Inbox `Select folder` / `Reconnect folder` action in Settings.
- Launch Google Picker for folder selection on connect.
- Read `picked_file_ids` from `AuthorizationResult.getTokenResponseParams()`.
- Persist the selected folder id through the existing Agent Inbox Drive settings state.
- Show a clear cancelled/no-folder/error state without erasing local library data.

Acceptance:

- Tests cover picked folder id persistence, empty picker result, cancellation, reconnect replacing the old folder id, and no raw folder id in Portable Profile.

Current coverage:

- Covered: picked folder id persistence, reconnect replacement, empty picker result, privacy-safe connection analytics, and no raw folder id in remote-safe analytics payloads.
- Covered: Portable Profile omits raw Agent Inbox folder ids and raw Agent Inbox scan failure text in `AccountLightProfileExporterTest`.
- Covered after GPT Pro R1: Picker folder selection persists a durable grant marker and reconnect replaces both the folder id and marker.

### Slice 28.2 - Scan Semantics

- Stop auto-creating a Drive folder during Agent Inbox scan.
- If no selected folder id exists, scan must not call Drive and must show a reconnect/select-folder message.
- Scans list packages only under the persisted selected folder id.
- Drive 403/404 access failures become finite reconnect-required UI, not `connected, no packages found`.

Acceptance:

- Unit tests prove scan with no folder id is blocked before Drive calls.
- Client tests prove the package-list query uses the selected parent folder and no folder-create request.
- Failure tests distinguish empty accessible folder from inaccessible/revoked selected folder.

Current coverage:

- Covered: scan with no folder id is blocked before Drive calls; the HTTP client rejects missing folder id before any request; selected-folder scans do not create/search by folder name.
- Covered: selected-folder HTTP 401/403/404 scan failures clear the local folder grant, show `Select folder`, and record `AGENT_INBOX_SCAN_FAILED` with `reason=access_lost`.
- Covered after GPT Pro R1: a pre-upgrade legacy folder id without the Picker grant marker is not restored as connected and cannot enter `AGENT_INBOX_SCAN`.

### Slice 28.3 - External Package Spike Evidence

- Add a repeatable manual/live spike checklist for the exact scenario:
  1. User selects the Agent Inbox folder through Picker.
  2. A package folder with `manifest.json` and `content.md`/`content.epub` is added later through rclone.
  3. The app scans the selected folder and sees the package.
- If live credentials are unavailable in Codex, record that the implementation is built against Google's documented Picker contract and keep the fallback decision open for device validation.

Acceptance:

- Evidence records whether the later-added child package is visible under `drive.file`.
- If not visible, create the `drive.readonly` fallback plan and do not claim release readiness for Picker-only behavior.

Current evidence:

- Checklist is documented in `evidence/sprint28_agent_inbox_drive_access/device_spike/RCLONE_PICKER_FOLDER_SPIKE.md`.
- Result is pending because no device/emulator is attached in the current environment.

### Slice 28.4 - Visual E2E

- Add deterministic screenshots for:
  - disconnected Agent Inbox with `Select folder`,
  - Picker-granted folder connected,
  - scan blocked before folder selection,
  - inaccessible/revoked folder reconnect state,
  - selected-folder scan showing package review,
  - imported package still flowing through existing Markdown/EPUB reader evidence.

Acceptance:

- Screenshots live under `evidence/sprint28_agent_inbox_drive_access/`.
- Tests use debug-only fake Drive/Picker state, not live OAuth.

Current coverage:

- Added and passed: `VisualQaScreenshotTest#captureSprint28AgentInboxDriveAccessStates`, including disconnected select-folder, selected-folder, access-lost reconnect, Agent Inbox Markdown image reader, and dark selected-folder evidence.
- Canonical screenshot run: `evidence/sprint28_agent_inbox_drive_access/visual_e2e/sprint28-agent-inbox-drive-access-1781433607325/`.
- Contact sheet: `evidence/sprint28_agent_inbox_drive_access/visual_e2e/contact_sheet_r2.png`.
- Connected result XML/logs: `evidence/sprint28_agent_inbox_drive_access/android-results-r2/` and `evidence/sprint28_agent_inbox_drive_access/logs/connected_sprint28_visual_r2_final.log`.

### Slice 28.6 - Markdown Image Attachment Parity

- Fix the manual picker bug where adding image files after a Markdown file was already selected still showed "choose Markdown first".
- Keep the current Add Document form state when the user opens the picker only to add Markdown image attachments: title, selected topics, and priority survive the second picker result.
- Let Agent Inbox Markdown packages include a small bounded set of safe image sidecars while keeping EPUB sidecars unsupported.
- Download image sidecars only after content review, enforce per-image and total byte limits, store them under the Agent Inbox document root, pass them through `UserDocumentDraft.imageAttachmentUris`, and delete them if the import is rolled back.

Acceptance:

- Unit tests cover image-only picker results merging into an already selected Markdown candidate.
- Unit tests cover Agent Inbox Markdown image sidecar review, unsupported EPUB sidecars, too many images, per-image and total-size limits, Drive download/storage, and reader-visible image attachment import.
- Visual E2E shows an Agent Inbox Markdown package rendering its sidecar image in the reader.

### Slice 28.5 - GPT Pro Review And Release

- Build a scoped review bundle with PRD, sprint plan, changed source, tests, logs, visual screenshots, and dependency evidence.
- Iterate with GPT Pro until `SCORE 10/10`, `VERDICT PASS`, and `VISUAL REVIEW PASS`.
- Bump Android version, run full release gate, publish the alpha APK.

Current review state:

- R1 output: `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R1.md`.
- R1 result: `SCORE 7/10`, `VERDICT BLOCK`, `VISUAL REVIEW REVISE`.
- R1 blockers fixed locally before R2: durable Picker grant marker, legacy state normalization, and missing-folder connected-state normalization.
- R2 output: `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R2.md`.
- R2 result: `SCORE 7/10`, `VERDICT REVISE`, `VISUAL REVIEW PASS`.
- R2 blockers to fix before R3: duplicate/colliding Markdown sidecar names, Agent Inbox reader fallback to unreviewed local image paths, and mid-write sidecar cleanup.

Acceptance:

- GPT Pro output is committed in the evidence trail.
- APK, SHA-256, commit, tag, release URL, and integration method are recorded in `docs/LANE_STATUS.md`.

## Out Of Scope

- Open-web crawling.
- Silent background import.
- Whole-Drive scanning.
- Copying Drive grants through Portable Profile.
- Widening to `drive.readonly` without the fallback decision record.
