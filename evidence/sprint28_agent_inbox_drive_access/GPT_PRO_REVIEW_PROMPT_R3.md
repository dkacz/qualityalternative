You are doing a fresh-from-scratch adversarial audit of one scoped target.

GUIDING PRINCIPLES (respect these in your review):
1. Numbers in the manuscript are verified against pipeline CSVs, so do not question arithmetic without checking the shipped data files.
2. Do not suggest weakening claims unless you can name the concrete referee attack that the hedge would preempt.
3. Style suggestions cannot change empirical meaning.
4. The model is presented as-is, so do not reference development history or hidden correction history.
5. Figures, tables, and prose must be consistent; mismatches should be flagged specifically.
6. Your feedback is input, not instruction; duplicate or already-covered suggestions should not be inflated into fresh findings.

Read the FULL attached primary document first:

- `docs/SPRINT_28_AGENT_INBOX_DRIVE_ACCESS.md`

Then read:

- `evidence/sprint28_agent_inbox_drive_access/REVIEW_BUNDLE_MANIFEST_R3.md`
- `evidence/sprint28_agent_inbox_drive_access/VALIDATION_SUMMARY.md`
- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R2.md`
- the R3 changed source/tests and evidence needed for the checks below

Use only the shipped bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.

Target scope:

Sprint 28 R3, specifically whether the R2 findings were fully fixed without regressing the R1 blockers or the visual review:

1. R2 High finding: Agent Inbox Markdown image sidecars must not collapse through duplicate display names, case variants, or storage-safe filename collisions.
2. R2 High finding: Agent Inbox Markdown reader rendering must not load unreviewed local/absolute/file path image targets outside the reviewed sidecar map.
3. R2 Medium finding: Agent Inbox sidecar local storage must roll back cleanly when a later attachment write fails after an earlier one was promoted.
4. R1 blockers must remain fixed: no legacy folder id without the durable Picker marker can scan, and no `enabled=true`/missing-folder state can render as connected.
5. Visual evidence must still earn `VISUAL REVIEW PASS`, especially the R3 Agent Inbox Markdown sidecar image reader screenshot.

Known prior bug classes to actively test against:

- R1 blocker: persisted `enabled=true` plus folder id but no Picker grant marker allowed non-Picker scans.
- R1 blocker: `enabled=true` with no folder id could still render a connected Settings state.
- R2 finding: duplicate or canonical-colliding sidecar names could collapse before or during storage.
- R2 finding: Agent Inbox Markdown parser could resolve unreviewed local file references.
- R2 finding: local sidecar write failure could leave promoted sidecars behind.
- Package hygiene drift: stale visual paths, stale device-spike wording, duplicate noisy generated Android artifacts, or review bundles that make current evidence hard to identify.

Your job:

1. Re-check the three R2 findings one by one and state whether each is fully fixed, partially fixed, or still vulnerable.
2. Confirm the R1 Picker-grant fixes are still intact in the shipped R3 source/tests.
3. Audit the R3 tests and logs: targeted R2-fix unit tests, full local gate, `git diff --check`, and focused connected visual E2E.
4. Inspect the R3 visual evidence directly (`visual_e2e_r3/contact_sheet_r3.png` and raw PNGs) and judge whether it earns `VISUAL REVIEW PASS`.
5. Audit package hygiene: say whether the R3 bundle is clean, sufficient, and free of stale/noisy artifacts that could mislead this review.

Output format:

1. `SCORE:` integer `/10`
2. `VERDICT:` PASS / REVISE / BLOCK
3. `VISUAL REVIEW:` PASS / REVISE / BLOCK
4. `R2 FINDING RECHECK:` bullet list for each R2 finding
5. `R1 BLOCKER REGRESSION CHECK:` bullet list
6. `FRESH FINDINGS:` numbered list with severity, exact claim, why it is vulnerable, file(s) checked, and the tightest fix. If none, write `None`.
7. `TRACE CHECKS:` exact files, tests, logs, screenshots, or passages used
8. `BUNDLE GAPS:` only if needed
9. `PACKAGE HYGIENE:` whether the bundle is clean enough for this lane and what to remove/add next time

Scoring guidance:

- `10/10 PASS/PASS` means no implementation blockers, no visual blockers, no package-hygiene blockers, and only the explicitly documented live rclone/Picker spike remains as a release-gate proof outside this deterministic local review.
- Do not give `10/10` if any shipped implementation path can still import without Picker grant, lose or collapse Markdown images, accept unsafe sidecars, load unreviewed local images for Agent Inbox Markdown, leak raw Drive identifiers to remote-safe analytics/profile export, leave sidecars behind on failed import/write, or show misleading visual states.
- Do not downgrade solely because live rclone-after-Picker proof is pending if the bundle clearly documents it as a separate release gate and the deterministic implementation is otherwise sound.
