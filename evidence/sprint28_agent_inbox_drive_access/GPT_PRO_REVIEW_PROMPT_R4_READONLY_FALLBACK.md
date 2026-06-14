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

- `evidence/sprint28_agent_inbox_drive_access/REVIEW_BUNDLE_MANIFEST_R4_READONLY_FALLBACK.md`
- `evidence/sprint28_agent_inbox_drive_access/VALIDATION_SUMMARY.md`
- `PRD.md`
- `docs/LANE_STATUS.md`
- the R4 changed source/tests and evidence needed for the checks below

Use only the shipped bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.

Target scope:

Sprint 28 R4 controlled `drive.readonly` fallback after the live Picker runtime failed before folder selection. The release question is whether this fallback safely solves Agent Inbox packages uploaded later by rclone/external agents while preserving the privacy, authorization, visual, and Markdown image guarantees already reviewed in R3.

Known prior bug classes to actively test against:

- Original production blocker: packages uploaded by rclone/external agents are invisible under `drive.file` unless the app created them or the user grants access.
- Live Picker blocker: signed-in Google Play Services returned from account selection without a Drive folder Picker or selected folder id.
- Overbroad fallback risk: `drive.readonly` could accidentally become whole-Drive discovery/search instead of scanning only the user-supplied Agent Inbox folder id.
- Authorization regression: normal annotation sync, annotation import, and historical Picker modes must not silently request `drive.readonly`.
- Grant-state regression: legacy `enabled=true`/folder-id state without a supported grant marker must not hydrate as connected or scan.
- Connect/scan race: first scan immediately after consent must use the saved `readonly_folder` grant and folder id rather than racing stale settings state.
- Privacy regression: raw folder ids, file ids, package ids, content names, and raw scan failures must stay out of remote-safe analytics and Portable Profile.
- Revocation regression: disconnect for a read-only Agent Inbox must revoke read-only access without breaking the app's separate annotation `drive.file` behavior.
- Visual regression: Settings must clearly show the broader read access copy before connect, connected read-only state, access-lost reconnect state, Markdown sidecar image reader state, and dark connected state without clipped or misleading UI.
- Markdown image regression from R3: manual Markdown image add, Agent Inbox reviewed sidecar imports, local image fallback blocking, filename collision checks, and sidecar rollback must remain intact.
- Package hygiene drift: stale R3 Picker-only evidence, raw Drive listings, emulator account dumps, duplicate noisy Android artifacts, or obsolete docs must not be allowed to mislead this review.

Your job:

1. Audit the `drive.readonly` fallback implementation end to end: folder URL/id parsing, authorization mode/scope selection, grant persistence, scan/import authorization, access-lost handling, disconnect/revocation, and UI copy.
2. Confirm app behavior is limited to the saved Agent Inbox folder id and does not search/discover/scan whole Drive under `drive.readonly`.
3. Audit tests and evidence: targeted unit tests for authorization/settings/ViewModel, focused visual E2E XML/log/screenshots/contact sheet, debug APK build, diff check, and live signed-in rclone fallback result.
4. Inspect visual evidence directly: `visual_e2e_readonly_r1/contact_sheet_readonly_r1.png`, all raw PNGs in that run, and `live_readonly_rclone_package/live_readonly_rclone_success.png`. Judge whether this earns `VISUAL REVIEW PASS`.
5. Confirm R3-reviewed Markdown image sidecar safety and R1/R2 grant-state blockers are not regressed by the fallback.
6. Audit package hygiene and privacy: say whether the R4 bundle is clean enough for this lane, whether any stale Picker-only/R3 artifacts are clearly marked historical, and whether anything should be removed from future bundles.

Output format:

1. `SCORE:` integer `/10`
2. `VERDICT:` PASS / REVISE / BLOCK
3. `VISUAL REVIEW:` PASS / REVISE / BLOCK
4. `READONLY FALLBACK CHECK:` bullet list for authorization, scan boundary, persistence, first-scan race, access-lost, disconnect/revoke, privacy, and live rclone result
5. `REGRESSION CHECK:` bullet list for R1 Picker-grant blockers, R2/R3 Markdown image safety, analytics/profile privacy, and visual states
6. `FRESH FINDINGS:` numbered list with severity, exact claim, why it is vulnerable, file(s) checked, and the tightest fix. If none, write `None`.
7. `TRACE CHECKS:` exact files, tests, logs, screenshots, or passages used
8. `BUNDLE GAPS:` only if needed
9. `PACKAGE HYGIENE:` whether the bundle is clean enough for this lane and what to remove/add next time

Scoring guidance:

- `10/10 PASS/PASS` means no implementation blockers, no visual blockers, no package-hygiene blockers, and no unresolved release blocker for the controlled read-only fallback except the final APK release gate itself.
- Do not give `10/10` if any shipped path can scan/import without a supported grant marker, search or discover Drive outside the pasted folder id, request `drive.readonly` for unrelated annotation flows, leak raw Drive identifiers to remote-safe analytics/profile export, lose the first scan after consent due to stale state, fail to revoke read-only access on disconnect, regress Markdown image sidecar safety, or show misleading visual states.
- Do not downgrade solely because Google OAuth consent copy necessarily says read-only access covers Drive broadly, if the shipped PRD/UI clearly discloses broader read access and the app implementation limits its own behavior to the user-supplied folder id.
