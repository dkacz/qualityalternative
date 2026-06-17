# Sprint 37 Review Bundle Manifest

Purpose: adversarial GPT Pro review of the Agent Inbox Drive authorization repair after the user's real-device failure screenshot.

## Primary Files

- `LIVE_E2E_REPORT.md`: narrative evidence index and diagnosis.
- `source_diff.patch`: scoped source/doc diff for the repair.
- `git_status_short.txt`: current worktree status at bundle creation time.
- `user_failure_screenshot_20260617_1616.jpg`: original user screenshot showing the failure.
- `docs/AGENT_INBOX_LIVE_REVIEW_GATE.md`: permanent release gate for Agent Inbox Drive changes.
- `docs/LANE_STATUS.md`: current Sprint 37 lane status.

## Source Files Included

- `app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt`
- `app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt`
- `app/src/main/java/com/qualityalternative/app/ui/GoogleDriveAuthorization.kt`
- `app/src/test/java/com/qualityalternative/app/ui/GoogleDriveAuthorizationTest.kt`
- `app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt`
- `app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt`

## Evidence Included

- `live_e2e/`: signed-in emulator screenshots from fresh install through reader rendering.
- `logs/live_*.xml`: UIAutomator dumps matching the live screenshots.
- `logs/live_debug_apk_metadata.txt`: APK path, hash, versionCode, versionName.
- `logs/live_adb_devices.txt`, `logs/live_emulator_account_state.txt`: device/account evidence.
- `live_drive_package/`, `live_drive_folder_name.txt`, `live_drive_folder_link.txt`: external package and folder identity.
- `logs/rclone_remote_listing*.json`, `logs/rclone_remote_listing.txt`: Google Drive state.
- `logs/package_validator.log`: package shape/hash/priority validation.
- `logs/targeted_unit_tests.log`, `logs/connected_visual.log`, `logs/TEST-visual.xml`: automated validation.
- `visual_e2e/`: connected visual states, including the fixed authorization-failure state.
- `logs/live_e2e_logcat.txt`, `logs/live_e2e_app_related_logcat.txt`, `logs/live_e2e_health_sentinels.txt`: runtime health evidence.

## Curation Notes

- The bundle intentionally excludes old Sprint 35/36 packet archives and release artifacts. Sprint 36 is relevant only as historical context in `docs/LANE_STATUS.md`; Sprint 37 must stand on current evidence.
- The user's physical phone was not attached to ADB. The live proof is a signed-in emulator using real Google Play Services, a real Google account, and a real Google Drive folder/package. The user screenshot is included as the target failure under repair.
- The reviewer must fail the candidate if this bundle does not prove the exact failure state is fixed or if the signed-in live flow is not sufficient for release confidence.
