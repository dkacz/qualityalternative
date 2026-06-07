# Sprint 26 Slice 26.3 R2 Evidence - Chrome Verified-Host Website Intervention

## R1 GPT Pro Result

- R1 review: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_REVIEW.md`
- R1 result: `SCORE 7/10`, `VERDICT FAIL`, `VISUAL REVIEW PASS`
- Main blockers fixed in R2:
  - stale Settings copy still saying Chrome support was "next",
  - website Open Anyway suppression could stop whole-browser Chrome fallback,
  - visual/evidence did not exercise real Chrome host reading,
  - adapter snapshot lacked hidden/currentness fields,
  - adapter scanned too shallowly for the real Chrome toolbar hierarchy,
  - lint artifact and Chrome package/version evidence were missing.

## R2 Implementation Fixes

- Updated Settings website-rule copy and browser support matrix:
  - Chrome now says domain rules are supported when the current Chrome host is readable through the verified-host adapter.
  - Other browsers remain whole-browser app-target fallback.
  - Full-path and universal URL blocking remain explicitly out of scope.
- Added `BrowserNodeSnapshot.visibleToUser`, `focused`, and `editable`.
- Real AccessibilityNodeInfo snapshots now copy text only from visible nodes.
- Address-bar verification rejects hidden nodes and focused editable omnibox states, which covers typed-but-not-loaded input.
- Increased the adapter snapshot depth from 8 to 14 after the real Chrome harness exposed that `com.android.chrome:id/url_bar` is deeper than the original cap.
- Added `AccessibilityInterceptionPlanner` and changed `QualityAlternativeAccessibilityService` so only a suppressed website target falls through to whole-browser app target evaluation; handled/duplicate/not-ready website states do not double-trigger.

## R2 Chrome Verified-Host Evidence

- Real Chrome package: `com.android.chrome`
- Real Chrome version: recorded in `chrome_verified_host_e2e_r2_latest/sprint26-chrome-verified-host-1780841890430/chrome_verified_host_e2e.txt`
- Tested URL set:
  - non-match loaded host: `https://example.org/`
  - typed but not submitted host: `example.com`
  - matching loaded host: `https://example.com/`
- Android test: `AccessibilityInterceptionTest#chromeVerifiedHostAdapterHarnessAcceptsOnlyLoadedMatchingHost`
- What the harness proves:
  - real Chrome loaded `example.org` is readable by the adapter but does not resolve against an `example.com` rule,
  - typed-but-not-loaded `example.com` returns `Unreadable`,
  - real Chrome loaded `example.com` is readable and resolves against the exact-domain rule,
  - screenshots preserve the three visual Chrome states for review.

## R2 Test Evidence

- Unit tests:
  - `VerifiedBrowserHostAdapterTest`
  - `WebsiteInterceptionResolverTest`
  - `AccessibilityInterceptionPlannerTest`
  - `MainViewModelTest.requestSystemWebsiteInterception_opensInterventionWithoutSelectedBrowserAndKeepsDomainPrivate`
  - `MainViewModelTest.websiteOpenAnywaySuppressesWebsiteKeyWithoutSuppressingWholeBrowserTarget`
- Android tests:
  - `AccessibilityInterceptionTest#chromeVerifiedHostAdapterHarnessAcceptsOnlyLoadedMatchingHost`
  - `VisualQaScreenshotTest#captureSprint26WebsiteRuleSettingsScreens`
  - `VisualQaScreenshotTest#captureSprint26ChromeWebsiteInterventionScreens`
- Lint:
  - `:app:lintDebug`
- Log and XML evidence:
  - `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r2/`

## R2 Visual Evidence

- Contact sheet: `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3_r2/sprint26_slice26_3_r2_chrome_verified_host_contact_sheet.png`
- Includes:
  - updated Settings browser support matrix,
  - Soft Chrome website intervention,
  - Firm wait Chrome website intervention,
  - real Chrome non-matching host,
  - real Chrome typed-but-not-loaded host,
  - real Chrome loaded matching host.

## Privacy Notes

- Runtime analytics metadata still excludes raw URL, host/domain, path/query, page title, URL-bar text, non-match observations, browsing-history rows, and domain-derived hashes.
- The real Chrome evidence file deliberately records the local test URL set for reviewer reproducibility only.
- Raw logcat from Chrome harness is not included because UIAutomator/Chrome logs can contain test URL strings; the bundle keeps controlled evidence instead.

## Emulator Cleanup

- Emulator used: `qaApi36(AVD) - 16`.
- Chrome package/version was recorded in evidence.
- The emulator should be shut down after R2 bundle creation.
