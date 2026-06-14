SCORE: 9/10

VERDICT: PASS

VISUAL REVIEW: PASS

READONLY FALLBACK CHECK:

Authorization: PASS. drive.readonly is scoped to AGENT_INBOX_CONNECT_READONLY, AGENT_INBOX_READONLY_SCAN, and AGENT_INBOX_READONLY_IMPORT; annotation sync, annotation retry, historical Picker scan/import, and Picker folder selection remain on drive.file. AGENT_INBOX_CONNECT_READONLY is the only read-only mode with CONSENT prompt, while read-only scan/import reuse the read-only scope without Picker parameters.

Scan boundary: PASS. The app parses and normalizes only a user-supplied folder URL/id, requires a supported folder grant before scanning, sends the selected folder id to AgentInboxDriveScanRequest, and the Drive client only lists '<saved-folder-id>' in parents for package folders and then '<package-folder-id>' in parents for files. I found no whole-Drive search, no folder-name discovery query, and no fallback auto-create path.

Persistence: PASS. Connected Agent Inbox state hydrates only when enabled=true, a nonblank folder id exists, and the grant marker is in the supported set: picker_folder or readonly_folder. Scan success is accepted only when the returned folder id matches the persisted current folder.

First-scan race: PASS. After read-only OAuth returns, the app writes readonly_folder into UI state through connectAgentInboxReadonlyDriveFolder, immediately calls scanAgentInboxDrive(token), and scanAgentInboxDrive saves the selected folder/grant again immediately before the Drive client call. The scan does not depend on stale repository hydration.

Access-lost: PASS. HTTP 401/403/404 during scan clears enabled/folder/grant/last-success state, preserves the prior folder id in the reconnect draft, clears candidates, and records a privacy-safe reason=access_lost event.

Disconnect/revoke: PASS. Read-only Agent Inbox disconnect revokes drive.readonly; historical Picker Agent Inbox disconnect revokes drive.file only when no separate annotation Google Drive configuration needs that scope. Local Agent Inbox state is cleared independently of annotation Drive state.

Privacy: PASS. Source and tests support that remote-safe analytics contain only grant-mode/status/count/reason metadata, not raw folder ids, file ids, package ids, content names, or raw failure text. Portable Profile export excludes Agent Inbox connection state, raw Drive ids, raw file ids, tokens, grants, and raw scan failures.

Live rclone result: PASS for final app state. The shipped live screenshot and UI XML show a connected Agent Inbox, one package waiting for review, and the visible package title Rclone Readonly Inbox Smoke. The live evidence has a bundle-hygiene defect described below: the shipped logcat does not prove the auth flow, and raw rclone listing JSON files referenced by the result are not present.

REGRESSION CHECK:

R1 Picker-grant blockers: PASS. Legacy enabled=true plus folder-id state without a supported grant marker hydrates disconnected and cannot scan. Missing folder id also blocks scan before any Drive request.

R2/R3 Markdown image safety: PASS. The fallback does not regress the reviewed Markdown image controls: manual image-only follow-up picker imports are supported, Markdown sidecars are bounded and filename-validated, EPUB sidecars remain invalid, duplicate/colliding storage names are rejected, unreviewed local fallback is blocked for Agent Inbox Markdown, and sidecar storage rollback removes promoted sidecars after later write failures.

Analytics/profile privacy: PASS. The read-only fallback uses privacy-safe connection, scan, candidate, import, and access-lost metadata; profile export tests explicitly seed raw annotation/Agent Inbox Drive identifiers and raw failures and assert they are absent from exported JSON.

Visual states: PASS. Direct inspection of contact_sheet_readonly_r1.png, all five raw PNGs in the run, and live_readonly_rclone_success.png confirms the pre-connect broader read-access copy, connected read-only state, access-lost reconnect state with preserved folder draft, Markdown sidecar image reader state, and dark connected state. The live package description is partially covered at the bottom by navigation, but the connected state, package count, package title, format/priority metadata, and scan controls are visible; this is not a visual blocker.

FRESH FINDINGS:

LOW — evidence/package hygiene. Exact claim: live_readonly_rclone_package/RESULT.md claims that logcat_live_readonly_success.txt is a “filtered authorization/app log with Google authorization flow success” and lists rclone_parent_listing.json plus rclone_package_listing.json as evidence. Why vulnerable: the shipped logcat contains only two IPCThreadState warnings and no app/authorization/Google consent entries, while the rclone listing JSON files are not shipped and the R4 manifest says raw rclone listing JSON files are intentionally excluded. A referee can therefore attack the live-flow evidence as overstated, even though the final screenshot and XML do prove the post-scan UI state. Files checked: evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/RESULT.md:24-29, evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/logcat_live_readonly_success.txt:1-2, evidence/sprint28_agent_inbox_drive_access/REVIEW_BUNDLE_MANIFEST_R4_READONLY_FALLBACK.md:53-56, and the shipped evidence file inventory. Tightest fix: either ship a real filtered app/auth log and a sanitized rclone listing summary with raw Drive identifiers redacted, or revise RESULT.md so it no longer names absent JSON files or claims the two-line logcat proves the authorization flow.

TRACE CHECKS:

Primary contract and scope: docs/SPRINT_28_AGENT_INBOX_DRIVE_ACCESS.md:3-7, 19-23, 34-49, 161-178, 204-211; PRD.md:253-273, 431-452, 454-471, 495-502; docs/LANE_STATUS.md:14-62.

R4 manifest and validation summary: evidence/sprint28_agent_inbox_drive_access/REVIEW_BUNDLE_MANIFEST_R4_READONLY_FALLBACK.md:25-63; evidence/sprint28_agent_inbox_drive_access/VALIDATION_SUMMARY.md:45-82, 112-140.

Picker failure basis for fallback: evidence/sprint28_agent_inbox_drive_access/device_spike/live_picker_runtime_20260614/RESULT.md:24-42.

Authorization source/tests: app/src/main/java/com/qualityalternative/app/domain/service/ReadingAnnotationDriveSync.kt:1-4; app/src/main/java/com/qualityalternative/app/ui/GoogleDriveAuthorization.kt:9-18, 27-68, 70-106; app/src/test/java/com/qualityalternative/app/ui/GoogleDriveAuthorizationTest.kt:9-84.

Folder parsing, grant predicates, first scan, access-lost, analytics: app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt:253-275, 1287-1319, 1417-1470, 1492-1579, 1586-1610, 1658-1802, 1980-2032; app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt:2587-3040.

Settings persistence: app/src/main/java/com/qualityalternative/app/data/PreferencesSettingsRepository.kt:68-73, 115-120, 354-397, 545-548; app/src/test/java/com/qualityalternative/app/data/PreferencesSettingsRepositoryTest.kt:393-498.

Drive scan boundary: app/src/main/java/com/qualityalternative/app/domain/service/AgentInboxDrive.kt:3-17, 47-59; app/src/main/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClient.kt:27-49, 65-80, 82-120, 184-204, 227-229; app/src/test/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClientTest.kt:32-154.

UI/revoke paths: app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt:408-539, 752-775, 5322-5444, 9649-9670.

Markdown image sidecar safety: app/src/main/java/com/qualityalternative/app/data/AgentInboxReviewCandidate.kt:116-153, 218-250; app/src/main/java/com/qualityalternative/app/data/AgentInboxDocumentStore.kt:92-143; app/src/test/java/com/qualityalternative/app/data/AgentInboxPackageImporterTest.kt:67-91, 158-187; app/src/test/java/com/qualityalternative/app/data/MarkdownReaderDocumentParserTest.kt:56-90; app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt:2973-3040.

Analytics/profile privacy: app/src/main/java/com/qualityalternative/app/domain/model/AnalyticsPrivacyGuard.kt:18-65; app/src/test/java/com/qualityalternative/app/data/AccountLightProfileExporterTest.kt:86-102, 185-200.

Visual evidence: evidence/sprint28_agent_inbox_drive_access/visual_e2e_readonly_r1/contact_sheet_readonly_r1.png; all raw PNGs in evidence/sprint28_agent_inbox_drive_access/visual_e2e_readonly_r1/sprint28-agent-inbox-drive-access-1781460684272/; evidence/sprint28_agent_inbox_drive_access/visual_e2e_readonly_r1/TEST-sprint28-readonly-visual.xml:1-10; evidence/sprint28_agent_inbox_drive_access/visual_e2e_readonly_r1/logcat-sprint28-readonly-visual.txt.

Live evidence: evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/RESULT.md:11-29; evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/live_readonly_rclone_success.png; evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/window_live_readonly_success.xml; evidence/sprint28_agent_inbox_drive_access/live_readonly_rclone_package/logcat_live_readonly_success.txt:1-2.

Package hygiene checks: shipped file inventory under evidence/sprint28_agent_inbox_drive_access/; absence of *rclone*listing*.json, *account*dump*, *package*dump*, *.apk, and OAuth-token artifacts from the R4 bundle; empty evidence/sprint28_agent_inbox_drive_access/logs/git_diff_check_r4_readonly_fallback.log.

BUNDLE GAPS:

BUNDLE GAP: Raw Gradle output for the listed full/targeted unit-test passes and assembleDebug is not shipped. The bundle contains the source tests, validation-summary pass claims, visual XML pass result, and an empty git diff --check log, but it does not independently prove the unit/build command executions from raw logs.

BUNDLE GAP: The live OAuth/read-only consent flow is not proven by the shipped logcat. The final live UI state is proven by screenshot/XML and summarized by RESULT.md, but the specific auth-flow trace claimed in RESULT.md is not present in the bundle.

PACKAGE HYGIENE:

Clean enough for this lane, with the low evidence-hygiene defect above. The current R4 visual run and live read-only fallback evidence are clearly separated from historical R1/R2/R3 context, and the manifest explicitly marks older Picker-first/R3 materials as historical. Raw rclone listings, account dumps, package dumps, OAuth screenshots, APKs, and release artifacts are excluded as intended.

Remove or revise the stale evidence references in live_readonly_rclone_package/RESULT.md before the next bundle. Add either a sanitized rclone listing summary and a real filtered app/auth log, or state plainly that raw listing/auth-flow artifacts are excluded and that the shipped proof is final-state screenshot/XML plus source/tests. Add raw Gradle logs or a signed validation transcript for the targeted unit tests and debug APK build if the next review is expected to independently verify execution rather than rely on VALIDATION_SUMMARY.md.