# Sprint 19 Meditation Calm Alternative Evidence

Scope: meditation must be presented as a distinct calm-reset alternative, not as a normal row inside `Other options`.

## Implementation

- `InterventionScreen` separates meditation from normal backup rows when meditation is eligible and primary content is reading.
- A new `MeditationAlternativeCard` presents meditation as a green calm-reset panel with:
  - `Calm reset` label
  - meditation title
  - quiet description
  - `Start` action
  - 1m/3m/5m/10m duration chips
- `Other options` now lists only non-meditation reading/link/file alternatives.
- Visual QA helpers now click the dedicated meditation start button when present.

## Validation

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest assembleDebugAndroidTest`: PASS
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#sprint19InterventionShowsMeditationAsCalmAlternativeWhenPrimaryIsReading`: PASS

## Visual Evidence

- `screenshots/12_meditation_calm_alternative.png`

## Notes

- The recommendation engine still treats meditation as an eligible standing alternative, preserving analytics and existing recommendation semantics.
- The user-visible intervention surface no longer renders meditation as an `Other options` backup row.
- Emulator was shut down after visual validation.
