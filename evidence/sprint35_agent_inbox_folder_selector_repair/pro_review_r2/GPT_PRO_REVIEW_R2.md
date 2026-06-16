SCORE: 9/10

VERDICT: REVISE

VISUAL REVIEW: PASS

FRESH FINDINGS:

Legacy picker_folder is repaired by the visible Compose path, but it is not hard-blocked below the UI. app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt:780-785 routes a legacy picker grant into readonly reconnect/repair, which is the desired user-facing behavior. However, app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt:276-279 still includes hasAgentInboxPickerFolderGrant in hasAgentInboxDriveFolderGrant, and scanAgentInboxDrive() admits any such grant with a nonblank token at app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt:1307-1335. importAgentInboxCandidate() applies the same token requirement without excluding picker_folder at app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt:1715-1735. Persistence also treats picker_folder as a supported scan-success grant at app/src/main/java/com/qualityalternative/app/data/PreferencesSettingsRepository.kt:546-550, and app/src/test/java/com/qualityalternative/app/data/PreferencesSettingsRepositoryTest.kt:426-440 explicitly asserts that a picker_folder grant can persist scan success. The bundle proves that the main UI no longer requests drive.file for Agent Inbox, but it does not prove the stronger invariant that legacy picker grants cannot continue as operational grants beneath the UI layer.

TRACE CHECKS:

Bundle scope is adequate for source/routing/evidence review. evidence/sprint35_agent_inbox_folder_selector_repair/pro_review_r2/REVIEW_BUNDLE_MANIFEST.md includes the current Agent Inbox UI, ViewModel, authorization, production wiring, Drive/document-tree clients, package model/importer, tests, portable authoring docs, validator, and release evidence, while excluding the full noisy lane tracker and raw Drive/rclone listings.

R1 bundle-completeness blockers are materially addressed. app/src/main/java/com/qualityalternative/app/data/AppContainer.kt:91-97 wires AndroidHybridAgentInboxDriveClient and AgentInboxPackageImporter in production. app/src/main/java/com/qualityalternative/app/domain/service/ReadingAnnotationDriveSync.kt:3-4 ships the drive.file and drive.readonly constants. App-side package model/validation/import sources are present in app/src/main/java/com/qualityalternative/app/data/AgentInboxManifest.kt, AgentInboxReviewCandidate.kt, AgentInboxDocumentStore.kt, and AgentInboxPackageImporter.kt.

Agent Inbox production folder selection no longer uses a Google file Picker route. app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt:582-602 uses ActivityResultContracts.OpenDocumentTree() for Agent Inbox, persists a read URI grant through persistAgentInboxFolderPermission(), and connects the selected tree URI through connectAgentInboxDocumentTreeFolder().

Android document-tree selection is the primary selector. The scan button falls through to beginAgentInboxFolderSelection() and agentInboxFolderPicker.launch(null) when no Agent Inbox grant exists at app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt:786-788.

Local/system document-tree scans and imports do not require a Google token. app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt:594-598 and :770-775 call scanAgentInboxDrive(accessToken = "") for non-Google document trees, while app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt:1313-1319 requires a token only for non-document-tree grants or Google Drive-backed document-tree URIs. app/src/main/java/com/qualityalternative/app/data/AndroidDocumentTreeAgentInboxClient.kt:80-118 scans through DocumentsContract/ContentResolver, and :120-138 downloads through openInputStream() without using the token.

Google Drive-backed document-tree selections require readonly authorization. app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt:594-595 detects Google Drive document-tree URIs and starts AGENT_INBOX_READONLY_SCAN; app/src/main/java/com/qualityalternative/app/ui/GoogleDriveAuthorization.kt:27-40 requests only AGENT_INBOX_DRIVE_READONLY_SCOPE for Agent Inbox connect/scan/import modes. app/src/main/java/com/qualityalternative/app/domain/service/ReadingAnnotationDriveSync.kt:3-4 confirms annotation sync remains drive.file while Agent Inbox uses drive.readonly.

Google Drive scans are bounded to the selected or extracted folder id. app/src/main/java/com/qualityalternative/app/data/AndroidDocumentTreeAgentInboxClient.kt:38-46 extracts the Drive folder id from a Google Drive document-tree URI and routes only that id to the Drive client. app/src/main/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClient.kt:30-49 requires a provided folder id, :67-81 lists only children whose parent is that id or a package-folder id, and :94-100 uses a bounded files.list request rather than whole-Drive discovery. app/src/test/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClientTest.kt:50-154 asserts parent-bounded queries, no name search, and no folder creation.

Existing readonly_folder grants continue through readonly scan/import. app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt:777-779 routes readonly scan through AGENT_INBOX_READONLY_SCAN, and :797-806 routes non-document-tree imports through AGENT_INBOX_READONLY_IMPORT.

Package authoring instructions are portable. docs/AGENT_INBOX_PACKAGE_AUTHORING.md:6-12 explicitly forbids assuming the operator’s machine, rclone config, Google account, Drive folder id, absolute paths, remote name, account, or path; :124-125 forbids raw Drive ids, absolute paths, tokens, and user-specific machine paths in manifests; :151-175 describes direct-child package upload under the user-selected Agent Inbox folder. tools/validate_agent_inbox_package.py:68-216 implements a local folder validator without user-specific Drive/rclone assumptions.

Visual evidence passes. app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt:861-970 captures the Sprint35-named flow, asserts that the scan action opens Android DocumentsUI, captures the system folder picker, connected folder state, access-lost recovery, Markdown-image import, and dark-mode connected state. evidence/sprint35_agent_inbox_folder_selector_repair/logs/TEST-sprint35-agent-inbox-folder-selector-repair.xml reports one connected test with zero failures/errors/skips, and evidence/sprint35_agent_inbox_folder_selector_repair/visual_e2e/contact_sheet_sprint35_agent_inbox_folder_selector_repair.png visually matches the claimed states.

BUNDLE GAPS:

evidence/sprint35_agent_inbox_folder_selector_repair/pro_review_r2/REVIEW_BUNDLE_MANIFEST.md intentionally excludes full APK binaries. Therefore the shipped hash files and badging/install logs support the APK claim, but the actual APK bytes cannot be independently re-hashed or re-inspected from this bundle alone. This is an evidence limitation, not a source-routing finding.

PACKAGE HYGIENE:

The bundle is scoped enough for this review and no longer depends on full docs/LANE_STATUS.md or Sprint 34 evidence. The scoped status excerpt is evidence/sprint35_agent_inbox_folder_selector_repair/pro_review_r2/SPRINT35_STATUS_EXCERPT.md, and the review manifest documents the intentional exclusions.

Package authoring hygiene is good: docs/AGENT_INBOX_PACKAGE_AUTHORING.md and tools/validate_agent_inbox_package.py do not assume this user’s rclone remote, account, folder id, tokens, or local paths.

Minor evidence hygiene issue: docs/release-gate-logs/2026-06-16-sprint35-agent-inbox-folder-selector-repair/final_gradle_build.log contains an incidental local lint report path under /Users/omare/.... This does not affect the Agent Inbox package-authoring portability claim, but it is still user-machine leakage in the release evidence log.

RELEASE READINESS:

Not release-ready until the legacy picker_folder invariant is hardened so picker grants are repair-only at the ViewModel/repository boundary, not merely in the Compose click path.

Release evidence is otherwise strong for the requested version. app/build.gradle.kts:16-17 declares versionCode = 39 and versionName = "0.11.23-alpha". docs/release-gate-logs/2026-06-16-sprint35-agent-inbox-folder-selector-repair/apk_debug_badging.txt:1 reports the same version metadata, :100 reports launchable activity com.qualityalternative.app.MainActivity, and docs/release-gate-logs/2026-06-16-sprint35-agent-inbox-folder-selector-repair/dumpsys_package_after_install.txt:62-64 reports versionCode=39 and versionName=0.11.23-alpha after install.

Gate evidence is present: docs/release-gate-logs/2026-06-16-sprint35-agent-inbox-folder-selector-repair/final_gradle_build.log reports BUILD SUCCESSFUL and 104 actionable tasks: 104 executed; the unit XML set under docs/release-gate-logs/2026-06-16-sprint35-agent-inbox-folder-selector-repair/unit-test-results/ contains 45 XML reports totaling 553 tests with zero failures/errors/skips; the connected visual XML passes; apk_debug_sha256.txt and apk_release_unsigned_sha256.txt provide hashes; adb_install_debug.status.txt reports Success; adb_direct_launch_after_install.txt reports a direct launch intent for com.qualityalternative.app/.MainActivity.