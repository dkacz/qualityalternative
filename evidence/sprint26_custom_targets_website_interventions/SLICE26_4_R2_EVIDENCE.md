# Sprint 26 Slice 26.4 R2 Evidence - Privacy, Analytics, And Portable Profile Hardening

Status: implementation ready for GPT Pro R2 review.

## R1 Review Result

- GPT Pro R1 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_4_REVIEW.md`
- R1 verdict: `SCORE 8/10`, `VERDICT FAIL`, `VISUAL REVIEW NOT APPLICABLE`
- R1 release blockers:
  - `remoteTargetClass()` echoed unknown `metadata["targetType"]` values into top-level `RemoteAnalyticsPayload.targetClass`.
  - `isRemoteSafeValue()` did not reject IP literals, host-with-port, trailing-dot host, or Unicode/IDNA host variants.
  - `unsafeRemoteFields()` checked only metadata and missed unsafe top-level payload fields.

## R2 Fixes

- `AnalyticsPrivacyGuard.remoteTargetClass()` now maps only known local target types to fixed remote classes:
  - `website_domain` -> `supported_browser_website`
  - `custom_app` -> `custom_app`
  - `standard_app` -> `standard_app`
  - unknown target types with a local app package -> `app_target`
  - unknown target types without a local app package -> no top-level target class.
- `targetType` metadata now passes only for known target-type values; unknown safe-looking strings are not echoed through metadata.
- Remote value filtering now rejects:
  - IPv4 literals,
  - IPv6 literals,
  - host-with-port values,
  - trailing-dot hosts,
  - Unicode/IDNA host-like values,
  - package-like values,
  - URLs, URI/file/content schemes, path/query/hash/address-like values, and control characters.
- `unsafeRemoteFields()` now inspects top-level fields: `semanticKey`, `interventionId`, `sessionId`, `targetClass`, `primaryContentId`, `backupContentIds`, and `contentId`.
- `AnalyticsTracker.allRemoteSafeDebugSummaries()` now provides a production diagnostic path that renders only remote-safe payloads through `AnalyticsPrivacyGuard.remoteSafeDebugSummary()` and `scrubDebugValue()`.

## Regression Coverage

- `AnalyticsPrivacyGuardTest.toRemotePayload_doesNotEchoUnknownTargetTypeIntoRemoteTargetClass`
- `AnalyticsPrivacyGuardTest.toRemotePayload_rejectsIpPortTrailingDotAndUnicodeHostMetadataValues`
- `AnalyticsPrivacyGuardTest.unsafeRemoteFields_checksTopLevelPayloadFields`
- `AnalyticsPrivacyGuardTest.scrubDebugValue_redactsUrlsHostsPackagesAndKeepsSafeCounters`
- `AnalyticsPrivacyGuardTest.analyticsTracker_exposesRemoteSafeDebugSummariesThroughScrubber`
- Existing Portable Profile regression coverage remains:
  - website rules export without browser package/support/observed-host state,
  - missing custom app package warnings do not reveal package names.

## Validation

- Targeted unit tests passed:
  - `AnalyticsPrivacyGuardTest`
  - `AccountLightProfileExporterTest`
  - `AccountLightProfileImporterTest`
- Full unit + lint passed with Homebrew JDK 17:
  - `:app:testDebugUnitTest`: 442 tests, 0 failures, 0 errors, 0 skipped.
  - `:app:lintDebug`: 0 errors; existing warnings only.
- `git diff --check`: PASS.

## Evidence Paths

- Full unit/lint console: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_4_r2/unit_lint_console.txt`
- Unit XML: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_4_r2/unit_xml/`
- Unit HTML report: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_4_r2/unit_report/`
- Lint reports: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_4_r2/lint-results-debug.{txt,xml,html}`
- Diff-check status: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_4_r2/git_diff_check.status.txt`
- Source diff: `evidence/sprint26_custom_targets_website_interventions/SLICE26_4_R2_DIFF.patch`

## Visual Review

- Visual review remains not applicable for this slice because no UI or screenshot-rendered behavior changed.
- The slice changes code-level privacy/export boundaries, analytics diagnostics, and unit-tested profile behavior only.

## Package Hygiene

- No release artifact or APK is required for this slice review.
- R2 bundle should use the R1 review, R2 evidence summary, R2 diff, raw unit/lint outputs, current source tree, and existing Slice 26.3 R7 context.
