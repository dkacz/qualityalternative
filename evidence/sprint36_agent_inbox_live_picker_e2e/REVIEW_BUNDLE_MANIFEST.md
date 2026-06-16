# Sprint 36 Review Bundle Manifest

Purpose: adversarial GPT Pro review of Agent Inbox live Google Drive folder browser repair.

Primary documents:

- `GPT_PRO_REVIEW_PROMPT.md`
- `LIVE_E2E_REPORT.md`
- `docs/AGENT_INBOX_LIVE_REVIEW_GATE.md`
- `docs/AGENT_INBOX_PACKAGE_AUTHORING.md`
- `docs/LANE_STATUS.md`
- `AGENTS.md`
- `PRD.md`

Implementation scope:

- `app/src/main/java/com/qualityalternative/app/domain/service/AgentInboxDrive.kt`
- `app/src/main/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClient.kt`
- `app/src/main/java/com/qualityalternative/app/data/AndroidDocumentTreeAgentInboxClient.kt`
- `app/src/main/java/com/qualityalternative/app/ui/GoogleDriveAuthorization.kt`
- `app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt`
- `app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt`

Test scope:

- `app/src/test/java/com/qualityalternative/app/data/AndroidGoogleDriveAgentInboxClientTest.kt`
- `app/src/test/java/com/qualityalternative/app/data/AndroidHybridAgentInboxDriveClientTest.kt`
- `app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt`
- `app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt`
- `app/src/androidTest/java/com/qualityalternative/app/MainActivityTest.kt`

Evidence:

- `live_e2e/`: final live emulator screenshots from fresh install through reader rendering.
- `visual_e2e/sprint35-agent-inbox-folder-selector-repair-1781630925520/`: final connected visual test screenshots after the layout fix.
- `logs/live_e2e_xml_milestones.txt`: machine-readable milestone summary.
- `logs/live_debug_apk_metadata.txt`: final APK hash and package metadata.
- `logs/live_emulator_account_state.txt`: device and Google account state.
- `logs/rclone_remote_listing_after_import.json`: real Drive folder state.
- `logs/live_e2e_health_sentinels.txt`: crash and known failure string sentinel result.
- `logs/live_e2e_logcat.txt`: full live logcat.
- `logs/r3_targeted_unit_tests.log`, `logs/r3_connected_drive_folder_browser_visual.log`, `logs/r3_git_diff_check.log`: latest validation logs.
- `gpt_pro_review_response.md`: prior GPT Pro `SCORE: 8/10`, `VERDICT: FAIL` response that identified clipped controls and snackbar coverage.
- `live_drive_package/`: externally uploaded package content used for live E2E.
- `source_diff.patch`: full patch for the scoped files.

Excluded on purpose:

- Earlier failed DocumentsUI/system picker screenshots and logs. They were diagnostic only and would make the review packet noisy.
- Earlier intermediate visual test runs before the final `Drive sync not connected` label, final layout pass, and R4 live rerun.
- Whole repo history and unrelated sprint evidence.
