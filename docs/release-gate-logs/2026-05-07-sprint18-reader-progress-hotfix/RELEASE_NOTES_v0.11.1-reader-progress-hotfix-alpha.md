# Quality Alternative v0.11.1-reader-progress-hotfix-alpha

This hotfix follows Sprint 18 and fixes two Reader regressions found after the Google Drive / reader-fit release.

## What Changed

- Reader progress percent is now anchored to the source paragraph and text offset, not the current page index.
- Changing Reader text size can repaginate the document without moving the saved progress percent.
- The Reader footer keeps the saved percent stable after reopening a document at a different Reader text size.
- Removed the user-facing technical annotation selection copy such as `Selection block ... steps ...`.
- Preserved compact cross-page annotation controls and long-quote scrolling behavior.
- Added visual evidence for default text size, larger text size after restore, compact annotation controls, long quote scrolling, and reopened cross-page quote state.

## Validation

- GPT Pro Sprint 18 progress hotfix R3: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Unit tests: PASS
- Targeted connected Android tests: 2/2 PASS on `qaApi36(AVD) - 16`
- APK build: PASS
- APK signature verification: PASS, Android Debug certificate, v2 signature
- Emulator install smoke: PASS

## APK

- File: `quality-alternative-v0.11.1-reader-progress-hotfix-alpha-debug.apk`
- Package: `com.qualityalternative.app`
- Version code: `17`
- Version name: `0.11.1-alpha`
- SHA-256: `5ec7fa54fdefc2aaa15c87fbaf8b57546d04d1a371f955a4ab541a50c5062b26`

## Changelog Versus `v0.11.0-gdrive-reader-fit-alpha`

- The Google Drive E2E release remains intact; this release only changes Reader progress anchoring and annotation popup copy.
- Saved progress no longer appears to jump when Reader font size changes and pages are recalculated.
- The technical selection-range status line is removed from the annotation popup.
- The same visual review trail now covers both progress stability and compact annotation selection behavior.
