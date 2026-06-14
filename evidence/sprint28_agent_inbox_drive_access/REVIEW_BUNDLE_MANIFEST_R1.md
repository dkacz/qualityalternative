# Sprint 28 GPT Pro Review Bundle Manifest R1

Review lane: Agent Inbox Drive access fix under Google Drive `drive.file`.

Base commit: `0925266` (`Record Agent Inbox Drive access diagnosis`)

Included commit range: `0925266..HEAD` on branch `codex/sprint28-agent-inbox-drive-access`.

## Primary Document

- `docs/SPRINT_28_AGENT_INBOX_DRIVE_ACCESS.md`

## Included Source And Test Files

- `PRD.md`
- `docs/LANE_STATUS.md`
- `gradle/libs.versions.toml`
- `app/src/main/java/com/qualityalternative/app/ui/GoogleDriveAuthorization.kt`
- `app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt`
- `app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt`
- `app/src/main/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClient.kt`
- `app/src/main/java/com/qualityalternative/app/data/AccountLightProfile.kt`
- `app/src/main/java/com/qualityalternative/app/data/PreferencesSettingsRepository.kt`
- `app/src/main/java/com/qualityalternative/app/analytics/InMemoryAnalyticsTracker.kt`
- `app/src/main/java/com/qualityalternative/app/domain/service/AgentInboxDrive.kt`
- `app/src/main/java/com/qualityalternative/app/domain/service/Contracts.kt`
- `app/src/main/java/com/qualityalternative/app/domain/model/InterventionModels.kt`
- `app/src/main/java/com/qualityalternative/app/domain/model/AnalyticsPrivacyGuard.kt`
- `app/src/test/java/com/qualityalternative/app/ui/GoogleDriveAuthorizationTest.kt`
- `app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt`
- `app/src/test/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClientTest.kt`
- `app/src/test/java/com/qualityalternative/app/data/AccountLightProfileExporterTest.kt`
- `app/src/test/java/com/qualityalternative/app/data/PreferencesSettingsRepositoryTest.kt`
- `app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt`

## Included Evidence

- `evidence/sprint28_agent_inbox_drive_access/VALIDATION_SUMMARY.md`
- `evidence/sprint28_agent_inbox_drive_access/device_spike/RCLONE_PICKER_FOLDER_SPIKE.md`
- `evidence/sprint28_agent_inbox_drive_access/GPT_PRO_REVIEW_PROMPT_R1.md`
- `evidence/sprint28_agent_inbox_drive_access/sprint28_r1_tracked_diff.patch`
- `evidence/sprint28_agent_inbox_drive_access/git_log_r1.txt`

## Intentional Exclusions

- No full repo dump.
- No previous Sprint 27 review bundle zips or harvest archive; Sprint 27 release history is summarized in `docs/LANE_STATUS.md`.
- No physical PNG screenshots yet. `VisualQaScreenshotTest#captureSprint28AgentInboxDriveAccessStates` compiles, but the current environment has no attached ADB device and no emulator binary in PATH.
- No live rclone/Picker result yet. The exact device spike checklist is included and remains a release gate.

## Bundle Hygiene Notes

This bundle is intentionally scoped to code, tests, PRD/docs, and current evidence for the Drive access fix. If GPT Pro needs context outside these files to prove a finding, it should label that as `BUNDLE GAP` rather than infer from absent code.
