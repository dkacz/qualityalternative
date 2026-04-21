# Quality Alternative v0.2.0-ui-alpha

## Release Status

Status: internal UI alpha ready for tester distribution.

This release keeps the Android-first interception thesis from `v0.1.0-alpha` and adds the Sprint 4-5 product layer: user-added links plus a calmer analog UI pass across the core replacement loop.

## What Works

- Android onboarding for selecting distracting apps, topics, preferred reading length, and starter packs.
- Accessibility-driven app interception for the launch support list.
- Soft intervention with one primary recommendation, two backups, `Open anyway`, and `Pause 15 min`.
- Local in-app reader, feedback flow, replacement history, delay state, and analytics ledger.
- User-added links with local persistence, source-aware recommendation ranking, and a clear saved-link handoff before opening the browser.
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
- ChatGPT Pro review returned `VERDICT: PASS` for Sprint 3 completion, Sprint 4 user-added links, and Sprint 5 UI/UX slices.
- Real-device smoke passed on Samsung `SM-S721B` for YouTube, X, and Facebook.

## Known Limits

- This is a debug/internal alpha build, not a public distribution build.
- Real-device smoke has been performed on one Samsung Android device only.
- iOS is out of scope for this release.
- PDFs, hosted analytics, premium hard-block mode, and sub-surface blocking are not included.
- User-added links are simple local web links only; there is no article extraction, crawling, summarization, cloud sync, or PDF support yet.
- Overlay permission remains optional and future-facing; Accessibility is the active interception path.
