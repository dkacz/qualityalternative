# v0.11.21 Agent Inbox Drive file picker alpha

This hotfix replaces `v0.11.20-agent-inbox-drive-picker-alpha`.

## What changed

- Removed the Agent Inbox readonly Google Picker authorization mode that caused Google Play Services `INTERNAL_ERROR` on device.
- Routed Agent Inbox folder selection through the supported Google Picker request using `drive.file` plus explicit folder-selection resource parameters.
- Kept Drive readonly authorization available for typed/manual folder-id scans and imports, without picker resource parameters.
- Preserved the reconnect behavior for legacy Google Drive document-tree Agent Inbox grants.

## Validation

- Passed: `testDebugUnitTest`
- Passed: `lintDebug`
- Passed: `assembleRelease`
- Passed: `assembleDebug`
- Passed: `git diff --check`
- Not run: connected visual/e2e, because no Android device was attached and no local emulator binary was available.

## Artifact

- APK: `release_artifacts/quality-alternative-v0.11.21-agent-inbox-drive-file-picker-alpha-debug.apk`
- SHA-256: `8390cf0fb2a09c0301e11cb9f850a0da3031e2c42f24db6ad8b76064b07760da`
