SCORE: 5/10

VERDICT: FAIL

VISUAL REVIEW: PASS

BLOCKERS:

BUNDLE GAP: The shipped source is not build-complete for the audited runtime path. Scope-critical files reference non-shipped classes, including InterceptionRuntimeGate, ForegroundAppDetectionPolicy, FixtureTargetRegistry, QualityAlternativeApplication, fixture activities, QualityAlternativeDatabase, PreferencesDelayGate, CompositeContentRepository, AndroidInterceptionMonitor, and AndroidAccountLightProfileAutosaveWriter. Because these are referenced by QualityAlternativeAccessibilityService.kt, InterceptionTargetResolver.kt, SupportedCatalog.kt, AppContainer.kt, MainActivityTest.kt, VisualQaScreenshotTest.kt, and MainViewModelTest.kt, the raw Gradle logs cannot be accepted as proof that this shipped bundle compiles or that the full AccessibilityService-to-MainActivity-to-ViewModel path is auditable from the bundle.

Portable Profile has a concrete all-missing import hydration bug. AccountLightProfileImporter.applyReplace() correctly filters unsupported imported packages out of selectedAppPackages, but MainViewModel then calls AppSettings.toUserPreferences(), whose empty selected-app mapping falls back to supportedApps.take(3). As a result, an imported profile whose app targets are all missing can hydrate Settings as if unrelated default supported apps are active, while the repository and AccessibilityService still see an empty selected package set. That violates the “missing packages inactive” requirement and creates a Settings/service mismatch.

The raw unit-test and lint logs are mostly UP-TO-DATE task transcripts and do not list executed test names or counts. Combined with missing source dependencies, they are insufficient to prove the validation claims against the shipped bundle.

R1 BLOCKER RECHECK:

Default intervention mode: PASS from shipped source and visuals. DEFAULT_INTERVENTION_MODE is InterventionMode.SOFT in UserModels.kt; PreferencesSettingsRepository.parseInterventionMode() falls back to that value; Settings/ViewModel tests assert Soft defaults; the Soft screenshot shows Open Calendar immediately enabled and does not show Open in 5s.

DocumentsUI / file picker safety: PASS for policy and visual-emulator rationale. InstalledAppTargetEligibilityPolicy.kt excludes com.android.documentsui, com.google.android.documentsui, and com.google.android.apps.docs; the policy test covers all three; AndroidInstalledAppTargetCatalog.kt only builds candidates from launcher activities; documentsui_package_check.log shows the Google DocumentsUI/Docs packages installed on the visual emulator but with no launcher activity.

Portable Profile auditability: PARTIAL / FAIL. AccountLightProfile.kt, SupportedCatalog.kt, and exporter/importer tests are shipped, and mixed eligible-plus-missing import cases are covered. However, the all-missing import path hydrates default supported apps in Settings through the ViewModel fallback, and source gaps prevent trusting the full shipped-bundle test execution claim.

Full intervention E2E auditability: PARTIAL / FAIL. MainActivity.kt, QualityAlternativeAccessibilityService.kt, and InterceptionTargetResolver.kt are shipped, and the direct code path is visible. However, the path depends on missing InterceptionRuntimeGate, ForegroundAppDetectionPolicy, FixtureTargetRegistry, and application/container classes, so the complete E2E path is not auditable from the shipped bundle.

Visual evidence completeness: PASS. screenshots-slice26_1_r2/CONTACT_SHEET.png and raw screenshots cover custom app search, self-exclusion, eligible search, selection, persistence after restart, removal/unselect, unselected no-trigger, Soft, Firm, and Bedtime states. No material UI regression is visible from the shipped screenshots.

Test/package evidence: FAIL. Logs exist, but the most important logs are up-to-date transcripts rather than fresh execution evidence with test names/counts, and the bundle omits classes required by the source and tests that those logs purport to validate.

CUSTOM APP TARGETS:

The shipped implementation supports searchable custom installed-app candidates through AndroidInstalledAppTargetCatalog.kt, using ACTION_MAIN plus CATEGORY_LAUNCHER and sorting/deduplicating candidates.

Standard suggestions remain separate in QualityAlternativeApp.kt: the UI labels “Standard suggestions” separately from “Add another app,” and AndroidInstalledAppTargetCatalog.kt filters out packages already present in SupportedCatalog.distractingApps.

PreferencesSettingsRepository.supportedDistractingApps() merges standard apps with eligible custom candidates and deduplicates by package name.

Visual evidence shows com.google.android.calendar as an eligible custom app, selected, persisted after restart, removed, and used in intervention variants.

ELIGIBILITY / SAFETY:

The policy excludes the app itself, launcher/home packages, Settings and permission packages, phone/emergency packages, installers/app stores, DocumentsUI/file pickers, and system-critical packages.

DocumentsUI/file-picker safety is materially improved from R1: the explicit policy list covers com.android.documentsui, com.google.android.documentsui, and com.google.android.apps.docs, and the shipped visual-emulator log supports the explanation that installed Google document packages were not launchable on that emulator.

Disabled-row rationale exists in AndroidInstalledAppTargetCatalog.kt, including the setup-safe copy that files and document pickers must remain available for imports, exports, and profile setup.

Remaining audit limitation: because package-critical support classes are missing, the safety policy tests cannot be accepted as proven executed from this shipped bundle.

INTERVENTION E2E:

The intended selected-custom-app flow is visible: QualityAlternativeAccessibilityService resolves a foreground package through InterceptionTargetResolver, starts MainActivity.createSystemInterceptionIntent(), and MainActivity passes the target package into MainViewModel.requestSystemInterception().

InterceptionTargetResolverTest.kt covers selected standard package matching, own-package fixture matching, own-package non-fixture ignore, and selected custom target resolution through knownTargets.

MainViewModel uses the existing replacement-first flow for system-origin interventions, including recommendation generation, pause/open-anyway handling, Soft/Firm delay behavior, Bedtime emergency delay behavior, and runtime suppression after open-anyway.

This is not release-auditable from the bundle because the service path depends on missing runtime-gate and detection-policy classes, and the shipped connected visual test invokes requestSystemInterception() directly rather than proving a live AccessibilityService foreground event through MainActivity.

PORTABLE PROFILE:

Export/import source files are shipped, and AccountLightSettings.selectedAppPackages persists selected package IDs.

Exporter tests include eligible custom app selection export.

Importer tests include mixed supported-plus-unsupported package import and mixed standard-plus-custom-plus-missing import, with unsupported packages filtered inactive at repository level.

Release blocker: the all-missing imported package case is not covered and is mishandled by Settings hydration because AppSettings.toUserPreferences() falls back to the first three supported apps when the selected package mapping is empty.

The broader typed target-rule model with last-known app display label from the planning document is not implemented in the shipped profile schema; the shipped proof is package-ID portability through selectedAppPackages, not typed target rules.

PRIVACY / ANALYTICS:

Website/domain blocking is not implemented in the shipped source, which is correct for Slice 26.1 because that work is deferred to Slice 26.2+.

No browser adapter, verified-host resolver, domain rule matcher, wildcard-domain matcher, DNS/VPN/proxy behavior, or browsing-history collection path is present in the shipped Slice 26.1 source.

Custom app detection records local analytics metadata including target package and foreground package/class in QualityAlternativeAccessibilityService; no remote analytics transport is shipped in this bundle. BUNDLE GAP: because remote analytics infrastructure is not present, remote-field minimization cannot be audited beyond confirming that this bundle does not add a remote transport path.

TEST / EVIDENCE:

Shipped tests cover many intended behaviors in source form: eligibility policy, repository custom candidate inclusion, resolver custom target matching, ViewModel Soft/Firm/Bedtime behavior, Portable Profile custom export/import, and visual screenshot capture.

visual_connected.log shows one connected instrumentation test completed successfully on qaTall(AVD) - 16, and the expected raw screenshots are present.

unit_targeted.log shows :app:testDebugUnitTest UP-TO-DATE and BUILD SUCCESSFUL, but no individual test execution list, no counts, and no visible command line in the raw log.

lint_compile_android_test.log shows lintDebug and compileDebugAndroidTestKotlin success, but mostly as up-to-date tasks.

git_diff_check.log is empty, which is compatible with a passing git diff --check, but it does not include command provenance.

Because the shipped source omits referenced classes, the logs appear to have been generated from a different or fuller workspace than the review bundle.

BUNDLE GAPS:

BUNDLE GAP: Missing source required by the shipped runtime path and tests: InterceptionRuntimeGate, ForegroundAppDetectionPolicy, FixtureTargetRegistry, QualityAlternativeApplication, fixture activities, database/repository implementations, PreferencesDelayGate, AndroidInterceptionMonitor, and profile autosave writer classes.

BUNDLE GAP: The shipped logs do not independently prove the targeted unit-test class set executed against this bundle, because the unit-test transcript is entirely up-to-date and lacks test names/counts.

BUNDLE GAP: The full AccessibilityService-to-MainActivity-to-ViewModel E2E cannot be validated from shipped files because key service-gating and fixture registry classes are absent.

BUNDLE GAP: The visual evidence proves the direct ViewModel-triggered custom app intervention states, but not a live AccessibilityService foreground event for an external selected custom package.

PACKAGE HYGIENE:

The R2 screenshot set is clean and relevant; the R1 review file is intentionally included and clearly named, so it is not itself misleading.

The R2 bundle manifest is misleadingly incomplete for an implementation audit because it lists build/test evidence while omitting source files required by included source files and tests.

Raw logs are not clean release evidence because they are mostly up-to-date Gradle transcripts and appear inconsistent with the shipped incomplete source tree.

The package is sufficient to review several isolated R2 changes, but not sufficient to certify release readiness or reproduce the claimed validation from the shipped bundle.

REQUIRED FIXES:

Ship a build-complete R2 bundle containing all source files referenced by included production code and tests, especially InterceptionRuntimeGate, ForegroundAppDetectionPolicy, FixtureTargetRegistry, QualityAlternativeApplication, fixture activities, database/repository implementations, PreferencesDelayGate, AndroidInterceptionMonitor, and profile autosave writer classes.

Regenerate validation logs from the exact shipped bundle with fresh execution evidence, preferably using --rerun-tasks or including test reports that list executed test classes, test counts, failures, and skipped tests.

Fix Settings hydration after Portable Profile replace import so an empty post-filter selectedAppPackages set caused by unsupported/missing imported packages does not fall back to unrelated default supported apps. The fallback should be limited to first-run onboarding or explicit legacy-default migration, not completed-profile import.

Add tests for a replace import where all selected app packages are unsupported or missing, asserting that repository settings, Settings UI hydration, and AccessibilityService resolver behavior keep those packages inactive without silently selecting defaults.

Add or regenerate E2E evidence that proves the AccessibilityService resolver can launch MainActivity for a selected custom foreground package through the actual system-interception intent path, not only through direct MainViewModel.requestSystemInterception() invocation.