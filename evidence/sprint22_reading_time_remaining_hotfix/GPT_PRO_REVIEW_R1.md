SCORE: 8/10

VERDICT: PASS

VISUAL REVIEW: PASS

BLOCKERS

None proven from the shipped bundle.

READING TIME REMAINING

The hotfix addresses the reported 41% read · 12 min left stale-duration regression for the targeted case: an unfinished private-reader user document whose persisted durationMinutes still equals the old session cap. The new repository path persists a repaired durationMinutes, updates the in-memory document stream, and the Home card evidence shows the same document changing from 41% read · 12 min left to 41% read · 1 hr 20 min left.

The candidate predicate is appropriately narrow for the main regression: USER_DOCUMENT, repository-backed/private reader content, and durationMinutes <= ReadingTimeEstimator.MAX_SESSION_MINUTES. The repair also requires unfinished progress for the background path, so completed documents and documents without reading progress are not repaired from startup observation.

The reader-open path is also correct for the core bug: after loading the reader document, it estimates from the loaded text, persists the repaired duration, and uses the repaired ContentItem for currentContent, screenForReplacement, and manual-continue analytics metadata. Because the body is already loaded for opening, this path does not add a second full-document parse.

Non-blocking caveat: the background scheduler limits each scheduling pass to one candidate, but a successful repair updates the document stream and can trigger another scheduling pass, so a library with many unfinished legacy private-reader documents may be repaired sequentially during startup. This is still limited to relevant stale unfinished private-reader documents, not the whole library, but it is not a strict “only one document per app start” cap.

FLOW / RACE CHECK

The combined document/progress stream is a sound improvement over independent observation because the repair decision is made against a document snapshot paired with the latest available progress snapshot. Sorting by unfinished-progress recency also targets the most likely Home “Continue Reading” item first.

The durationRepairAttemptedContentIds guard prevents repeated background repair jobs for the same document in a single ViewModel lifetime. This reduces duplicate parsing risk.

Non-blocking race risk: background repair and reader-open repair can race. If the user opens a stale document while a background repair is already in flight, both paths can estimate from the old ContentItem. The repository may avoid a second database write when the safe duration already matches, but applyRecoveredReadingTimeEstimate can still record a second READING_TIME_ESTIMATE_APPLIED event and run autosave because it compares against the stale input content.durationMinutes, not the current persisted duration. This is analytics/autosave noise rather than a correctness blocker.

Non-blocking scheduling edge case: if the most recent candidate fails parsing, or estimates to <= the existing duration, it remains the first sorted candidate and can prevent older candidates from being scheduled in the same ViewModel lifetime because .take(1) is applied before attempted-ID filtering. This does not block the reported long-document case shown in evidence, but it is a regression-risk gap for multi-document libraries.

PERSISTENCE / AUTOSAVE / ANALYTICS

Persistence is acceptable. UserDocumentDao.updateDurationMinutes(...) provides a durable Room update, and RoomUserDocumentRepository.updateEstimatedDuration(...) clamps the new estimate, updates the DAO, and updates the repository’s optimistic state flow.

Portable-profile autosave is invoked after a non-null repaired document is returned. Because the repository state is updated before autosave is called, the repaired duration should be available to the exporter through the repository state.

Analytics behavior is acceptable for privacy. The repair event records estimate source, repair source, previous duration, and repaired duration. For private-reader user documents, the repair metadata path does not add document text, word content, display name, or URI; ContentItem.analyticsMetadata() only includes externalUrl when present, and private reader documents should not have one.

Non-blocking coverage gap: the unit tests verify the repair analytics event, but do not assert that autosave was invoked after repair.

TEST / EVIDENCE

Unit coverage is directionally sufficient for the hotfix. The added tests cover background repair of an unfinished legacy document and reader-open repair from loaded reader text. They assert repaired duration persistence in the fake repository and verify the repair analytics metadata for the background path.

Connected visual evidence is strong. The bundled screenshots show:

00_home_continue_before_repair_assertion.png: 41% read · 12 min left

01_home_continue_after_repair_wait.png: 41% read · 1 hr 20 min left

02_home_continue_repaired_remaining_time.png: 41% read · 1 hr 20 min left

The connected Android test log reports the focused screenshot test completed successfully, and the unit-test log reports testDebugUnitTest build success.

Test limitations are non-blocking: the connected test asserts that 12 min left disappears and that repaired duration exceeds 20, but it does not programmatically assert the exact 1 hr 20 min left text; that exact value is proven by the shipped screenshots rather than by the test assertion. The tests also do not cover completed documents, external/PDF documents, unavailable private-reader documents, multiple queued legacy documents, parser failure, background/open duplicate repair races, or autosave invocation.

BUNDLE GAPS

BUNDLE GAP: The bundle does not include the production AppContainer or composite content repository wiring, so the review cannot independently prove from shipped files alone that contentRepository.readerDocument(item) in production delegates to the user-document repository/body loader for private reader documents. The existing reader-open path uses the same content repository mechanism, and the connected visual test provides functional evidence.

BUNDLE GAP: The bundle does not include the Home composable or remaining-time formatter source, so the exact UI formula cannot be audited from code here. The screenshots prove the visible Home-card change for the seeded case.

BUNDLE GAP: The bundle does not include ContentItem, usesRepositoryBody, ReadingTimeEstimator, Room entity/schema files, or account-light exporter implementation. Compilation and connected-test logs reduce risk, but these internals cannot be fully audited from the shipped files.

BUNDLE GAP: No release APK build log or release artifact is included. The supplied logs prove debug unit and connected instrumentation success, not release-variant assembly.

PACKAGE HYGIENE

Package hygiene is acceptable. The bundle contains the six scoped source/test files, a focused patch, status/stat files, unit and connected-test logs, and three final screenshots. No unrelated source edits or superseded failed-run screenshots are visible in the shipped archive.

Minor non-blocking hygiene note: the prompt/status refer to evidence under evidence/sprint22_reading_time_remaining_hotfix/, while the archive places the manifest, logs, and screenshots directly under the bundle’s evidence/ directory. The contents are still focused and auditable.