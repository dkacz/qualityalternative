# v0.11.22 Agent Inbox readonly link fallback alpha

This release replaces `v0.11.21-agent-inbox-drive-file-picker-alpha`.

## What changed

- Kept Agent Inbox Google Drive folder selection on the supported `drive.file` Picker path.
- Added an explicit `Drive folder link or id` fallback in Settings for the controlled `drive.readonly` typed/manual folder-id path.
- The fallback validates the pasted Drive folder URL/id, requests readonly consent without Picker resource parameters, then scans only the saved Agent Inbox folder id.
- Removed a stale unreachable Google Drive document-tree branch after the early Picker redirect.
- Added literal OAuth scope assertions for `drive.file` and `drive.readonly`.

## Validation

- Passed: `testDebugUnitTest`
- Passed: `lintDebug`
- Passed: `assembleRelease`
- Passed: `assembleDebug`
- Passed: `git diff --check`
- Not run: connected visual/e2e, because no Android device was attached and no local emulator binary was available.

## Artifact

- APK: `release_artifacts/quality-alternative-v0.11.22-agent-inbox-readonly-link-fallback-alpha-debug.apk`
- SHA-256: `2bd452f4b37b5e92fa203940096474da7d35b092bb820d306e19e1bc2c280264`
