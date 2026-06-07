# Sprint 26 Slice 26.2 R2 Evidence - Website Rule Model And Settings UI

## R1 GPT Pro Result

- R1 review: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_2_REVIEW.md`
- R1 verdict: `SCORE 7/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`
- R1 blockers fixed in R2:
  - all IP literals and all-numeric host forms are rejected, including public IPv4 and IPv6 inputs,
  - typed `*.example.com` now becomes an explicit subdomain rule with the apex toggle visible before save,
  - wildcard apex inclusion defaults to off and is saved only after the visible `Include main domain` toggle is selected,
  - website-rule count now says `enabled` instead of `active`,
  - R2 bundle includes `gradle/libs.versions.toml`,
  - R2 bundle uses sanitized Android test metadata without absolute local macOS paths.

## Automated Checks

- `:app:testDebugUnitTest --tests com.qualityalternative.app.data.WebsiteRuleNormalizerTest --tests com.qualityalternative.app.data.PreferencesSettingsRepositoryTest --tests com.qualityalternative.app.data.AccountLightProfileExporterTest --tests com.qualityalternative.app.data.AccountLightProfileImporterTest --tests com.qualityalternative.app.ui.MainViewModelTest` - PASS
- `:app:lintDebug` - PASS
- `:app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.VisualQaScreenshotTest#captureSprint26WebsiteRuleSettingsScreens` - PASS on `qaApi36(AVD) - 16`
- `git diff --check` - PASS

## New / Updated Test Coverage

- `WebsiteRuleNormalizerTest`
  - rejects public IPv4 literals (`8.8.8.8`, `1.1.1.1`),
  - rejects malformed all-numeric hosts (`999.1.1.1`, `1.2.3`),
  - rejects IPv6 literals, including bracketed URI form,
  - rejects bare user-info/email-like input and search-like text.
- `PreferencesSettingsRepositoryTest`
  - ignores stored public-IP website rules during DataStore hydration.
- `AccountLightProfileImporterTest`
  - rejects public-IP website rules during Portable Profile validation.
- `MainViewModelTest`
  - rejects public-IP input through Settings draft save,
  - proves typed `*.News.Example` saves as wildcard subdomains with `includeApex=false`,
  - proves apex inclusion is saved only after `setWebsiteRuleDraftIncludeApex(true)`.
- `VisualQaScreenshotTest#captureSprint26WebsiteRuleSettingsScreens`
  - captures public IP rejection,
  - captures typed wildcard with visible `Include main domain` toggle before save,
  - captures subdomain-only wildcard save,
  - captures visible apex toggle save,
  - captures cancel edit preserving the original rule.

## Visual Evidence

- Final R2 screenshot directory: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_r2/sprint26-custom-targets-1780835556853/`
- Final R2 contact sheet: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_r2/sprint26_slice26_2_r2_website_rules_contact_sheet.png`
- Covered states:
  - empty website rules,
  - private IP rejection,
  - public IP rejection,
  - exact-domain rule saved,
  - typed wildcard with visible apex toggle,
  - typed wildcard saved as subdomain-only,
  - visible apex toggle saved,
  - paused exact rule,
  - edit cancel keeps original rule,
  - edit save,
  - delete keeps wildcard rule,
  - browser support matrix.

## Package Hygiene

- R2 bundle includes `gradle/libs.versions.toml` plus Gradle wrapper files and app Gradle files.
- R2 bundle includes sanitized Android test metadata: `SLICE26_2_R2_ANDROID_TEST_RESULT_SANITIZED.textproto`.
- R2 bundle does not include the original unsanitized Android `test-result.textproto`.
- Release APK artifacts remain excluded because this is still a slice review before the Sprint 26 release gate.

## Emulator Hygiene

- `qaApi36` was shut down after the final visual E2E run.
