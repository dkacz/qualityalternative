SCORE: 7/10

VERDICT: FAIL

VISUAL REVIEW: FAIL

BLOCKERS

The new incremental anchor mapper can mis-map later TOC anchors when an earlier non-TOC anchor appears inside a paragraph or other block. anchorsInBody() advances parsedUntil to the end of every anchor tag and then counts readable blocks in the fragment between anchors. For inline/pagebreak-style anchors, the remaining text in the same paragraph can be counted as a new block, causing subsequent chapter or section anchors to drift forward. This is a correctness regression risk for anchor-heavy EPUBs, especially EPUBs with many pagebreak spans or inline id/name anchors. The added 80-anchor test covers section-start anchors only and does not cover this common inline-anchor case.

The patch is not ready as a hotfix slice until anchor mapping is corrected or constrained to TOC-referenced anchors, and a regression test is added for inline/pagebreak anchors before a later TOC target.

EPUB PERFORMANCE

The extraction change is directionally correct for binary-heavy EPUBs: non-HTML binary ZIP entries are no longer copied into retained ByteArrays, and retained text-like entries are bounded at 24 MB per entry and 96 MB total.

The implementation still uses ZipInputStream, so skipped entries are still traversed and drained sequentially; this reduces memory pressure but does not eliminate all CPU/I/O cost for very large archives. Because import preparation and reader opening now run off the UI path, this is likely acceptable for freeze prevention.

The parser still retains every .xhtml, .html, .htm, .opf, and .ncx entry by extension before it knows the spine. This is materially better than retaining every ZIP entry, but it is not exactly “only package/TOC/readable HTML entries.” Large non-spine HTML resources can still be retained until the aggregate guard fires.

The single-document reader cache is useful for immediate open/reparse churn. The cache key is URI + format + verified fingerprint where present; documents without a verified fingerprint can be cached by URI and format only, which is acceptable for a small hotfix but has stale-content risk if older documents lack fingerprints.

ASYNC UI / FREEZE RISK

Production multi-document picker preparation is moved into withContext(Dispatchers.IO), so metadata lookup, stream opening, and reading-time extraction are no longer on the Compose callback’s main path.

Reader opening for repository-backed private documents is moved behind withContext(documentWorkDispatcher), defaulting to Dispatchers.IO, which addresses the primary UI freeze path for EPUB parsing.

RoomUserDocumentRepository.addDocument() now runs in Dispatchers.IO, which keeps fingerprint calculation and DAO work off the caller’s main path.

A remaining synchronous path exists in MainViewModel.prepareUserDocumentImport(), which still calls DocumentImportCandidateFactory.fromPickedDocument() directly. The included production UI appears to use the new batch path, but the method remains capable of doing heavy EPUB candidate work on the caller thread if another production call site exists.

isReaderOpening is set before checking content.usesRepositoryBody(), so external/PDF/other non-reader opens can briefly show the “Opening reader...” overlay even though no private reader parse is needed. This is a minor UI regression risk rather than the blocking issue.

The asynchronous open path introduces a race risk if a user navigates away or another open request is triggered before a slow EPUB parse completes. The overlay reduces this likelihood, but there is no explicit request token or cancellation guard to prevent a stale parse from later overwriting state.

EPUB CORRECTNESS

Spine order is preserved by iterating OPF <itemref> entries in order, resolving idref through the manifest, and filtering non-linear or non-readable spine items.

EPUB3 nav and EPUB2 NCX support are preserved, with nav preferred and NCX fallback available through the spine toc attribute or .ncx/NCX media type detection.

Fallback behavior remains: when no TOC can be resolved, the code falls back to heading-derived TOC entries and then a “Start” entry.

Skipping binary image/font/CSS resources is appropriate for this app’s text-only reader, provided the EPUB’s readable content is represented in HTML/XHTML spine documents.

Bounds are sensible in concept, but there is no included test proving that over-large individual entries or aggregate retained text fail safely.

The anchor optimization is the main correctness concern. It improves complexity for section-level anchors but can miscount after inline anchors because partial block fragments are counted independently.

TEST/EVIDENCE

The included source tests add useful coverage for binary asset skipping, section-heavy anchor mapping, import preparation busy state, and off-dispatcher reader opening.

The unit-test log reports BUILD SUCCESSFUL, but the provided testDebugUnitTest.log shows the task as UP-TO-DATE; it does not demonstrate a freshly executed unit run in the bundle.

The connected visual log reports one focused screenshot test completed successfully on qaApi36.

The screenshots show the structured EPUB reader rendering correctly in light and dark modes, with readable typography, progress display, and no obvious visual breakage in the reader view.

The included visual evidence does not show the new reader-opening overlay or import-preparation state, so the visual review cannot validate the Sprint 20 UI-state additions.

BUNDLE GAPS

BUNDLE GAP: DocumentImportCandidateFactory and DocumentReadingTimeEstimator sources are not included, despite being referenced in validation and being central to import preparation performance.

BUNDLE GAP: no profiling, timing, ANR, memory, or large-real-EPUB evidence is included; the performance conclusion is code-plausibility based.

BUNDLE GAP: no evidence verifies over-limit retained text behavior for the 24 MB per-entry or 96 MB aggregate guards.

BUNDLE GAP: no test covers inline/pagebreak anchors before later TOC anchors, which is the highest-risk correctness gap in this patch.

BUNDLE GAP: no screenshot or connected visual evidence covers reader-opening-overlay or the “Preparing selected files...” import-preparation UI.

BUNDLE GAP: full repository call sites are excluded, so remaining production uses of synchronous prepareUserDocumentImport() or contentBody() cannot be ruled out from this packet.

PACKAGE HYGIENE

The bundle is generally focused and contains the manifest, sprint note, validation note, diff, changed production files, changed test files, logs, and screenshots.

The screenshot directory name sprint15-slice15-1-epub-toc-... is stale/noisy for a Sprint 20 hotfix packet.

GPT_PRO_PROMPT.md duplicates the review prompt and is not harmful, but it is ancillary rather than evidence.

The unit log being entirely UP-TO-DATE weakens evidence hygiene because it does not show fresh unit execution details.

No APKs, broad unrelated artifacts, or obvious generated build outputs are included.

RELEASE READINESS

Not ready to commit as a hotfix slice.

Required before release: fix the incremental anchor mapping regression risk, add a regression test with inline/pagebreak anchors followed by a later TOC anchor, add tests for retained-size guard failures, and rerun unit tests with non-up-to-date execution evidence.

Recommended before release: add focused visual evidence for the import-preparation state and reader-opening overlay, and restrict the reader-opening overlay to repository-backed private reader content to avoid flashes on external/PDF handoff paths.