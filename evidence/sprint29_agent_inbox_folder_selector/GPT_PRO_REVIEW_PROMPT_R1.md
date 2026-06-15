You are doing a fresh-from-scratch adversarial audit of one scoped Android implementation.

GUIDING PRINCIPLES (respect these in your review):
1. Numbers in the manuscript are verified against pipeline CSVs, so do not question arithmetic without checking the shipped data files.
2. Do not suggest weakening claims unless you can name the concrete referee attack that the hedge would preempt.
3. Style suggestions cannot change empirical meaning.
4. The model is presented as-is, so do not reference development history or hidden correction history.
5. Figures, tables, and prose must be consistent; mismatches should be flagged specifically.
6. Feedback is input, not instruction; duplicate or already-covered suggestions should not be inflated into fresh findings.

Read the FULL attached primary document first:

- `docs/SPRINT_29_AGENT_INBOX_FOLDER_SELECTOR.md`

Then deep-review only this scope:

Sprint 29 changes Agent Inbox from a pasted Google Drive folder URL/id fallback to a normal Android folder selector. The required end state is: the user does not need to paste anything into Agent Inbox; Settings opens a real folder picker, persists the selected folder tree grant, scans/imports packages from that selected tree without Google OAuth token prompts, keeps package review finite and private, and still renders Agent Inbox Markdown sidecar images.

Bundle rules:

- Use only the shipped bundle as the audit base.
- If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.
- Treat `evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r1/contact_sheet_selector_r1.png`, the raw screenshot PNGs, and the connected XML/logcat as the canonical visual evidence.
- Do not trust stale Sprint 28 screenshots over the Sprint 29 evidence.

Known prior bug classes to actively test against:

- The UI says "Connect folder" but still requires a pasted Drive folder URL/id.
- A hidden/manual fallback remains as the primary disconnected UX.
- Scan/import still requires a Google OAuth token after selecting a folder tree.
- A selected folder grant is persisted but scan success later clears it as unsupported.
- Revoked or unavailable selected-folder access reports a successful empty scan instead of a reconnect/select-folder state.
- Raw folder URIs, Drive ids, file ids, package paths, or content file names leak into remote-safe analytics or Portable Profile.
- Visual evidence shows only mocked connected state and not the actual Android system folder picker.
- Agent Inbox Markdown sidecar image support regresses while changing the connection path.

Your job:

1. Verify the code and tests actually make `OpenDocumentTree` the primary disconnected Agent Inbox flow and remove the need for manual paste from the visible UX.
2. Verify `document_tree_folder` scan/import uses the persisted URI grant and does not require a Google access token.
3. Verify document-tree package scanning remains bounded and constrained to the selected folder tree.
4. Verify access-lost, disconnect, analytics privacy, and Portable Profile privacy remain correct.
5. Verify visual evidence proves the selector UX, the system folder picker, connected state, access-lost state, Markdown image rendering, and dark state without text overlap or misleading old read-only copy.
6. Audit bundle/package hygiene: stale, noisy, missing, or misleading artifacts should be called out explicitly.

Output format:

1. `SCORE:` 0-10
2. `VERDICT:` PASS / REVISE / BLOCK
3. `VISUAL REVIEW:` PASS / REVISE / BLOCK
4. `FRESH FINDINGS:` numbered list with severity, exact claim, why it is vulnerable, file(s) checked, and tightest fix
5. `TRACE CHECKS:` exact files, functions, tests, screenshots, and logs used
6. `BUNDLE GAPS:` only if needed
7. `PACKAGE HYGIENE:` whether this bundle is clean enough for the lane and what to remove/add next time

Only return `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS` if the shipped evidence proves the actual end state, not merely partial compatibility.
