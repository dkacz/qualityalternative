# v0.11.20-agent-inbox-drive-picker-alpha

Hotfixes the Agent Inbox post-release failure where Google Drive document-tree access was cleared as `Agent Inbox folder access was lost`.

## Changed

- Added a dedicated Agent Inbox Google Drive folder picker that requests `drive.readonly`.
- When Google Drive is already configured, `Choose folder` now opens the Google Drive readonly folder picker instead of relying on Android's Drive document-tree URI.
- Selecting a Google Drive folder through Android's system folder picker now redirects to the Google Drive picker instead of persisting the fragile provider URI.
- Legacy Google Drive document-tree Agent Inbox grants now show a reconnect state and route through the Drive picker.
- Local Android folders still use the local document-tree flow without Google login.

## Validation

- Local Gradle gate: `testDebugUnitTest lintDebug assembleRelease assembleDebug` passed.
- Targeted authorization/UI tests passed before the full gate.
- APK metadata: `versionCode=36`, `versionName=0.11.20-alpha`.
- Connected visual/e2e was not run locally because no adb device was attached and no accessible emulator binary was found.

## Artifact

- APK: `release_artifacts/quality-alternative-v0.11.20-agent-inbox-drive-picker-alpha-debug.apk`
- SHA-256: `e7dc89166cdfd3406796a1f89e491bdbec1af850c830134ff3899371464c17c7`
