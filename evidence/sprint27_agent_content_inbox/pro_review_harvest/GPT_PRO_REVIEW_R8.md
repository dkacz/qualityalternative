SCORE: 9/10

VERDICT: REVISE

VISUAL REVIEW: PASS

FRESH FINDINGS:

Severity: Medium — local Agent Inbox document writes are not transactional on write failure, so a failed private-document save can leave a retained partial file and block later import of the same verified content path.

Exact claim: FileAgentInboxDocumentStore.writeDocument writes directly to the final deterministic Agent Inbox file path and has no temporary-file, atomic-rename, or failure cleanup guard around the write itself.

Why it is vulnerable: AgentInboxDocumentStore.kt creates the final file path from packageFolderId and verified content SHA, checks an existing file’s SHA, and then calls file.writeBytes(bytes) directly at the final path. If the local write fails after file creation, for example under low storage or interrupted I/O, writeDocument throws before returning a StoredAgentInboxDocument. The cleanup added in AgentInboxPackageImporter.kt only runs after stored exists and the repository operation returns DUPLICATE, returns REJECTED, or throws; it cannot clean a partial file left by writeDocument before return. A later import of the same package/content path will see the stale file, compute its SHA, fail the existing-file check, and still have no cleanup path. This is a private-content retention and denial-of-import failure path, not an analytics or Portable Profile leak.

Files checked: app/src/main/java/com/qualityalternative/app/data/AgentInboxDocumentStore.kt:30-58, especially 45-52; app/src/main/java/com/qualityalternative/app/data/AgentInboxPackageImporter.kt:85-116 and 125-135; app/src/test/java/com/qualityalternative/app/data/AgentInboxPackageImporterTest.kt:150-179, 283-320; app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt:3053-3093; docs/SPRINT_27_AGENT_CONTENT_INBOX.md:248-254.

Tightest fix: write to a scoped temporary file under the Agent Inbox storage root, verify the temporary file SHA after a complete write, then atomically rename or replace into the final path; delete the temporary file in finally, and delete any newly created final file on write failure. Add a store-level or importer-level test that simulates a local write failure after file creation and proves that no file remains under the Agent Inbox storage root and a later successful import is not blocked by a stale mismatching file.

TRACE CHECKS:

PRD alignment:

PRD.md:105-110 — Agent Inbox scoped to explicit Drive packages.

PRD.md:121 — silent cloud ingestion and silent prioritization out of scope.

PRD.md:246-268 — explicit review, package contract, private content, finite invalid/duplicate/unavailable states, operator priority acceptance.

PRD.md:276-287 — user-private provenance and no raw Drive identifiers in remote analytics/profile.

PRD.md:303 — user-confirmed priority only.

PRD.md:443-448 — Agent Inbox analytics restrictions.

PRD.md:459-468 — Portable Profile exclusions for tokens, grants, raw Drive IDs, paths, account emails, and document binaries.

PRD.md:493-498 — explicit, revocable, scoped cloud review.

Sprint documentation and bundle manifest:

docs/SPRINT_27_AGENT_CONTENT_INBOX.md:7-16, 30-71, 97-156 — product constraints, package contract, slice scope.

docs/SPRINT_27_AGENT_CONTENT_INBOX.md:157-260 — GPT Pro R1/R2/R3/R4/R5/R6/R7/R8 fix notes.

evidence/sprint27_agent_content_inbox/REVIEW_BUNDLE_MANIFEST.md:5-55 — scoped evidence base, canonical screenshot run, connected XML/logcat requirements, and excluded artifacts.

evidence/sprint27_agent_content_inbox/VALIDATION_SUMMARY.md:9-22, 24-65, 69-82 — automated check summary, coverage claims, canonical visual list, and R8 notes.

Implementation paths:

app/src/main/java/com/qualityalternative/app/data/AgentInboxManifest.kt:60-145, 147-150, 211-219 — manifest validation, SHA comparison, and unsafe path rejection.

app/src/main/java/com/qualityalternative/app/domain/service/AgentInboxDrive.kt — package/file/download limits.

app/src/main/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClient.kt:27-63, 65-95, 97-136, 138-155, 250-264 — explicit folder binding, bounded metadata/body downloads, package/file caps, and no unqualified Drive name search.

app/src/main/java/com/qualityalternative/app/data/AgentInboxReviewCandidate.kt:47-125, 165-167 — finite review states, actual-content-SHA duplicate basis, and review display construction.

app/src/main/java/com/qualityalternative/app/data/AgentInboxPackageImporter.kt:32-143 — reviewed byte recheck, import-time duplicate lookup, atomic repository add, post-write duplicate/rejected/throw cleanup, and neutral stored display name.

app/src/main/java/com/qualityalternative/app/data/AgentInboxDocumentStore.kt:30-69 — scoped file naming and guarded deletion; fresh finding applies to direct final-path write at 45-52.

app/src/main/java/com/qualityalternative/app/data/RoomUserDocumentRepository.kt — repository write mutex and atomic fingerprint add path.

app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt:1255-1420, 1462-1504, 1507-1589, 1592-1767, 5224-5230 — scan readiness gate, bounded download failure handling, priority acceptance, rejection, import single-flight guard, failed-row invalidation, duplicate row updates, and privacy-safe Agent Inbox analytics metadata.

app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt:382-470, 5271-5574, 9569-9675, 9707-9733 — Drive authorization flow, finite review UI, visible Remove/Import/priority controls, invalid cleanup copy, and local auth error copy.

app/src/main/java/com/qualityalternative/app/data/AccountLightProfile.kt:1571-1610, 1635-1667, 1923-2058 — Portable Profile excludes Agent Inbox connection state and exports user documents only as missing/unverified safe metadata.

app/src/main/java/com/qualityalternative/app/domain/model/AnalyticsPrivacyGuard.kt:18-34, 126-143, 168-232, 256-268 — remote analytics allowlist and sensitive key/value filtering.

app/src/main/AndroidManifest.xml, app/src/debug/AndroidManifest.xml, evidence/sprint27_agent_content_inbox/manifests/release-merged-main-AndroidManifest.xml, evidence/sprint27_agent_content_inbox/manifests/release-packaged-AndroidManifest.xml — fixture/debug components absent from release manifests.

Unit and Android test evidence:

evidence/sprint27_agent_content_inbox/unit-results/testDebugUnitTest/TEST-*.xml — 42 XML suites, 509 test cases, 0 failures, 0 errors.

app/src/test/java/com/qualityalternative/app/data/AgentInboxManifestValidatorTest.kt — manifest schema, topic, rights, priority, file-path, format, and SHA validation.

app/src/test/java/com/qualityalternative/app/data/AgentInboxReviewCandidateFactoryTest.kt — duplicate status from actual content SHA, not manifest-declared SHA; invalid/truncated package cases.

app/src/test/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClientTest.kt — bounded streaming, folder binding, pagination caps, no unqualified name search.

app/src/test/java/com/qualityalternative/app/data/AgentInboxPackageImporterTest.kt:67-179, 182-256, 283-320 — changed bytes, duplicate prevention, concurrent same-SHA packages, oversize/SHA mismatch, repository rejection, and post-write exception cleanup.

app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt:2584-3455 — scan/import review behavior, priority confirmation, same-scan siblings, import-time invalidation, repository rejection state, duplicate review, bounded download failures, repository readiness, truncation, and disconnect behavior.

app/src/test/java/com/qualityalternative/app/data/AccountLightProfileExporterTest.kt, AccountLightProfileImporterTest.kt, AnalyticsPrivacyGuardTest.kt — Portable Profile and analytics privacy coverage.

app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt:706-819, 2137-2185 — canonical visual capture assertions and seeded Agent Inbox review candidates.

Visual and connected evidence:

Canonical screenshot run: evidence/sprint27_agent_content_inbox/visual_e2e/sprint27-agent-content-inbox-1781267009056/.

02_agent_inbox_review_ready_light.png — visibly shows ready package, Accept priority, Remove, and Import.

03_agent_inbox_priority_accepted_light.png — visibly shows Priority accepted after operator confirmation.

04_agent_inbox_invalid_duplicate_light.png — duplicate and invalid rows retain visible Remove actions and do not show importable ready controls.

04b_agent_inbox_rejected_light.png — rejected row is removed from review.

05_agent_inbox_review_dark.png — dark-mode review state remains bounded and legible.

06_agent_inbox_library_imported_markdown_light.png, 07_agent_inbox_intervention_imported_markdown_light.png, 08_agent_inbox_reader_markdown_light.png, 09_agent_inbox_reader_epub_light.png — imported private Markdown/EPUB flows are represented.

evidence/sprint27_agent_content_inbox/android-results/connected_debug/TEST-qaApi36(AVD) - 16-_app-.xml — canonical connected test XML, 1 test, 0 failures, 0 errors.

evidence/sprint27_agent_content_inbox/android-results/connected_debug/logcat-VisualQaScreenshotTest-captureSprint27AgentInboxReviewScreens.txt — standalone logcat matching the visual screenshot run and screenshot names.

evidence/sprint27_agent_content_inbox/android-results/connected_debug/test-result-exit-code.txt — exit code 0.

BUNDLE GAPS:

None identified for PRD alignment, analytics/Portable Profile privacy, visual evidence, connected XML/logcat presence, or canonical screenshot-run selection. The fresh finding is implementation/test coverage, not an absence of shipped evidence.

PACKAGE HYGIENE:

The bundle is clean enough for review-gate auditing, with one implementation revision required before pass. The visual evidence directory contains only the canonical run sprint27-agent-content-inbox-1781267009056; the connected evidence includes both the canonical XML and a standalone logcat for the same visual test; the APK SHA file matches the shipped debug APK; release manifests do not contain the debug visual fixture targets; and no stale screenshot run was present under evidence/sprint27_agent_content_inbox/visual_e2e/.