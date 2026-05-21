You are doing a fresh-from-scratch adversarial audit of Sprint 24 Bedtime Hard Ban R4.

Read these first:
1. `evidence/sprint24_bedtime_hard_ban/README.md`
2. `evidence/sprint24_bedtime_hard_ban/pro_review_harvest_r1/Adversarial_Audit_Scope.md`
3. `evidence/sprint24_bedtime_hard_ban/pro_review_harvest_r2/Adversarial_Audit_R2.md`
4. `evidence/sprint24_bedtime_hard_ban/pro_review_harvest_r3/Adversarial_Audit_R3.md`
5. `PRD.md`

Prior Pro results:
- R1: `8/10`, `REVISE`, visual `PASS`.
- R2: `8/10`, `REVISE`, visual `PASS`.
- R3: `8/10`, `REVISE`, visual `PASS`.

R4 fixes to audit:
- `MainUiState` now separates global `isBedtimeActive` from per-intervention `currentInterventionBedtimeEnforced`.
- `MainViewModel.openAnyway()` converts to a Bedtime emergency gate whenever Bedtime is active and the current intervention is not yet Bedtime-enforced, regardless of whether global `isBedtimeActive` was updated by a settings emission.
- Unlock analytics, early-block analytics, Firm completion avoidance, runtime suppression marking, and Intervention UI all use `currentInterventionBedtimeEnforced` for the current screen.
- Regression coverage includes Soft and Firm interventions that cross into Bedtime, emit settings from the intervention, and then try `Open anyway`.
- The bundle now includes `ForegroundAppDetectionPolicy.kt` and `MeditationReplacement.kt` to reduce R3 bundle gaps.

Bundle rules:
- Use only the shipped bundle as the audit base.
- If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.
- Prefer source files, test logs, XML, and screenshots over derived prose when they conflict.
- Inspect both PNG screenshots visually and still provide `VISUAL REVIEW`.

Known prior bug classes to actively test against:
- R1 normal runtime suppression bypass still present;
- R2 stale normal intervention click bypass still present;
- R3 settings-emission/global-state bypass still present;
- legitimate Bedtime emergency unlock no longer quiets repeated opens;
- bedtime hard block accidentally becoming default;
- bedtime hiding reading/meditation/backup alternatives;
- `Pause 15 min` visible during active Bedtime;
- emergency unlock using five seconds instead of one minute;
- Portable Profile import/export warning on new settings fields;
- stale/noisy package artifacts.

Output format:
1. `SCORE:` integer `/10`
2. `VERDICT:` PASS / REVISE / BLOCK
3. `VISUAL REVIEW:` PASS / FAIL
4. `R1 BLOCKER RECHECK:`
5. `R2 BLOCKER RECHECK:`
6. `R3 BLOCKER RECHECK:`
7. `FRESH FINDINGS:` numbered list with severity, exact claim, why it is vulnerable, file(s) checked, and the tightest fix; write `None` if no issues.
8. `TRACE CHECKS:` exact files, tests, screenshots, log lines, or source facts used.
9. `BEDTIME SUPPRESSION / BOUNDARY BEHAVIOR:`
10. `SETTINGS/PERSISTENCE:`
11. `ALTERNATIVES/MEDITATION:`
12. `TEST/EVIDENCE:`
13. `BUNDLE GAPS:`
14. `PACKAGE HYGIENE:`

Passing bar:
- Give `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS` only if R1, R2, and R3 are fully closed and the shipped evidence proves the slice is ready without a code, visual, test, or package-hygiene blocker.
