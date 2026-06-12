SCORE: 6/10

VERDICT: BLOCK

VISUAL REVIEW: PASS

FRESH FINDINGS:

Severity: High — Agent Inbox imports can overwrite or mutate a prior private document when the same Drive package folder id and content file name are imported again with different bytes.

Exact claim: FileAgentInboxDocumentStore derives the local file path only from packageFolderId and contentFileName, while RoomUserDocumentRepository.addDocument() upserts by uri; therefore, a second accepted package from the same Drive folder and file name can overwrite the old local file and update the existing document row rather than creating a distinct imported document or rejecting the replacement.

Why it is vulnerable: A user-controlled agent can publish content.md, the user imports it, then the agent can later publish different bytes under the same package folder and content.md. The importer writes over the same local file path before the repository upsert. Because RoomUserDocumentRepository reuses an existing row by URI and preserves the existing fingerprint when present, the app can leave a stale fingerprint attached to changed private content. This breaks private-document integrity, duplicate semantics, and the release-gate claim that changed content is guarded by reviewed SHA and byte size.

Files checked: source/app/src/main/java/com/qualityalternative/app/data/AgentInboxDocumentStore.kt lines 31-39 and 44-50; source/app/src/main/java/com/qualityalternative/app/data/AgentInboxPackageImporter.kt lines 86-105; source/app/src/main/java/com/qualityalternative/app/data/RoomUserDocumentRepository.kt lines 66-81 and 117-129; source/app/src/main/java/com/qualityalternative/app/data/local/UserDocumentDao.kt lines 15-25; source/app/src/main/java/com/qualityalternative/app/data/local/UserDocumentEntity.kt lines 7-12; source/app/src/test/java/com/qualityalternative/app/data/AgentInboxPackageImporterTest.kt lines 23-49 and 247-307.

Tightest fix: make Agent Inbox stored document URIs content-addressed or import-instance-addressed, for example include the verified content SHA-256 or a generated import id in the stored filename; reject or version any attempted write to an existing local path unless the existing file’s verified SHA matches; add a repository-backed test proving that two imports from the same package folder and content name with different bytes do not overwrite, upsert, or retain a stale fingerprint.

Severity: High — duplicate review status can be decided from an unverified manifest-declared SHA before the app hashes the actual content bytes.

Exact claim: AgentInboxReviewCandidateFactory maps manifest.documentSha256 to an existing document before MainViewModel downloads and hashes content; because MainViewModel only downloads content when initialCandidate.canImport is true, a manifest that declares the SHA of an existing document is marked DUPLICATE and never byte-verified.

Why it is vulnerable: The manifest is untrusted package metadata. A malformed or adversarial package can claim the SHA of an existing private document while pointing to different content; the UI will present the package as “Already in your library” instead of showing the actual package as ready or as a SHA mismatch. This preempts the R3 claim that content SHA is computed during review for duplicate and mismatch visibility.

Files checked: source/app/src/main/java/com/qualityalternative/app/data/AgentInboxReviewCandidate.kt lines 85-123; source/app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt lines 1294-1301 and 1320-1326; source/app/src/test/java/com/qualityalternative/app/data/AgentInboxReviewCandidateFactoryTest.kt lines 78-95 and 97-115; source/app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt lines 2784-2846.

Tightest fix: never finalize DUPLICATE from manifest.documentSha256 alone. For every package with exactly one manifest and one content file under metadata limits, download bounded content, compute the actual SHA, compare the manifest SHA to the actual SHA, and use the actual SHA as the duplicate key. Add tests for “manifest SHA matches existing document but actual content differs” and “manifest SHA absent/present with actual duplicate.”

Severity: High — manifest and content size caps are applied after an unbounded network read when Drive metadata is missing or wrong.

Exact claim: AndroidGoogleDriveAgentInboxClient.downloadFile() returns request(...).body, and request() reads connection.inputStream.readBytes() without a byte limit; MainViewModel checks manifest and content byte sizes only after that full allocation has completed.

Why it is vulnerable: A user-controlled Drive package can provide an oversized manifest.json or Markdown/EPUB file whose Drive size metadata is absent, stale, or misleading. The app can allocate the full response before it can mark the candidate as MANIFEST_FILE_TOO_LARGE or CONTENT_FILE_TOO_LARGE, creating a release-gate availability attack against the finite review surface.

Files checked: source/app/src/main/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClient.kt lines 49-55 and 172-205; source/app/src/main/java/com/qualityalternative/app/domain/service/AgentInboxDrive.kt lines 7-8 and 39-42; source/app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt lines 1274-1291 and 1304-1318; source/app/src/test/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClientTest.kt lines 270-288; source/app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt lines 2850-2892.

Tightest fix: change the Drive client API to accept a maximum byte count for manifest and content downloads, stream into a bounded buffer, abort at limit + 1, and return a typed too-large result without allocating the full body. Add HTTP-server tests where metadata is missing or understated and the response body exceeds 64 KiB or 10 MiB.

Severity: Medium — the shipped UI and ViewModel do not implement an explicit package reject/remove path or a rejected-package analytics event.

Exact claim: The review UI exposes scan, disconnect, priority toggle, and import, but no operator action to reject or remove a package from the finite review list; AnalyticsEventType has detected/imported/import-failed/duplicate/priority/disconnect events, but no rejected candidate event.

Why it is vulnerable: PRD and sprint docs require a visible accept/reject step and analytics for accepted, rejected, duplicate, and validation-failed packages. Without a reject action, a stale or intentionally noisy package can remain pinned in the review surface until the user cleans up Drive externally, and the release gate cannot prove rejected-package behavior.

Files checked: PRD.md lines 443 and 497; docs/SPRINT_27_AGENT_CONTENT_INBOX.md lines 110-116 and 128-136; source/app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt lines 5268-5480; source/app/src/main/java/com/qualityalternative/app/domain/model/InterventionModels.kt lines 79-86; grep over source/app/src/main/java for rejectAgentInbox and AGENT_INBOX_CANDIDATE_REJECTED.

Tightest fix: add a local “Remove from review” or “Reject package” action for each candidate, remove it from agentInboxCandidates, record a privacy-safe rejected event using only status/format/priority/error counts, and add ViewModel, analytics, and visual tests for rejection.

Severity: Medium — initial Agent Inbox folder discovery is an unqualified name search, not the explicit parent-folder query claimed by the sprint plan.

Exact claim: On first scan with no stored folder id, AndroidGoogleDriveAgentInboxClient calls findFolder() with name = 'Quality Alternative Agent Inbox', folder MIME type, trashed = false, and pageSize=1; only after a folder id is selected do child package and file scans use an explicit parent query.

Why it is vulnerable: If more than one app-accessible folder has the reserved name, the code can select the first returned folder and present its packages as the user’s Agent Inbox. This does not prove live whole-Drive behavior, but from shipped code it does create an ambiguous folder-selection path and conflicts with the sprint-plan claim that the client uses explicit parent folder queries for the scoped inbox.

Files checked: docs/SPRINT_27_AGENT_CONTENT_INBOX.md lines 97-106; source/app/src/main/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClient.kt lines 27-30, 58-77, and 95-110; source/app/src/test/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClientTest.kt lines 31-142.

Tightest fix: on first connection, create and persist an app-owned Agent Inbox folder id without satisfying the connection from an unqualified name search, or require explicit user folder selection and persist that id. Add tests that initial connection cannot bind to an arbitrary same-name folder.

Severity: Medium — visual fixture target behavior is present in production source paths without a release-wide BuildConfig.DEBUG gate.

Exact claim: The Agent Inbox visual fixture hook in MainViewModel.seedAgentInboxReviewForTests() is debug-gated, and DebugVisualParityDensityScale is debug-gated, but fixture distractor activities and the fixture target registry are under src/main; SupportedCatalog.findByPackage() and InterceptionTargetResolver.resolve() can resolve fixture package/component identities without checking BuildConfig.DEBUG.

Why it is vulnerable: Debug-only visual and interception fixture identities can become production-recognized targets if selected state or a foreground component matches them. This is exactly the class of drift where visual fixture hooks become production behavior. The shipped bundle also lacks an AndroidManifest.xml or release APK to prove the fixture activities are excluded from a release variant.

Files checked: source/app/src/main/java/com/qualityalternative/app/fixture/FixtureDistractorOneActivity.kt lines 23-69; source/app/src/main/java/com/qualityalternative/app/fixture/FixtureDistractorTwoActivity.kt lines 23-69; source/app/src/main/java/com/qualityalternative/app/interception/FixtureTargetRegistry.kt lines 5-37; source/app/src/main/java/com/qualityalternative/app/data/SupportedCatalog.kt lines 16-19; source/app/src/main/java/com/qualityalternative/app/interception/InterceptionTargetResolver.kt lines 15-24; source/app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt lines 4816-4822; source/app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt lines 354-364.

Tightest fix: move fixture activities and registry into src/debug or src/androidTest, or gate every fixture resolution path with BuildConfig.DEBUG; add a release-unit or static source-set test proving fixture packages and fixture components cannot resolve in production builds.

TRACE CHECKS:

PRD: PRD.md, especially FR3B lines 249-269, FR4 lines 270-283, FR5 lines 291-303, FR12 lines 427-448, FR13 lines 450-469, and NFR4 lines 491-497.

Sprint and bundle docs: docs/SPRINT_27_AGENT_CONTENT_INBOX.md; evidence/sprint27_agent_content_inbox/REVIEW_BUNDLE_MANIFEST.md; evidence/sprint27_agent_content_inbox/VALIDATION_SUMMARY.md.

Implementation: AgentInboxManifest.kt; AndroidGoogleDriveAgentInboxClient.kt; AgentInboxReviewCandidate.kt; AgentInboxPackageImporter.kt; AgentInboxDocumentStore.kt; MainViewModel.kt; QualityAlternativeApp.kt; AccountLightProfile.kt; PreferencesSettingsRepository.kt; AnalyticsPrivacyGuard.kt; ReadingAnnotationDriveSync.kt; SupportedCatalog.kt; FixtureTargetRegistry.kt; InterceptionTargetResolver.kt.

Unit tests and XML: AgentInboxManifestValidatorTest.kt; AgentInboxReviewCandidateFactoryTest.kt; AgentInboxPackageImporterTest.kt; AndroidGoogleDriveAgentInboxClientTest.kt; MainViewModelTest.kt; AccountLightProfileExporterTest.kt; AnalyticsPrivacyGuardTest.kt; matching XML under evidence/sprint27_agent_content_inbox/unit-results/testDebugUnitTest/. Parsed result: 491 unit tests, 0 failures, 0 errors, 0 skipped.

Canonical connected result: evidence/sprint27_agent_content_inbox/android-results/connected_debug/TEST-qaApi36(AVD) - 16-_app-.xml; test-result-exit-code.txt; qaApi36(AVD) - 16/logcat-com.qualityalternative.app.VisualQaScreenshotTest-captureSprint27AgentInboxReviewScreens.txt. Parsed result: 1 connected test, 0 failures, 0 errors, 0 skipped, exit code 0.

Canonical visual run: evidence/sprint27_agent_content_inbox/visual_e2e/sprint27-agent-content-inbox-1781256187276/00_agent_inbox_disconnected_light.png; 01_agent_inbox_connected_empty_light.png; 02_agent_inbox_review_ready_light.png; 03_agent_inbox_priority_accepted_light.png; 04_agent_inbox_invalid_duplicate_light.png; 05_agent_inbox_review_dark.png; 06_agent_inbox_library_imported_markdown_light.png; 07_agent_inbox_intervention_imported_markdown_light.png; 08_agent_inbox_reader_markdown_light.png; 09_agent_inbox_reader_epub_light.png; evidence/sprint27_agent_content_inbox/visual_e2e/sprint27_agent_inbox_contact_sheet.png.

BUNDLE GAPS:

docs/SPRINT_27_AGENT_CONTENT_INBOX.md requires an internal alpha APK, APK path, commit SHA, final summary, and integration method record, but the shipped bundle contains no APK, no commit SHA, and no final release summary proving those release-gate items.

The bundle does not include an AndroidManifest.xml, release manifest, or release APK, so fixture activity registration/export status cannot be proven from shipped artifacts; the production source-level fixture resolution issue remains independently verifiable.

PACKAGE HYGIENE:

The bundle is clean enough for scoped source, test, and visual audit: it contains a single canonical screenshot run, the canonical connected XML/logcat, unit XML results, the review manifest, validation summary, tracked diff, and git status. It is not clean enough for a final release gate because APK/commit/final-summary evidence is missing, and the current-sprint pro_review_harvest files are included as non-canonical review history; they are labeled, but they are noise for a fresh-from-scratch release-gate audit.