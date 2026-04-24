# Sprint 12 iOS Implementation

Status: `slice_12_3_followup_pro_review_pending`
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
- Unit tests: 4 passed, 0 failed
- UI visual QA tests: 1 passed, 0 failed
- Manual simulator screenshot pass: PASS on `QA iPhone 16 Sprint12`
- Follow-up validation bundle: `output/ios_sprint12_slice12_1_fix_validation_20260424_120330/QualityAlternative.xcresult`
- Follow-up validation log: `output/ios_sprint12_slice12_1_fix_validation_20260424_120330/xcodebuild_test.log`

Visual QA artifacts:

- Contact sheet: `docs/visual-qa/sprint12-ios-slice12-1/contact_sheet.png`
- Light screenshots: home, library, intervention, reader, handoff, meditation, progress, settings
- Dark screenshots: intervention, reader, meditation

GPT Pro review:

- Lane: `https://chatgpt.com/c/69eb3a1e-5f60-8386-bf9a-7588993d741e`
- Harvested verdict: `REVISE`
- Prompt: `PRO_REVIEW_PROMPT_20260424_113738.md`
- Bundle: `QUALITY_ALTERNATIVE_IOS_REVIEW_BUNDLE_20260424_113738.zip`
- Expected harvest path: `PRO_REVIEW_OUTPUT_20260424_113738/`

GPT Pro follow-up review:

- Lane: `https://chatgpt.com/c/69eb4181-a094-8387-b7f6-b1ea3caabb71`
- Harvested verdict: `10/10 PASS`
- Prompt: `PRO_REVIEW_PROMPT_20260424_120841.md`
- Bundle: `QUALITY_ALTERNATIVE_IOS_REVIEW_BUNDLE_20260424_120841.zip`
- Harvest path: `PRO_REVIEW_OUTPUT_20260424_120841/`

Follow-up fixes applied:

- Added an explicit "Continue intentionally" intervention action for the iOS-safe conscious-continuation path.
- Render backup actions from `QAReplacementSession.backups` instead of hard-coded buttons.
- Capped `QAReplacementSession.backups` to two items in the model.
- Added unit coverage for the two-backup cap.
- Added UI assertions for primary, two backups, pause, continue, and absence of a third backup.
- Moved screen accessibility markers off the root container so action identifiers remain queryable.
- Regenerated the Slice 12.1 screenshot set under `docs/visual-qa/sprint12-ios-slice12-1/` so bundle paths match the documented paths.

Observed scope state:

- The app is a SwiftUI visual/flow skeleton only.
- The intervention screen intentionally mirrors Android's finite shape: one primary replacement plus two backups.
- The reader, external handoff, meditation, and progress screens use local sample state only.
- Screen Time permission flows, app selection, shielding, real replacement persistence, and signing are left for later slices.

## Slice 12.2: Screen Time Setup and Protected Selection

Scope:

- Add host-app Screen Time setup UI in Settings.
- Request FamilyControls authorization for the individual user through public APIs.
- Present Apple's FamilyActivityPicker for protected app/category/site selection.
- Persist the opaque FamilyActivitySelection locally so selection can survive app restart.
- Show only token counts and setup readiness; do not expose or fake readable app identities.
- Add tests for setup readiness and visual assertions for the Settings setup surface.

Out of scope:

- No DeviceActivity monitor extension yet.
- No ManagedSettings shield application yet.
- No ManagedSettingsUI shield configuration yet.
- No ShieldActionDelegate routing yet.
- No claim that simulator validates real Screen Time behavior.

Validation notes:

- Simulator build/test can verify host-app UI, persistence code, and picker wiring.
- Physical-device validation remains required before any PASS claim about actual authorization dialogs, app selection behavior, shielding, shield actions, pause, or open-anyway behavior.
- The app target now includes `com.apple.developer.family-controls=true` in `ios/QualityAlternative/QualityAlternative.entitlements`; Apple Developer entitlement approval/signing remains a later physical-device and distribution gate.

Validation completed on 2026-04-24:

- `git diff --check`: PASS
- `xcodebuild test`: PASS on `QA iPhone 16 Sprint12`
- Unit tests: 5 passed, 0 failed
- UI visual QA tests: 1 passed, 0 failed
- Total tests: 6 passed, 0 failed
- Initial result bundle: `output/ios_sprint12_slice12_2_validation_20260424_124050/QualityAlternative.xcresult`
- Review-fix result bundle: `output/ios_sprint12_slice12_2_review_fix_validation_20260424_130205/QualityAlternative.xcresult`
- Review-fix test summary: `output/ios_sprint12_slice12_2_review_fix_validation_20260424_130205/test_summary.json`

Visual QA artifacts:

- Contact sheet: `docs/visual-qa/sprint12-ios-slice12-2/contact_sheet.png`
- Light screenshots: home, library, intervention, reader, handoff, meditation, progress, settings with Screen Time setup
- Dark screenshots: intervention, reader, meditation

Implemented state:

- Settings has a Screen Time setup card with authorization status, opaque protected-selection summary, authorization request action, and FamilyActivityPicker launch action.
- `FamilyActivitySelection` is loaded/saved locally through JSON in `UserDefaults` for host-app continuity.
- The UI deliberately shows token counts and readiness only; it does not expose, infer, or fake readable app identities.
- No Screen Time shielding, DeviceActivity monitoring, ManagedSettings store, ManagedSettingsUI shield, or ShieldActionDelegate routing has been added in this slice.

GPT Pro review:

- Lane: `https://chatgpt.com/c/69eb4a61-a454-838b-bd7a-e8099482ad97`
- Harvested verdict: `REVISE`
- Prompt: `PRO_REVIEW_PROMPT_20260424_124547.md`
- Bundle: `QUALITY_ALTERNATIVE_IOS_REVIEW_BUNDLE_20260424_124547.zip`
- Harvest path: `PRO_REVIEW_OUTPUT_20260424_124547/`

Review fixes applied:

- Removed hidden `Color.clear` accessibility marker views from the Screen Time setup card so visual layout and accessibility traversal are not affected by test-only nodes.
- Updated the visual QA test to assert visible user-facing Settings controls directly instead of hidden duplicate markers.
- Added `CODE_SIGN_ENTITLEMENTS: QualityAlternative/QualityAlternative.entitlements` to `ios/project.yml` so project regeneration preserves Family Controls entitlements.
- Added repo-root review prompts, Pro review outputs, and iOS review ZIP bundles to `.gitignore`; generated review packets remain local artifacts and are not committed.

GPT Pro follow-up review:

- Harvested verdict: `10/10 PASS`
- Lane: `https://chatgpt.com/c/69eb4ec3-449c-838b-b29b-8677b0a1deeb`
- Prompt: `PRO_REVIEW_PROMPT_20260424_130548.md`
- Bundle: `QUALITY_ALTERNATIVE_IOS_REVIEW_BUNDLE_20260424_130548.zip`
- Harvest path: `PRO_REVIEW_OUTPUT_20260424_130548/`

## Slice 12.3: Shared Shield State and Host Controls

Scope:

- Add an App Group-backed state model for the current iOS replacement/shield session.
- Add host-app controls that can prepare, pause, resume, and clear shield state from the Settings setup surface.
- Apply selected protected tokens to a `ManagedSettingsStore` only when authorization and at least one protected target are present.
- Keep state token-safe: store counts, selected replacement ids, pause expiry, and action mode, not readable protected-app names.
- Add unit tests for shield readiness, pause expiry, action state, and no-selection safety.
- Add simulator visual QA for the Settings shield-control surface.

Out of scope:

- No DeviceActivity monitor extension yet.
- No ManagedSettingsUI shield configuration extension yet.
- No ShieldActionDelegate routing yet.
- No physical-device PASS claim for actual shield display or Screen Time enforcement.
- No private API workaround for opening another app or bypassing Screen Time constraints.

Validation notes:

- Simulator validation covers host-app state, App Group persistence plumbing, ManagedSettings API wiring, and visual QA only.
- The shared state deliberately stores only generic trigger context, replacement ids, backup ids, token counts, action mode, pause expiry, and timestamps.
- Real shield display, Screen Time enforcement, app-opening behavior, pause expiry enforcement, and open-anyway behavior still require a signed physical-device pass.

Validation completed on 2026-04-24:

- `git diff --check`: PASS
- `plutil -lint ios/QualityAlternative/QualityAlternative.entitlements`: PASS
- `xcodebuild test`: PASS on `QA iPhone 16 Sprint12`
- Unit tests: 9 passed, 0 failed
- UI visual QA tests: 1 passed, 0 failed
- Total tests: 10 passed, 0 failed
- Result bundle: `output/ios_sprint12_slice12_3_validation_20260424_132000/QualityAlternative.xcresult`
- Test summary: `output/ios_sprint12_slice12_3_validation_20260424_132000/test_summary.json`
- Follow-up result bundle: `output/ios_sprint12_slice12_3_followup_validation_20260424_135000/QualityAlternative.xcresult`
- Follow-up test summary: `output/ios_sprint12_slice12_3_followup_validation_20260424_135000/test_summary.json`
- Follow-up tests: 11 passed, 0 failed

Visual QA artifacts:

- Contact sheet: `docs/visual-qa/sprint12-ios-slice12-3/contact_sheet.png`
- Light screenshots: home, library, intervention, reader, handoff, meditation, progress, settings with Screen Time setup and shield controls
- Dark screenshots: intervention, reader, meditation

Implemented state:

- App and future extensions share the App Group id `group.com.qualityalternative.ios`.
- `QAFamilyActivitySelectionStore` now persists protected selection through the App Group-backed `UserDefaults` fallback.
- `QAShieldSessionState` stores a token-safe current shield session for App Group sharing.
- `QAManagedSettingsShieldApplier` applies selected app/category/domain tokens to `ManagedSettingsStore` only when setup is ready.
- Settings exposes shield preparation and state controls without claiming simulator enforcement.

GPT Pro review:

- Harvested verdict: `REVISE`
- Lane: `https://chatgpt.com/c/69eb55b3-0bb8-838e-a4c3-0bc034486057`
- Prompt: `PRO_REVIEW_PROMPT_20260424_133526.md`
- Bundle: `QUALITY_ALTERNATIVE_IOS_REVIEW_BUNDLE_20260424_133526.zip`
- Harvest path: `PRO_REVIEW_OUTPUT_20260424_133526/`

Review fixes applied:

- Removed the silent App Group fallback to `.standard`; App Group-backed stores now fail closed with observable logging when shared defaults are unavailable.
- Added unit coverage for nil shared-defaults behavior so stores do not mask App Group availability failures.
- Removed stale generated `ios/output/` artifacts that could pollute review packets with failed validation logs.
- Regenerated follow-up validation evidence and Slice 12.3 screenshots from the latest successful simulator test run.

GPT Pro follow-up review:

- Status: pending
