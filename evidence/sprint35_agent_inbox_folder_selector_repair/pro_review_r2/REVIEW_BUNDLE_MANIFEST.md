# Sprint 35 GPT Pro R2 Review Bundle Manifest

## Scope

Agent Inbox folder selector repair for `v0.11.23-alpha`, revised after GPT Pro R1.

## Included

- R2 prompt and this manifest.
- R1 review output and R1 fix summary.
- Scoped Sprint 35 status excerpt, not the full historical lane tracker.
- Current diff and git status.
- Current source for Agent Inbox folder selection, authorization, AppContainer production wiring, Drive/document-tree clients, package review models/importer, package constants, annotation/Agent Inbox scope constants, settings persistence, and release version metadata.
- Current unit tests and visual E2E test source for the changed behavior.
- Portable package authoring instructions and package validator.
- Final release gate evidence:
  - forced Gradle build log with `104 actionable tasks: 104 executed`
  - unit-test XML reports
  - connected Sprint35 visual E2E XML/logcat
  - individual screenshots and contact sheet
  - APK SHA-256 hashes
  - APK badging output
  - debug APK install status, package dump, device listing, and direct launch output
  - release notes

## Excluded

- Full APK binaries, because the review question is source/routing/evidence audit and the shipped APK metadata, hashes, Gradle log, install output, and package dump cover the scoped binary claim without adding 65MB of attachments.
- Full `docs/LANE_STATUS.md`, because it contains historical Picker-era notes that R1 correctly flagged as noisy for a scoped Sprint 35 packet.
- Prior Sprint 34 review output, because R1 already established that it did not prove the current live-device behavior.
- Raw Google Drive/rclone listings, because no fresh live Drive inspection is claimed in this R2 packet.

## Key Files

- `app/src/main/java/com/qualityalternative/app/data/AppContainer.kt`
- `app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt`
- `app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt`
- `app/src/main/java/com/qualityalternative/app/ui/GoogleDriveAuthorization.kt`
- `app/src/main/java/com/qualityalternative/app/data/AndroidDocumentTreeAgentInboxClient.kt`
- `app/src/main/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClient.kt`
- `app/src/main/java/com/qualityalternative/app/domain/service/ReadingAnnotationDriveSync.kt`
- `app/src/main/java/com/qualityalternative/app/domain/service/AgentInboxDrive.kt`
- `app/src/main/java/com/qualityalternative/app/data/AgentInboxManifest.kt`
- `app/src/main/java/com/qualityalternative/app/data/AgentInboxDocumentStore.kt`
- `app/src/main/java/com/qualityalternative/app/data/AgentInboxPackageImporter.kt`
- `app/src/main/java/com/qualityalternative/app/data/AgentInboxReviewCandidate.kt`
- `app/src/main/java/com/qualityalternative/app/domain/model/ContentModels.kt`
- `app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt`
- `docs/AGENT_INBOX_PACKAGE_AUTHORING.md`
- `tools/validate_agent_inbox_package.py`
