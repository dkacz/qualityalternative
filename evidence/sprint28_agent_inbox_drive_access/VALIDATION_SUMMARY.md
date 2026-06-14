# Sprint 28 Validation Summary

Status: in progress

Branch: `codex/sprint28-agent-inbox-drive-access`

Review base commits include the first Drive access slice, device validation gate, GPT Pro R1 fix commit, Markdown image attachment fixes, and the current controlled read-only fallback slice on `codex/sprint28-agent-inbox-drive-access`. Later evidence should update this file with the final reviewed release commit.

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
- R3 lane: `https://chatgpt.com/c/6a2e94c5-d82c-83eb-88f9-3a0304226708`
- R3 prompt: `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_PROMPT_R3.md`
- R3 bundle manifest: `evidence/sprint28_agent_inbox_drive_access/REVIEW_BUNDLE_MANIFEST_R3.md`
- R3 output: `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R3.md`
- R3 result: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`
- R3 finding recheck: all three R2 findings fully fixed; no fresh findings.
- R3 bundle gap: live rclone-after-Picker visibility remains unproven and is correctly treated as a pending release gate outside the deterministic local review.
- Live Picker runtime result: `evidence/sprint28_agent_inbox_drive_access/device_spike/live_picker_runtime_20260614/RESULT.md`
- Live Picker runtime outcome: on the signed-in `qaApi36` emulator, Google Play Services returned from account selection without opening a Drive folder Picker and without a selected folder id. The Picker-first path remains unreleasable in this environment and the next slice is the documented `drive.readonly` fallback.
- Readonly fallback scope implemented locally after the Picker failure: Settings accepts an Agent Inbox folder URL/id, requests explicit `drive.readonly` consent, persists `readonly_folder`, scans only the saved folder id, revokes read-only access on disconnect, and keeps analytics privacy-safe.
- Readonly fallback live result: `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/RESULT.md`
- Readonly fallback live outcome: after Google consent on `qaApi36`, the app scanned the pasted Drive folder id and displayed the rclone-created package as one package waiting for review.
- Readonly fallback GPT Pro R4 lane: `https://chatgpt.com/c/6a2ef5f8-543c-83ed-945d-c023fc92a8b2`
- Readonly fallback GPT Pro R4 prompt: `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_PROMPT_R4_READONLY_FALLBACK.md`
- Readonly fallback GPT Pro R4 bundle manifest: `evidence/sprint28_agent_inbox_drive_access/REVIEW_BUNDLE_MANIFEST_R4_READONLY_FALLBACK.md`
- Readonly fallback GPT Pro R4 bundle: `SPRINT28_R4_READONLY_FALLBACK_REVIEW_BUNDLE_20260614.zip`
- Readonly fallback GPT Pro R4 output: `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R4_READONLY_FALLBACK.md`
- Readonly fallback GPT Pro R4 result: `SCORE 9/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`
- R4 implementation result: PASS across authorization, scan boundary, persistence, first-scan race, access-lost, disconnect/revoke, privacy, live final app state, R1/R2/R3 regression checks, and visual evidence.
- R4 remaining finding: low evidence/package hygiene. `live_readonly_rclone_package/RESULT.md` overstated `logcat_live_readonly_success.txt` as auth-flow proof and referenced raw rclone listing JSON files excluded from the bundle.
- R4 bundle gaps: raw Gradle execution logs were not shipped for all listed unit/build pass claims, and the shipped logcat did not prove live OAuth consent.
- R4 fixes implemented locally before R5: revised the live result so it relies on final-state screenshot/UI XML instead of logcat for proof, added `rclone_listing_summary_redacted.md`, added raw local/targeted Gradle logs, fixed Sprint 27 visual test seeding to use the durable `picker_folder` grant marker before scan success, and passed a fresh full connected Android run with 138/138 tests.
- Readonly fallback GPT Pro R5 lane: `https://chatgpt.com/c/6a2f0b18-a1e0-83ed-a622-e228bc775631`
- Readonly fallback GPT Pro R5 prompt: `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_PROMPT_R5_EVIDENCE_HYGIENE.md`
- Readonly fallback GPT Pro R5 bundle manifest: `evidence/sprint28_agent_inbox_drive_access/REVIEW_BUNDLE_MANIFEST_R5_EVIDENCE_HYGIENE.md`
- Readonly fallback GPT Pro R5 bundle: `SPRINT28_R5_EVIDENCE_HYGIENE_REVIEW_BUNDLE_20260614.zip`
- Readonly fallback GPT Pro R5 output: `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R5_EVIDENCE_HYGIENE.md`
- Readonly fallback GPT Pro R5 result: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`
- R5 result details: R4 finding closure PASS; fresh findings none; bundle gaps none; package hygiene clean enough for this lane.
- Final release gate summary: `docs/release-gate-logs/2026-06-14-sprint28-agent-inbox-drive-access/VALIDATION_SUMMARY.md`
- Final release gate result: PASS for `v0.11.16-agent-inbox-drive-access-alpha`, `versionCode=32`, `versionName=0.11.16-alpha`; final connected Android gate passed 138/138; debug APK badging/signature/install/launch passed.

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
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --tests com.qualityalternative.app.ui.GoogleDriveAuthorizationTest --tests com.qualityalternative.app.ui.MainViewModelTest --tests com.qualityalternative.app.data.PreferencesSettingsRepositoryTest`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.VisualQaScreenshotTest#captureSprint28AgentInboxDriveAccessStates`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew assembleDebug`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:lintDebug :app:processReleaseManifestForPackage :app:assembleDebug`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.AccessibilityInterceptionTest#chromeVerifiedHostAdapterHarnessAcceptsOnlyLoadedMatchingHost`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.VisualQaScreenshotTest#captureSprint27AgentInboxReviewScreens`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.VisualQaScreenshotTest#captureSprint28AgentInboxDriveAccessStates`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:lintDebug :app:processReleaseManifestForPackage :app:assembleDebug`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest`

## Implemented Contract

- Agent Inbox still supports the historical `drive.file` Picker path with `PICKER_OAUTH_TRIGGER=true`, `PICKER_ALLOW_FOLDER_SELECTION=true`, `prompt=CONSENT`, and `optOutIncludingGrantedScopes=true`, but the live Picker runtime failed before folder selection.
- The active fallback requests `drive.readonly` only after the user enters an Agent Inbox folder URL/id and taps `Connect folder`.
- Agent Inbox scans require a persisted selected folder id plus supported grant marker: `picker_folder` for the historical Picker path or `readonly_folder` for the controlled fallback.
- Legacy Agent Inbox Drive state with `enabled=true` and a folder id but no supported grant marker is normalized to disconnected and cannot enter scan mode.
- The Drive client no longer creates an app-owned inbox folder when `folderId` is missing.
- HTTP 401/403/404 scan failures clear the local folder grant and return Settings to reconnect-copy with the prior folder id preserved in the draft.
- Connection and access-lost analytics are privacy-safe and do not include raw folder ids, file ids, package ids, content names, or raw scan failure text.
- The `drive.readonly` fallback is constrained by app behavior to scanning the saved Agent Inbox folder id. It must not search Drive or discover inbox folders by name.
- Settings copy explicitly says Drive read access is used only after consent and that the app scans only the pasted folder id.
- Disconnect revokes the read-only scope for a `readonly_folder` Agent Inbox connection while preserving normal annotation `drive.file` behavior.
- Manual Markdown image attachment imports support image-only follow-up picker selections against the already selected Markdown document while preserving edited title, topics, and priority.
- Agent Inbox Markdown sidecar images use safe filenames, a maximum of six attachments, a 5 MiB per-image metadata/download cap, and a 15 MiB total download cap.
- Agent Inbox Markdown sidecar image filenames must be unique after case normalization and after local storage-segment cleanup.
- Agent Inbox Markdown reader rendering disables local file/path fallback; unmatched local/absolute/file URI image targets render as placeholders instead of reading unreviewed local bytes.
- Agent Inbox sidecar storage writes attachments through rollback-aware temp/backup handling and removes promoted sidecars if a later attachment write fails.
- Agent Inbox imported Markdown image sidecars are stored under the Agent Inbox document root, passed through `UserDocumentDraft.imageAttachmentUris`, rendered by the existing Markdown reader, and deleted if import is rolled back.
- Agent Inbox EPUB package sidecars and unsupported extra files remain invalid finite review items.

## Historical R2 Visual Evidence

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

- GPT Pro R3 review: `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R3.md`
- Targeted R2-fix unit log: `evidence/sprint28_agent_inbox_drive_access/logs/targeted_r2_fixes.log`
- Full local gate log: `evidence/sprint28_agent_inbox_drive_access/logs/full_local_gate_r3_candidate.log`
- Connected visual log: `evidence/sprint28_agent_inbox_drive_access/logs/connected_sprint28_visual_r3_candidate.log`
- Diff-check log: `evidence/sprint28_agent_inbox_drive_access/logs/git_diff_check_r3.log`
- R3 diff and commit context: `evidence/sprint28_agent_inbox_drive_access/sprint28_r3_tracked_diff.patch`, `evidence/sprint28_agent_inbox_drive_access/git_log_r3.txt`, and `evidence/sprint28_agent_inbox_drive_access/adb_devices_r3.txt`
- Android connected results: `evidence/sprint28_agent_inbox_drive_access/android-results-r3/`
- Canonical R3 screenshot run: `evidence/sprint28_agent_inbox_drive_access/visual_e2e_r3/sprint28-agent-inbox-drive-access-1781437194813/`
- R3 contact sheet: `evidence/sprint28_agent_inbox_drive_access/visual_e2e_r3/contact_sheet_r3.png`
- R3 ChatGPT URL: `evidence/sprint28_agent_inbox_drive_access/chatgpt_r3_url.txt`
- Live spike runbook and fixture: `evidence/sprint28_agent_inbox_drive_access/device_spike/LIVE_RCLONE_PICKER_SPIKE_RUNBOOK.md` and `evidence/sprint28_agent_inbox_drive_access/device_spike/rclone_picker_live_package/`
- Live environment probe: `evidence/sprint28_agent_inbox_drive_access/device_spike/LIVE_ENVIRONMENT_PROBE_20260614.txt`

The focused visual E2E passed on `qaApi36(AVD) - 16` with `tests=1`, `failures=0`, `errors=0`, `skipped=0`.

## Current Readonly Fallback Evidence

- Deterministic visual E2E contact sheet: `evidence/sprint28_agent_inbox_drive_access/visual_e2e_readonly_r1/contact_sheet_readonly_r1.png`
- Deterministic visual E2E screenshot run: `evidence/sprint28_agent_inbox_drive_access/visual_e2e_readonly_r1/sprint28-agent-inbox-drive-access-1781460684272/`
- Deterministic visual E2E result XML: `evidence/sprint28_agent_inbox_drive_access/visual_e2e_readonly_r1/TEST-sprint28-readonly-visual.xml`
- Deterministic visual E2E logcat: `evidence/sprint28_agent_inbox_drive_access/visual_e2e_readonly_r1/logcat-sprint28-readonly-visual.txt`
- Live rclone fallback result: `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/RESULT.md`
- Redacted rclone listing summary: `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/rclone_listing_summary_redacted.md`
- Live rclone fallback success screenshot: `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/live_readonly_rclone_success.png`
- Live rclone fallback UI dump: `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/window_live_readonly_success.xml`
- Live rclone fallback logcat: `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/logcat_live_readonly_success.txt` retained as post-run diagnostics only; not used as OAuth proof.
- Full local gate log after R4 launch: `evidence/sprint28_agent_inbox_drive_access/logs/full_local_gate_r4_readonly_fallback.log`
- Full local gate log after R5 evidence/test update: `evidence/sprint28_agent_inbox_drive_access/logs/full_local_gate_r5_evidence_hygiene.log`
- Full connected Android exploratory run: `evidence/sprint28_agent_inbox_drive_access/logs/full_connected_debug_android_test_r4_readonly_fallback.log` failed 2/138 before the Sprint 27 visual seed fix and before targeted reruns.
- Targeted Chrome rerun after the full connected failure: `evidence/sprint28_agent_inbox_drive_access/logs/targeted_chrome_verified_host_rerun_r4.log` passed 1/1.
- Targeted Sprint 27 visual rerun after durable grant seed fix: `evidence/sprint28_agent_inbox_drive_access/logs/targeted_sprint27_visual_rerun_r4_after_grant_seed.log` passed 1/1.
- Targeted Sprint 28 readonly visual rerun after the seed fix: `evidence/sprint28_agent_inbox_drive_access/logs/targeted_sprint28_readonly_visual_rerun_after_seed_fix.log` passed 1/1.
- Fresh full connected Android run after the R4 evidence/test fix: `evidence/sprint28_agent_inbox_drive_access/logs/full_connected_debug_android_test_r5_evidence_hygiene.log` passed 138/138.

The current readonly visual E2E passed on `qaApi36(AVD) - 16` with `tests=1`, `failures=0`, `errors=0`, `skipped=0`. The live rclone fallback flow also passed on `qaApi36`: after explicit Google read-only consent, the app scanned the pasted folder id and displayed the rclone-uploaded package as one package waiting for review.

## Pending Before Release

- Local final release gate is complete and passed.
- Commit/tag/push/GitHub release publication remains the integration step if this release gate is accepted for publication.

## Preflight APK Evidence

- Preflight summary: `docs/release-gate-logs/2026-06-14-sprint28-agent-inbox-drive-access-preflight/VALIDATION_SUMMARY.md`
- Status: preparation only, not final release gate.
- APK install/launch preflight passed on the local emulator, but final release remains blocked on the fallback GPT Pro review and final release gate.

## Live Spike Constraint

On 2026-06-14, the original environment probe recorded no signed-in account. A follow-up live run signed into `qaApi36`, freshly installed the current debug APK, and prepared the rclone folder. The Google Play Services authorization UI showed the account chooser, then returned to the app with `No Agent Inbox folder was selected`; no folder Picker appeared. The evidence is in `evidence/sprint28_agent_inbox_drive_access/device_spike/live_picker_runtime_20260614/`.

The controlled `drive.readonly` fallback was then implemented and tested on the same signed-in emulator. The app requested explicit read-only Drive consent, scanned only the pasted Agent Inbox folder id, and displayed the rclone-uploaded package. Evidence is in `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/`. The sprint remains unreleased until GPT Pro reviews this fallback scope and the final APK gate passes.
