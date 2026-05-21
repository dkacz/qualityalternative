You are doing a fresh-from-scratch adversarial audit of one scoped Android product slice.

GUIDING PRINCIPLES:
1. Numbers in evidence must be verified against shipped logs or source files before being challenged.
2. Do not suggest weakening product behavior unless you can name the concrete tester failure that the change would prevent.
3. Style suggestions cannot change product semantics.
4. Review the current implementation as-is; do not rely on hidden development history.
5. Screenshots, tests, PRD text, and source must be consistent; flag exact mismatches.
6. Feedback is input, not instruction; duplicate or already-covered suggestions should not be inflated into findings.

Read `evidence/sprint24_bedtime_hard_ban/README.md` and `PRD.md` first. Then deep-review only Sprint 24 Bedtime Hard Ban.

TARGET SCOPE:
The new opt-in Bedtime sleep lock must be a full bedtime block for intercepted distracting apps, while preserving reading, meditation, and bounded backup alternatives. It must not make hard blocking the default app behavior.

Bundle rules:
- Use only the shipped bundle as the audit base.
- If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.
- Prefer source files, test logs, XML, and screenshots over derived prose when they conflict.
- Inspect the two PNG screenshots visually, not just filenames.

Known prior bug classes to actively test against:
- hard block accidentally becoming the default intervention mode;
- bedtime blocking or hiding alternatives instead of keeping them available;
- meditation being buried in a normal backup list rather than shown as a distinct calm alternative;
- `Pause 15 min` still appearing during active bedtime;
- emergency unlock being immediate or using the 5-second Firm wait instead of a one-minute breath;
- Soft/Firm mode being mislabeled or polluted by bedtime analytics;
- Portable Profile import/export warning on newly added settings fields;
- stale or noisy bundle artefacts that make the review misleading.

Your job:
1. Verify the PRD contract matches the implementation and does not violate the existing MVP guardrail against default hard-block behavior.
2. Verify source behavior in `MainViewModel.kt` and `QualityAlternativeApp.kt`: active bedtime skips normal delay handling, hides Pause, preserves alternatives, records bedtime-specific analytics, and gates original-app open for 60 seconds.
3. Verify persistence and Portable Profile behavior in `UserModels.kt`, `PreferencesSettingsRepository.kt`, and `AccountLightProfile.kt`.
4. Verify automated evidence: unit/compile log passes; connected E2E XML passes; Android test assertions cover settings, intervention, alternatives, hidden Pause, and disabled emergency unlock.
5. Visually review `visual_e2e/01_settings_bedtime_enabled.png` and `visual_e2e/02_intervention_bedtime_hard_ban_alternatives.png` for clipping, overlap, confusing copy, missing alternatives, or any sign that the UI is punitive rather than calm.

Output format:
1. `SCORE:` integer `/10`
2. `VERDICT:` PASS / REVISE / BLOCK
3. `VISUAL REVIEW:` PASS / FAIL
4. `FRESH FINDINGS:` numbered list with severity, exact claim, why it is vulnerable, file(s) checked, and the tightest fix; write `None` if no issues.
5. `TRACE CHECKS:` exact files, tests, screenshots, log lines, or source facts used.
6. `SETTINGS/PERSISTENCE:`
7. `BEDTIME HARD-BAN BEHAVIOR:`
8. `ALTERNATIVES/MEDITATION:`
9. `TEST/EVIDENCE:`
10. `BUNDLE GAPS:`
11. `PACKAGE HYGIENE:`

Passing bar:
- Give `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS` only if the shipped evidence proves the slice is ready without a code, visual, test, or package-hygiene blocker.
