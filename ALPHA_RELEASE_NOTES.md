# Quality Alternative v0.4.0-documents-alpha

## Release Status

Status: internal documents alpha candidate.

This release keeps the Android-first interception thesis and adds the Sprint 9 private document layer: user-owned PDF, Markdown, and EPUB imports as finite replacement candidates.

## What Works

- Android onboarding for selecting distracting apps, topics, preferred reading length, and starter packs.
- Accessibility-driven app interception for the launch support list.
- Soft intervention with one primary recommendation, two backups, `Open anyway`, and `Pause 15 min`.
- Local in-app reader, feedback flow, replacement history, delay state, and analytics ledger.
- User-added links with local persistence, source-aware recommendation ranking, and a clear saved-link handoff before opening the browser.
- User-owned document import for PDF, Markdown, and EPUB.
- Markdown files open in the in-app private reader.
- PDF and EPUB files open through a clearly labeled Android external document-viewer handoff.
- Light and Dark analog themes with persisted theme choice.
- Restyled intervention, home, library, add-link, reader, handoff, feedback, and progress surfaces.
- Constructive progress framing around converted impulses and recent replacements, not shame-heavy streaks.
- Durable local state using `DataStore` and `Room`.
- Internal fixture apps and emulator instrumentation for repeatable cross-app validation.

## Supported Apps

- Instagram
- X
- YouTube
- Facebook
- Reddit
- TikTok

## Validation

- `testDebugUnitTest` passed.
- `lintDebug` passed.
- `connectedDebugAndroidTest` passed on `qaApi36`.
- ChatGPT Pro review is the release gate for the Sprint 9 documents alpha.
- Real-device smoke passed on Samsung `SM-S721B` for YouTube, X, and Facebook.

## Known Limits

- This is a debug/internal alpha build, not a public distribution build.
- Real-device smoke has been performed on one Samsung Android device only.
- iOS is out of scope for this release.
- Native PDF/EPUB rendering, hosted analytics, premium hard-block mode, and sub-surface blocking are not included.
- User-added links and user-owned documents are local/private; there is no article extraction, crawling, summarization, cloud sync, OCR, annotation, or document management.
- Overlay permission remains optional and future-facing; Accessibility is the active interception path.
