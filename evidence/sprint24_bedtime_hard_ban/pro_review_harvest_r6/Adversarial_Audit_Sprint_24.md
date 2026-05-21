SCORE: 8/10

VERDICT: REVISE

VISUAL REVIEW: PASS

R1/R2/R3/R4/R5 BLOCKER RECHECK:

R1 normal runtime suppression bypass: PASS. InterceptionRuntimeGate.shouldSuppress(..., bedtimeActive = true) rejects normal suppressions unless allowedDuringBedtime=true, and MainViewModel.triggerIntervention() computes Bedtime before consulting the runtime gate.

R2 stale normal intervention Open Anyway bypass: PASS. openAnyway() calls ensureCurrentInterventionBedtimeEnforced() before honoring Open Anyway, converts stale normal interventions, installs a fresh one-minute gate, and returns false.

R3 settings-emission/global-state Open Anyway bypass: PASS. Global isBedtimeActive and per-intervention currentInterventionBedtimeEnforced remain separated; settings emissions call the shared enforcement helper.

R4 service duplicate-detection ordering bypass: PASS for the exact prior ordering bug. QualityAlternativeAccessibilityService now computes bedtimeActive and checks the runtime gate before foreground duplicate detection. A separate active-Bedtime duplicate issue remains in Fresh Finding 1.

R4 stale Pause 15 min execution during active Bedtime: PASS. delayFor15Minutes() uses nowProvider(), calls the shared Bedtime enforcement helper, and returns without storing a normal delay when Bedtime enforcement is active.

R5 foreground duplicate suppression at the Bedtime boundary: PASS. ForegroundAppDetectionPolicy.shouldLog() accepts bedtimeActive and allows the same package when crossing from non-Bedtime into active Bedtime; QualityAlternativeAccessibilityService passes the computed value.

R5 pure clock-transition stale intervention UI: PASS. The production ticker calls refreshBedtimeInterventionBoundary() after refreshActiveDelayWindow(), and the shipped unit test covers idle conversion without settings emission or user click.

Legitimate Bedtime emergency unlock quieting repeated opens: PASS. openAnyway() creates InterceptionRuntimeGate.suppressPackage(..., allowedDuringBedtime = uiState.currentInterventionBedtimeEnforced) only after the emergency unlock path is actually available and used.

Bedtime accidentally becoming default: PASS. DEFAULT_BEDTIME_ENABLED=false; default intervention mode remains FIRM.

Bedtime hiding reading, meditation, or backup alternatives: PASS. Source and screenshots show primary reading, a distinct meditation alternative, and quiet finite backups.

Emergency unlock duration: PASS. BEDTIME_OPEN_ANYWAY_UNLOCK_DELAY_MILLIS = 60_000L; tests and screenshot show 60 seconds.

Portable Profile import/export warning on new Bedtime settings fields: PASS from source. Bedtime fields are exported, imported, range-validated, and included in allowed settings keys.

Stale/noisy package artifacts: PASS. No APK, AAB, class, jar, build-output, editor-backup, .DS_Store, or unrelated binary artifact was found.

FRESH FINDINGS:

Severity: High — Active-Bedtime same-package duplicate suppression can still become an unintended quiet-open path without a Bedtime emergency unlock.

Exact claim: After a selected app has already generated an active-Bedtime foreground event, a second same-package foreground event within 1,500 ms is dropped by ForegroundAppDetectionPolicy even when InterceptionRuntimeGate has no legitimate Bedtime emergency-unlock suppression for that app. If the user abandons the Bedtime intervention and returns to the original app inside that interval, the service can return before launching the hard-ban screen, despite no one-minute emergency unlock having occurred.

Why it is vulnerable: QualityAlternativeAccessibilityService correctly checks InterceptionRuntimeGate.shouldSuppress(targetApp.packageName, nowMillis, bedtimeActive) first, but when that returns false, it still uses ForegroundAppDetectionPolicy.shouldLog() as the launch gate. ForegroundAppDetectionPolicy treats a same-package active-Bedtime event as a duplicate whenever nowMillis - lastSeenAtMillis < 1_500L and the prior same-package event was also active-Bedtime. QualityAlternativeApp allows the intervention to be abandoned through BackHandler { onAbandon() }, and MainViewModel.abandonFormIntervention() clears the intervention without installing a runtime suppression. The next same-package event can therefore be suppressed solely by foreground de-duplication, not by an authorized Bedtime unlock.

Files checked: app/src/main/java/com/qualityalternative/app/interception/ForegroundAppDetectionPolicy.kt; app/src/main/java/com/qualityalternative/app/interception/QualityAlternativeAccessibilityService.kt; app/src/main/java/com/qualityalternative/app/interception/InterceptionRuntimeGate.kt; app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt; app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt; app/src/test/java/com/qualityalternative/app/interception/ForegroundAppDetectionPolicyTest.kt; PRD.md.

Tightest fix: Do not let same-package duplicate suppression preempt active-Bedtime launch gating unless InterceptionRuntimeGate has already accepted a allowedDuringBedtime=true suppression. Split duplicate de-noising from launch gating, or make the service bypass ForegroundAppDetectionPolicy for active Bedtime after the runtime gate returns false. Add a regression where an active-Bedtime same-package event is followed by another active-Bedtime same-package event within 1,500 ms, with no emergency unlock suppression, and assert that the second event still launches the Bedtime intervention.

TRACE CHECKS:

README.md:5-11 defines the slice contract: opt-in Bedtime lock, reading and meditation alternatives retained, finite quiet backups, hidden Pause, one-minute emergency breath, and settings persistence.

README.md:91-98 records the intended R6 fixes for foreground duplicate boundary and pure clock-transition conversion.

PRD.md:102 and PRD.md:291-293 require Bedtime to preserve alternatives, hide Pause, and allow the original app only through a visible one-minute emergency unlock wait; quiet repeated opens are tied to Open Anyway from a system intervention.

UserModels.kt:28-33 confirms Bedtime defaults off and the emergency wait is 60_000L; UserModels.kt:65-70 confirms normal intervention mode remains Soft/Firm with default Firm.

InterceptionRuntimeGate.kt:7-15 rejects normal suppressions during active Bedtime and honors only allowedDuringBedtime suppressions.

InterceptionRuntimeGate.kt:18-27 stores the allowedDuringBedtime marker on suppression creation.

QualityAlternativeAccessibilityService.kt:55-72 computes bedtimeActive, checks the runtime gate, then applies foreground duplicate detection with the Bedtime value.

ForegroundAppDetectionPolicy.kt:16-25 allows the non-Bedtime-to-Bedtime boundary crossing but still suppresses same-package duplicates inside 1,500 ms when the previous event was already active-Bedtime.

MainViewModel.kt:485-490 shows the production ticker calling refreshBedtimeInterventionBoundary() after active delay refresh.

MainViewModel.kt:1738-1750 computes Bedtime before runtime suppression in system interventions.

MainViewModel.kt:1885-1957 installs the 60-second Bedtime gate and sets currentInterventionBedtimeEnforced.

MainViewModel.kt:2033-2039 blocks programmatic Pause after Bedtime enforcement.

MainViewModel.kt:2202-2244 converts stale normal interventions into Bedtime-enforced interventions.

MainViewModel.kt:2246-2359 blocks early Bedtime unlocks and marks runtime suppression as Bedtime-allowed only after a legitimate emergency unlock.

QualityAlternativeApp.kt:1909-1913 derives Bedtime UI from currentInterventionBedtimeEnforced and removes meditation from ordinary backup rows.

QualityAlternativeApp.kt:2068-2153 renders meditation, quiet alternatives, hides Pause during Bedtime, and disables the emergency unlock until the countdown expires.

QualityAlternativeApp.kt:1960 and MainViewModel.kt:2095-2125 show intervention abandonment without runtime suppression creation, which is relevant to Fresh Finding 1.

ForegroundAppDetectionPolicyTest.kt:41-60 covers the R5 boundary crossing from non-Bedtime to Bedtime.

MainViewModelTest.kt:4289-4338 covers alternatives, 60-second wait, early unlock blocking, Bedtime unlock analytics, and absence of Firm unlock-used analytics.

MainViewModelTest.kt:4342-4378 covers the pre-existing normal suppression regression.

MainViewModelTest.kt:4382-4444 covers stale Soft/Firm Open Anyway crossing into Bedtime.

MainViewModelTest.kt:4448-4518 covers the settings-emission/global-state regression.

MainViewModelTest.kt:4523-4570 covers settings-emission conversion plus programmatic Pause refusal.

MainViewModelTest.kt:4574-4622 covers idle pre-Bedtime intervention conversion without settings emission or user click.

gradle_unit_compile_r6.log:37-50 reports :app:testDebugUnitTest, :app:compileDebugAndroidTestKotlin, and BUILD SUCCESSFUL.

gradle_r5_blocker_regression.log:33-40 reports compilation and targeted unit-test task success for the R5 regressions.

connected_bedtime_e2e.xml:2-9 reports one connected Bedtime E2E test with zero failures, zero errors, and zero skips.

connected_bedtime_e2e_logcat.txt:1,572,784,870 shows the connected test start, both screenshot captures, and test finish.

visual_e2e/01_settings_bedtime_enabled.png and visual_e2e/02_intervention_bedtime_hard_ban_alternatives.png were inspected directly.

BEDTIME SUPPRESSION / BOUNDARY BEHAVIOR:

The prior R1-R5 boundary defects are closed in the exact cases previously reported. Normal Open Anyway suppressions do not override active Bedtime; stale Open Anyway clicks convert; settings emissions convert stale interventions; programmatic Pause is blocked; same-package non-Bedtime-to-Bedtime foreground events are no longer dropped; and idle displayed interventions can convert through the production ticker path.

The remaining suppression defect is narrower but source-proven: active-Bedtime duplicate de-noising still acts as a launch gate for repeated same-package foreground events inside 1,500 ms even when no emergency unlock was used.

SETTINGS/PERSISTENCE:

PASS. Bedtime is opt-in by default, with local persistence through PreferencesSettingsRepository keys for enabled/start/end values. saveBedtimeSettings() persists the fields without resetting onboarding. Portable Profile export/import includes bedtimeEnabled, bedtimeStartMinutes, and bedtimeEndMinutes; the fields are range-validated and included in AllowedSettingsKeys, so they are not treated as unknown-field warning noise.

ALTERNATIVES/MEDITATION:

PASS. fullReplacementInventory() adds the meditation timer. DefaultRecommendationEngine caps backups at six and preserves meditation when eligible. QualityAlternativeApp renders meditation as a distinct calm-reset card and filters it out of ordinary backup rows. The visual evidence shows primary reading, meditation, quiet alternatives, hidden Pause, and disabled emergency unlock countdown.

TEST/EVIDENCE:

The R6 full unit/compile log passes, the targeted R5 regression log passes, and the connected E2E XML/logcat pass for the Bedtime visual scenario. The R6 tests cover the exact R5 boundary and pure clock-transition fixes. They do not cover Fresh Finding 1, because there is no service-level or policy-level test asserting that active-Bedtime same-package duplicates remain launch-gated when no emergency unlock suppression exists.

BUNDLE GAPS:

BUNDLE GAP: The extracted bundle does not include a Gradle wrapper or the full app/src/main/res tree referenced by the manifest, so I did not independently rerun Gradle from the extracted files. Compile/test status is based on the shipped Gradle logs.

BUNDLE GAP: The Gradle logs show task success but do not include unit-test XML or per-test execution reports for the targeted JVM tests; exact targeted execution is inferred from README command lines, source test names, and successful Gradle task logs.

BUNDLE GAP: No AccessibilityService-level connected test or log proves behavior for same-package active-Bedtime duplicate events after intervention abandonment; Fresh Finding 1 is source-proven, while device-timing frequency is not proven from shipped evidence.

BUNDLE GAP: The connected E2E evidence covers the all-day Bedtime visual path, not the real-time pure clock boundary; that boundary is supported by source and unit coverage.

PACKAGE HYGIENE:

PASS. The extracted bundle contains 136 files. No APKs, AABs, class files, jars, build directories, editor backups, .DS_Store files, or unrelated binary artifacts were found. The included prior Pro harvests, prompts, logs, XML, screenshots, PATCH.diff, and ADB shutdown files are relevant to the requested audit scope.