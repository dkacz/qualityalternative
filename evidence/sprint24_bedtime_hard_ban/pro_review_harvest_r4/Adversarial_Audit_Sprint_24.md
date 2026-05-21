SCORE: 8/10

VERDICT: REVISE

VISUAL REVIEW: PASS

R1 BLOCKER RECHECK:

REVISE. The direct R1 runtime-suppression path is fixed in the shipped gate and ViewModel source: InterceptionRuntimeGate.shouldSuppress() rejects normal suppressions during active Bedtime unless allowedDuringBedtime=true (InterceptionRuntimeGate.kt:7-15), MainViewModel.triggerIntervention() computes bedtimeActive before consulting the runtime gate (MainViewModel.kt:1737-1750), and legitimate Bedtime emergency unlocks create suppressions with allowedDuringBedtime = uiState.currentInterventionBedtimeEnforced (MainViewModel.kt:2329-2334). The unit coverage directly asserts normal suppression rejection and Bedtime emergency suppression acceptance (InterceptionRuntimeGateTest.kt:26-63) and verifies that a pre-existing normal suppression does not produce the “still unlocked” path in the ViewModel (MainViewModelTest.kt:4342-4378).

However, R1 is not fully closed in the shipped AccessibilityService path because QualityAlternativeAccessibilityService still invokes ForegroundAppDetectionPolicy.shouldLog() before computing Bedtime and before invoking InterceptionRuntimeGate.shouldSuppress() (QualityAlternativeAccessibilityService.kt:55-66). The shipped ForegroundAppDetectionPolicy de-duplicates the same selected package for 1.5 seconds without Bedtime awareness (ForegroundAppDetectionPolicy.kt:14-24), which leaves a narrow normal-suppression-to-Bedtime boundary bypass described in Fresh Finding 1.

R2 BLOCKER RECHECK:

PASS for the exact stale normal-intervention Open Anyway click bypass. MainViewModel.openAnyway() recomputes active Bedtime at click time (MainViewModel.kt:2201-2207) and converts whenever bedtimeActiveNow && !uiState.currentInterventionBedtimeEnforced (MainViewModel.kt:2208-2236), regardless of whether global isBedtimeActive has already been updated. The conversion sets currentInterventionBedtimeEnforced=true, writes a fresh now + BEDTIME_OPEN_ANYWAY_UNLOCK_DELAY_MILLIS gate, records BEDTIME_INTERVENTION_SHOWN, and returns false rather than exiting to the target app. The regression test covers Soft and Firm interventions crossing from 21:59 to 22:01 and asserts no runtime suppression and no OPEN_ANYWAY_SELECTED event before the one-minute Bedtime gate is installed (MainViewModelTest.kt:4382-4444).

R3 BLOCKER RECHECK:

PASS for the exact settings-emission/global-state Open Anyway bypass. MainUiState now separates global isBedtimeActive from currentInterventionBedtimeEnforced (MainViewModel.kt:164-168). applySettings() updates the global Bedtime status but does not mark the current intervention as Bedtime-enforced (MainViewModel.kt:3054-3076). openAnyway() keys conversion, blocking, Bedtime unlock analytics, Firm analytics avoidance, and runtime suppression marking off currentInterventionBedtimeEnforced (MainViewModel.kt:2208-2236, 2241-2267, 2273-2334). The R4 regression test creates Soft and Firm normal interventions before Bedtime, advances into Bedtime, emits settings through setMeditationDurationMinutes(5), verifies isBedtimeActive=true while currentInterventionBedtimeEnforced=false, and then verifies that openAnyway() converts to a fresh one-minute Bedtime gate rather than unlocking (MainViewModelTest.kt:4448-4515).

FRESH FINDINGS:

Severity: High — AccessibilityService duplicate detection can still let a pre-Bedtime normal quiet-unlock event suppress the first active-Bedtime service interception.

Exact claim: A selected target-app foreground event observed shortly before Bedtime can be quieted by a normal Open Anyway suppression, and a second foreground event for the same app within 1.5 seconds after Bedtime begins can be dropped by duplicate detection before the service computes active Bedtime or applies the Bedtime-aware runtime gate.

Why it is vulnerable: QualityAlternativeAccessibilityService.onAccessibilityEvent() resolves the target app, captures nowMillis, then calls detectionPolicy.shouldLog(...) and returns immediately on duplicate (QualityAlternativeAccessibilityService.kt:49-57). Only after that return point does it compute bedtimeActive and ask InterceptionRuntimeGate.shouldSuppress(..., bedtimeActive = bedtimeActive) (QualityAlternativeAccessibilityService.kt:59-66). The shipped ForegroundAppDetectionPolicy treats a same-package event as duplicate whenever nowMillis - lastSeenAtMillis < 1_500L, updates lastSeenPackage/lastSeenAtMillis, and has no Bedtime parameter or suppression-origin awareness (ForegroundAppDetectionPolicy.kt:14-24). A normal suppression that is valid at 21:59:59 can therefore cause the first event to return through the runtime gate while Bedtime is inactive; a second event at 22:00:00 can then return through duplicate detection before the Bedtime-aware gate gets a chance to reject that normal suppression. This is a narrow boundary window, but it is a source-proven hard-ban bypass in the service path and is not covered by the shipped tests.

Files checked: QualityAlternativeAccessibilityService.kt, ForegroundAppDetectionPolicy.kt, InterceptionRuntimeGate.kt, MainViewModel.kt, InterceptionRuntimeGateTest.kt, MainViewModelTest.kt, PRD.md.

Tightest fix: Compute Bedtime before duplicate detection and make duplicate suppression incapable of preempting active Bedtime unless there is a legitimate Bedtime emergency unlock suppression. The minimal safe ordering is: resolve target, compute bedtimeActive, consult InterceptionRuntimeGate.shouldSuppress(target, now, bedtimeActive), and only then apply foreground duplicate suppression to avoid duplicate logs/launches. Alternatively, pass bedtimeActive and the last event’s Bedtime status into ForegroundAppDetectionPolicy, and force shouldLog=true when the current event is active Bedtime but the previous same-package event was not.

Severity: Medium — A stale pre-Bedtime normal intervention can still display and execute Pause 15 min during active Bedtime.

Exact claim: If a normal Soft or Firm intervention is rendered before Bedtime, then Bedtime becomes active and a settings emission updates global isBedtimeActive while leaving currentInterventionBedtimeEnforced=false, the current screen remains a normal intervention for UI purposes. In that state, Pause 15 min remains visible and delayFor15Minutes() can store a normal delay window, despite the Bedtime requirement that Pause be hidden because Bedtime is the active delay condition.

Why it is vulnerable: The R4 regression itself proves the reachable intermediate state: after setMeditationDurationMinutes(5), isBedtimeActive is true while currentInterventionBedtimeEnforced is false (MainViewModelTest.kt:4488-4492). InterventionScreen derives isBedtime only from state.currentInterventionBedtimeEnforced (QualityAlternativeApp.kt:1909) and renders Pause 15 min whenever !isBedtime (QualityAlternativeApp.kt:2130-2139). delayFor15Minutes() does not recompute Bedtime or convert the intervention before storing a delay window; it records/stores the delay and clears the intervention (MainViewModel.kt:2032-2059). This does not open the original app, so it is less severe than the Open Anyway bypasses, but it violates the slice contract and PRD that active Bedtime hides Pause (README.md:5-10; PRD.md:291).

Files checked: MainViewModel.kt, QualityAlternativeApp.kt, MainViewModelTest.kt, README.md, PRD.md.

Tightest fix: Factor the click-time Bedtime conversion into a shared helper, for example ensureCurrentInterventionBedtimeEnforced(nowMillis): Boolean, and call it from both openAnyway() and delayFor15Minutes() before any normal action is honored. For UI correctness, also trigger conversion when a settings emission or clock tick makes Bedtime active while screen == MainScreen.Intervention, currentInterventionOrigin == SYSTEM, and currentInterventionBedtimeEnforced == false; this hides Pause and replaces the stale Soft/Firm affordance with the one-minute emergency gate without waiting for an Open Anyway click.

TRACE CHECKS:

README.md:5-11 defines the Sprint 24 contract: opt-in Bedtime sleep lock, reading and meditation alternatives preserved, finite quiet backups, Pause 15 min hidden, visible one-minute emergency breath, and persistence through local settings plus Portable Profile.

PRD.md:99-102 allows optional Bedtime protection while preserving replacement and meditation alternatives and requiring a visible one-minute emergency breath before opening the original app.

PRD.md:258-263 requires exactly one primary recommendation, bounded backups, and a separate meditation calm-reset alternative when primary is reading.

PRD.md:291-293 requires active Bedtime to keep alternatives available, hide Pause, require a visible one-minute emergency unlock wait, and quiet repeated opens only after Open Anyway from a system intervention.

PRD.md:356-357 confirms Soft/Firm remain normal modes, with Bedtime opt-in and not the default intervention mode.

UserModels.kt:28-33 confirms Bedtime defaults off and BEDTIME_OPEN_ANYWAY_UNLOCK_DELAY_MILLIS = 60_000L.

UserModels.kt:65-70 confirms ordinary intervention modes remain SOFT and FIRM, with default FIRM.

MainUiState includes both isBedtimeActive and currentInterventionBedtimeEnforced (MainViewModel.kt:164-168).

MainViewModel.triggerIntervention() computes Bedtime before runtime suppression, skips normal delay handling during active Bedtime, and marks a newly shown Bedtime intervention as enforced (MainViewModel.kt:1737-1750, 1767-1799, 1884-1957).

MainViewModel.openAnyway() recomputes Bedtime at click time, converts stale normal interventions into Bedtime-enforced interventions, blocks early Bedtime unlocks, records Bedtime unlock analytics, and creates Bedtime-allowed runtime suppression only after legitimate Bedtime unlock (MainViewModel.kt:2198-2352).

QualityAlternativeAccessibilityService uses a single immutable settings snapshot (QualityAlternativeAccessibilityService.kt:23-36, 48-54), but it still applies duplicate detection before Bedtime computation (QualityAlternativeAccessibilityService.kt:55-66).

ForegroundAppDetectionPolicy is shipped in R4 and de-duplicates same-package events for 1.5 seconds without Bedtime awareness (ForegroundAppDetectionPolicy.kt:14-24).

QualityAlternativeApp.kt:1909-1913 derives intervention UI Bedtime state from currentInterventionBedtimeEnforced and filters meditation out of ordinary backup rows.

QualityAlternativeApp.kt:2068-2077 renders the meditation alternative as a separate card.

QualityAlternativeApp.kt:2079-2113 labels Bedtime backup rows as quiet alternatives and renders a bounded backup list.

QualityAlternativeApp.kt:2130-2139 hides Pause 15 min only when currentInterventionBedtimeEnforced is true.

QualityAlternativeApp.kt:2141-2157 renders the Bedtime emergency unlock action and disables it until the countdown expires.

DefaultRecommendationEngine.kt:91-120 caps backups at six and preserves a meditation backup when eligible.

MeditationReplacement.kt:3-24 defines the meditation timer item, constants, and bounded duration coercion.

InterceptionRuntimeGateTest.kt:26-63 tests normal suppression rejection during Bedtime and Bedtime emergency suppression acceptance.

MainViewModelTest.kt:4289-4338 tests alternatives, 60-second Bedtime wait, early unlock blocking, unlock enabled/used analytics, and absence of Firm unlock-used analytics.

MainViewModelTest.kt:4342-4378 tests the R1 pre-existing normal suppression regression in the ViewModel path.

MainViewModelTest.kt:4382-4444 tests Soft and Firm stale normal interventions crossing into Bedtime before Open Anyway.

MainViewModelTest.kt:4448-4515 tests the R3 settings-emission/global-state bypass and confirms R4’s per-intervention enforcement marker.

MainActivityTest.kt:556-600 tests the connected Bedtime visual path, including settings active, Bedtime heading, quiet alternatives, disabled emergency unlock, and absence of Pause 15 min.

MainActivityTest.kt:1947-1967 tests that meditation is shown as a distinct calm alternative and not duplicated as a normal backup action.

connected_bedtime_e2e.xml reports one connected test, zero failures, zero errors, and zero skips.

connected_bedtime_e2e_logcat.txt:1, 572, 784, and 870 show connected test start, both screenshot captures, and test finish.

The PNG screenshots are both 1080×2400 RGB/sRGB files.

BEDTIME SUPPRESSION / BOUNDARY BEHAVIOR:

The core ViewModel and runtime gate behavior is materially correct for the prior blockers. Normal Open Anyway suppressions are not honored during active Bedtime; legitimate Bedtime emergency unlocks are honored during active Bedtime; stale pre-Bedtime Soft/Firm Open Anyway clicks are converted to a fresh one-minute Bedtime gate; and the R3 settings-emission path no longer bypasses the one-minute gate because conversion uses currentInterventionBedtimeEnforced rather than global isBedtimeActive.

The remaining suppression blocker is service-side ordering: foreground duplicate detection can return before active Bedtime is computed and before the Bedtime-aware runtime gate runs. The remaining boundary UI defect is that stale pre-Bedtime screens can still expose normal actions, especially Pause 15 min, until an Open Anyway click forces conversion.

SETTINGS/PERSISTENCE:

Bedtime is opt-in, not default: DEFAULT_BEDTIME_ENABLED=false and default intervention mode remains FIRM (UserModels.kt:28-33, 65-70). Local persistence reads, writes, and bounds Bedtime settings through PreferencesSettingsRepository (PreferencesSettingsRepository.kt:80-88, 237-242, 377-379, 401-403). The local persistence regression test verifies saveBedtimeSettings() does not reset onboarding and persists start/end values (PreferencesSettingsRepositoryTest.kt:207-228).

Portable Profile support includes the Bedtime fields in AccountLightSettings with defaults (AccountLightProfile.kt:136-139), validates Bedtime minute ranges (AccountLightProfile.kt:171-175), imports them into AppSettings (AccountLightProfile.kt:1513-1516), exports them from AppSettings (AccountLightProfile.kt:1548-1551), and lists them as allowed settings keys so the new fields are not unknown-field warning noise (AccountLightProfile.kt:2168-2188). The export test verifies Bedtime values and empty warnings in the settings-only profile path (AccountLightProfileExporterTest.kt:58-117, 131).

ALTERNATIVES/MEDITATION:

The shipped source and visual evidence satisfy the main Bedtime alternatives requirement. fullReplacementInventory() adds the meditation timer to ordinary inventory (MainViewModel.kt:3668-3669), the recommendation engine caps backups at six and forces meditation into backups when eligible (DefaultRecommendationEngine.kt:91-120, 204-207), the UI extracts meditation from backup rows and renders it separately (QualityAlternativeApp.kt:1909-1913, 2068-2077), and the visual intervention screenshot shows primary reading, a distinct “Calm reset” meditation card, quiet alternatives, no visible Pause 15 min, and a disabled “Breathe 60s” emergency unlock action.

TEST/EVIDENCE:

The shipped R4 validation log reports :app:testDebugUnitTest and :app:compileDebugAndroidTestKotlin with BUILD SUCCESSFUL in 6s (gradle_unit_compile_r4.log:37-49). The R1, R2, and R3 targeted regression logs each report :app:testDebugUnitTest with BUILD SUCCESSFUL (gradle_r1_blocker_regression.log:37-40; gradle_r2_blocker_regression.log:37-40; gradle_r3_blocker_regression.log:37-40). The connected XML reports one Bedtime E2E test with zero failures/errors/skips, and the logcat confirms screenshot capture and test completion.

The shipped tests cover the exact Open Anyway bypasses from R1, R2, and R3 at the ViewModel/runtime-gate level. They do not cover the service-side foreground duplicate ordering boundary, and they do not cover Pause 15 min visibility/actionability after a stale pre-Bedtime intervention crosses into active Bedtime.

BUNDLE GAPS:

BUNDLE GAP: The bundle is scoped and not self-contained for independent compilation or execution because Gradle project files, manifests, dependency configuration, and several app-wide implementation files are omitted. I used shipped source plus shipped Gradle logs rather than rerunning the build from the bundle.

BUNDLE GAP: InterceptionTargetResolver is referenced by QualityAlternativeAccessibilityService.kt:49-54 but is not shipped, so target-resolution correctness cannot be independently proven from the bundle.

BUNDLE GAP: The Gradle logs show task success but do not include full unit-test XML or per-test result files; exact unit-test pass evidence is inferred from source test names, README command lines, and task success logs.

BUNDLE GAP: The connected XML includes only bedtimeModeShowsCalmHardBanWithAlternativesAndEmergencyWait; other Android tests are shipped as source and compiled, but their connected execution results are not included.

PACKAGE HYGIENE:

No stale APKs, AABs, build outputs, class files, JARs, .DS_Store files, temporary files, unrelated sprint archives, or obvious noisy package artifacts were found in the extracted bundle. The archive contains 43 files, matching the scoped bundle shape described in BUNDLE_MANIFEST.md. ForegroundAppDetectionPolicy.kt and MeditationReplacement.kt are present in R4, closing the prior R3 bundle omissions for those two files. adb_devices_before_shutdown.txt shows emulator-5554 device, and adb_devices_after_shutdown.txt shows no attached devices, matching the README emulator-shutdown note.