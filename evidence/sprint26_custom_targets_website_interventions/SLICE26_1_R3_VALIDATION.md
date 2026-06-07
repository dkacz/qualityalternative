# Sprint 26 Slice 26.1 R3 Validation

Date: 2026-06-07

## Scope

Slice 26.1 implements the custom installed-app target vertical before website/domain work:

- eligible launchable installed apps can appear as custom intervention targets,
- unsafe targets are visible but disabled with a reason,
- standard suggestions stay separate from custom apps,
- selected custom packages persist through settings and restore into app settings,
- the system-interception intent path can show the existing replacement-first intervention flow for a selected custom package,
- Portable Profile export/import keeps eligible custom app package selections active on the current device and keeps missing packages inactive with warnings.

Website/domain rules remain deferred to Slice 26.2 and later.

## Prior Review Recheck

GPT Pro R1 returned `SCORE 6/10`, `VERDICT FAIL`, `VISUAL REVIEW FAIL`.

GPT Pro R2 returned `SCORE 5/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`.

R3 closes the R2 blockers as follows:

- The shipped bundle is now build-complete for this app module review: it includes all of `app/src`, not a narrow source subset.
- Portable Profile replace import now keeps selected app packages empty when all imported packages are unsupported or missing. Completed-profile import no longer hydrates unrelated default target apps into Settings/UI.
- Regression coverage was added at importer and ViewModel levels for all-missing app targets.
- Fresh unit tests were run with `--rerun-tasks`; XML reports and a summary list executed test classes/counts.
- The custom target intervention path is now exercised through `MainActivity.createSystemInterceptionIntent()` for a selected custom installed app, not only through direct `MainViewModel.requestSystemInterception()`.
- The R3 visual Soft intervention screenshot is generated from the system-interception intent path.

## Automated Validation

Passed with Homebrew JDK 17:

```bash
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH ./gradlew --rerun-tasks :app:testDebugUnitTest --tests com.qualityalternative.app.data.InstalledAppTargetEligibilityPolicyTest --tests com.qualityalternative.app.data.PreferencesSettingsRepositoryTest --tests com.qualityalternative.app.ui.MainViewModelTest --tests com.qualityalternative.app.data.AccountLightProfileExporterTest --tests com.qualityalternative.app.data.AccountLightProfileImporterTest --tests com.qualityalternative.app.interception.InterceptionTargetResolverTest
```

Result: `BUILD SUCCESSFUL`

Executed test summary:

- `AccountLightProfileExporterTest`: 10 tests, 0 failures
- `AccountLightProfileImporterTest`: 23 tests, 0 failures
- `InstalledAppTargetEligibilityPolicyTest`: 2 tests, 0 failures
- `PreferencesSettingsRepositoryTest`: 20 tests, 0 failures
- `InterceptionTargetResolverTest`: 4 tests, 0 failures
- `MainViewModelTest`: 135 tests, 0 failures
- Total: 194 tests, 0 failures

Raw logs and reports:

- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r3/unit_targeted_rerun.log`
- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r3/unit_test_report_summary.txt`
- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r3/TEST-*.xml`

Passed with Homebrew JDK 17:

```bash
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH ./gradlew --rerun-tasks :app:lintDebug :app:compileDebugAndroidTestKotlin
```

Result: `BUILD SUCCESSFUL`

Raw log:

- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r3/lint_compile_android_test_rerun.log`
- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r3/compile_android_test_after_intent_path.log`

Passed on emulator `qaTall(AVD) - 16`:

```bash
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#systemInterceptionIntentShowsLiveInterventionForSelectedCustomTarget
```

Result: `BUILD SUCCESSFUL`

Passed on emulator `qaTall(AVD) - 16`:

```bash
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.VisualQaScreenshotTest#captureSprint26CustomTargetSettingsScreens
```

Result: `BUILD SUCCESSFUL`

Connected test summary:

- `MainActivityTest#systemInterceptionIntentShowsLiveInterventionForSelectedCustomTarget`: 1 test, 0 failures
- `VisualQaScreenshotTest#captureSprint26CustomTargetSettingsScreens`: 1 test, 0 failures
- Total: 2 connected tests, 0 failures

Raw logs:

- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r3/custom_intent_path_connected.log`
- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r3/visual_connected_r3.log`
- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r3/connected_test_report_summary.txt`

Patch hygiene:

```bash
git diff --check
```

Result: pass, no whitespace errors.

Raw log:

- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r3/git_diff_check.log`

## Visual Evidence

Contact sheet:

- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r3/CONTACT_SHEET.png`

Raw screenshots:

- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r3/01_custom_app_search_empty_light.png`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r3/02_custom_app_self_excluded_light.png`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r3/03_custom_app_eligible_search_light.png`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r3/04_custom_app_selected_light.png`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r3/05_custom_app_persisted_after_restart_light.png`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r3/06_custom_app_removed_light.png`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r3/07_custom_app_unselected_no_intervention_light.png`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r3/08_custom_app_soft_intervention_light.png`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r3/09_custom_app_firm_intervention_wait_light.png`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r3/10_custom_app_bedtime_intervention_light.png`

Codex visual check: PASS. The screenshots show the custom app picker, explicit self-app exclusion, eligible app search, selected custom app state, selected-state persistence after restart, remove/unselect, a non-trigger state after removal, Soft intervention with the original app available immediately, Firm intervention with a five-second wait, and Bedtime intervention with a sixty-second emergency wait. In R3, the Soft intervention screenshot is reached by launching `MainActivity.createSystemInterceptionIntent()` for the selected custom package.

## Review Notes

- Package visibility is limited to launchable/home queries in `AndroidManifest.xml`.
- Excluded packages include Quality Alternative itself, launcher/home packages, Settings/permission controllers, phone/emergency packages, installers/app stores, DocumentsUI/file pickers, and system-critical packages.
- Custom app support does not change website/domain scope; no URL matching or website blocking is introduced in this slice.
- Remote analytics fields were not expanded in this slice.
- The R3 bundle includes all app source under `app/src` to avoid partial-bundle audit gaps.
