# Sprint 26 Slice 26.5 R2 Evidence - Bedtime And Supported Website Target Integration

Date: 2026-06-07

## Scope

Slice 26.5 verifies that Bedtime behavior is applied consistently to the target classes introduced in Sprint 26:

- selected custom installed-app targets,
- supported Chrome website/domain targets reached through the verified-host website-intervention path,
- existing replacement alternatives, especially reading and meditation,
- emergency unlock behavior without the normal `Pause 15 min` escape during Bedtime.

R1 GPT Pro review returned `SCORE 8/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`. The blocker was evidentiary: the bundle proved the Bedtime website UI after a direct website intent, but did not prove that the Bedtime website emergency unlock was reachable only through the verified-host path, and the bundle omitted source needed to audit the intent boundary.

R2 GPT Pro review returned `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`. No blockers, bundle gaps, privacy issues, or package hygiene issues remain.

## R2 Fixes

- Added `MainActivityTest.forgedWebsiteInterceptionIntentWithoutLaunchTokenIsIgnored`.
  - Starts exported `MainActivity` with the system-intervention action and website extras but without the per-process launch token.
  - Proves the app stays on Home and does not show `Chrome website` or Bedtime website intervention copy.
- Added `AccessibilityInterceptionTest.seedBedtimeWebsiteRuleSelectionForExternalLiveServiceE2e`.
  - Seeds onboarding, exact-domain website rule, and all-day Bedtime for an external shell harness.
  - Leaves seeded state in place when run with `preserve_state=true`.
- Added external live-service E2E evidence outside instrumentation UiAutomation.
  - Installs app/test APKs, seeds all-day Bedtime website settings, binds `QualityAlternativeAccessibilityService`, launches real Chrome on the matching host, and waits for the app UI.
  - Result: live Chrome verified-host path opened `Bedtime is protecting sleep from Chrome website`.
  - The UI dump proves `Breathe before emergency unlock`, `Calm reset`, and `Chrome website` are visible, while `Pause 15 min`, `example.com`, and `https://` are absent.
- Review bundle R2 includes `MainActivity.kt`, `AndroidManifest.xml`, `AnalyticsPrivacyGuard.kt`, prior Slice 26.1 R4 validation/contact sheet/logs, scrubbed lint artifacts, and a scrubbed copy of the R1 GPT Pro review.

## Automated Validation

Full unit/lint validation:

- Scrubbed command output: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_5_r2/unit_lint_console.scrubbed.txt`
- Unit XML: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_5_r2/unit_xml/`
- Unit totals: 443 tests, 0 failures, 0 errors, 0 skipped.
- Scrubbed lint reports: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_5_r2/lint-results-debug.scrubbed.{txt,xml}`
- Lint result: PASS, 0 errors.

Connected Android validation:

- Command result: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_5_r2/connected_debug/`
- Connected XML: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_5_r2/connected_debug/TEST-qaApi36(AVD) - 16-_app-.xml`
- Connected result: 2 tests, 0 failures, 0 errors, 0 skipped.
- Tests:
  - `MainActivityTest#forgedWebsiteInterceptionIntentWithoutLaunchTokenIsIgnored`
  - `VisualQaScreenshotTest#captureSprint26ChromeWebsiteInterventionScreens`

External live-service E2E:

- Evidence directory: `evidence/sprint26_custom_targets_website_interventions/live_service_e2e_slice26_5_r2/`
- Result file: `evidence/sprint26_custom_targets_website_interventions/live_service_e2e_slice26_5_r2/result.txt`
- Result: PASS.
- Proof files:
  - `seed_instrumentation.log` proves the all-day Bedtime website rule seed completed.
  - `dumpsys_accessibility_enabled.txt` proves the Quality Alternative accessibility service was bound.
  - `window_live.xml` proves the live service-triggered app UI text.
  - `live_chrome_bedtime_service_intervention.png` captures the live Chrome service-triggered Bedtime website UI.

Diff hygiene:

- `git diff --check`: PASS.
- Output: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_5_r2/git_diff_check.txt`
- Status: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_5_r2/git_diff_check.status.txt`

Emulator cleanup:

- The `qaApi36` emulator was shut down after connected and live-service validation.
- Shutdown proof: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_5_r2/adb_devices_after_emulator_shutdown.txt`

## Visual Evidence

R2 visual directory:

- `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_5_r2/`

R2 contact sheet:

- `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_5_r2/sprint26_slice26_5_r2_bedtime_website_live_contact_sheet.png`

Captured states:

- `23_website_chrome_verified_host_soft_intervention_light.png`
  - Shows `You reached for Chrome website`, meditation alternative, bounded reading alternatives, `Pause 15 min`, and immediate open behavior.
- `24_website_chrome_verified_host_firm_wait_light.png`
  - Shows `Chrome website`, meditation alternative, bounded alternatives, normal `Pause 15 min`, and five-second Firm wait state.
- `25_website_chrome_bedtime_emergency_unlock_light.png`
  - Shows direct-intent Bedtime website UI after the token-bound internal entrypoint.
  - Shows `Bedtime is protecting sleep from Chrome website`, no raw domain/host/path, meditation, `Quiet alternatives`, `Breathe before emergency unlock 60s`, and no `Pause 15 min`.
- `live_chrome_bedtime_service_intervention.png`
  - Shows the external live Chrome AccessibilityService path reaching `Bedtime is protecting sleep from Chrome website`.
  - Shows meditation and quiet alternatives.
  - Shows the emergency breath wait.
  - Does not show `Pause 15 min` or the raw matched domain.

## Package Hygiene

- R2 preserves the original R1 review output at `GPT_PRO_SLICE26_5_REVIEW.md` and ships `GPT_PRO_SLICE26_5_REVIEW_SCRUBBED_FOR_R2.md` in the external review bundle to avoid repeating the absolute-path leakage that R1 flagged.
- R2 review bundle must include full source needed to audit the path: `MainActivity.kt`, `AndroidManifest.xml`, `QualityAlternativeAccessibilityService.kt`, `VerifiedBrowserHostAdapter.kt`, `WebsiteInterceptionResolver.kt`, `MainViewModel.kt`, `QualityAlternativeApp.kt`, and `AnalyticsPrivacyGuard.kt`.
- R2 review bundle includes the prior Slice 26.1 R4 artifacts referenced by current acceptance: validation summary, contact sheet, and raw logs.
- R2 review bundle uses scrubbed lint text/XML artifacts and a scrubbed R1 review copy to avoid leaking absolute local paths into the external review package.
- R2 review bundle excludes the Chrome shell launch log and empty binary result directory; the auditable live-service proof is the seed log, accessibility service dump, UI dump, result file, and screenshot.
- Earlier failed/intermediate visual attempts remain outside the R2 canonical screenshot directory.
