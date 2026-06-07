# Sprint 26 Slice 26.1 R2 Validation

Date: 2026-06-07

## Scope

Slice 26.1 implements the custom installed-app target vertical before website/domain work:

- eligible launchable installed apps can appear as custom intervention targets,
- unsafe targets are visible but disabled with a reason,
- standard suggestions stay separate from custom apps,
- selected custom packages persist through settings and restore into app settings,
- the AccessibilityService resolver can match a selected custom foreground package through the existing replacement-first intervention flow,
- Portable Profile export/import keeps eligible custom app package selections active on the current device and keeps missing packages inactive with warnings.

Website/domain rules remain deferred to Slice 26.2 and later.

## R1 Blocker Recheck

GPT Pro R1 returned `SCORE 6/10`, `VERDICT FAIL`, `VISUAL REVIEW FAIL`.

R2 closes the R1 blockers as follows:

- Default intervention mode is now `SOFT` in `UserModels.kt`. Tests that previously expected a Firm default now expect Soft, and Firm-only flows explicitly select `InterventionMode.FIRM`.
- Setup-critical file/document pickers are excluded by `InstalledAppTargetEligibilityPolicy`, including `com.android.documentsui`, `com.google.android.documentsui`, and `com.google.android.apps.docs`, with disabled-row copy explaining that file/profile flows must stay available.
- Eligibility policy has direct unit coverage for self, launcher/home, Settings/permission, phone/emergency, installer/app-store, system-critical, and DocumentsUI/setup-critical packages.
- Visual evidence now covers persistence after restart, remove/unselect, unselected app non-trigger, Soft, Firm, and Bedtime variants.
- The R2 bundle includes the previously missing Portable Profile implementation, `SupportedCatalog`, `MainActivity`, analytics model/tracker files, Gradle files, raw logs, and R2 visual evidence.

## Automated Validation

Passed with Homebrew JDK 17:

```bash
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH ./gradlew :app:testDebugUnitTest --tests com.qualityalternative.app.data.InstalledAppTargetEligibilityPolicyTest --tests com.qualityalternative.app.data.PreferencesSettingsRepositoryTest --tests com.qualityalternative.app.ui.MainViewModelTest --tests com.qualityalternative.app.data.AccountLightProfileExporterTest --tests com.qualityalternative.app.data.AccountLightProfileImporterTest --tests com.qualityalternative.app.interception.InterceptionTargetResolverTest
```

Result: `BUILD SUCCESSFUL`

Raw log:

- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r2/unit_targeted.log`

Passed with Homebrew JDK 17:

```bash
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH ./gradlew :app:lintDebug :app:compileDebugAndroidTestKotlin
```

Result: `BUILD SUCCESSFUL`

Raw log:

- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r2/lint_compile_android_test.log`

Passed on emulator `qaTall(AVD) - 16`:

```bash
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.VisualQaScreenshotTest#captureSprint26CustomTargetSettingsScreens
```

Result: `BUILD SUCCESSFUL`

Raw log:

- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r2/visual_connected.log`

DocumentsUI / setup-critical package check on the R2 visual emulator:

- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r2/documentsui_package_check.log`

Patch hygiene:

```bash
git diff --check
```

Result: pass, no whitespace errors.

Raw log:

- `evidence/sprint26_custom_targets_website_interventions/logs-slice26_1_r2/git_diff_check.log`

## Visual Evidence

Contact sheet:

- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r2/CONTACT_SHEET.png`

Raw screenshots:

- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r2/01_custom_app_search_empty_light.png`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r2/02_custom_app_self_excluded_light.png`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r2/03_custom_app_eligible_search_light.png`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r2/04_custom_app_selected_light.png`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r2/05_custom_app_persisted_after_restart_light.png`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r2/06_custom_app_removed_light.png`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r2/07_custom_app_unselected_no_intervention_light.png`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r2/08_custom_app_soft_intervention_light.png`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r2/09_custom_app_firm_intervention_wait_light.png`
- `evidence/sprint26_custom_targets_website_interventions/screenshots-slice26_1_r2/10_custom_app_bedtime_intervention_light.png`

Codex visual check: PASS. The screenshots show the custom app picker, explicit self-app exclusion, eligible app search, selected custom app state, selected-state persistence after restart, remove/unselect, a non-trigger state after removal, Soft intervention with the original app available immediately, Firm intervention with a five-second wait, and Bedtime intervention with a sixty-second emergency wait.

## Review Notes

- Package visibility is limited to launchable/home queries in `AndroidManifest.xml`.
- Excluded packages include Quality Alternative itself, launcher/home packages, Settings/permission controllers, phone/emergency packages, installers/app stores, DocumentsUI/file pickers, and system-critical packages.
- Custom app support does not change website/domain scope; no URL matching or website blocking is introduced in this slice.
- Remote analytics fields were not expanded in this slice.
