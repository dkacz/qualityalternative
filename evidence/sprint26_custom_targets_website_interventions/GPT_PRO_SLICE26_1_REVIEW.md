SCORE: 6/10

VERDICT: FAIL

VISUAL REVIEW: FAIL

BLOCKERS

Default intervention mode contradicts the shipped PRD/plan and visual evidence. PRD.md, AGENTS.md, and docs/SPRINT_26_CUSTOM_TARGETS_WEBSITE_INTERVENTIONS.md preserve Soft intervention as the default product shape, but UserModels.kt sets DEFAULT_INTERVENTION_MODE = InterventionMode.FIRM, PreferencesSettingsRepositoryTest.kt and MainViewModelTest.kt assert Firm as default, and 05_custom_app_intervention_light.png shows the Open action locked behind “Open in 5s.” This is a code-test-visual-doc mismatch and changes the default override path for custom targets.

Safety exclusions are incomplete for setup-critical Android flows. The custom app picker visibly treats com.google.android.documentsui / Files as an eligible selectable target in 01_custom_app_search_empty_light.png, and AndroidInstalledAppTargetCatalog.kt does not exclude com.android.documentsui or com.google.android.documentsui. Selecting DocumentsUI can interrupt Android file-picker / SAF flows used for document import, profile import/export, and local file selection, creating a concrete setup-trapping failure that the PRD’s setup-critical exclusion is meant to prevent.

Portable Profile behavior is not fully auditable from shipped implementation files. The bundle includes tests for AccountLightProfileExporter and AccountLightProfileImporter, but the actual exporter, importer, codec, and profile model source files are not shipped. Therefore the claim that export/import keeps eligible custom packages active and missing packages inactive with warnings is BUNDLE GAP at implementation level.

Required Slice 26.1 visual evidence is incomplete. The contact sheet covers search, self-exclusion, eligible search, selected state, and an intervention, but it does not capture persistence after restart, remove/unselect, unselected app non-trigger, or Soft/Firm/Bedtime variants required by the Sprint 26.1 evidence contract.

CUSTOM APP TARGETS

The core custom target UI is substantially implemented. QualityAlternativeApp.kt separates “Standard suggestions” from “Add another app,” filters standard packages out of custom candidates, supports search over display name and package name, and renders selected custom rows with the same row component used for standard app selection.

The repository path also hydrates eligible custom candidates into supportedDistractingApps() and MainViewModel.applySettings() maps selected package ids back into active target apps. The visual evidence shows an eligible non-standard package selected and then used for an intervention.

The shipped implementation persists only package identifiers in selectedAppPackages. The broader Sprint 26 TargetRule model and “last known user-visible label” portable payload are not proven by shipped implementation files.

ELIGIBILITY / SAFETY

PASS for self-exclusion: Quality Alternative is shown as disabled with the reason “Quality Alternative cannot interrupt itself,” and AndroidInstalledAppTargetCatalog.kt explicitly classifies packageName == appContext.packageName as EXCLUDED_SELF.

PASS for standard/custom deduplication: standard suggestions are filtered out of custom candidates and displayed separately.

FAIL for setup-critical/system-critical coverage: DocumentsUI/Files is visible as selectable, not disabled. This is a concrete Android-platform safety miss because it can intercept the system document picker used by the app’s own file/profile workflows.

BUNDLE GAP: Actual eligibility coverage for launchers, Settings, permission controllers, phone/emergency packages, installers, and system-critical packages is only partly inspectable. The code contains exact-name exclusions, but there is no shipped test for the real Android catalog classification path.

INTERVENTION E2E

Partial pass. InterceptionTargetResolver.kt resolves selected custom packages from knownTargets, and QualityAlternativeAccessibilityService.kt hydrates knownTargets from settingsRepository.supportedDistractingApps(). MainViewModel.requestSystemInterception() then uses the existing replacement-first intervention path. The visual screenshot demonstrates that a selected custom package can reach the intervention UI.

BUNDLE GAP: MainActivity intent handling is not shipped, so the full AccessibilityService → MainActivity intent → ViewModel intervention path cannot be independently verified from the bundle.

The unselected app non-trigger behavior is covered by a ViewModel test for a fixture target, but it is not visually captured and not specifically exercised with a real custom installed package.

PORTABLE PROFILE

Partial test coverage exists: exporter/importer tests assert selected eligible custom package export and active import while missing packages create warnings and remain inactive.

BUNDLE GAP: The actual Portable Profile exporter/importer implementation files are absent. Tests alone do not prove shipped production behavior when the production source cannot be inspected.

BUNDLE GAP: The bundle does not prove the broader Sprint 26 TargetRule representation with custom_app payload and last-known display label; the visible schema and tests remain centered on settings.selectedAppPackages.

PRIVACY / ANALYTICS

PASS for no website/domain implementation in Slice 26.1. The shipped Slice 26.1 code does not implement domain matching, browser host extraction, URL-bar reading, browsing-history capture, VPN/DNS/proxy behavior, browser extension behavior, or website rule UI.

BUNDLE GAP: Remote analytics behavior cannot be fully proven because the concrete analytics storage/export/transport implementation is not shipped. The shipped service records targetAppPackage, foregroundPackage, and foregroundClass for foreground detection; whether those remain local-only or become remote analytics is not auditable from this bundle.

TEST / EVIDENCE

The validation file reports successful targeted unit tests, lint, visual instrumentation, and git diff --check, but raw test output artifacts are not included.

Unit coverage is directionally useful for repository hydration, resolver behavior, ViewModel intervention behavior, and Portable Profile tests. It is not sufficient for a 10/10 gate because the production Portable Profile implementation is absent, actual Android catalog eligibility is not directly tested, and required visual states are missing.

The visual contact sheet is internally consistent with the five captured files, but it is not sufficient for Slice 26.1 because it omits persistence-after-restart, remove/unselect, unselected app non-trigger, and Soft/Firm/Bedtime screenshots. It also exposes the default Firm-mode mismatch.

BUNDLE GAPS

BUNDLE GAP: Missing production source for AccountLightProfileExporter, AccountLightProfileImporter, AccountLightProfileCodec, profile DTOs, SupportedCatalog, MainActivity, analytics event/model definitions, and analytics tracker implementation.

BUNDLE GAP: No Gradle files, build configuration, or full source tree are shipped, so the test commands in SLICE26_1_VALIDATION.md cannot be independently rerun from the bundle.

BUNDLE GAP: No raw unit-test, lint, instrumentation, or git diff --check logs are included; only the validation summary claims pass results.

BUNDLE GAP: Full AccessibilityService-to-activity E2E cannot be proven because MainActivity is not shipped.

PACKAGE HYGIENE

PASS for scope containment: the bundle excludes website/domain implementation files, and the shipped code does not add URL/domain matching.

PASS for manifest package visibility restraint: AndroidManifest.xml uses <queries> for launchable and home activities rather than broad package visibility.

FAIL for audit completeness: the bundle is too partial to support the claimed Portable Profile and full E2E proof. This is acceptable for a narrow code-diff bundle only if explicitly treated as partial, but it is not sufficient for a release-gate PASS.

REQUIRED FIXES

Change the default intervention mode to Soft, update tests that currently assert Firm default, and regenerate the custom-app intervention screenshot so the default Open anyway path matches the PRD/plan.

Extend custom target exclusions to cover Android DocumentsUI / Files packages at minimum, including com.android.documentsui and com.google.android.documentsui, with disabled-row copy explaining that file picker and document-management flows must remain available.

Add direct tests for the real custom app eligibility policy, including self, launcher/home, Settings/permission, phone/emergency, installer/app-store, System UI/system-critical, and DocumentsUI/setup-critical packages.

Ship the actual Portable Profile exporter/importer/codec/model implementation files in the review bundle, or explicitly mark Portable Profile implementation as out of the auditable bundle with BUNDLE GAP.

Add or provide visual evidence for remove/unselect, persistence after restart, unselected app non-trigger, and Soft/Firm/Bedtime behavior for custom targets.

Include raw test/lint/instrumentation output or auditable result artifacts sufficient to verify the validation claims from the bundle.