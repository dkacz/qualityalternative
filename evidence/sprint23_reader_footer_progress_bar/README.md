# Sprint 23 Reader Footer Progress Bar Hotfix Evidence

## User-Visible Issue

The reader footer could show a textual progress value such as `287/618 · 46%` while the small footer progress bar did not visually read as the same percentage.

## Fix

- The reader footer progress track now has a deterministic `104dp` width instead of a loose `widthIn` constraint.
- `ProgressLine` now derives its fill from a shared `readerProgressFraction(progressPercent)` helper.
- `ProgressLine` exposes `ProgressBarRangeInfo`.
- The reader footer progress bar has the test tag `reader-footer-progress-bar`, and its rendered fill has `reader-footer-progress-bar-fill`.

## Verification

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.ui.ProgressSnapshotTest.readerProgressFractionMatchesDisplayedPercentForFooterBar'`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew compileDebugAndroidTestKotlin`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#readerProgressPercentRestoresFromSourceAnchorAfterReaderFontChange`

## Visual Evidence

- `visual_e2e_r4/01_default_font_saved_progress.png`
- `visual_e2e_r4/02_large_font_restored_same_progress.png`
