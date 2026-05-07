SCORE: 10/10

VERDICT: PASS

VISUAL REVIEW: PASS

FINDINGS:

No blockers.

R2 RECHECK:

Full progress persistence source is now included for the requested path: MainViewModel.kt, ReadingProgressModels.kt, Contracts.kt with ReadingProgressRepository, RoomReadingProgressRepository.kt, ReadingProgressDao.kt, ReadingProgressEntity.kt, QualityAlternativeDatabase.kt with the lastVisibleTextOffset migration, and AccountLightProfile.kt.

Per-test unit XML is now included under evidence/sprint18_reader_progress_percent_hotfix/test_reports/unit/; the reports cover ProgressSnapshotTest, MainViewModelTest, AccountLightProfileExporterTest, and AccountLightProfileImporterTest, all with zero failures, zero errors, and zero skipped tests.

Package hygiene remains clean. The screenshot set contains six distinct PNGs, the prior duplicate screenshot artifact is absent, and emulator shutdown evidence is present.

READER PROGRESS:

The percent is source anchored and stable across font repagination based on shipped source, tests, and screenshots. ReaderScreen derives progress through readerBlockLayout.sourcePositionForDisplayBlock(currentPage.endInclusive) and readerProgressPercentForSourcePosition(...), not through page index/page count. On forward navigation, it saves the source block and offset through lastVisibleParagraphIndex and lastVisibleTextOffset.

Restored progress is anchored by lastVisibleParagraphIndex and lastVisibleTextOffset: displayBlockIndexForSourcePosition(...) remaps the saved source position into the current display layout after splitting or repagination.

The footer behavior matches the requirement. The default-font screenshot shows 2/4 · 49%; the large-font reopened screenshot shows 4/8 · 49%. The page number and page count change after font repagination, while the saved/read percent remains stable until navigation.

Persistence/export/import evidence is sufficient. Room entity, DAO, repository, database migration, domain model, ViewModel save path, and Account Light export/import mapping all preserve lastVisibleTextOffset. Account Light unit tests verify offset export/import behavior, and repository/migration instrumented-test source is present for the persistence path.

ANNOTATION POPUP:

Technical selection copy is absent from active UI and accessibility semantics. Active source under app/src contains no Selection block, steps, rangeSummaryText, rangePositionText, or reader-annotation-range-summary UI path. The range-control semantics are reduced to Annotation range controls, and individual controls expose action descriptions such as Move start earlier, Move start later, Move end earlier, and Move end later.

Compact cross-page controls remain usable. Source keeps four compact arrow controls in the popup header, the connected test repeatedly expands a cross-page range, verifies the controls remain displayed, saves the note, and reopens the preserved cross-page quote. The screenshots show compact controls, a scrollable long-quote region, and reopened quote state without technical range-summary copy.

E2E / TESTS:

Unit-test evidence is sufficient for the algorithmic and persistence-adjacent changes: 173 unit tests are reported across the four included XML files with zero failures/errors/skips.

The key source-anchoring unit tests are present and passing in XML, including readerProgressPercentUsesSourcePositionInsteadOfRepaginatedPageCount and readerProgressSourceIndexSurvivesAdaptiveResplitting.

Export/import unit coverage is present and passing, including exportSettingsOnlyProfileJson_includesPortableLibraryAndReadingStateWithoutRawUris and validateImportProfileJson_acceptsReadingProgressTextOffsetWithoutUnknownWarning.

Connected-test evidence is sufficient for the scoped runtime regressions. connected_hotfix.xml reports two tests, zero failures, zero errors, and zero skipped on qaApi36(AVD) - 16: readerProgressPercentRestoresFromSourceAnchorAfterReaderFontChange and readerAnnotationControlsExpandAndReopenAcrossPages.

Logcat files match the connected report and record screenshot capture for the progress hotfix and annotation-control scenarios.

RELEASE READINESS:

This hotfix is safe to release as a new APK based on the shipped source, patch, unit XML, connected-test XML, logs, and visual evidence. This is a source/evidence review rather than an APK binary audit; no APK artifact is included in the bundle.

BUNDLE GAPS:

None for the requested hotfix scope.

PACKAGE HYGIENE:

The bundle is clean and scoped. It includes the R3 manifest, source needed for the progress persistence path, hotfix patch/diff, relevant unit and connected test source, per-test unit XML, connected-test XML/textproto, logcat evidence, shutdown evidence, and six distinct screenshots.

Prior R1/R2 review files are clearly labeled as historical review evidence and are not misleading as active validation.

The root HOTFIX_FULL_DIFF_R3.patch and evidence reader_progress_percent_hotfix.diff are duplicate copies of the same hotfix diff, but this is redundant rather than stale or misleading.

No stale screenshot duplicates, old APKs, release artifacts, or unrelated build-output directories are present.