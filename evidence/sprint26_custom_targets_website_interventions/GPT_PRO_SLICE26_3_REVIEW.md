SCORE

SCORE: 7/10

VERDICT

VERDICT: FAIL

VISUAL REVIEW

VISUAL REVIEW: PASS

BLOCKERS

Chrome support/status UI remains stale after Slice 26.3 enables the adapter.
Blocking file: app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt, lines 5375 and 5582-5585.
The Settings copy still says browser interruption starts only when a verified current-host adapter is available “until then,” and the Chrome support row still says Verified-host adapter next with “activates after the Chrome current-host check passes the next slice.” That was accurate in Slice 26.2, but it is now user-facing ambiguity for Slice 26.3 because the implementation claims Chrome verified-host intervention is active.
Minimum fix: update the website-rule explanatory copy and Chrome support row to say Chrome domain intervention is supported only when the current Chrome host is readable through the verified-host adapter; keep other browsers on whole-browser app-target fallback and keep full-path/universal URL blocking explicitly out of scope.

Website Open Anyway suppression is separated at the runtime-key level, but the AccessibilityService can still prevent the whole-browser app target from running.
Blocking file: app/src/main/java/com/qualityalternative/app/interception/QualityAlternativeAccessibilityService.kt, lines 78-102 and 162-170.
Once websiteTarget != null, the service calls processInterceptionTarget(...) and unconditionally returns at line 101. If processInterceptionTarget returns early because the website suppression key is active, the whole-browser Chrome app target is never evaluated, even when Chrome is selected as a whole-app target. This undercuts the product requirement that website Open Anyway suppression not suppress the browser app target key. The existing MainViewModelTest.websiteOpenAnywaySuppressesWebsiteKeyWithoutSuppressingWholeBrowserTarget only proves the raw InterceptionRuntimeGate key lookup; it does not prove the service fallback path.
Minimum fix: make website processing return a handled/not-handled result, or pre-check website-key suppression and fall through to InterceptionTargetResolver when only the website key is suppressed. Add a service-level or extracted-pipeline regression proving: matching website rule + active website suppression + Chrome selected as an app target still allows the whole-browser Chrome app intervention path.

The visual E2E does not exercise Chrome, the AccessibilityService, or the verified-host adapter.
Blocking file/test: app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt, lines 615-664.
The test launches MainActivity.createWebsiteInterceptionIntent(...) directly. It does not open Chrome, does not configure or navigate a real Chrome address-bar host, does not exercise QualityAlternativeAccessibilityService, and does not prove that a current verified Chrome address-bar host triggered the intervention. The connected-test log confirms only one direct UI screenshot test: VisualQaScreenshotTest#captureSprint26ChromeWebsiteInterventionScreens.
Minimum fix: add an instrumentation/E2E path, or a testable service-adapter integration harness, that proves a matching Chrome address-bar host triggers; a non-matching Chrome host does not trigger; an unreadable/hidden/stale state does not reuse a previous host; and Chrome package/version plus the test URL set are recorded in evidence. The current direct-intent screenshot may remain as visual UI coverage, but it cannot be the verified-host E2E.

Hidden/currentness safety is not represented in the adapter snapshot model.
Blocking file: app/src/main/java/com/qualityalternative/app/interception/VerifiedBrowserHostAdapter.kt, lines 74-78 and 86-106.
BrowserNodeSnapshot stores only viewIdResourceName, text, and children; toBrowserNodeSnapshot copies every traversed node’s text without checking AccessibilityNodeInfo.isVisibleToUser or another current/readable-state signal. A hidden or offscreen whitelisted url_bar/search_box_text node with stale text would be indistinguishable from a visible current address-bar node to readHostFromSnapshot. The tests cover title/body rejection and unsupported browsers, but they cannot cover hidden-state rejection because the model has no visibility field.
Minimum fix: add visibility/currentness fields to BrowserNodeSnapshot, filter real AccessibilityNodeInfo nodes before copying text, require visible supported address-bar nodes for verification, and add unit tests that hidden whitelisted nodes return Unreadable.

CHROME VERIFIED-HOST ADAPTER

PASS with limitations. The adapter is Chrome-scoped through VerifiedBrowserHostAdapter.CHROME_PACKAGE = "com.android.chrome" and rejects unsupported browser packages before reading any tree. It reads only resource names ending in url_bar or search_box_text, normalizes candidate text through WebsiteRuleNormalizer, rejects whitespace searches, and returns Unreadable when no valid whitelisted node exists.

The adapter does not store or reuse a previous host, which is good for stale-cache safety. The problem is that it verifies “whitelisted address-bar node text,” not fully “current visible loaded Chrome host.” Visibility/currentness is not modeled, and the bundle does not prove that TYPE_WINDOW_CONTENT_CHANGED cannot originate from an edited but not loaded omnibox state.

WEBSITE RULE MATCHING

PASS. Exact and wildcard matching remain boundary-safe in the inspected source. WebsiteRuleNormalizer.matches(...) requires exact equality for exact-domain rules and host.endsWith(".${rule.host}") for wildcard subdomains, with optional apex inclusion. Existing tests cover notexample.com, example.com.evil.example, wildcard subdomains, apex inclusion, disabled rules, IDNA/punycode normalization, trailing dots, invalid/private/local/IP hosts, and all-numeric hosts.

No substring-spoofing blocker was found.

STALE / UNREADABLE STATE SAFETY

PARTIAL FAIL. The implementation does not cache a prior observed host, so unreadable or unsupported states do not reuse an in-app previous-host variable. readHostFromWindow(...) returns Unreadable for a null root and UnsupportedBrowser for non-Chrome packages.

The missing piece is hidden/currentness safety. Because the snapshot model lacks visibility/current-state fields and copies node text without filtering visibility, the bundle does not satisfy the hidden-state portion of the contract. The tests also do not cover custom-tab, PWA, incognito-like, focused omnibox edit, or hidden address-bar states.

SOFT / FIRM / OPEN ANYWAY BEHAVIOR

VISUAL PASS for the supplied contact sheet. The soft website screen shows Chrome website, a finite reading recommendation, a meditation alternative, backup choices, Pause 15 min, and an enabled Open Chrome website action. The firm screen preserves the same finite alternatives and shows the five-second wait state with the Open Anyway action disabled.

FUNCTIONAL FAIL for the Open Anyway fallback edge case. InterceptionRuntimeGate supports a separate website suppression key, and the ViewModel unit test proves the raw browser key is not marked suppressed. However, the AccessibilityService returns immediately after any matching website target is found, so a suppressed website target can still prevent the whole-browser Chrome app target from being evaluated.

PRIVACY / ANALYTICS

PASS on inspected source and supplied logs. I found no raw URL, host/domain, path/query, page title, URL-bar text, browsing-history row, non-match observation, or domain-derived hash in the website analytics path. WebsiteInterceptionResolver returns metadata limited to target type, browser package, browser support status, rule type, and apex-inclusion boolean. MainViewModel propagates that metadata without adding the observed host. The visual logcat grep did not show test domains or URLs.

One caution: browserPackage and foregroundPackage are present in analytics metadata for website events. That does not violate the stated denylist because it is not a host/domain or URL, but it should remain an intentional allowlisted field.

TEST / EVIDENCE

PASSING EVIDENCE:

VerifiedBrowserHostAdapterTest: 4 tests, 0 failures.

WebsiteInterceptionResolverTest: 3 tests, 0 failures.

targeted MainViewModelTest: 2 tests, 0 failures.

connected screenshot test: 1 test, 0 failures.

visual contact sheet exists and is visually acceptable for soft/firm intervention UI states.

MISSING/BLOCKING EVIDENCE:

No actual Chrome navigation or live Chrome address-bar E2E is present.

No test proves non-matching Chrome host non-trigger behavior through the service path.

No test proves typed-but-not-loaded URL safety.

No test proves hidden/stale/custom-tab/PWA/incognito-like safety.

No service-level test proves website suppression falls through to whole-browser app target when Chrome is also selected.

The bundle claims :app:lintDebug passed, but no lint report or lint console output is included under logs-slice26_3/.

BUNDLE GAPS

BUNDLE GAP: Chrome package version is not recorded in the evidence, despite the Sprint 26 evidence contract requiring browser package/version for website evidence.

BUNDLE GAP: The test URL set is not recorded, and the visual test does not use real URLs because it directly launches an internal website-intervention intent.

BUNDLE GAP: The bundle does not prove real Chrome exposes url_bar or search_box_text as a current loaded host on the tested emulator.

BUNDLE GAP: The bundle does not prove TYPE_WINDOW_CONTENT_CHANGED is safe from typed but not loaded omnibox text.

BUNDLE GAP: No lint artifact is included, although the manifest states ./gradlew :app:lintDebug was run.

PACKAGE HYGIENE

PARTIAL PASS. The bundle includes source, tests, evidence summaries, unit XML, connected-test XML, logcat, Gradle wrapper files, and the visual contact sheet.

Issue: evidence/sprint26_custom_targets_website_interventions/SLICE26_3_DIFF.patch does not include diffs for the two core new implementation files, VerifiedBrowserHostAdapter.kt and WebsiteInterceptionResolver.kt, or their corresponding unit-test files, even though the manifest lists them as source files and they are present in the bundle. Since the full source files are included, this is not a standalone blocker, but the diff artifact is incomplete for the primary slice review path.