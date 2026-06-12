# v0.11.15 Agent Content Inbox Alpha

This alpha adds a user-reviewed Agent Content Inbox backed by an explicit Google Drive folder. Codex, Claude Code, or another local agent can place a bounded package with `manifest.json` plus one private Markdown or EPUB file into the folder, and the app can scan, validate, review, and import it through the existing private document model.

## Highlights

- Added Google Drive Agent Inbox connection, scan, disconnect, finite review candidates, validation errors, duplicates, and local remove/reject actions.
- Added the package manifest contract with `USER_PRIVATE` rights, Markdown/EPUB format checks, optional SHA-256 verification, and operator-reviewed `high` priority intent.
- Imported accepted packages through existing user-document storage, reader, ranking, progress, and analytics paths.
- Kept priority opt-in explicit: a manifest can request high priority, but the user must accept it before import.
- Hardened duplicate and storage behavior with reviewed-content fingerprints, atomic add-if-absent, content-addressed local storage, temp-file SHA verification, and cleanup on rejected or duplicate post-write results.
- Preserved privacy boundaries for Portable Profile and remote-safe analytics: no Drive ids, raw file names, document text, tokens, or raw private document fingerprints are exported.
- Added visual E2E coverage for review states, priority acceptance, duplicate/invalid/rejected packages, imported Markdown/EPUB consumption, and Portable Profile privacy copy.

## Validation

- GPT Pro R10: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`.
- Final Gradle gate: `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug` PASS.
- Final connected Android gate: `connectedDebugAndroidTest` PASS, 137 tests, 0 failures.
- APK badging: `versionCode=31`, `versionName=0.11.15-alpha`.
- APK signature verification: PASS.
- APK install and launch evidence: PASS.

## Artifact

- APK: `quality-alternative-v0.11.15-agent-content-inbox-alpha-debug.apk`
- SHA-256: `10f2d54f7dc06c561afa32a83bcc7c5790c211f17cd320d469d93e6c957278f6`
