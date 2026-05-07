# Quality Alternative v0.11.2-reader-regression-form-alpha

This is the Sprint 19 regression-fix APK. It ships reader and intervention fixes before any AI note-assist work begins.

## What Changed

- EPUB reader source blocks are now globally indexed across spine chapters, so annotation range movement from later chapters no longer confuses Chapter Two or Chapter Three with the beginning of the book.
- Reader progress is anchored to stable source block identity and text offset, so Chapter Three no longer presents as a beginning-of-book `1%` state.
- Reader font-size changes can repaginate the document while preserving the same source progress percent.
- Annotation start controls can move backward across source/page boundaries while preserving the selected quote and saved note after reopen.
- `Open anyway` form intervention now has a visible 5-second calm wait. The open action and close icon stay disabled until the wait completes.
- Form intervention analytics now cover shown, unlock blocked, unlock enabled, unlock used, completed, and abandoned states.
- Portable Profile / profile-autosave coverage now asserts source-anchor progress fields persist through existing export/autosave paths.

## Not Included

- AI note assistance is intentionally not included in this APK.
- OpenRouter/Gemini configuration and `Ask AI` annotation UI remain queued for the second part of Sprint 19 after this regression APK is released.

## Validation

- GPT Pro Sprint 19 regression gate R2: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Full debug unit tests: PASS
- Debug APK and debug Android test build: PASS
- Connected reader/progress/annotation E2E: PASS
- Connected form-intervention E2E: PASS
- APK signature verification: PASS, Android Debug certificate, v2 signature
- Emulator install smoke: PASS
- Launch smoke: PASS, `com.qualityalternative.app/.MainActivity` focused
- Emulator shutdown after smoke: PASS

## APK

- File: `quality-alternative-v0.11.2-reader-regression-form-alpha-debug.apk`
- Package: `com.qualityalternative.app`
- Version code: `18`
- Version name: `0.11.2-alpha`
- SHA-256: `a027802ed0f648be722cb41136ed30bf0879c939de6023b86f5bb1d85c2e75b3`

## Changelog Versus `v0.11.1-reader-progress-hotfix-alpha`

- Extends the previous source-anchored progress hotfix to chaptered EPUB extraction by removing chapter-local source block collisions.
- Adds visual E2E evidence that Chapter Three progress is not `1%` and remains stable when reader text size changes.
- Fixes annotation start-backward movement across chapter/source boundaries and proves saved/reopened quote persistence.
- Replaces the broken form-intervention bypass with a finite 5-second unlock wait and analytics coverage.
- Keeps AI out of the APK so tester validation can focus on reader, profile progress, annotation selection, and intervention behavior.
