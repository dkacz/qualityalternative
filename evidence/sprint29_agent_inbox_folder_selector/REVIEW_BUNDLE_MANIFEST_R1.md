# Sprint 29 GPT Pro R1 Review Bundle Manifest

Review target: Agent Inbox folder selector replacement for the pasted-folder fallback.

## Primary Documents

- `docs/SPRINT_29_AGENT_INBOX_FOLDER_SELECTOR.md` - primary sprint contract/status.
- `evidence/sprint29_agent_inbox_folder_selector/VALIDATION_SUMMARY.md` - validation and evidence summary.
- `PRD.md` - product requirements updated for Android folder-picker fallback.
- `docs/LANE_STATUS.md` - repo-level lane status.

## Implementation Files

- `app/src/main/java/com/qualityalternative/app/domain/service/AgentInboxDrive.kt`
- `app/src/main/java/com/qualityalternative/app/data/AndroidDocumentTreeAgentInboxClient.kt`
- `app/src/main/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClient.kt`
- `app/src/main/java/com/qualityalternative/app/data/AppContainer.kt`
- `app/src/main/java/com/qualityalternative/app/data/PreferencesSettingsRepository.kt`
- `app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt`
- `app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt`

## Test Files

- `app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt`
- `app/src/test/java/com/qualityalternative/app/data/PreferencesSettingsRepositoryTest.kt`
- `app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt`

## Evidence

- `evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r1/contact_sheet_selector_r1.png`
- `evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r1/sprint28-agent-inbox-drive-access-1781511250971/*.png`
- `evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r1/TEST-sprint29-selector-visual.xml`
- `evidence/sprint29_agent_inbox_folder_selector/visual_e2e_selector_r1/logcat-sprint29-selector-visual.txt`
- `evidence/sprint29_agent_inbox_folder_selector/logs/full_local_gate_r1.log`
- `evidence/sprint29_agent_inbox_folder_selector/logs/full_local_gate_r1.status.txt`
- `evidence/sprint29_agent_inbox_folder_selector/logs/git_diff_check_r1.log`
- `evidence/sprint29_agent_inbox_folder_selector/logs/git_diff_check_r1.status.txt`
- `evidence/sprint29_agent_inbox_folder_selector/sprint29_selector_tracked_diff.patch`
- `evidence/sprint29_agent_inbox_folder_selector/git_status_r1.txt`
- `evidence/sprint29_agent_inbox_folder_selector/git_log_r1.txt`

## Exclusions

- Prior Sprint 28 review ZIPs and release APKs are excluded as stale/heavy. The bundle includes only the Sprint 29 selector delta and the previous-state context needed through `docs/LANE_STATUS.md` and `docs/SPRINT_29_AGENT_INBOX_FOLDER_SELECTOR.md`.
- Full build directories are excluded; raw validation logs and result XML are included instead.
