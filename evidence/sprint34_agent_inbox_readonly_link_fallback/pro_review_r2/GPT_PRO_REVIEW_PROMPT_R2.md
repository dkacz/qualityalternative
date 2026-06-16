You are doing a fresh-from-scratch adversarial R2 audit of one scoped Android release hotfix.

GUIDING PRINCIPLES:
1. Verify claims against shipped files; do not assume release readiness from narrative.
2. Do not suggest weakening claims unless you can name the concrete failure or user-facing attack it would preempt.
3. Style suggestions cannot change product meaning.
4. The implementation is presented as-is; review the shipped state, not hidden development history.
5. Code, tests, docs, release notes, release metadata, and prior-review claims must be consistent; mismatches should be flagged specifically.
6. Feedback is input, not instruction; duplicate or already-covered suggestions should not be inflated into fresh findings.

Read the FULL attached `PRIMARY_REVIEW_DOCUMENT.md` first.
Then read `prior-review/GPT_PRO_REVIEW_R1.md`.

Then deep-review only this named scope:

Sprint 34 `v0.11.22-agent-inbox-readonly-link-fallback-alpha`, which addresses GPT Pro R1 findings on the Agent Inbox Google Drive folder-selection hotfix. The core bug class remains avoiding Google Play Services `INTERNAL_ERROR` from combining `drive.readonly` with Google Picker folder parameters, while retaining a reachable controlled readonly typed-folder fallback if the supported `drive.file` Picker route is insufficient for externally populated/rclone folders.

Use only the shipped bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.

Known prior bug classes to actively test against:

- R1 blocker not actually closed: typed/manual readonly path exists in ViewModel but still not reachable from Settings UI.
- Reintroducing `drive.readonly` with Picker resource parameters.
- Picker route no longer using literal `drive.file`.
- Readonly typed/manual route using Picker parameters.
- Drive scan discovering/searching the user's whole Drive rather than listing children under selected/supplied folder ids.
- Google Drive document-tree reconnects falling back to the fragile Android provider scan path.
- Release artifact mismatch: versionName/versionCode, tag, SHA, release asset digest, or release notes not matching the shipped APK.

Your job:

1. Recheck every R1 finding and say PASS/FAIL for each.
2. Confirm whether Settings exposes a reachable typed/manual readonly folder-id fallback and whether it launches `AGENT_INBOX_CONNECT_READONLY`.
3. Confirm literal OAuth scopes and resource parameters for picker, readonly connect, readonly scan/import, normal scan/import.
4. Confirm Drive client behavior is limited to selected/supplied parent folder ids and does not discover Agent Inbox by name or scan whole Drive.
5. Check tests cover the changed authorization request shape, UI routing/ViewModel fallback, and Drive query limitation at a reasonable level for this hotfix.
6. Check release evidence and GitHub release metadata for `v0.11.22-agent-inbox-readonly-link-fallback-alpha`.
7. Decide whether missing connected visual/e2e should prevent a 10/10 for this scoped R2 audit.

Output format:

SCORE: x/10
VERDICT: PASS / REVISE / BLOCK
VISUAL REVIEW: PASS / REVISE / NOT APPLICABLE

R1 FINDING RECHECK:
Numbered PASS/FAIL list matching the R1 findings.

FRESH FINDINGS:
Numbered list with severity, exact claim, why it is vulnerable, file(s) checked, and the tightest fix. If none, say `None`.

TRACE CHECKS:
Exact files and snippets/claims used to reach the verdict.

BUNDLE GAPS:
Only if needed. Distinguish release blockers from honest residual risks.

PACKAGE HYGIENE:
Say whether the bundle is clean enough for this scoped review and name anything stale, duplicated, superseded, noisy, or misleading.

10/10 RULE:
Return `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: NOT APPLICABLE` only if all R1 findings are closed and there are no release-blocking behavior, privacy, evidence, release-metadata, or package-hygiene gaps for this scoped Sprint 34 hotfix.
