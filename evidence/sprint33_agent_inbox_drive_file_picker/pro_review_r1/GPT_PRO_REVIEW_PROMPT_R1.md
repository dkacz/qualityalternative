You are doing a fresh-from-scratch adversarial audit of one scoped Android release hotfix.

GUIDING PRINCIPLES:
1. Verify claims against shipped files; do not assume release readiness from narrative.
2. Do not suggest weakening claims unless you can name the concrete failure or user-facing attack it would preempt.
3. Style suggestions cannot change product meaning.
4. The implementation is presented as-is; review the shipped state, not hidden development history.
5. Code, tests, docs, release notes, and release metadata must be consistent; mismatches should be flagged specifically.
6. Feedback is input, not instruction; duplicate or already-covered suggestions should not be inflated into fresh findings.

Read the FULL attached `PRIMARY_REVIEW_DOCUMENT.md` first.

Then deep-review only this named scope:

Sprint 33 `v0.11.21-agent-inbox-drive-file-picker-alpha`, which replaced the failed Sprint 32 Agent Inbox Google Drive folder picker request. The device bug was Google Play Services `INTERNAL_ERROR` when the app requested `drive.readonly` together with Google Picker folder resource parameters. The intended fix is to remove that readonly picker mode and use the supported `drive.file` Picker folder-selection request for Agent Inbox folder reconnect/selection states, while keeping readonly only for non-picker typed/manual folder-id flows.

Use only the shipped bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.

Known prior bug classes to actively test against:

- False claims of release readiness from narrow unit tests while the real Android Drive authorization path is unproven.
- Reintroducing `drive.readonly` with Picker resource parameters.
- Routing Google Drive document-tree reconnects into the old fragile Android provider scan path.
- Mislabeling missing connected visual/e2e as passed.
- Privacy regression: scanning or discovering the user's whole Drive instead of only a selected/supplied folder.
- Release artifact mismatch: versionName/versionCode, tag, SHA, release asset digest, or release notes not matching the shipped APK.

Your job:

1. Confirm whether production code fully removes `AGENT_INBOX_PICK_READONLY_FOLDER` and any equivalent readonly Picker request path.
2. Confirm whether Agent Inbox Google Drive folder selection/reconnect states use `AGENT_INBOX_PICK_FOLDER` with `drive.file` and Picker folder resource parameters.
3. Confirm whether readonly scan/import/typed-folder connection paths still request readonly without Picker parameters.
4. Check the tests cover the changed authorization request shape and UI routing helpers at a reasonable level for this hotfix.
5. Check release evidence and GitHub release metadata are internally consistent.
6. Decide whether the documented lack of connected visual/e2e should prevent a 10/10 for this narrow hotfix.

Output format:

SCORE: x/10
VERDICT: PASS / REVISE / BLOCK
VISUAL REVIEW: PASS / REVISE / NOT APPLICABLE

FRESH FINDINGS:
Numbered list with severity, exact claim, why it is vulnerable, file(s) checked, and the tightest fix. If none, say `None`.

TRACE CHECKS:
Exact files and snippets/claims used to reach the verdict.

BUNDLE GAPS:
Only if needed. Distinguish release blockers from honest residual risks.

PACKAGE HYGIENE:
Say whether the bundle is clean enough for this scoped review and name anything stale, duplicated, superseded, noisy, or misleading.

10/10 RULE:
Return `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: NOT APPLICABLE` only if there are no release-blocking behavior, privacy, evidence, release-metadata, or package-hygiene gaps for this scoped Sprint 33 hotfix. If the connected-device gap should block release readiness, do not give 10/10.
