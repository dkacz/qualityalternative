# Internal Alpha Checklist

Status: `internal alpha validated on real device`

This checklist tracks the Android-first internal alpha defined in Sprint 0-3.

## Automated Validation

- [x] `./gradlew testDebugUnitTest --no-daemon`
- [x] `./gradlew lintDebug --no-daemon`
- [x] `./gradlew connectedDebugAndroidTest --no-daemon`
- [x] Cross-app fixture automation reaches the intervention surface
- [x] Delay flow and `Open anyway` behavior are covered by automated tests
- [x] Persistence, analytics, and history survive restart paths covered in tests

## Alpha Product Surface

- [x] Onboarding persists selected apps, topics, duration, and starter packs
- [x] Intervention shows one primary replacement and two backups
- [x] Reader and feedback flows are wired to analytics
- [x] Pause 15 min works in the real intervention flow
- [x] `Open anyway` returns the user to the target-app path without looping
- [x] Accessibility readiness guidance is visible in the app
- [x] Fixture distractor apps exist for repeatable cross-app automation

## Operational Readiness

- [x] Repo docs reflect the current Sprint 3 state
- [x] Real-device smoke instructions are documented in `docs/REAL_DEVICE_SMOKE.md`
- [x] Internal alpha can be exercised on emulator for engineering validation
- [x] Manual smoke test completed on one real Android device
- [x] Manual interception confirmed on two real apps from the launch support list

## Notes

- Real-device smoke passed on a Samsung `SM-S721B` for YouTube, X, and Facebook.
- Confirmed on device: real-app interception, `Open anyway` without immediate loop, and `Pause 15 min`.
- Sprint 0-3 internal alpha is validated for the current Android-first scope.
