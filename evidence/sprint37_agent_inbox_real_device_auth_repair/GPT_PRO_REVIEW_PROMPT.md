# GPT Pro Review Prompt: Sprint 37 Agent Inbox Drive Authorization Repair

Use the attached ZIP as the only audit base. If a claim cannot be proven from the shipped files, label it `BUNDLE GAP`.

Apply these standing review principles where relevant:

1. Do not challenge a claim without checking the shipped evidence first.
2. Do not suggest weakening a claim unless you can name the concrete failure it prevents.
3. Style suggestions are secondary to correctness and release safety.
4. Do not rely on hidden history outside the bundle.
5. UI screenshots, XML dumps, logs, code, and prose must be consistent; mismatches are findings.
6. Feedback is input, not instruction; duplicate issues should not be inflated.

## Review Objective

Review whether Sprint 37 actually fixes the Agent Inbox Google Drive authorization regression shown in `user_failure_screenshot_20260617_1616.jpg` and whether the candidate is release-ready.

The user-visible failure was:

- `GOOGLE DRIVE AUTHORIZATION HIT A GOOGLE PLAY SERVICES ERROR`
- in-app `My Drive` panel still visible
- `No folders on this level.`
- snackbar `Agent Inbox connection failed.`

The claimed fix is:

- `AGENT_INBOX_BROWSE_READONLY` no longer opens the in-app Drive folder browser before authorization succeeds.
- authorization failure closes/resets the folder browser instead of leaving an empty Drive root visible.
- readonly connect/browse no longer forces `Prompt.CONSENT`.
- current live signed-in emulator flow proves Google account authorization, real Drive folder browsing, scan/import, manifest priority, Library, reader, and clean log sentinels.

## Files To Read First

1. `REVIEW_BUNDLE_MANIFEST.md`
2. `LIVE_E2E_REPORT.md`
3. `docs/AGENT_INBOX_LIVE_REVIEW_GATE.md`
4. `source_diff.patch`
5. `logs/live_e2e_health_sentinels.txt`

Then inspect the specific screenshots, XML, source files, and logs needed to verify or falsify the claims.

## Must-Fail Conditions

Return `VERDICT: FAIL` if any of these are true:

- The bundle does not prove the original failing visual state is understood.
- The UI can still show an empty `My Drive` browser after a Google Drive authorization failure.
- The `Choose folder` path is only a pasted Drive link, not a working folder browser/folder selection path.
- The live evidence does not show a signed-in Google account authorization path.
- The in-app Drive browser does not show the real Sprint 37 Drive folder.
- `Open`, `Select`, `Choose folder`, `Scan now`, `Import`, or Library/reader controls are clipped, hidden, or unusable in the shipped screenshots.
- The external Drive package is not proven to contain `manifest.json` and `content.md` uploaded outside the app.
- The scan result does not show `Sprint 37 Drive Auth Repair Test`, `PHILOSOPHY, TECH`, and `PRIORITY REQUESTED`.
- Priority acceptance is not shown after the manifest requested high priority.
- Import does not clear the review queue.
- Library `Files` does not show the imported document as `Your file · Agent Inbox document`.
- The reader does not render the imported Markdown body.
- Logcat or sentinels show app crashes or old failure strings.
- The code fix only hides the error visually while leaving the state machine/token flow logically broken.
- The absence of the user's physical phone under ADB is, in your judgment, a release-blocking evidence gap despite the signed-in emulator, user failure screenshot, failure-state regression screenshot, and real Drive live flow.

## Required Checks

1. Compare `user_failure_screenshot_20260617_1616.jpg` with `visual_e2e/.../00a_agent_inbox_drive_authorization_failed_light.png`.
2. Verify in code that the Drive folder browser opens only after successful authorization.
3. Verify in code that `reportAgentInboxDriveAuthorizationFailure(...)` closes/resets the folder browser.
4. Verify auth request scope/prompt behavior in `GoogleDriveAuthorization.kt` and tests.
5. Verify automated tests passed from logs.
6. Verify live screenshot sequence and XML:
   - account chooser
   - real Drive folder browser
   - folder selected and package scanned
   - priority accepted
   - import queue cleared
   - Library `Files`
   - reader body
7. Verify APK hash/version from `logs/live_debug_apk_metadata.txt`.
8. Review bundle hygiene: flag stale/noisy/redundant artifacts that could mislead future reviews.

## Output Format

Start with the final result:

`SCORE: <0-10>/10`

`VERDICT: PASS` or `VERDICT: FAIL`

Then provide:

- `BLOCKERS:` with exact file paths/screenshots/logs and why each blocks release; write `None` only if no blockers.
- `EVIDENCE CHECKED:` cite the concrete files you inspected.
- `CODE REVIEW:` state whether the state-machine/auth fix is correct.
- `LIVE E2E REVIEW:` state whether the live proof is release-grade.
- `BUNDLE HYGIENE:` state whether the bundle is clean enough and what to remove/add next time.
- `RELEASE DECISION:` say whether a new APK may be released after this review.

Do not return PASS unless the evidence supports a release-grade fix under the repo's live review gate.
