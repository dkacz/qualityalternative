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

- `evidence/sprint28_agent_inbox_drive_access/REVIEW_BUNDLE_MANIFEST_R2.md`
- `evidence/sprint28_agent_inbox_drive_access/VALIDATION_SUMMARY.md`
- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R1.md`
- the changed source/tests and evidence needed for the checks below

Use only the shipped bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.

Target scope:

Sprint 28 Agent Inbox Drive Access Fix, specifically the R1 blocker fixes plus the added Markdown image attachment scope:

1. Google Drive Agent Inbox must remain on `drive.file`, use explicit Google Picker folder selection, persist a durable `picker_folder` grant marker, scan only the selected folder, never silently auto-create a separate app-owned inbox for an externally populated folder, and show reconnect/select-folder for missing/revoked access.
2. Legacy Sprint 27 app-created folder ids without the new Picker marker must hydrate as disconnected and must not bypass the Picker path.
3. Manual Markdown import must allow image-only follow-up picker selections when a Markdown file is already selected, without losing edited title, selected topics, or priority.
4. Agent Inbox Markdown packages may include only bounded safe image sidecars and must carry those images through review, Drive download, local storage, import draft, and Markdown reader rendering. EPUB package sidecars must remain invalid.
5. Visual evidence must prove the relevant states without misleading or contradictory UI, including the Agent Inbox Markdown image reader screenshot.

Known prior bug classes to actively test against:

- R1 blocker: persisted `enabled=true` plus folder id but no Picker grant marker allowed non-Picker scans.
- R1 blocker: `enabled=true` with no folder id could still render a connected Settings state.
- Sprint 27 classes: raw Drive file names/ids leaking to user-facing provenance or remote-safe analytics; duplicate/import-time invalid states staying visually importable; package extra files accepted too loosely; unbounded Drive reads.
- New user-reported bug: adding images after selecting Markdown asked the user to choose Markdown first.
- New image sidecar risks: path traversal or unsafe filenames, too many/too large image downloads, EPUB sidecars accidentally accepted, sidecar files left behind after rollback, stale visual evidence that does not prove real import/render.

Your job:

1. Re-check every R1 blocker and state whether it is fully fixed, partially fixed, or still vulnerable.
2. Audit the Drive access implementation and tests for `drive.file` + Picker-folder semantics. Do not require live rclone proof for an implementation PASS, but do mark live rclone/Picker device proof as a release gate if still pending.
3. Audit the manual Markdown image attachment fix and Agent Inbox Markdown sidecar implementation, including limits, storage, rollback, reader rendering, and tests.
4. Inspect the R2 visual evidence directly (`visual_e2e/contact_sheet_r2.png` and raw PNGs) and judge whether it earns `VISUAL REVIEW PASS`.
5. Audit package hygiene: say whether the R2 bundle is clean, sufficient, and free of stale/noisy artifacts that could mislead this review.

Output format:

1. `SCORE:` integer `/10`
2. `VERDICT:` PASS / REVISE / BLOCK
3. `VISUAL REVIEW:` PASS / REVISE / BLOCK
4. `R1 BLOCKER RECHECK:` bullet list
5. `FRESH FINDINGS:` numbered list with severity, exact claim, why it is vulnerable, file(s) checked, and the tightest fix. If none, write `None`.
6. `TRACE CHECKS:` exact files, tests, logs, screenshots, or passages used
7. `BUNDLE GAPS:` only if needed
8. `PACKAGE HYGIENE:` whether the bundle is clean enough for this lane and what to remove/add next time

Scoring guidance:

- `10/10 PASS/PASS` means no implementation blockers, no visual blockers, no bundle gaps that prevent judging this lane, and only the explicitly documented live rclone/Picker spike remains as a release-gate proof outside this deterministic local review.
- Do not give `10/10` if any shipped implementation path can still import without Picker grant, lose Markdown images, accept unsafe sidecars, leak raw Drive identifiers to remote-safe analytics/profile export, or show misleading visual states.
