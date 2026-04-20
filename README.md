# Quality Alternative

Android-first MVP for replacing low-intention social app opens with one high-quality long-form alternative at the moment of impulse.

## Tester Quick Start

This repo publishes the internal alpha APK through GitHub Releases.

Polish step-by-step tester instructions are in [`TESTER_README.md`](TESTER_README.md).

For testers, the shortest path is:

1. Download the latest APK from [GitHub Releases](https://github.com/dkacz/qualityalternative/releases/latest).
2. Install it on an Android phone.
3. Open `Quality Alternative` and complete onboarding.
4. Enable the app in Android Accessibility settings when prompted.
5. Select distracting apps such as YouTube, X, Facebook, Instagram, Reddit, or TikTok.
6. Open one selected app and verify that the intervention appears.
7. Test `Read now`, `Open anyway`, and `Delay for 15 minutes`.

Important: this is a debug/internal alpha build. It is Android-only, requires Accessibility permission for interception, and is not a Play Store production release.

## Repository Status

This repository now contains:

- the frozen product definition and working rules
- a bootstrapped Android app in `Kotlin + Jetpack Compose`
- a working Sprint 0 foundation
- a first local replacement-loop prototype for Sprint 1, triggered manually from inside the app
- a completed Sprint 2 local-state slice with onboarding, persisted delay logic, history, and readiness signaling
- a completed Sprint 3 Android interception alpha slice with live system-intent intervention, fixture distractor apps, and cross-app automation coverage

## Source of Truth

1. `PRD.md`
   The execution-level source of truth for MVP scope, requirements, constraints, metrics, and launch decisions.
2. `PRODUCT_MEMO.md`
   Strategic context and positioning rationale. Use it to understand the thesis, not to expand scope.
3. `AGENTS.md`
   Repository working rules for human and AI contributors.

## Current Product Decision

- Build Android first.
- Focus on soft intervention by default.
- Show one primary recommendation, two backup options, and a conscious override path.
- Treat the replacement engine as the core moat.
- Keep the intervention finite and non-feed-like.

## Current Implementation

- `app/` contains the Android client prototype.
- The current build is local-first and uses editorial starter packs from app assets.
- User settings and delay windows are persisted with `DataStore`.
- Analytics and replacement history are persisted with `Room`.
- The prototype already supports:
  - onboarding with selected distracting apps, preferred topics, session length, and starter packs
  - manual debug intervention trigger
  - one primary recommendation plus two backups
  - reader flow
  - optional feedback flow
  - local analytics ledger
  - recent replacement history for the last 7 days
  - live accessibility-driven interception readiness guidance
  - persisted onboarding and target-app preferences after restart
  - persisted delay windows across sessions
  - live system-interception routing from the accessibility service into the in-app intervention surface
  - internal fixture distractor apps for cross-app automation tests
  - cross-app instrumentation coverage for fixture-to-intervention transitions
  - internal alpha readiness docs in `docs/INTERNAL_ALPHA_CHECKLIST.md` and `docs/REAL_DEVICE_SMOKE.md`

## Working Approach

- Build in small vertical slices.
- Use the PRD acceptance criteria as the default implementation contract.
- Instrument analytics from day one.
- Prefer bounded systems and simple ranking over premature complexity.

## Development Commands

- Unit tests:
  - `./gradlew testDebugUnitTest`
- Instrumentation smoke test:
  - `./gradlew connectedDebugAndroidTest`
- Android lint:
  - `./gradlew lintDebug`

Use `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home` and `ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools` when running the project from the CLI on this machine.

## Alpha Status

- Sprint 0-3 implementation is complete on Android.
- Emulator validation is green for unit, lint, and instrumentation suites.
- Internal alpha has been validated on a real Samsung `SM-S721B`.
- Manual smoke passed for YouTube, X, and Facebook, including real interception, `Open anyway`, and `Delay for 15 minutes`.
