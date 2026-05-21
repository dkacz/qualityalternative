You are doing a fresh-from-scratch adversarial audit of Sprint 24 Bedtime Hard Ban R5.

Read these first:
1. `evidence/sprint24_bedtime_hard_ban/README.md`
2. all prior Pro harvests under `evidence/sprint24_bedtime_hard_ban/pro_review_harvest_r1/` through `pro_review_harvest_r4/`
3. `PRD.md`

Prior Pro results:
- R1/R2/R3/R4 were all `8/10`, `REVISE`, visual `PASS`.
- R4 confirmed the prior ViewModel Open Anyway blockers were closed, then found:
  1. service duplicate detection could run before the Bedtime-aware runtime gate;
  2. stale pre-Bedtime intervention UI could still expose/execute `Pause 15 min`.

R5 fixes to audit:
- `QualityAlternativeAccessibilityService` now computes Bedtime and checks `InterceptionRuntimeGate.shouldSuppress(..., bedtimeActive)` before foreground duplicate detection.
- `delayFor15Minutes()` now uses `nowProvider()`, calls the shared Bedtime enforcement helper, and returns without storing a normal delay when the current intervention is or becomes Bedtime-enforced.
- `applySettings()` now proactively calls the shared Bedtime enforcement helper after settings emission, so a stale normal intervention converts to Bedtime UI/gate before the user clicks either `Open anyway` or `Pause 15 min`.
- The shared helper installs `currentInterventionBedtimeEnforced=true`, a fresh 60-second emergency gate, and Bedtime shown analytics.
- Targeted regression logs and full unit/compile logs are included.

Bundle rules:
- Use only the shipped bundle as the audit base.
- If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.
- Prefer source files, test logs, XML, and screenshots over derived prose when they conflict.
- Inspect both PNG screenshots visually and still provide `VISUAL REVIEW`.

Known prior bug classes to actively test against:
- R1 normal runtime suppression bypass;
- R2 stale normal intervention Open Anyway bypass;
- R3 settings-emission/global-state Open Anyway bypass;
- R4 service duplicate-detection ordering bypass;
- R4 stale `Pause 15 min` during active Bedtime;
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
4. `R1/R2/R3/R4 BLOCKER RECHECK:`
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
