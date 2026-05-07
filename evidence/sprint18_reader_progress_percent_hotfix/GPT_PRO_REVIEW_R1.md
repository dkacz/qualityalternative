SCORE: 9/10

VERDICT: PASS

VISUAL REVIEW: PASS

FINDINGS:

No blockers.

Non-blocking package hygiene: 02_after_end_later_clicks_light.png and 03_compact_controls_later_page_light.png are byte-identical, although they are described as separate visual checkpoints. This does not invalidate the tested behavior, but it slightly weakens the screenshot set as independent visual evidence.

Non-blocking documentation noise: docs/SPRINT_18_GDRIVE_E2E_ACCOUNT_UX.md still says a “small live range summary” was added to the annotation popup, which is stale relative to this hotfix because the current source removes that summary from the popup and accessibility semantics.

READER PROGRESS:

The shipped implementation is source anchored for this hotfix. ReaderScreen derives progress from readerBlockLayout.sourcePositionForDisplayBlock(currentPage.endInclusive) and readerProgressPercentForSourcePosition(...), using the source block index and source text offset rather than pageIndex/pageCount.

Forward navigation persists progressPercent, sourceBlockIndex, and textOffset through onProgressChanged(...), so the saved progress anchor is tied to lastVisibleParagraphIndex and lastVisibleTextOffset.

Restore uses displayBlockIndexForSourcePosition(...) to map the saved source anchor back into the current paginated display layout, which is the correct mechanism when pages are rebuilt under a different reader font scale.

The footer behavior is consistent with the requirement: screenshots show default text at 2/4 · 49% and large text at 4/8 · 49%. The page number and page count changed after repagination, while the displayed saved percent remained stable. That page-number change is acceptable because the saved/read percent is no longer page-count anchored.

ANNOTATION POPUP:

The technical Selection block ... steps ... copy is gone from the current user-facing popup source: the MonoText(selection.rangeSummaryText()) row was removed, rangeSummaryText() and rangePositionText() were removed from ReaderAnnotationSelection, and the range-control accessibility description is now just Annotation range controls.

The shipped current source does not contain the removed technical text outside diffs/docs. The only remaining matches are in patch deletion hunks, validation/manifests describing the fix, and stale documentation, not in the active UI implementation.

The screenshots show the popup header, compact arrow controls, quote region, note field, and actions without the prior technical summary copy.

Compact cross-page controls remain usable based on source, tests, and screenshots: the connected test exercises expansion across pages, verifies the controls remain displayed, checks compact sizing relative to the expanded editor, verifies clickable dimensions in the test environment, saves the cross-page annotation, and reopens it with the source-anchored quote preserved.

E2E / TESTS:

Unit coverage is adequate for the core algorithmic change. ProgressSnapshotTest.kt adds readerProgressPercentUsesSourcePositionInsteadOfRepaginatedPageCount, which directly contrasts source-position progress with page-count progress, and the existing source-position/resplitting tests cover mapping saved offsets into a repaginated layout.

Connected coverage is adequate for the regression path. readerProgressPercentRestoresFromSourceAnchorAfterReaderFontChange creates a markdown document, advances the reader, records saved progress, changes reader font scale to 130%, reopens the item, and asserts the saved percent remains displayed and persisted.

Connected annotation coverage is adequate for the scoped popup/control behavior. readerAnnotationControlsExpandAndReopenAcrossPages verifies compact controls, cross-page expansion, long quote containment, save, and reopen behavior.

Runtime reports are consistent with the validation summary: connected_hotfix.xml reports 2 tests, 0 failures, 0 errors, 0 skipped on qaApi36(AVD) - 16; the textproto marks the same two tests as passed; logcat contains the corresponding test start/finish and screenshot capture events.

The unit-test runtime evidence is less strong than the connected evidence because unit_debug.log shows :app:testDebugUnitTest UP-TO-DATE rather than a per-test XML report, but the source-level unit tests are present and the supplied Gradle task result is successful.

RELEASE READINESS:

This hotfix is safe to release as a new APK based on the shipped source, unit-test source, connected-test evidence, runtime reports, logs, and screenshots. The scoped product behavior is implemented and validated, with no release-blocking mismatch found.

BUNDLE GAPS:

BUNDLE GAP: VALIDATION.md states that the emulator was shut down after screenshots and connected validation, but the shipped evidence does not independently prove emulator shutdown. This is not material to the hotfix behavior.

No hotfix-critical evidence is missing for the reader progress or annotation popup claims.

PACKAGE HYGIENE:

The bundle is mostly clean and scoped: it includes the expected source, unit test source, connected test source, hotfix diff, validation summary, connected XML/textproto reports, logcat logs, and screenshots.

Stale/noisy artifacts present: the Sprint 18 Google Drive doc still describes adding a live range summary, which conflicts with the current hotfix removal of that summary; two annotation screenshots are exact duplicates despite distinct filenames and descriptions.

No old APKs, prior review bundles, large build directories, or unrelated release artifacts are present in the shipped bundle.