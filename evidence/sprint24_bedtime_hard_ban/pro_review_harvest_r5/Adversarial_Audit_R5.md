SCORE: 8/10

VERDICT: REVISE

VISUAL REVIEW: PASS

R1/R2/R3/R4 BLOCKER RECHECK:

R1 normal runtime suppression bypass: PASS for the core runtime gate and ViewModel path. InterceptionRuntimeGate.shouldSuppress(..., bedtimeActive = true) rejects normal suppressions unless allowedDuringBedtime=true, and MainViewModel.triggerIntervention() computes Bedtime before checking the runtime gate. Regression coverage exists in InterceptionRuntimeGateTest.kt:26-63 and MainViewModelTest.kt:4342-4378.

R2 stale normal-intervention Open Anyway bypass: PASS. MainViewModel.openAnyway() calls ensureCurrentInterventionBedtimeEnforced() before honoring Open Anyway, converts stale normal interventions to Bedtime-enforced interventions, records Bedtime shown analytics, installs a fresh 60-second wait, and returns false. Coverage exists in MainViewModelTest.kt:4382-4444.

R3 settings-emission/global-state Open Anyway bypass: PASS. isBedtimeActive and currentInterventionBedtimeEnforced are separated, and applySettings() now calls the shared helper after settings emission. Coverage exists in MainViewModelTest.kt:4448-4518.

R4 service duplicate-ordering and stale Pause blockers: REVISE. R5 fixes the exact order by moving Bedtime computation and InterceptionRuntimeGate.shouldSuppress(..., bedtimeActive) before ForegroundAppDetectionPolicy.shouldLog() in QualityAlternativeAccessibilityService.kt:55-66, and delayFor15Minutes() now refuses to store a normal delay after Bedtime enforcement in MainViewModel.kt:2032-2039. However, active-Bedtime duplicate suppression can still drop a boundary event after the runtime gate rejects a normal suppression, and a pre-Bedtime intervention still does not automatically convert on a pure clock transition without a settings emission or user action.

FRESH FINDINGS:

Severity: High — Active-Bedtime service interception can still be dropped by foreground duplicate detection after the runtime gate rejects a normal suppression.

Exact claim: R5 closes the prior “duplicate detection before runtime gate” ordering bug, but the service can still return through duplicate detection during active Bedtime when the same package was last logged just before Bedtime. In a narrow but source-supported sequence, a Soft normal intervention shown immediately before Bedtime can set ForegroundAppDetectionPolicy.lastSeenPackage, a fast normal Open Anyway can create a non-Bedtime runtime suppression, and the first post-Bedtime foreground event can be rejected by InterceptionRuntimeGate as not suppressible during Bedtime but then still dropped as a duplicate.

Why it is vulnerable: QualityAlternativeAccessibilityService.onAccessibilityEvent() now computes bedtimeActive and checks InterceptionRuntimeGate.shouldSuppress(...) before duplicate detection, which is the R5 ordering fix (QualityAlternativeAccessibilityService.kt:55-66). However, ForegroundAppDetectionPolicy.shouldLog() still has no Bedtime parameter and suppresses any same-package event within 1,500 ms of the last logged same-package event (ForegroundAppDetectionPolicy.kt:14-20). Because that last-seen state can be established by a pre-Bedtime service event that showed the normal intervention, a later active-Bedtime event can reach duplicate detection after the runtime gate has refused to honor the normal suppression, then return at QualityAlternativeAccessibilityService.kt:65-66. Soft mode permits immediate normal Open Anyway when not Bedtime-enforced because openAnywayDelayMillis is null outside Bedtime/Firm mode (MainViewModel.kt:1885-1889), and the UI enables the normal open action when no wait remains (QualityAlternativeApp.kt:2141-2153). The result is a remaining service-path hard-ban boundary bypass, not covered by shipped tests.

Files checked: QualityAlternativeAccessibilityService.kt, ForegroundAppDetectionPolicy.kt, InterceptionRuntimeGate.kt, MainViewModel.kt, QualityAlternativeApp.kt, InterceptionRuntimeGateTest.kt, MainViewModelTest.kt, PRD.md.

Tightest fix: Make duplicate detection Bedtime-aware rather than only reordering it. Pass bedtimeActive into ForegroundAppDetectionPolicy, store whether the previous same-package event was active-Bedtime, and force shouldLog=true when the current event is active Bedtime but the previous same-package event was not. Alternatively, when bedtimeActive=true and InterceptionRuntimeGate.shouldSuppress(...) returns false, bypass duplicate suppression for launch gating and reserve duplicate policy only for analytics de-noising.

Severity: Medium — A pre-Bedtime normal intervention still does not automatically convert to Bedtime UI on a pure clock transition.

Exact claim: If a normal intervention is already displayed before Bedtime begins and no settings emission or user click occurs, the UI can continue showing normal affordances during active Bedtime, including Pause 15 min and the normal Open Anyway presentation, until openAnyway(), delayFor15Minutes(), or applySettings() calls the shared helper.

Why it is vulnerable: ensureCurrentInterventionBedtimeEnforced(nowMillis) correctly converts the current intervention when invoked (MainViewModel.kt:2201-2238), and R5 invokes it from delayFor15Minutes() (MainViewModel.kt:2032-2039), openAnyway() (MainViewModel.kt:2241-2247), and applySettings() (MainViewModel.kt:3064-3123). There is no corresponding ViewModel clock-transition hook for Bedtime; the only visible ticker in this area refreshes active delay windows (MainViewModel.kt:485-490). The intervention UI derives Bedtime state exclusively from state.currentInterventionBedtimeEnforced (QualityAlternativeApp.kt:1909) and renders Pause 15 min whenever that value is false (QualityAlternativeApp.kt:2130-2139). This no longer appears to execute a delay because the click path now enforces Bedtime first, but it still violates the PRD and README requirement that active Bedtime hides Pause and presents the one-minute emergency wait before unlock is available.

Files checked: MainViewModel.kt, QualityAlternativeApp.kt, MainViewModelTest.kt, README.md, PRD.md.

Tightest fix: Add a Bedtime boundary refresh path while screen == MainScreen.Intervention, such as a lightweight ViewModel ticker or UI LaunchedEffect that calls a public wrapper around the shared enforcement helper when the next Bedtime transition is reached. Add a regression test where a Soft pre-Bedtime intervention remains idle across the boundary without a settings emission, then assert currentInterventionBedtimeEnforced=true, Pause 15 min hidden, and currentOpenAnywayUnlockAvailableAtMillis = boundaryNow + 60_000L.

TRACE CHECKS:

README.md:5-11 defines the Sprint 24 contract: opt-in Bedtime lock, reading and meditation alternatives retained, finite quiet alternatives, Pause 15 min hidden, visible one-minute emergency breath, and settings persistence.

PRD.md:99-102, PRD.md:258-263, PRD.md:291-293, and PRD.md:356-357 establish the relevant product requirements for alternatives, bounded backups, hidden Pause during active Bedtime, one-minute emergency unlock, quiet repeated opens after Open Anyway, and Bedtime not being the default mode.

UserModels.kt:28-33 confirms Bedtime defaults off and BEDTIME_OPEN_ANYWAY_UNLOCK_DELAY_MILLIS = 60_000L; UserModels.kt:65-70 confirms normal modes remain SOFT and FIRM, with default FIRM.

InterceptionRuntimeGate.kt:7-15 rejects normal suppressions during active Bedtime; InterceptionRuntimeGate.kt:19-27 stores allowedDuringBedtime.

QualityAlternativeAccessibilityService.kt:55-66 verifies the R5 ordering improvement: Bedtime is computed and runtime suppression is checked before duplicate detection.

ForegroundAppDetectionPolicy.kt:14-20 remains Bedtime-unaware and suppresses same-package events within 1,500 ms.

MainViewModel.kt:1737-1750 computes Bedtime before runtime suppression in system intervention handling; MainViewModel.kt:1767-1799 skips normal delay handling during active Bedtime.

MainViewModel.kt:1885-1957 installs the 60-second Bedtime unlock wait for newly shown active-Bedtime interventions and marks currentInterventionBedtimeEnforced=bedtimeActive.

MainViewModel.kt:2032-2039 blocks delayFor15Minutes() after calling the shared Bedtime enforcement helper.

MainViewModel.kt:2201-2238 is the shared helper: it converts a stale normal intervention, marks currentInterventionBedtimeEnforced=true, installs now + 60_000L, and records BEDTIME_INTERVENTION_SHOWN.

MainViewModel.kt:2241-2353 blocks early Bedtime Open Anyway, records Bedtime unlock analytics, and creates Bedtime-allowed runtime suppression only after a legitimate Bedtime emergency unlock.

MainViewModel.kt:3064-3123 computes global Bedtime status during settings application and proactively calls the shared helper after settings emission.

QualityAlternativeApp.kt:1909-1913 derives intervention Bedtime UI from currentInterventionBedtimeEnforced and separates meditation from normal backup rows.

QualityAlternativeApp.kt:2068-2077 renders the meditation calm-reset card; QualityAlternativeApp.kt:2079-2113 renders quiet alternatives/backups; QualityAlternativeApp.kt:2130-2153 hides Pause only when isBedtime is true and renders the Bedtime emergency unlock action.

InterceptionRuntimeGateTest.kt:26-63 covers normal suppression rejection and Bedtime emergency suppression acceptance.

MainViewModelTest.kt:4289-4338 covers alternatives, one-minute wait, early unlock blocking, Bedtime unlock analytics, and avoidance of Firm unlock-used analytics.

MainViewModelTest.kt:4342-4378 covers the R1 pre-existing normal suppression ViewModel path.

MainViewModelTest.kt:4382-4444 covers stale Soft/Firm Open Anyway crossing into Bedtime.

MainViewModelTest.kt:4448-4518 covers the settings-emission/global-state Open Anyway regression.

MainViewModelTest.kt:4523-4570 covers settings-emission conversion plus programmatic delayFor15Minutes() refusal.

No shipped test directly exercises QualityAlternativeAccessibilityService or ForegroundAppDetectionPolicy across a Bedtime boundary.

gradle_unit_compile_r5.log:37-49 reports :app:testDebugUnitTest, :app:compileDebugAndroidTestKotlin, and BUILD SUCCESSFUL.

gradle_r1_blocker_regression.log:37-40, gradle_r2_blocker_regression.log:37-40, gradle_r3_blocker_regression.log:37-40, and gradle_r4_blocker_regression.log:37-40 report successful targeted regression runs.

connected_bedtime_e2e.xml:2-9 reports one connected Bedtime E2E test, zero failures, zero errors, and zero skips.

connected_bedtime_e2e_logcat.txt:1, :572, :784, and :870 show connected test start, both screenshot captures, and test finish.

visual_e2e/01_settings_bedtime_enabled.png and visual_e2e/02_intervention_bedtime_hard_ban_alternatives.png are both 1080×2400 RGB/sRGB PNGs and were visually inspected.

BEDTIME SUPPRESSION / BOUNDARY BEHAVIOR:

The core suppression model is materially improved and mostly correct. Normal Open Anyway suppressions are not honored during active Bedtime by the runtime gate; legitimate Bedtime emergency unlocks remain quiet for repeated opens because openAnyway() passes allowedDuringBedtime = uiState.currentInterventionBedtimeEnforced when creating the runtime suppression; newly shown active-Bedtime interventions use a 60-second wait, not the Firm five-second wait; and stale Open Anyway clicks convert to Bedtime mode instead of exiting to the target app.

The remaining boundary risk is service-side and timing-specific: duplicate detection can still suppress a current active-Bedtime event after the runtime gate has declined to suppress it, because the duplicate policy is unaware of whether the previous same-package event occurred outside Bedtime. The remaining UI boundary defect is that a pure clock transition does not itself convert an already displayed normal intervention.

SETTINGS/PERSISTENCE:

Bedtime is opt-in and not the default hard block: DEFAULT_BEDTIME_ENABLED=false and DEFAULT_INTERVENTION_MODE=FIRM in UserModels.kt:28-33 and UserModels.kt:65-70. Local persistence reads, writes, and clamps Bedtime fields in PreferencesSettingsRepository.kt:80-88, :154-156, :237-242, and :377-403. PreferencesSettingsRepositoryTest.kt:207-228 verifies that saveBedtimeSettings() persists Bedtime values without resetting onboarding.

Portable Profile handling includes Bedtime fields with defaults in AccountLightProfile.kt:136-139, validates Bedtime minute ranges in AccountLightProfile.kt:171-175, imports them into AppSettings in AccountLightProfile.kt:1513-1516, exports them in AccountLightProfile.kt:1548-1551, and allows them as settings keys in AccountLightProfile.kt:2182-2188. AccountLightProfileExporterTest.kt:58-117 verifies exported Bedtime values and the configured Open Anyway unlock duration.

ALTERNATIVES/MEDITATION:

PASS. MainViewModel.kt:3681-3683 adds the meditation timer to replacement inventory. DefaultRecommendationEngine.kt:91-120 caps backups at six and preserves meditation as a backup when eligible; DefaultRecommendationEngine.kt:204-207 defines the backup cap. QualityAlternativeApp.kt:2068-2077 renders meditation as a separate calm-reset alternative, while QualityAlternativeApp.kt:2090-2111 keeps the bounded backup list separate. The connected visual screenshot shows primary reading, a distinct “Calm reset” meditation card, quiet alternatives, no visible Pause button, and a disabled “Breathe 60s” emergency unlock action.

TEST/EVIDENCE:

The shipped R5 unit/compile log passes, and the targeted R1-R4 regression logs pass. The connected E2E XML passes the single Bedtime visual test and logcat confirms both screenshot captures. The test suite covers the prior ViewModel/runtime-gate blockers, the 60-second emergency wait, Bedtime analytics, settings-emission conversion, and programmatic Pause refusal after conversion.

The evidence does not include a service-level duplicate-boundary regression test, nor does it include a pure clock-transition test proving that a displayed pre-Bedtime intervention automatically converts without a settings emission or click. Those omissions correspond directly to the two findings above.

BUNDLE GAPS:

BUNDLE GAP: The bundle is scoped rather than build-complete; Gradle project files, manifests, dependency configuration, and several application-wide implementation files are not shipped, so I did not independently rerun Gradle from the extracted bundle.

BUNDLE GAP: InterceptionTargetResolver is referenced by QualityAlternativeAccessibilityService.kt:49-54 but is not shipped, so target-resolution correctness cannot be independently proven from source.

BUNDLE GAP: The Gradle logs show task success but do not include full unit-test XML or per-test result files; exact per-test execution is inferred from source test names, README command lines, and successful Gradle task logs.

BUNDLE GAP: The connected XML includes only bedtimeModeShowsCalmHardBanWithAlternativesAndEmergencyWait; other Android tests are shipped as source and compile evidence exists, but connected execution results for them are not included.

BUNDLE GAP: The bundle does not include an AccessibilityService boundary log demonstrating actual same-package event cadence across the Bedtime transition; Fresh Finding 1 is source-policy proven, while field frequency is not proven from shipped evidence.

PACKAGE HYGIENE:

The extracted bundle contains 47 files and no APKs, AABs, build directories, class files, JARs, .DS_Store files, editor backups, temporary files, or unrelated binary artifacts. The included historical prompts, prior Pro harvests, Gradle logs, connected XML/logcat, screenshots, and PATCH.diff are relevant to the audit scope. adb_devices_before_shutdown.txt shows emulator-5554 device, and adb_devices_after_shutdown.txt shows no attached devices, matching the README shutdown note.