You are doing a fresh-from-scratch adversarial audit of one scoped Android MVP change.

GUIDING PRINCIPLES (respect these in your review):
1. Numbers in the manuscript are verified against pipeline CSVs, so do not question arithmetic without checking the shipped data files.
2. Do not suggest weakening claims unless you can name the concrete referee attack that the hedge would preempt.
3. Style suggestions cannot change empirical meaning.
4. The model is presented as-is, so do not reference development history or hidden correction history.
5. Figures, tables, and prose must be consistent; mismatches should be flagged specifically.
6. Your feedback is input, not instruction; duplicate or already-covered suggestions should not be inflated into fresh findings.

Read the FULL attached primary document first:

- `docs/SPRINT_28_AGENT_INBOX_DRIVE_ACCESS.md`

Then deep-review only this scope:

Sprint 28 Agent Inbox Drive access fix for the Android app. The change is intended to keep Google Drive access at `drive.file`, use Google Picker folder selection for Agent Inbox, persist that selected folder id, scan only that selected folder, stop silently creating an app-owned inbox folder, handle missing/revoked folder access as a finite `Select folder` reconnect state, keep analytics/profile privacy-safe, and add visual E2E coverage for the Settings states.

Bundle rules:

- Use only the shipped bundle as the audit base.
- If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.
- Do not treat missing live device artifacts as implicitly passed. The bundle intentionally records that connected screenshot run and rclone-after-Picker spike are pending because no emulator/device is attached in the current Codex environment.
- Do not ask for broader `drive.readonly` unless you can show from shipped files or Google's documented API model that Picker-folder `drive.file` is insufficient for externally added package children.

Known prior bug classes to actively test against:

- The app silently creates or scans an app-owned Agent Inbox folder instead of the user's externally populated folder.
- `drive.file` is widened to `drive.readonly` without PRD/privacy decision and review.
- Scan reports `connected, no packages found` when the selected folder is missing, revoked, or inaccessible.
- Agent Inbox scan/import leaks raw Drive folder ids, file ids, package ids, content filenames, or tokens into remote-safe analytics or Portable Profile.
- Normal annotation Drive sync accidentally triggers the Agent Inbox Picker folder flow.
- Existing Sprint 27 Agent Inbox import guarantees are weakened: finite review, explicit priority opt-in, no silent import, one manifest plus one content file, bounded downloads, duplicate/fingerprint safety.
- Visual evidence asserts stale copy or misses the new `Select folder` / access-lost states.

Your job:

1. Verify the authorization flow: `AGENT_INBOX_PICK_FOLDER` should request only the existing `drive.file` scope plus `PICKER_OAUTH_TRIGGER=true`, `PICKER_ALLOW_FOLDER_SELECTION=true`, `prompt=CONSENT`, and `optOutIncludingGrantedScopes=true`; non-picker modes should not carry those Picker parameters.
2. Verify scan semantics: no selected folder means no Drive call; selected-folder scan lists children of exactly that folder; the Drive client no longer creates/searches by a hardcoded inbox name.
3. Verify access-lost behavior: selected-folder 401/403/404 failures clear local Agent Inbox Drive connection state, return Settings to a selectable folder path, and record privacy-safe analytics.
4. Verify privacy and product scope: no whole-Drive scan, no silent import, no raw Drive identifiers in remote-safe analytics or Portable Profile, no scope drift beyond PRD.
5. Verify tests/evidence: unit tests should cover the risky logic; instrumented visual test should cover disconnected, missing-folder, selected-folder, and access-lost Settings states; recorded evidence should honestly gate live rclone/Picker proof and physical screenshots before release.

Output format:

1. `SCORE:` integer 0-10
2. `VERDICT:` PASS / REVISE / BLOCK
3. `VISUAL REVIEW:` PASS / REVISE / BLOCK
4. `FRESH FINDINGS:` numbered list with severity, exact claim, why it is vulnerable, file(s) checked, and the tightest fix
5. `TRACE CHECKS:` exact files, functions, tests, docs, or evidence passages used
6. `BUNDLE GAPS:` only if needed
7. `PACKAGE HYGIENE:` say whether the shipped bundle is clean enough for this lane, and name stale/redundant/missing artifacts if not

Scoring guidance:

- `10/10 PASS/PASS` means no code, PRD, test, privacy, or visual-test design blocker remains in the shipped bundle. It does not require pretending live device artifacts exist; if release readiness is blocked only by explicitly documented external device gates, say that precisely.
- Use `BLOCK` for any issue that could ship the original rclone invisibility bug, widen Drive scope unsafely, leak private identifiers, or falsely claim release readiness without required device evidence.
- Use `REVISE` for narrow fixes or evidence gaps that do not invalidate the implementation direction.
