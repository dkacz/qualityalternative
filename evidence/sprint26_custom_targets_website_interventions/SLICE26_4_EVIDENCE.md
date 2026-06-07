# Sprint 26 Slice 26.4 Evidence - Privacy, Analytics, And Portable Profile Hardening

Status: implementation ready for GPT Pro review.

## Scope

- Added `AnalyticsPrivacyGuard` as the explicit remote/export analytics boundary.
- Local analytics can still retain package fields needed for device-local behavior, but `AnalyticsTracker.allRemoteSafePayloads()` and `observeRemoteSafePayloads()` collapse targets into classes and sanitize metadata before any future remote/export use.
- Remote-safe analytics now use an allowlist plus denylist: unsafe keys/values such as URL, URI, host/domain, page title, URL-bar text, package/class names, paths, queries, and website rule ids are removed.
- Added debug-value scrubbing helper for log/breadcrumb paths that need a privacy-safe value.
- Extended Portable Profile tests to prove website rules export without browser package/support/observed-host state and missing imported custom app warnings do not reveal package names.

## Validation

- Targeted unit tests passed:
  - `AnalyticsPrivacyGuardTest`
  - `AccountLightProfileExporterTest`
  - `AccountLightProfileImporterTest`
- Full unit + lint passed with Homebrew JDK 17:
  - `:app:testDebugUnitTest`: 438 tests, 0 failures, 0 errors, 0 skipped.
  - `:app:lintDebug`: 0 errors; existing warnings only.
- `git diff --check`: PASS.

## Evidence Paths

- Full unit/lint console: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_4/unit_lint_console.txt`
- Unit XML: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_4/unit_xml/`
- Unit HTML report: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_4/unit_report/`
- Lint reports: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_4/lint-results-debug.{txt,xml,html}`
- Diff-check status: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_4/git_diff_check.status.txt`
- Source diff: `evidence/sprint26_custom_targets_website_interventions/SLICE26_4_DIFF.patch`

## Visual Review

- Visual review is not applicable for this slice because no UI or screenshot-rendered behavior changed.
- The slice changes code-level privacy/export boundaries and unit-tested profile behavior only.

## Package Hygiene

- No release artifact or APK is required for this slice review.
- Review should use the current source diff, evidence summary, raw unit/lint outputs, and GPT Pro R7 Slice 26.3 context already preserved in this evidence directory.
