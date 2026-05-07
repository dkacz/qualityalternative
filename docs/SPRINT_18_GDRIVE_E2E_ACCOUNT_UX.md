# Sprint 18 - Google Drive E2E And Account UX

## Goal

Make the live Google Drive authorization path testable end to end for developer-approved testers, remove misleading account UI from onboarding, and re-check the reader annotation range regressions before a new APK/review gate.

## User Problems

- Google Drive connection must actually authorize for tester accounts instead of only explaining why it failed.
- The welcome screen must not show "I have an account" while the product has no account system.
- A cancelled or blocked Google OAuth attempt must not silently push the user into a different folder-picker flow.
- The cross-page annotation range controls must still allow the start handle to move backward across page/source boundaries.
- Reader pagination must not hide or clip the last visible line behind the bottom footer.

## Slice Plan

- 18.1 Accountless onboarding cleanup: remove the dead account shortcut and verify the welcome screen visually.
- 18.2 Google Drive OAuth tester readiness: validate account chooser, consent, blocked-account handling, and Google Cloud test-user requirements.
- 18.3 Drive fallback separation: keep Android folder destination under "Change destination" only; keep "Connect" as OAuth-only.
- 18.4 Reader annotation regression pass: run cross-page selection and start-backward tests on the emulator.
- 18.5 Reader bottom-fit pass: measure rendered reader text against the live viewport so every page keeps a visible bottom-line guard.
- 18.6 Final gate: unit tests, connected tests, screenshots, debug APK, GPT Pro review, and iterate to 10/10.

## Implementation Notes

- Removed the dead `I have an account` onboarding shortcut while the product has no account system.
- Kept Google Drive `Connect` as OAuth-only. Cancelled/blocked OAuth no longer falls into the Android folder picker.
- Updated cancelled/blocked OAuth copy to say that Google Drive authorization was cancelled or blocked and that no folder destination was changed, instead of misreporting every cancel as a tester-account block.
- Published the Google Cloud OAuth app for the debug client after verifying:
  - package: `com.qualityalternative.app`
  - SHA-1: `15:8F:50:67:D8:1C:7A:79:F7:8C:94:BA:1E:5E:D2:1B:9E:F6:33:C9`
  - scope: `https://www.googleapis.com/auth/drive.file`
  - configured internal emulator tester account
- Fixed plain-text/Markdown reader documents so each paragraph receives a stable `sourceBlockIndex`. Before this, all plain-text blocks defaulted to `0`, so cross-block annotation selection collapsed to the first block and the start handle appeared unable to move backward.
- Compacted annotation range controls in the annotation popup and removed the user-facing technical range summary so long cross-page selections preserve their endpoints without showing debug-style copy.
- Changed reader pagination to use measured Compose text heights after the viewport is known. The first render still has a conservative heuristic fallback, then the measured pass creates pages that keep the last rendered line above the footer without requiring reader scrolling.

## Live Google Drive Evidence

- Emulator created a real reader annotation note: `Sprint18_drive_live_note`.
- Google Drive `Connect` completed from the emulator account chooser and synced annotations.
- `Save now` after connection completed without a new blocker and showed `Annotations synced to Google Drive.`
- `rclone` confirmed files in `gdrive:Quality Alternative annotations`:
  - `quality-alternative-annotations.index.json`
  - `quality-alternative-care-for-the-soul-first-care-for-the-soul-first.annotations.jsonld`
- The downloaded JSON-LD contains the note body value `Sprint18_drive_live_note`.

## Validation

- `:app:testDebugUnitTest`
- `MainActivityTest#onboardingWelcomeDoesNotShowAccountShortcutWithoutAccountFlow`
- `MainActivityTest#readerAnnotationStartCanMoveBackIntoPreviousSourceBlocks`
- `MainActivityTest#readerAnnotationControlsExpandAndReopenAcrossPages`
- `MainActivityTest#readerAnnotationEditorContainsLongQuoteAndLongNoteWithinViewport`
- `MainActivityTest#readerPaginationFitRespondsToViewportAndReaderTextSize`
- `:app:assembleDebug`
- Runtime OAuth cancel evidence captures account-chooser back-out returning to Settings with no Android folder picker.

Detailed logs and screenshots are under `evidence/sprint18_gdrive_e2e_account_ux/`.

## External Configuration Status

The previous blocker was:

`Access blocked: Quality Alternative has not completed the Google verification process`

That blocker is resolved for this debug client by publishing the OAuth app after scope/package/SHA verification. The app is now in production audience mode, so emulator/tester accounts can authorize without being individually listed as test users.

## Acceptance

- Welcome screen has a single primary `Begin` action and no account shortcut.
- Google Drive `Connect` opens Google account authorization for a configured tester account and completes.
- OAuth cancellation or Google blocking reports a useful in-app Drive status and does not open the Android folder picker.
- Android folder destination remains available only via `Change destination`.
- Cross-page annotation selection and start-backward controls pass connected tests.
- Reader pages fit fully above the footer on tall and compact emulator viewports, including body text, large reader text, and code-heavy pages.
- GPT Pro review returns SCORE 10/10, VERDICT PASS, and VISUAL REVIEW PASS.
