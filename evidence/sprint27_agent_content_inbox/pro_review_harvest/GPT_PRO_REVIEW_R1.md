SCORE: 6/10

VERDICT: BLOCK

VISUAL REVIEW: REVISE

FRESH FINDINGS:

HIGH — Manifest priority is effectively accepted by default.
Exact claim: A manifest with priority = "high" is automatically inserted into agentInboxPriorityAcceptedPackageIds during scan, and import later applies that accepted state without requiring an affirmative priority-specific operator action.
Why it is vulnerable: An agent-controlled package can request high priority and receive ranking priority when the user presses Import, even if the user did not actively accept the ranking boost. This violates the PRD requirement that user-confirmed priority may raise rank only after the user accepts that priority during import.
Files checked: PRD.md:297-304; docs/SPRINT_27_AGENT_CONTENT_INBOX.md:66-72; app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt:1313-1317, 1421-1429; app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt:5423-5439, 5454-5458; app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt:2574-2599, 2624-2676, 2681-2726; screenshots 01_agent_inbox_review_ready_light.png and 02_agent_inbox_priority_toggled_light.png.
Tightest fix: Default manifest priority requests to unchecked/unaccepted, label the control as an explicit acceptance action such as “Accept high priority,” apply priority only after a user-initiated toggle in the current review session, and update the ViewModel tests and screenshots to prove the default-off path.

HIGH — Agent Inbox scan and review are unbounded.
Exact claim: The Drive client follows all Drive pagination with no package cap, the ViewModel maps every returned package into candidates, and the Settings UI renders every candidate with forEachIndexed.
Why it is vulnerable: A user-controlled or compromised Agent Inbox folder can create a feed-like review surface or a large scan workload by adding many package folders. This conflicts with the PRD and sprint guardrail that Agent Inbox review must remain finite and must not become a discovery feed.
Files checked: PRD.md:265-267, 491-497; docs/SPRINT_27_AGENT_CONTENT_INBOX.md:12-16, 118-127; app/src/main/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClient.kt:105-133, 204-208; app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt:1265-1280, 1290-1294, 1318-1322; app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt:5261-5269, 5336-5356; app/src/test/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClientTest.kt:142-184.
Tightest fix: Add hard caps for packages per scan and files per package, stop pagination after the cap, surface an explicit “N more packages not shown” review item, log only capped aggregate counts, and render the candidate list inside a bounded scroll region.

HIGH — Agent Inbox cloud access is not revocable from the Agent Inbox UI.
Exact claim: The repository has a clearAgentInboxDriveConnection() method, but the Settings route and Agent Inbox section expose only Connect/Scan, priority toggle, and import actions; the only wired Drive disconnect action is for annotation sync.
Why it is vulnerable: A user can authorize and persist an Agent Inbox folder id but has no Agent Inbox-specific revocation/clear path in the shipped UI, which violates the PRD requirement that cloud access remain explicit and revocable. The concrete user risk is stale Drive connection state and continued scan affordance after the user expects the Agent Inbox path to be disabled.
Files checked: PRD.md:491-497; docs/SPRINT_27_AGENT_CONTENT_INBOX.md:7-16; app/src/main/java/com/qualityalternative/app/data/PreferencesSettingsRepository.kt:344-362; app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt:488-493, 700-713, 5254-5323; rg check found no onDisconnectAgentInbox or production call site for clearAgentInboxDriveConnection().
Tightest fix: Add a visible Agent Inbox disconnect action that clears enabled state, folder id, last scan metadata, candidates, accepted-priority selections, and any pending import state; if the Drive scope is shared with annotation sync, the UI must explain the shared authorization boundary and revoke the scope only when no other Drive feature remains connected.

HIGH — Portable Profile exports raw SHA-256 fingerprints for private user documents.
Exact claim: User documents are exported with documentFingerprint.strategy = "SHA256_BYTES" and the raw sha256 value when a verified document fingerprint exists. Agent Inbox imports become user documents, so private Markdown/EPUB imports inherit this export path.
Why it is vulnerable: A raw SHA-256 of private document bytes is a stable cross-device identifier and may be dictionary-matched for known documents. The PRD allows only safe document fingerprints and separately forbids reversible document fingerprints; the shipped bundle does not prove that raw SHA-256 is non-reversible or safe for private Agent Inbox documents. Against the supplied privacy attack list, this is a Portable Profile leak.
Files checked: PRD.md:448, 459-467; app/src/main/java/com/qualityalternative/app/data/AccountLightProfile.kt:306-333, 1635-1678, 1735-1751; app/src/main/java/com/qualityalternative/app/data/AgentInboxPackageImporter.kt:51-58; app/src/test/java/com/qualityalternative/app/data/AccountLightProfileExporterTest.kt:500-518.
Tightest fix: Omit raw sha256 for Agent Inbox/private documents or replace it with a profile-local, non-portable, non-reversible keyed value; keep reattachment verification local after the user reselects a document. Update exporter/importer tests so raw SHA values are denied in portable JSON for private user documents.

MEDIUM — Duplicate and SHA-mismatch visibility at review time depends on optional manifest SHA and does not verify actual content.
Exact claim: Scan downloads only manifest.json; AgentInboxReviewCandidateFactory marks duplicates only when manifest.documentSha256 is present and matches an existing document id, and it does not receive actualContentSha256 from the scan path. The importer prevents duplicate import later, but the review candidate can still appear READY until import is attempted.
Why it is vulnerable: The PRD and sprint plan require invalid and duplicated packages to be visible as finite review items. A duplicate package without documentSha256, or a package whose declared SHA does not match actual content, is not visibly flagged during review even though import later rejects or de-duplicates it. This creates misleading review UI and weaker operator decision-making, although it does not prove duplicate import occurs twice.
Files checked: PRD.md:261-267; docs/SPRINT_27_AGENT_CONTENT_INBOX.md:62-71; app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt:1265-1279; app/src/main/java/com/qualityalternative/app/data/AgentInboxReviewCandidate.kt:50-53, 71-80; app/src/main/java/com/qualityalternative/app/data/AgentInboxPackageImporter.kt:43-58; app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt:2599-2601; app/src/test/java/com/qualityalternative/app/data/AgentInboxReviewCandidateFactoryTest.kt:78-95; app/src/test/java/com/qualityalternative/app/data/AgentInboxPackageImporterTest.kt:50-86.
Tightest fix: During bounded scan/review, download the named content file up to a defensive size cap, compute SHA-256, pass actualContentSha256 into candidate validation, and mark duplicates or SHA mismatches before the user reaches the import button.

MEDIUM — Canonical visual evidence does not cover the sprint’s stated visual acceptance set.
Exact claim: The canonical screenshot run contains four review-state screenshots: ready light, priority toggled light, invalid/duplicate light, and dark review. The sprint plan requires additional visual E2E evidence for disconnected Agent Inbox, connected empty, successful import confirmation, library row, intervention recommendation, Markdown reader, and EPUB review/reader smoke where practical.
Why it is vulnerable: The shipped screenshots prove the review cards are legible in light/dark states, but they do not prove the end-to-end import, library, recommendation, reader, or EPUB visual paths. This creates a release-gate evidence mismatch between sprint acceptance text and validation prose.
Files checked: docs/SPRINT_27_AGENT_CONTENT_INBOX.md:138-155; evidence/sprint27_agent_content_inbox/VALIDATION_SUMMARY.md:23-34; app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt:706-747, 2065-2134; canonical screenshots under evidence/sprint27_agent_content_inbox/visual_e2e/sprint27-agent-content-inbox-1781250344829/; contact sheet evidence/sprint27_agent_content_inbox/visual_e2e/sprint27_agent_inbox_contact_sheet.png; canonical connected XML evidence/sprint27_agent_content_inbox/android-results/connected_debug/TEST-qaApi36(AVD) - 16-_app-.xml.
Tightest fix: Add deterministic screenshots for the missing states or revise the sprint acceptance document and validation summary to explicitly narrow the visual gate. The canonical run should include the complete agreed set and a regenerated contact sheet.

TRACE CHECKS:

Required planning and release documents: PRD.md; docs/SPRINT_27_AGENT_CONTENT_INBOX.md; evidence/sprint27_agent_content_inbox/REVIEW_BUNDLE_MANIFEST.md; evidence/sprint27_agent_content_inbox/VALIDATION_SUMMARY.md.

Implementation paths audited: app/src/main/java/com/qualityalternative/app/data/AgentInboxManifest.kt; AndroidGoogleDriveAgentInboxClient.kt; AgentInboxReviewCandidate.kt; AgentInboxPackageImporter.kt; AgentInboxDocumentStore.kt; PreferencesSettingsRepository.kt; AccountLightProfile.kt; AppContainer.kt; app/src/main/java/com/qualityalternative/app/domain/model/AnalyticsPrivacyGuard.kt; InterventionModels.kt; UserModels.kt; app/src/main/java/com/qualityalternative/app/domain/service/AgentInboxDrive.kt; Contracts.kt; app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt; QualityAlternativeApp.kt.

Unit and Android tests audited: AgentInboxManifestValidatorTest.kt; AgentInboxReviewCandidateFactoryTest.kt; AgentInboxPackageImporterTest.kt; AndroidGoogleDriveAgentInboxClientTest.kt; MainViewModelTest.kt; AccountLightProfileExporterTest.kt; PreferencesSettingsRepositoryTest.kt; VisualQaScreenshotTest.kt.

Canonical screenshots audited: 01_agent_inbox_review_ready_light.png; 02_agent_inbox_priority_toggled_light.png; 03_agent_inbox_invalid_duplicate_light.png; 04_agent_inbox_review_dark.png; sprint27_agent_inbox_contact_sheet.png.

Canonical connected test result audited: evidence/sprint27_agent_content_inbox/android-results/connected_debug/TEST-qaApi36(AVD) - 16-_app-.xml, which reports tests="1", failures="0", errors="0", and testcase captureSprint27AgentInboxReviewScreens.

Connected run support logs audited: test-result-exit-code.txt returned 0; qaApi36(AVD) - 16/test-result.textproto reports test_status: PASSED; qaApi36(AVD) - 16/testlog/test-results.log reports OK (1 test); logcat-com.qualityalternative.app.VisualQaScreenshotTest-captureSprint27AgentInboxReviewScreens.txt shows the canonical test start and finish.

Positive checks verified: unsafe relative content paths are rejected in manifest validation; import-time duplicate prevention happens before document write; raw Drive folder ids, file ids, file names, scan errors, and document text are not present in the checked Agent Inbox remote analytics metadata paths; Agent Inbox Drive folder id and scan error are omitted from the checked Portable Profile export test; the visual fixture hook is guarded by BuildConfig.DEBUG in MainViewModel.kt:4733-4751.

BUNDLE GAPS:

BUNDLE GAP — Exact Google Drive OAuth scope cannot be proven from shipped files. QualityAlternativeApp.kt:412-466 requests ANNOTATION_DRIVE_SCOPE, and the sprint/manifest prose claims drive.file-style scoped access, but the definition of ANNOTATION_DRIVE_SCOPE is not included in the shipped bundle. Therefore the narrowest-practical Drive access claim cannot be fully proven from shipped source.

BUNDLE GAP — Live Google Drive behavior is not proven. The shipped code and tests prove the constructed Drive queries and local state transitions only; they do not prove live Google Drive authorization behavior, scope enforcement, or Drive search results outside the test server.

BUNDLE GAP — Final sprint release metadata is incomplete if the sprint plan release gate is applied literally. The sprint plan requires an internal alpha APK path and commit SHA in the final summary, but the shipped evidence bundle contains neither an APK path nor a commit SHA record.

PACKAGE HYGIENE:

The bundle is not clean enough for a final release gate. The evidence tree does not contain stale screenshot run directories beyond the single canonical run identified in the manifest, and the connected test XML/logs are present and internally consistent. However, the canonical visual evidence is incomplete relative to the sprint plan, the exact Drive scope is not auditable from the shipped source, and the sprint-plan final release metadata for APK path and commit SHA is absent. The included VisualQaScreenshotTest.kt also carries legacy screenshot harness fields and methods from earlier sprints, which is source-level noise; it did not contaminate the canonical connected result because the XML proves only captureSprint27AgentInboxReviewScreens was executed.