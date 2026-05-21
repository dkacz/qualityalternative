SCORE: 8/10

VERDICT: REVISE

VISUAL REVIEW: PASS

R1 BLOCKER RECHECK:

PASS for the exact R1 runtime-suppression blocker. InterceptionRuntimeGate.shouldSuppress() now rejects a normal suppression during active Bedtime when allowedDuringBedtime=false, while honoring a suppression created by a legitimate Bedtime emergency unlock when allowedDuringBedtime=true (InterceptionRuntimeGate.kt:7-15, 19-27). MainViewModel.triggerIntervention() computes bedtimeActive before consulting the runtime gate (MainViewModel.kt:1735-1748), and active Bedtime skips the normal 15-minute delay path (MainViewModel.kt:1764-1795). QualityAlternativeAccessibilityService also computes active Bedtime before calling the runtime gate (QualityAlternativeAccessibilityService.kt:59-66).

The shipped regression evidence covers this: InterceptionRuntimeGateTest.kt:26-63 tests normal suppression rejection and Bedtime emergency suppression acceptance; MainViewModelTest.kt:4341-4375 preloads a normal suppression and verifies a Bedtime intervention still appears with a fresh 60-second unlock wait.

R2 BLOCKER RECHECK:

PASS for the exact R2 stale normal-intervention click path covered by R3. openAnyway() recomputes active Bedtime at click time (MainViewModel.kt:2193-2198), converts a previously normal intervention into Bedtime mode when bedtimeActiveNow && !uiState.isBedtimeActive (MainViewModel.kt:2199-2227), preserves the current recommendation set by not replacing currentRecommendationSet, records BEDTIME_INTERVENTION_SHOWN, sets currentOpenAnywayUnlockAvailableAtMillis = now + 60_000L, and returns false. MainViewModelTest.kt:4380-4438 covers both SOFT and FIRM interventions crossing from 21:59 to 22:01 and verifies no OPEN_ANYWAY_SELECTED event and no runtime suppression.

REVISE remains necessary because a related stale-normal-intervention boundary remains when a settings emission changes uiState.isBedtimeActive before openAnyway() runs, as detailed below.

FRESH FINDINGS:

Severity: High — active Bedtime can still be bypassed from a pre-Bedtime normal intervention if a settings emission updates uiState.isBedtimeActive before Open anyway is clicked.

Exact claim: A user can trigger a normal Soft or Firm intervention before Bedtime, wait until Bedtime is active, cause any settings emission from the intervention itself, and then use Open anyway without receiving a fresh one-minute emergency unlock wait. The most direct shipped path is changing the meditation duration from the intervention’s calm-reset controls after Bedtime has become active.

Why it is vulnerable: R3’s conversion guard in openAnyway() depends on !uiState.isBedtimeActive (MainViewModel.kt:2199). That field is not a durable marker of whether the current intervention was originally shown as Bedtime; it is also updated globally whenever settings are applied. applySettings() recomputes isBedtimeActive from the current clock on every settings emission (MainViewModel.kt:3043-3065) while preserving the existing currentOpenAnywayUnlockAvailableAtMillis and currentRecommendationSet. From the intervention UI, meditation duration selection is wired to viewModel.setMeditationDurationMinutes (QualityAlternativeApp.kt:674-680), and the intervention exposes meditation duration controls through the calm-reset card (QualityAlternativeApp.kt:2068-2077, 5581-5585). setMeditationDurationMinutes() saves settings (MainViewModel.kt:586-604), and the repository emits updated settings (PreferencesSettingsRepository.kt:52-88, 194-197). After that emission, uiState.isBedtimeActive can become true without setting a Bedtime unlock gate. A Soft intervention still has currentOpenAnywayUnlockAvailableAtMillis=null; a Firm intervention may still have only the old five-second wait from MainViewModel.kt:1880-1887. Because openAnyway() now sees bedtimeActiveNow=true and uiState.isBedtimeActive=true, it skips the R3 conversion block and proceeds to unlock using the stale normal availability state (MainViewModel.kt:2228-2342). It can also record BEDTIME_UNLOCK_USED and create a runtime suppression allowed during Bedtime even though the one-minute wait was never enforced (MainViewModel.kt:2284-2299, 2320-2324).

Files checked: MainViewModel.kt, QualityAlternativeApp.kt, PreferencesSettingsRepository.kt, MainViewModelTest.kt, InterceptionRuntimeGate.kt, UserModels.kt, PRD.md.

Tightest fix: stop using uiState.isBedtimeActive as the marker for whether the current intervention already has Bedtime enforcement. Add an immutable per-intervention field such as currentInterventionUnlockPolicy = NORMAL | FIRM_WAIT | BEDTIME_EMERGENCY_WAIT or currentInterventionBedtimeEnforced: Boolean, set it when the intervention is created, and update it only through an atomic Bedtime conversion path. openAnyway() should convert whenever bedtimeActiveNow is true and the current intervention does not already have a Bedtime emergency-wait policy, regardless of the global isBedtimeActive display flag. Add a regression test that creates a Soft and Firm normal system intervention at 21:59, advances to 22:01, calls setMeditationDurationMinutes(...) or otherwise emits settings, then asserts that openAnyway() returns false, currentOpenAnywayUnlockAvailableAtMillis == now + BEDTIME_OPEN_ANYWAY_UNLOCK_DELAY_MILLIS, OPEN_ANYWAY_SELECTED is absent, and no runtime suppression is created.

TRACE CHECKS:

README.md:5-11 defines the slice contract: opt-in Bedtime sleep lock, alternatives preserved, quiet finite backups, Pause 15 min hidden, one-minute emergency breath, and local plus Portable Profile persistence.

README.md:47-53 states the intended R3 fixes: click-time Bedtime recomputation, conversion of stale normal interventions, preserved alternatives, fresh one-minute wait, Bedtime analytics, settings snapshot consolidation, and Soft/Firm regression coverage.

PRD.md:100-102, 291-293, and 356-357 require optional Bedtime protection, preserved replacement and meditation alternatives, hidden Pause, visible one-minute emergency unlock before original app access, quiet repeated opens only after Open anyway, and Bedtime not being the default intervention mode.

UserModels.kt:28-33 confirms Bedtime defaults off, default schedule values, and BEDTIME_OPEN_ANYWAY_UNLOCK_DELAY_MILLIS = 60_000L; UserModels.kt:65-70 confirms default intervention mode remains FIRM, not hard-block.

InterceptionRuntimeGate.kt:7-15 and 19-27 implement suppression windows with allowedDuringBedtime.

QualityAlternativeAccessibilityService.kt:23-36 uses a single immutable volatile settings snapshot, and QualityAlternativeAccessibilityService.kt:48-66 reads one local snapshot before resolving active Bedtime and consulting the runtime gate.

MainViewModel.kt:1735-1748 computes Bedtime before runtime suppression; 1764-1795 skips normal delay handling during Bedtime; 1880-1887 selects 60 seconds for Bedtime and five seconds for Firm; 1888-1905 records Bedtime shown analytics.

MainViewModel.kt:2193-2227 contains the R3 click-time conversion logic; 2228-2258 blocks premature unlocks; 2263-2324 records unlock analytics and creates runtime suppression; 2326-2342 clears the intervention after successful unlock.

QualityAlternativeApp.kt:2034-2043 renders Bedtime copy; 2068-2077 renders the distinct meditation alternative; 2079-2113 renders quiet alternatives/backups; 2130-2139 hides Pause 15 min during Bedtime; 2141-2153 renders the Bedtime emergency unlock action.

MainViewModelTest.kt:4289-4337 verifies Bedtime alternatives, 60-second wait, blocked immediate unlock, enabled unlock, and Bedtime analytics; 4341-4375 covers the R1 normal-suppression crossing case; 4380-4438 covers the R2 Soft/Firm stale normal-intervention click case.

InterceptionRuntimeGateTest.kt:26-63 directly verifies normal suppression rejection and legitimate Bedtime suppression acceptance.

MainActivityTest.kt:556-600 verifies the visual Bedtime settings and intervention path, quiet alternatives, disabled emergency unlock, and absence of Pause 15 min; MainActivityTest.kt:1947-1967 verifies meditation is shown as a separate calm alternative and not duplicated as a normal backup row.

gradle_unit_compile_r3.log reports :app:testDebugUnitTest and :app:compileDebugAndroidTestKotlin with BUILD SUCCESSFUL.

gradle_r2_blocker_regression.log reports the targeted R2 regression task with BUILD SUCCESSFUL.

connected_bedtime_e2e.xml reports one connected Bedtime E2E test, zero failures, zero errors, and zero skips.

connected_bedtime_e2e_logcat.txt:1, 572, 784, and 870 show the connected test start, both screenshot captures, and test finish.

Both PNG screenshots were inspected visually. 01_settings_bedtime_enabled.png is 1080×2400 and shows Bedtime active, On selected, and all-day deterministic schedule copy. 02_intervention_bedtime_hard_ban_alternatives.png is 1080×2400 and shows Bedtime header copy, primary reading, calm-reset meditation, quiet alternatives, no Pause 15 min, and a disabled Breathe 60s emergency unlock.

BEDTIME SUPPRESSION / BOUNDARY BEHAVIOR:

The original R1 suppression bypass is closed in the shipped gate and ViewModel path: active Bedtime ignores normal suppressions, while legitimate Bedtime emergency unlocks can still quiet repeated opens because openAnyway() passes allowedDuringBedtime = uiState.isBedtimeActive when the unlock was a Bedtime unlock (MainViewModel.kt:2320-2324). The exact R2 click-time stale intervention case is also closed for a normal state that still has uiState.isBedtimeActive=false at click time.

The remaining boundary defect is that uiState.isBedtimeActive is a live settings-derived status, not a per-intervention enforcement marker. A settings emission can make the R3 guard believe the existing intervention is already converted to Bedtime even when it still has Soft immediate availability or Firm five-second availability.

SETTINGS/PERSISTENCE:

Local settings persistence is implemented. PreferencesSettingsRepository reads Bedtime fields with safe defaults and bounds (PreferencesSettingsRepository.kt:82-88), writes them through saveBedtimeSettings() (PreferencesSettingsRepository.kt:237-242), and defines dedicated DataStore keys (PreferencesSettingsRepository.kt:401-403).

Portable Profile export/import includes the new Bedtime fields. AccountLightSettings includes bedtimeEnabled, bedtimeStartMinutes, and bedtimeEndMinutes with defaults (AccountLightProfile.kt:137-139), validates minute ranges (AccountLightProfile.kt:171-175), maps them into AppSettings on import (AccountLightProfile.kt:1514-1516), maps them out on export (AccountLightProfile.kt:1549-1551), and treats them as allowed settings keys rather than unknown warning noise (AccountLightProfile.kt:2182-2188). AccountLightProfileExporterTest.kt:58-117 verifies export of Bedtime values and zero warnings in the settings-only profile path. PreferencesSettingsRepositoryTest.kt:207-228 verifies local Bedtime persistence without resetting onboarding.

ALTERNATIVES/MEDITATION:

The intervention UI preserves primary reading, a separate meditation calm-reset alternative, and finite quiet backups. The UI filters meditation out of the normal backup rows and renders it separately (QualityAlternativeApp.kt:1909-1913, 2068-2077), labels the backup list as Quiet alternatives during Bedtime (QualityAlternativeApp.kt:2079-2081), and keeps the bounded backup list in a LazyColumn (QualityAlternativeApp.kt:2090-2113). DefaultRecommendationEngine.kt:91-120 caps backups at six and ensures a meditation backup is included when the primary is not meditation and meditation is otherwise available. The second screenshot visually confirms the intended layout: primary reading, “Calm reset” meditation, quiet alternatives, and a disabled Breathe 60s action.

TEST/EVIDENCE:

The R3 source includes direct unit coverage for the R1 and R2 blockers and a connected visual E2E for the Bedtime hard-ban presentation. The logs show successful unit-test and Android-test compilation, and the connected XML shows the Bedtime E2E passed. The R3 test set does not cover the fresh finding above: there is no test that creates a normal pre-Bedtime intervention, advances into Bedtime, emits settings from the intervention, and then calls openAnyway().

BUNDLE GAPS:

BUNDLE GAP: ForegroundAppDetectionPolicy is referenced by QualityAlternativeAccessibilityService.kt:22 and can return before Bedtime and runtime suppression are evaluated (QualityAlternativeAccessibilityService.kt:56-57), but its implementation is not shipped. Therefore, the AccessibilityService path cannot be fully proven against a bedtime-unaware foreground de-duplication bypass from shipped files alone.

BUNDLE GAP: the shipped source slice is not self-contained for compilation. Relevant referenced Sprint 24/Sprint 21 symbols such as MEDITATION_TIMER_CONTENT_ID, meditationTimerContentItem(...), DEFAULT_MEDITATION_MINUTES, MIN_MEDITATION_MINUTES, and MAX_MEDITATION_MINUTES are used in shipped files but their definitions are not present in the bundle. Visual screenshots, tests, and Gradle logs support the meditation behavior, but the exact helper definitions cannot be source-reviewed from this bundle.

BUNDLE GAP: the Gradle logs show task success but do not include full unit-test XML or per-test result listings. The exact targeted test list is supported by the README and source test names, while the log itself only proves the Gradle test task succeeded.

BUNDLE GAP: the connected XML includes only bedtimeModeShowsCalmHardBanWithAlternativesAndEmergencyWait; other Android tests are shipped and compiled, but their connected execution results are not included.

PACKAGE HYGIENE:

No stale APKs, build outputs, unrelated sprint archives, or obvious noisy package artifacts were found in the shipped file set. BUNDLE_MANIFEST.md accurately describes a scoped review bundle, and the file list is limited to the PRD, scoped source and tests, review prompts, diff, logs, connected XML/logcat, ADB device state files, prior review harvests, and two PNG screenshots. adb_devices_before_shutdown.txt shows emulator-5554 device, and adb_devices_after_shutdown.txt shows no attached devices, matching the README’s emulator shutdown note. Logcat contains normal emulator/system-service noise, but the test-runner start/finish markers and screenshot capture lines are unambiguous.