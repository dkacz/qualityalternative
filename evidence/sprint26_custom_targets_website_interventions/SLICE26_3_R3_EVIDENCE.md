# Sprint 26 Slice 26.3 R3 Evidence

Date: 2026-06-07

Scope: fix the GPT Pro R2 release blocker for Chrome verified-host website interventions by removing raw replacement URLs from analytics metadata.

## GPT Pro R2 Result

- Review file: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_3_R2_REVIEW.md`
- SCORE: `8/10`
- VERDICT: `FAIL`
- VISUAL REVIEW: `PASS`
- Blocking issue: website-domain intervention analytics could still include raw replacement `externalUrl` values from recommendation content metadata.

## R3 Implementation

- Removed `externalUrl` from shared `ContentItem.analyticsMetadata()`.
- Existing user-link and link-only handoff tests now assert analytics metadata does not contain `externalUrl`.
- Added `MainViewModelTest.requestSystemWebsiteInterceptionWithExternalUrlRecommendationKeepsAnalyticsUrlPrivate`, which exercises:
  - website-domain system intervention,
  - a recommended user-link replacement with a URL containing host, path, and query,
  - primary accept,
  - external fallback-open analytics,
  - metadata scan proving no raw URL, host, path, query, or secret fragment is present.

## Validation

- Passed targeted unit tests with JDK 17:
  - `MainViewModelTest.requestSystemWebsiteInterceptionWithExternalUrlRecommendationKeepsAnalyticsUrlPrivate`
  - `MainViewModelTest.requestSystemWebsiteInterception_opensInterventionWithoutSelectedBrowserAndKeepsDomainPrivate`
  - `MainViewModelTest.acceptingUserLinkRoutesToExternalHandoffAndRecordsFallbackOpen`
  - `MainViewModelTest.acceptingSharedLinkOnlyRoutesToExternalHandoffAndRecordsGenericOpen`
  - `MainViewModelTest.saveUserLinkFromForm_persistsLinkAndRecordsAnalytics`
  - `VerifiedBrowserHostAdapterTest`
  - `WebsiteInterceptionResolverTest`
  - `AccessibilityInterceptionPlannerTest`
- Passed `:app:lintDebug`.
- Passed `git diff --check`.

## Evidence Files

- R3 test/lint artifacts: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_3_r3/`
- R3 diff: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R3_DIFF.patch`
- R3 bundle manifest: `evidence/sprint26_custom_targets_website_interventions/SLICE26_3_R3_REVIEW_BUNDLE_MANIFEST.md`
- R2 visual evidence remains current because R3 changes only analytics metadata:
  - `evidence/sprint26_custom_targets_website_interventions/visual_e2e_slice26_3_r2/sprint26_slice26_3_r2_chrome_verified_host_contact_sheet.png`
  - `evidence/sprint26_custom_targets_website_interventions/chrome_verified_host_e2e_r2_latest/`

## Package Hygiene Fix

R3 regenerates the review diff to include the Slice 26.3 core source and test files that GPT Pro R2 identified as missing from the R2 diff artifact.
