# Sprint 26 Slice 26.1 R4 Validation

Date: 2026-06-07

Scope: Custom App Target Vertical only. Website/domain rules remain deferred to Slice 26.2+.

## R3 Result

- GPT Pro R3 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R3_REVIEW.md`
- SCORE: `8/10`
- VERDICT: `FAIL`
- VISUAL REVIEW: `PASS`

R3 accepted the visual flow and the custom-target intent path, but blocked release on bundle completeness and a completed-profile empty-state recovery bug.

## R4 Fixes

### Build-complete bundle blocker

R4 includes the files GPT Pro called out as missing:

- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`
- `app/proguard-rules.pro`

The R4 bundle also keeps all `app/src` production, unit-test, androidTest, resources, and assets.

### Empty completed Settings recovery

R3 found that an all-missing Portable Profile replace import correctly left selected targets empty, but Settings could not rebuild from that empty state because `MainViewModel.toggleSettingsApp()` rejected any add while the selected count stayed below `MIN_SELECTED_DISTRACTING_APPS`.

R4 changes `toggleSettingsApp()` so additions are allowed from an empty or below-minimum completed Settings state. Removals that would drop below the minimum are still blocked.

Regression coverage:

- `MainViewModelTest#accountLightReplaceImportWithAllMissingAppTargetsDoesNotSelectDefaults`
  - verifies all-missing replace import stays empty,
  - verifies selecting an eligible standard target from the empty completed Settings state is allowed and persists,
  - verifies selecting an eligible custom target from that repaired Settings state is allowed and persists.

### Setup-critical/OEM boundary

R4 keeps the production safety boundary as an explicit static denylist for known unsafe package families plus launcher/home discovery, rather than a broad OEM-prefix block.

Regression coverage:

- `InstalledAppTargetEligibilityPolicyTest#eligibilityFor_excludesKnownOemSafetyAppsWithoutBlanketOemBlock`
  - excludes `com.samsung.android.permissioncontroller`,
  - excludes `com.samsung.android.dialer`,
  - allows ordinary launchable OEM apps such as `com.samsung.android.calendar`.

This proves the implemented boundary: known OEM safety/permission/phone apps are excluded, but ordinary user-launchable OEM apps remain eligible. It does not claim exhaustive coverage for every future OEM package name.

## Fresh R4 Test Evidence

- Targeted unit rerun: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r4/unit_targeted_rerun.log`
- Unit XML reports: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r4/TEST-*.xml`
- Unit summary: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r4/unit_test_report_summary.txt`
- Unit result: `203 tests, 0 failures, 0 errors, 0 skipped`

Connected Android evidence:

- `MainActivityTest#systemInterceptionIntentShowsLiveInterventionForSelectedCustomTarget`: PASS
- `VisualQaScreenshotTest#captureSprint26CustomTargetSettingsScreens`: PASS
- Connected summary: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r4/connected_test_report_summary.txt`

Build/lint evidence:

- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r4/lint_compile_android_test_rerun.log`
- Result: `BUILD SUCCESSFUL`

Diff hygiene:

- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r4/git_diff_check.log`
- Result: empty output, command exited successfully.

## Visual Evidence

- Contact sheet: `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r4/CONTACT_SHEET.png`
- Individual R4 screenshots: `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r4/sprint26-custom-targets-1780829672518/*.png`

Screenshots cover:

1. custom app search default state,
2. self package excluded/disabled,
3. eligible installed app search,
4. selected custom app,
5. selected custom app persisted after restart,
6. custom app removed,
7. unselected no-intervention state,
8. Soft intervention via system-interception intent path,
9. Firm wait/unlock behavior,
10. Bedtime intervention.

## Emulator Cleanup

The R4 visual run used `qaTall` / `emulator-5554`. The emulator was killed after screenshots were pulled, and `adb devices` no longer listed it.

