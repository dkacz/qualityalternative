You are doing a fresh-from-scratch adversarial release audit of the Quality Alternative Android app after Review Pro R1 blocked the sprint.

Read the full primary R2 report first:
- evidence/sprint39_agent_inbox_autoimport_options_prompt/LIVE_E2E_REPORT_R2.md

Then review only this scope:
Sprint 39 Agent Inbox Autoimport Options R2:
- priority mode options: Ask me, Ignore, Auto high;
- category mode options: Manifest topics, No category;
- copied agent prompt is generic and mode-consistent;
- import policy is snapshotted when an import starts;
- Drive folder browser remains a real folder-selection path;
- live two-package Import all works on a real signed-in emulator with a real Drive folder and externally created packages;
- live autoimport-on-start imports a package uploaded only after autoimport was enabled;
- imported content appears in Library as Agent Inbox document, OTHER, Priority, and opens in reader;
- the exact installable APK exercised live is included and hash-bound to the installed base APK.

R1 blockers to re-check:
1. Final/live APK mismatch.
2. Missing live two-package Import all.
3. Incomplete full-flow logcat.
4. Prompt was contradictory and repo-specific.
5. Clipboard payload was not proven.
6. Import options could drift during in-flight import.
7. Connected visual did not cover Drive-browser state.

Rules:
- Use only this shipped R2 bundle as the audit base.
- If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.
- Do not rely on old R1 screenshots as proof for R2 behavior.
- The repo’s established release pattern publishes a signed debug APK as the installable tester artifact and may also publish an unsigned release APK. For this R2 review, the proof-bearing installable artifact is `app/build/outputs/apk/debug/app-debug.apk`; block only if that is unacceptable for this repository’s alpha release pattern or if its hash is not bound to the installed base APK.
- The ADB paste probe did not work and is explicitly not claimed as positive clipboard proof. Positive prompt/copy evidence is the updated prompt source/hash plus the live Android clipboard overlay screenshot after tapping Copy. Decide whether that is sufficient; if not, mark it as a finding.

Output format:
1. `VERDICT:` PASS / REVISE / BLOCK
2. `SCORE:` N/10
3. `R1 BLOCKER CLOSURE:` one bullet per R1 blocker with exact R2 file paths checked
4. `LIVE EVIDENCE CHECK:` exact files used for APK identity, signed-in account, Drive folder, folder selection, Import all, autoimport restart, Library, reader, prompt/copy, logcat sentinel
5. `FRESH FINDINGS:` numbered list with severity, exact claim, why vulnerable, file(s) checked, and tightest fix. If none, say `None`.
6. `TRACE CHECKS:` exact code files, tests, screenshots/XML/logs, hashes, and APK metadata used
7. `BUNDLE GAPS:` only if needed
8. `PACKAGE HYGIENE:` whether the R2 bundle is clean enough for release audit

Release rule:
- Return `VERDICT: PASS` and `SCORE: 10/10` only if the R2 implementation and live evidence satisfy docs/AGENT_INBOX_LIVE_REVIEW_GATE.md and all R1 release blockers are closed.
- If any R1 blocker remains open, return `VERDICT: BLOCK`.
