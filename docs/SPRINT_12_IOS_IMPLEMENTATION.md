# Sprint 12 iOS Implementation

Status: `slice_12_1_gpt_pro_review_pending`
Branch: `codex/sprint12-ios-implementation`

## Goal

Build an iOS implementation that preserves the Android product flow and visual language while accepting iOS system constraints. The target is not a literal Android overlay clone. The target is an iOS-native Screen Time implementation with the same replacement-first user promise:

1. The user attempts to open a distracting app.
2. Quality Alternative presents one primary replacement and up to two backups.
3. The user can choose the replacement, pause, or intentionally continue.
4. Progress is based on completed replacements.

## Slice Gate

Each slice must pass:

- implementation
- right-sized automated tests
- simulator visual QA where available
- physical-device validation for Screen Time slices
- GPT Pro review
- concrete fix iteration until `10/10 PASS`

Heartbeat automations should be created only while waiting for the current GPT Pro lane, not while active implementation is underway.

## Slice 12.1: iOS Skeleton and Visual Parity Foundation

Scope:

- Add a native SwiftUI iOS project under `ios/`.
- Reuse the Android visual language: parchment/dark palette, rounded cards, serif display typography, readable replacement surfaces.
- Provide mock parity screens for home, library, intervention, reader, link handoff, meditation, progress, and settings.
- Add unit tests for finite replacement assumptions and mixed render modes.
- Add UI screenshot tests for Light and Dark visual QA.

Out of scope:

- No FamilyControls, DeviceActivity, ManagedSettings, or Shield extensions yet.
- No real content repository sharing yet.
- No signing/release artifact yet.

## Environment

Installed locally:

- Xcode 26.1.1 at `/Applications/Xcode-26.1.1.app`
- iOS 26.1 SDK
- iOS 26.1 Simulator runtime
- `QA iPhone 16 Sprint12` simulator

Use `DEVELOPER_DIR=/Applications/Xcode-26.1.1.app/Contents/Developer` when invoking Xcode tools unless the global developer directory is switched manually.

## Slice 12.1 Validation

Completed on 2026-04-24:

- `git diff --check`: PASS
- `xcodebuild build`: PASS
- `xcodebuild test`: PASS
- Unit tests: 3 passed, 0 failed
- UI visual QA tests: 1 passed, 0 failed
- Manual simulator screenshot pass: PASS on `QA iPhone 16 Sprint12`

Visual QA artifacts:

- Contact sheet: `docs/visual-qa/sprint12-ios-slice12-1/contact_sheet.png`
- Light screenshots: home, library, intervention, reader, handoff, meditation, progress, settings
- Dark screenshots: intervention, reader, meditation

GPT Pro review:

- Lane: `https://chatgpt.com/c/69eb3a1e-5f60-8386-bf9a-7588993d741e`
- Prompt: `PRO_REVIEW_PROMPT_20260424_113738.md`
- Bundle: `QUALITY_ALTERNATIVE_IOS_REVIEW_BUNDLE_20260424_113738.zip`
- Expected harvest path: `PRO_REVIEW_OUTPUT_20260424_113738/`

Observed scope state:

- The app is a SwiftUI visual/flow skeleton only.
- The intervention screen intentionally mirrors Android's finite shape: one primary replacement plus two backups.
- The reader, external handoff, meditation, and progress screens use local sample state only.
- Screen Time permission flows, app selection, shielding, real replacement persistence, and signing are left for later slices.
