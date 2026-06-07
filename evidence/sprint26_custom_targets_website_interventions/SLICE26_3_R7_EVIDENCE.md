# Sprint 26 Slice 26.3 R7 Evidence

Date: 2026-06-07

Scope: close the GPT Pro R6 blocker for package-authenticated Chrome verified-host website interventions.

## GPT Pro R6 Result

- Review file: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R6_REVIEW.md`
- SCORE: `9/10`
- VERDICT: `FAIL`
- VISUAL REVIEW: `PASS`
- Blocker: the bundle did not prove stale active-window/package-mismatch safety at the same evidentiary level as the positive Chrome live-service path.

Minimum requested fix from R6:

- Add package identity to the verified browser snapshot path.
- Require the verified address-bar node or root to belong to `com.android.chrome`.
- Ship a connected negative test proving that a Chrome event cannot verify a host from a stale, unreadable, or non-Chrome active window.

## R7 Implementation Fixes

- `BrowserNodeSnapshot` now carries `packageName`.
- `AccessibilityNodeInfo.toBrowserNodeSnapshot()` captures `AccessibilityNodeInfo.packageName` for root and child nodes.
- `VerifiedBrowserHostAdapter.readHostFromSnapshot()` now requires the snapshot root package to equal the supported browser package.
- Address-bar candidate nodes must also carry the supported browser package, not merely a Chrome-looking `viewIdResourceName`.
- Added unit coverage proving Chrome-looking address resource names are rejected when the root package or address node package is non-Chrome.
- Expanded connected negative evidence to cover:
  - unsupported browser package,
  - null unreadable root,
  - package-mismatched stale root,
  - package-mismatched address node,
  - hidden address node,
  - focused editable omnibox,
  - no prior-host cache.

## Automated Validation

Full unit and lint rerun:

- Command: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew --rerun-tasks :app:testDebugUnitTest :app:lintDebug`
- Console log: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r7_unit_lint_console.txt`
- Unit XML: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r7/unit_xml/`
- Unit result: `433 tests`, `0 failures`, `0 errors`, `0 skipped`.
- Lint result: `0 errors`, `46 warnings`, `6 hints`.

Fresh connected Android test and visual evidence rerun:

- Command: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.AccessibilityInterceptionTest#chromeVerifiedHostAdapterHarnessAcceptsOnlyLoadedMatchingHost,com.qualityalternative.app.AccessibilityInterceptionTest#chromeVerifiedHostAdapterRejectsSyntheticUnsupportedAndStaleStates,com.qualityalternative.app.VisualQaScreenshotTest#captureSprint26WebsiteRuleSettingsScreens,com.qualityalternative.app.VisualQaScreenshotTest#captureSprint26ChromeWebsiteInterventionScreens`
- Raw connected output: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r7/connected_debug/`
- Connected result: `4/4 passed` on `qaApi36(AVD) - 16`.
- Test methods:
  - `chromeVerifiedHostAdapterHarnessAcceptsOnlyLoadedMatchingHost`
  - `chromeVerifiedHostAdapterRejectsSyntheticUnsupportedAndStaleStates`
  - `captureSprint26WebsiteRuleSettingsScreens`
  - `captureSprint26ChromeWebsiteInterventionScreens`

External live-service E2E after the package-authentication change:

- Evidence directory: `evidence/sprint26_custom_targets_website_interventions/live_service_e2e_r7/`
- Result: `PASS`
- Proof files:
  - `dumpsys_accessibility_after_launch.txt` shows `Quality Alternative Foreground Detection` in bound services.
  - `window_live.xml` contains `You reached for Chrome website` and `Open Chrome website`.
  - `live_chrome_service_intervention.png` captures the live service-triggered intervention UI.

Diff hygiene:

- Output: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r7/git_diff_check_r7.txt`
- Status: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r7/git_diff_check_r7.status.txt`
- Result: `PASS`

## Visual Evidence

- Chrome adapter, package-mismatch negative, and live-service contact sheet: `evidence/sprint26_custom_targets_website_interventions/chrome_verified_host_e2e_r7_latest/sprint26_slice26_3_r7_chrome_e2e_contact_sheet.png`
- Website settings contact sheet: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3_r7/sprint26_slice26_3_r7_settings_rules_contact_sheet.png`
- Intervention state contact sheet: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3_r7/sprint26_slice26_3_r7_intervention_contact_sheet.png`

## R6 Blocker Closure Map

| R6 blocker item | R7 closure |
| --- | --- |
| Snapshot lacks package identity | `BrowserNodeSnapshot.packageName` added and populated from `AccessibilityNodeInfo.packageName` |
| Chrome-looking address nodes could be event-package-authenticated only | Adapter requires root package and address-node package to match `com.android.chrome` |
| No package-mismatched negative test | Unit test and connected negative test reject package-mismatched stale root and address node |
| Positive live service might regress after package-auth | R7 reran external live-service E2E and passed |
| R6 visual review was PASS | R7 reran Settings and intervention screenshots; visual contact sheets remain PASS-ready |

## Known Remaining Limits

- Sprint 26.3 remains Chrome-first and verified-host only.
- Full URL/path rules, universal URL blocking, browser extensions, VPN/DNS/proxy filtering, packet inspection, and browsing-history collection remain out of scope.
- Custom-tab, PWA, and incognito surfaces remain unsupported/unreadable unless the active Chrome address-bar host is package-authenticated and visible through the adapter.
