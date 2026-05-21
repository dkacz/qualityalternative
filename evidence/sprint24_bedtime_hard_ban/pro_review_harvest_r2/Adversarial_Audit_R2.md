SCORE: 8/10

VERDICT: REVISE

VISUAL REVIEW: PASS

R1 BLOCKER RECHECK:

PASS for the exact R1 blocker in the steady-state AccessibilityService and ViewModel paths. InterceptionRuntimeGate.shouldSuppress() now rejects non-Bedtime suppressions when bedtimeActive=true, while honoring suppressions marked allowedDuringBedtime=true. QualityAlternativeAccessibilityService computes bedtimeActive before calling the gate, and MainViewModel.triggerIntervention() computes bedtimeActive before honoring runtime suppression. The shipped regression test MainViewModelTest.bedtimeModeIgnoresPreExistingNormalOpenAnywaySuppression directly covers a pre-existing normal suppression and asserts that the Bedtime intervention still appears.

R1 is not sufficient for PASS because a separate Bedtime-boundary bypass remains in openAnyway().

FRESH FINDINGS:

Severity: High — A normal intervention opened before Bedtime can still unlock after Bedtime becomes active without the one-minute emergency breath.
Exact claim: If a user triggers a normal Soft or Firm intervention shortly before Bedtime starts, waits on that already-rendered intervention until Bedtime is active, and then chooses Open Anyway, the ViewModel can return true and call the target-exit path without converting the action into a Bedtime emergency unlock.
Why it is vulnerable: triggerIntervention() computes bedtimeActive once at intervention creation and stores it in uiState.isBedtimeActive at MainViewModel.kt lines 1735-1740 and 1940-1948. The normal Open Anyway availability is also chosen at creation time at lines 1879-1887. openAnyway() then checks only the stored uiState.isBedtimeActive and the stored currentOpenAnywayUnlockAvailableAtMillis; it does not recompute active Bedtime before honoring the click. The recomputation at lines 2298-2303 happens only after the unlock has already been accepted. QualityAlternativeApp.kt lines 682-684 calls onExitToTarget() when viewModel.openAnyway() returns true. This conflicts with PRD.md lines 291-292, which require active Bedtime to allow the original app only through a visible one-minute wait.
Files checked: PRD.md; MainViewModel.kt; QualityAlternativeApp.kt; UserModels.kt; MainViewModelTest.kt; InterceptionRuntimeGate.kt.
Tightest fix: At the start of MainViewModel.openAnyway(), compute bedtimeActiveNow = bedtimeWindowIsActive(...) using nowMillis. If bedtimeActiveNow is true and the current intervention was not already a Bedtime intervention, do not return true; instead update the existing intervention into Bedtime mode, set currentOpenAnywayUnlockAvailableAtMillis = nowMillis + BEDTIME_OPEN_ANYWAY_UNLOCK_DELAY_MILLIS, set isBedtimeActive = true, keep the current alternatives visible, and require the one-minute emergency unlock. Add a unit regression that creates a normal Soft and Firm system intervention just before Bedtime, advances nowProvider into active Bedtime, calls openAnyway(), and asserts false, MainScreen.Intervention, isBedtimeActive=true, a fresh 60-second unlock wait, no runtime suppression, and no target-exit path.

Severity: Low — AccessibilityService reads Bedtime settings through a non-atomic multi-field snapshot.
Exact claim: A foreground event can theoretically observe selectedPackages, bedtimeEnabled, bedtimeStartMinutes, and bedtimeEndMinutes from different settings emissions while the service collector is updating them, which can make the service compute bedtimeActive=false for one event even though the imported or saved settings are active.
Why it is vulnerable: QualityAlternativeAccessibilityService.kt stores four separate @Volatile fields at lines 23-30 and assigns them sequentially at lines 35-41. onAccessibilityEvent() reads those fields separately at lines 52-68 before honoring runtime suppression. An interleaving during settings update could allow a normal suppression to be honored for a single active-Bedtime event.
Files checked: QualityAlternativeAccessibilityService.kt; InterceptionRuntimeGate.kt; PreferencesSettingsRepository.kt.
Tightest fix: Replace the separate volatile fields with one immutable volatile settings snapshot, for example InterceptionSettings(selectedPackages, bedtimeEnabled, bedtimeStartMinutes, bedtimeEndMinutes), assign it once per settings emission, and read that local snapshot once per accessibility event.

TRACE CHECKS:

README.md lines 5-11 define the slice contract: opt-in Bedtime sleep lock, alternatives preserved, finite quiet alternatives, Pause 15 min hidden, one-minute emergency breath, and local plus Portable Profile persistence.

PRD.md lines 99-102 allow optional Bedtime protection and require replacement plus meditation alternatives to remain available while the original app requires a visible one-minute emergency breath.

PRD.md lines 291-293 require active Bedtime to keep alternatives, hide Pause 15 min, require a one-minute emergency unlock wait, and then allow repeated opens to be quiet after Open Anyway.

UserModels.kt lines 28-33 prove Bedtime is off by default and the Bedtime unlock delay is 60_000L; lines 65-70 prove the ordinary intervention modes remain SOFT and FIRM, with default FIRM.

InterceptionRuntimeGate.kt lines 7-15 prove normal suppressions are not honored when bedtimeActive=true unless the suppression is marked allowedDuringBedtime=true; lines 19-27 store that marker.

QualityAlternativeAccessibilityService.kt lines 62-68 compute active Bedtime before calling InterceptionRuntimeGate.shouldSuppress(...).

MainViewModel.kt lines 1735-1748 compute active Bedtime before honoring runtime suppression in the ViewModel system-interception path.

MainViewModel.kt lines 1880-1887 set the Bedtime Open Anyway wait to BEDTIME_OPEN_ANYWAY_UNLOCK_DELAY_MILLIS; lines 1888-1905 record BEDTIME_INTERVENTION_SHOWN.

MainViewModel.kt lines 2193-2223 block premature Open Anyway attempts while the stored unlock wait is still active.

MainViewModel.kt lines 2249-2264 record BEDTIME_UNLOCK_USED when the stored state is Bedtime-active.

MainViewModel.kt lines 2284-2289 mark runtime suppressions as Bedtime-allowed only when uiState.isBedtimeActive is true.

QualityAlternativeApp.kt lines 1909-1913 separate meditation from ordinary backups; lines 2068-2077 render the meditation alternative; lines 2079-2081 label Bedtime backups as Quiet alternatives; lines 2130-2139 hide Pause 15 min; lines 2141-2153 render the disabled Bedtime emergency unlock action until the countdown expires.

InterceptionRuntimeGateTest.kt lines 26-45 assert that a normal unlock is ignored during Bedtime; lines 49-62 assert that a Bedtime emergency unlock is honored during Bedtime.

MainViewModelTest.kt lines 4286-4333 assert Bedtime alternatives, the 60-second wait, Bedtime unlock analytics, blocked early Open Anyway, and unlock after the wait.

MainViewModelTest.kt lines 4338-4372 assert the R1 blocker regression: a pre-existing normal suppression does not produce the “still unlocked” path during active Bedtime.

MainActivityTest.kt lines 556-600 assert the visual Bedtime screen, quiet alternatives, backup list, disabled emergency unlock, and absence of Pause 15 min.

connected_bedtime_e2e.xml lines 2-9 reports one connected test, zero failures, zero errors, and zero skipped tests.

connected_bedtime_e2e_logcat.txt lines 1, 572, 784, and 870 show the Bedtime E2E starting, capturing both screenshots, and finishing.

gradle_unit_compile_r2.log shows :app:testDebugUnitTest, :app:compileDebugAndroidTestKotlin, and BUILD SUCCESSFUL in 5s.

The two PNG screenshots are 1080×2400 RGB files; visual inspection confirms the Settings Bedtime section is active and the intervention shows primary reading, a separate Calm reset meditation card, quiet alternatives, no Pause 15 min, and a disabled Breathe 60s emergency unlock.

BEDTIME SUPPRESSION BEHAVIOR:

The R2 suppression model is materially improved. Normal Open Anyway suppressions are stored with allowedDuringBedtime=false by default, and the gate rejects them during active Bedtime. Bedtime emergency unlocks are stored with allowedDuringBedtime=true when uiState.isBedtimeActive is true, and the gate honors them during active Bedtime. The ViewModel system-interception path now computes bedtimeActive before suppression, so the exact R1 “still unlocked” bypass is closed in source and in the shipped unit regression.

The remaining failure is not the R1 pre-existing-suppression path; it is the stale-state Open Anyway path where an intervention that was normal at render time can be acted on after Bedtime becomes active.

SETTINGS/PERSISTENCE:

Bedtime is not default. DEFAULT_BEDTIME_ENABLED=false, default intervention mode remains FIRM, and the product has not converted the default experience into a hard block. Local persistence is implemented through PreferencesSettingsRepository.kt lines 82-88, 237-242, and 401-403. Portable Profile export/import includes Bedtime fields in AccountLightProfile.kt lines 137-139, validates Bedtime minute ranges at lines 171-175, imports them into AppSettings at lines 1514-1516, exports them at lines 1549-1551, and treats them as allowed settings keys at lines 2182-2188. The shipped export test asserts the Bedtime fields and empty warnings at AccountLightProfileExporterTest.kt lines 101-131.

ALTERNATIVES/MEDITATION:

The shipped UI satisfies the visual and source-level Bedtime alternative requirements for the tested fixture. Meditation is added to replacement inventory in MainViewModel.kt lines 3621-3622, allowed through filtering at lines 1798-1805, split out of the normal backup list in QualityAlternativeApp.kt lines 1909-1913, and rendered as a distinct calm-reset card at lines 2068-2077. Backup alternatives remain visible under Quiet alternatives, and Pause 15 min is not rendered during Bedtime. The screenshot 02_intervention_bedtime_hard_ban_alternatives.png visually confirms those properties.

TEST/EVIDENCE:

The shipped unit and compile logs pass, the R1 blocker regression is present in source, and the connected Bedtime UI test passes. The test evidence is adequate for the exact R1 blocker, local persistence, Portable Profile export inclusion, the 60-second Bedtime wait, hidden Pause action, and visual Bedtime composition.

The test evidence does not cover the new pre-Bedtime-intervention/active-Bedtime-click boundary. No shipped test advances time between normal intervention rendering and openAnyway() execution to prove that the ViewModel recomputes Bedtime at click time.

BUNDLE GAPS:

MainActivity.kt is not included, so the exact implementation of onExitToTarget() and MainActivity.createSystemInterceptionIntent(...) cannot be audited from shipped files. The shipped UI source proves that onExitToTarget() is called when viewModel.openAnyway() returns true, but the target-launch mechanics are a BUNDLE GAP.

The production RecommendationEngine implementation is not included. The shipped ViewModel includes meditation in inventory and the E2E fixture shows meditation, but a universal production guarantee that meditation is always selected as a visible alternative is a BUNDLE GAP.

There is no connected or service-level regression that exercises the real AccessibilityService with a normal runtime suppression during active Bedtime. The source and ViewModel/gate unit tests support the fix, but the end-to-end service path is source-inspected rather than behavior-tested.

PACKAGE HYGIENE:

No package-hygiene blocker found. The bundle is scoped to 33 files, contains the requested PRD, README, R1 audit scope, R2 source files, tests, logs, XML, logcat, manifest, patch, and exactly two PNG screenshots. The retained R1 prompt/scope and pre-R2 validation log are potentially noisy, but they are named and contextualized by the README and do not conflict with the shipped source or R2 evidence.