# v0.11.25 Agent Inbox Drive Auth Repair Alpha

This release fixes the Agent Inbox state shown in the user's real-device failure screenshot: Google Drive authorization failed, but the app still showed an empty in-app `My Drive` folder browser and `No folders on this level.`

## Changes

- Agent Inbox `Choose folder` no longer opens the in-app Drive browser until Google authorization succeeds and returns an access token.
- Google Drive authorization failure now closes and resets the Agent Inbox folder browser instead of leaving a misleading empty Drive root visible.
- Agent Inbox readonly connect/browse authorization no longer forces a consent prompt; Google Identity can reuse already-granted scopes and prompt only when needed.
- Regression visual coverage now asserts the authorization-failure state does not show an empty `My Drive` browser.

## Validation

- GPT Pro review: `SCORE: 10/10`, `VERDICT: PASS`, blockers none.
- Live signed-in emulator E2E used real Google Play Services, account `omareth@gmail.com`, and a real Google Drive folder/package:
  - Folder: `QA-Agent-Inbox-Sprint37-Auth-Repair-20260617-143008`
  - Package: `codex-sprint37-drive-auth-repair-package`
  - Manifest priority: `high`
- Evidence covers account chooser, Drive folder browser, folder selection, scan, priority acceptance, import, Library `Files`, reader rendering, and clean logcat sentinels.
- Final release build gate after version bump passed: `testDebugUnitTest`, `lintDebug`, `assembleDebug`, `assembleRelease`.

## APKs

- Debug APK: `release_artifacts/quality-alternative-v0.11.25-agent-inbox-drive-auth-repair-alpha-debug.apk`
- Unsigned release APK: `release_artifacts/quality-alternative-v0.11.25-agent-inbox-drive-auth-repair-alpha-release-unsigned.apk`
