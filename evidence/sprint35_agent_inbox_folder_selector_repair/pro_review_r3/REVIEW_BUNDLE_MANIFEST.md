# Sprint 35 GPT Pro R3 Review Bundle Manifest

## Scope

R3 verifies the single blocker from GPT Pro R2: legacy `picker_folder` must be repair-only below the UI, not just in the visible Compose click path.

## Included

- R3 prompt and this manifest.
- GPT Pro R2 output and R2 fix summary.
- Current diff and git status.
- Current source and tests for the R2 finding:
  - `MainViewModel.kt`
  - `QualityAlternativeApp.kt`
  - `PreferencesSettingsRepository.kt`
  - `AgentInboxDrive.kt`
  - `GoogleDriveAuthorization.kt`
  - `MainViewModelTest.kt`
  - `PreferencesSettingsRepositoryTest.kt`
  - `GoogleDriveAuthorizationUiTest.kt`
- Production wiring and client files previously accepted in R2, retained for regression context.
- Release evidence refreshed after the R2 fix:
  - sanitized final Gradle build log
  - unit XML reports
  - APK hashes and badging
  - install, package dump, launch, and device listing
  - connected visual E2E XML/logcat
  - updated screenshots and contact sheet

## Excluded

- Full APK binaries, for the same reason as R2: this is source/routing/evidence review, and the scoped binary claim is covered by metadata, hashes, install, package dump, and launch evidence.
- Full historical lane tracker and older Sprint reviews, because R3 is scoped to the R2 blocker.
- Raw Google Drive/rclone listings, because no fresh live Drive inspection is claimed.
