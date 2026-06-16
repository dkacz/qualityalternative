SCORE: 8/10

VERDICT: REVISE

VISUAL REVIEW: REVISE

FRESH FINDINGS:

MAJOR - BUNDLE GAP against the production Drive-backed document-tree scan/import claim.

Exact claim: Google Drive-backed OpenDocumentTree selections scan only the selected/extracted folder id through the Drive API client, and Drive-backed imports use readonly Drive authorization.

Why vulnerable: The shipped implementation of AndroidHybridAgentInboxDriveClient supports this behavior, but the shipped source slice does not include the production AppContainer or equivalent dependency-wiring source that proves the release MainViewModel actually receives that hybrid client. MainViewModelFactory passes appContainer.agentInboxDriveClient into the ViewModel, but the implementation of that property is not in the bundle. A referee can therefore accept the UI/ViewModel/client logic while still rejecting the production-routing proof.

Tightest fix: Include the production dependency-wiring source showing agentInboxDriveClient is AndroidHybridAgentInboxDriveClient(context) or equivalent in the release app, plus a small wiring test or diff hunk tying the release container to the hybrid client.

MINOR - Visual access-lost evidence is not production-state-equivalent.

Exact claim: The UI evidence shows a visible recovery path rather than a false successful empty scan when access is insufficient.

Why vulnerable: The contact sheet's access-lost panel visibly shows the recovery copy, but it is produced by a debug seed helper, not by the production access-lost branch. The production branch clears agentInboxDriveFolderDraft, while the visual fixture leaves the prior draft intact; the screenshot therefore shows a stale content://... value inside the "Drive folder link or id" field. A referee can attack the screenshot as evidence of a seeded, non-production state even though the source branch itself is better behaved.

Tightest fix: Either trigger the production access-lost branch in the screenshot test or make the debug seed mirror reportAgentInboxDriveAccessLost, then recapture Sprint 35-named visual evidence and assert the error text plus empty recovery field.

TRACE CHECKS:

- Agent Inbox Google file Picker production route is removed from the shipped UI/auth source. GoogleDriveAuthorizationMode contains only annotation modes and Agent Inbox readonly modes. No AGENT_INBOX_PICK_FOLDER mode exists.
- Android OpenDocumentTree is the primary Agent Inbox folder selector. Local/system trees proceed without a Google token.
- Google Drive-backed OpenDocumentTree selections require a readonly token and are scanned through the Drive API using the extracted selected folder id.
- Existing readonly_folder grants route through AGENT_INBOX_READONLY_SCAN and AGENT_INBOX_READONLY_IMPORT.
- Existing legacy picker_folder grants are preserved only as repair state and routed to reconnect/readonly repair instead of drive.file scan/import.
- Google Drive scanning remains parent-constrained to the saved selected folder id and does not do folder-name discovery or create/search behavior.
- Drive HTTP 401/403/404 and document-tree access loss are treated as access-lost recovery rather than successful empty scans.
- Package authoring docs require complete package folders and validation before upload, with no machine-specific path, rclone remote, Google account, Drive folder id, or access-token assumptions.

BUNDLE GAPS:

- Production dependency wiring for agentInboxDriveClient is absent. This blocks full proof of the Drive-backed document-tree scan/import claim from the shipped bundle alone.
- The source file defining AGENT_INBOX_DRIVE_READONLY_SCOPE and ANNOTATION_DRIVE_SCOPE is not shipped.
- The app-side package validation factory and model definitions used by MainViewModel.kt are not shipped.
- APK hashes are present, but APK binaries are intentionally excluded. Unit-test source is present and the aggregate Gradle log reports success, but no unit-test XML report is shipped, and testDebugUnitTest is UP-TO-DATE in the final log rather than freshly executed in that log.

PACKAGE HYGIENE:

The bundle is close, but not clean enough for a PASS because it omits production wiring and several source definitions needed to prove scoped claims end to end.

Stale, redundant, or misleading artifacts to remove or repackage in future review packets:

- docs/LANE_STATUS.md keeps extensive superseded Picker-era implementation notes. They are marked historical, but they are noisy in a scoped Sprint 35 packet.
- Sprint 34 review output is intentionally historical context, but should be summarized rather than bundled beside current Sprint 35 proof artifacts.
- Visual evidence remains named sprint29-agent-inbox-folder-selector even though it is retained for Sprint 35; the test name and screenshot directory should be copied or renamed for Sprint 35 to avoid stale-evidence attacks.
- VALIDATION_SUMMARY.md mixes process metadata with proof artifacts.
- Future packets should include APK analyzer manifest output or the APKs when APK metadata/hashes are a scoped claim.
