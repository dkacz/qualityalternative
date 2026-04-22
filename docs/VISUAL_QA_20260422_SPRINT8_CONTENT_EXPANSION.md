# Sprint 8 Content Expansion Visual QA

Status: `passed on emulator`

Scope: visual validation for Sprint 8 content scale-up after `link-only-modern-v1`, `public-domain-expansion-v2`, and the 3-minute meditation replacement.

Device: `qaApi36` emulator via `emulator-5554`.

## Screenshots

Artifacts:

- `docs/visual-qa/2026-04-22-sprint8-content-expansion/01_home_light.png`
- `docs/visual-qa/2026-04-22-sprint8-content-expansion/02_library_mixed_light.png`
- `docs/visual-qa/2026-04-22-sprint8-content-expansion/03_intervention_link_only_light.png`
- `docs/visual-qa/2026-04-22-sprint8-content-expansion/04_external_handoff_light.png`
- `docs/visual-qa/2026-04-22-sprint8-content-expansion/05_reader_renderable_v2_light.png`
- `docs/visual-qa/2026-04-22-sprint8-content-expansion/06_feedback_light.png`
- `docs/visual-qa/2026-04-22-sprint8-content-expansion/07_progress_light.png`
- `docs/visual-qa/2026-04-22-sprint8-content-expansion/08_intervention_meditation_light.png`
- `docs/visual-qa/2026-04-22-sprint8-content-expansion/09_meditation_timer_light.png`
- `docs/visual-qa/2026-04-22-sprint8-content-expansion/10_settings_dark.png`
- `docs/visual-qa/2026-04-22-sprint8-content-expansion/11_library_mixed_dark.png`
- `docs/visual-qa/2026-04-22-sprint8-content-expansion/12_intervention_link_only_dark.png`
- `docs/visual-qa/2026-04-22-sprint8-content-expansion/13_external_handoff_dark.png`
- `docs/visual-qa/2026-04-22-sprint8-content-expansion/14_reader_renderable_v2_dark.png`
- `docs/visual-qa/2026-04-22-sprint8-content-expansion/15_feedback_dark.png`
- `docs/visual-qa/2026-04-22-sprint8-content-expansion/16_progress_dark.png`
- `docs/visual-qa/2026-04-22-sprint8-content-expansion/17_intervention_meditation_dark.png`
- `docs/visual-qa/2026-04-22-sprint8-content-expansion/18_meditation_timer_dark.png`
- `docs/visual-qa/2026-04-22-sprint8-content-expansion/contact_sheet.png`

## What Was Covered

- Light and Dark library surfaces with mixed shared inventory.
- Light and Dark intervention surfaces where the primary recommendation is a shared `EXTERNAL_HANDOFF` link-only item.
- Light and Dark external handoff surfaces with canonical URL display.
- Light and Dark reader surfaces for `public-domain-expansion-v2`.
- Light and Dark feedback and progress surfaces after completing/skipping the replacement session.
- Light and Dark meditation intervention and timer surfaces.

## Findings

No blocker visual issues were found.

Notes:

- The intervention remains finite: one primary recommendation plus two backups, or no extra choices when the inventory is intentionally constrained to meditation.
- Link-only items route to the external handoff surface rather than the in-app reader.
- Renderable v2 items show author-facing source labels, not provenance-heavy Project Gutenberg or Wikisource labels.
- Dark-mode screenshots use light Android system-bar icons after the theme-aware system-bar fix.
- The progress surface can show the transient "Feedback skipped" snackbar when captured immediately after skipping feedback. This is acceptable for visual QA and mirrors the real app state.

## Validation Commands

Targeted visual QA run:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools \
ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools \
ANDROID_SERIAL=emulator-5554 \
./gradlew connectedDebugAndroidTest --no-daemon \
  -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.VisualQaScreenshotTest
```

Manual artifact capture:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools \
ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools \
ANDROID_SERIAL=emulator-5554 \
./gradlew installDebug installDebugAndroidTest --no-daemon

JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools \
ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools \
ANDROID_SERIAL=emulator-5554 \
adb shell am instrument -w \
  -e class com.qualityalternative.app.VisualQaScreenshotTest \
  com.qualityalternative.app.test/androidx.test.runner.AndroidJUnitRunner

ANDROID_HOME=/opt/homebrew/share/android-commandlinetools \
ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools \
ANDROID_SERIAL=emulator-5554 \
adb exec-out run-as com.qualityalternative.app \
  tar -C files/visual-qa -cf - sprint8-content-expansion \
  | tar -C /tmp/sprint8-content-expansion-screens -xf -
```
