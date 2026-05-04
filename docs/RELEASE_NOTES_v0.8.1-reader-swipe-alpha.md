# Quality Alternative v0.8.1-reader-swipe-alpha

Internal Android alpha patch for GitHub testers.

This release fixes the reader swipe gesture regression found after `v0.8.0-kindle-drive-annotations-alpha`.

## What changed since v0.8.0-kindle-drive-annotations-alpha

- Horizontal swipe gestures now work inside the paginated reader.
- Swiping left advances to the next page.
- Swiping right returns to the previous page.
- Swiping left on the final page keeps the existing completion behavior.
- Tap-to-next still works.
- Vertical scrolling remains disabled in the reader, preserving the Kindle-style fixed page model.
- The reader instrumentation test now verifies left and right swipes on the page viewport.

## Validation

- Targeted reader regression test passed on `qaApi36(AVD) - 16`:
  `MainActivityTest#homeReadNowOpensLibraryAndPaginatedReaderWithoutIntervention`
- EPUB paging and table-of-contents regression test passed on `qaApi36(AVD) - 16`:
  `MainActivityTest#epubReaderUsesKindlePagingAndTableOfContentsNavigation`
- Full validation passed: `./gradlew testDebugUnitTest connectedDebugAndroidTest`
- Connected Android tests: 86/86 passed on `qaApi36(AVD) - 16`
- Debug APK build passed.
- APK signature verification passed with Android Debug certificate.
- Emulator install smoke passed and reported `versionCode=13`, `versionName=0.8.1-alpha`.

## APK Assets

- Installable alpha APK: `quality-alternative-v0.8.1-reader-swipe-alpha-debug.apk`
- APK versionCode: 13
- APK versionName: `0.8.1-alpha`
- SHA-256: `a45fd743ef7e2aebea1cabca9561cbf0f88bbbd3f9b68b606a7fe1fa526526ff`

## Evidence

- Release validation summary: `docs/release-gate-logs/2026-05-04-reader-swipe-fix/VALIDATION_SUMMARY.md`
