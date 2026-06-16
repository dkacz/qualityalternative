# v0.11.19-agent-inbox-manifest-visibility-alpha

Fixes Agent Inbox packages selected from Google Drive through Android's folder picker being incorrectly shown as `Package is missing manifest.json`.

## Changed

- Google Drive-backed Android document-tree folders now scan through Drive API with readonly authorization.
- The app extracts the Drive folder ID from the selected tree URI, but keeps the original `content://...` URI in settings for Android permission handling.
- Local Android document-tree folders still scan and import without Google login.
- Agent Inbox import now requests Drive readonly authorization when importing packages discovered through a Google Drive-backed document-tree folder.

## Validation

- GPT Pro review R1: `VERDICT: PASS`, `SCORE: 10/10`, no blockers.
- Local Gradle gate: `testDebugUnitTest lintDebug assembleRelease assembleDebug` passed.
- APK metadata: `versionCode=35`, `versionName=0.11.19-alpha`.
- Connected visual/e2e was not run locally because no adb device was attached and no accessible emulator binary was found.

## Artifact

- APK: `release_artifacts/quality-alternative-v0.11.19-agent-inbox-manifest-visibility-alpha-debug.apk`
- SHA-256: `e6b83c4adcff52ed13e45485a6f9cef5012759a0894aa8f7c6650a8a1b61a79d`
