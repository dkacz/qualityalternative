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

- `evidence/sprint28_agent_inbox_drive_access/REVIEW_BUNDLE_MANIFEST_R5_EVIDENCE_HYGIENE.md`
- `evidence/sprint28_agent_inbox_drive_access/VALIDATION_SUMMARY.md`
- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_R4_READONLY_FALLBACK.md`
- `PRD.md`
- `docs/LANE_STATUS.md`
- the source, tests, logs, screenshots, and live evidence needed for the checks below

Use only the shipped bundle as the audit base. If a claim cannot be proven from shipped files, label it `BUNDLE GAP`.

Target scope:

Sprint 28 R5 re-review of the controlled `drive.readonly` fallback after R4 returned `SCORE 9/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`.

This R5 lane is intentionally narrow. R4 already passed the implementation and visual fallback behavior, with one low evidence/package-hygiene finding:

- `live_readonly_rclone_package/RESULT.md` overstated `logcat_live_readonly_success.txt` as authorization-flow proof.
- It also referenced raw rclone listing JSON files that were excluded from the review bundle.
- R4 also noted bundle gaps for raw Gradle execution logs and live OAuth proof.

The R5 question is whether the shipped fixes close those evidence/package-hygiene issues without regressing the fallback, Sprint 27 visual coverage, or the R3 Markdown image safety guarantees.

Known constraints and non-goals:

- The Google OAuth consent screen is intentionally not shipped because it exposed private account UI. Do not require a private account screenshot as proof.
- The live OAuth claim must therefore be judged from final app state after consent/scan, source/tests for authorization behavior, and any non-private logs shipped in this bundle.
- Raw rclone listing JSON, Google account dumps, package dumps, OAuth screenshots, APKs, and release artifacts are intentionally excluded. A redacted rclone listing summary is shipped instead.
- This is still the pre-release Pro review. The final APK release gate is allowed to remain pending unless the shipped implementation/evidence proves the fallback itself is not releasable.

Known prior bug classes to actively test against:

- R4 evidence overclaim: a diagnostic log must not be presented as OAuth proof when it does not prove OAuth.
- R4 evidence contradiction: result files must not cite raw JSON artifacts that the bundle excludes.
- R4 raw-log gap: validation claims should be backed by raw Gradle logs or an equivalent shipped transcript.
- Sprint 27 visual regression: seeding a scan-success state must require the durable `picker_folder` grant marker, not an enabled/folder-id legacy shortcut.
- Original production blocker: externally uploaded rclone packages are invisible under plain `drive.file`; fallback must solve this only after explicit folder id entry and read-only consent.
- Overbroad fallback risk: `drive.readonly` must not become whole-Drive discovery/search; app behavior must scan only the saved user-supplied Agent Inbox folder id.
- Privacy regression: raw folder ids, file ids, package ids, content names, and raw scan failures must stay out of remote-safe analytics and Portable Profile.
- Markdown image regression from R3: manual Markdown image add, Agent Inbox reviewed sidecar imports, local image fallback blocking, filename collision checks, and sidecar rollback must remain intact.

Your job:

1. Check whether the single R4 finding is fully fixed in the shipped R5 evidence:
   - `live_readonly_rclone_package/RESULT.md`
   - `live_readonly_rclone_package/rclone_listing_summary_redacted.md`
   - `REVIEW_BUNDLE_MANIFEST_R5_EVIDENCE_HYGIENE.md`
   - the shipped file inventory
2. Check whether raw validation evidence is now adequate:
   - full local Gradle gate log
   - targeted Chrome rerun log
   - targeted Sprint 27 visual rerun log after durable grant seed fix
   - targeted Sprint 28 readonly visual rerun log after the seed fix
   - fresh full connected Android run log
   - `git diff --check` log
3. Re-audit only the fallback boundaries needed to confirm no regression:
   - read-only authorization modes/scopes
   - saved-folder scan boundary
   - grant persistence/hydration
   - first scan after consent
   - access-lost reconnect state
   - read-only disconnect/revoke behavior
   - privacy-safe analytics/profile export
4. Inspect visual evidence directly:
   - `visual_e2e_readonly_r1/contact_sheet_readonly_r1.png`
   - all raw PNGs in that run
   - `live_readonly_rclone_package/live_readonly_rclone_success.png`
   - result XML/logs for the targeted reruns
5. Confirm the Sprint 27 visual seed fix does not weaken the product contract and that Sprint 28 readonly visual coverage still earns `VISUAL REVIEW PASS`.
6. Audit package hygiene: say whether the R5 bundle is clean enough for this lane and whether any stale, private, duplicated, or misleading artifacts should be removed from future bundles.

Output format:

1. `SCORE:` integer `/10`
2. `VERDICT:` PASS / REVISE / BLOCK
3. `VISUAL REVIEW:` PASS / REVISE / BLOCK
4. `R4 FINDING CLOSURE:` PASS / PARTIAL / FAIL, with a short explanation
5. `READONLY FALLBACK REGRESSION CHECK:` bullet list for authorization, scan boundary, persistence, first-scan race, access-lost, disconnect/revoke, privacy, and live rclone final state
6. `VALIDATION LOG CHECK:` bullet list for each shipped raw log and whether it proves the claimed run
7. `VISUAL CHECK:` bullet list for current screenshots/contact sheet/live screenshot and whether they remain acceptable
8. `FRESH FINDINGS:` numbered list with severity, exact claim, why it is vulnerable, file(s) checked, and the tightest fix. If none, write `None`.
9. `TRACE CHECKS:` exact files, tests, logs, screenshots, or passages used
10. `BUNDLE GAPS:` only if needed
11. `PACKAGE HYGIENE:` whether the bundle is clean enough for this lane and what to remove/add next time

Scoring guidance:

- `10/10 PASS/PASS` means R4's evidence/package-hygiene finding is closed, raw validation logs are adequate, no visual blocker exists, no implementation regression is found, and no unresolved release blocker remains for the controlled read-only fallback except the final APK publication gate itself.
- Do not give `10/10` if the shipped R5 evidence still overclaims logcat as OAuth proof, cites excluded raw rclone JSON as evidence, lacks raw logs for material pass claims, includes private account artifacts, scans/discovers outside the saved folder id, leaks raw Drive identifiers through remote-safe analytics/profile export, regresses Sprint 27 or Sprint 28 visual coverage, or weakens R3 Markdown image sidecar safety.
- Do not downgrade solely because the OAuth consent screenshot is absent, provided the bundle is explicit that private account UI was excluded and the final live app state plus source/tests/logs are sufficient for the scoped claim.
