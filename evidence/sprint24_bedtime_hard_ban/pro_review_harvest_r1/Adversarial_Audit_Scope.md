SCORE: 8/10

VERDICT: REVISE

VISUAL REVIEW: PASS

FRESH FINDINGS:

Severity: High — Active Bedtime can be bypassed by a pre-existing Open Anyway quiet-unlock window.
Exact claim: If the user opens a distracting app through normal Soft/Firm Open Anyway shortly before Bedtime begins, repeated system interceptions for that same app can be suppressed during active Bedtime without showing the Bedtime hard-ban screen, the one-minute breath, Bedtime analytics, or alternatives.
Why vulnerable: MainViewModel.triggerIntervention() checks InterceptionRuntimeGate.shouldSuppress(...) and returns to MainScreen.Home at MainViewModel.kt lines 1736-1753 before it computes bedtimeActive at lines 1755-1760. openAnyway() creates that runtime suppression window at lines 2281-2285. The existing test openAnyway_usesConfiguredUnlockWindowAndSuppressesRepeatedSystemIntervention confirms that repeated system interceptions are suppressed during the configured unlock window at MainViewModelTest.kt lines 4151-4195. This conflicts with PRD FR7 line 291, which says active Bedtime allows the original app only through a visible one-minute emergency unlock wait.
Concrete tester failure: Configure Bedtime 22:00-07:00, use normal Open Anyway for a selected distracting app at 21:55, then tap the same app at 22:05. Expected result: Bedtime intervention with alternatives, hidden Pause, disabled emergency unlock, and 60-second wait. Vulnerable result: the app remains “still unlocked,” no Bedtime intervention appears, and no one-minute breath is required.
Files checked: PRD.md; MainViewModel.kt; UserModels.kt; MainViewModelTest.kt; InterventionModels.kt; QualityAlternativeApp.kt.
Tightest fix: Compute bedtimeActive before the suppression check. During active Bedtime, ignore suppressions that were created outside Bedtime. To preserve PRD line 293 after a legitimate Bedtime emergency unlock, attach an origin or mode marker to runtime suppressions and honor only suppressions created after BEDTIME_UNLOCK_USED while Bedtime is active. Add a regression unit test that preloads or creates a non-Bedtime suppression, advances into an active Bedtime window, and asserts BEDTIME_INTERVENTION_SHOWN, currentOpenAnywayUnlockAvailableAtMillis = now + 60_000L, alternatives present, and no “still unlocked” return path.

TRACE CHECKS:

evidence/sprint24_bedtime_hard_ban/README.md lines 5-11 define the shipped scope: opt-in Bedtime sleep lock, reading and meditation alternatives preserved, finite backup alternatives, Pause 15 min hidden, one-minute emergency breath, and persistence in local settings plus Portable Profile.

PRD.md lines 99-103 allow optional Bedtime protection but not default hard blocking; line 124 explicitly excludes hard-block mode as the default experience.

PRD.md lines 258-263 require bounded backups and meditation as a separate calm-reset alternative when primary is reading.

PRD.md lines 291-293 require active Bedtime to remain finite and calm, keep alternatives available, hide Pause, require a visible one-minute emergency unlock wait, and then permit quiet repeated opens after Open Anyway.

PRD.md lines 356-357 require Soft/Firm mode plus opt-in Bedtime, with Bedtime not default and alternatives preserved.

UserModels.kt lines 25-30 verify DEFAULT_BEDTIME_ENABLED = false, default schedule values, and BEDTIME_OPEN_ANYWAY_UNLOCK_DELAY_MILLIS = 60_000L; line 47 verifies the default intervention mode is FIRM, not Bedtime hard block.

PreferencesSettingsRepository.kt lines 82-88 read Bedtime fields with defaults and bounds; lines 237-242 save Bedtime settings; lines 401-403 define the persisted keys.

AccountLightProfile.kt lines 137-139 add Bedtime fields with defaults; lines 171-175 validate Bedtime minute ranges; lines 1514-1516 import Bedtime into AppSettings; lines 1549-1551 export Bedtime into Portable Profile; lines 2182-2188 allow Bedtime fields in profile settings without treating them as unknown.

MainViewModel.kt lines 1761-1792 skip normal delay handling when Bedtime is active; lines 1877-1884 set the Bedtime unlock delay to 60 seconds; lines 1885-1902 record BEDTIME_INTERVENTION_SHOWN; lines 2053-2079 record Bedtime unlock availability; lines 2192-2219 block premature emergency unlock attempts; lines 2246-2262 record BEDTIME_UNLOCK_USED; line 3887 prevents Bedtime from recording Firm completion analytics.

QualityAlternativeApp.kt lines 1909-1913 separate meditation from normal backups; lines 2034-2043 show Bedtime-specific copy; lines 2068-2077 show the meditation alternative card; lines 2079-2081 label Bedtime backups as “Quiet alternatives”; lines 2130-2139 hide Pause 15 min during Bedtime; lines 2141-2153 render the disabled emergency unlock action until the countdown expires.

gradle_unit_compile.log shows :app:testDebugUnitTest, :app:compileDebugAndroidTestKotlin, and BUILD SUCCESSFUL in 7s, with 37 actionable tasks: 7 executed, 30 up-to-date.

connected_bedtime_e2e.xml reports tests="1" failures="0" errors="0" skipped="0" for bedtimeModeShowsCalmHardBanWithAlternativesAndEmergencyWait.

connected_bedtime_e2e_logcat.txt line 1 starts the Bedtime E2E test, line 572 captures 01_settings_bedtime_enabled.png, line 784 captures 02_intervention_bedtime_hard_ban_alternatives.png, and line 870 finishes the test.

MainActivityTest.kt lines 556-600 assert Bedtime settings active, the Bedtime intervention header, “QUIET ALTERNATIVES,” backup list visibility, disabled emergency unlock action, and absence of Pause 15 min.

MainActivityTest.kt lines 1950-1967 separately assert the meditation calm alternative is displayed and not duplicated as a normal backup action.

MainViewModelTest.kt lines 4284-4332 assert Bedtime keeps alternatives, sets a 60-second unlock wait, records Bedtime analytics, blocks immediate Open Anyway, enables unlock after the 60-second delay, and avoids Firm unlock-used analytics.

PreferencesSettingsRepositoryTest.kt lines 207-228 assert Bedtime settings persist without resetting onboarding.

AccountLightProfileExporterTest.kt lines 36-139 assert Portable Profile export includes Bedtime fields and excludes unsafe raw URI/token/provider material.

Visual screenshots inspected directly: both PNGs are 1080x2400. 01_settings_bedtime_enabled.png shows the Bedtime settings section enabled and active with calm copy. 02_intervention_bedtime_hard_ban_alternatives.png shows primary reading, a distinct “Calm reset” meditation card, quiet alternatives, hidden Pause, and a disabled 60-second emergency unlock action.

SETTINGS/PERSISTENCE:

The default-hard-block guardrail is satisfied in the shipped settings model: Bedtime is opt-in by default (DEFAULT_BEDTIME_ENABLED = false), and default intervention behavior remains Firm rather than Bedtime hard block.

Settings UI copy distinguishes Soft and Firm behavior: Soft says Open Anyway is immediate, while Firm says a five-second pause is added (QualityAlternativeApp.kt lines 4133-4149).

Bedtime settings persist locally through DataStore keys and a dedicated saveBedtimeSettings(...) path.

Portable Profile export/import includes the new Bedtime fields, validates their minute ranges, and lists them as allowed settings keys, which prevents the newly added fields from being treated as unknown-field warning noise.

Merge import keeps local settings, while replace import applies portable settings; the source maps Bedtime fields in both directions. The shipped tests prove Bedtime export and local persistence directly; replace-import coverage is source-verified but not specifically asserted for Bedtime values in a dedicated test.

BEDTIME HARD-BAN BEHAVIOR:

Active Bedtime correctly skips normal delay handling, so the prior Pause 15 min delay gate does not supersede the Bedtime intervention.

The UI hides Pause 15 min during active Bedtime and the connected E2E asserts that the text does not exist.

The emergency unlock wait is 60 seconds, not the five-second Firm wait, and premature Open Anyway attempts record BEDTIME_UNLOCK_BLOCKED.

Bedtime-specific analytics are present and are not folded into Firm analytics for show, unlock-used, or completion paths.

The one uncovered behavior is the suppression-order defect described in Fresh Finding 1, where a non-Bedtime quiet unlock can preempt the Bedtime hard-ban path before bedtimeActive is computed.

ALTERNATIVES/MEDITATION:

Reading alternatives are preserved through the normal recommendation path, and the connected E2E asserts a visible backup list under “QUIET ALTERNATIVES.”

Meditation is intentionally separated from the normal backup list in QualityAlternativeApp.kt by extracting meditationAlternative and filtering meditation out of backups.

The intervention screenshot confirms the calm alternative is visually distinct: “Calm reset,” “3-minute reset,” Start button, and duration chips appear above “QUIET ALTERNATIVES.”

The screenshot copy is calm and non-moralizing; it describes protection, alternatives, and emergency breath without punitive or shaming language.

BUNDLE GAP: The bundle does not include the domain implementation of usesMeditationTimer(), meditationTimerContentItem(...), ContentItem, RecommendationSet, or the recommendation-engine cap logic. The meditation and bounded-list conclusions are therefore based on shipped call sites, tests, and screenshots, not on the omitted helper implementations.

TEST/EVIDENCE:

Unit and compile evidence passes: gradle_unit_compile.log shows the unit-test task and Android-test Kotlin compilation succeeded.

Connected E2E evidence passes: connected_bedtime_e2e.xml reports one Bedtime E2E test with zero failures, errors, or skips.

The connected E2E covers settings activation, intervention heading, quiet alternatives, backup list, hidden Pause, and disabled emergency unlock.

The shipped source includes a separate Android test assertion that meditation appears as a distinct calm alternative and is not duplicated as a normal backup row.

The unit test bedtimeModeKeepsAlternativesButRequiresOneMinuteEmergencyUnlock covers the 60-second Bedtime wait, Bedtime analytics, immediate unlock blocking, unlock enablement, and non-pollution of Firm unlock-used analytics.

Test gap tied to the fresh finding: no shipped unit or connected test exercises a pre-existing Open Anyway suppression window crossing into active Bedtime.

BUNDLE GAPS:

BUNDLE GAP: InterceptionRuntimeGate implementation is not included in the shipped source slice. Its behavior is inferred from MainViewModel.kt call sites and shipped tests that call shouldSuppress(...).

BUNDLE GAP: The full recommendation-engine and content-model implementations are not included, so the exact global cap on backup alternatives cannot be source-reviewed from this bundle. The shipped UI, tests, and screenshots show a finite visible list for the Bedtime path.

BUNDLE GAP: The bundle contains a Gradle success log for unit tests but not the full unit-test XML result set; exact per-unit-test pass records are therefore not independently listed in the bundle.

BUNDLE GAP: The connected XML contains only the Sprint 24 Bedtime E2E method. Other Android test methods are shipped as source and compiled, but their connected execution results are not included.

PACKAGE HYGIENE:

No package-hygiene blocker found.

BUNDLE_MANIFEST.md accurately states that the bundle is scoped and excludes full build output, APKs, prior sprint archives, and unrelated evidence directories.

The file set is limited to the PRD, scoped source/test files, evidence README, manifest, patch, logs, XML, adb device state files, and the two visual screenshots.

PATCH.diff is present as derived review context, but source files, logs, XML, and screenshots were used as the primary audit base.

adb_devices_before_shutdown.txt shows emulator-5554 device; adb_devices_after_shutdown.txt shows no attached devices, matching the README note that the emulator was shut down.

connected_bedtime_e2e_logcat.txt contains normal emulator/system-service noise, but the test-runner start/finish markers and XML pass result are unambiguous.