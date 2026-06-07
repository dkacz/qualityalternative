SCORE: 10/10

VERDICT: PASS

VISUAL REVIEW: PASS

BLOCKERS: None

R1 BLOCKER RECHECK: PASS

R1’s launch-provenance blocker is closed by canonical source, manifest, connected test XML, and live-service evidence.

app/src/main/java/com/qualityalternative/app/MainActivity.kt:45-75 rejects all ACTION_SYSTEM_INTERVENTION intents unless EXTRA_LAUNCH_TOKEN equals the private per-process SYSTEM_INTERVENTION_TOKEN; website requests are forwarded only after that token check. MainActivity.kt:77-136 shows the token is private, generated per process, and inserted only by createSystemInterceptionIntent(...) and createWebsiteInterceptionIntent(...).

app/src/main/AndroidManifest.xml:26-37 exposes only .MainActivity as the launcher activity, with only MAIN / LAUNCHER in the intent filter. No website-intervention action is exported. AndroidManifest.xml:49-62 declares QualityAlternativeAccessibilityService as exported="false" with android.permission.BIND_ACCESSIBILITY_SERVICE.

app/src/androidTest/java/com/qualityalternative/app/MainActivityTest.kt:219-240 directly tests a forged explicit MainActivity website-intervention intent without the token and asserts that neither You reached for Chrome website nor Bedtime is protecting sleep from Chrome website appears. The connected XML at evidence/sprint26_custom_targets_website_interventions/logs-slice26_5_r2/connected_debug/TEST-qaApi36(AVD) - 16-_app-.xml reports forgedWebsiteInterceptionIntentWithoutLaunchTokenIsIgnored passed with 0 failures and 0 errors.

The real-service path is also proven. QualityAlternativeAccessibilityService.kt:74-100 resolves website targets before app targets and launches MainActivity.createWebsiteInterceptionIntent(...) only for a resolved website target. QualityAlternativeAccessibilityService.kt:134-151 requires VerifiedBrowserHostAdapter.readHostFromWindow(...) to return Verified before calling WebsiteInterceptionResolver.resolve(...). VerifiedBrowserHostAdapter.kt:16-24 supports only Chrome, VerifiedBrowserHostAdapter.kt:35-43 rejects package-mismatched or unreadable roots, and VerifiedBrowserHostAdapter.kt:46-68 accepts only visible, non-editing Chrome address-bar nodes whose text normalizes to a host.

The external live-service evidence is sufficient: live_service_e2e_slice26_5_r2/result.txt:1-11 records real Chrome com.android.chrome, a bound QualityAlternativeAccessibilityService, a matching verified host launch, and the resulting Bedtime Chrome website UI. dumpsys_accessibility_enabled.txt:20-21 and dumpsys_accessibility_after_launch.txt:20-21 show the QA accessibility service bound and enabled; dumpsys_accessibility_after_launch.txt:36 shows both Chrome and Quality Alternative windows in the live state. window_live.xml contains Bedtime is protecting sleep from Chrome website, Breathe before emergency unlock, Calm reset, and QUIET ALTERNATIVES, and it does not contain Pause 15 min, example.com, or https://.

BEDTIME CUSTOM APPS: PASS

Selected custom app Bedtime behavior remains covered and unchanged. The prior Slice 26.1 R4 evidence is present: SLICE26_1_R4_VALIDATION.md, GPT_PRO_SLICE26_1_R4_REVIEW.md, logs-slice26_1_r4/, and screenshots-slice26_1_r4/CONTACT_SHEET.png. GPT_PRO_SLICE26_1_R4_REVIEW.md:1-9 reports SCORE: 10/10, VERDICT: PASS, VISUAL REVIEW: PASS, and no blockers. SLICE26_1_R4_VALIDATION.md:61-65 records connected coverage for MainActivityTest#systemInterceptionIntentShowsLiveInterventionForSelectedCustomTarget and VisualQaScreenshotTest#captureSprint26CustomTargetSettingsScreens; SLICE26_1_R4_VALIDATION.md:82-93 states that the R4 screenshots cover Soft, Firm, and Bedtime intervention states.

Current source still preserves the shared Bedtime path for app targets. MainViewModel.kt:1898-1917 requires a selected app target before system app interception proceeds. MainViewModel.kt:1982-1995 applies runtime suppression with Bedtime awareness, MainViewModel.kt:2015-2049 bypasses the ordinary 15-minute delay gate during Bedtime, MainViewModel.kt:2137-2160 applies the 60-second Bedtime wait, and MainViewModel.kt:2286-2293 makes delayFor15Minutes() return without storing a delay when the intervention is Bedtime-enforced.

Current unit coverage remains green. MainViewModelTest.kt:4340-4389 verifies Bedtime custom-app behavior keeps recommendations/backups, uses BEDTIME_OPEN_ANYWAY_UNLOCK_DELAY_MILLIS, blocks early Open anyway, and records Bedtime unlock events. The current XML logs-slice26_5_r2/unit_xml/TEST-com.qualityalternative.app.ui.MainViewModelTest.xml reports that bedtimeModeKeepsAlternativesButRequiresOneMinuteEmergencyUnlock passed. ForegroundAppDetectionPolicyTest.kt:41-83 and its XML confirm same-package foreground events are allowed when entering or remaining in Bedtime, preventing duplicate suppression from hiding Bedtime interventions.

BEDTIME WEBSITE TARGETS: PASS

Supported Chrome website/domain targets get Bedtime emergency unlock only through the verified-host website path.

The source path is constrained to Chrome verified-host detection. VerifiedBrowserHostAdapter.kt:7-24 limits supported browser host reading to com.android.chrome; VerifiedBrowserHostAdapter.kt:35-43 rejects stale/package-mismatched roots; VerifiedBrowserHostAdapter.kt:46-68 rejects hidden, editable, whitespace-containing, or non-normalizable address-bar text. AccessibilityInterceptionTest.kt:79-151 exercises real Chrome states: non-matching loaded host does not resolve, typed-but-not-submitted host is unreadable and does not trigger, and the loaded matching host resolves. AccessibilityInterceptionTest.kt:154-211 covers unsupported and stale/package-mismatched negative states.

WebsiteInterceptionResolver.kt:18-24 resolves only enabled matching website rules; WebsiteInterceptionResolver.kt:26-41 displays the target as Chrome website and emits coarse website metadata; WebsiteInterceptionResolver.kt:45 scopes suppression to website-domain:<browserPackage>, not the raw domain and not the whole browser app target. WebsiteInterceptionResolverTest.kt:13-41 verifies no domain appears in analytics metadata or suppression key; WebsiteInterceptionResolverTest.kt:75-107 rejects disabled rules and boundary-spoofing hosts such as an example.com rule against example.com.evil.test.

Bedtime website semantics are covered by MainViewModelTest.kt:4877-4961: it verifies Chrome website, Bedtime enforcement, browserSupportStatus=verified_host, the 60-second wait, meditation availability, non-empty backups, blocked delayFor15Minutes(), early emergency unlock blocking, later unlock success, website-key suppression during Bedtime, no whole-Chrome suppression, and absence of example / https:// in serialized metadata.

SOFT/FIRM PRESERVATION: PASS

Outside Bedtime, Soft and Firm website behavior remains unchanged. MainViewModel.kt:2137-2140 applies no open-anyway delay in Soft, the 5-second form wait in Firm, and the 60-second wait only in Bedtime. QualityAlternativeApp.kt:2172-2182 shows Pause 15 min when !isBedtime.

Visual evidence confirms preservation. 23_website_chrome_verified_host_soft_intervention_light.png shows You reached for Chrome website, meditation, ordinary alternatives, Pause 15 min, and enabled Open Chrome website. 24_website_chrome_verified_host_firm_wait_light.png shows You reached for Chrome website, meditation, ordinary alternatives, Pause 15 min, Take five seconds, 5S, and disabled Open in 5s.

MainViewModelTest.kt:4829-4873 verifies website Open anyway suppresses the website key without suppressing the whole Chrome package. The current XML reports websiteOpenAnywaySuppressesWebsiteKeyWithoutSuppressingWholeBrowserTarget passed.

ALTERNATIVES / MEDITATION: PASS

Meditation and quiet alternatives remain available in Bedtime. MainViewModel.kt:2051-2059 explicitly preserves ContentSourceType.MEDITATION while filtering unavailable user links/documents and unselected editorial packs. QualityAlternativeApp.kt:2110-2119 renders the meditation alternative card when present, and QualityAlternativeApp.kt:2121-2124 changes the backup section label to Quiet alternatives during Bedtime.

MainViewModelTest.kt:4918-4921 asserts MEDITATION_TIMER_CONTENT_ID is offered and backups are non-empty in the Bedtime website flow. The direct Bedtime website screenshot and the live Chrome service screenshot both visibly show Calm reset, 3-minute reset, duration choices 1m / 3m / 5m / 10m, Start, QUIET ALTERNATIVES, and bounded reading alternatives.

PRIVACY / ANALYTICS: PASS

Remote analytics privacy is intact. PRD.md:423-424 and docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md:119-144 forbid raw URLs, hosts/domains, paths, queries, page titles, URL-bar text, browsing-history rows, and domain-derived identifiers in remote analytics.

WebsiteInterceptionResolver.kt:35-41 emits only targetType, browserPackage, browserSupportStatus, websiteRuleType, and websiteRuleIncludesApex; it does not emit the matched host, URL, path, page title, rule id, or domain-derived identifier. MainViewModel.kt:1947-1955 mirrors the same coarse website metadata for website intervention UI state and analytics.

Remote export is guarded. AnalyticsPrivacyGuard.kt:18-33 filters event metadata before remote payload construction. AnalyticsPrivacyGuard.kt:92-99 maps website events to supported_browser_website rather than a package/domain identity. AnalyticsPrivacyGuard.kt:111-142 rejects sensitive keys and values, including URL-like strings, host-like strings, package-like values, paths, queries, IP literals, and host-with-port values. AnalyticsPrivacyGuard.kt:233-258 explicitly denies exact sensitive keys such as browserPackage, foregroundPackage, foregroundClass, websiteRuleId, rawUrl, urlBarText, and pageTitle, and rejects key tokens such as url, uri, host, domain, title, package, class, path, query, address, and omnibox.

AnalyticsPrivacyGuardTest.kt:10-49 verifies that website remote payloads keep only safe website class/rule metadata and drop URL, host, package, foreground class, page title, and rule id. AnalyticsPrivacyGuardTest.kt:101-140 verifies IP, host-with-port, trailing-dot host, and Unicode/punycode host-like values are rejected. The current XML reports all 8 privacy-guard tests passed.

Portable Profile constraints are also covered by current XML: AccountLightProfileExporterTest.xml includes passing tests for portable settings profiles, filtering non-portable references/display names, exporting library/reading state without raw URIs, and including selected eligible custom app packages; AccountLightProfileImporterTest.xml includes passing tests for rejecting unsafe website rules, restoring portable website rules, activating eligible custom apps while keeping missing packages inactive, and not exposing missing custom package names in warnings.

TEST / EVIDENCE: PASS

Exact files checked:

PRD.md; docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md; docs/LANE_STATUS.md; evidence/sprint26_custom_targets_website_interventions/SLICE26_5_EVIDENCE.md; evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_5_REVIEW_SCRUBBED_FOR_R2.md; evidence/sprint26_custom_targets_website_interventions/SLICE26_5_R2_REVIEW_BUNDLE_MANIFEST.md; evidence/sprint26_custom_targets_website_interventions/SLICE26_1_R4_VALIDATION.md; evidence/sprint26_custom_targets_website_interventions/GPT_PRO_SLICE26_1_R4_REVIEW.md.

Source checked: AndroidManifest.xml; MainActivity.kt; QualityAlternativeAccessibilityService.kt; VerifiedBrowserHostAdapter.kt; WebsiteInterceptionResolver.kt; InterceptionRuntimeGate.kt; MainViewModel.kt; QualityAlternativeApp.kt; AnalyticsPrivacyGuard.kt; UserModels.kt.

Test source checked: MainActivityTest.kt; AccessibilityInterceptionTest.kt; VisualQaScreenshotTest.kt; MainViewModelTest.kt; InterceptionRuntimeGateTest.kt; ForegroundAppDetectionPolicyTest.kt; WebsiteInterceptionResolverTest.kt; AnalyticsPrivacyGuardTest.kt.

Test artifacts checked: all 38 current unit XML files under logs-slice26_5_r2/unit_xml/, totaling 443 tests, 0 failures, 0 errors, 0 skipped; logs-slice26_5_r2/connected_debug/TEST-qaApi36(AVD) - 16-_app-.xml, totaling 2 tests, 0 failures, 0 errors, 0 skipped; prior R4 XML under logs-slice26_1_r4/, totaling 204 shipped tests/artifacts with 0 failures and 0 errors.

Live-service artifacts checked: live_service_e2e_slice26_5_r2/result.txt; seed_instrumentation.log; dumpsys_accessibility_enabled.txt; dumpsys_accessibility_after_launch.txt; window_live.xml; live_chrome_bedtime_service_intervention.png.

Hygiene/validation artifacts checked: unit_lint_console.scrubbed.txt; lint-results-debug.scrubbed.txt; lint-results-debug.scrubbed.xml; git_diff_check.status.txt; git_diff_check.txt; adb_devices_after_emulator_shutdown.txt. lintDebug completed successfully with 0 errors; git diff --check exited with exit_code=0; emulator shutdown evidence lists no attached devices.

VISUAL REVIEW DETAILS:

The R2 contact sheet sprint26_slice26_5_r2_bedtime_website_live_contact_sheet.png contains four panels: Soft website, Firm website, Bedtime direct-intent UI, and Bedtime live Chrome service. The panels are internally consistent and do not mix stale failed attempts.

The Soft website screenshot shows the website target class correctly as Chrome website, not a raw domain and not a normal selected app target. It shows You reached for Chrome website, a reading primary, Calm reset, OTHER OPTIONS, Pause 15 min, and enabled Open Chrome website.

The Firm website screenshot shows You reached for Chrome website, Calm reset, OTHER OPTIONS, Pause 15 min, the five-second wait row Take five seconds, 5S, and disabled Open in 5s.

The Bedtime direct-intent screenshot shows Bedtime is protecting sleep from Chrome website, no raw domain/URL/path, a reading primary, Bedtime explanatory copy, Calm reset, duration chips, QUIET ALTERNATIVES, Breathe before emergency unlock, 60S, and disabled Breathe 60s. Pause 15 min is absent.

The live Chrome service screenshot shows the same target class and Bedtime behavior through the external live service path: Bedtime is protecting sleep from Chrome website, Calm reset, QUIET ALTERNATIVES, Breathe before emergency unlock, 58S, and disabled Breathe 58s. It does not show Pause 15 min, example.com, or https://.

The prior Slice 26.1 R4 contact sheet shows selected custom app coverage, including custom target selection/persistence/removal, an unselected no-intervention state, Soft custom app intervention, Firm wait state, and Bedtime custom app intervention with Bedtime is protecting sleep from Calendar, QUIET ALTERNATIVES, Breathe before emergency unlock, and no Pause 15 min.

BUNDLE GAPS: None.

PACKAGE HYGIENE: PASS

The R2 bundle includes the files that R1 identified as missing: MainActivity.kt, AndroidManifest.xml, AnalyticsPrivacyGuard.kt, and prior Slice 26.1 R4 validation/contact-sheet/log artifacts. The original unsanitized R1 review is not shipped; the bundle ships GPT_PRO_SLICE26_5_REVIEW_SCRUBBED_FOR_R2.md. Absolute local build paths were not found in shipped text artifacts; lint paths are scrubbed to <repo>.

The canonical R2 screenshot directory contains only the final website images and contact sheet, plus the live-service screenshot; no stale failed visual attempts or wrong-target Bedtime screenshot are mixed into the final visual evidence.

Non-blocking cleanup for future bundles: remove or update stale references to non-shipped unsanitized/html artifacts in SLICE26_5_EVIDENCE.md:34-40, and stale references to individual R4 screenshots in SLICE26_1_R4_VALIDATION.md:80 when only the R4 contact sheet is shipped. Scrubbing JUnit XML hostname attributes would further reduce local-machine disclosure, although those attributes did not affect this audit.