# Reader Swipe Fix Release Validation Summary

Release target: `v0.8.1-reader-swipe-alpha`

## Scope

- Fixed horizontal reader swipes in the paginated reader.
- Added connected Android coverage for left and right page swipes.
- Preserved tap-to-next and no-vertical-scroll behavior.

## Local Tests

- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#homeReadNowOpensLibraryAndPaginatedReaderWithoutIntervention`
- Result: PASS
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#epubReaderUsesKindlePagingAndTableOfContentsNavigation`
- Result: PASS
- `./gradlew testDebugUnitTest connectedDebugAndroidTest`
- Result: PASS
- Connected Android tests: 86/86 passed on `qaApi36(AVD) - 16`

## APK Build And Install

- `./gradlew assembleDebug`
- Result: PASS
- APK: `quality-alternative-v0.8.1-reader-swipe-alpha-debug.apk`
- versionCode: 13
- versionName: `0.8.1-alpha`
- SHA-256: `a45fd743ef7e2aebea1cabca9561cbf0f88bbbd3f9b68b606a7fe1fa526526ff`
- Signature verification: PASS, Android Debug certificate
- Emulator install smoke: PASS
- Installed package reported `versionCode=13`, `versionName=0.8.1-alpha`
