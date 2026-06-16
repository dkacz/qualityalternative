# GPT Pro Review Request - Sprint 30 Agent Inbox Large Image Import Fix

Use only the attached bundle as your audit base. Read `docs/AGENT_INBOX_LARGE_IMAGE_IMPORT_BUG.md` first, then audit the implementation and tests against that bug report.

Guiding principles:

1. Do not inflate suggestions that duplicate existing coverage into fresh findings.
2. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.
3. Prioritize concrete correctness, privacy, memory, test, and release-readiness risks.
4. Style suggestions are secondary unless they hide a real defect.
5. Treat validation XML and patch files as evidence only when they directly prove the relevant requirement.
6. Include bundle hygiene feedback.

## Scope To Review

The goal is to fix Agent Inbox import of structurally valid Markdown packages with multi-megabyte image sidecars. The bug was that packages with image sidecars around 2.4 MiB failed with generic `LOCAL_IMPORT_REJECTED` even though the advertised per-image limit is 5 MiB.

Review the implementation for these requirements:

- P0: import-time catch-all failures are logged with class, message, and stacktrace through `Log.e`; `CancellationException` is still rethrown; the failure class/message is carried into `AgentInboxReviewCandidate`; candidate detail text can show a specific reason instead of only `Package could not be saved`.
- P1: peak-memory amplification is reduced on the import path. In particular, downloads use known expected/content length when available instead of unhinted `ByteArrayOutputStream` growth; content temp-file SHA verification no longer re-reads the written file; image sidecar write `IOException`/`OutOfMemoryError` maps to a specific documented `IMAGE_WRITE_FAILED` instead of generic `LOCAL_IMPORT_REJECTED`.
- P2: regression coverage proves a multi-megabyte Markdown image sidecar import succeeds under the existing 5 MiB contract, or the code/docs honestly lower the limit. Here the intended implementation keeps the 5 MiB limit and adds a 3.5 MiB sidecar import test.
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

- `FINDINGS`: ordered by severity; each finding must cite file paths and explain the concrete failure mode.
- `REQUIREMENT AUDIT`: P0/P1/P2 and validation status.
- `BUNDLE GAPS`: only if the bundle lacks evidence needed for a claim.
- `PACKAGE HYGIENE`: say whether the bundle is clean enough for this lane.
- `RELEASE READINESS`: whether this can proceed to APK release.

Use `PASS` only if there are no blocking or revise-level findings for the scoped Sprint 30 release.
