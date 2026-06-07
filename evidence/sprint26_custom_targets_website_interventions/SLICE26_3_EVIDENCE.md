# Sprint 26 Slice 26.3 Evidence - Chrome Verified-Host Website Intervention

## Scope

Slice 26.3 wires the Slice 26.2 website rule model into the Android interception path for Chrome only, using a verified address-bar host source. It does not claim universal URL blocking, full-path matching, browser history collection, or unsupported-browser protection.

## Implementation Summary

- Added `VerifiedBrowserHostAdapter` for Chrome package `com.android.chrome`.
- The adapter reads only whitelisted Chrome address-bar node ids (`url_bar`, `search_box_text`) with reported view ids.
- The adapter normalizes host text through the existing `WebsiteRuleNormalizer` and rejects searches, IP literals, local/private hosts, unsupported browsers, and unreadable states.
- Added `WebsiteInterceptionResolver` for enabled exact-domain and wildcard-subdomain rules.
- The resolver returns a privacy-safe target (`Chrome website`), a non-domain suppression key, and metadata with `targetType=website_domain`, `browserSupportStatus=verified_host`, and rule type only.
- Updated the AccessibilityService to process `TYPE_WINDOW_STATE_CHANGED` and `TYPE_WINDOW_CONTENT_CHANGED`, read the active Chrome window, and never reuse a prior host when the current state is unreadable.
- Updated the system intervention intent and `MainViewModel` to handle website interventions even when Chrome is not selected as a whole-app target.
- Updated open-anyway suppression to support a separate website key so website unlock does not suppress the whole browser target key.
- Added visual evidence for Soft and Firm website intervention states.

## Privacy Checks

- No raw URL, host, path, query, page title, or URL-bar string is passed from the adapter into analytics.
- `WebsiteInterceptionResolverTest.resolve_matchesEnabledRulesWithoutDomainInAnalytics` asserts matched rule metadata and suppression key do not contain the matched domain.
- `MainViewModelTest.requestSystemWebsiteInterception_opensInterventionWithoutSelectedBrowserAndKeepsDomainPrivate` asserts `INTERVENTION_SHOWN` metadata contains website target status but no domain or `https://`.
- Screenshot labels use generic `website_chrome_verified_host_*` names and do not contain test domains.

## Validation

- Unit tests: `:app:testDebugUnitTest`
  - `VerifiedBrowserHostAdapterTest`
  - `WebsiteInterceptionResolverTest`
  - `MainViewModelTest.requestSystemWebsiteInterception_opensInterventionWithoutSelectedBrowserAndKeepsDomainPrivate`
  - `MainViewModelTest.websiteOpenAnywaySuppressesWebsiteKeyWithoutSuppressingWholeBrowserTarget`
  - Existing system interception unlock regression tests
- Lint: `:app:lintDebug`
- Android visual E2E: `VisualQaScreenshotTest#captureSprint26ChromeWebsiteInterventionScreens`
  - Device: `qaApi36(AVD) - 16`
  - Emulator closed after the visual run.

## Evidence Files

- Diff: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_DIFF.patch`
- Unit/connected test XML and logcat: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3/`
- Visual screenshots: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3/sprint26-custom-targets-1780838366859/`
- Visual contact sheet: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3/sprint26_slice26_3_chrome_website_intervention_contact_sheet.png`

## Known Boundaries

- Supported browser is Chrome only for this slice.
- Full URL/path rules remain out of scope.
- Unsupported browsers, hidden/unreadable address bars, custom tabs, PWAs, and incognito states must not trigger from stale observations.
- Whole-browser app intervention remains the fallback for unsupported browsers or unreadable website states.
