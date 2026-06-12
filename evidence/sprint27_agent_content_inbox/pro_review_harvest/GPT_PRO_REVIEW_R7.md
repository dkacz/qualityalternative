SCORE: 9/10

VERDICT: REVISE

VISUAL REVIEW: PASS

FRESH FINDINGS:

High — Import-time duplicate or rejected Agent Inbox imports can leave private document bytes in app-private storage without a library row or visible cleanup path.

Exact claim: AgentInboxPackageImporter.importCandidate() writes the downloaded private Markdown/EPUB bytes to FileAgentInboxDocumentStore before the authoritative atomic repository operation decides whether the verified fingerprint may be inserted. If addDocumentIfFingerprintAbsent() returns Duplicate because another same-SHA import won the race, or returns Rejected, the ViewModel correctly changes the candidate to finite DUPLICATE or non-importable INVALID, but no shipped code deletes the already-written private file.

Why it is vulnerable: This is not the already-covered “two user-document rows” bug; the shipped repository mutex prevents the second database row. The residual privacy/control issue is that the second package can still create a local private file first. With two same-SHA packages from different Drive package folders, FileAgentInboxDocumentStore uses packageFolderId + verifiedSha in the file name, so the race-lost package can create a distinct local file even though the repository returns DUPLICATE. On repository/local save rejection, the same ordering leaves bytes in context.filesDir/agent-inbox-imports while the UI tells the operator that the package could not be saved and exposes only review-row removal. The concrete user/privacy attack is invisible local retention of private agent-supplied document bytes after the product presents the import as duplicate or failed and after the operator removes the review row.

Files checked: app/src/main/java/com/qualityalternative/app/data/AgentInboxPackageImporter.kt:76-91,107-131; app/src/main/java/com/qualityalternative/app/data/AgentInboxDocumentStore.kt:14-22,24-55; app/src/main/java/com/qualityalternative/app/data/AppContainer.kt:91-96; app/src/main/java/com/qualityalternative/app/data/RoomUserDocumentRepository.kt:69-95; app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt:1667-1705,1709-1762; app/src/test/java/com/qualityalternative/app/data/AgentInboxPackageImporterTest.kt:150-177,280-297,441-505; app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt:2823-2870,3053-3093,7548-7575.

Tightest fix: Make Agent Inbox local persistence transactional from the user’s perspective. Either write to a staging file and delete it on Duplicate, Rejected, or exception before returning the import result, then promote it only after the repository accepts the row, or extend AgentInboxDocumentStore with a cleanup/delete operation and call it on every post-write non-imported result. Add tests that assert only one stored file remains for concurrent same-SHA packages from different package folders, and that repository rejection or local save failure leaves no staged Agent Inbox file after the candidate becomes INVALID/LOCAL_IMPORT_REJECTED.

TRACE CHECKS:

PRD alignment: PRD.md:253-268 for FR3B Agent Inbox; PRD.md:283 for private provenance without raw Drive identifiers in analytics/profile; PRD.md:303 for priority only after user acceptance; PRD.md:427-448 for analytics privacy; PRD.md:450-467 for Portable Profile exclusions; PRD.md:493-497 for explicit, finite, user-reviewed cloud access.

Sprint evidence and release notes: docs/SPRINT_27_AGENT_CONTENT_INBOX.md:5-16, 30-72, 97-107, 157-184, 194-201, 211-220, 230-245, 259-267; evidence/sprint27_agent_content_inbox/REVIEW_BUNDLE_MANIFEST.md:1-53; evidence/sprint27_agent_content_inbox/VALIDATION_SUMMARY.md:1-79.

Manifest/package validation: app/src/main/java/com/qualityalternative/app/data/AgentInboxManifest.kt:57-145,147-150,211-230; app/src/main/java/com/qualityalternative/app/data/AgentInboxReviewCandidate.kt:6-45,47-167.

Drive scan/download implementation: app/src/main/java/com/qualityalternative/app/domain/service/AgentInboxDrive.kt:3-47; app/src/main/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClient.kt:24-63,65-95,97-135,158-195,250-265; app/src/main/java/com/qualityalternative/app/domain/service/ReadingAnnotationDriveSync.kt:3.

Import and duplicate prevention: app/src/main/java/com/qualityalternative/app/data/AgentInboxPackageImporter.kt:32-137; app/src/main/java/com/qualityalternative/app/data/AgentInboxDocumentStore.kt:14-64; app/src/main/java/com/qualityalternative/app/data/RoomUserDocumentRepository.kt:40-43,69-95; app/src/main/java/com/qualityalternative/app/data/local/UserDocumentDao.kt:21-25; app/src/main/java/com/qualityalternative/app/data/local/UserDocumentEntity.kt:7-13.

ViewModel state, priority, analytics, and failure handling: app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt:1254-1420,1461-1503,1506-1588,1591-1765,5001-5018,5223-5230,5660-5670.

Settings/review UI: app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt:410-435,455-505,717-726,5271-5405,5408-5521,5525-5574,9606-9667.

Portable Profile privacy: app/src/main/java/com/qualityalternative/app/data/AccountLightProfile.kt:261-350,1635-1667,1730-1739,1754-1773.

Fixture/debug gating: app/src/main/java/com/qualityalternative/app/interception/FixtureTargetRegistry.kt:31-45; app/src/main/java/com/qualityalternative/app/interception/InterceptionTargetResolver.kt:7-34; app/src/main/AndroidManifest.xml; app/src/debug/AndroidManifest.xml; evidence/sprint27_agent_content_inbox/manifests/release-merged-main-AndroidManifest.xml; evidence/sprint27_agent_content_inbox/manifests/release-packaged-AndroidManifest.xml.

Unit/android coverage checked: app/src/test/java/com/qualityalternative/app/data/AgentInboxManifestValidatorTest.kt; app/src/test/java/com/qualityalternative/app/data/AgentInboxReviewCandidateFactoryTest.kt; app/src/test/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClientTest.kt; app/src/test/java/com/qualityalternative/app/data/AgentInboxPackageImporterTest.kt; app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt; app/src/test/java/com/qualityalternative/app/data/AccountLightProfileExporterTest.kt; app/src/test/java/com/qualityalternative/app/data/AccountLightProfileImporterTest.kt; app/src/test/java/com/qualityalternative/app/interception/InterceptionTargetResolverTest.kt.

Test-result evidence checked: evidence/sprint27_agent_content_inbox/unit-results/testDebugUnitTest/*.xml totals 42 XML suite files, 508 testcases, 0 failures, 0 errors, 0 skipped; evidence/sprint27_agent_content_inbox/android-results/connected_debug/TEST-qaApi36(AVD) - 16-_app-.xml contains one passing VisualQaScreenshotTest.captureSprint27AgentInboxReviewScreens run; evidence/sprint27_agent_content_inbox/android-results/connected_debug/test-result-exit-code.txt is 0.

Connected visual log evidence checked: evidence/sprint27_agent_content_inbox/android-results/connected_debug/logcat-VisualQaScreenshotTest-captureSprint27AgentInboxReviewScreens.txt is present, 999 lines, records the canonical screenshot run, and matches the nested original logcat at evidence/sprint27_agent_content_inbox/android-results/connected_debug/qaApi36(AVD) - 16/logcat-com.qualityalternative.app.VisualQaScreenshotTest-captureSprint27AgentInboxReviewScreens.txt by SHA-256.

Canonical screenshots checked: evidence/sprint27_agent_content_inbox/visual_e2e/sprint27-agent-content-inbox-1781265259529/00_agent_inbox_disconnected_light.png; 01_agent_inbox_connected_empty_light.png; 02_agent_inbox_review_ready_light.png; 03_agent_inbox_priority_accepted_light.png; 04_agent_inbox_invalid_duplicate_light.png; 04b_agent_inbox_rejected_light.png; 05_agent_inbox_review_dark.png; 06_agent_inbox_library_imported_markdown_light.png; 07_agent_inbox_intervention_imported_markdown_light.png; 08_agent_inbox_reader_markdown_light.png; 09_agent_inbox_reader_epub_light.png; evidence/sprint27_agent_content_inbox/visual_e2e/sprint27_agent_inbox_contact_sheet.png.

Visual-specific checks: 02_agent_inbox_review_ready_light.png visibly shows the READY review item with Accept priority, Remove, and Import; 03_agent_inbox_priority_accepted_light.png visibly shows Priority accepted; 04_agent_inbox_invalid_duplicate_light.png shows duplicate and invalid review rows with visible Remove and without import controls; 04b_agent_inbox_rejected_light.png shows review-row removal feedback; 05_agent_inbox_review_dark.png is bounded and legible in dark mode; library, intervention, Markdown reader, and EPUB reader screenshots show imported private content in the expected downstream surfaces.

PACKAGE HYGIENE:

Clean enough for audit, with one implementation finding above. The canonical screenshot run is not ambiguous: evidence/sprint27_agent_content_inbox/visual_e2e/ contains only sprint27-agent-content-inbox-1781265259529/ plus the contact sheet. The canonical connected XML and standalone logcat are present, and the standalone logcat matches the nested device logcat. The debug APK checksum matches the declared SHA-256 74502b4ddb717e72f13c47b2590c00b510965fda687445129f8b093e9cb501af. Release manifest evidence contains no fixture activities. Lint evidence contains warnings and hints but no lint errors; those warnings are not Agent Inbox privacy, visual, or release-gate blockers. Prior GPT Pro R1–R6 harvest files are explicitly listed in the manifest and are review history rather than stale canonical evidence.