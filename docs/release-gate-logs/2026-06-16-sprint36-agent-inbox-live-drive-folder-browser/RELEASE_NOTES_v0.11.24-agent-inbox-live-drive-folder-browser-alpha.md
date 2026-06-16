# v0.11.24 Agent Inbox Live Drive Folder Browser Alpha

This release repairs Agent Inbox Google Drive folder selection after the previous picker/link flows failed on real device use. `Choose folder` now opens an in-app Drive folder browser backed by `drive.readonly`, lets the user select a real Drive folder, scans the selected folder, and imports complete Agent Inbox packages created outside the app.

Key changes:

- Added Drive API folder listing for Agent Inbox folder browsing.
- Added an in-app Drive folder browser with `Open`, `Select`, back, loading, and error states.
- Persisted selected Drive folders as readonly Agent Inbox folder grants and scan immediately after selection.
- Routed legacy insufficient picker grants back through the folder browser repair path.
- Kept pasted Drive link/id as a fallback, not the primary path.
- Added a permanent live review gate requiring signed-in emulator/device evidence before future Agent Inbox Drive releases.

Release evidence:

- GPT Pro R2: `SCORE: 10/10`, `VERDICT: PASS`, `BLOCKERS: None`.
- Live evidence: signed-in emulator `emulator-5554`, Google account `omareth@gmail.com`, real Drive folder `QA-Agent-Inbox-Live-E2E-20260616-173729`, scan/import/Library/reader proof.
- Release gate: `docs/release-gate-logs/2026-06-16-sprint36-agent-inbox-live-drive-folder-browser/VALIDATION_SUMMARY.md`.

Artifacts:

- Debug APK: `release_artifacts/quality-alternative-v0.11.24-agent-inbox-live-drive-folder-browser-alpha-debug.apk`
- Debug SHA-256: `96fc0011e3ce192897da4750d83497244fa97fcc4c924e9b49191199d6dddb54`
- Release unsigned APK: `release_artifacts/quality-alternative-v0.11.24-agent-inbox-live-drive-folder-browser-alpha-release-unsigned.apk`
- Release unsigned SHA-256: `0054437d86962e7abba49391dd7a514894f404ed0720d8e92e1a995a85ec66a9`
