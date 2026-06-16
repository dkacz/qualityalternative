# v0.11.18 Agent Inbox Large Image Import Fix Alpha

This alpha fixes Agent Inbox Markdown packages whose valid image sidecars could fail during import with a generic "Package could not be saved" state despite being below the documented 5 MiB per-image limit.

## Highlights

- Added specific import failure details for Agent Inbox rows, including local exception class/message in UI state.
- Added `IMAGE_WRITE_FAILED` for image sidecar persistence failures instead of collapsing those failures into generic local rejection.
- Reduced avoidable memory amplification when downloading Agent Inbox files by using expected/content length when available.
- Removed a redundant temp-file read-back during document SHA verification.
- Covered a 3.5 MiB Markdown image sidecar import under the existing 5 MiB contract.
- Covered sidecar temp-file creation failure so it maps to `IMAGE_WRITE_FAILED` and leaves no orphaned files.
- Kept raw exception messages, Drive ids, file ids, paths, package ids, and tokens out of remote-safe analytics.

## Validation

- GPT Pro R2: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW NOT APPLICABLE`.
- Full connected Android gate before release: `connectedDebugAndroidTest` PASS, 138 tests, 0 failures.
- Final release-gate validation is recorded in this folder's `VALIDATION_SUMMARY.md`.

## Artifact

- APK: `quality-alternative-v0.11.18-agent-inbox-large-image-import-fix-alpha-debug.apk`
