You are doing a fresh-from-scratch adversarial audit of Sprint 24 Bedtime Hard Ban R2.

Read these first:
1. `evidence/sprint24_bedtime_hard_ban/README.md`
2. `evidence/sprint24_bedtime_hard_ban/pro_review_harvest_r1/Adversarial_Audit_Scope.md`
3. `PRD.md`

R1 result was `SCORE: 8/10`, `VERDICT: REVISE`, `VISUAL REVIEW: PASS`.
R1's only blocker was that a normal Open Anyway quiet-unlock window could cross into active Bedtime and suppress the Bedtime hard-ban screen before the one-minute emergency breath.

R2 fix to audit:
- `InterceptionRuntimeGate` now stores whether a suppression is allowed during active Bedtime.
- `QualityAlternativeAccessibilityService` computes active Bedtime from observed settings before honoring runtime suppression.
- `MainViewModel.triggerIntervention()` computes `bedtimeActive` before honoring runtime suppression.
- `MainViewModel.openAnyway()` marks suppressions as Bedtime-allowed only when they come from a Bedtime emergency unlock.
- Unit regressions cover gate behavior and the ViewModel system-interception path.

Bundle rules:
- Use only the shipped bundle as the audit base.
- If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.
- Prefer source files, test logs, XML, and screenshots over derived prose when they conflict.
- Inspect the two PNG screenshots visually; R1 passed visual review, but R2 still needs an explicit visual verdict.

Known prior bug classes to actively test against:
- R1 blocker still present in AccessibilityService or ViewModel;
- normal quiet-unlock incorrectly honored during active Bedtime;
- legitimate Bedtime emergency unlock no longer quiets repeated opens;
- bedtime hard block accidentally becoming default;
- bedtime hiding reading/meditation/backup alternatives;
- `Pause 15 min` visible during active Bedtime;
- emergency unlock using five seconds instead of one minute;
- Portable Profile import/export warning on new settings fields;
- stale or noisy bundle artifacts.

Output format:
1. `SCORE:` integer `/10`
2. `VERDICT:` PASS / REVISE / BLOCK
3. `VISUAL REVIEW:` PASS / FAIL
4. `R1 BLOCKER RECHECK:`
5. `FRESH FINDINGS:` numbered list with severity, exact claim, why it is vulnerable, file(s) checked, and the tightest fix; write `None` if no issues.
6. `TRACE CHECKS:` exact files, tests, screenshots, log lines, or source facts used.
7. `BEDTIME SUPPRESSION BEHAVIOR:`
8. `SETTINGS/PERSISTENCE:`
9. `ALTERNATIVES/MEDITATION:`
10. `TEST/EVIDENCE:`
11. `BUNDLE GAPS:`
12. `PACKAGE HYGIENE:`

Passing bar:
- Give `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS` only if R1 is fully closed and the shipped evidence proves the slice is ready without a code, visual, test, or package-hygiene blocker.
