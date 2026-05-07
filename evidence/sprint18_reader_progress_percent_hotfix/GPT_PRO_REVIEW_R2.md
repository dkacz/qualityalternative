SCORE: 9/10

VERDICT: PASS

VISUAL REVIEW: PASS

FINDINGS:

No blockers.

Non-blocking evidence limitation: the shipped source proves the Reader UI anchoring path, and connected tests prove the scoped runtime behavior, but the bundle does not include the full MainViewModel, ReadingProgress, repository, or storage source needed to inspect the persistence layer below the UI callback.

Non-blocking test-evidence limitation: unit_debug.log shows :app:testDebugUnitTest UP-TO-DATE and BUILD SUCCESSFUL, but no per-test unit XML/report is included.

R1 RECHECK:

Stale live-range-summary documentation is fixed. docs/SPRINT_18_GDRIVE_E2E_ACCOUNT_UX.md now says the annotation popup removed the user-facing technical range summary rather than adding a small live range summary.

Duplicate screenshot evidence is fixed. The current screenshot set contains six PNG files, and their hashes are distinct; the prior duplicate 02_after_end_later_clicks_light.png capture is no longer present.

Emulator shutdown evidence is now present. logs/adb_devices_after_emulator_shutdown.txt contains List of devices attached with no attached devices listed.

READER PROGRESS:

The percent is source anchored in the shipped Reader UI source. ReaderScreen derives progress through readerBlockLayout.sourcePositionForDisplayBlock(currentPage.endInclusive) and readerProgressPercentForSourcePosition(...), not from pageIndex/pageCount.

Saved/restored progress remains anchored by lastVisibleParagraphIndex / lastVisibleTextOffset at the UI layer. Restore maps restoredProgress.lastVisibleParagraphIndex and restoredProgress.lastVisibleTextOffset through displayBlockIndexForSourcePosition(...); forward navigation saves sourcePosition.sourceBlockIndex and sourcePosition.textOffset through onProgressChanged(...).

The footer behavior matches the requirement. The default-font screenshot shows 2/4 · 49%; after reopening at 130% Reader text size, the screenshot shows 4/8 · 49%. The page number and page count change after repagination, while the saved/read percent remains stable until manual navigation.

ANNOTATION POPUP:

Technical selection copy is absent from active UI and accessibility semantics. The active source no longer contains Selection block, steps, rangeSummaryText, rangePositionText, or reader-annotation-range-summary; the range-control semantics are reduced to Annotation range controls, and individual controls expose action labels such as Move start earlier.

The compact cross-page controls remain usable. The source keeps four compact arrow controls in the popup header, the connected test repeatedly expands the selection across pages, verifies the controls remain displayed, checks hit-area dimensions in the test environment, saves the cross-page annotation, and reopens the preserved quote. Screenshots show the compact controls, long quote region, and reopened note without technical summary copy.

E2E / TESTS:

Unit-test source is sufficient for the core algorithmic change. readerProgressPercentUsesSourcePositionInsteadOfRepaginatedPageCount directly distinguishes source-position progress from page-count progress, and the existing source-position/resplitting test covers offset-based remapping after layout changes.

Connected-test coverage is sufficient for the scoped regression. connected_hotfix.xml reports 2 tests, 0 failures, 0 errors, and 0 skipped on qaApi36(AVD) - 16: readerProgressPercentRestoresFromSourceAnchorAfterReaderFontChange and readerAnnotationControlsExpandAndReopenAcrossPages.

Logcat evidence matches the connected report and records screenshot capture for the progress hotfix and annotation-control scenarios.

RELEASE READINESS:

This hotfix is safe to release as a new APK based on the shipped UI source, patch, tests, connected-test reports, logs, and screenshots. The release decision is source/test-evidence based; no APK artifact is included in the bundle.

BUNDLE GAPS:

BUNDLE GAP: Full MainViewModel, ReadingProgress, repository, and persistence-layer source are not included, so the implementation below ReaderScreen’s onProgressChanged(...) callback cannot be independently source-inspected from this bundle.

BUNDLE GAP: No per-test unit XML/report is included; the shipped unit evidence is a successful Gradle log where :app:testDebugUnitTest was UP-TO-DATE.

PACKAGE HYGIENE:

The bundle is clean and scoped for the R2 hotfix. It includes the hotfix patch, current Reader UI source, relevant unit and connected test source, validation summary, connected XML/textproto reports, logcat logs, shutdown evidence, and six distinct screenshots.

No stale live-range-summary documentation remains in the active Sprint 18 doc.

No exact duplicate screenshot artifacts were found.

The historical GPT_PRO_REVIEW_R1.md still contains R1’s old hygiene notes, but it is clearly labeled as prior-review evidence and is not misleading as an active implementation or validation artifact.