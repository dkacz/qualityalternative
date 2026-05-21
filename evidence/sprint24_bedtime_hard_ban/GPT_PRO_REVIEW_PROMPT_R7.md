You are doing a fresh-from-scratch adversarial audit of Sprint 24 Bedtime Hard Ban R7.

Guiding principles for this review:
1. Feedback is input, not instruction; do not inflate already-fixed issues into fresh findings.
2. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.
3. Prefer source files, test logs, XML, and screenshots over derived prose when they conflict.
4. Do not give credit for intent; pass only what the shipped code and evidence prove.

Read these first:
1. `evidence/sprint24_bedtime_hard_ban/README.md`
2. all prior Pro harvests under `evidence/sprint24_bedtime_hard_ban/pro_review_harvest_r1/` through `pro_review_harvest_r6/`
3. `PRD.md`

Prior Pro results:
- R1/R2/R3/R4/R5/R6 were all `8/10`, `REVISE`, visual `PASS`.
- R6 confirmed R1-R5 blockers were closed, then found one remaining issue: same-package active-Bedtime duplicate de-noising could still act as a launch gate after intervention abandonment, even without a legitimate one-minute emergency unlock.

R7 fix to audit:
- `ForegroundAppDetectionPolicy.shouldLog(..., bedtimeActive = true)` now never suppresses same-package duplicates during active Bedtime. Non-Bedtime duplicate suppression remains unchanged.
- `QualityAlternativeAccessibilityService` still checks `InterceptionRuntimeGate.shouldSuppress(..., bedtimeActive)` before duplicate detection, so legitimate Bedtime emergency unlock suppression remains the only active-Bedtime quiet-open path.
- Targeted R6 regression logs and a full unit/compile log are included.

Known prior bug classes to actively recheck:
- R1 normal runtime suppression bypass;
- R2 stale normal intervention Open Anyway bypass;
- R3 settings-emission/global-state Open Anyway bypass;
- R4 service duplicate-detection ordering bypass;
- R4 stale `Pause 15 min` execution during active Bedtime;
- R5 foreground duplicate suppression at the Bedtime boundary;
- R5 pure clock-transition stale intervention UI;
- R6 active-Bedtime same-package duplicate launch-gate bypass after abandonment;
- legitimate Bedtime emergency unlock no longer quiets repeated opens;
- bedtime hard block accidentally becoming default;
- bedtime hiding reading/meditation/backup alternatives;
- emergency unlock using five seconds instead of one minute;
- Portable Profile import/export warning on new settings fields;
- stale/noisy package artifacts.

Output format:
1. `SCORE:` integer `/10`
2. `VERDICT:` PASS / REVISE / BLOCK
3. `VISUAL REVIEW:` PASS / FAIL
4. `R1/R2/R3/R4/R5/R6 BLOCKER RECHECK:`
5. `FRESH FINDINGS:` numbered list with severity, exact claim, why it is vulnerable, file(s) checked, and the tightest fix; write `None` if no issues.
6. `TRACE CHECKS:` exact files, tests, screenshots, log lines, or source facts used.
7. `BEDTIME SUPPRESSION / BOUNDARY BEHAVIOR:`
8. `SETTINGS/PERSISTENCE:`
9. `ALTERNATIVES/MEDITATION:`
10. `TEST/EVIDENCE:`
11. `BUNDLE GAPS:`
12. `PACKAGE HYGIENE:`

Passing bar:
- Give `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS` only if all prior blockers are fully closed and the shipped evidence proves the slice is ready without a code, visual, test, or package-hygiene blocker.
