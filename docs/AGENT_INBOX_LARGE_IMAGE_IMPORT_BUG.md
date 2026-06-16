# Bug: Agent Inbox import fails for image attachments above ~2.4 MiB ("Package could not be saved")

Status: fixed locally on `codex/agent-inbox-large-image-import-fix`
Severity: high (silent, opaque failure that blocks valid packages and gives the author no diagnosable reason)
Area: Agent Inbox package import (Markdown packages with image sidecars)
Filed: 2026-06-16

## Summary

A structurally valid Agent Inbox package (one folder: `manifest.json` + one Markdown content file + one PNG image sidecar) imports successfully when the image file is up to ~2.36 MiB and **fails** when the image file is ~2.45 MiB or larger. The app shows the candidate as `NEEDS PACKAGE CLEANUP` with the detail **"Package could not be saved. Update it and scan again."**

That message maps to `AgentInboxPackageValidationError.LOCAL_IMPORT_REJECTED`, which is set by a catch‑all `catch (error: Throwable)` in the import path. The real exception is swallowed and never surfaced, so neither the user nor the package author can see why the save failed.

The contract and all structural validators advertise a **5 MiB** per‑image limit (`AGENT_INBOX_MAX_IMAGE_ATTACHMENT_BYTES`). The real, device‑side failure point is roughly **half** of that. There is no code constant near 2.4 MiB; the failure is environmental (heap/IO), correlated with the image **file byte size**, not with pixel count.

## Reproduction

1. Build any valid Markdown package whose single image sidecar is a PNG of ~2.45 MiB or larger (pixel dimensions are irrelevant; ~1.5 MP is enough).
2. Upload the package folder to the connected Agent Inbox Drive folder.
3. Scan in the app. The candidate validates structurally, then on import shows `NEEDS PACKAGE CLEANUP` / "Package could not be saved. Update it and scan again."
4. Repeat with the same content but a smaller image (≤ ~2.36 MiB, e.g. re‑encoded as JPEG): the import succeeds.

### Observed data (five real packages, same authoring pipeline)

| Package | Image bytes | MiB | Pixels | Result |
|---|---|---|---|---|
| penrose | 2,299,566 | 2.19 | ~1.57 MP | imported OK |
| pinkard | 2,478,709 | 2.36 | ~1.57 MP | imported OK |
| vorstellung | 2,541,585 | 2.42 | ~1.57 MP | (untested at the time) |
| kant‑obiektywnosc | 2,564,830 | 2.45 | ~1.57 MP | NEEDS PACKAGE CLEANUP |
| pojecie‑a‑platonska‑idea | 2,901,578 | 2.77 | ~1.57 MP | NEEDS PACKAGE CLEANUP |

All five PNGs are RGB 8‑bit, ~1.57 megapixels (near‑identical pixel counts), so the discriminator is **file bytes**, not pixels. All five pass `tools/validate_agent_inbox_package.py` (PASS) and their `documentSha256` match the content bytes.

## Ruled out

- **Not structural / manifest validation.** All packages pass the authoritative validator (`tools/validate_agent_inbox_package.py`) and the in‑app structural checks. `documentSha256` matches.
- **Not the declared download limit.** Image download uses `maxBytes = AGENT_INBOX_MAX_IMAGE_ATTACHMENT_BYTES` (5 MiB) and exceeding it throws `AgentInboxDriveDownloadTooLargeException` → `IMAGE_ATTACHMENT_TOO_LARGE`, a *different* error. Our images are < 3 MiB, so this path is not taken.
- **Not image decoding / pixels.** `BitmapFactory.decode*` is only invoked in the Reader UI render path (`QualityAlternativeApp.kt` ~10107‑10135), **not** during import. The failing images do not have more pixels than the passing ones, so it is not a bitmap/decode issue.
- **Not a DB blob of image bytes.** Images are stored as **files** on disk; the Room row stores only short file URIs (`imageAttachmentUrisJson`, ~200‑300 bytes), not image bytes.

## Code path (where the failure is reported, and where it is thrown)

The import action wraps the whole flow in one catch‑all that produces the opaque error:

- `app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt`
  - download loop accumulates each image fully in memory: `imageAttachmentBytes[attachment.fileName] = attachmentBytes` (**:1847**), then `agentInboxPackageImporter.importCandidate(...)` (**:1849**).
  - outer `catch (error: Throwable)` (**:1860**) → unconditionally sets `packageErrors = setOf(AgentInboxPackageValidationError.LOCAL_IMPORT_REJECTED)` (**:1867**). Any non‑`CancellationException`/non‑`AgentInboxDriveAccessLostException` throwable lands here with no logging and no class/message captured.
- `app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt`
  - maps `LOCAL_IMPORT_REJECTED` → "Package could not be saved. Update it and scan again." (~**:9775**).

Throw sites that can reach that catch (unproven which one fires; see Root cause):

- `app/src/main/java/com/qualityalternative/app/data/AgentInboxPackageImporter.kt`
  - `documentStore.writeDocument(...)` (**:106**) — **not** individually wrapped; exceptions propagate to MainViewModel.
  - `userDocumentRepository.addDocumentIfFingerprintAbsent(...)` (**:133**) — wrapped only for file cleanup, then re‑throws.
- `app/src/main/java/com/qualityalternative/app/data/AgentInboxDocumentStore.kt`
  - content temp write + **read‑back for sha256**: `tempFile.writeBytes(bytes)` (**:69**), `sha256(tempFile.readBytes())` (**:70**).
  - image attachment write loop: `plan.tempFile.writeBytes(plan.bytes)` (**:111**).
- `app/src/main/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClient.kt`
  - `readBoundedBytes` buffers the entire download in a `ByteArrayOutputStream` created with **no initial capacity** (**:233**), growing by power‑of‑two reallocation, then `toByteArray()` copies again (**:245**).

## Root cause analysis

Two distinct problems, one certain and one a strong hypothesis.

### Problem 1 (certain): the failure is opaque

`MainViewModel.kt:1860` catches every `Throwable` and collapses it to `LOCAL_IMPORT_REJECTED` without logging the exception class or message and without distinguishing OOM vs IOException vs SQLite vs anything else. This is why the symptom is undiagnosable from the app, and why a 5 MiB‑advertised limit silently fails at ~2.4 MiB. **This is the first thing to fix regardless of the underlying mechanism.**

### Problem 2 (leading hypothesis, needs a device logcat to confirm): peak heap pressure from in‑memory byte buffering

The failure is byte‑size‑correlated (not pixel‑correlated) and there is no size constant near 2.4 MiB, so the most plausible mechanism is an `OutOfMemoryError` (or a memory‑related IO failure) caused by holding several full‑size copies of the image bytes in heap at once during import:

1. `readBoundedBytes` buffers the whole download in a `ByteArrayOutputStream` with no capacity hint (power‑of‑two growth → up to ~2× transient over‑allocation), then `toByteArray()` produces another full copy.
2. The resulting `ByteArray` is stored in the `imageAttachmentBytes` map and passed to `importCandidate`.
3. `writeDocument` writes bytes to a temp file and, for the **content** file, reads it back to recompute sha256 (another allocation; small for content, but the same pattern).

On a real device, the transient peak of multiple ~2.5+ MiB allocations (plus the doubling buffer) is a credible OOM trigger, and OOM probability rises monotonically with file size — matching the observed "bigger file → fails" ordering. Static analysis cannot prove this is the exact throw; a `logcat` capture of the caught exception (see Problem 1 fix) will confirm it immediately.

Note: the persistence path stores only short file URIs in Room, so a `CursorWindow`/Binder row‑size limit is **unlikely** (the row does not contain image bytes). The download/write byte buffering is the stronger lead.

## Proposed fix

### P0 — Surface the real exception (do this first; it is also how we confirm P1)

In `MainViewModel.kt:1860` (and the analogous handlers), stop collapsing every `Throwable` into a bare `LOCAL_IMPORT_REJECTED`:

- Log the exception (`Log.e` with class + message + stacktrace).
- Carry the exception class/message into the review candidate so the detail text can show e.g. "Could not save: OutOfMemoryError" instead of "Package could not be saved."
- Keep re‑throwing `CancellationException`.

This alone makes the bug diagnosable on any device and turns every future "could not be saved" into an actionable message.

### P1 — Remove the peak‑memory amplification on the import path

- In `AndroidGoogleDriveAgentInboxClient.readBoundedBytes`, size the `ByteArrayOutputStream` with the known/declared content length (or stream directly to the destination file) instead of growing from zero.
- In `AgentInboxDocumentStore.writeDocument`, avoid the write‑then‑read‑back‑for‑sha256 round trip: compute the SHA‑256 from the in‑memory bytes you already hold (or via a streaming digest while writing) rather than `readBytes()` after the write.
- Prefer streaming the Drive download straight into the temp file (digesting as you go) so a full second copy of the image never needs to live in heap.
- Wrap the image write/persist specifically so an `OutOfMemoryError`/`IOException` maps to a precise, documented error (e.g. a new `IMAGE_WRITE_FAILED`, or `IMAGE_ATTACHMENT_TOO_LARGE` with a device‑memory note) instead of the generic `LOCAL_IMPORT_REJECTED`.

### P2 — Make the limit honest, and test it

- Add an instrumentation/integration test that imports a package with a ~3–4 MiB image sidecar and asserts success (after P1), or asserts a *specific, documented* failure.
- If, after P1, the device genuinely cannot persist images up to the advertised 5 MiB, then lower the documented + enforced per‑image limit to a value that is actually tested to import, and update `docs/AGENT_INBOX_PACKAGE_AUTHORING.md` to match. The advertised limit and the working limit must agree.

## Acceptance criteria

- Importing a valid package whose image sidecar is ~3 MiB succeeds (Markdown renders with the image), OR fails with a specific, logged, human‑readable reason — never the generic "Package could not be saved."
- The caught exception's class/message is logged for every import failure.
- A regression test covers a multi‑megabyte image attachment.
- `docs/AGENT_INBOX_PACKAGE_AUTHORING.md` per‑image limit equals the limit the import path actually honors.

## Fix status on 2026-06-16

- `MainViewModel` now logs import-time caught exceptions with `Log.e`, including class, message, and stacktrace, while still rethrowing `CancellationException`.
- `AgentInboxReviewCandidate` now carries `importFailureDetail` so invalid rows can show the exception class/message.
- Import-time download failures and local write failures now preserve specific failure details in UI state.
- Image sidecar write `IOException`/`OutOfMemoryError` now maps to `IMAGE_WRITE_FAILED` instead of generic `LOCAL_IMPORT_REJECTED`.
- GPT Pro R1 found one remaining image-write edge case: sidecar temp-file creation could fail before the wrapping/cleanup scope. The fix moves sidecar plan/temp-file creation into that scope, avoids rewrapping an existing `AgentInboxImageAttachmentWriteException`, and unwraps nested image-write wrappers to the root class/message for UI detail.
- GPT Pro R2 returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW NOT APPLICABLE`; the R1 edge case is fixed and the release can proceed.
- Drive/document-tree downloads now accept known `expectedBytes` and pre-size/read directly when metadata provides a bounded size.
- `FileAgentInboxDocumentStore` no longer writes the content temp file and then re-reads it just to recompute SHA-256; it verifies the already-held bytes.
- Regression coverage includes a 3.5 MiB Markdown image sidecar import through `FileAgentInboxDocumentStore`, plus a sidecar temp-file creation failure that maps to `IMAGE_WRITE_FAILED` and leaves no orphaned files.
- Validation passed: targeted Agent Inbox unit tests, full `testDebugUnitTest`, `lintDebug`, `compileDebugAndroidTestKotlin`, `assembleDebug`, targeted rerun for the Chrome evidence-path failure, and full `connectedDebugAndroidTest` with 138/138 tests passing.

## Appendix: authoring‑side mitigation already in place (not a substitute for the app fix)

The package‑authoring tool on the producer side (`build_inbox_package.py`, external to this repo) now auto‑re‑encodes any image sidecar above 1.8 MiB to a JPEG below 1.4 MiB, so newly authored packages stay well under the failing threshold. That keeps the authoring pipeline working today but does **not** fix the app: the 5 MiB contract is still not delivered, and any package (from any author) with a larger image will still fail opaquely until P0/P1 land.
