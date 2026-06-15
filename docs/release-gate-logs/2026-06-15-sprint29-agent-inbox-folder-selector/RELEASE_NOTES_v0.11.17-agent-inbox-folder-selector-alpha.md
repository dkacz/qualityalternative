# v0.11.17 Agent Inbox Folder Selector Alpha

This alpha removes the pasted-folder fallback from the primary Agent Inbox connection flow. The app now uses Android's normal folder picker, persists the selected folder tree grant, and scans only that selected folder.

## Highlights

- Replaced the disconnected Agent Inbox pasted Drive folder URL/id field with a `Choose folder` action.
- Added Android `OpenDocumentTree` support so the user can grant the app access to a Drive-backed folder through the system picker.
- Added document-tree Agent Inbox scanning/importing without requiring a Google OAuth token for the selected folder.
- Preserved historical Drive API folder compatibility for already-connected states while making the folder selector the primary UX.
- Added access-loss handling that clears the stale folder grant and returns the user to the select-folder state instead of showing a false empty inbox.
- Kept privacy boundaries: raw `content://tree/...` folder URIs, Drive ids, file ids, package paths, and content filenames stay out of Portable Profile and remote-safe analytics.

## Validation

- GPT Pro R2: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`.
- Full connected Android gate before release: `connectedDebugAndroidTest` PASS, 138 tests, 0 failures.
- Final release-gate validation is recorded in this folder's `VALIDATION_SUMMARY.md`.

## Artifact

- APK: `quality-alternative-v0.11.17-agent-inbox-folder-selector-alpha-debug.apk`
