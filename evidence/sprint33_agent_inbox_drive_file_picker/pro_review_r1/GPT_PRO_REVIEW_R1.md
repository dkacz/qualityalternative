SCORE: 8/10
VERDICT: REVISE
VISUAL REVIEW: NOT APPLICABLE

FRESH FINDINGS:

Medium — the documented typed/manual readonly folder-id connection path is not reachable from the shipped UI.
Exact claim: docs/LANE_STATUS.md:25 says existing typed/manual readonly folder-id support remains available for non-picker Drive readonly scans/imports, and docs/release-gate/RELEASE_NOTES_v0.11.21-agent-inbox-drive-file-picker-alpha.md:9 says Drive readonly authorization remains available for typed/manual folder-id scans and imports without picker resource parameters.
Why it is vulnerable: the authorization mode and ViewModel methods exist, but the shipped UI source does not expose a caller for the typed/manual connection route. A source-wide check found no call to startGoogleDriveSyncAuthorization(GoogleDriveAuthorizationMode.AGENT_INBOX_CONNECT_READONLY), no caller of beginAgentInboxReadonlyFolderConnection(), and no caller of updateAgentInboxDriveFolderDraft(...). If the drive.file Picker opens but later fails to expose externally added/rclone package children, the user-facing fallback described by the release materials is not actually initiable from Settings in the shipped UI. Existing readonly grants can scan/import, but a fresh typed/manual readonly connection is not proven reachable.
Files checked: src/app-ui/QualityAlternativeApp.kt:437-441, src/app-ui/QualityAlternativeApp.kt:481-497, src/app-ui/MainViewModel.kt:1521-1599, docs/LANE_STATUS.md:22-26, docs/release-gate/RELEASE_NOTES_v0.11.21-agent-inbox-drive-file-picker-alpha.md:6-10.
Tightest fix: wire an explicit Settings fallback that accepts a Drive folder URL/id, calls beginAgentInboxReadonlyFolderConnection(), and then launches AGENT_INBOX_CONNECT_READONLY; add a UI-routing unit test for that path. If the intended shipped product only preserves pre-existing readonly grants, narrow the release notes and lane status to say that.

Medium — the changed authorization tests do not prove the literal drive.file versus drive.readonly scope values from the shipped bundle.
Exact claim: Sprint 33 claims Agent Inbox Picker folder selection now uses drive.file, while readonly paths use drive.readonly without Picker parameters.
Why it is vulnerable: tests/GoogleDriveAuthorizationTest.kt compares picker requests to ANNOTATION_DRIVE_SCOPE and readonly requests to AGENT_INBOX_DRIVE_READONLY_SCOPE, but the shipped bundle does not include the source definitions of either constant. src/domain/AgentInboxDrive.kt contains the grant-mode constants, but not the Drive scope constants that PRIMARY_REVIEW_DOCUMENT.md says it contains. A regression where ANNOTATION_DRIVE_SCOPE was wrong would not be visible from this bundle and would not be caught by these tests as written. The concrete user-facing failure would be reintroducing the same Play Services INTERNAL_ERROR if the picker scope resolved to readonly, or requesting a broader/narrower scope than the release claims.
Files checked: src/app-ui/GoogleDriveAuthorization.kt:31-40, src/app-ui/GoogleDriveAuthorization.kt:43-56, tests/GoogleDriveAuthorizationTest.kt:10-28, tests/GoogleDriveAuthorizationTest.kt:48-69, src/domain/AgentInboxDrive.kt:1-14.
Tightest fix: include the source file that defines ANNOTATION_DRIVE_SCOPE and AGENT_INBOX_DRIVE_READONLY_SCOPE in the review bundle, and add literal-scope assertions such as ANNOTATION_DRIVE_SCOPE == ".../auth/drive.file" and AGENT_INBOX_DRIVE_READONLY_SCOPE == ".../auth/drive.readonly" or assert those literal values directly in the request-shape tests.

TRACE CHECKS:

Primary review document read first: PRIMARY_REVIEW_DOCUMENT.md states the intended Sprint 33 fix is to remove the readonly Picker mode and route Agent Inbox folder selection through drive.file with PICKER_OAUTH_TRIGGER=true, PICKER_ALLOW_FOLDER_SELECTION=true, consent prompt, and picked_file_ids extraction, while keeping readonly only for non-picker typed/manual folder-id flows.

Forbidden mode removal: current shipped source has no AGENT_INBOX_PICK_READONLY_FOLDER occurrence in src/ or tests/. The generated patch shows the old enum value and branch were deleted from GoogleDriveAuthorization.kt and QualityAlternativeApp.kt.

Picker request shape in included production code: src/app-ui/GoogleDriveAuthorization.kt:31-40 maps GoogleDriveAuthorizationMode.AGENT_INBOX_PICK_FOLDER to requestedScopes = listOf(ANNOTATION_DRIVE_SCOPE), optOutIncludingGrantedScopes = true, prompt = AuthorizationRequest.Prompt.CONSENT, and both Picker folder resource parameters: PICKER_OAUTH_TRIGGER and PICKER_ALLOW_FOLDER_SELECTION.

Readonly request shape in included production code: src/app-ui/GoogleDriveAuthorization.kt:43-56 maps AGENT_INBOX_CONNECT_READONLY, AGENT_INBOX_READONLY_SCAN, and AGENT_INBOX_READONLY_IMPORT to AGENT_INBOX_DRIVE_READONLY_SCOPE with no resourceParameters. AGENT_INBOX_CONNECT_READONLY keeps Prompt.CONSENT; readonly scan/import use the default prompt.

Non-picker drive-file scan/import request shape: src/app-ui/GoogleDriveAuthorization.kt:58-66 maps AGENT_INBOX_SCAN and AGENT_INBOX_IMPORT to ANNOTATION_DRIVE_SCOPE without Picker resource parameters.

Picker result handling: src/app-ui/QualityAlternativeApp.kt:428-435 handles AGENT_INBOX_PICK_FOLDER by reading result.pickedDriveFileIds().firstOrNull(), calling viewModel.connectAgentInboxDriveFolder(folderId), and then scanning with the returned token.

Google Drive document-tree selection routing: src/app-ui/QualityAlternativeApp.kt:602-609 redirects a Google Drive OpenDocumentTree URI to startGoogleDriveSyncAuthorization(GoogleDriveAuthorizationMode.AGENT_INBOX_PICK_FOLDER) before persisting or scanning the Android provider URI.

Legacy Google Drive document-tree reconnect routing: src/app-ui/QualityAlternativeApp.kt:792-809 routes scan actions for Google Drive document-tree grants to AGENT_INBOX_PICK_FOLDER, not to the old fragile Android provider scan. It also routes already-readonly grants to AGENT_INBOX_READONLY_SCAN and already-picker grants to AGENT_INBOX_SCAN.

Empty Google-Drive-configured state routing: src/app-ui/QualityAlternativeApp.kt:9861-9866 returns true from agentInboxShouldUseDrivePicker(state) when there is no Agent Inbox folder grant and Google Drive is already configured through annotation Drive sync or a Google Drive-backed annotation export URI. The scan button path at src/app-ui/QualityAlternativeApp.kt:807-808 then launches AGENT_INBOX_PICK_FOLDER.

Grant-mode persistence: src/app-ui/MainViewModel.kt:1548-1573 persists Picker-selected folders with AGENT_INBOX_DRIVE_GRANT_MODE_PICKER_FOLDER; src/app-ui/MainViewModel.kt:1576-1599 persists readonly folder-id connections with AGENT_INBOX_DRIVE_GRANT_MODE_READONLY_FOLDER; src/domain/PreferencesSettingsRepository.kt:355-366 stores only supported grant modes and defaults unknown modes to picker-folder.

Scan scope limitation visible in included ViewModel: src/app-ui/MainViewModel.kt:1307-1335 requires a selected folder id and grant mode, then passes folderId = selectedFolderId into AgentInboxDriveScanRequest. This supports the privacy claim at the UI/ViewModel boundary, but the concrete Drive client implementation is not included, so the actual Drive API query cannot be independently audited from the bundle.

Tests: tests/GoogleDriveAuthorizationTest.kt:10-28 covers Picker folder parameters and consent; tests/GoogleDriveAuthorizationTest.kt:30-45 covers non-picker drive-file modes without Picker parameters; tests/GoogleDriveAuthorizationTest.kt:48-69 covers readonly modes without Picker parameters; tests/GoogleDriveAuthorizationUiTest.kt:72-113 covers the Google-Drive-configured empty-state and legacy document-tree helper logic. Coverage is reasonable for the hotfix’s mode mapping, but not sufficient for 10/10 because the literal scope constants are absent and the typed/manual readonly connection route is not tested.

Release metadata consistency: docs/release-gate/apk_debug_output_metadata.json reports versionCode=37, versionName=0.11.21-alpha, applicationId=com.qualityalternative.app, variantName=debug. generated/diff_v0.11.20_to_v0.11.21_release_commit.patch shows the version bump from 36 / 0.11.20-alpha to 37 / 0.11.21-alpha.

Tag, SHA, and asset consistency: generated/release_tag_ref.txt tags v0.11.21-agent-inbox-drive-file-picker-alpha at 28bbf7f8cedb619c976eac05f1c55b8cb9f5407a. generated/github_release_view_v0.11.21.json uses the same tag and target commit. The APK asset digest is sha256:8390cf0fb2a09c0301e11cb9f850a0da3031e2c42f24db6ad8b76064b07760da, matching docs/release-gate/apk_sha256.txt and the release notes. The .sha256 release asset digest 412c71389865378893c9db3bdc7e6e9ff4a33fd20702b99005b4a20826c99448 matches the hash of the shipped apk_sha256.txt.

Post-release HEAD consistency: generated/current_head.txt shows current HEAD at 8e6ef274274a4c63669c73a5247fc20b2e516497, after the release commit. generated/diff_release_commit_to_current_head.patch only changes docs/LANE_STATUS.md from release_ready_local to release_published and records the release commit/tag/URL/next gate; it does not show production-code drift after the tagged release.

Connected visual/e2e: docs/release-gate/connected_visual_e2e.status.txt says NOT RUN, with the reason that no device was attached and no emulator binary was available. docs/release-gate/adb_devices.txt contains only List of devices attached. This is honestly documented and should not by itself block this narrow request-shape hotfix, because the specific Sprint 33 defect is the authorization request shape rather than visual layout. It remains a live-device residual risk, not passed evidence.

BUNDLE GAPS:

Blocks 10/10 evidence, not a proven production behavior blocker: the bundle omits the source definitions of ANNOTATION_DRIVE_SCOPE and AGENT_INBOX_DRIVE_READONLY_SCOPE. Because the central claim is specifically drive.file versus drive.readonly, the current bundle proves only “Picker uses ANNOTATION_DRIVE_SCOPE” and “readonly modes use AGENT_INBOX_DRIVE_READONLY_SCOPE,” not the literal scope strings.

Honest residual privacy risk: the concrete AgentInboxDriveClient implementation is absent. The included ViewModel passes a selected/supplied folderId into the scan request, but the bundle does not prove that the Drive API client actually limits files.list to that folder or never searches the user’s whole Drive.

Honest residual routing risk: the source definition of isGoogleDriveDocumentTreeUri is absent. The included UI tests cover representative Google Drive document-tree strings through the helper path, but the actual URI predicate cannot be inspected from shipped files.

Honest residual release-artifact risk: the APK binary is intentionally excluded. The shipped digest, checksum file hash, GitHub release asset digest, tag, commit, versionName, and versionCode are internally consistent, but the actual APK bytes cannot be rehashed from the bundle.

PACKAGE HYGIENE:

The bundle is clean enough for a scoped source-and-release-evidence review, with curated current source files, tests, release-gate files, generated diffs, tag refs, and GitHub release metadata. It is not clean enough for a 10/10 adversarial audit because the scope-constant definitions and concrete Drive client are omitted.

Stale/noisy items: docs/SPRINT_28_AGENT_INBOX_DRIVE_ACCESS.md and docs/SPRINT_29_AGENT_INBOX_FOLDER_SELECTOR.md contain historical readonly and connected-evidence context that predates this Sprint 33 hotfix; they are useful background but should not be read as Sprint 33 validation. src/app-ui/QualityAlternativeApp.kt:618-620 retains an unreachable stale branch that would launch AGENT_INBOX_READONLY_SCAN after persisting a Google Drive document-tree URI, but src/app-ui/QualityAlternativeApp.kt:606-609 returns earlier for the same URI shape, so it is package/code noise rather than current shipped behavior.

10/10 RULE:

Do not assign 10/10 to this bundle. The documented absence of connected visual/e2e does not by itself prevent a 10/10 for this narrow hotfix, because it is explicitly disclosed and the hotfix targets a request-builder shape. The score is below 10 because the bundle does not prove the literal OAuth scope constants, omits the concrete Drive scan implementation needed for the privacy check, and documents typed/manual readonly support that is not reachable from the included UI source.