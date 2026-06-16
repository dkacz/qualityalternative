# GPT Pro Review Request R2 - Sprint 30 Agent Inbox Large Image Import Fix

Use only the attached bundle as your audit base. Read these first:

1. `docs/AGENT_INBOX_LARGE_IMAGE_IMPORT_BUG.md`
2. `evidence/sprint30_agent_inbox_large_image_import_fix/review/GPT_PRO_REVIEW_R1.md`
3. `evidence/sprint30_agent_inbox_large_image_import_fix/review/REVIEW_BUNDLE_MANIFEST.md`

R1 returned:

```text
SCORE: 8
VERDICT: REVISE
VISUAL REVIEW: NOT APPLICABLE
```

R1 finding to recheck:

- In `FileAgentInboxDocumentStore.writeImageAttachmentsAtomically`, sidecar `AttachmentWritePlan`/temp-file creation happened before the sidecar wrapping/cleanup `try`, so `File.createTempFile(...)` failures could still escape as generic `LOCAL_IMPORT_REJECTED` and could leave temp files.

R2 claims:

- Sidecar plan/temp-file creation now happens inside the cleanup/wrap scope.
- Existing `AgentInboxImageAttachmentWriteException` values are not rewrapped.
- Failure detail unwraps nested image-write wrappers to the root cause class/message.
- `AgentInboxPackageImporterTest.importCandidateMapsSidecarTempCreationFailureToImageWriteFailure` forces sidecar temp creation failure and checks `IMAGE_WRITE_FAILED` mapping, root-cause message preservation, and cleanup.
- The earlier 3.5 MiB Markdown image sidecar import regression still passes.
- Full `testDebugUnitTest` passes after the R1 fix; focused XML evidence is included for the relevant test classes.

## Scope To Review

Re-audit the full Sprint 30 implementation and specifically verify whether the R1 finding is fully fixed. The release goal remains:

- P0: import-time catch-all failures are logged with class, message, and stacktrace through `Log.e`; `CancellationException` is still rethrown; the failure class/message is carried into `AgentInboxReviewCandidate`; candidate detail text can show a specific reason instead of only `Package could not be saved`.
- P1: peak-memory amplification is reduced on the import path. Downloads use known expected/content length when available instead of unhinted `ByteArrayOutputStream` growth; content temp-file SHA verification no longer re-reads the written file; image sidecar write `IOException`/`OutOfMemoryError` maps to `IMAGE_WRITE_FAILED` instead of generic `LOCAL_IMPORT_REJECTED`, including sidecar temp-file creation failures.
- P2: regression coverage proves a multi-megabyte Markdown image sidecar import succeeds under the existing 5 MiB contract.
- Existing structural validation and authoring contract are not weakened.
- Remote-safe analytics do not start carrying raw exception messages, Drive ids, file paths, package ids, or tokens.
- The unrelated Chrome evidence-path fix is appropriate and does not weaken test coverage.

## Output Format

Start with:

```text
SCORE: <0-10>
VERDICT: PASS | REVISE | BLOCK
VISUAL REVIEW: PASS | REVISE | NOT APPLICABLE
```

Then provide:

- `R1 RECHECK`: say whether the R1 finding is fixed, with file/test citations.
- `FINDINGS`: ordered by severity; each finding must cite file paths and explain the concrete failure mode.
- `REQUIREMENT AUDIT`: P0/P1/P2 and validation status.
- `BUNDLE GAPS`: only if the bundle lacks evidence needed for a claim.
- `PACKAGE HYGIENE`: say whether the bundle is clean enough for this lane.
- `RELEASE READINESS`: whether this can proceed to APK release.

Use `PASS` only if there are no blocking or revise-level findings for the scoped Sprint 30 release.
