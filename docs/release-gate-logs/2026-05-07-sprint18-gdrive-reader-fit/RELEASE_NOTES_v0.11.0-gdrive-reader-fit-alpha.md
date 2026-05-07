# Quality Alternative v0.11.0-gdrive-reader-fit-alpha

This release closes Sprint 18: live Google Drive annotation sync, accountless onboarding cleanup, annotation selection regression fixes, and measured reader bottom-fit pagination.

## What Changed

- Made Google Drive annotation sync authorize and write end to end for the configured debug/tester flow.
- Published and verified the debug OAuth client with the Drive file scope, package, and SHA-1 used by this APK.
- Kept `Connect` as a Google OAuth-only action; cancelled or blocked authorization no longer falls through to the Android folder picker.
- Kept the local annotation destination under `Change destination`, separate from Drive authorization.
- Removed the misleading `I have an account` onboarding shortcut while the product has no account system.
- Fixed reader source block indexing so annotation start selection can move backward across source/page boundaries while preserving the end.
- Updated the annotation editor test flow for compact range controls: expand the end, move the end back, then save the note.
- Switched reader pagination to measured Compose text height after viewport layout, with a footer-safe bottom guard so the last visible line is not hidden.
- Added Sprint 18 evidence for live Drive readback, rclone JSON-LD verification, reader bottom-fit screenshots, and GPT Pro R3 `10/10 PASS/PASS`.

## Validation

- GPT Pro Sprint 18 R3: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`
- Unit tests: PASS
- Connected Android tests: 105/105 PASS on `qaApi36(AVD) - 16`
- APK build: PASS
- APK signature verification: PASS, Android Debug certificate, v2 signature
- Emulator install smoke: PASS

## APK

- File: `quality-alternative-v0.11.0-gdrive-reader-fit-alpha-debug.apk`
- Package: `com.qualityalternative.app`
- Version code: `16`
- Version name: `0.11.0-alpha`
- SHA-256: `879b1c89c7dbb825c738913b98f0a19efeab8899c21559b3657ebec3b4c5ba2d`

