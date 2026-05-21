# Sprint 24 Bedtime Hard Ban Evidence

## Scope

Adds an opt-in Bedtime sleep lock. During the configured bedtime window, intercepted apps show a calm hard-ban intervention:

- Reading and meditation alternatives remain available.
- Backup alternatives remain finite and visible as quiet alternatives.
- `Pause 15 min` is hidden because bedtime is already the active delay condition.
- Opening the original app requires a visible one-minute emergency breath.
- Bedtime settings persist locally and in Portable Profile export/import.

## Visual Evidence

- `visual_e2e/01_settings_bedtime_enabled.png` - Settings Bedtime section enabled, active, and showing an all-day deterministic test schedule.
- `visual_e2e/02_intervention_bedtime_hard_ban_alternatives.png` - Bedtime intervention with primary reading, meditation, quiet alternatives, and disabled emergency unlock countdown.

## Validation

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest compileDebugAndroidTestKotlin`
  - Result: PASS
  - Log: `gradle_unit_compile.log`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.interception.InterceptionRuntimeGateTest' --tests 'com.qualityalternative.app.ui.MainViewModelTest.bedtimeModeKeepsAlternativesButRequiresOneMinuteEmergencyUnlock' --tests 'com.qualityalternative.app.ui.MainViewModelTest.bedtimeModeIgnoresPreExistingNormalOpenAnywaySuppression'`
  - Result: PASS
  - Log: `gradle_r1_blocker_regression.log`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest compileDebugAndroidTestKotlin`
  - Result after GPT Pro R1 blocker fix: PASS
  - Log: `gradle_unit_compile_r2.log`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.interception.InterceptionRuntimeGateTest' --tests 'com.qualityalternative.app.ui.MainViewModelTest.bedtimeModeIgnoresPreExistingNormalOpenAnywaySuppression' --tests 'com.qualityalternative.app.ui.MainViewModelTest.openAnywayRechecksBedtimeWhenNormalInterventionCrossesIntoBedtimeWindow' --tests 'com.qualityalternative.app.ui.MainViewModelTest.bedtimeModeKeepsAlternativesButRequiresOneMinuteEmergencyUnlock'`
  - Result: PASS
  - Log: `gradle_r2_blocker_regression.log`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest compileDebugAndroidTestKotlin`
  - Result after GPT Pro R2 blocker fix: PASS
  - Log: `gradle_unit_compile_r3.log`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.interception.InterceptionRuntimeGateTest' --tests 'com.qualityalternative.app.ui.MainViewModelTest.bedtimeModeIgnoresPreExistingNormalOpenAnywaySuppression' --tests 'com.qualityalternative.app.ui.MainViewModelTest.openAnywayRechecksBedtimeWhenNormalInterventionCrossesIntoBedtimeWindow' --tests 'com.qualityalternative.app.ui.MainViewModelTest.openAnywayRechecksBedtimeEvenAfterSettingsEmissionUpdatesGlobalBedtimeState' --tests 'com.qualityalternative.app.ui.MainViewModelTest.bedtimeModeKeepsAlternativesButRequiresOneMinuteEmergencyUnlock'`
  - Result: PASS
  - Log: `gradle_r3_blocker_regression.log`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest compileDebugAndroidTestKotlin`
  - Result after GPT Pro R3 blocker fix: PASS
  - Log: `gradle_unit_compile_r4.log`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.interception.InterceptionRuntimeGateTest' --tests 'com.qualityalternative.app.ui.MainViewModelTest.bedtimeModeIgnoresPreExistingNormalOpenAnywaySuppression' --tests 'com.qualityalternative.app.ui.MainViewModelTest.openAnywayRechecksBedtimeWhenNormalInterventionCrossesIntoBedtimeWindow' --tests 'com.qualityalternative.app.ui.MainViewModelTest.openAnywayRechecksBedtimeEvenAfterSettingsEmissionUpdatesGlobalBedtimeState' --tests 'com.qualityalternative.app.ui.MainViewModelTest.delayFor15MinutesIsNotAvailableAfterSettingsEmissionConvertsInterventionToBedtime' --tests 'com.qualityalternative.app.ui.MainViewModelTest.bedtimeModeKeepsAlternativesButRequiresOneMinuteEmergencyUnlock'`
  - Result: PASS
  - Log: `gradle_r4_blocker_regression.log`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest compileDebugAndroidTestKotlin`
  - Result after GPT Pro R4 blocker fix: PASS
  - Log: `gradle_unit_compile_r5.log`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.interception.ForegroundAppDetectionPolicyTest' --tests 'com.qualityalternative.app.interception.InterceptionRuntimeGateTest' --tests 'com.qualityalternative.app.ui.MainViewModelTest.bedtimeBoundaryRefreshConvertsIdleNormalInterventionWithoutSettingsEmission' --tests 'com.qualityalternative.app.ui.MainViewModelTest.delayFor15MinutesIsNotAvailableAfterSettingsEmissionConvertsInterventionToBedtime' --tests 'com.qualityalternative.app.ui.MainViewModelTest.openAnywayRechecksBedtimeEvenAfterSettingsEmissionUpdatesGlobalBedtimeState' --tests 'com.qualityalternative.app.ui.MainViewModelTest.openAnywayRechecksBedtimeWhenNormalInterventionCrossesIntoBedtimeWindow' --tests 'com.qualityalternative.app.ui.MainViewModelTest.bedtimeModeIgnoresPreExistingNormalOpenAnywaySuppression' --tests 'com.qualityalternative.app.ui.MainViewModelTest.bedtimeModeKeepsAlternativesButRequiresOneMinuteEmergencyUnlock'`
  - Result: PASS
  - Log: `gradle_r5_blocker_regression.log`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest compileDebugAndroidTestKotlin`
  - Result after GPT Pro R5 blocker fix: PASS
  - Log: `gradle_unit_compile_r6.log`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.interception.ForegroundAppDetectionPolicyTest' --tests 'com.qualityalternative.app.interception.InterceptionRuntimeGateTest' --tests 'com.qualityalternative.app.ui.MainViewModelTest.bedtimeBoundaryRefreshConvertsIdleNormalInterventionWithoutSettingsEmission' --tests 'com.qualityalternative.app.ui.MainViewModelTest.delayFor15MinutesIsNotAvailableAfterSettingsEmissionConvertsInterventionToBedtime' --tests 'com.qualityalternative.app.ui.MainViewModelTest.openAnywayRechecksBedtimeEvenAfterSettingsEmissionUpdatesGlobalBedtimeState' --tests 'com.qualityalternative.app.ui.MainViewModelTest.openAnywayRechecksBedtimeWhenNormalInterventionCrossesIntoBedtimeWindow' --tests 'com.qualityalternative.app.ui.MainViewModelTest.bedtimeModeIgnoresPreExistingNormalOpenAnywaySuppression' --tests 'com.qualityalternative.app.ui.MainViewModelTest.bedtimeModeKeepsAlternativesButRequiresOneMinuteEmergencyUnlock'`
  - Result: PASS
  - Log: `gradle_r6_blocker_regression.log`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew testDebugUnitTest compileDebugAndroidTestKotlin`
  - Result after GPT Pro R6 blocker fix: PASS
  - Log: `gradle_unit_compile_r7.log`
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#bedtimeModeShowsCalmHardBanWithAlternativesAndEmergencyWait`
  - Result: PASS
  - XML: `connected_bedtime_e2e.xml`
  - Logcat: `connected_bedtime_e2e_logcat.txt`

## GPT Pro R1 Recheck

- R1 score was `8/10`, `VERDICT: REVISE`, `VISUAL REVIEW: PASS`.
- The sole blocker was that a non-Bedtime Open Anyway quiet-unlock window could cross into active Bedtime and suppress the Bedtime hard-ban screen.
- Fix: runtime suppressions now carry whether they are allowed during Bedtime. AccessibilityService and MainViewModel compute active Bedtime before honoring suppression; active Bedtime ignores normal suppressions and honors only suppressions created by a Bedtime emergency unlock.
- Regression tests cover the gate-level rule and the system-interception ViewModel path.

## GPT Pro R2 Recheck

- R2 score was `8/10`, `VERDICT: REVISE`, `VISUAL REVIEW: PASS`.
- R2 confirmed R1 was closed, then found a separate boundary case: a normal intervention rendered before Bedtime could be acted on after Bedtime starts.
- Fix: `openAnyway()` now recomputes active Bedtime at click time. If Bedtime became active after the intervention was shown, it converts the current intervention into Bedtime mode, keeps alternatives visible, sets a fresh one-minute emergency unlock wait, and returns `false`.
- Fix: AccessibilityService now uses a single immutable volatile settings snapshot so selected apps and Bedtime fields cannot be read from different settings emissions.
- Regression tests cover Soft and Firm interventions crossing from 21:59 to 22:01 before `Open anyway` is clicked.

## GPT Pro R3 Recheck

- R3 score was `8/10`, `VERDICT: REVISE`, `VISUAL REVIEW: PASS`.
- R3 confirmed R1 and R2 were closed, then found that a Settings emission during an existing pre-Bedtime intervention could update global `isBedtimeActive` before the click-time conversion ran.
- Fix: `isBedtimeActive` remains the global schedule status for Settings, but a new `currentInterventionBedtimeEnforced` field is the per-intervention enforcement marker. The UI, analytics, unlock blocking, Firm completion, and runtime suppression now use the per-intervention marker.
- Regression tests cover Soft and Firm interventions that cross into Bedtime, emit settings from the intervention, and then try `Open anyway`.

## GPT Pro R4 Recheck

- R4 score was `8/10`, `VERDICT: REVISE`, `VISUAL REVIEW: PASS`.
- R4 confirmed the previous ViewModel Open Anyway blockers were closed, then found service duplicate-detection ordering and stale `Pause 15 min` actionability during a pre-Bedtime intervention.
- Fix: AccessibilityService now computes Bedtime and checks the Bedtime-aware runtime suppression gate before foreground duplicate detection.
- Fix: `delayFor15Minutes()` uses `nowProvider()`, calls the shared Bedtime enforcement helper, and refuses normal delay while the current intervention is Bedtime-enforced.
- Fix: settings emissions now proactively call the shared Bedtime enforcement helper, so stale pre-Bedtime intervention UI converts to the Bedtime emergency gate before the user clicks `Open anyway` or `Pause 15 min`.
- Regression tests cover the settings-emission conversion plus programmatic `delayFor15Minutes()` attempt.

## GPT Pro R5 Recheck

- R5 score was `8/10`, `VERDICT: REVISE`, `VISUAL REVIEW: PASS`.
- R5 confirmed the runtime-gate, Open Anyway, global-state, and settings-emission `Pause 15 min` blockers were closed, then found two remaining boundary issues:
  - foreground duplicate suppression was still unaware of Bedtime status and could suppress the first active-Bedtime event after a pre-Bedtime same-package foreground event;
  - a pre-Bedtime normal intervention converted only on settings emission or user action, not on a pure clock transition while the screen sat idle.
- Fix: `ForegroundAppDetectionPolicy.shouldLog()` now accepts `bedtimeActive`, remembers whether the previous same-package foreground event was active-Bedtime, and does not suppress the boundary-crossing active-Bedtime event.
- Fix: `QualityAlternativeAccessibilityService` passes the computed active Bedtime status into foreground duplicate detection.
- Fix: the ViewModel delay refresh ticker also calls `refreshBedtimeInterventionBoundary()`, a public wrapper around the shared Bedtime enforcement helper, so an already displayed normal intervention converts to Bedtime mode when the configured window becomes active.
- Regression tests cover the service duplicate boundary and an idle pre-Bedtime intervention crossing into Bedtime without settings emission or user click.

## GPT Pro R6 Recheck

- R6 score was `8/10`, `VERDICT: REVISE`, `VISUAL REVIEW: PASS`.
- R6 confirmed all R1-R5 blockers were closed, then found one narrower source-proven issue: active-Bedtime same-package duplicate de-noising could still act as a launch gate after intervention abandonment even without a legitimate emergency unlock.
- Fix: `ForegroundAppDetectionPolicy.shouldLog(..., bedtimeActive = true)` now does not suppress same-package duplicates during active Bedtime. The runtime gate is the only Bedtime quiet-open mechanism; it still honors only suppressions created after a legitimate Bedtime emergency unlock.
- Regression tests cover both non-Bedtime duplicate suppression and active-Bedtime duplicate allowance.

## GPT Pro R7 Final Gate

- R7 score was `10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`.
- R7 rechecked R1-R6 blockers and reported `FRESH FINDINGS: None`.
- The review is preserved at `pro_review_harvest_r7/Adversarial_Audit_R7_Review.md`.

## Notes

- Emulator was shut down after screenshot capture and connected E2E validation.
- `PATCH.diff` contains the implementation diff used for GPT Pro review.
