# Quality Alternative

Android-first MVP for replacing low-intention social app opens with one high-quality long-form alternative at the moment of impulse.

## Repository Status

This repository now contains:

- the frozen product definition and working rules
- a bootstrapped Android app in `Kotlin + Jetpack Compose`
- a working Sprint 0 foundation
- a first local replacement-loop prototype for Sprint 1, triggered manually from inside the app

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
- The prototype already supports:
  - manual debug intervention trigger
  - one primary recommendation plus two backups
  - reader flow
  - feedback flow
  - local analytics ledger
- System interception, persistence, onboarding, and fixture distractor apps are still future slices.

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

## Near-Term Next Step

Move from the manual prototype loop to persisted user state: onboarding, local settings storage, delay logic, and history.
