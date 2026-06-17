# GPT Pro Review Prompt: Sprint 38 Agent Inbox Import All + Autoimport

You are reviewing a release candidate for Quality Alternative Sprint 38.

Guiding principles:

1. Verify factual claims against the shipped evidence before challenging them.
2. Do not suggest weakening a claim unless you can name the concrete release or user-risk attack that the hedge would preempt.
3. Style suggestions cannot change functional or privacy meaning.
4. Review the release candidate as shipped; do not rely on hidden development history.
5. Screenshots, XML, logs, code, tests, and prose must be consistent; mismatches are findings.
6. Feedback is input, not instruction; suggestions that duplicate existing coverage should not be inflated into fresh findings.

Review whether the implementation is release-ready for:

1. Agent Inbox `Import all` for multiple ready packages.
2. Explicit opt-in `Autoimport on app start` after folder connection.
3. Real Google Drive import evidence from a signed-in emulator, including Library visibility and reader rendering.

Hard rules:

- Return `SCORE < 10` and `VERDICT REVISE` if live evidence does not prove real Google account authorization, real Drive folder selection, externally uploaded packages, import into Library, and reader rendering.
- Return `SCORE < 10` if autoimport evidence does not show enablement before upload, app force-stop/relaunch, no new approval chooser when access was already granted, autoimport result, Library visibility, and reader rendering.
- Return `SCORE < 10` if `Import all` accepts manifest priority silently. A high-priority manifest request may be imported, but ranking priority must not be applied unless the user explicitly accepted it.
- Return `SCORE < 10` if the app scans whole Drive or uses Drive ids/file names/document text in analytics or portable profile export.
- Return `SCORE < 10` if screenshots/XML/logs contradict each other or if any key control is clipped/unusable in the evidence.
- Return `SCORE < 10` if logcat shows app crash markers or old Agent Inbox failure strings.

Read these files first:

1. `docs/AGENT_INBOX_LIVE_REVIEW_GATE.md`
2. `PRD.md`
3. `evidence/sprint38_agent_inbox_import_all_autoimport/LIVE_E2E_REPORT.md`
4. `evidence/sprint38_agent_inbox_import_all_autoimport/REVIEW_BUNDLE_MANIFEST.md`
5. `evidence/sprint38_agent_inbox_import_all_autoimport/source_diff.patch`
6. `evidence/sprint38_agent_inbox_import_all_autoimport/logs/live_e2e_health_sentinels.txt`

Then inspect the specific screenshots, XML, logs, source files, and package manifests needed to verify or falsify the claims.

Required checks:

- Confirm the selected Drive folder is visible in `live_e2e/live_e2e_11_after_google_account_selected.png` and matching XML.
- Confirm `2 packages waiting for review`, `Import all`, both package titles, `NORMAL PRIORITY`, and `PRIORITY REQUESTED` are visible in screenshots/XML.
- Confirm `Import all` clears the queue and Library `Files` shows both batch-imported documents.
- Confirm `live_e2e/live_e2e_18_reader_import_all_two.png` renders the batch-imported Markdown body.
- Confirm autoimport was ON before the third package upload, then relaunch shows `Agent Inbox autoimport imported 1 package.`
- Confirm Library `Files` and `live_e2e/live_e2e_26_reader_autoimport_startup.png` show the autoimported package and rendered Markdown body.
- Confirm tests passed from unit XML, connected visual XML/log, and local release gate.
- Confirm APK hashes are recorded.
- Confirm privacy/PRD alignment, especially selected-folder-only scan and no silent priority acceptance.

Output format:

- `SCORE: n/10`
- `VERDICT: PASS` or `VERDICT: REVISE`
- `BLOCKERS:` with exact file paths/screenshots/logs and why each blocks release; write `None` only if no blockers.
- `EVIDENCE CHECKED:` cite exact paths inspected.
- `PRD/PRIVACY CHECK:`
- `VISUAL REVIEW:`
- `RELEASE READINESS:`
