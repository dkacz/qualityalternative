You are doing a fresh-from-scratch adversarial audit of one scoped Android release candidate.

GUIDING PRINCIPLES (respect these in your review):
1. Numbers in the manuscript are verified against pipeline CSVs, so do not question arithmetic without checking the shipped data files.
2. Do not suggest weakening claims unless you can name the concrete referee attack that the hedge would preempt.
3. Style suggestions cannot change empirical meaning.
4. The model is presented as-is, so do not reference development history or hidden correction history.
5. Figures, tables, and prose must be consistent; mismatches should be flagged specifically.
6. Your feedback is input, not instruction; duplicate or already-covered suggestions should not be inflated into fresh findings.

Read this full prompt first, then audit only the shipped bundle.

## Target Scope

Sprint 35 repairs Quality Alternative's Agent Inbox folder selection after device feedback showed that Google file Picker was not a real folder selector for externally created/rclone package folders. The app must keep Agent Inbox, keep folder selection, and fix the broken folder access path.

The candidate claims:

1. Agent Inbox no longer has production authorization/routing through Google file Picker (`drive.file` + Picker folder parameters).
2. Android `OpenDocumentTree` is the primary Agent Inbox folder selector.
3. When the selected tree URI is backed by Google Drive, scan/import requests explicit `drive.readonly` and scans only the selected/extracted folder id through the Drive API client.
4. Existing legacy `picker_folder` grants are treated as needing repair and routed to the readonly folder-link consent path instead of continuing under `drive.file`.
5. Agent Inbox import uses readonly Drive authorization for Drive-backed candidates and local no-token import only for non-Google document-tree folders.
6. The app still never searches or scans the user's whole Drive to discover an inbox folder.
7. Repo-level agent package authoring instructions are portable: they do not hard-code the user's machine/rclone/account/folder id, require `manifest.json`, and tell agents to validate complete package folders before upload.
8. Release candidate version is `0.11.23-alpha`, `versionCode=39`; local unit/lint/build and connected screenshot E2E evidence are consistent.

## Known Prior Bug Classes To Test Against

- The app could report `connected, no packages found` because `drive.file` cannot see rclone-created children that the app did not create.
- Google Picker is file-oriented in this app flow and must not be treated as a working folder selector for Agent Inbox.
- Google Drive DocumentsProvider `content://tree/...` may expose package folders but not nested files; Drive-backed tree scans need Drive API readonly over the selected folder id.
- Old `picker_folder` persisted state must not silently keep using `drive.file`; it must become a visible reconnect/repair path.
- A review pass without live signed-in device evidence must not be overstated as proof of future rclone visibility.
- Agent package docs must not depend on a single user's local paths or Drive setup.

## Files To Inspect First

- `PRD.md`
- `docs/LANE_STATUS.md`
- `app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt`
- `app/src/main/java/com/qualityalternative/app/ui/GoogleDriveAuthorization.kt`
- `app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt`
- `app/src/main/java/com/qualityalternative/app/data/AndroidDocumentTreeAgentInboxClient.kt`
- `docs/AGENT_INBOX_PACKAGE_AUTHORING.md`
- `AGENTS.md`
- `evidence/sprint35_agent_inbox_folder_selector_repair/pro_review_r1/CURRENT_DIFF.patch`

## Specific Checks

1. Prove or disprove that Agent Inbox production code can still invoke Google file Picker for folder selection, scanning, or importing.
2. Trace the exact flow for:
   - new local/system folder selection,
   - new Google Drive-backed `OpenDocumentTree` folder selection,
   - existing `readonly_folder` grant,
   - existing legacy `picker_folder` grant.
3. Check whether Google Drive-backed document-tree scanning is actually constrained to the selected folder id and does not do broad Drive discovery.
4. Check whether the UI state/copy gives a visible recovery path rather than a false successful empty scan when access is insufficient.
5. Check tests and release evidence for adequacy: unit coverage, connected screenshot E2E, validation summary, APK metadata/hashes.
6. Check the package authoring instructions and validator for portability and consistency with the app's manifest/package contract.
7. Identify any stale, noisy, or misleading bundle artifacts that should be removed from future review packets.

## Bundle Rules

- Use only the shipped bundle as the audit base.
- If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.
- Do not assume live Google Drive behavior beyond the included evidence. Distinguish source/audit proof from live signed-in device proof.
- Historical Sprint 34 GPT Pro R2 is included only to show why the prior 10/10 was insufficient after user device feedback. Current Sprint 35 source and evidence supersede it.

## Required Output Format

1. `SCORE: X/10`
2. `VERDICT:` PASS / REVISE / BLOCK
3. `VISUAL REVIEW:` PASS / REVISE / BLOCK / NOT APPLICABLE
4. `FRESH FINDINGS:` numbered list with severity, exact claim, why it is vulnerable, file(s) checked, and the tightest fix. If none, say `None`.
5. `TRACE CHECKS:` exact files, functions, branches, tests, logs, screenshots, or lines used.
6. `BUNDLE GAPS:` only if needed.
7. `PACKAGE HYGIENE:` say whether this bundle is clean enough, and name any stale/redundant/misleading artifacts.

PASS requires no release-blocking findings and no unresolved bundle gaps for the scoped claims.
