# Sprint 26 Slice 26.4 R3 Evidence - Privacy, Analytics, And Portable Profile Hardening

Status: implementation ready for GPT Pro R3 review.

## R2 Review Result

- GPT Pro R2 output: `evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_4_R2_REVIEW.md`
- R2 verdict: `SCORE 9/10`, `VERDICT FAIL`, `VISUAL REVIEW NOT APPLICABLE`
- R2 release blocker:
  - sanitizer still allowed IDNA/punycode host-like values with punycode TLDs, such as `xn--e1afmkfd.xn--p1ai` and `example.xn--p1ai`;
  - sanitizer still allowed trailing-dot IPv4 literals, such as `192.168.1.1.`;
  - those cases also needed proof through top-level `unsafeRemoteFields()` diagnostics.

## R3 Fixes

- Host/IP canonicalization now trims trailing dots before IP checks.
- Bracketed IPv6 and unbracketed IPv6 are rejected after canonicalization.
- DNS-style multi-label values are rejected regardless of whether the final label is alphabetic, numeric-ish, or punycode.
- IDNA normalization is still applied, but host-like detection no longer depends on `[a-z]{2,}` TLD matching.
- `HostLikeRegex` now treats any valid multi-label ASCII DNS form as host-like, including punycode labels/TLDs.

## Regression Coverage

- `AnalyticsPrivacyGuardTest.toRemotePayload_rejectsIpPortTrailingDotAndUnicodeHostMetadataValues` now covers:
  - `xn--e1afmkfd.xn--p1ai`,
  - `example.xn--p1ai`,
  - `192.168.1.1.`,
  - plus the prior IPv4, IPv6, host-with-port, trailing-dot domain, and Unicode host cases.
- `AnalyticsPrivacyGuardTest.unsafeRemoteFields_checksTopLevelPayloadFields` now checks those remaining sanitizer cases through top-level diagnostics and metadata diagnostics.
- `AnalyticsPrivacyGuardTest.scrubDebugValue_redactsUrlsHostsPackagesAndKeepsSafeCounters` now covers punycode host-like values and trailing-dot IPv4 literals.

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

- Full unit/lint console: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_4_r3/unit_lint_console.txt`
- Unit XML: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_4_r3/unit_xml/`
- Unit HTML report: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_4_r3/unit_report/`
- Lint reports: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_4_r3/lint-results-debug.{txt,xml,html}`
- Diff-check status: `evidence/sprint26_custom_targets_website_interventions/logs-slice26_4_r3/git_diff_check.status.txt`
- Source diff: `evidence/sprint26_custom_targets_website_interventions/SLICE26_4_R3_DIFF.patch`

## Visual Review

- Visual review remains not applicable for this slice because no UI or screenshot-rendered behavior changed.

## Package Hygiene

- No release artifact or APK is required for this slice review.
- R3 bundle should use the R1 and R2 reviews, R3 evidence summary, R3 diff, raw unit/lint outputs, current source tree, and existing Slice 26.3 R7 context.
