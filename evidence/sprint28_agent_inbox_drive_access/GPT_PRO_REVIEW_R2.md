SCORE: 7/10

VERDICT: REVISE

VISUAL REVIEW: PASS

R1 BLOCKER RECHECK:

Fully fixed — R1 blocker: persisted enabled=true plus folder id but no Picker grant marker allowed non-Picker scans. R2 now derives Agent Inbox Drive connectivity from enabled=true, a nonblank folder id, and agent_inbox_drive_grant_mode=picker_folder; legacy Sprint 27 folder ids without that marker hydrate as disconnected and cannot enter AGENT_INBOX_SCAN.

Fully fixed — R1 blocker: enabled=true with no folder id could still render a connected Settings state. R2 removes the connected-without-folder persisted state, and Settings copy, badge, primary button, disconnect button, and scan routing all key off state.hasAgentInboxPickerFolderGrant, not a raw enabled boolean.

Implementation pass — Drive access model. The shipped source keeps Agent Inbox authorization on drive.file, uses Picker folder parameters only for AGENT_INBOX_PICK_FOLDER, parses picked_file_ids, persists the durable picker_folder grant marker, scans only the selected folder id, rejects missing folder ids before HTTP, and contains no Agent Inbox folder name search/create path. The live rclone-after-Picker proof remains a release-gate evidence gap, not a deterministic local implementation failure.

Implementation pass — missing/revoked Drive access. HTTP 401/403/404 scan failures clear the selected folder id, clear the Picker marker, clear candidates, record only reason=access_lost, and return the UI to a select-folder/reconnect state.

Implementation pass — manual Markdown image-only follow-up picker selection. The new picker path allows image-only selections when a Markdown candidate is already in the Add Document form and preserves the edited title, selected topics, priority flag, Markdown URI, and existing candidate state.

Partially fixed — Agent Inbox Markdown image sidecars. The ordinary path covers Markdown-only sidecars, EPUB sidecar rejection, count and byte limits, Drive download limits, local storage, import draft propagation, and reader rendering. The fresh findings below prevent a full implementation PASS for the added image sidecar scope.

FRESH FINDINGS:

Severity: High — Agent Inbox Markdown sidecars are not required to be uniquely addressable, so images can be silently collapsed or overwritten.
Exact claim: A package can include duplicate image sidecar filenames or distinct filenames that canonicalize to the same local storage segment, and R2 can import the package while losing one image’s bytes or mapping two Markdown targets to the same stored file.
Why it is vulnerable: AgentInboxReviewCandidateFactory accepts every safe image sidecar by Drive file id and filename, but it does not reject duplicate file.name values or names that collide after local storage canonicalization. MainViewModel.importAgentInboxCandidate stores downloaded sidecar bytes in imageAttachmentBytes[attachment.fileName], so exact duplicate names collapse before storage. FileAgentInboxDocumentStore.writeImageAttachments then transforms the display name with safeFileSegment() and writes to $contentSafeName-img-$safeAttachmentName; names such as chart one.png and chart:one.png both become chart-one.png, so one sidecar can overwrite the other while both original keys point to one URI.
Files checked: app/src/main/java/com/qualityalternative/app/data/AgentInboxReviewCandidate.kt:115-145,210-234; app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt:1626-1685; app/src/main/java/com/qualityalternative/app/data/AgentInboxPackageImporter.kt:104-125; app/src/main/java/com/qualityalternative/app/data/AgentInboxDocumentStore.kt:98-115,165-171; app/src/main/java/com/qualityalternative/app/data/MarkdownReaderDocumentParser.kt:271-280; app/src/test/java/com/qualityalternative/app/data/AgentInboxReviewCandidateFactoryTest.kt:194-305; app/src/test/java/com/qualityalternative/app/data/AgentInboxPackageImporterTest.kt:66-90.
Tightest fix: Reject duplicate Markdown sidecar names after case-normalization and reject duplicate storage-safe segments during review, or store sidecars by a collision-proof key such as Drive file id plus content hash while still mapping each unique Markdown filename to the correct URI. Add tests for exact duplicate Drive filenames and canonical collisions such as chart one.png versus chart:one.png.

Severity: High — Agent Inbox Markdown reader rendering is not confined to reviewed sidecars.
Exact claim: Imported Agent Inbox Markdown can render local file references that were not included as bounded, reviewed sidecars.
Why it is vulnerable: Agent Inbox Markdown is stored under a local file: URI. The Markdown parser first consults imageAttachmentUris, but when no attachment map entry matches, it preserves explicit URI schemes, converts absolute paths to file: URIs, and resolves relative paths against the local file base URI. The Compose image loader then decodes file: sources and bare local file paths when present and readable by the app. A package can therefore reference ../outside.png or an explicit file:///... target and cause the reader to attempt rendering local bytes that were not in the Drive package sidecar set and were not subject to the sidecar count/size checks.
Files checked: app/src/main/java/com/qualityalternative/app/data/MarkdownReaderDocumentParser.kt:241-269; app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt:10001-10033; app/src/main/java/com/qualityalternative/app/data/RoomUserDocumentRepository.kt:282-292,357-373; app/src/main/java/com/qualityalternative/app/data/AgentInboxDocumentStore.kt:133-142; app/src/test/java/com/qualityalternative/app/data/MarkdownReaderDocumentParserTest.kt:9-30,56-65; PRD.md:259-271.
Tightest fix: Add an Agent Inbox reader mode, or an allowLocalImageFallback=false parser option, so Agent Inbox Markdown resolves images only from data:image/... and the vetted imageAttachmentUris map. Manual local Markdown can keep relative-file fallback, but Agent Inbox imports should render unmatched local, absolute, parent-directory, and non-attachment URI image targets as placeholders. Add regression tests for ../outside.png, absolute file paths, and unmatched explicit file: image sources in an Agent Inbox imported document.

Severity: Medium — sidecar rollback is incomplete for mid-write attachment failures.
Exact claim: If local sidecar writing fails after one or more attachment files have already been written, those sidecar files can remain behind even though the import fails.
Why it is vulnerable: In the new-document branch, attachmentUris is assigned only after writeImageAttachments returns successfully; if the helper throws mid-loop, the catch block deletes the main content file and then iterates an empty attachmentUris map, leaving any previously written sidecar files. The existing-file branch calls writeImageAttachments before returning and has no equivalent catch around attachment writes. Current rollback tests cover repository rejection after a successful store write, but not a store-level mid-attachment failure.
Files checked: app/src/main/java/com/qualityalternative/app/data/AgentInboxDocumentStore.kt:57-64,69-95,98-115,145-160; app/src/main/java/com/qualityalternative/app/data/AgentInboxPackageImporter.kt:130-165; app/src/test/java/com/qualityalternative/app/data/AgentInboxPackageImporterTest.kt:333-371.
Tightest fix: Write sidecars to temporary files and move them into place only after all attachments succeed, or track each written sidecar file inside writeImageAttachments and delete tracked files on failure. Add a store test that injects a failure after the first sidecar write and asserts the storage root contains neither the main document nor any sidecar file.

TRACE CHECKS:

Primary sprint and prior review documents: docs/SPRINT_28_AGENT_INBOX_DRIVE_ACCESS.md:3-19,30-42,44-62,64-80,82-99,101-117,119-152; evidence/sprint28_agent_inbox_drive_access/REVIEW_BUNDLE_MANIFEST_R2.md:13-44; evidence/sprint28_agent_inbox_drive_access/VALIDATION_SUMMARY.md:9-21,23-48,50-71; evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R1.md:1-19,43-51; PRD.md:259-271,286,451,470,500-501.

Drive scope, Picker, and grant marker implementation: app/src/main/java/com/qualityalternative/app/domain/service/ReadingAnnotationDriveSync.kt:1-4; app/src/main/java/com/qualityalternative/app/ui/GoogleDriveAuthorization.kt:8-75; gradle/libs.versions.toml:18,49; app/build.gradle.kts:92; source-wide check for drive.readonly in shipped app source found no app-source use; app/src/test/java/com/qualityalternative/app/ui/GoogleDriveAuthorizationTest.kt:9-54.

Picker result routing and UI state: app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt:403-430,457-468,718-724,5277-5363,9575-9589; app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt:211-218,250-253,1463-1518,1908-1937.

Persistence and R1 regressions: app/src/main/java/com/qualityalternative/app/data/PreferencesSettingsRepository.kt:66-73,114-119,353-395,538-542; app/src/test/java/com/qualityalternative/app/data/PreferencesSettingsRepositoryTest.kt:392-475; app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt:2586-2708,2711-2757.

Selected-folder scan semantics and bounded Drive reads: app/src/main/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClient.kt:27-49,65-121,143-181; app/src/test/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClientTest.kt:32-154,302-341; app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt:1266-1394,1416-1441.

Manual Markdown image attachment path: app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt:9943-9986,10145-10161; app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt:1977-1990,2053-2097,2490-2541,5715-5755; app/src/test/java/com/qualityalternative/app/ui/DocumentImportCandidateFactoryTest.kt:44-79; app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt:3820-3858,4138-4200.

Agent Inbox Markdown sidecar review, download, import, storage, and reader rendering: app/src/main/java/com/qualityalternative/app/domain/service/AgentInboxDrive.kt:3-11; app/src/main/java/com/qualityalternative/app/data/AgentInboxReviewCandidate.kt:17-37,61-166,199-234; app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt:1299-1385,1566-1710; app/src/main/java/com/qualityalternative/app/data/AgentInboxPackageImporter.kt:35-165; app/src/main/java/com/qualityalternative/app/data/AgentInboxDocumentStore.kt:48-115,145-171; app/src/main/java/com/qualityalternative/app/data/RoomUserDocumentRepository.kt:125-160,282-292,357-373,511-524; app/src/main/java/com/qualityalternative/app/data/MarkdownReaderDocumentParser.kt:127-160,241-280; app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt:10001-10033; app/src/test/java/com/qualityalternative/app/data/AgentInboxReviewCandidateFactoryTest.kt:194-305; app/src/test/java/com/qualityalternative/app/data/AgentInboxPackageImporterTest.kt:66-90,150-198,232-270,333-371; app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt:2897-2964.

Validation logs: evidence/sprint28_agent_inbox_drive_access/logs/targeted_markdown_image_r2.log; evidence/sprint28_agent_inbox_drive_access/logs/full_local_gate_r2.log; evidence/sprint28_agent_inbox_drive_access/android-results-r2/TEST-qaApi36(AVD) - 16-_app-.xml:2-10; evidence/sprint28_agent_inbox_drive_access/logs/connected_sprint28_visual_r2_final.log.

Visual evidence directly inspected: evidence/sprint28_agent_inbox_drive_access/visual_e2e/contact_sheet_r2.png; evidence/sprint28_agent_inbox_drive_access/visual_e2e/sprint28-agent-inbox-drive-access-1781433607325/00_agent_inbox_select_folder_light.png; 01_agent_inbox_picker_folder_selected_light.png; 02_agent_inbox_access_lost_light.png; 03_agent_inbox_markdown_image_reader_light.png; 04_agent_inbox_picker_folder_selected_dark.png; app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt:849-930,2324-2370,2407-2430,2490-2574.

BUNDLE GAPS:

BUNDLE GAP — live rclone-after-Picker visibility is not proven by shipped files. The implementation can be reviewed locally against the Picker-folder contract, but the shipped bundle still does not prove that a folder selected through Google Picker under drive.file exposes child package folders added later by rclone. The bundle correctly records this as pending in evidence/sprint28_agent_inbox_drive_access/device_spike/RCLONE_PICKER_FOLDER_SPIKE.md and VALIDATION_SUMMARY.md. This remains a release gate.

PACKAGE HYGIENE:

The R2 bundle is clean and sufficient for deterministic local implementation and visual review: it includes the primary document, R2 manifest, R2 validation summary, R1 review, changed source and tests, targeted logs, full local gate logs, Android visual test XML, the R2 contact sheet, and raw PNGs.

The R1 bundle gaps for missing scope definition, missing Agent Inbox model/importer source, and missing physical screenshots are resolved in R2. The live rclone/Picker proof is still absent, but it is explicitly disclosed and should remain outside the deterministic local review score as a release-gate proof.

Two hygiene issues should be corrected next time: device_spike/RCLONE_PICKER_FOLDER_SPIKE.md still says the spike is pending because no device/emulator was attached, while the R2 evidence includes an attached emulator and connected visual run; the accurate pending reason is lack of a signed-in production OAuth/rclone live scenario. Also, docs/SPRINT_28_AGENT_INBOX_DRIVE_ACCESS.md:121-127 still describes visual evidence including a selected-folder package-review screenshot, but the shipped R2 raw PNG set proves selected-folder state, access-lost state, and imported Markdown image reader rendering rather than a separate package-review PNG. Add that raw package-review screenshot or revise the visual-evidence wording to match the shipped artifact set.