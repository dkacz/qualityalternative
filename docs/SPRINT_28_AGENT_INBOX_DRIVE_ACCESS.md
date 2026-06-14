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
- Slice 28.4 has a deterministic instrumented screenshot test added for disconnected, missing-folder error, selected-folder, and access-lost states. Physical screenshots are pending a connected emulator/device.
- Current validation/evidence summary: `evidence/sprint28_agent_inbox_drive_access/VALIDATION_SUMMARY.md`.
- Live rclone/Picker spike checklist: `evidence/sprint28_agent_inbox_drive_access/device_spike/RCLONE_PICKER_FOLDER_SPIKE.md`.
- Slice 28.3 and the GPT Pro/release gate are still pending.

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

- Added: `VisualQaScreenshotTest#captureSprint28AgentInboxDriveAccessStates`, including access-lost reconnect evidence.
- Passed: `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:compileDebugAndroidTestKotlin`.
- Pending: connected emulator run and copying/curating PNG evidence under `evidence/sprint28_agent_inbox_drive_access/`.

### Slice 28.5 - GPT Pro Review And Release

- Build a scoped review bundle with PRD, sprint plan, changed source, tests, logs, visual screenshots, and dependency evidence.
- Iterate with GPT Pro until `SCORE 10/10`, `VERDICT PASS`, and `VISUAL REVIEW PASS`.
- Bump Android version, run full release gate, publish the alpha APK.

Acceptance:

- GPT Pro output is committed in the evidence trail.
- APK, SHA-256, commit, tag, release URL, and integration method are recorded in `docs/LANE_STATUS.md`.

## Out Of Scope

- Open-web crawling.
- Silent background import.
- Whole-Drive scanning.
- Copying Drive grants through Portable Profile.
- Widening to `drive.readonly` without the fallback decision record.
