SCORE: 9/10

VERDICT: FAIL

VISUAL REVIEW: NOT APPLICABLE

BLOCKERS

Release-blocking sanitizer gap remains. AnalyticsPrivacyGuard.isHostLike() in app/src/main/java/com/qualityalternative/app/domain/model/AnalyticsPrivacyGuard.kt rejects ordinary ASCII hosts and the tested Unicode host bücher.example, but its HostLikeRegex requires the final label to match [a-z]{2,}. This allows IDNA/punycode host-like values with punycode TLDs, such as xn--e1afmkfd.xn--p1ai or example.xn--p1ai, to pass as remote-safe metadata values because they are ASCII, are not matched as host-like, and are not matched as package-like. This violates the R1 blocker requirement to reject Unicode/IDNA host variants.

The same value filter also allows a trailing-dot IPv4 literal such as 192.168.1.1. because isIpLiteral() checks before trailing-dot canonicalization, while isHostLike() trims the dot but then rejects the numeric final label as non-host-like. This leaves an IP-literal/trailing-dot host variant that can survive under an allowlisted metadata key.

Minimum fix: canonicalize before classification by trimming a single trailing dot before IP checks, rejecting bracketed and unbracketed IP literals after canonicalization, IDNA-normalizing host candidates, and rejecting any DNS-style value with multiple valid labels including xn-- labels and punycode TLDs. Add regression tests for xn--e1afmkfd.xn--p1ai, example.xn--p1ai, their trailing-dot forms, and 192.168.1.1. through both metadata filtering and unsafeRemoteFields() top-level checks.

R1 BLOCKER RECHECK

Unknown targetType echo: PASS. remoteTargetClass() now maps only website_domain, custom_app, and standard_app to fixed remote classes, maps unknown target types with a local package to app_target, and returns no class for unknown target types without a package. The regression toRemotePayload_doesNotEchoUnknownTargetTypeIntoRemoteTargetClass proves the previous example.com echo path is closed.

IP, port, trailing-dot, and Unicode/IDNA sanitizer: FAIL. The shipped test covers 192.168.1.1, 2001:db8::1, example.com:443, example.com., and bücher.example, but the implementation still permits punycode-TLD IDNA host-like values and trailing-dot IPv4 literals as described above.

Top-level unsafe-field diagnostics: PARTIAL PASS. unsafeRemoteFields() now inspects semanticKey, interventionId, sessionId, targetClass, primaryContentId, backupContentIds, and contentId, and the direct RemoteAnalyticsPayload regression proves several unsafe top-level examples are detected. The result inherits the remaining sanitizer gap because the same host/IP classifier is used for top-level values.

PRIVACY / ANALYTICS BOUNDARY

The bundle proves a local/remote split was added. AnalyticsTracker.allEvents() and observeEvents() remain local, while allRemoteSafePayloads(), observeRemoteSafePayloads(), and allRemoteSafeDebugSummaries() map through AnalyticsPrivacyGuard. RemoteAnalyticsPayload does not copy targetAppPackage, and metadata keys such as browserPackage, foregroundPackage, foregroundClass, targetAppPackage, websiteRuleId, rawUrl, externalUrl, urlBarText, and pageTitle are denied.

The boundary is not safe enough to commit because the remote-safe value filter still allows specific host/IP variants. A value such as xn--e1afmkfd.xn--p1ai under an allowlisted key such as origin, reason, action, availability, or format would survive toRemotePayload() and would also survive scrubDebugValue().

URL / HOST / PACKAGE / TITLE / RULE-ID LEAK PREVENTION

Simple package, URL, host, page-title, URL-bar, browser-package, foreground-package/class, and website-rule-id leak routes are covered by source filtering and tests. The toRemotePayload_keepsWebsiteRuleClassButDropsUrlHostPackageAndRuleId test proves the ordinary Chrome website payload drops com.android.chrome, news.example, https://, the private page title, and the website rule id while retaining coarse class/status fields.

The remaining failure is host/IP variant handling. The current regex strategy does not reject punycode-TLD host-like values or trailing-dot IPv4 literals, so the bundle does not prove URL/host leak prevention for all R1-required sanitizer classes.

LOCAL BEHAVIOR PRESERVATION

PASS. The slice does not remove AnalyticsEvent.targetAppPackage, does not change RoomAnalyticsTracker local persistence, and does not alter the interception source paths. The remote-safe accessors are additive interface methods and do not mutate local events, delay attribution, return attribution, or suppression behavior.

PORTABLE PROFILE HARDENING

PASS. The exporter test proves website rules are exported as portable user-authored rules with id, ruleType, host, enabled, and includeApex, while omitting browser package/support/observed-host state such as browserPackage, browserSupportStatus, verified_host, observedHost, and Chrome website.

PASS. The importer test proves a missing custom app package increments unsupportedAppCount, is not activated on replace import, and is not rendered in warning text. Source inspection also shows replace import filters selected app packages through the current build’s supported package set before applying settings.

TEST / EVIDENCE

The unit XML evidence supports 442 tests, 0 failures, 0 errors, and 0 skipped. AnalyticsPrivacyGuardTest has 8 passing tests, AccountLightProfileExporterTest has 10 passing tests, and AccountLightProfileImporterTest has 26 passing tests. The lint console shows BUILD SUCCESSFUL; lint has 0 errors with existing warnings, and git diff --check is marked PASS.

The evidence is insufficient for a 10/10 privacy gate because it lacks regressions for punycode-TLD IDNA host values and trailing-dot IP literals. The current tests cover only one Unicode host with an ASCII final label and one trailing-dot ordinary domain.

VISUAL REVIEW

NOT APPLICABLE. The R2 diff changes domain/service code, unit tests, and documentation only. No UI source, visual resources, screenshots, or screenshot-rendered behavior changed in this slice.

BUNDLE GAPS

BUNDLE GAP: No shipped test proves that IDNA/punycode host-like values with punycode TLDs are rejected.

BUNDLE GAP: No shipped test proves that trailing-dot IP literals such as 192.168.1.1. are rejected.

BUNDLE GAP: No shipped test proves the remaining sanitizer edge cases through both remote payload conversion and top-level unsafeRemoteFields() diagnostics.

PACKAGE HYGIENE

PASS. The bundle contains the primary planning document, R1 review, R2 evidence summary, R2 manifest, R2 diff, current source tree, unit XML/report artifacts, lint reports, diff-check output, Gradle files, and Android source/test context. APKs and release artifacts are excluded, which is appropriate for this non-release hardening review.