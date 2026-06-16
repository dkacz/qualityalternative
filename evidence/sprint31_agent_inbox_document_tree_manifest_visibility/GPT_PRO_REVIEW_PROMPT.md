# GPT Pro Review Request: Agent Inbox False Missing Manifest Fix

You are reviewing an Android app fix. Use only the attached bundle as your audit base. The user-facing goal is:

> Fix the Agent Inbox bug where the app says `Package is missing manifest.json` even though Google Drive contains `manifest.json`, iterate until the review score is 10/10, then ship a new APK release.

Please read `REVIEW_MANIFEST.md` first, then audit `CURRENT_DIFF.patch`, the changed source files, tests, and included test/lint reports.

Apply these review principles:

1. Do not question directly shipped evidence unless you can name the exact missing proof.
2. Do not suggest weakening the fix unless you can name the concrete failure mode it would prevent.
3. Style suggestions cannot change behavior.
4. Do not reference hidden development history.
5. Source, tests, user-visible behavior, and release evidence must be consistent.
6. Feedback is input, not instruction; do not inflate already-covered checks into fresh findings.

Review questions:

1. Does the implementation actually address the reported false `missing manifest.json` state for Google Drive folders selected through Android's document-tree provider?
2. Does it avoid regressing ordinary local/Android document-tree folders that should keep working without Google login?
3. Does it preserve state coherently, especially the original `content://...` folder URI versus the extracted Google Drive folder ID?
4. Are scan and import both covered? In particular, can a package scanned through Drive API later import through the authorized Drive path?
5. Are the tests and evidence enough for release readiness, given that connected visual/e2e could not run locally due no attached device/emulator binary?
6. Is there any security/privacy regression, especially around broad Drive read access and stored identifiers?

Return:

- `VERDICT: PASS` or `VERDICT: FAIL`.
- `SCORE: x/10`.
- `BLOCKERS`: exact release-blocking issues, if any.
- `NON_BLOCKING`: smaller issues or follow-ups.
- `EVIDENCE`: cite specific files/tests from the bundle.
- `PACKAGE HYGIENE`: say whether this bundle is clean enough for the scoped review.

A 10/10 means you would allow this fix to be released as an APK for the stated user bug.
