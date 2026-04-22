# Visual QA: Content Display Baseline

Status: `pass with no blocker findings`

Date: `2026-04-22`

Device: `qaApi36` Android emulator

Resolution: `1080x2400`

Branch: `codex/meditation-timer-replacement`

## Scope

This run checks whether the current content and replacement surfaces display cleanly on emulator after Sprint 7, especially the new `Attention Classics v1` renderable content and the 3-minute meditation timer in Light and Dark modes.

Covered surfaces:

- Home
- Library with `Attention Classics v1`
- Intervention card
- In-app reader
- Feedback
- Progress
- Settings
- Meditation timer
- Dark-mode home, intervention, reader, and meditation timer

Screenshots:

- `docs/visual-qa/2026-04-22-content-display/01_home_light.png`
- `docs/visual-qa/2026-04-22-content-display/02_library_attention_light.png`
- `docs/visual-qa/2026-04-22-content-display/03_intervention_light.png`
- `docs/visual-qa/2026-04-22-content-display/04_reader_attention_light.png`
- `docs/visual-qa/2026-04-22-content-display/05_feedback_light.png`
- `docs/visual-qa/2026-04-22-content-display/06_progress_light.png`
- `docs/visual-qa/2026-04-22-content-display/07_intervention_meditation_light.png`
- `docs/visual-qa/2026-04-22-content-display/08_meditation_timer_light.png`
- `docs/visual-qa/2026-04-22-content-display/09_settings_dark.png`
- `docs/visual-qa/2026-04-22-content-display/10_home_dark.png`
- `docs/visual-qa/2026-04-22-content-display/11_intervention_dark.png`
- `docs/visual-qa/2026-04-22-content-display/12_reader_attention_dark.png`
- `docs/visual-qa/2026-04-22-content-display/13_intervention_meditation_dark.png`
- `docs/visual-qa/2026-04-22-content-display/14_meditation_timer_dark.png`
- `docs/visual-qa/2026-04-22-content-display/contact_sheet.png`

## Automated Harness

Added `VisualQaScreenshotTest` as a repeatable instrumentation screenshot harness.

The test:

- seeds `attention-classics-v1` as the selected pack
- launches the normal home flow
- captures Light screenshots
- launches a fixture system intervention
- accepts a renderable recommendation
- verifies the reader shows an `Attention Classics v1` title
- verifies the reader shows an author-facing label rather than `Project Gutenberg`
- captures feedback and progress
- seeds a meditation-only fixture setup
- captures the meditation intervention and timer in Light mode
- switches to Dark mode
- repeats home, intervention, reader, meditation intervention, and meditation timer screenshots in Dark mode

## Visual Findings

Pass:

- `Attention Classics v1` content appears in the Library.
- Intervention displays a finite set: one primary recommendation plus two backups.
- Renderable content opens in the in-app reader.
- Reader shows author-facing source labels and topic metadata.
- Reader body content is scrollable and not visibly truncated at the first screen.
- Light and Dark modes are both legible.
- Feedback and progress surfaces remain visually coherent.
- The 3-minute meditation timer displays as a finite replacement surface, not a content feed.
- Meditation timer screens render clearly in both Light and Dark modes.

Fix made during this QA pass:

- Compact metadata rows now constrain long author/topic strings to one or two lines with ellipsis instead of awkward wrapping. This improves Library and backup-row readability without changing content metadata.
- The in-app reader no longer shows the dashed placeholder illustration when an item has no `imageAssetPath`. This keeps image-less readings from looking like unfinished mockups.
- Feedback fit chips now use a shorter `Great fit` label and one-line chip text so the three answer buttons keep a balanced height.
- The 3-minute meditation timer now has its own visual QA coverage in Light and Dark mode.

Refresh after placeholder and timer changes:

- Re-ran the screenshot harness on `qaApi36`.
- Replaced the feedback, reader, meditation timer, and contact-sheet artifacts so the baseline reflects the no-placeholder reader layout, one-line feedback chips, and timer replacement screens.

Non-blocker note:

- Progress screenshot can show a transient snackbar after feedback skip. It does not block the core content display, but future visual baselines may prefer a screenshot state without transient messages.

## Validation Commands

Targeted screenshot harness:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools \
ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools \
ANDROID_SERIAL=emulator-5554 \
./gradlew connectedDebugAndroidTest --no-daemon \
  -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.VisualQaScreenshotTest
```

Manual screenshot artifact capture:

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

JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
ANDROID_HOME=/opt/homebrew/share/android-commandlinetools \
ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools \
adb exec-out run-as com.qualityalternative.app \
  tar -C files/visual-qa -cf - content-display \
  | tar -C visual-qa -xf -
```

## Result

The current content display baseline is acceptable for continuing into Sprint 8 content expansion.

Before any external release that includes much larger inventory, rerun this harness after adding shared link-only items and renderable pack v2.
