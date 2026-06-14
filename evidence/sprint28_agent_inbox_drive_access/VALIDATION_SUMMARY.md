# Sprint 28 Validation Summary

Status: in progress

Branch: `codex/sprint28-agent-inbox-drive-access`

Review base commits include the first Drive access slice and device validation gate commits on `codex/sprint28-agent-inbox-drive-access`. Use `git log --oneline -3` as the canonical commit list for the exact bundle base. Later evidence should update this file with the final reviewed release commit.

## GPT Pro Review Trail

- R1 output: `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R1.md`
- R1 result: `SCORE 7/10`, `VERDICT BLOCK`, `VISUAL REVIEW REVISE`
- R1 blocker 1: legacy Sprint 27 app-created Agent Inbox folder ids could bypass the new Picker path because persisted state had only `enabled` plus `folderId`, not a durable Picker grant marker.
- R1 blocker 2: `agentInboxDriveEnabled=true` with a missing folder id could still render connected Settings copy.
- R1 fixes: persist `agent_inbox_drive_grant_mode=picker_folder`; hydrate Agent Inbox Drive as connected only when enabled, folder id, and marker are all present; route scan authorization from that derived predicate; clear connection state for `saveAgentInboxDriveConnection(null)`; add regressions for legacy no-marker state and missing-folder behavior.

## Passed Locally

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest --tests com.qualityalternative.app.ui.GoogleDriveAuthorizationTest --tests com.qualityalternative.app.ui.MainViewModelTest --tests com.qualityalternative.app.data.AndroidGoogleDriveAgentInboxClientTest`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest --rerun-tasks --tests com.qualityalternative.app.data.PreferencesSettingsRepositoryTest --tests com.qualityalternative.app.ui.MainViewModelTest`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:compileDebugAndroidTestKotlin`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:lintDebug`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:processReleaseManifestForPackage`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:assembleDebug`
- `git diff --check`

## Implemented Contract

- Agent Inbox keeps `drive.file`; no `drive.readonly` fallback has been added.
- Agent Inbox first connection uses Google Picker folder selection with `PICKER_OAUTH_TRIGGER=true`, `PICKER_ALLOW_FOLDER_SELECTION=true`, `prompt=CONSENT`, and `optOutIncludingGrantedScopes=true`.
- Agent Inbox scans require a persisted selected folder id plus durable `picker_folder` grant marker.
- Legacy Agent Inbox Drive state with `enabled=true` and a folder id but no `picker_folder` marker is normalized to disconnected and cannot enter non-Picker scan mode.
- The Drive client no longer creates an app-owned inbox folder when `folderId` is missing.
- Selected-folder HTTP 401/403/404 failures clear the local folder grant and return Settings to `Select folder`.
- Connection and access-lost analytics are privacy-safe and do not include raw folder ids.

## Pending Before Release

- Connected run of `VisualQaScreenshotTest#captureSprint28AgentInboxDriveAccessStates`.
- Copy and curate generated PNGs under `evidence/sprint28_agent_inbox_drive_access/visual_e2e/`.
- Live device spike: select the Agent Inbox folder through Picker, add a later package through rclone, scan it in the app under `drive.file`, and record the result.
- GPT Pro review iterations until `SCORE 10/10`, `VERDICT PASS`, and `VISUAL REVIEW PASS`.
- Final release APK gate and alpha publication.

## Environment Blocker

On 2026-06-14, `adb devices` returned no attached devices and `emulator` was not available in PATH. `local.properties` points to `/opt/homebrew/share/android-commandlinetools`, which does not include a runnable emulator binary in the checked locations.
