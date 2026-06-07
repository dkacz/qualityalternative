# Sprint 26 Slice 26.3 R6 Evidence

Date: 2026-06-07

Scope: close the GPT Pro R5 evidence gaps for Chrome verified-host website interventions and resubmit Slice 26.3 for a 10/10 gate.

## GPT Pro R5 Result

- Review file: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R5_REVIEW.md`
- SCORE: `9/10`
- VERDICT: `PASS`
- VISUAL REVIEW: `PASS`
- BLOCKERS: none release-blocking.

R5 did not reach 10/10 because the package lacked fresh connected evidence for the current source snapshot, a live Chrome AccessibilityService event-to-intervention proof, connected negative evidence for unsupported/unreadable states, emulator/device/API proof, schemed Unicode URL normalization coverage, and raw `git diff --check` output.

## R6 Implementation Fixes

- Added schemed Unicode-host normalization coverage. `https://MÜNICH.example/lesen?utm=private#top` now normalizes to `xn--mnich-kva.example`.
- Increased the real Chrome accessibility snapshot scan budget from 96 to 512 nodes so the adapter can reach Chrome's toolbar when page content appears earlier in the tree.
- Added device/API/Chrome version proof to connected Chrome adapter evidence.
- Added connected negative evidence for unsupported browser package and unreadable root states.
- Added an external live-service E2E harness outside UiAutomation instrumentation to prove Chrome launch, enabled `QualityAlternativeAccessibilityService`, host read, and intervention UI from the live service path.
- Shipped raw `git diff --check` output and status.

## Automated Validation

Full unit and lint rerun:

- Command: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew --rerun-tasks :app:testDebugUnitTest :app:lintDebug`
- Console log: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r6_unit_lint_console.txt`
- Unit XML: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r6/unit_xml/`
- Unit result: `432 tests`, `0 failures`, `0 errors`, `0 skipped`.
- Lint result: `0 errors`, `46 warnings`, `6 hints`.

Fresh connected Android test and visual evidence rerun:

- Command: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.AccessibilityInterceptionTest#chromeVerifiedHostAdapterHarnessAcceptsOnlyLoadedMatchingHost,com.qualityalternative.app.AccessibilityInterceptionTest#chromeVerifiedHostAdapterRejectsSyntheticUnsupportedAndStaleStates,com.qualityalternative.app.VisualQaScreenshotTest#captureSprint26WebsiteRuleSettingsScreens,com.qualityalternative.app.VisualQaScreenshotTest#captureSprint26ChromeWebsiteInterventionScreens`
- Raw connected output: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r6/connected_debug/`
- Connected result: `4/4 passed` on `qaApi36(AVD) - 16`.
- Test methods:
  - `chromeVerifiedHostAdapterHarnessAcceptsOnlyLoadedMatchingHost`
  - `chromeVerifiedHostAdapterRejectsSyntheticUnsupportedAndStaleStates`
  - `captureSprint26WebsiteRuleSettingsScreens`
  - `captureSprint26ChromeWebsiteInterventionScreens`

External live-service E2E:

- Evidence directory: `evidence/sprint26_custom_targets_website_interventions/live_service_e2e_r6/`
- Result file: `evidence/sprint26_custom_targets_website_interventions/live_service_e2e_r6/result.txt`
- Result: `PASS`
- Proof files:
  - `dumpsys_accessibility_enabled.txt` proves the real app AccessibilityService was bound.
  - `window_live.xml` contains the live intervention text `You reached for Chrome website`.
  - `live_chrome_service_intervention.png` captures the live service-triggered intervention UI.

Diff hygiene:

- Output: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r6/git_diff_check_r6.txt`
- Status: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r6/git_diff_check_r6.status.txt`
- Result: `PASS`

## Visual Evidence

- Chrome adapter and live-service contact sheet: `evidence/sprint26_custom_targets_website_interventions/chrome_verified_host_e2e_r6_latest/sprint26_slice26_3_r6_chrome_e2e_contact_sheet.png`
- Website settings contact sheet: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3_r6/sprint26_slice26_3_r6_settings_rules_contact_sheet.png`
- Intervention state contact sheet: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3_r6/sprint26_slice26_3_r6_intervention_contact_sheet.png`

The Chrome R6 contact sheet includes:

- Chrome `example.org` non-match with no intervention,
- typed-but-not-loaded `example.com` with no intervention,
- loaded `https://example.com/` verified by adapter,
- live Chrome service-triggered website intervention,
- unsupported/unreadable negative-state proof.

## R5 Gap Closure Map

| R5 gap | R6 evidence |
| --- | --- |
| No fresh connected-test rerun | `logs-slice26_3_r6/connected_debug/` with 4/4 passed |
| No full live Chrome service path | `live_service_e2e_r6/` with bound service, UI dump, and screenshot |
| No connected unsupported/stale-state negative proof | `chromeVerifiedHostAdapterRejectsSyntheticUnsupportedAndStaleStates` and `negative_states/` evidence |
| Emulator/device/API not proven | `chrome_verified_host_e2e_r6_latest/*/chrome_verified_host_e2e.txt` records model/API/release |
| Schemed Unicode host not tested | `WebsiteRuleNormalizerTest.normalize_convertsSchemedUnicodeHostToAscii` |
| Raw `git diff --check` output missing | `logs-slice26_3_r6/git_diff_check_r6.txt` and `.status.txt` |

## Known Remaining Limits

- Sprint 26.3 remains Chrome-first. Unsupported browsers and unreadable states do not claim domain protection and fall back to whole-browser app targeting.
- Custom-tab, PWA, and incognito UI surfaces are still treated as unsupported/unreadable unless the current Chrome address-bar host can be verified through the adapter. The implementation does not cache or reuse prior hosts.
- Full URL/path rules remain out of scope for Sprint 26.
