# GPT Pro Review Prompt: Sprint 36 Agent Inbox Live Drive Picker R2

You are reviewing a release candidate for the Android app `qualityalternative`.

Required verdict format:

- `SCORE: N/10`
- `VERDICT: PASS` or `FAIL`
- `BLOCKERS: ...`
- `EVIDENCE CHECKED: ...`
- `RELEASE DECISION: ...`

Important rule: a 10/10 PASS is invalid unless you cite the live evidence paths and confirm the final APK hash.

This is a resubmission after an initial GPT Pro FAIL. The previous review is included at `evidence/sprint36_agent_inbox_live_picker_e2e/gpt_pro_review_response.md` and returned `SCORE: 8/10`, `VERDICT: FAIL` because primary folder-browser controls were clipped and the scan/import screenshot had a snackbar over the lower candidate action area. Re-check that the current R2 evidence fixes those exact issues.

## Scope

The feature under review is Agent Inbox Drive folder selection and import. The user complained that previous reviews passed code-only work while the real app still failed with missing manifests, connection failures, or no working folder picker. This review must be evidence-driven, not trust-based.

## Final APK

The final debug APK under test is:

- `app/build/outputs/apk/debug/app-debug.apk`
- SHA-256: `83544a7efce11141c48cca25bed5ffb6a8da9e1429565c9c074b2fe35ba71348`
- Metadata evidence: `evidence/sprint36_agent_inbox_live_picker_e2e/logs/live_debug_apk_metadata.txt`

## Evidence Bundle

Use:

- `evidence/sprint36_agent_inbox_live_picker_e2e/LIVE_E2E_REPORT.md`
- `evidence/sprint36_agent_inbox_live_picker_e2e/live_e2e/`
- `evidence/sprint36_agent_inbox_live_picker_e2e/logs/`
- `evidence/sprint36_agent_inbox_live_picker_e2e/visual_e2e/sprint35-agent-inbox-folder-selector-repair-1781630925520/`
- `docs/AGENT_INBOX_LIVE_REVIEW_GATE.md`
- `evidence/sprint36_agent_inbox_live_picker_e2e/gpt_pro_review_response.md` for the prior failed review and the exact blocker being re-tested.

## Must-Fail Conditions

Return FAIL if any of these are true:

- The evidence does not show a signed-in emulator or device.
- The evidence does not show Google Play Services account authorization.
- The in-app folder browser does not show the real Drive folder.
- The app only uses a pasted Drive link as the primary path.
- The scan does not show `Live Drive Agent Inbox Test`.
- Import does not clear the queue.
- Library does not show the imported document under `Files`.
- Reader does not render the imported Markdown content.
- The APK hash in evidence is not `83544a7efce11141c48cca25bed5ffb6a8da9e1429565c9c074b2fe35ba71348`.
- The target folder row's `Open` or `Select` controls are clipped, overlapped by bottom navigation, or unusable in `live_e2e/09_after_google_account_selected.png`.
- The connected visual fixture still shows clipped folder-browser controls in `visual_e2e/sprint35-agent-inbox-folder-selector-repair-1781630925520/00b_agent_inbox_drive_folder_browser_light.png`.
- The scan/import evidence is obscured by a snackbar or does not clearly show the `Import` control in `live_e2e/10_after_drive_folder_selected.png`.
- Logcat shows an app crash or old failure strings: `Package is missing manifest`, `Package could not be saved`, `Agent Inbox package could not be imported`, or `Google Drive authorization hit a Google Play services error`.

## Review Tasks

1. Check code changes for regressions around Drive scope, folder listing, scan, import, legacy picker state, and UI state.
2. Check automated test coverage and whether the tests match the changed behavior.
3. Inspect the evidence report and key screenshots.
4. Compare the current evidence against the prior FAIL in `gpt_pro_review_response.md`, especially clipping in `live_e2e/09_after_google_account_selected.png`, the visual fixture `00b_agent_inbox_drive_folder_browser_light.png`, and snackbar coverage in `live_e2e/10_after_drive_folder_selected.png`.
5. Confirm that live evidence proves a real external Drive package was selected, scanned, imported, and opened.
6. Decide whether this is release-ready. Do not give 10/10 unless every required evidence item is present and consistent.
