# Lane Status

Status timestamp: 2026-06-16

This file is the repo-level index for active and recently completed execution lanes. It should point to the canonical branch, review lane, validation artifacts, and next gate for each lane.

## Current Rule

- Use this document for cross-lane status.
- Use the branch-specific sprint docs for detailed implementation notes.
- Heartbeats should exist only while waiting for a current GPT Pro lane.
- Do not infer lane status from untracked local files alone. For example, `ios/` can appear untracked on non-iOS branches; the canonical iOS implementation source is the pushed iOS branch listed below.

## Sprint 36 Agent Inbox Live Drive Folder Browser Repair

Status: `release_artifacts_built_after_gpt_pro_r2_pass_10_10`

- Date opened: 2026-06-16
- Trigger: after published `v0.11.23-agent-inbox-folder-selector-repair-alpha`, the user reports the installed app still shows only `Use Drive link`, clicking it returns `connection failed`, and no real folder picker is visible.
- Correction to Sprint 35: Sprint 35 GPT Pro R3 reached `SCORE: 10/10`, `VERDICT: PASS`, but the review gate was still insufficient because it accepted emulator visual fixtures and code evidence without requiring a signed-in live Google Drive folder selection, scan, import, and screenshots from that real flow.
- Active goal: repair Agent Inbox so the primary `Choose folder` path is a real Drive folder selection path, not the Drive link fallback; verify on a signed-in emulator or device with a real Google Drive folder and Agent Inbox package; run GPT Pro review that must reject missing live visual/log evidence; release a new APK only after `SCORE: 10/10`, `VERDICT: PASS`.
- Branch: `codex/agent-inbox-folder-selector-repair`.
- Current implementation:
  - Added `AgentInboxDriveClient.listFolders(...)` plus Drive API folder listing through `AndroidGoogleDriveAgentInboxClient`.
  - Added in-app Agent Inbox Drive folder browser state and UI: current Drive level, back stack, folder rows, `Open`, `Select`, and `Close`.
  - Added `GoogleDriveAuthorizationMode.AGENT_INBOX_BROWSE_READONLY`; `Choose folder` now requests `drive.readonly`, loads Drive folders, and selecting a folder persists `readonly_folder` and scans it with the same token.
  - Legacy `picker_folder` repair now routes to the Drive folder browser path instead of the typed Drive link path.
  - The typed `Use Drive link` fallback remains separate and visible only as fallback.
  - Adjusted the annotation Drive sync card title to `Drive sync not connected` so it does not visually contradict Agent Inbox `DRIVE` connection state.
- Validation so far:
  - Passed targeted unit tests for `AndroidGoogleDriveAgentInboxClientTest`, `AndroidHybridAgentInboxDriveClientTest`, and `MainViewModelTest`.
  - Initial Sprint 36 GPT Pro review returned `SCORE: 8/10`, `VERDICT: FAIL` because live visual evidence showed clipped folder-browser controls and the scan screenshot had snackbar coverage over the lower action area.
  - Fixed the folder-browser layout by hiding the Drive-link fallback and redundant scan/disconnect row while the browser is open; reran connected visual evidence after the fix.
  - Passed connected visual test `VisualQaScreenshotTest#captureSprint35AgentInboxFolderSelectorRepairStates` after the final folder-browser layout/text pass.
  - Final connected visual evidence: `evidence/sprint36_agent_inbox_live_picker_e2e/visual_e2e/sprint35-agent-inbox-folder-selector-repair-1781630925520/`.
  - Final live signed-in Drive E2E R4 passed on emulator `emulator-5554`: account chooser, in-app Drive folder browser, folder selection, package scan after snackbar clearance, import, Library `Files` visibility, and reader rendering.
  - Final debug APK evidence hash: `83544a7efce11141c48cca25bed5ffb6a8da9e1429565c9c074b2fe35ba71348`.
  - Final live evidence report: `evidence/sprint36_agent_inbox_live_picker_e2e/LIVE_E2E_REPORT.md`.
  - Final Pro review prompt: `evidence/sprint36_agent_inbox_live_picker_e2e/GPT_PRO_REVIEW_PROMPT.md`.
  - GPT Pro R1 review URL: `https://chatgpt.com/c/6a3183b8-daf8-83ed-93b2-e5fde8a012ab`.
  - GPT Pro R1 result: `SCORE: 8/10`, `VERDICT: FAIL`.
  - GPT Pro R1 output: `evidence/sprint36_agent_inbox_live_picker_e2e/gpt_pro_review_response.md`.
  - GPT Pro R2 bundle: `SPRINT36_AGENT_INBOX_LIVE_REVIEW_BUNDLE_R2.zip`, SHA-256 `e97eb53d4b814f845025455085a8ac33d8380192bd43d20a3fc09f3c7cc55de4`.
  - GPT Pro R2 review URL: `https://chatgpt.com/c/6a318b65-c210-83eb-9af7-c86ab9c4afc4`.
  - GPT Pro R2 result: `SCORE: 10/10`, `VERDICT: PASS`, `BLOCKERS: None`.
  - GPT Pro R2 output: `evidence/sprint36_agent_inbox_live_picker_e2e/gpt_pro_review_response_r2.md`.
  - Release version bump after Pro PASS: `versionCode=40`, `versionName=0.11.24-alpha`.
  - Release gate: `docs/release-gate-logs/2026-06-16-sprint36-agent-inbox-live-drive-folder-browser/VALIDATION_SUMMARY.md`.
  - Debug APK: `release_artifacts/quality-alternative-v0.11.24-agent-inbox-live-drive-folder-browser-alpha-debug.apk`, SHA-256 `96fc0011e3ce192897da4750d83497244fa97fcc4c924e9b49191199d6dddb54`.
  - Release unsigned APK: `release_artifacts/quality-alternative-v0.11.24-agent-inbox-live-drive-folder-browser-alpha-release-unsigned.apk`, SHA-256 `0054437d86962e7abba49391dd7a514894f404ed0720d8e92e1a995a85ec66a9`.
  - New permanent release gate: `docs/AGENT_INBOX_LIVE_REVIEW_GATE.md`.
- Live Drive evidence prepared:
  - Signed-in emulator account observed: `omareth@gmail.com`.
  - Real Google Drive folder created via rclone: `QA-Agent-Inbox-Live-E2E-20260616-173729`.
  - Drive folder link: `https://drive.google.com/open?id=1-mUNYizvDv3XLn6ap3j3g3be5eXuHNuV`.
  - Package folder: `codex-live-drive-e2e-package` with `manifest.json` and `content.md`.
  - Content SHA-256: `5f78462f8b982817e184803849779175d391c0d58ecf1fcbb95f564c34f774e1`.
  - Evidence directory: `evidence/sprint36_agent_inbox_live_picker_e2e/`.
- Next gates:
  - Commit the release gate and artifacts.
  - Push the branch, tag the release, and publish the APK assets.

## Sprint 35 Agent Inbox Folder Selector Repair

Status: `release_published_gpt_pro_r3_pass_10_10`

- Date opened: 2026-06-16
- Trigger: after `v0.11.22-agent-inbox-readonly-link-fallback-alpha`, the user reports that the Picker path still does not work for Agent Inbox and the app shows `No packages waiting for review`.
- Device feedback: Google Picker is not a reliable folder grant for this rclone/external-package Agent Inbox workflow; the app still shows `No packages waiting for review`.
- Correction to prior assumption: Sprint 34 GPT Pro R2 returned `SCORE: 10/10`, `VERDICT: PASS` for the source/release audit, but that review explicitly did not prove live signed-in Android behavior or future rclone child visibility. The real device result now supersedes the Picker-based product assumption.
- Required next implementation direction:
  - Use a real folder-selection flow for Agent Inbox: Android `OpenDocumentTree` for folder selection, including Google Drive folders exposed by DocumentsUI.
  - When the selected system folder is backed by Google Drive, persist the selected tree and scan it through Drive API with `drive.readonly` against the selected folder id rather than Google file Picker.
  - Keep the controlled `drive.readonly` user-supplied folder link/id path as a recovery/manual path for externally populated folders.
  - Treat existing `picker_folder` grants as needing repair because file Picker access does not prove package-folder visibility.
  - Keep scanning limited to the saved user-supplied Agent Inbox folder id; do not discover or scan the user's whole Drive.
- Local implementation:
  - Removed Agent Inbox production authorization/routing through Google file Picker.
  - Android `OpenDocumentTree` is again the primary folder selector.
  - Google Drive-backed document-tree folders scan via explicit readonly token and selected folder id.
  - Legacy `picker_folder` grants now route to the readonly repair path instead of continuing under `drive.file`.
  - Repo-level package authoring instructions now tell agents to build and validate complete package folders before upload and not to assume user-specific Drive/rclone details.
- Local validation:
  - Passed `testDebugUnitTest`, `lintDebug`, `assembleRelease`, and `assembleDebug`.
  - Final Gradle gate was rerun with `--rerun-tasks`.
  - Passed connected screenshot E2E on `emulator-5554` / `qaApi36(AVD) - 16`: `VisualQaScreenshotTest#captureSprint35AgentInboxFolderSelectorRepairStates`.
  - Evidence: `evidence/sprint35_agent_inbox_folder_selector_repair/` and `docs/release-gate-logs/2026-06-16-sprint35-agent-inbox-folder-selector-repair/`.
- APK artifacts:
  - `release_artifacts/quality-alternative-v0.11.23-agent-inbox-folder-selector-repair-alpha-debug.apk`
  - `release_artifacts/quality-alternative-v0.11.23-agent-inbox-folder-selector-repair-alpha-release-unsigned.apk`
- Next gate:
  - Validate with GPT Pro until `SCORE: 10/10`, `VERDICT: PASS`, then publish/tag the release.
  - R1 launched on 2026-06-16 with prompt `evidence/sprint35_agent_inbox_folder_selector_repair/pro_review_r1/SPRINT35_GPT_PRO_REVIEW_PROMPT_R1.md`.
  - R1 bundle ZIP artifact: `SPRINT35_AGENT_INBOX_FOLDER_SELECTOR_REPAIR_R1_GPT_PRO_REVIEW_BUNDLE_20260616.zip`.
  - R1 ChatGPT URL: `https://chatgpt.com/c/6a3146df-11c4-83eb-a68e-f069f7a0e407`.
  - R1 result: `SCORE: 8/10`, `VERDICT: REVISE`, `VISUAL REVIEW: REVISE`.
  - R1 output: `evidence/sprint35_agent_inbox_folder_selector_repair/pro_review_r1/GPT_PRO_REVIEW_R1.md`.
  - R1 findings addressed for R2: include production `AppContainer` wiring, scope constants, app-side package model/validation sources, unit XML reports, APK badging/install evidence, and Sprint 35-named visual E2E with access-lost fixture parity.
  - R2 launched on 2026-06-16 with prompt `evidence/sprint35_agent_inbox_folder_selector_repair/pro_review_r2/SPRINT35_GPT_PRO_REVIEW_PROMPT_R2.md`.
  - R2 bundle ZIP artifact: `SPRINT35_AGENT_INBOX_FOLDER_SELECTOR_REPAIR_R2_GPT_PRO_REVIEW_BUNDLE_20260616.zip`.
  - R2 ChatGPT URL: `https://chatgpt.com/c/6a314ea6-2ad8-83eb-b3da-7980bd829eeb`.
  - R2 result: `SCORE: 9/10`, `VERDICT: REVISE`, `VISUAL REVIEW: PASS`.
  - R2 output: `evidence/sprint35_agent_inbox_folder_selector_repair/pro_review_r2/GPT_PRO_REVIEW_R2.md`.
  - R2 finding addressed for R3: legacy `picker_folder` grants remain restoreable as repair state but are excluded from operational `hasAgentInboxDriveFolderGrant`, blocked in direct ViewModel scan/import calls, and rejected by repository scan-success persistence.
  - R3 launched on 2026-06-16 with prompt `evidence/sprint35_agent_inbox_folder_selector_repair/pro_review_r3/SPRINT35_GPT_PRO_REVIEW_PROMPT_R3.md`.
  - R3 bundle ZIP artifact: `SPRINT35_AGENT_INBOX_FOLDER_SELECTOR_REPAIR_R3_GPT_PRO_REVIEW_BUNDLE_20260616.zip`.
  - R3 ChatGPT URL: `https://chatgpt.com/c/6a3155b0-8178-83eb-9e9b-cc378abbcd30`.
  - R3 result: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`.
  - R3 output: `evidence/sprint35_agent_inbox_folder_selector_repair/pro_review_r3/GPT_PRO_REVIEW_R3.md`.
  - Release commit: `4060a3c`.
  - Release tag: `v0.11.23-agent-inbox-folder-selector-repair-alpha`.
  - Release URL: `https://github.com/dkacz/qualityalternative/releases/tag/v0.11.23-agent-inbox-folder-selector-repair-alpha`.
  - Integration method: committed and pushed the repair branch, tagged release commit `4060a3c`, pushed the tag, and published the GitHub release with debug and unsigned release APK artifacts.

## Sprint 34 Agent Inbox Readonly Link Fallback

Status: `release_published_gpt_pro_r2_pass_10_10`

- Date opened: 2026-06-16
- Trigger: GPT Pro R1 for Sprint 33 returned `SCORE: 8/10`, `VERDICT: REVISE`.
- R1 review output: `evidence/sprint33_agent_inbox_drive_file_picker/pro_review_r1/GPT_PRO_REVIEW_R1.md`.
- R1 findings addressed:
  - The controlled typed/manual readonly folder-id path is now reachable from Settings through a visible `Drive folder link or id` field and `Use Drive link` action.
  - Authorization tests now assert literal Google OAuth scope strings for `drive.file` and `drive.readonly`.
  - Removed the stale unreachable Google Drive document-tree branch after the early Picker redirect.
- Fix branch: `codex/agent-inbox-drive-tree-access-lost`.
- Validation so far:
  - Final local gate passed: `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home ./gradlew testDebugUnitTest lintDebug assembleRelease assembleDebug`.
  - `git diff --check` passed.
  - Debug APK artifact built with `versionCode=38`, `versionName=0.11.22-alpha`.
  - Release artifact: `release_artifacts/quality-alternative-v0.11.22-agent-inbox-readonly-link-fallback-alpha-debug.apk`.
  - SHA-256: `2bd452f4b37b5e92fa203940096474da7d35b092bb820d306e19e1bc2c280264`.
  - Connected visual/e2e was not run locally because `adb devices -l` showed no attached device and no Android emulator binary was available in the standard SDK paths checked.
- Release commit: `8c3d20e1858c125f96fa45c84282e6e3da0aed99`.
- Release tag: `v0.11.22-agent-inbox-readonly-link-fallback-alpha`.
- Release URL: `https://github.com/dkacz/qualityalternative/releases/tag/v0.11.22-agent-inbox-readonly-link-fallback-alpha`.
- Integration method: committed and pushed the R1 fixes and release gate on `codex/agent-inbox-drive-tree-access-lost`, tagged the release commit, pushed the tag, and published the GitHub release from the committed release notes.
- GPT Pro review:
  - R2 launched on 2026-06-16 with prompt `evidence/sprint34_agent_inbox_readonly_link_fallback/pro_review_r2/GPT_PRO_REVIEW_PROMPT_R2.md`.
  - R2 bundle ZIP artifact: `SPRINT34_AGENT_INBOX_READONLY_LINK_FALLBACK_R2_GPT_PRO_REVIEW_BUNDLE_20260616.zip`.
  - R2 ChatGPT URL: `https://chatgpt.com/c/6a31294a-f7ac-83eb-970f-1a1652c1c431`.
  - R2 result: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: NOT APPLICABLE`.
  - R2 output: `evidence/sprint34_agent_inbox_readonly_link_fallback/pro_review_r2/GPT_PRO_REVIEW_R2.md`.
- Next gate:
  - Install the published APK on the signed-in Android device and confirm the Google Drive folder picker opens instead of returning Google Play Services `INTERNAL_ERROR`; use the `Drive folder link or id` fallback if Picker grants still do not expose externally populated package children.

## Sprint 33 Agent Inbox Google Drive Picker Play Services Hotfix

Status: `release_published_superseded_by_sprint34_hotfix`

- Date opened: 2026-06-16
- Trigger: after installing the Sprint 32 APK, Settings > Agent Inbox showed `Google Drive authorization hit a Google Play services error. Retry Google Drive connection.` before the user could select a folder.
- Root cause: the Sprint 32 reconnect path requested `drive.readonly` together with Google Picker folder resource parameters. On the user's device, Google Play Services rejected that picker request with `INTERNAL_ERROR`.
- Fix branch: `codex/agent-inbox-drive-tree-access-lost`.
- Implementation state:
  - Removed the dedicated readonly folder picker authorization mode.
  - Agent Inbox folder picker now reuses the supported Google Picker request with `drive.file`, explicit folder selection parameters, consent prompt, and picked folder id extraction.
  - Existing typed/manual readonly folder-id support remains available for non-picker Drive readonly scans/imports.
  - Legacy Google Drive document-tree Agent Inbox grants and empty Agent Inbox states with Google Drive already configured now route through the supported picker request instead of the readonly picker.
- Validation so far:
  - Final local gate passed: `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home ./gradlew testDebugUnitTest lintDebug assembleRelease assembleDebug`.
  - `git diff --check` passed.
  - Debug APK artifact built with `versionCode=37`, `versionName=0.11.21-alpha`.
  - Release artifact: `release_artifacts/quality-alternative-v0.11.21-agent-inbox-drive-file-picker-alpha-debug.apk`.
  - SHA-256: `8390cf0fb2a09c0301e11cb9f850a0da3031e2c42f24db6ad8b76064b07760da`.
  - Connected visual/e2e was not run locally because `adb devices -l` showed no attached device and no Android emulator binary was available in the standard SDK paths checked.
- Release commit: `28bbf7f8cedb619c976eac05f1c55b8cb9f5407a`.
- Release tag: `v0.11.21-agent-inbox-drive-file-picker-alpha`.
- Release URL: `https://github.com/dkacz/qualityalternative/releases/tag/v0.11.21-agent-inbox-drive-file-picker-alpha`.
- Integration method: committed and pushed the release gate on `codex/agent-inbox-drive-tree-access-lost`, tagged the release commit, pushed the tag, and published the GitHub release from the committed release notes.
- GPT Pro review:
  - R1 launched on 2026-06-16 with prompt `evidence/sprint33_agent_inbox_drive_file_picker/pro_review_r1/GPT_PRO_REVIEW_PROMPT_R1.md`.
  - R1 bundle ZIP artifact: `SPRINT33_AGENT_INBOX_DRIVE_FILE_PICKER_R1_GPT_PRO_REVIEW_BUNDLE_20260616.zip`.
  - R1 ChatGPT URL: `https://chatgpt.com/c/6a31231c-4224-83eb-a48e-cacaa7fd2a6d`.
  - R1 result: `SCORE: 8/10`, `VERDICT: REVISE`, `VISUAL REVIEW: NOT APPLICABLE`.
  - R1 findings: typed/manual readonly fallback was documented but not reachable from UI; bundle omitted literal scope definitions and concrete Drive client evidence.
- Next gate:
  - Superseded by Sprint 34 before claiming review-clean status.

## Sprint 32 Agent Inbox Google Drive Document-Tree Access Lost Regression

Status: `release_published_superseded_by_sprint33_hotfix`

- Date opened: 2026-06-16
- Trigger: after installing the Sprint 31 APK, the app no longer shows false `Package is missing manifest.json`, but Agent Inbox is now `OFF` with `Agent Inbox folder access was lost. Connect the folder again.`
- Screenshot state: Settings > Agent Inbox shows `Agent Inbox folder not selected`; the folder grant has been cleared from app state.
- Working hypothesis: Sprint 31 routed Google Drive-backed `content://.../tree/...` folder grants through Drive API, but some real Google Drive document-tree URIs may not encode the target folder ID in the simple `doc=<folderId>` shape. The hybrid client then throws `AgentInboxDriveAccessLostException`, and `MainViewModel` clears the saved folder grant.
- Fix branch: `codex/agent-inbox-drive-tree-access-lost`.
- Implementation state:
  - Added an Agent Inbox Google Drive folder picker reconnect path that requested `drive.readonly` with Google Picker folder parameters and returned the selected folder ID.
  - Empty Agent Inbox state now starts the Google Drive picker when Google Drive is already configured; local Android document-tree selection remains available when Drive is not configured.
  - Selecting a Google Drive folder through Android `OpenDocumentTree` now redirects to the Google Drive picker instead of persisting/scanning the provider URI.
  - Legacy Google Drive document-tree Agent Inbox grants now show a reconnect state and route `Scan now` through the Google Drive picker instead of attempting another scan through the fragile `content://tree` URI.
  - Post-release device feedback: `drive.readonly` combined with Google Picker folder parameters fails on-device with Google Play Services `INTERNAL_ERROR`, so Sprint 33 replaces the picker scope while keeping readonly for non-picker typed folder IDs.
- Validation so far:
  - Targeted `GoogleDriveAuthorizationTest` and `GoogleDriveAuthorizationUiTest` passed with JDK 17.
  - Final local gate passed after version bump: `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home ./gradlew testDebugUnitTest lintDebug assembleRelease assembleDebug`.
  - Signed debug APK artifact built with `versionCode=36`, `versionName=0.11.20-alpha`.
  - Release artifact: `release_artifacts/quality-alternative-v0.11.20-agent-inbox-drive-picker-alpha-debug.apk`.
  - SHA-256: `e7dc89166cdfd3406796a1f89e491bdbec1af850c830134ff3899371464c17c7`.
  - Connected visual/e2e was not run locally because `adb devices -l` showed no attached device and the machine has AVD definitions but no accessible Android emulator binary in the usual SDK paths.
- Release commit: `38b96dade376228d3fca8293b227915ac487deae`.
- Release tag: `v0.11.20-agent-inbox-drive-picker-alpha`.
- Release URL: `https://github.com/dkacz/qualityalternative/releases/tag/v0.11.20-agent-inbox-drive-picker-alpha`.

## Sprint 31 Agent Inbox Google Drive Document-Tree Manifest Visibility

Status: `release_published`

- Date opened: 2026-06-16
- Trigger: after Sprint 30 release, the app screenshot still shows multiple Drive Agent Inbox folders such as `hegel-maybee-kant-obiektywnosc-a-intersubiektywnosc` in `NEEDS PACKAGE CLEANUP` with `Package is missing manifest.json`.
- Direct Drive inspection confirmed the package shape is valid: the selected Agent Inbox parent `10adaGo_eN3Pnb-FplpkJfxF4cNJCXiz-` contains six package folders, and each inspected package has direct `manifest.json`, `content.md`, and image children.
- Root cause: the app trusted Android's Google Drive document-tree provider listing. That provider can expose package folders while not exposing their nested files to the app, so the review factory saw an incomplete `allFiles` list and mislabeled valid Drive packages as missing `manifest.json`.
- Fix branch: `codex/agent-inbox-document-tree-manifest-visibility`.
- Implementation state:
  - Google Drive-backed document-tree URIs now require Drive readonly authorization for scan/import.
  - The hybrid Agent Inbox Drive client extracts `doc=<folderId>` from Google Drive document-tree URIs and scans packages through the existing Drive API client when a token is available.
  - The original `content://...` folder URI is preserved in app state/settings after Drive API scans so Android permission release and UI status stay coherent.
  - Local Android document-tree folders still scan and import without a Google token.
- Evidence:
  - Final local gate passed after version bump: `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home ./gradlew testDebugUnitTest lintDebug assembleRelease`.
  - Signed debug APK artifact built with `versionCode=35`, `versionName=0.11.19-alpha`.
  - Release artifact: `release_artifacts/quality-alternative-v0.11.19-agent-inbox-manifest-visibility-alpha-debug.apk`.
  - SHA-256: `e6b83c4adcff52ed13e45485a6f9cef5012759a0894aa8f7c6650a8a1b61a79d`.
  - Connected visual/e2e was not run locally because `adb devices -l` showed no attached device and the machine has AVD definitions but no accessible Android emulator binary in the usual SDK paths.
  - GPT Pro R1 returned `VERDICT: PASS`, `SCORE: 10/10`, no blockers, and accepted the connected-test gap for this routing/state fix.
- Review bundle: `ARCHITECT_REVIEW_BUNDLE_sprint31_agent_inbox_manifest_visibility.zip`.
- Release commit: `8221eade1bdeee5e06374ffb43ef7d2480f758eb`.
- Release tag: `v0.11.19-agent-inbox-manifest-visibility-alpha`.
- Release URL: `https://github.com/dkacz/qualityalternative/releases/tag/v0.11.19-agent-inbox-manifest-visibility-alpha`.

## Sprint 30 Agent Inbox Large Image Import Fix

Status: `release_published`

- Branch: `codex/agent-inbox-large-image-import-fix`
- Scope: fix Agent Inbox Markdown image sidecar imports that failed opaquely around 2.4 MiB despite the documented 5 MiB per-image limit.
- Canonical sprint plan: `docs/SPRINT_30_AGENT_INBOX_LARGE_IMAGE_IMPORT_FIX.md`
- Current implementation state:
  - Import-time exceptions are logged with class/message/stacktrace through `Log.e`.
  - Failed Agent Inbox candidates now carry `importFailureDetail` so UI detail text can show the exception class/message.
  - Image sidecar write `IOException`/`OutOfMemoryError` maps to `IMAGE_WRITE_FAILED` instead of generic `LOCAL_IMPORT_REJECTED`.
  - Drive and document-tree download paths accept expected byte sizes and avoid unhinted buffer growth when metadata provides bounded file size.
  - `FileAgentInboxDocumentStore` verifies content SHA from existing bytes instead of re-reading the temp file.
  - R1 fix: sidecar temp-file plan creation now happens inside the same cleanup/wrap scope as sidecar writes, so pre-write `IOException`/`OutOfMemoryError` maps to `IMAGE_WRITE_FAILED` and temp files are cleaned.
  - R1 fix: image-write failure detail unwraps nested `AgentInboxImageAttachmentWriteException` wrappers to show the root class/message.
  - A 3.5 MiB Markdown image sidecar regression test imports successfully under the existing 5 MiB contract.
  - A sidecar temp-file creation regression test verifies `IMAGE_WRITE_FAILED` mapping and cleanup.
  - The unrelated Chrome connected-test evidence path now uses app-scoped external files instead of a stale public Downloads directory.
  - GPT Pro R1 returned `SCORE 8/10`, `VERDICT REVISE`, `VISUAL REVIEW NOT APPLICABLE`; R2 fixed the sidecar temp-file creation gap.
  - GPT Pro R2 returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW NOT APPLICABLE`; no findings remain and release readiness is approved.
  - Release gate passed for `v0.11.18-agent-inbox-large-image-import-fix-alpha`: `versionCode=34`, `versionName=0.11.18-alpha`, final local Gradle gate PASS, final connected Android gate PASS 138/138, APK badging/signature/install/launch PASS.
  - Release artifact: `release_artifacts/quality-alternative-v0.11.18-agent-inbox-large-image-import-fix-alpha-debug.apk`
  - SHA-256: `705c344ade36cd96753183c967f46908a647c4d3310c78f10adb268f0047ab8b`
  - Release gate summary: `docs/release-gate-logs/2026-06-16-sprint30-agent-inbox-large-image-import-fix/VALIDATION_SUMMARY.md`
  - Release notes: `docs/release-gate-logs/2026-06-16-sprint30-agent-inbox-large-image-import-fix/RELEASE_NOTES_v0.11.18-agent-inbox-large-image-import-fix-alpha.md`
  - Release commit: `a1f257692860004e6101d311aded3472c2f8a1b6` (`Prepare Sprint 30 agent inbox image import release`).
  - Release tag: `v0.11.18-agent-inbox-large-image-import-fix-alpha`.
  - Release URL: `https://github.com/dkacz/qualityalternative/releases/tag/v0.11.18-agent-inbox-large-image-import-fix-alpha`.
  - Published assets: `quality-alternative-v0.11.18-agent-inbox-large-image-import-fix-alpha-debug.apk` and `quality-alternative-v0.11.18-agent-inbox-large-image-import-fix-alpha-debug.apk.sha256`.
  - Integration method: committed the release gate on `codex/agent-inbox-large-image-import-fix`, tagged the release commit, pushed branch and tag to `origin`, and published the GitHub release from the committed release notes.
- Validation:
  - Passed: targeted Agent Inbox unit tests for `AgentInboxPackageImporterTest` and `MainViewModelTest`.
  - Passed: full `testDebugUnitTest`.
  - Passed: `lintDebug`, `compileDebugAndroidTestKotlin`, and `assembleDebug`.
  - Passed: targeted Chrome evidence-path rerun after the app-scoped evidence fix.
  - Passed after the R1 fix: full `connectedDebugAndroidTest` on `qaApi36(AVD) - 16`, 138/138 tests, 0 skipped, 0 failed.
  - Passed: `git diff --check`.
- Evidence:
  - Sprint doc: `docs/SPRINT_30_AGENT_INBOX_LARGE_IMAGE_IMPORT_FIX.md`
  - Bug report/fix record: `docs/AGENT_INBOX_LARGE_IMAGE_IMPORT_BUG.md`
  - Unit and connected XML: `evidence/sprint30_agent_inbox_large_image_import_fix/logs/`
  - Review patch: `evidence/sprint30_agent_inbox_large_image_import_fix/review/current_patch.diff`
  - GPT Pro R1 output: `evidence/sprint30_agent_inbox_large_image_import_fix/review/GPT_PRO_REVIEW_R1.md`
  - GPT Pro R2 output: `evidence/sprint30_agent_inbox_large_image_import_fix/review/GPT_PRO_REVIEW_R2.md`
- Next gate:
  - Merge/cherry-pick the release branch into the stable integration branch when ready.

## Sprint 29 Agent Inbox Folder Selector

Status: `release_published`

- Branch: `codex/sprint29-agent-inbox-folder-selector`
- Scope: remove the need to paste an Agent Inbox Drive folder URL/id by making Android's normal folder picker the primary connection path.
- Canonical sprint plan: `docs/SPRINT_29_AGENT_INBOX_FOLDER_SELECTOR.md`
- Current implementation state:
  - Added `AGENT_INBOX_DRIVE_GRANT_MODE_DOCUMENT_TREE_FOLDER`.
  - Added a document-tree Agent Inbox client that scans direct child folders under a persisted `content://tree/...` URI and downloads manifest/content/image files through the selected tree grant.
  - Added a hybrid production client so existing Google Drive API folder ids still route through the Drive API, while new system-folder grants route through DocumentsProvider.
  - Settings disconnected Agent Inbox state now shows `Choose folder` and launches `OpenDocumentTree`; it no longer shows the pasted Drive folder URL/id field in the primary UX.
  - Document-tree scan/import no longer require a Google OAuth token; the persisted URI read grant is the authorization boundary.
  - Disconnect releases the persisted document-tree read permission.
  - Historical `drive.readonly` compatibility remains for already-connected states, but the disconnected primary UX is the folder selector.
  - GPT Pro R1 returned `SCORE 8/10`, `VERDICT REVISE`, `VISUAL REVIEW REVISE`; R2 fixes the access-loss stream handling and stale Drive visual copy.
  - R2 visual evidence uses `VisualQaScreenshotTest#captureSprint29AgentInboxFolderSelectorStates`, opens Android DocumentsUI, selects the `Documents` folder through the system picker, returns through the real ActivityResult callback, and captures the connected folder state.
  - R2 adds direct Portable Profile exporter evidence that raw `content://tree/...` Agent Inbox folder URIs are omitted from profile JSON.
  - GPT Pro R2 returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`; fresh findings none, bundle gaps none.
  - Final release gate passed for `v0.11.17-agent-inbox-folder-selector-alpha`: `versionCode=33`, `versionName=0.11.17-alpha`, final local Gradle gate PASS, final connected Android gate PASS 138/138, APK badging/signature/install/launch PASS.
  - Release artifact: `release_artifacts/quality-alternative-v0.11.17-agent-inbox-folder-selector-alpha-debug.apk`
  - SHA-256: `753362b76fdd0110fd15668a1215cbe6e1291b674efca9dc9c94e61c8d9b0fec`
  - Release gate summary: `docs/release-gate-logs/2026-06-15-sprint29-agent-inbox-folder-selector/VALIDATION_SUMMARY.md`
  - Release notes: `docs/release-gate-logs/2026-06-15-sprint29-agent-inbox-folder-selector/RELEASE_NOTES_v0.11.17-agent-inbox-folder-selector-alpha.md`
  - Release commit: `9e88d7c0b081b43ace7b01f54ecb35f9c5e34ae9` (`Prepare Sprint 29 agent inbox folder selector release`).
  - Release tag: `v0.11.17-agent-inbox-folder-selector-alpha`.
  - Release URL: `https://github.com/dkacz/qualityalternative/releases/tag/v0.11.17-agent-inbox-folder-selector-alpha`.
  - Published assets: `quality-alternative-v0.11.17-agent-inbox-folder-selector-alpha-debug.apk` and `quality-alternative-v0.11.17-agent-inbox-folder-selector-alpha-debug.apk.sha256`.
  - Integration method: committed on `codex/sprint29-agent-inbox-folder-selector`, tagged the release commit, pushed branch and tag to `origin`, and published the GitHub release from the committed release notes.
- Validation:
  - Passed: targeted `MainViewModelTest`, `PreferencesSettingsRepositoryTest`, and `AccountLightProfileExporterTest`.
  - Passed: full local gate `testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:lintDebug :app:processReleaseManifestForPackage :app:assembleDebug`.
  - Passed: focused connected visual E2E on `qaApi36(AVD) - 16` for `VisualQaScreenshotTest#captureSprint29AgentInboxFolderSelectorStates`.
  - Passed: full `connectedDebugAndroidTest` on `qaApi36(AVD) - 16`, 138/138 tests, 0 skipped, 0 failed.
  - Passed: `git diff --check`.
  - Passed: release-gate final Gradle build, APK badging, signature verification, emulator install, and explicit launch smoke.
- Evidence:
  - Visual selector contact sheet: `evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r2/contact_sheet_selector_r2.png`
  - Visual selector screenshots: `evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r2/sprint29-agent-inbox-folder-selector-1781513593337/`
  - Focused visual result XML/logcat: `evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r2/TEST-sprint29-selector-visual-r2.xml`, `evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r2/logcat-sprint29-selector-visual-r2.txt`
  - Full connected log/XML: `evidence/sprint29_agent_inbox_folder_selector/logs/full_connected_debug_android_test_r2.log`, `evidence/sprint29_agent_inbox_folder_selector/logs/TEST-full-connected-debug-android-test-r2.xml`
  - Local gate log: `evidence/sprint29_agent_inbox_folder_selector/logs/full_local_gate_r2.log`
  - GPT Pro R2 output: `evidence/sprint29_agent_inbox_folder_selector/GPT_PRO_REVIEW_R2.md`
  - Release gate summary: `docs/release-gate-logs/2026-06-15-sprint29-agent-inbox-folder-selector/VALIDATION_SUMMARY.md`
  - Release APK: `release_artifacts/quality-alternative-v0.11.17-agent-inbox-folder-selector-alpha-debug.apk`
- Next gate:
  - Decide whether to merge the release branch back to `main`.

## Sprint 28 Agent Inbox Drive Access Fix

Status: `release_gate_passed`

- Branch: `codex/sprint28-agent-inbox-drive-access`
- Scope: fix the post-release Agent Inbox Drive access gap for packages uploaded later by rclone/external agents under the current `drive.file` model, and close the added Markdown image attachment gap for manual imports plus Agent Inbox Markdown packages.
- Canonical sprint plan: `docs/SPRINT_28_AGENT_INBOX_DRIVE_ACCESS.md`
- Current implementation state:
  - Sprint opened from Sprint 27 release branch after recording the `drive.file`/rclone diagnosis.
  - Initial decision was Picker-first: keep `drive.file`, require explicit Google Picker folder selection for Agent Inbox, and only consider `drive.readonly` if a selected folder grant cannot be obtained or does not expose later-added package children.
  - Current validation/evidence summary: `evidence/sprint28_agent_inbox_drive_access/VALIDATION_SUMMARY.md`
  - Live rclone/Picker spike checklist: `evidence/sprint28_agent_inbox_drive_access/device_spike/RCLONE_PICKER_FOLDER_SPIKE.md`
  - `play-services-auth` is bumped to `21.6.0` because Google Play services release notes state this version adds `PICKER_ALLOW_FOLDER_SELECTION`.
  - App-side Picker-folder authorization is implemented with `PICKER_OAUTH_TRIGGER=true`, `PICKER_ALLOW_FOLDER_SELECTION=true`, consent prompt, and opt-out from previously granted scopes.
  - Agent Inbox scan now requires a selected folder id and no longer silently creates a separate app-owned inbox folder.
  - Selected-folder Drive 401/403/404 scan failures are treated as access-lost states that clear the local folder grant, show reconnect copy, and record privacy-safe failure analytics.
  - GPT Pro R1 returned `SCORE 7/10`, `VERDICT BLOCK`, `VISUAL REVIEW REVISE`; the blockers were legacy Sprint 27 app-created folder ids bypassing Picker and a connected-without-folder Settings state.
  - R1 fixes are implemented locally: Agent Inbox connection now requires durable `agent_inbox_drive_grant_mode=picker_folder`, legacy folder ids without that marker hydrate as disconnected, `saveAgentInboxDriveConnection(null)` clears connection state, and Settings copy/actions derive connected state from the Picker grant predicate.
  - Manual Markdown image attachment imports now allow image-only follow-up picker results to merge into the already selected Markdown file while preserving edited title, selected topics, and priority.
  - Agent Inbox Markdown packages can carry bounded safe sidecar images through review, Drive download, local Agent Inbox document storage, `UserDocumentDraft.imageAttachmentUris`, and reader rendering; EPUB package sidecars remain invalid.
  - GPT Pro R2 returned `SCORE 7/10`, `VERDICT REVISE`, `VISUAL REVIEW PASS`; the remaining findings were duplicate/colliding sidecar names, unreviewed Markdown local-image fallback, and incomplete sidecar rollback on mid-write failure.
  - R2 fixes are implemented locally: duplicate/colliding sidecar names are invalid during review, Agent Inbox Markdown reader rendering disables local fallback for stored Agent Inbox files, and sidecar writes use temp/backup rollback that removes promoted files after later failures.
  - GPT Pro R3 returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`; no fresh findings.
  - Historical R3 visual E2E coverage for disconnected Picker connect, selected-folder, access-lost reconnect, Agent Inbox Markdown image reader, and dark selected-folder states passed in `VisualQaScreenshotTest#captureSprint28AgentInboxDriveAccessStates`.
  - Canonical Sprint 28 R3 visual evidence: `evidence/sprint28_agent_inbox_drive_access/visual_e2e_r3/contact_sheet_r3.png` and `evidence/sprint28_agent_inbox_drive_access/visual_e2e_r3/sprint28-agent-inbox-drive-access-1781437194813/`.
  - Live Picker runtime evidence on signed-in `qaApi36` failed before folder selection: Google Play Services showed account selection, then returned to the app with `No Agent Inbox folder was selected`; no Drive folder Picker appeared and no folder id was returned.
  - The controlled `drive.readonly` fallback is implemented locally: Settings accepts an Agent Inbox Drive folder URL/id, validates and normalizes the folder id, requests explicit read-only Drive consent, persists `agent_inbox_drive_grant_mode=readonly_folder`, scans only the saved folder id, and revokes the read-only scope on disconnect.
  - Fallback UX copy says Drive read access is used only after consent and that the app scans only the pasted folder id.
  - Fallback analytics remain privacy-safe: connection metadata records only `grantMode=readOnlyFolder`, and raw folder ids/file ids/package ids/content names/raw failure text stay out of remote-safe analytics and Portable Profile.
  - Current readonly visual evidence: `evidence/sprint28_agent_inbox_drive_access/visual_e2e_readonly_r1/contact_sheet_readonly_r1.png` and `evidence/sprint28_agent_inbox_drive_access/visual_e2e_readonly_r1/sprint28-agent-inbox-drive-access-1781460684272/`.
  - Current live rclone fallback evidence: `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/RESULT.md` and `evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/live_readonly_rclone_success.png`.
  - Live readonly fallback passed on `qaApi36`: after explicit Google consent, the app scanned the pasted Drive folder id and displayed the rclone-uploaded package as one package waiting for review.
  - GPT Pro R4 readonly fallback review returned `SCORE 9/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`; the only fresh finding was evidence/package hygiene: the live result overstated a two-line logcat as auth-flow proof and referenced rclone listing JSON files excluded from the bundle.
  - R4 evidence-hygiene fixes are implemented locally: live RESULT now relies on final-state screenshot/UI XML, `rclone_listing_summary_redacted.md` replaces raw Drive-id listing evidence for review, raw Gradle logs are available, and Sprint 27 visual seeding now writes a durable `picker_folder` grant marker before scan success.
  - GPT Pro R5 evidence-hygiene lane was launched at `https://chatgpt.com/c/6a2f0b18-a1e0-83ed-a622-e228bc775631` with bundle `SPRINT28_R5_EVIDENCE_HYGIENE_REVIEW_BUNDLE_20260614.zip`.
  - GPT Pro R5 returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`; fresh findings none, bundle gaps none, package hygiene clean enough for this lane.
  - Final release gate passed for `v0.11.16-agent-inbox-drive-access-alpha`: `versionCode=32`, `versionName=0.11.16-alpha`, final local Gradle gate PASS, final connected Android gate PASS 138/138, APK badging/signature/install/launch PASS.
  - Release artifact: `release_artifacts/quality-alternative-v0.11.16-agent-inbox-drive-access-alpha-debug.apk`
  - SHA-256: `acb460d2ca6e8e1129607eed43171464eef192f7e470f4ef82dcda7286e5841d`
  - Release gate summary: `docs/release-gate-logs/2026-06-14-sprint28-agent-inbox-drive-access/VALIDATION_SUMMARY.md`
  - Release notes: `docs/release-gate-logs/2026-06-14-sprint28-agent-inbox-drive-access/RELEASE_NOTES_v0.11.16-agent-inbox-drive-access-alpha.md`
  - Release commit: `646d6f79551e79e0a4a4f5204919348c78968ab9` (`Complete Sprint 28 Agent Inbox Drive access release`).
  - Release tag: `v0.11.16-agent-inbox-drive-access-alpha`.
  - Release URL: `https://github.com/dkacz/qualityalternative/releases/tag/v0.11.16-agent-inbox-drive-access-alpha`.
  - Integration method: committed on `codex/sprint28-agent-inbox-drive-access`, tagged the release commit, pushed branch and tag to `origin`, and published the GitHub release from the committed release notes.
- Validation:
  - Passed: full `testDebugUnitTest`.
  - Passed: targeted Markdown image/Agent Inbox tests for `DocumentImportCandidateFactoryTest`, `MainViewModelTest`, `AgentInboxReviewCandidateFactoryTest`, and `AgentInboxPackageImporterTest`.
  - Passed: targeted `testDebugUnitTest` for `GoogleDriveAuthorizationTest`, `MainViewModelTest`, and `AndroidGoogleDriveAgentInboxClientTest`.
  - Passed after R1 fixes: targeted rerun for `PreferencesSettingsRepositoryTest` and `MainViewModelTest`.
  - Passed: `compileDebugAndroidTestKotlin`.
  - Passed: targeted R2-fix unit tests for `AgentInboxReviewCandidateFactoryTest`, `AgentInboxPackageImporterTest`, `MarkdownReaderDocumentParserTest`, and `RoomUserDocumentRepositoryTest`.
  - Passed: focused connected visual E2E on `qaApi36(AVD) - 16` for `VisualQaScreenshotTest#captureSprint28AgentInboxDriveAccessStates`.
  - Passed: `lintDebug`, `processReleaseManifestForPackage`, and `assembleDebug`.
  - Passed: `git diff --check`.
  - Passed for readonly fallback: targeted `GoogleDriveAuthorizationTest`, `MainViewModelTest`, and `PreferencesSettingsRepositoryTest`.
  - Passed for readonly fallback: focused connected visual E2E on `qaApi36(AVD) - 16` for `VisualQaScreenshotTest#captureSprint28AgentInboxDriveAccessStates`.
  - Passed for readonly fallback: `assembleDebug`.
  - Passed for readonly fallback: live signed-in emulator/rclone scan.
  - Passed after R4 evidence fix: targeted Chrome verified-host rerun, targeted Sprint 27 Agent Inbox visual rerun, targeted Sprint 28 readonly visual rerun, full local unit/compile/lint/release-manifest/build gate, and fresh full connected Android run with 138/138 tests.
  - Historical exploratory full connected run after R4 failed 2/138 before targeted reruns: Chrome verified-host test passed when rerun alone, Sprint 27 visual passed after the durable grant seed fix, and the fresh R5 full connected run passed.
- Next gate:
  - Commit, tag, push, and publish the GitHub release if this local release gate is accepted for publication.
  - Preflight APK install/launch evidence remains at `docs/release-gate-logs/2026-06-14-sprint28-agent-inbox-drive-access-preflight/`, but the final release gate is now `docs/release-gate-logs/2026-06-14-sprint28-agent-inbox-drive-access/`.

## Sprint 27 Agent Content Inbox

Status: `release_published`

- Branch: `codex/sprint-agent-content-inbox`
- Scope: add a Google Drive-backed Agent Inbox so Codex/Claude-style agents can hand private Markdown or EPUB content to the app through a bounded package contract, with manifest priority requiring explicit user confirmation.
- Canonical sprint plan: `docs/SPRINT_27_AGENT_CONTENT_INBOX.md`
- Current implementation state:
  - Drive Agent Inbox scanning, review, import, duplicate detection, disconnect, settings UI, analytics, and Portable Profile metadata-only export/import are implemented locally.
  - Manifest priority is shown during review and is opt-in before it affects ranking.
  - Import accepts one reviewed private Markdown or EPUB content file through the existing document model; raw Drive content file names and raw document SHA values are not exported in Portable Profile.
  - Visual E2E covers Settings review, manifest priority opt-in, duplicate/rejected packages, imported reader content, EPUB rendering, and Portable Profile privacy copy.
  - R1 GPT Pro review returned `SCORE 6/10`, `VERDICT BLOCK`, `VISUAL REVIEW REVISE`; blockers were priority auto-acceptance, unbounded scan/review, missing disconnect UI, raw SHA export, weak duplicate/SHA visibility, and incomplete visual evidence.
  - R1 blockers were fixed with explicit priority opt-in, bounded scan and package review, disconnect UI, metadata-only Portable Profile export, duplicate/SHA mismatch review states, and expanded visual evidence.
  - R2 GPT Pro review returned `SCORE 6/10`, `VERDICT BLOCK`, `VISUAL REVIEW PASS`; blockers were missing cryptographic binding from reviewed content to import, raw Drive content file names leaking through `sourceLabel`, loose package cardinality, and unbounded manifest download.
  - R2 blockers were fixed with reviewed content SHA/size binding at import, neutral imported document source label, strict one manifest plus one content file contract, unsupported extra-file rejection, and a 64 KiB manifest cap before and after metadata download.
  - Validation passed after R2 fixes: `testDebugUnitTest`, `compileDebugAndroidTestKotlin`, and connected `VisualQaScreenshotTest#captureSprint27AgentInboxReviewScreens`.
  - R3 GPT Pro review returned `SCORE 6/10`, `VERDICT BLOCK`, `VISUAL REVIEW PASS`; blockers were same folder/name changed-byte overwrite risk, duplicate review from unverified manifest SHA, unbounded network reads when Drive metadata is missing/wrong, missing reject/remove path and rejected analytics, unqualified first-connect folder name search, and production fixture resolution/manifest risk.
  - R3 blockers were fixed with verified-SHA content-addressed Agent Inbox storage, duplicate status only from actual reviewed content SHA, bounded Drive client downloads with typed too-large handling, visible Remove action plus `AGENT_INBOX_CANDIDATE_REJECTED`, first-connect folder creation with persisted id, and BuildConfig/debug-manifest fixture gating.
  - R4 validation passed: 498 debug unit testcases, `compileDebugAndroidTestKotlin`, `lintDebug`, `processReleaseManifestForPackage`, `assembleDebug`, `git diff --check`, and connected `VisualQaScreenshotTest#captureSprint27AgentInboxReviewScreens`.
  - R4 GPT Pro review returned `SCORE 7/10`, `VERDICT REVISE`, `VISUAL REVIEW REVISE`; findings were non-size Drive download failures collapsing a scan, duplicate safety depending on an unhydrated in-memory document list, clipped priority-control labels in screenshots, and a connected-logcat bundle gap.
  - R5 fixes the R4 findings with package-level `DOWNLOAD_UNAVAILABLE`, a user-document readiness gate, repository/DAO duplicate lookup by verified fingerprint, full-width priority confirmation UI plus screenshot assertions, a fresh canonical visual run, and standalone connected logcat evidence.
  - R5 validation passed: 502 debug unit testcases, `compileDebugAndroidTestKotlin`, `lintDebug`, `processReleaseManifestForPackage`, `assembleDebug`, connected `VisualQaScreenshotTest#captureSprint27AgentInboxReviewScreens`, and `git diff --check`.
  - R5 GPT Pro review returned `SCORE 8/10`, `VERDICT BLOCK`, `VISUAL REVIEW PASS`; the remaining blocker was non-atomic same-fingerprint duplicate prevention plus duplicate import results leaving candidates visually ready.
  - R6 fixes the R5 blocker with `addDocumentIfFingerprintAbsent`, Room repository write mutex serialization, verified fingerprint fields on `UserDocumentDraft`, ViewModel import single-flight, duplicate-state updates after import-time duplicates, and same-scan same-SHA sibling duplicate marking after first import.
  - R6 validation passed: 505 debug unit testcases, `compileDebugAndroidTestKotlin`, `lintDebug`, `processReleaseManifestForPackage`, `assembleDebug`, connected `VisualQaScreenshotTest#captureSprint27AgentInboxReviewScreens`, and `git diff --check`.
  - R6 GPT Pro review returned `SCORE 8/10`, `VERDICT BLOCK`, `VISUAL REVIEW PASS`; the remaining blocker was import-time invalid/rejected/download-failure paths leaving rows visually READY with stale reviewed fingerprints.
  - R7 fixes the R6 blocker by converting import-time invalid/rejected/download-failure results into finite non-importable invalid candidates, clearing stale reviewed fingerprint/size, clearing accepted priority, adding `LOCAL_IMPORT_REJECTED` copy, and covering changed-after-review, oversize, download failure, and repository rejection with ViewModel tests.
  - R7 validation passed: 508 debug unit testcases, `compileDebugAndroidTestKotlin`, `lintDebug`, `processReleaseManifestForPackage`, `assembleDebug`, connected `VisualQaScreenshotTest#captureSprint27AgentInboxReviewScreens`, and `git diff --check`.
  - R7 GPT Pro review returned `SCORE 9/10`, `VERDICT REVISE`, `VISUAL REVIEW PASS`; the remaining finding was invisible local retention of private Agent Inbox files after post-write duplicate/rejected results.
  - R8 fixes the R7 finding with `AgentInboxDocumentStore.deleteDocument`, guarded File store deletion under the Agent Inbox root, importer cleanup on duplicate/rejected/exception after write, and tests for concurrent duplicate cleanup, repository rejection cleanup, and atomic-add exception cleanup.
  - R8 validation passed: 509 debug unit testcases, `compileDebugAndroidTestKotlin`, `lintDebug`, `processReleaseManifestForPackage`, `assembleDebug`, connected `VisualQaScreenshotTest#captureSprint27AgentInboxReviewScreens`, and `git diff --check`.
  - R8 GPT Pro review returned `SCORE 9/10`, `VERDICT REVISE`, `VISUAL REVIEW PASS`; the remaining finding was direct final-path writes in `FileAgentInboxDocumentStore.writeDocument`, which could leave a stale partial/mismatching final file after a write failure and block later valid import.
  - R9 fixes the R8 finding with scoped temp-file writes, SHA-256 verification before final promotion, atomic move with filesystem fallback, stale mismatching final-file replacement, temp cleanup in `finally`, and a regression test for stale-final replacement.
  - R9 validation passed: 510 debug unit testcases, `compileDebugAndroidTestKotlin`, `lintDebug`, `processReleaseManifestForPackage`, `assembleDebug`, connected `VisualQaScreenshotTest#captureSprint27AgentInboxReviewScreens`, and `git diff --check`.
  - R9 GPT Pro review returned `SCORE 9/10`, `VERDICT REVISE`, `VISUAL REVIEW REVISE`; the remaining finding was that screenshots `06`-`09` were generic user-document seed smoke evidence instead of proof that actual Agent Inbox import renders correctly in Library, intervention, Markdown reader, and EPUB reader.
  - R10 fixes the R9 visual finding by generating imported-content screenshots after `scanAgentInboxDrive` and `importAgentInboxCandidate` against a debug-gated fake Drive client, accepting Markdown priority before import, asserting verified fingerprint and neutral `Agent Inbox document` provenance, and asserting raw Drive content file names/package ids are not visible.
  - R10 validation passed: 510 debug unit testcases, `compileDebugAndroidTestKotlin`, `lintDebug`, `processReleaseManifestForPackage`, `assembleDebug`, connected `VisualQaScreenshotTest#captureSprint27AgentInboxReviewScreens`, and `git diff --check`.
  - R10 GPT Pro review returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`; no fresh findings, no bundle gaps, and package hygiene clean enough for release-gate audit.
- Current evidence:
  - Validation summary: `evidence/sprint27_agent_content_inbox/VALIDATION_SUMMARY.md`
  - Review bundle manifest: `evidence/sprint27_agent_content_inbox/REVIEW_BUNDLE_MANIFEST.md`
  - Visual contact sheet: `evidence/sprint27_agent_content_inbox/visual_e2e/sprint27_agent_inbox_contact_sheet.png`
  - Canonical screenshot run: `evidence/sprint27_agent_content_inbox/visual_e2e/sprint27-agent-content-inbox-1781272063934/`
  - GPT Pro R1 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R1.md`
  - GPT Pro R2 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R2.md`
  - GPT Pro R3 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R3.md`
  - GPT Pro R4 lane: `https://chatgpt.com/c/6a2bdd0d-b5e8-83eb-972d-813bd00130f7`
  - GPT Pro R4 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R4.md`
  - GPT Pro R5 lane: `https://chatgpt.com/c/6a2be5be-1d60-83eb-b0ba-01b633e2bcd1`
  - GPT Pro R5 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R5.md`
  - GPT Pro R6 lane: `https://chatgpt.com/c/6a2bed8f-7c34-83ed-a148-9e749cf8a099`
  - GPT Pro R6 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R6.md`
  - GPT Pro R7 lane: `https://chatgpt.com/c/6a2bf46d-43bc-83eb-8125-c1002b3ea3dd`
  - GPT Pro R7 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R7.md`
  - GPT Pro R8 lane: `https://chatgpt.com/c/6a2bfb46-2550-83eb-a06a-889265bc2c57`
  - GPT Pro R8 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R8.md`
  - GPT Pro R9 lane: `https://chatgpt.com/c/6a2c059d-9764-83ed-b808-4538ad6a3160`
  - GPT Pro R9 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R9.md`
  - GPT Pro R10 lane: `https://chatgpt.com/c/6a2c0f4a-b3e0-83eb-97a2-4762aeb641b2`
  - GPT Pro R10 output: `evidence/sprint27_agent_content_inbox/pro_review_harvest/GPT_PRO_REVIEW_R10.md`
  - GPT Pro R10 bundle manifest: `evidence/sprint27_agent_content_inbox/REVIEW_BUNDLE_MANIFEST.md`
  - Final release gate passed after Android version bump to `versionCode=31`, `versionName=0.11.15-alpha`.
  - Final Gradle gate passed: `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug`.
  - Final full connected Android gate passed on R2: 137 tests, 0 failures, 0 skipped.
  - APK badging/signature/install/launch evidence passed.
  - Release artifact: `release_artifacts/quality-alternative-v0.11.15-agent-content-inbox-alpha-debug.apk`
  - Release APK SHA-256: `10f2d54f7dc06c561afa32a83bcc7c5790c211f17cd320d469d93e6c957278f6`
  - Release gate summary: `docs/release-gate-logs/2026-06-12-sprint27-agent-content-inbox/VALIDATION_SUMMARY.md`
  - Release notes: `docs/release-gate-logs/2026-06-12-sprint27-agent-content-inbox/RELEASE_NOTES_v0.11.15-agent-content-inbox-alpha.md`
  - Release commit: `b173e3c` (`Complete Sprint 27 agent content inbox`).
  - Release tag: `v0.11.15-agent-content-inbox-alpha`.
  - Release URL: `https://github.com/dkacz/qualityalternative/releases/tag/v0.11.15-agent-content-inbox-alpha`.
  - Published assets: `quality-alternative-v0.11.15-agent-content-inbox-alpha-debug.apk` and `quality-alternative-v0.11.15-agent-content-inbox-alpha-debug.apk.sha256`.
  - Integration method: committed on `codex/sprint-agent-content-inbox`, tagged the release commit, pushed branch and tag to `origin`, and published the GitHub release from the committed release notes.
- Post-release diagnosis on 2026-06-14:
  - Agent Inbox packages uploaded by an external rclone daemon are not visible to the Android app under the current `drive.file` OAuth scope unless the app created those files or the user explicitly grants access through a Drive selection flow.
  - This differs from the user's Boox/rclone annotation pipeline, which runs outside the app with broader Drive access, and from the app's internal annotation sync, where the app creates/uploads the files itself and can later see them.
  - The observed app state `connected, no packages found` with externally uploaded packages is therefore expected under `drive.file`; the blocker is file authorization/ownership, not upload method.
  - Follow-up implementation decision: either request broader `drive.readonly` for Agent Inbox, or keep `drive.file` and add a Google Picker folder/file grant flow. The Picker route still needs validation for whether files added later to the granted folder remain visible.
- Next gate:
  - Choose the post-release Agent Inbox Drive access strategy before implementing the rclone-uploaded package fix.

## Sprint 26 Custom Targets And Website Interventions

Status: `release_published`

- Branch: `codex/sprint26-custom-targets-website-interventions`
- Scope: plan and implement support for replacement-first interventions on eligible arbitrary installed apps plus supported-browser website/domain rules, while avoiding universal URL blocking claims.
- Canonical sprint plan: `docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md`
- GPT Pro plan review R1 lane: `https://chatgpt.com/c/6a25282f-e0b8-83ed-bc77-15b0fef88cad`
- GPT Pro plan review R1 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_PLAN_REVIEW.md`
- GPT Pro plan review R1 verdict: `SCORE 6/10`, `VERDICT FAIL`, `VISUAL REVIEW NOT APPLICABLE`
- GPT Pro plan review R2 lane: `https://chatgpt.com/c/6a252ba7-7c48-83eb-a9b5-a95bd1d499dd`
- GPT Pro plan review R2 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_PLAN_REVIEW_R2.md`
- GPT Pro plan review R2 verdict: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW NOT APPLICABLE`
- Heartbeat automation: deleted after successful R2 harvest.
- Current plan review bundle: `evidence/sprint26_custom_targets_website_interventions/SPRINT26_PLAN_REVIEW_R2_BUNDLE_20260607.zip`
- Current plan review prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_PLAN_REVIEW_PROMPT_R2.md`
- Current implementation state:
  - Slice 26.0 contract/plan gate is complete and committed.
  - Slice 26.1 custom app target vertical is implemented locally.
  - Android now enumerates launchable installed apps for custom target candidates and exposes unsafe packages as disabled rows with reasons.
  - R2 fixed the GPT Pro R1 blockers: default mode is Soft, DocumentsUI/file-picker packages are excluded, eligibility policy has direct unit coverage, and the review bundle includes Portable Profile, MainActivity, analytics, Gradle, raw logs, and expanded visual evidence.
  - R3 fixed the GPT Pro R2 blockers: the bundle included all `app/src`, Portable Profile all-missing app-target replace import stayed empty instead of selecting defaults, fresh `--rerun-tasks` unit reports included XML/test counts, and custom installed-app intervention was covered through `MainActivity.createSystemInterceptionIntent()`.
  - R4 fixes the GPT Pro R3 blockers: the review bundle now includes complete Gradle wrapper files and `app/proguard-rules.pro`; Settings can rebuild from an empty completed target set by allowing additions below the minimum while still blocking below-minimum removals; and the OEM safety boundary has direct regression coverage.
  - Settings separates standard suggestions from custom installed-app search/selection.
  - Selected eligible custom app packages hydrate into settings, Portable Profile import/export, and the AccessibilityService resolver's known target list.
  - Slice 26.2 implements the website rule model and Settings UI for exact-domain and wildcard-subdomain rules.
  - Website rules are normalized, reject local/private/public-IP/IPv6/all-numeric/ambiguous hosts, persist in DataStore, and round-trip through Portable Profile import/export without browser support state, URL observations, tokens, or local folders.
  - Settings exposes website rules as a separate target category with add, validation, pause, edit, delete, and a browser support matrix. Chrome current-host interception remains scoped to Slice 26.3.
  - Slice 26.2 R2 fixes the GPT Pro R1 blockers: all IP literals are rejected; typed `*.example.com` exposes the subdomain mode and apex toggle before save; apex inclusion defaults off; the website rule count says `enabled`; the review bundle includes `gradle/libs.versions.toml`; and Android test metadata is sanitized to remove absolute local paths.
  - Slice 26.3 implements Chrome-first verified-host website intervention through whitelisted address-bar accessibility nodes only.
  - Slice 26.3 keeps unsupported/unreadable browser states non-triggering, never reuses a prior host, and routes matching rules into the existing Soft/Firm intervention flow as `Chrome website`.
  - Slice 26.3 uses privacy-safe analytics metadata (`targetType`, browser support status, rule type) without raw URL, host/domain, path/query, page title, URL-bar text, non-match observations, browsing history, or domain-derived hashes.
  - Slice 26.3 R2 fixes the GPT Pro R1 blockers: Settings copy now marks Chrome domain rules as supported when readable; hidden/focused omnibox states are rejected; adapter depth covers the real Chrome toolbar; website Open Anyway suppression falls through to whole-browser Chrome app target evaluation; and real Chrome package/version plus URL-set evidence is recorded.
  - Slice 26.3 R3 fixes the GPT Pro R2 blocker by removing raw `externalUrl` from shared content analytics metadata and adding a website-domain regression that proves a replacement link URL with host, path, and query does not appear in website intervention, accept, or fallback-open analytics metadata.
  - Slice 26.3 R4 is a package/evidence completeness review after R3 returned PASS/PASS at 9/10; it adds full unit XML, standalone activity/analytics/repository/model source files, and a cleaned manifest for GPT Pro to re-score.
  - Slice 26.3 R5 ships all `app/src` source/test files plus full unit/lint evidence to close the R4 package-completeness gap.
  - Slice 26.3 R5 review returned `SCORE 9/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`, with no release blockers but with evidence gaps for fresh connected evidence, live service E2E, negative unsupported/unreadable proof, device/API metadata, schemed Unicode URL coverage, and raw `git diff --check` output.
  - Slice 26.3 R6 closes those gaps with fresh connected 4/4 evidence, live Chrome AccessibilityService-to-intervention proof, connected unsupported/unreadable negative evidence, device/API/Chrome version metadata, schemed Unicode host normalization coverage, and raw diff-check output.
  - Slice 26.3 R6 review returned `SCORE 9/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`; GPT Pro's blocker was missing package-authenticated stale active-window/package-mismatch safety.
  - Slice 26.3 R7 adds package identity to browser snapshots, requires the root and address-bar node package to match `com.android.chrome`, and reruns unit, connected, visual, and live-service evidence after the change.
  - Slice 26.3 R7 review returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`; no blockers or release-blocking bundle gaps remain.
  - Slice 26.3 was committed as `8fdd20e` (`Complete Sprint 26 Chrome website interventions`).
  - Slice 26.4 is in progress: adding remote analytics privacy guards, log-scrub checks, and Portable Profile import/export hardening for custom app and website rules.
  - Slice 26.4 implementation/evidence was sent to GPT Pro at `https://chatgpt.com/c/6a25ae84-1310-83eb-a53a-1128d4a7edd1`; harvest heartbeat `harvest-sprint-26-slice26-4-gpt-pro-review` writes to `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_4_REVIEW.md`.
  - Slice 26.4 heartbeat check at `2026-06-07T17:57:22Z`: exact-lane harvest found GPT Pro still thinking, so the heartbeat remains active and no review output has been harvested yet.
  - Slice 26.4 R1 review returned `SCORE 8/10`, `VERDICT FAIL`, `VISUAL REVIEW NOT APPLICABLE`; blockers were unknown `targetType` echoing to `targetClass`, missing host/IP/IDNA sanitizer cases, and metadata-only unsafe-field diagnostics.
  - Slice 26.4 R1 heartbeat was deleted after successful harvest.
  - Slice 26.4 R2 fixes the R1 blockers with fixed target-class mapping, known-only `targetType` metadata, expanded IP/host/IDNA/package/URL filtering, top-level unsafe-field diagnostics, and a remote-safe debug summary path.
  - Slice 26.4 R2 implementation/evidence was sent to GPT Pro at `https://chatgpt.com/c/6a25b565-7d28-83ed-bc8a-de6a19da9613`; harvest heartbeat `harvest-sprint-26-slice26-4-r2-gpt-pro-review` writes to `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_4_R2_REVIEW.md`.
  - Slice 26.4 R2 heartbeat check at `2026-06-07T18:26:22Z`: exact-lane harvest found GPT Pro still thinking, so the heartbeat remains active and no R2 review output has been harvested yet.
  - Slice 26.4 R2 review returned `SCORE 9/10`, `VERDICT FAIL`, `VISUAL REVIEW NOT APPLICABLE`; remaining blocker was punycode/IDNA host-like values with punycode TLDs and trailing-dot IPv4 literals.
  - Slice 26.4 R2 heartbeat was deleted after successful harvest.
  - Slice 26.4 R3 fixes the R2 blocker by canonicalizing host candidates before IP checks and rejecting DNS-style multi-label values including punycode labels/TLDs, plus regression coverage through payload conversion, scrubber, and top-level diagnostics.
  - Slice 26.4 R3 implementation/evidence was sent to GPT Pro at `https://chatgpt.com/c/6a25bb13-7590-83ed-bbf4-8c84ac527bc0`; harvest heartbeat `harvest-sprint-26-slice26-4-r3-gpt-pro-review` writes to `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_4_R3_REVIEW.md`.
  - Slice 26.4 R3 heartbeat check at `2026-06-07T18:50:22Z`: exact-lane harvest found GPT Pro still thinking, so the heartbeat remains active and no R3 review output has been harvested yet.
  - Slice 26.4 R3 review returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW NOT APPLICABLE`; no blockers, bundle gaps, or package hygiene issues remain.
  - Slice 26.4 R3 heartbeat was deleted after successful harvest.
  - Slice 26.4 was committed as `2ce73f8` (`Harden Sprint 26 analytics privacy boundary`).
  - Slice 26.5 implements Bedtime/custom-target/website integration evidence. Production behavior was already shared through `triggerIntervention`; this slice adds targeted regression coverage and visual proof for supported Chrome website Bedtime.
  - Slice 26.5 unit coverage proves website Bedtime uses the 60-second emergency unlock, keeps meditation/alternatives, blocks `delayFor15Minutes()`, keeps website unlock scoped to the website suppression key, and avoids raw host/URL analytics metadata.
  - Slice 26.5 visual evidence proves Soft website, Firm website wait, and Bedtime `Chrome website` emergency-unlock states; the Bedtime screen hides `Pause 15 min`, keeps meditation/quiet alternatives, shows no raw domain, and shows the 60-second breath wait.
  - Slice 26.5 validation passed: 443 debug unit tests, `lintDebug` with 0 errors, 1/1 connected screenshot E2E on `qaApi36`, `git diff --check`, and emulator shutdown proof.
  - Slice 26.5 implementation/evidence was sent to GPT Pro at `https://chatgpt.com/c/6a25c47f-21d0-83eb-ace6-66cb604c351e`; harvest heartbeat `harvest-sprint-26-slice26-5-gpt-pro-review` writes to `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_5_REVIEW.md`.
  - Slice 26.5 R1 review returned `SCORE 8/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`; blocker was bundle/evidence completeness for verified-host launch provenance and missing source files (`MainActivity.kt`, `AndroidManifest.xml`, `AnalyticsPrivacyGuard.kt`) plus missing prior Slice 26.1 R4 artifacts.
  - Slice 26.5 R1 heartbeat was deleted after successful harvest.
  - Slice 26.5 R2 fixes the blocker with `MainActivityTest#forgedWebsiteInterceptionIntentWithoutLaunchTokenIsIgnored`, a repeatable all-day Bedtime website seed for external live-service E2E, and an external shell harness proving real Chrome -> bound `QualityAlternativeAccessibilityService` -> `Bedtime is protecting sleep from Chrome website`.
  - Slice 26.5 R2 evidence path: `evidence/sprint26_custom_targets_website_interventions/SLICE26_5_EVIDENCE.md`.
  - Slice 26.5 R2 visual contact sheet: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_5_r2/sprint26_slice26_5_r2_bedtime_website_live_contact_sheet.png`.
  - Slice 26.5 R2 live-service evidence: `evidence/sprint26_custom_targets_website_interventions/live_service_e2e_slice26_5_r2/`.
  - Slice 26.5 R2 validation passed: 443 debug unit tests, `lintDebug` with 0 errors, connected tests 2/2 on `qaApi36`, external live-service E2E PASS, and `git diff --check` PASS.
  - Slice 26.5 R2 clean review bundle was prepared for GPT Pro after removing inherited local-path leakage from the R1 review context. The exact active review URL is recorded locally after bundle export; harvest heartbeat `harvest-sprint-26-slice26-5-r2-gpt-pro-review` writes to `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_5_R2_REVIEW.md`.
  - Slice 26.5 R2 active GPT Pro review lane: `https://chatgpt.com/c/6a25ce86-3f54-83eb-8547-940e2d94ecf8`.
  - Slice 26.5 R2 superseded pre-harvest lanes: `https://chatgpt.com/c/6a25cdcc-13a8-83ed-90e2-960e68535cab`, `https://chatgpt.com/c/6a25ce25-d3c0-83eb-8d1e-23288dd931ee`.
  - Slice 26.5 R2 heartbeat check at `2026-06-07T20:10:22Z`: exact-lane harvest found GPT Pro still thinking with the stop button visible; no `SCORE`/`VERDICT` output was harvested and heartbeat `harvest-sprint-26-slice26-5-r2-gpt-pro-review` remains active.
  - Slice 26.5 R2 heartbeat check at `2026-06-07T20:20:22Z`: exact-lane harvest and fresh-tab retry still found GPT Pro thinking with the stop button visible; no `SCORE`/`VERDICT` output was harvested and heartbeat `harvest-sprint-26-slice26-5-r2-gpt-pro-review` remains active.
  - Slice 26.5 R2 review returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`; no blockers, bundle gaps, privacy issues, or package hygiene issues remain.
  - Slice 26.5 R2 heartbeat was deleted after successful harvest.
  - Slice 26.5 was committed as `ee0d001` (`Complete Sprint 26 bedtime target integration`).
  - Slice 26.6 release gate uses `v0.11.14-custom-targets-website-interventions-alpha`, `versionCode=30`, `versionName=0.11.14-alpha`.
  - Slice 26.6 final validation passed after the R2 gate repair: `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug` PASS; targeted connected rerun PASS; full connected Android test PASS (`136` tests, `0` failures, `0` errors, `0` skipped); APK badging/signature/install/launch evidence PASS.
  - Slice 26.6 validation summary: `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/VALIDATION_SUMMARY.md`.
  - Slice 26.6 release notes: `docs/release-gate-logs/2026-06-07-sprint26-custom-targets-website-interventions/RELEASE_NOTES_v0.11.14-custom-targets-website-interventions-alpha.md`.
  - Slice 26.6 review bundle: `SPRINT26_FINAL_RELEASE_REVIEW_BUNDLE_20260607.zip`.
  - Slice 26.6 final GPT Pro review lane: `https://chatgpt.com/c/6a25e184-a30c-83eb-b346-199c70ed88b5`.
  - Slice 26.6 final GPT Pro output path: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_FINAL_RELEASE_REVIEW.md`.
  - Slice 26.6 final GPT Pro heartbeat: deleted after successful R1 harvest.
  - Slice 26.6 final GPT Pro heartbeat check at `2026-06-07T21:34:23Z`: exact-lane harvest and fresh-tab retry found GPT Pro still thinking with the stop button visible; no `SCORE`/`VERDICT` output was harvested and the heartbeat remains active.
  - Slice 26.6 final GPT Pro R1 review returned `SCORE 8/10`, `VERDICT REVISE`, `VISUAL REVIEW PASS`; blockers were release-note privacy wording that overclaimed local analytics sanitization, and bundle-hygiene inconsistency around retaining the superseded failed connected log on disk while excluding it from the review ZIP.
  - Slice 26.6 R2 fixes the R1 blockers by narrowing the claim to remote/export analytics payloads, explicitly allowing device-local analytics rows to keep package-level fields needed for local behavior, aligning the validation summary and bundle manifest on the superseded failed connected run, adding verbose signature/status evidence, adding unit XML and full source/test trees to the R2 review bundle, and shipping scrubbed canonical logs in the bundle.
  - Slice 26.6 R2 review bundle: `SPRINT26_FINAL_RELEASE_R2_REVIEW_BUNDLE_20260607.zip`.
  - Slice 26.6 R2 GPT Pro lane: `https://chatgpt.com/c/6a25e7a6-74a4-83ed-a848-58c4e4eafa6e`.
  - Slice 26.6 R2 GPT Pro output path: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_FINAL_RELEASE_R2_REVIEW.md`.
  - Slice 26.6 R2 GPT Pro heartbeat: deleted after successful harvest.
  - Slice 26.6 R2 heartbeat check at `2026-06-07T22:00:23Z`: exact-lane harvest saw GPT Pro still thinking with the stop button visible; the helper's first refresh/fresh-tab attempt hit a target-closed browser error, and an immediate explicit fresh-tab retry still showed GPT Pro thinking. No `SCORE`/`VERDICT` output was harvested at that checkpoint.
  - Slice 26.6 R2 review returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`; blockers none, bundle gaps none, package hygiene PASS, release readiness PASS.
  - Slice 26.6 R2 review confirmed the R1 blocker recheck: release notes now correctly scope privacy to remote/export analytics payloads while permitting device-local package-level fields needed for local behavior, and the validation summary/manifest now consistently treat the superseded failed connected run as retained on disk only and excluded from the R2 review ZIP.
  - Release commit: `189ec67` (`Complete Sprint 26 final release gate`).
  - Release tag: `v0.11.14-custom-targets-website-interventions-alpha`.
  - Release URL: `https://github.com/dkacz/qualityalternative/releases/tag/v0.11.14-custom-targets-website-interventions-alpha`.
  - Published assets: `quality-alternative-v0.11.14-custom-targets-website-interventions-alpha-debug.apk` and `quality-alternative-v0.11.14-custom-targets-website-interventions-alpha-debug.apk.sha256`.
  - Integration method: committed on `codex/sprint26-custom-targets-website-interventions`, tagged the release commit, pushed branch and tag to `origin`, and published the GitHub release from the committed release notes.
  - Slice 26.1 GPT Pro R3 review returned `SCORE 8/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`.
  - Slice 26.1 GPT Pro R4 review returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`.
  - Slice 26.1 GPT Pro R4 heartbeat was deleted after successful harvest.
- Current Slice 26.1 validation:
  - Validation summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_1_VALIDATION.md`
  - Visual contact sheet R2: `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r2/CONTACT_SHEET.png`
  - Raw logs R2: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r2/`
  - GPT Pro Slice 26.1 lane: `https://chatgpt.com/c/6a2533e7-1718-83eb-a8a6-d55ddc6da463`
  - GPT Pro Slice 26.1 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_REVIEW_PROMPT.md`
  - GPT Pro Slice 26.1 bundle: `evidence/sprint26_custom_targets_website_interventions/SPRINT26_SLICE26_1_REVIEW_BUNDLE_20260607.zip`
  - GPT Pro Slice 26.1 R1 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_REVIEW.md`
  - GPT Pro Slice 26.1 R1 verdict: `SCORE 6/10`, `VERDICT FAIL`, `VISUAL REVIEW FAIL`
  - GPT Pro Slice 26.1 R2 lane: `https://chatgpt.com/c/6a253e58-239c-83ed-93df-5a24aad638fd`
  - GPT Pro Slice 26.1 R2 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R2_REVIEW_PROMPT.md`
  - GPT Pro Slice 26.1 R2 bundle: `evidence/sprint26_custom_targets_website_interventions/SPRINT26_SLICE26_1_R2_REVIEW_BUNDLE_20260607.zip`
  - GPT Pro Slice 26.1 R2 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R2_REVIEW.md`
  - GPT Pro Slice 26.1 R2 verdict: `SCORE 5/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`
  - GPT Pro Slice 26.1 R3 lane: `https://chatgpt.com/c/6a2546ce-9c3c-83eb-bb3e-732e636f8484`
  - GPT Pro Slice 26.1 R3 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R3_REVIEW_PROMPT.md`
  - GPT Pro Slice 26.1 R3 bundle: `evidence/sprint26_custom_targets_website_interventions/SPRINT26_SLICE26_1_R3_REVIEW_BUNDLE_20260607.zip`
  - GPT Pro Slice 26.1 R3 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R3_REVIEW.md`
  - GPT Pro Slice 26.1 R3 verdict: `SCORE 8/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`
  - GPT Pro Slice 26.1 R4 lane: `https://chatgpt.com/c/6a254f10-4c34-83eb-a943-148dde4a4efd`
  - GPT Pro Slice 26.1 R4 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R4_REVIEW_PROMPT.md`
  - GPT Pro Slice 26.1 R4 bundle: `evidence/sprint26_custom_targets_website_interventions/SPRINT26_SLICE26_1_R4_REVIEW_BUNDLE_20260607.zip`
  - GPT Pro Slice 26.1 R4 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R4_REVIEW.md`
  - GPT Pro Slice 26.1 R4 verdict: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`
  - GPT Pro Slice 26.1 R4 heartbeat: deleted after successful harvest.
  - R4 validation summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_1_R4_VALIDATION.md`
  - Visual contact sheet R4: `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r4/CONTACT_SHEET.png`
  - Raw logs R4: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r4/`
  - Passed targeted unit tests and lint for resolver, settings repository, ViewModel hydration, and Portable Profile import/export.
  - Passed emulator visual E2E: `VisualQaScreenshotTest#captureSprint26CustomTargetSettingsScreens`
  - Passed `git diff --check`.
- Next gate:
  - None for Sprint 26; release is published.

### Current Slice 26.2 Validation

- Validation summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_2_EVIDENCE.md`
- Visual screenshot directory: `evidence/sprint26_custom_targets_website_interventions/visual_e2e/sprint26-custom-targets-1780833001182/`
- Visual contact sheet: `evidence/sprint26_custom_targets_website_interventions/visual_e2e/sprint26_slice26_2_website_rules_contact_sheet.png`
- Passed targeted unit tests:
  - `WebsiteRuleNormalizerTest`
  - `PreferencesSettingsRepositoryTest`
  - `AccountLightProfileExporterTest`
  - `AccountLightProfileImporterTest`
  - `MainViewModelTest`
- Passed emulator visual E2E: `VisualQaScreenshotTest#captureSprint26WebsiteRuleSettingsScreens`
- Passed `:app:lintDebug`
- GPT Pro Slice 26.2 lane: `https://chatgpt.com/c/6a255d01-cb24-83eb-b76b-33fcd656b7e7`
- GPT Pro Slice 26.2 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_2_REVIEW_PROMPT.md`
- GPT Pro Slice 26.2 bundle: `evidence/sprint26_custom_targets_website_interventions/SPRINT26_SLICE26_2_REVIEW_BUNDLE_20260607.zip`
- GPT Pro Slice 26.2 R1 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_2_REVIEW.md`
- GPT Pro Slice 26.2 R1 verdict: `SCORE 7/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`
- GPT Pro Slice 26.2 R1 heartbeat: deleted after successful harvest.
- GPT Pro Slice 26.2 R2 evidence: `evidence/sprint26_custom_targets_website_interventions/SLICE26_2_R2_EVIDENCE.md`
- GPT Pro Slice 26.2 R2 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_2_R2_REVIEW_PROMPT.md`
- GPT Pro Slice 26.2 R2 bundle: `evidence/sprint26_custom_targets_website_interventions/SPRINT26_SLICE26_2_R2_REVIEW_BUNDLE_20260607.zip`
- GPT Pro Slice 26.2 R2 lane: `https://chatgpt.com/c/6a25665c-17d8-83eb-823f-46295216bbb1`
- GPT Pro Slice 26.2 R2 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_2_R2_REVIEW.md`
- GPT Pro Slice 26.2 R2 verdict: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`
- GPT Pro Slice 26.2 R2 heartbeat: deleted after successful harvest.
- GPT Pro Slice 26.2 R2 visual screenshot directory: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_r2/sprint26-custom-targets-1780835556853/`
- GPT Pro Slice 26.2 R2 visual contact sheet: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_r2/sprint26_slice26_2_r2_website_rules_contact_sheet.png`
- GPT Pro Slice 26.2 R2 bundle manifest: `evidence/sprint26_custom_targets_website_interventions/SLICE26_2_R2_REVIEW_BUNDLE_MANIFEST.md`

### Current Slice 26.3 R2 Validation

- Evidence summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_EVIDENCE.md`
- R2 evidence summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R2_EVIDENCE.md`
- Review bundle manifest: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_REVIEW_BUNDLE_MANIFEST.md`
- R2 review bundle manifest: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R2_REVIEW_BUNDLE_MANIFEST.md`
- R2 diff: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R2_DIFF.patch`
- GPT Pro Slice 26.3 lane: `https://chatgpt.com/c/6a257187-c76c-83eb-ab7a-c3b3e873fa85`
- GPT Pro Slice 26.3 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_REVIEW_PROMPT.md`
- GPT Pro Slice 26.3 R1 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_REVIEW.md`
- GPT Pro Slice 26.3 R1 verdict: `SCORE 7/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`
- GPT Pro Slice 26.3 R2 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R2_REVIEW_PROMPT.md`
- GPT Pro Slice 26.3 R2 bundle: `SPRINT26_SLICE26_3_R2_REVIEW_BUNDLE_20260607.zip`
- GPT Pro Slice 26.3 R2 lane: `https://chatgpt.com/c/6a257f25-eefc-83ed-b502-a7c70b89ed71`
- GPT Pro Slice 26.3 R2 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R2_REVIEW.md`
- GPT Pro Slice 26.3 R2 verdict: `SCORE 8/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`
- GPT Pro Slice 26.3 R2 blocker: website-domain analytics could still include replacement `externalUrl` values through shared content metadata.
- GPT Pro Slice 26.3 R2 heartbeat: deleted after successful harvest.
- R3 evidence summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R3_EVIDENCE.md`
- R3 diff: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R3_DIFF.patch`
- R3 raw test evidence: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r3/`
- GPT Pro Slice 26.3 R3 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R3_REVIEW_PROMPT.md`
- GPT Pro Slice 26.3 R3 bundle: `SPRINT26_SLICE26_3_R3_REVIEW_BUNDLE_20260607.zip`
- GPT Pro Slice 26.3 R3 lane: `https://chatgpt.com/c/6a258670-aa7c-83eb-a4ec-a37abb91c04f`
- GPT Pro Slice 26.3 R3 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R3_REVIEW.md`
- GPT Pro Slice 26.3 R3 verdict: `SCORE 9/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`, blockers none.
- GPT Pro Slice 26.3 R3 heartbeat: deleted after successful harvest.
- R4 evidence summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R4_EVIDENCE.md`
- R4 diff: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R4_DIFF.patch`
- R4 raw test evidence: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r4/`
- GPT Pro Slice 26.3 R4 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R4_REVIEW_PROMPT.md`
- GPT Pro Slice 26.3 R4 bundle: `SPRINT26_SLICE26_3_R4_REVIEW_BUNDLE_20260607.zip`
- GPT Pro Slice 26.3 R4 lane: `https://chatgpt.com/c/6a258c5f-0fd8-83eb-b96b-434cb0c95945`
- GPT Pro Slice 26.3 R4 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R4_REVIEW.md`
- GPT Pro Slice 26.3 R4 verdict: `SCORE 9/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`, blockers none.
- GPT Pro Slice 26.3 R4 heartbeat: deleted after successful harvest.
- R5 evidence summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R5_EVIDENCE.md`
- R5 review bundle manifest: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R5_REVIEW_BUNDLE_MANIFEST.md`
- GPT Pro Slice 26.3 R5 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R5_REVIEW_PROMPT.md`
- GPT Pro Slice 26.3 R5 bundle: `SPRINT26_SLICE26_3_R5_REVIEW_BUNDLE_20260607.zip`
- GPT Pro Slice 26.3 R5 lane: `https://chatgpt.com/c/6a259198-0f20-83eb-849d-4188836260c4`
- GPT Pro Slice 26.3 R5 review output target: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R5_REVIEW.md`
- GPT Pro Slice 26.3 R5 verdict: `SCORE 9/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`, blockers none.
- GPT Pro Slice 26.3 R5 heartbeat: deleted after successful harvest.
- R6 evidence summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R6_EVIDENCE.md`
- R6 review bundle manifest: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R6_REVIEW_BUNDLE_MANIFEST.md`
- R6 diff: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R6_DIFF.patch`
- GPT Pro Slice 26.3 R6 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R6_REVIEW_PROMPT.md`
- GPT Pro Slice 26.3 R6 bundle: `SPRINT26_SLICE26_3_R6_REVIEW_BUNDLE_20260607.zip`
- GPT Pro Slice 26.3 R6 lane: `https://chatgpt.com/c/6a259ebf-7448-83eb-8027-d21674b45366`
- GPT Pro Slice 26.3 R6 output target: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R6_REVIEW.md`
- GPT Pro Slice 26.3 R6 verdict: `SCORE 9/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`.
- GPT Pro Slice 26.3 R6 heartbeat: deleted after successful harvest.
- GPT Pro Slice 26.3 R6 heartbeat check `2026-06-07T16:49:22Z`: exact-lane harvest still showed ChatGPT thinking; final R6 harvest completed at the next heartbeat and the heartbeat was deleted.
- R7 evidence summary: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R7_EVIDENCE.md`
- R7 review bundle manifest: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R7_REVIEW_BUNDLE_MANIFEST.md`
- R7 diff: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R7_DIFF.patch`
- GPT Pro Slice 26.3 R7 prompt: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R7_REVIEW_PROMPT.md`
- GPT Pro Slice 26.3 R7 bundle: `SPRINT26_SLICE26_3_R7_REVIEW_BUNDLE_20260607.zip`
- GPT Pro Slice 26.3 R7 lane: `https://chatgpt.com/c/6a25a63b-ce44-83eb-8cdc-764669a47268`
- GPT Pro Slice 26.3 R7 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R7_REVIEW.md`
- GPT Pro Slice 26.3 R7 verdict: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`
- GPT Pro Slice 26.3 R7 heartbeat: deleted after successful harvest.
- GPT Pro Slice 26.3 R7 heartbeat check `2026-06-07T17:21:22Z`: exact-lane harvest still showed ChatGPT thinking; no completed `SCORE`/`VERDICT` response harvested yet, so the heartbeat remained active at that checkpoint.
- GPT Pro Slice 26.3 R7 harvest `2026-06-07T17:31:22Z`: exact-lane harvest completed from `https://chatgpt.com/c/6a25a63b-ce44-83eb-8cdc-764669a47268`; output preserved at `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R7_REVIEW.md`; heartbeat deleted.
- Visual screenshot directory: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3/sprint26-custom-targets-1780838366859/`
- Visual contact sheet: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3/sprint26_slice26_3_chrome_website_intervention_contact_sheet.png`
- R2 visual evidence: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3_r2/sprint26_slice26_3_r2_chrome_verified_host_contact_sheet.png`
- R2 Chrome evidence: `evidence/sprint26_custom_targets_website_interventions/chrome_verified_host_e2e_r2_latest/`
- R6 visual evidence: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3_r6/`
- R6 Chrome/live-service evidence: `evidence/sprint26_custom_targets_website_interventions/chrome_verified_host_e2e_r6_latest/` and `evidence/sprint26_custom_targets_website_interventions/live_service_e2e_r6/`
- R6 raw test evidence: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r6/`
- Raw test evidence: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r2/`
- Passed targeted unit tests:
  - `VerifiedBrowserHostAdapterTest`
  - `WebsiteInterceptionResolverTest`
  - `AccessibilityInterceptionPlannerTest`
  - `MainViewModelTest.requestSystemWebsiteInterception_opensInterventionWithoutSelectedBrowserAndKeepsDomainPrivate`
  - `MainViewModelTest.websiteOpenAnywaySuppressesWebsiteKeyWithoutSuppressingWholeBrowserTarget`
- Passed real Chrome adapter harness: `AccessibilityInterceptionTest#chromeVerifiedHostAdapterHarnessAcceptsOnlyLoadedMatchingHost` on `qaApi36(AVD) - 16`.
- Passed emulator visual E2E:
  - `VisualQaScreenshotTest#captureSprint26WebsiteRuleSettingsScreens`
  - `VisualQaScreenshotTest#captureSprint26ChromeWebsiteInterventionScreens`
- Passed `:app:lintDebug`.
- R3 passed targeted unit regression for website-domain analytics URL privacy.
- R3 passed `:app:lintDebug`.
- R3 passed `git diff --check`.
- R4 passed full `:app:testDebugUnitTest`.
- R4 passed `:app:lintDebug`.
- R6 passed full `:app:testDebugUnitTest` with 432 tests and no failures/errors/skips.
- R6 passed `:app:lintDebug` with 0 errors.
- R6 passed fresh connected Android tests 4/4, including Chrome loaded-host harness, unsupported/unreadable negative states, website Settings visuals, and website intervention visuals.
- R6 passed external live-service Chrome E2E and `git diff --check`.
- R7 passed full `:app:testDebugUnitTest` with 433 tests and no failures/errors/skips.
- R7 passed `:app:lintDebug` with 0 errors.
- R7 passed fresh connected Android tests 4/4, including package-mismatched stale snapshot negative evidence.
- R7 passed external live-service Chrome E2E after the package-authentication change and `git diff --check`.

## Sprint 25 Markdown Media And Tables

Status: `gpt_pro_10_10_pass_release_prep`

- Branch: `codex/sprint25-md-image-embeds`
- Scope: Markdown reader import/rendering fixes for embedded images and Markdown tables.
- Current implementation state:
  - Added `ReaderDocumentImage` and image metadata on reader document blocks.
  - Added `MarkdownReaderDocumentParser` for standalone Markdown images, inline-image alt text, `data:image/...` payloads, relative `file://` image resolution, and picked image attachment URI maps.
  - Added Markdown image attachment URI persistence via `user_documents.imageAttachmentUrisJson` and migration `14 -> 15`.
  - Updated the Android document picker to allow image files and attach picked images to Markdown documents instead of saving image files as separate library items.
  - Updated reader UI to render image blocks from `content://`, `file://`, `android.resource://`, and `data:image/...`; placeholders appear when an image source cannot be opened.
  - Added pipe Markdown table parsing with header/body rows and alignment metadata.
  - Updated reader UI to render Markdown tables as structured rows and columns with header styling, cell alignment, and horizontal scrolling for wide tables.
  - Updated reader pagination, progress snapshots, and reading-time estimates so image payloads and Markdown table delimiter syntax do not distort page fit or time estimates.
  - Updated reader gesture handling so horizontally scrolling wide Markdown tables do not advance or complete reader pages.
  - Narrowed the table gesture guard after GPT Pro R2 so ordinary text taps/swipes still advance reader pages.
  - Updated table pagination measurement so wrapped cell text contributes to page fit and oversized tables split by visual row weight.
- Validation status:
  - Unit/instrumented tests have been added for parser, reading-time estimate, Room migration, document attachment persistence, reader image block pagination, reader table pagination, and Sprint 25 visual evidence.
  - Passed with Homebrew JDK 17: `./gradlew :app:testDebugUnitTest :app:lintDebug`.
  - Passed on emulator `qaApi36(AVD) - 16`: `VisualQaScreenshotTest#captureSprint25MarkdownMediaAndTableScreens`.
  - Passed on emulator `qaApi36(AVD) - 16`: `VisualQaScreenshotTest#captureSprint25WideMarkdownTableHorizontalScrollDoesNotAdvanceReaderPage`.
  - Passed on emulator `qaApi36(AVD) - 16`: `VisualQaScreenshotTest#captureSprint25OrdinaryTextNavigationStillWorksAfterTableGestureGuard`.
  - Passed on emulator `qaApi36(AVD) - 16`: `RoomUserDocumentRepositoryTest`.
  - Passed on emulator `qaApi36(AVD) - 16`: `QualityAlternativeDatabaseMigrationInstrumentedTest`.
  - Visual evidence: `evidence/sprint25_markdown_media_tables/screenshots-r3/contact_sheet_r3.png`.
  - R3 Android results: `evidence/sprint25_markdown_media_tables/android-results-r3/`.
  - Passed: `git diff --check`.
  - GPT Pro R3: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`.
- Next gate:
  - Bump Android version, run final tests, build and verify the debug APK, then publish the GitHub release.

## Android Meditation / Content Priority Fix

Status: `complete_10_10_pass`

- Branch: `codex/android-meditation-priority-controls`
- Remote: `origin/codex/android-meditation-priority-controls`
- Latest implementation commit: `b37cdb8` (`Persist replacement history durations`)
- PR draft URL: `https://github.com/dkacz/qualityalternative/pull/new/codex/android-meditation-priority-controls`
- Current GPT Pro follow-up lane: `https://chatgpt.com/c/69eb34ec-0704-8388-b485-e94dc4080e4e`
- Heartbeat automation: `android-meditation-priority-pro-review`
- Current review prompt: `PRO_REVIEW_PROMPT_20260424_110657.md`
- Current review bundle: `QUALITY_ALTERNATIVE_REVIEW_BUNDLE_20260424_110657.zip`
- Final review output: `PRO_REVIEW_OUTPUT_20260424_110657_CLEAN/Android_Meditation_Review.md`
- Final verdict: `10/10 PASS`
- Prior review outputs: `PRO_REVIEW_OUTPUT_20260424_093850/`, `PRO_REVIEW_OUTPUT_20260424_103230/`
- Superseded mixed/accidental lane no longer tracked: `https://chatgpt.com/c/69eb335c-dc40-8391-84c3-383aefd24c0c`
- Visual QA contact sheet: `output/android-meditation-priority-visual-20260424/contact_sheet_meditation_priority.png`

Completed validation:

- `testDebugUnitTest`
- `lintDebug`
- `connectedDebugAndroidTest` on Android emulator, 50/50 passing
- `git diff --check`
- JSON validation for `app/src/main/assets/editorial/starter_packs.json`
- `VisualQaScreenshotTest#captureSprint10ReaderProgressStreakAndMeditationScreens`
- Manual visual review of the contact sheet

Next gate:

- Eligible for merge/release decision.
- Heartbeat `android-meditation-priority-pro-review` should be deleted after this PASS is reported.

## iOS Sprint 11 Discovery

Status: `complete_10_10_pass`

- Branch/source: `main` and `codex/sprint11-ios-discovery`
- Latest relevant commit on main: `bae1778` (`Remove iOS reopen gate overlap`)
- Canonical docs:
  - `docs/IOS_DISCOVERY.md`
  - `docs/SPRINT_11_IOS_DISCOVERY.md`
- Final GPT Pro output: `PRO_REVIEW_OUTPUT_20260423_233635_RETRY/iOS_Sprint_11_Review.md`
- Final verdict: `10/10 PASS`
- Recommendation: run a bounded `build_full_ios_spike`, not a promise of Android/iOS parity.

Important decision:

- iOS should be pursued as a Screen Time / FamilyControls / DeviceActivity / ManagedSettings feasibility spike.
- Exact Android overlay parity is not a public-API target on iOS.
- Android remains the MVP launch platform unless the PRD changes.

## iOS Sprint 12 Implementation Spike

Status: `slice_12_1_implemented_pushed_not_currently_under_review`

- Branch: `codex/sprint12-ios-implementation`
- Remote: `origin/codex/sprint12-ios-implementation`
- Commit: `571aef4` (`Add iOS visual parity skeleton`)
- Canonical branch doc: `docs/SPRINT_12_IOS_IMPLEMENTATION.md` on `codex/sprint12-ios-implementation`
- Visual QA artifacts on that branch: `docs/visual-qa/sprint12-ios-slice12-1/`
- Local reference bundle: `output/ios-android-visual-parity-reference-20260424_083016/IOS_ANDROID_VISUAL_PARITY_REFERENCE_20260424_083016.zip`

Implemented in Slice 12.1:

- Native SwiftUI project under `ios/`
- Visual parity skeleton for home, library, intervention, reader, link handoff, meditation, progress, and settings
- Shared visual language: parchment/dark palette, rounded cards, serif display typography, finite replacement shape
- iOS unit tests and UI screenshot tests
- Simulator visual QA screenshots

Known state:

- This is a visual/flow skeleton, not a complete iOS release implementation.
- Screen Time APIs, app shielding, FamilyControls authorization, DeviceActivity monitoring, real persistence, app selection, and signing/release packaging are not complete.
- The lane is not currently waiting on an active GPT Pro heartbeat.

Next gate:

- Resume this branch in a clean iOS worktree.
- Launch GPT Pro review for Slice 12.1 with the branch doc, source, tests, and visual QA screenshots.
- Iterate until `10/10 PASS`.
- Only then proceed to Slice 12.2.
