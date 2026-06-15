# GPT Pro Review R2 - Sprint 29 Agent Inbox Folder Selector

You are reviewing Sprint 29 R2 for the Android app in this bundle. The user goal is strict:

> The user should not need to paste anything into Agent Inbox. The app should use a normal folder selector, and this sprint should iterate until GPT Pro gives 10/10.

Please review as a senior Android/product-quality reviewer. Be skeptical and evidence-based.

## Review Principles

- Audit the shipped evidence and current code, not hidden development history.
- Do not inflate already-covered R1 feedback into fresh findings if the R2 code and evidence close it.
- If a claim cannot be proven from the bundle, label it `BUNDLE GAP` rather than guessing.
- Style suggestions are not blockers unless they change user-visible correctness, privacy, or release quality.

## R1 Result To Close

GPT Pro R1 returned:

- `SCORE: 8/10`
- `VERDICT: REVISE`
- `VISUAL REVIEW: REVISE`

R1 findings to verify are fixed:

1. High: document-tree access loss while opening manifest/content/image streams was swallowed as a package download failure. R2 should clear the selected folder grant and return to select-folder state.
2. Low visual: document-tree connected state still looked like Drive. R2 should show `Folder`.
3. Low visual: access-lost state said `Connect the folder again`. R2 should say `Choose the folder again`.
4. Bundle gap: visual evidence opened the system picker but did not prove a real folder selection callback. R2 visual test now selects the `Documents` folder through Android DocumentsUI, returns to the app through ActivityResult, and captures connected state.
5. Bundle gap: Portable Profile privacy proof was incomplete. R2 includes exporter source and a direct test that a raw `content://.../tree/...` Agent Inbox URI is omitted from exported JSON.

## Acceptance Contract

Pass only if all are true:

- Disconnected Agent Inbox UX has a `Choose folder` action, not a pasted Drive folder URL/id field.
- The action launches Android `OpenDocumentTree` / DocumentsUI.
- A selected tree URI is persisted as a distinct `document_tree_folder` grant mode.
- Scan/import under document-tree grants do not require a Google OAuth access token.
- Revoked/unavailable tree access during scan or import clears the folder grant, clears stale candidates, records privacy-safe access-lost analytics, and asks the user to choose the folder again.
- The scan remains finite and bounded: direct child folders only, existing package/file/content/image limits retained.
- Historical Google Drive API grant paths may remain for compatibility, but they must not be the primary disconnected UX.
- Remote-safe analytics and Portable Profile exports do not leak raw `content://tree/...` URIs, Drive ids, file ids, package ids, package paths, content file names, or raw failure text.
- Existing Agent Inbox sidecar image safety and import behavior remain intact.
- Visual evidence is sufficient for `VISUAL REVIEW: PASS`.

## Evidence To Inspect First

- `VALIDATION_SUMMARY.md`
- `docs/SPRINT_29_AGENT_INBOX_FOLDER_SELECTOR.md`
- `GPT_PRO_REVIEW_R1.md`
- `sprint29_selector_r2_code_docs.diff`
- `visual_e2e_selector_r2/contact_sheet_selector_r2.png`
- `visual_e2e_selector_r2/TEST-sprint29-selector-visual-r2.xml`
- `logs/full_connected_debug_android_test_r2.log`
- `logs/full_local_gate_r2.log`

Then inspect the included source files as needed.

## Required Output Format

Start your answer with exactly:

```text
SCORE: <0-10>/10
VERDICT: PASS | REVISE | BLOCK
VISUAL REVIEW: PASS | REVISE | BLOCK
```

Then include:

- `FRESH FINDINGS`: only actionable issues, ordered by severity, with file/line references where possible.
- `R1 CLOSURE`: say whether each R1 finding is closed.
- `BUNDLE GAPS`: missing evidence or source that prevents confidence.
- `RELEASE READINESS`: whether this can proceed as a completed sprint after review.

If there are no fresh findings and evidence is sufficient, give `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS`.
