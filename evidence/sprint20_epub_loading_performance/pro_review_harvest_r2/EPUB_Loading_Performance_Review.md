SCORE: 8/10

VERDICT: FAIL

VISUAL REVIEW: FAIL

BLOCKERS

Stale reader-open request guard is incomplete. A slow private EPUB load is guarded only against a later successful open request. Normal navigation methods such as openHome(), openLibrary(), openProgress(), openSettings(), and openAnnotationLibrary() do not increment readerOpenRequestId or clear isReaderOpening; therefore, a slow parse can still complete after navigation and overwrite the newer screen with Reader.

Stale reader-open failures are not token-guarded. loadReaderDocumentForSession() performs failure side effects before the caller checks requestId: it records a failure event, marks the document unavailable, autosaves, and calls clearActiveSession(). If request A is slow and fails after request B has started, request A can clear the newer open state and invalidate request B by incrementing readerOpenRequestId.

Import-preparation stale-state risk remains. Single-file and batch import preparation now run off the UI path, but cancellation/navigation is not request-token guarded. cancelAddLink() does not increment documentImportPreparationRequestId; a pending candidate preparation can return later and reopen or repopulate AddDocument. In addition, title/topic/priority mutations during isPreparing call updateAddDocumentForm(), which forces isPreparing = false, so the visible preparing state can be cleared before candidate preparation has finished.

R1 BLOCKER RECHECK

Inline/pagebreak anchor drift: resolved. The revised marker-based anchor pass avoids repeated full-prefix reparsing and the added extractDocumentMapsLaterTocAnchorAfterInlinePagebreakWithoutDrift regression covers an inline pagebreak before a later TOC target.

Missing retained-size guard tests: resolved. The bundle now includes per-entry and aggregate retained-text failure tests.

Up-to-date-only unit evidence: resolved. R2 includes testDebugUnitTest_r2.log and testDebugUnitTest_full_r2.log, both showing 28 executed tasks rather than only up-to-date tasks.

Missing busy-state visual evidence: resolved as evidence, but not sufficient for full visual sign-off. The screenshots now show import preparation and reader-opening overlay states; however, the overlay screenshot also exposes duplicate “Opening reader...” messaging and the functional stale-state blockers remain.

Possible synchronous single-import path: resolved for the heavy candidate preparation path. prepareUserDocumentImport() now launches work and calls DocumentImportCandidateFactory.fromPickedDocument() inside withContext(documentWorkDispatcher).

Stale-reader-open race risk: not resolved. Success after a newer open request is covered, but stale navigation and stale failure side effects remain unguarded.

EPUB PERFORMANCE

The EPUB extraction changes are materially improved for the reported freeze scenario. Binary ZIP entries such as images, fonts, audio, and other non-HTML resources are no longer copied into retained ByteArrays, and retained EPUB text-like entries are bounded by a 24 MB per-entry guard and a 96 MB aggregate guard.

Skipped ZIP entries are still traversed and drained sequentially by ZipInputStream, so very large binary-heavy EPUBs can still consume I/O and decompression time; however, that work is now plausibly off the main UI path for import preparation and private reader opening.

The implementation still retains every .xhtml, .html, .htm, .opf, and .ncx entry before it knows the final spine. This is a reasonable hotfix compromise for a single-pass ZipInputStream, but it is broader than “only spine-readable HTML plus TOC/package,” and large non-spine HTML resources can still count against the retained-text guard.

The one-document reader cache is useful for immediate reopen churn. Its URI/format/fingerprint key is appropriate where fingerprints exist, but legacy or imported items without verified fingerprints can still be cached by URI and format only.

ASYNC UI / FREEZE RISK

The main freeze paths are substantially improved: batch picker candidate creation runs in Dispatchers.IO, single-file prepareUserDocumentImport() runs through documentWorkDispatcher, repository-backed reader opening loads in withContext(documentWorkDispatcher), and RoomUserDocumentRepository.addDocument() moves validation, fingerprinting, and DAO work to Dispatchers.IO.

The remaining synchronous private-reader exposure inside this packet is RoomUserDocumentRepository.contentBody(), which now delegates to readerDocument() for private Markdown/EPUB. The changed MainViewModel no longer calls that path directly, but full repository call-site evidence is not in the bundle.

The request-token guard is only partial. It protects successful completion after another openLibraryItem() or openReplacementSession() call, but it does not protect completion after ordinary navigation, and it does not protect failure side effects before the token check.

The import-preparation UI state is also only partially robust. Heavy work is off the main path, but pending preparation is not invalidated by cancel/back navigation, and editable form actions can clear the preparing state prematurely.

EPUB CORRECTNESS

Spine order remains correct in the reviewed code: OPF itemref entries are traversed in order, resolved through the manifest, filtered for linear/readable documents, normalized against the OPF base directory, and parsed in that order.

EPUB3 nav and EPUB2 NCX behavior is preserved. The code prefers a manifest item with properties="nav" and falls back to the NCX item found via the spine toc attribute, NCX media type, or .ncx href.

The anchor-heavy optimization is much safer in R2. The marker insertion strategy maps all anchors through one readable-text pass, and the inline/pagebreak regression test targets the R1 drift case. I did not find a renewed TOC-drift blocker in the included anchor logic.

Skipping binary resources is correct for this app’s text-only reader, provided readable content is represented by HTML/XHTML spine documents. The patch does not attempt to render images, CSS, fonts, or other resources, which is consistent with the stated reader model.

Fallback behavior remains acceptable: if no TOC resolves, the extractor derives TOC entries from headings, then falls back to a “Start” entry.

TEST/EVIDENCE

Unit evidence is materially stronger than R1. The R2 logs show fresh execution, and the added tests cover binary asset skipping, many-anchor mapping, inline/pagebreak anchor drift, per-entry retained-size failure, aggregate retained-size failure, import-preparation busy state, background reader opening, and successful stale-open suppression when another open starts.

Connected visual evidence now includes both requested busy states. The import-preparation screenshot clearly shows “Preparing selected files...” and the explanatory message. The reader-opening screenshot shows the overlay and progress indicator, but also duplicates the same message in the bottom snackbar.

The stale-reader test is too narrow. It proves only that a successful older open does not overwrite a successful newer open. It does not cover navigation during a slow parse, nor does it cover stale failure side effects, which remain defective in code.

BUNDLE GAPS

BUNDLE GAP: no unit test demonstrates that a slow reader open cannot overwrite state after navigation away from the opening screen.

BUNDLE GAP: no unit test demonstrates that a stale failed reader parse cannot clear a newer reader-open request, mark an older document unavailable, or suppress the newer successful load.

BUNDLE GAP: no unit or visual test covers cancellation/back navigation during import preparation.

BUNDLE GAP: no profiling, ANR, memory, or large real-world EPUB evidence is included; the performance assessment is code-plausibility based.

BUNDLE GAP: full repository call sites are not included, so remaining production calls to synchronous contentBody() cannot be ruled out from this review packet.

PACKAGE HYGIENE

The bundle is focused and includes the manifest, sprint note, validation note, current diff, changed production and test files, unit logs, connected logs, and screenshots.

The old up-to-date-only testDebugUnitTest.log is still included, but R2 fresh-execution logs are also present and supersede it.

GPT_PRO_REVIEW.md and GPT_PRO_PROMPT.md are ancillary rather than validation artifacts, but they are useful for R1 comparison and do not create a material hygiene issue.

No APKs, broad generated build outputs, or unrelated screenshot sets are included.

RELEASE READINESS

Not ready to commit as a hotfix slice.

Required before release: guard reader-open completion and failure side effects with a token that is invalidated by navigation/cancel paths, add tests for navigation-after-open and stale-failure races, and prevent import-preparation completion from repopulating the add-document screen after cancellation or navigation.