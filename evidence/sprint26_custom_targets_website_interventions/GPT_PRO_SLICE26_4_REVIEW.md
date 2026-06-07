SCORE: 8/10

VERDICT: FAIL

VISUAL REVIEW: NOT APPLICABLE

BLOCKERS

Release-blocking privacy boundary gap: AnalyticsPrivacyGuard.remoteTargetClass() echoes metadata["targetType"] into the top-level remote payload for any value other than website_domain or null. Because this happens outside the metadata allowlist/value filter, an event with targetType = "com.android.chrome" or targetType = "example.com" would export that value as RemoteAnalyticsPayload.targetClass even though the metadata value itself would be filtered. Minimum fix: map only explicit known local target types to fixed remote classes, such as website_domain -> supported_browser_website, custom_app -> custom_app, standard_app -> standard_app, and otherwise return app_target or null; never echo raw targetType.

Release-blocking sanitizer gap: AnalyticsPrivacyGuard.isRemoteSafeValue() does not reject several host/domain forms that the slice contract forbids, including IPv4 literals such as 192.168.1.1, IPv6 literals such as 2001:db8::1, host-with-port values such as example.com:443, trailing-dot hosts such as example.com., and Unicode/IDNA-style hosts such as bücher.example. Any such value under an allowlisted metadata key can survive remote payload conversion. Minimum fix: add explicit IP-literal rejection, reject or normalize host-with-port and trailing-dot host forms before checking, reject Unicode host/domain forms by IDNA-normalizing before host detection, and add regression tests for those cases.

Release-blocking diagnostic gap: AnalyticsPrivacyGuard.unsafeRemoteFields() checks only payload.metadata; it does not inspect top-level fields such as targetClass, semanticKey, interventionId, sessionId, primaryContentId, backupContentIds, or contentId. Because targetClass can currently leak an unsafe targetType, the diagnostic can return empty while the payload is unsafe. Minimum fix: extend unsafe-field inspection, or make RemoteAnalyticsPayload construction private enough that only a fully sanitized factory can create it, and test unsafe top-level values.

PRIVACY / ANALYTICS BOUNDARY

The bundle proves that AnalyticsPrivacyGuard was added and that AnalyticsTracker now exposes allRemoteSafePayloads() and observeRemoteSafePayloads() through the guard. The intended local/remote split is visible in Contracts.kt: local allEvents() and observeEvents() remain unchanged, while remote-safe accessors map local events into RemoteAnalyticsPayload.

The boundary is not safe enough to commit because it filters metadata keys and values but does not fully sanitize top-level remote payload fields. The most serious concrete issue is targetClass, which is derived directly from metadata["targetType"] for unrecognized values. This violates the slice requirement that remote/export analytics collapse targets into classes and drop package, host, URL, title, path, query, and rule-id data.

The bundle proves no current remote upload implementation was added in this slice, so the risk is at the boundary/accessor level rather than an observed network-export path. That does not clear the blocker, because this slice’s purpose is to establish the explicit remote/export analytics boundary before future use.

URL / HOST / PACKAGE / TITLE / RULE-ID LEAK PREVENTION

Partial pass. The guard drops sensitive metadata keys such as browserPackage, foregroundPackage, foregroundClass, targetAppPackage, websiteRuleId, ruleId, rawUrl, externalUrl, urlBarText, and pageTitle. The unit test proves simple package, URL, host, page-title, and website-rule-id samples are absent from a rendered remote payload.

Fail for host/domain variants. The shipped value filter catches simple example.com-style host values and simple package-like values, but it does not catch IP literals, host-with-port values, trailing-dot hosts, or Unicode domain forms. Those are still raw host/domain observations for the purposes of the privacy contract.

Fail for unrecognized targetType. Even when metadata filtering would drop an unsafe targetType value, remoteTargetClass() can still export it as top-level targetClass. This creates a package/host leak route outside the metadata sanitizer.

LOCAL BEHAVIOR PRESERVATION

Pass. The slice does not remove local AnalyticsEvent.targetAppPackage, does not alter RoomAnalyticsTracker persistence, and does not change local app/website interception behavior. Local analytics still retain package fields needed for device-local intervention, delay, return-attribution, and suppression behavior.

The added remote-safe accessors are additive default methods on AnalyticsTracker; they do not mutate stored local events. The evidence therefore supports local behavior preservation.

PORTABLE PROFILE HARDENING

Pass. The exporter test proves website rules export as user-authored portable rules with id, ruleType, host, enabled, and includeApex, while excluding browser package/support/observed-host state such as browserPackage, browserSupportStatus, verified_host, observedHost, and Chrome website.

Pass. The importer test proves a missing custom app package increments the unsupported-app count, remains inactive after replace import, and does not appear in generated warning text. The importer re-resolves selected app packages against supported packages before applying replacement settings.

No blocker found in Portable Profile hardening.

TEST / EVIDENCE

The bundle includes full unit/lint evidence. The unit XML totals support the evidence claim of 438 tests, 0 failures, 0 errors, and 0 skipped. Targeted test suites for AnalyticsPrivacyGuardTest, AccountLightProfileExporterTest, and AccountLightProfileImporterTest are present and passed. Lint completed with 0 errors and existing warnings only. git diff --check passed.

Test coverage is insufficient for a 10/10 privacy boundary. Missing regressions include unsafe targetType leakage through targetClass, IP literals, host-with-port values, trailing-dot hosts, Unicode/IDNA host-like values, and top-level remote payload unsafe-field detection.

BUNDLE GAP: The evidence says tests inspect analytics payloads and logs for absence of forbidden strings. The bundle proves analytics payload tests and clean unit/lint logs for the sampled forbidden strings, but it does not prove an integrated log/breadcrumb call path using scrubDebugValue(); the helper is present and unit-tested, but no production call site is shipped in this slice.

VISUAL REVIEW

NOT APPLICABLE. The diff changes domain/service code, unit tests, and documentation only. No UI source, resources, screenshot-rendered behavior, or user-facing visual state changed in Slice 26.4. The absence of a screenshot contact sheet is therefore acceptable for this slice.

BUNDLE GAPS

BUNDLE GAP: No shipped test proves that AnalyticsPrivacyGuard rejects IP literals, host-with-port values, trailing-dot hosts, or Unicode/IDNA host-like values.

BUNDLE GAP: No shipped test proves that unsafe top-level RemoteAnalyticsPayload fields are detected or impossible.

BUNDLE GAP: No shipped production use of scrubDebugValue() is present, so the bundle proves a helper and helper-level test, not end-to-end log/breadcrumb hardening.

PACKAGE HYGIENE

Pass. The bundle contains the sprint docs, evidence summary, review prompt, Slice 26.3 R7 context, source diff, full app/src tree, Gradle build context, unit XML/report artifacts, lint reports, and diff-check output. APKs and release artifacts are excluded, which is appropriate for this non-release hardening review. No package hygiene issue blocks review.