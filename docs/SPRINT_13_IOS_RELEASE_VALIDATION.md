# Sprint 13 iOS Release Validation

Status: `slice_13_1_blocked_by_signing_identity_and_physical_device`
Branch: `codex/sprint13-ios-physical-release`

## Goal

Turn the Pro-reviewed Sprint 12 simulator implementation into a signed iOS release candidate, then validate the actual Screen Time, DeviceActivity, ManagedSettings shield, and extension behavior on a physical iPhone.

This sprint must not claim release readiness until signed physical-device validation passes. Simulator success remains useful compile/state/visual evidence only.

## Slice 13.1: Signing and Device Release Preflight

Scope:

- Confirm the iOS codebase builds for the real `iphoneos` SDK in Release configuration.
- Identify signing/provisioning blockers separately from code/build blockers.
- Record the bundle identifiers, entitlements, extension points, and physical-device validation checklist needed for the signed release candidate.
- Keep the iOS-native scope: public Apple Screen Time APIs only, no private API foreground detection, no Android overlay parity promise, and finite replacement behavior preserved.

Out of scope:

- No TestFlight, ad-hoc, or App Store release artifact until a valid signing identity, provisioning, and physical device are available.
- No PASS claim for real Screen Time authorization, app selection, DeviceActivity callbacks, ManagedSettings shield display, shield actions, or App Group behavior until tested on device.

## Preflight Result

Completed on 2026-04-24 with Xcode 26.1.1 build 17B100.

- Branch: `codex/sprint13-ios-physical-release`
- Base Sprint 12 status: slices 12.1 through 12.5 have GPT Pro `10/10 PASS`
- Physical devices visible to Xcode: none
- Valid code signing identities: none
- Provisioning profiles found locally: none
- Unsigned Release `iphoneos` build: PASS
- Signed Release `iphoneos` build: BLOCKED
- Blocker: all four shipped iOS targets require a development team before signing can proceed

Evidence:

- Environment and signing preflight: `output/ios_sprint13_slice13_1_release_preflight_20260424_171000/preflight_environment.log`
- Release build settings: `output/ios_sprint13_slice13_1_release_preflight_20260424_171000/release_build_settings.log`
- Unsigned Release build log: `output/ios_sprint13_slice13_1_release_preflight_20260424_171000/xcodebuild_release_unsigned.log`
- Signed Release attempt log: `output/ios_sprint13_slice13_1_release_preflight_20260424_171000/xcodebuild_release_signed_attempt.log`
- Signed Release attempt exit code: `output/ios_sprint13_slice13_1_release_preflight_20260424_171000/xcodebuild_release_signed_attempt.exitcode`
- Unsigned product path: `output/ios_sprint13_slice13_1_release_preflight_20260424_171000/DerivedData/Build/Products/Release-iphoneos/QualityAlternative.app`

The unsigned product contains the host app and all three embedded extension bundles:

- `QualityAlternative.app`
- `QualityAlternative.app/PlugIns/QualityAlternativeDeviceActivityMonitor.appex`
- `QualityAlternative.app/PlugIns/QualityAlternativeShieldAction.appex`
- `QualityAlternative.app/PlugIns/QualityAlternativeShieldConfiguration.appex`

The signed attempt failed before compilation/signing work with development-team errors for:

- `QualityAlternative`
- `QualityAlternativeDeviceActivityMonitor`
- `QualityAlternativeShieldAction`
- `QualityAlternativeShieldConfiguration`

## Signing Inputs Required

Apple Developer account setup needs to provide all of the following before a real release artifact can be produced:

- A valid Apple Development or Apple Distribution signing certificate installed in the login keychain.
- A `DEVELOPMENT_TEAM` value for all iOS targets.
- App ID / bundle ID registration for:
- `com.qualityalternative.ios`
- `com.qualityalternative.ios.deviceactivitymonitor`
- `com.qualityalternative.ios.shieldaction`
- `com.qualityalternative.ios.shieldconfiguration`
- App Group registration for `group.com.qualityalternative.ios`.
- Family Controls entitlement approval for the app and extension targets that use Screen Time APIs.
- Provisioning profiles covering the host app and all embedded extensions with the required App Group and Family Controls entitlements.
- A physical iPhone visible in Xcode for install and real Screen Time validation.

## Target Capability Map

Host app:

- Bundle ID: `com.qualityalternative.ios`
- Entitlements: Family Controls, App Group `group.com.qualityalternative.ios`
- Purpose: SwiftUI host app, replacement surfaces, Screen Time setup, shield/session state controls, DeviceActivity schedule controls.

DeviceActivity monitor extension:

- Bundle ID: `com.qualityalternative.ios.deviceactivitymonitor`
- Extension point: `com.apple.deviceactivity.monitor-extension`
- Entitlements: Family Controls, App Group `group.com.qualityalternative.ios`
- Purpose: receive DeviceActivity callbacks and apply/reapply finite shield state from token-safe shared state.

Shield action extension:

- Bundle ID: `com.qualityalternative.ios.shieldaction`
- Extension point: `com.apple.ManagedSettings.shield-action-service`
- Entitlements: Family Controls, App Group `group.com.qualityalternative.ios`
- Purpose: handle primary replacement intent and pause action from Apple shield UI without claiming direct host-app launch.

Shield configuration extension:

- Bundle ID: `com.qualityalternative.ios.shieldconfiguration`
- Extension point: `com.apple.ManagedSettingsUI.shield-configuration-service`
- Entitlements: Family Controls, App Group `group.com.qualityalternative.ios`
- Purpose: render token-safe shield copy using generic protected-selection wording.

## Physical-Device Validation Checklist

Run this only after signing/provisioning succeeds and the app installs on an iPhone.

1. Install the signed app on the device.
2. Launch the app and confirm the home, library, intervention, reader, meditation, progress, and settings screens render with the expected visual language.
3. Grant Screen Time / Family Controls authorization from the host app.
4. Select at least one distracting app in the FamilyActivityPicker.
5. Confirm Settings changes from `Needs setup` to ready states for Screen Time, shield controls, and DeviceActivity monitor.
6. Apply shield rules from Settings.
7. Open the selected distracting app and verify the Apple shield appears with Quality Alternative copy.
8. Tap the primary replacement action and confirm the app queues/routes to a finite replacement intervention without exposing readable protected-app identities.
9. Tap the pause action and confirm shield clearing is temporary and tracked as a pause, not a permanent bypass.
10. Trigger or wait for the DeviceActivity protected window and confirm monitor callbacks reapply the shield according to shared state.
11. Use open-anyway and confirm it is finite: one-shot or bounded by expiry, then re-arms/reapplies on a later callback.
12. Reboot or relaunch the app and confirm App Group state remains token-safe and recoverable.

Required evidence for PASS:

- Device model, iOS version, app version/build, signing team, provisioning profile type, and install method.
- Screenshots or screen recording of setup, shield display, replacement action, pause, DeviceActivity monitor state, and post-open-anyway rearm.
- Logs proving whether DeviceActivity callbacks fired on-device.
- Explicit PASS/FAIL table for every checklist item above.

## Next Slice Gate

Slice 13.2 can start only after the signing inputs are available. The expected first action is to set `DEVELOPMENT_TEAM` for all four iOS targets, build a signed Release archive, install it on a physical iPhone, and execute the physical-device checklist.
