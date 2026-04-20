# Quality Alternative v0.1.0-alpha

## Release Status

Status: internal alpha validated on real Android device.

This release proves the Android-first MVP thesis: selected distracting apps can be intercepted at the moment of impulse and replaced with a finite, high-quality content alternative.

## What Works

- Android onboarding for selecting distracting apps, topics, preferred reading length, and starter packs.
- Accessibility-driven app interception for the launch support list.
- Soft intervention with one primary recommendation, two backups, `Open anyway`, and `Delay for 15 minutes`.
- Local in-app reader, feedback flow, replacement history, delay state, and analytics ledger.
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
- ChatGPT Pro review returned `VERDICT: PASS` for Sprint 3 completion.
- Real-device smoke passed on Samsung `SM-S721B` for YouTube, X, and Facebook.

## Known Limits

- This is a debug/internal alpha build, not a public distribution build.
- Real-device smoke has been performed on one Samsung Android device only.
- iOS is out of scope for this release.
- User-added links, PDFs, hosted analytics, premium hard-block mode, and sub-surface blocking are not included.
- Overlay permission remains optional and future-facing; Accessibility is the active interception path.
