SCORE: 8/10

VERDICT: REVISE

VISUAL REVIEW: REVISE

FRESH FINDINGS:

Severity: High — document-tree access loss during file opening is swallowed as a package download failure instead of returning to the select-folder state.
Exact claim: OpenDocumentTree access loss is handled correctly only when the tree query itself fails; if access is lost or the provider becomes unavailable while opening manifest, content, or sidecar image streams, the app keeps the selected-folder state and marks a package invalid rather than clearing the grant and showing the reconnect/select-folder state.
Why vulnerable: AndroidDocumentTreeAgentInboxClient.downloadFile() converts SecurityException, FileNotFoundException, IllegalArgumentException, and null streams into AgentInboxDriveAccessLostException. However, MainViewModel.scanAgentInboxDrive() catches generic Throwable inside the manifest/content download paths and converts it to DOWNLOAD_UNAVAILABLE, so the outer access-lost handler never runs; scan can then save a successful timestamp against the same selected tree. MainViewModel.importAgentInboxCandidate() repeats the same pattern for reviewed content and sidecar image downloads, leaving the stale document-tree connection in place after import-time access loss. A concrete referee attack is: select a folder, scan packages, revoke the persisted tree grant or make the Drive DocumentsProvider unavailable between listing and stream open, then scan/import; the app can show invalid package/download-unavailable instead of clearing to “Choose folder.”
Files checked: app/src/main/java/com/qualityalternative/app/data/AndroidDocumentTreeAgentInboxClient.kt lines 89-105; app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt lines 1346-1363, 1389-1408, 1475-1485, 1745-1774, 1790-1819, 2057-2084; app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt lines 2829-2874, 3478-3520, 3811-3832.
Tightest fix: special-case AgentInboxDriveAccessLostException in the inner scan download catches and rethrow it so the existing outer scan access-lost handler clears state; in import, special-case the same exception before DOWNLOAD_UNAVAILABLE and call the access-lost state-clearing path. Add two focused tests: one where document-tree manifest/content download throws AgentInboxDriveAccessLostException during scan, and one where content or sidecar image download throws it during import; both should assert agentInboxDriveEnabled == false, agentInboxDriveFolderId == null, agentInboxDriveGrantMode == null, candidates cleared, and safe reason=access_lost analytics.

Severity: Low — the canonical selector visuals still carry stale Drive/connect framing in document-tree states.
Exact claim: the Sprint 29 visual evidence proves the picker launch and core screens, but the connected document-tree screen is still badged DRIVE, and the access-lost screenshot says AGENT INBOX FOLDER ACCESS WAS LOST. CONNECT THE FOLDER AGAIN while the selector action is Choose folder.
Why vulnerable: the implementation no longer requires Google Drive OAuth for document_tree_folder, and the actual document-tree scan failure branch uses “Choose the folder again.” The canonical screenshots nevertheless preserve old Drive/connect language, so a referee can point to 01_agent_inbox_document_tree_folder_connected_light.png, 02_agent_inbox_access_lost_light.png, and 04_agent_inbox_document_tree_folder_connected_dark.png and argue that the visible evidence still presents the new Android folder grant as a Drive connection or old connect-flow copy. This does not change the empirical implementation, but it prevents a clean visual pass.
Files checked: app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt lines 5362-5368 and 9667-9689; app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt lines 1478-1484 and 5350-5367; app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt lines 900-911; screenshots 01_agent_inbox_document_tree_folder_connected_light.png, 02_agent_inbox_access_lost_light.png, and 04_agent_inbox_document_tree_folder_connected_dark.png.
Tightest fix: render the section badge as Folder or Selected for AGENT_INBOX_DRIVE_GRANT_MODE_DOCUMENT_TREE_FOLDER, keep Drive only for Google picker/readonly compatibility modes, change the access-lost visual fixture to “Choose the folder again,” and recapture the selector visual sheet.

TRACE CHECKS:

Primary document read first: docs/SPRINT_29_AGENT_INBOX_FOLDER_SELECTOR.md lines 1-16 and 18-26.

Folder selector and permission flow:

app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt lines 592-608: rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()), persisted read permission, connectAgentInboxDocumentTreeFolder(uri.toString(), displayName), immediate scan with accessToken = "".

app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt lines 774-806: disconnected Agent Inbox launches agentInboxFolderPicker.launch(null); document-tree scan/import pass empty token.

app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt lines 5351-5455: visible Agent Inbox section shows Choose folder, not a pasted folder URL/id text field.

app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt lines 10161-10168: persisted read grant and release helper.

Production routing and document-tree scan:

app/src/main/java/com/qualityalternative/app/domain/service/AgentInboxDrive.kt lines 3-13 and 56-65: limits, grant modes, access-lost exception, client contract.

app/src/main/java/com/qualityalternative/app/data/AppContainer.kt lines 91-97: production uses AndroidHybridAgentInboxDriveClient.

app/src/main/java/com/qualityalternative/app/data/AndroidDocumentTreeAgentInboxClient.kt lines 24-43: hybrid routes content://tree/... scans and content:// downloads to the document-tree client.

app/src/main/java/com/qualityalternative/app/data/AndroidDocumentTreeAgentInboxClient.kt lines 49-87: scans the selected tree URI, direct child folders only, direct child package files only.

app/src/main/java/com/qualityalternative/app/data/AndroidDocumentTreeAgentInboxClient.kt lines 108-155, 174-181, 211-220, and 224-229: bounded child queries, provider exception mapping, root/package row caps, URI routing.

ViewModel state, token gating, privacy, access loss:

app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt lines 255-273: document-tree grant is a first-class connected state.

app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt lines 1296-1328: document-tree scan does not require a Google access token.

app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt lines 1426-1467: scan success saves the selected folder and keeps document-tree display draft safe.

app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt lines 1475-1485 and 2057-2084: top-level access-lost state clearing path.

app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt lines 1599-1634: connectAgentInboxDocumentTreeFolder() persists local URI and records only grantMode=documentTreeFolder.

app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt lines 1714-1819: document-tree import does not require a Google token, but contains the access-lost swallowing issue above.

app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt lines 5584-5590 and 6033-6042: Agent Inbox candidate and content analytics metadata omit raw folder IDs, file IDs, paths, and names.

Persistence:

app/src/main/java/com/qualityalternative/app/data/PreferencesSettingsRepository.kt lines 69-75 and 116-120: persisted Agent Inbox connection restores only with a supported grant mode.

app/src/main/java/com/qualityalternative/app/data/PreferencesSettingsRepository.kt lines 355-398 and 546-550: document_tree_folder is supported; scan success preserves the grant only when the returned folder matches the current folder.

app/src/main/java/com/qualityalternative/app/data/PreferencesSettingsRepository.kt lines 166-192: portable settings replacement does not import Agent Inbox connection fields.

Unit tests:

app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt lines 2737-2773: document-tree connection persists locally and remote-safe analytics do not include the raw URI.

app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt lines 2829-2874: Google access failure clears the connection; this does not cover document-tree download-time access loss.

app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt lines 2878-2906: document-tree scan uses empty token.

app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt lines 2910-2975: scan candidate review remains finite/private and analytics-safe.

app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt lines 3048-3097: document-tree import uses empty token.

app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt lines 3101-3166: Markdown sidecar image attachments are downloaded and stored.

app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt lines 3683-3759 and 3870-3916: manifest/content limits and truncated package/file results remain covered.

app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt lines 8098-8137: fake Drive client records access tokens and can throw download failures.

app/src/test/java/com/qualityalternative/app/data/PreferencesSettingsRepositoryTest.kt lines 394-497: picker, readonly, and document-tree Agent Inbox grants persist; mismatched scan success clears the connection.

Visual evidence:

evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r1/contact_sheet_selector_r1.png: shows disconnected Choose folder, Android DocumentsUI picker, document-tree connected state, access-lost state, Markdown image rendering, and dark connected state.

Raw screenshots checked: 00_agent_inbox_choose_folder_light.png, 00b_agent_inbox_system_folder_picker_light.png, 01_agent_inbox_document_tree_folder_connected_light.png, 02_agent_inbox_access_lost_light.png, 03_agent_inbox_markdown_image_reader_light.png, 04_agent_inbox_document_tree_folder_connected_dark.png.

evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r1/TEST-sprint29-selector-visual.xml: one connected visual test, zero failures/errors/skips, device qaApi36(AVD) - 16.

evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r1/logcat-sprint29-selector-visual.txt: PickActivity is launched by DocumentsUI; screenshot capture lines cover all six canonical images.

Validation and bundle logs:

evidence/sprint29_agent_inbox_folder_selector/VALIDATION_SUMMARY.md: claimed Kotlin/unit/instrumentation/lint/build validation.

evidence/sprint29_agent_inbox_folder_selector/logs/full_local_gate_r1.status.txt: 0.

evidence/sprint29_agent_inbox_folder_selector/logs/full_local_gate_r1.log: BUILD SUCCESSFUL.

evidence/sprint29_agent_inbox_folder_selector/logs/git_diff_check_r1.status.txt: 0.

evidence/sprint29_agent_inbox_folder_selector/git_status_r1.txt: core new file and evidence are untracked.

BUNDLE GAPS:

BUNDLE GAP — Portable Profile export privacy is not fully provable from shipped source. The bundle includes PreferencesSettingsRepository.replacePortableSettings(), which does not import Agent Inbox connection fields, and tests prove remote-safe analytics do not leak document-tree URIs. The shipped implementation files do not include AccountLightProfileExporter, and I did not find a direct Sprint 29 assertion that an exported Portable Profile omits agentInboxDriveFolderId, document_tree_folder, or a content://tree/... Agent Inbox URI.

BUNDLE GAP — source-level review of Markdown sidecar candidate/import internals is incomplete. Behavior is covered by MainViewModelTest.importAgentInboxMarkdownCandidateDownloadsAndStoresImageAttachments() and the visual reader screenshot, but the shipped implementation set omits AgentInboxReviewCandidateFactory, AgentInboxPackageImporter, and the file-store implementation, so exact source-level proof for manifest-to-file matching, sidecar filename handling, and storage sanitization cannot be completed from the bundle alone.

BUNDLE GAP — the visual run proves the real Android folder picker opens, but not a full real picker selection round-trip. VisualQaScreenshotTest#captureSprint28AgentInboxDriveAccessStates clicks Choose folder, verifies the current package contains documentsui, captures the system picker, presses Back, and then seeds the connected state through connectAgentInboxDocumentTreeFolder(). Source verifies the ActivityResult callback and permission persistence, but the visual evidence does not show choosing an actual DocumentsUI folder and returning a persisted grant.

PACKAGE HYGIENE:

The bundle is usable for scoped review but is not clean enough for a final pass lane. git_status_r1.txt shows the core new implementation file app/src/main/java/com/qualityalternative/app/data/AndroidDocumentTreeAgentInboxClient.kt, the primary Sprint 29 document, and the evidence directory as untracked; consequently sprint29_selector_tracked_diff.patch omits the new document-tree client entirely, and git diff --check does not validate that untracked source. The visual test method and screenshot directory still carry Sprint 28 names (captureSprint28AgentInboxDriveAccessStates, sprint28-agent-inbox-drive-access-...), which is noisy and risks stale-evidence confusion even though the contents are Sprint 29. The logcat is also noisy with unrelated DocumentsUI, Google Play services, EmojiCompat, and MediaProvider errors; these did not fail the test, but a filtered evidence summary would make the lane easier to audit. Next bundle should include staged/tracked diffs for all implementation files, rename the selector visual method/output directory to Sprint 29, include the missing exporter/importer/factory source files or explicitly mark the source subset as non-standalone, add direct Portable Profile export privacy assertions for Agent Inbox, and add document-tree access-lost tests for scan-time and import-time stream failures.