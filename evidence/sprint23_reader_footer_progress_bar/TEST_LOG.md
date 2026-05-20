# Sprint 23 Reader Footer Progress Bar Hotfix Test Log

## JVM Regression

Command:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.ui.ProgressSnapshotTest.readerProgressFractionMatchesDisplayedPercentForFooterBar'
```

Result:

```text
BUILD SUCCESSFUL in 25s
28 actionable tasks: 7 executed, 21 up-to-date
```

R3 rerun after adding rendered fill-width assertion:

```text
BUILD SUCCESSFUL in 5s
28 actionable tasks: 5 executed, 23 up-to-date
```

## Android Test Compilation

Command:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew compileDebugAndroidTestKotlin
```

Result:

```text
BUILD SUCCESSFUL in 3s
30 actionable tasks: 2 executed, 28 up-to-date
```

R3 rerun after adding rendered fill-width assertion:

```text
BUILD SUCCESSFUL in 21s
30 actionable tasks: 4 executed, 26 up-to-date
```

R4 rerun after screenshot helper rename:

```text
BUILD SUCCESSFUL in 2s
30 actionable tasks: 2 executed, 28 up-to-date
```

## Emulator E2E

Emulator:

```text
qaApi36(AVD) - Android 16 / API 36
```

Command:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#readerProgressPercentRestoresFromSourceAnchorAfterReaderFontChange
```

Result:

```text
Starting 1 tests on qaApi36(AVD) - 16
Finished 1 tests on qaApi36(AVD) - 16
BUILD SUCCESSFUL in 14s
73 actionable tasks: 8 executed, 65 up-to-date
```

R3 rerun after adding rendered fill-width assertion:

```text
Starting 1 tests on qaApi36(AVD) - 16
Finished 1 tests on qaApi36(AVD) - 16
BUILD SUCCESSFUL in 15s
73 actionable tasks: 8 executed, 65 up-to-date
```

R4 rerun after screenshot helper rename:

```text
Starting 1 tests on qaApi36(AVD) - 16
Finished 1 tests on qaApi36(AVD) - 16
BUILD SUCCESSFUL in 13s
73 actionable tasks: 4 executed, 69 up-to-date
```

The E2E flow asserts that `reader-footer-progress-bar` exposes `ProgressBarRangeInfo.current == savedPercent / 100f`, and that rendered `reader-footer-progress-bar-fill` width divided by rendered `reader-footer-progress-bar` width equals `savedPercent / 100f` within 0.03. It runs before and after reader-font repagination, and captures visual screenshots in `visual_e2e_r4/`.
