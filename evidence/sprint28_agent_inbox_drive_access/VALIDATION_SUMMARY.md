# Sprint 28 Validation Summary

Status: in progress

Branch: `codex/sprint28-agent-inbox-drive-access`

Review base commits include the first Drive access slice, device validation gate, GPT Pro R1 fix commit, and the current Markdown image attachment slice on `codex/sprint28-agent-inbox-drive-access`. Later evidence should update this file with the final reviewed release commit.

## GPT Pro Review Trail

- R1 output: `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R1.md`
- R1 result: `SCORE 7/10`, `VERDICT BLOCK`, `VISUAL REVIEW REVISE`
- R1 blocker 1: legacy Sprint 27 app-created Agent Inbox folder ids could bypass the new Picker path because persisted state had only `enabled` plus `folderId`, not a durable Picker grant marker.
- R1 blocker 2: `agentInboxDriveEnabled=true` with a missing folder id could still render connected Settings copy.
- R1 fixes: persist `agent_inbox_drive_grant_mode=picker_folder`; hydrate Agent Inbox Drive as connected only when enabled, folder id, and marker are all present; route scan authorization from that derived predicate; clear connection state for `saveAgentInboxDriveConnection(null)`; add regressions for legacy no-marker state and missing-folder behavior.
- R2 lane: `https://chatgpt.com/c/6a2e869b-ec48-83ed-a929-adb856a72d07`
- R2 prompt: `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_PROMPT_R2.md`
- R2 bundle manifest: `evidence/sprint28_agent_inbox_drive_access/REVIEW_BUNDLE_MANIFEST_R2.md`
- R2 output: `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R2.md`
- R2 result: `SCORE 7/10`, `VERDICT REVISE`, `VISUAL REVIEW PASS`
- R2 finding 1: Agent Inbox Markdown image sidecars can collapse through duplicate filenames or storage-safe filename collisions.
- R2 finding 2: Agent Inbox Markdown reader rendering can fall back to local file paths that were not reviewed sidecars.
- R2 finding 3: sidecar rollback can leave files behind if local attachment writing fails mid-loop.
- R2 fixes implemented locally before R3: duplicate/colliding sidecar names are invalid during review; Agent Inbox Markdown image resolution allows only reviewed attachments and `data:image` sources; sidecar writes use temp/backup cleanup and remove promoted files on failure.

R2 candidate scope adds the user-requested Markdown image fix:

- Manual picker: adding image files after a Markdown file is already selected now merges those images into the existing Markdown candidate instead of asking the user to choose Markdown again.
- Agent Inbox: Markdown packages may include bounded safe image sidecars; EPUB package sidecars remain invalid because EPUB assets belong inside the EPUB.
- Visual evidence now includes an Agent Inbox Markdown package imported through the fake Drive flow and rendered with its sidecar image in the reader.

## Passed Locally

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest --tests com.qualityalternative.app.ui.DocumentImportCandidateFactoryTest --tests com.qualityalternative.app.ui.MainViewModelTest --tests com.qualityalternative.app.data.AgentInboxReviewCandidateFactoryTest --tests com.qualityalternative.app.data.AgentInboxPackageImporterTest`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest --tests com.qualityalternative.app.data.AgentInboxReviewCandidateFactoryTest --tests com.qualityalternative.app.data.AgentInboxPackageImporterTest --tests com.qualityalternative.app.data.MarkdownReaderDocumentParserTest --tests com.qualityalternative.app.data.RoomUserDocumentRepositoryTest`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest --tests com.qualityalternative.app.ui.GoogleDriveAuthorizationTest --tests com.qualityalternative.app.ui.MainViewModelTest --tests com.qualityalternative.app.data.AndroidGoogleDriveAgentInboxClientTest`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest --rerun-tasks --tests com.qualityalternative.app.data.PreferencesSettingsRepositoryTest --tests com.qualityalternative.app.ui.MainViewModelTest`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:compileDebugAndroidTestKotlin`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.VisualQaScreenshotTest#captureSprint28AgentInboxDriveAccessStates`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:lintDebug`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:processReleaseManifestForPackage`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:assembleDebug`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:lintDebug :app:processReleaseManifestForPackage :app:assembleDebug`
- `git diff --check`

## Implemented Contract

- Agent Inbox keeps `drive.file`; no `drive.readonly` fallback has been added.
- Agent Inbox first connection uses Google Picker folder selection with `PICKER_OAUTH_TRIGGER=true`, `PICKER_ALLOW_FOLDER_SELECTION=true`, `prompt=CONSENT`, and `optOutIncludingGrantedScopes=true`.
- Agent Inbox scans require a persisted selected folder id plus durable `picker_folder` grant marker.
- Legacy Agent Inbox Drive state with `enabled=true` and a folder id but no `picker_folder` marker is normalized to disconnected and cannot enter non-Picker scan mode.
- The Drive client no longer creates an app-owned inbox folder when `folderId` is missing.
- Selected-folder HTTP 401/403/404 failures clear the local folder grant and return Settings to `Select folder`.
- Connection and access-lost analytics are privacy-safe and do not include raw folder ids.
- Manual Markdown image attachment imports support image-only follow-up picker selections against the already selected Markdown document while preserving edited title, topics, and priority.
- Agent Inbox Markdown sidecar images use safe filenames, a maximum of six attachments, a 5 MiB per-image metadata/download cap, and a 15 MiB total download cap.
- Agent Inbox Markdown sidecar image filenames must be unique after case normalization and after local storage-segment cleanup.
- Agent Inbox Markdown reader rendering disables local file/path fallback; unmatched local/absolute/file URI image targets render as placeholders instead of reading unreviewed local bytes.
- Agent Inbox sidecar storage writes attachments through rollback-aware temp/backup handling and removes promoted sidecars if a later attachment write fails.
- Agent Inbox imported Markdown image sidecars are stored under the Agent Inbox document root, passed through `UserDocumentDraft.imageAttachmentUris`, rendered by the existing Markdown reader, and deleted if import is rolled back.
- Agent Inbox EPUB package sidecars and unsupported extra files remain invalid finite review items.

## Visual Evidence

- Canonical screenshot run: `evidence/sprint28_agent_inbox_drive_access/visual_e2e/sprint28-agent-inbox-drive-access-1781433607325/`
- Contact sheet: `evidence/sprint28_agent_inbox_drive_access/visual_e2e/contact_sheet_r2.png`
- Connected result XML/logs: `evidence/sprint28_agent_inbox_drive_access/android-results-r2/`
- Connected Gradle log: `evidence/sprint28_agent_inbox_drive_access/logs/connected_sprint28_visual_r2_final.log`
- Targeted Markdown image unit log: `evidence/sprint28_agent_inbox_drive_access/logs/targeted_markdown_image_r2.log`
- Targeted R2-fix unit log: `evidence/sprint28_agent_inbox_drive_access/logs/targeted_r2_fixes.log`
- Full local gate log: `evidence/sprint28_agent_inbox_drive_access/logs/full_local_gate_r2.log`
- Diff-check log: `evidence/sprint28_agent_inbox_drive_access/logs/git_diff_check_r2.log`
- R2 diff and commit context: `evidence/sprint28_agent_inbox_drive_access/sprint28_r2_tracked_diff.patch`, `evidence/sprint28_agent_inbox_drive_access/git_log_r2.txt`, and `evidence/sprint28_agent_inbox_drive_access/adb_devices_r2.txt`

## R3 Candidate Evidence

- Targeted R2-fix unit log: `evidence/sprint28_agent_inbox_drive_access/logs/targeted_r2_fixes.log`
- Full local gate log: `evidence/sprint28_agent_inbox_drive_access/logs/full_local_gate_r3_candidate.log`
- Connected visual log: `evidence/sprint28_agent_inbox_drive_access/logs/connected_sprint28_visual_r3_candidate.log`
- Diff-check log: `evidence/sprint28_agent_inbox_drive_access/logs/git_diff_check_r3.log`
- R3 diff and commit context: `evidence/sprint28_agent_inbox_drive_access/sprint28_r3_tracked_diff.patch`, `evidence/sprint28_agent_inbox_drive_access/git_log_r3.txt`, and `evidence/sprint28_agent_inbox_drive_access/adb_devices_r3.txt`
- Android connected results: `evidence/sprint28_agent_inbox_drive_access/android-results-r3/`
- Canonical R3 screenshot run: `evidence/sprint28_agent_inbox_drive_access/visual_e2e_r3/sprint28-agent-inbox-drive-access-1781437194813/`
- R3 contact sheet: `evidence/sprint28_agent_inbox_drive_access/visual_e2e_r3/contact_sheet_r3.png`

The focused visual E2E passed on `qaApi36(AVD) - 16` with `tests=1`, `failures=0`, `errors=0`, `skipped=0`.

## Pending Before Release

- Live device spike: select the Agent Inbox folder through Picker, add a later package through rclone, scan it in the app under `drive.file`, and record the result.
- GPT Pro review iterations until `SCORE 10/10`, `VERDICT PASS`, and `VISUAL REVIEW PASS`.
- Final release APK gate and alpha publication.

## Live Spike Constraint

On 2026-06-14, a local `qaApi36` emulator was available for deterministic visual E2E. The live rclone/Picker spike still requires a signed-in Google account/device environment with the production OAuth flow and an external rclone upload into the selected folder.
