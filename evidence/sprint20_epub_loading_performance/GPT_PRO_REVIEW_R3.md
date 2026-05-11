SCORE: 10/10

VERDICT: PASS

VISUAL REVIEW: PASS

BLOCKERS

None.

R1 BLOCKER RECHECK

Inline/pagebreak anchor drift: resolved. The R3 anchorsInBody() path inserts anchor markers and performs one readable-text pass, rather than repeatedly reparsing full prefixes or counting partial post-anchor fragments as new blocks. The included extractDocumentMapsLaterTocAnchorAfterInlinePagebreakWithoutDrift test covers the prior R1 failure shape: an inline pagebreak anchor inside a paragraph followed by a later TOC anchor.

Missing retained-size guard tests: resolved. extractThrowsWhenReadableEntryExceedsSafetyLimit covers the 24 MB per-entry guard, and extractThrowsWhenAggregateReadableTextExceedsSafetyLimit covers the 96 MB aggregate retained-readable-text guard.

Up-to-date-only unit evidence: resolved. R3 includes fresh unit logs with 28 actionable tasks: 28 executed, including testDebugUnitTest_r3.log and testDebugUnitTest_full_r3.log.

Missing busy-state visual evidence: resolved. The R3 screenshot set includes both 01_epub_import_preparing_light.png and 02_reader_opening_overlay_light.png.

Possible synchronous single-import path: resolved. prepareUserDocumentImport() now sets isPreparing, launches work from viewModelScope, and runs DocumentImportCandidateFactory.fromPickedDocument() inside withContext(documentWorkDispatcher).

Stale reader-open race risk: resolved for the reviewed blocker cases. Reader-open request IDs are incremented for new opens and invalidated by the normal top-level navigation methods reviewed in R2, with stale completion ignored before state replacement.

R2 BLOCKER RECHECK

Incomplete stale reader-open navigation guard: resolved. openHome(), openLibrary(), openProgress(), openSettings(), and openAnnotationLibrary() now invalidate pending reader opens and clear isReaderOpening, and the added openLibraryItemIgnoresSlowPrivateReaderLoadAfterNavigationAway test covers a slow private-reader load followed by navigation away.

Unguarded stale reader-open failure side effects: resolved for the R2 failure ordering. loadReaderDocumentForSession() now checks requestId before invoking handleRepositoryBodyLoadFailure(), and staleFailedPrivateReaderLoadDoesNotClearNewerOpen verifies that a failed older open does not log the stale body-load failure or clear the newer reader state.

Import-preparation cancellation/stale-state risk: resolved. Batch preparation is now keyed by a request ID returned from beginUserDocumentImportPreparation(), single-file preparation has its own request ID, and cancellation/navigation invalidates pending preparation. The added tests cover late batch results, late single-file results, and edits during preparation.

Form edits during preparation clearing busy state: resolved. updateAddDocumentForm() preserves isPreparing, and canSave remains false while preparation is active.

Duplicate opening snackbar under overlay: resolved. The reader-open path now sets latestMessage = null while showing the overlay, and the R3 overlay screenshot no longer shows the duplicate bottom snackbar seen in R2.

EPUB PERFORMANCE

The hotfix plausibly addresses the reported freeze class. EPUB ZIP extraction now skips non-retained entries such as images, fonts, audio, and other binary resources instead of copying them into retained ByteArray values, which materially reduces memory pressure for binary-heavy EPUBs.

The retained-entry filter still keeps every .opf, .ncx, .xhtml, .html, and .htm entry before final spine selection, so it is broader than “only final spine readable entries.” This remains an acceptable hotfix tradeoff because binary-heavy archives are the highest-risk case, text retention is bounded, and retained non-spine HTML is capped by the same aggregate guard.

The parser still traverses skipped ZIP entries through ZipInputStream, so very large binary entries can still cost background I/O/decompression time. The important freeze mitigation is that this work is no longer retained in memory and the main reviewed import/open paths move it off the main UI path.

The one-document reader cache is reasonable for immediate reopen churn. Its key includes URI, format, and verified fingerprint/size when present; legacy items without verified fingerprints still cache by URI and format only, which is a non-blocking stale-content caveat rather than a Sprint 20 hotfix blocker.

ASYNC UI / FREEZE RISK

Batch import preparation is off the UI path through withContext(Dispatchers.IO) in the document picker flow.

Single-file import preparation is off the UI path through withContext(documentWorkDispatcher) in prepareUserDocumentImport().

Repository-backed private reader opening is off the UI path through withContext(documentWorkDispatcher) in loadReaderDocumentForSession().

RoomUserDocumentRepository.addDocument() now runs validation, fingerprinting, DAO insertion, and optimistic state update inside withContext(Dispatchers.IO), reducing save-time main-thread exposure.

The reviewed MainViewModel reader-open call path no longer synchronously calls private EPUB parsing before updating state. Remaining direct uses of repository contentBody() outside the included changed files cannot be ruled out from this bundle, but no reviewed changed path regresses into synchronous private EPUB parsing.

EPUB CORRECTNESS

Spine order is preserved. OPF itemref entries are read in order, resolved through manifest idref, filtered for readable linear spine documents, normalized against the OPF base directory, and parsed in that order.

EPUB3 nav and EPUB2 NCX behavior are preserved. EPUB3 nav is preferred through properties="nav", while NCX fallback uses the spine toc attribute, NCX media type, or .ncx href detection.

Anchor mapping is now substantially safer. Marker insertion maps anchors through a single readable-text pass, and block indexes are incremented only for marker-stripped nonblank blocks, avoiding the prior inline/pagebreak drift. The regression coverage directly targets the R1 drift case.

Skipping binary resources is correct for this app’s text-only reader model. Images, CSS, fonts, and other non-reading resources are not required for the reviewed plain-text/structured-reader extraction behavior.

Bounds are present and tested: 24 MB per retained entry and 96 MB total retained EPUB text. These guards prevent pathological retained-text memory use but do not constitute full decompression-bomb or profiling evidence.

Fallback behavior remains acceptable: unresolved or missing TOC falls back to headings, then to a “Start” entry when readable blocks exist.

TEST/EVIDENCE

Unit evidence is strong enough for this hotfix slice. R3 targeted and full unit logs both show successful fresh execution with 28 actionable tasks: 28 executed.

Connected visual evidence is adequate for Sprint 20. The structured EPUB reader screenshots cover light, mid-scroll light, and mid-scroll dark states; the busy-state screenshots cover import preparation and reader-opening overlay states.

The import-preparation screenshot clearly shows “Preparing selected files...” and “Preparing the book. Large EPUBs can take a moment,” with disabled file/save controls.

The reader-opening screenshot shows a full-screen lightweight overlay with spinner and “Opening reader...” text, and no duplicate snackbar competing with the overlay.

BUNDLE GAPS

BUNDLE GAP: no ANR, profiler, heap, timing, or large real-world EPUB benchmark evidence is included; the performance conclusion is code-plausibility plus focused fixture evidence.

BUNDLE GAP: full production call-site coverage is not included, so this review cannot rule out unrelated older code paths that might still call synchronous contentBody() for private documents outside the changed-file packet.

BUNDLE GAP: no explicit unit test covers the one-document reader cache behavior or stale-cache invalidation; cache review is code-based.

BUNDLE GAP: connected visual evidence is focused rather than broad; no full connected-test suite result is included.

PACKAGE HYGIENE

The R3 packet is focused and contains the manifest, sprint note, validation note, current diff, changed production files, changed test files, unit logs, connected logs, and screenshots.

The old up-to-date-only unit log and R2 logs remain in the packet, but they are clearly superseded by R3 fresh-execution logs and are useful for review history rather than harmful.

The stale R2 busy-state screenshot set appears to have been removed; R3 keeps the post-fix Sprint 20 busy-state screenshot directory.

GPT_PRO_PROMPT.md, GPT_PRO_REVIEW.md, and GPT_PRO_REVIEW_R2.md are ancillary, but they are useful for blocker recheck traceability.

No APKs, broad generated build outputs, unrelated screenshot sets, or obvious noisy repository artifacts are included.

RELEASE READINESS

Ready to commit as a focused Sprint 20 EPUB loading-performance hotfix slice. The R1 and R2 blockers are resolved in the reviewed implementation and supported by fresh unit evidence plus focused connected visual evidence.