# Sprint 18 Evidence - Google Drive E2E And Account UX

## Captured Flow

- `screenshots/initial_state.png`: pre-fix onboarding with misleading account shortcut.
- `screenshots/onboarding_without_account_shortcut.png`: post-fix onboarding with only `Begin`.
- `screenshots/settings_annotation_drive_controls.png`: Drive controls before OAuth.
- `screenshots/google_cloud_oauth_published.png`: Google Cloud OAuth audience after publishing the verified debug app.
- `screenshots/live_drive_flow_09_annotation_popup.png`: compact range controls and live range summary in the annotation editor.
- `screenshots/live_drive_flow_11_annotation_saved.png`: emulator annotation note saved locally.
- `screenshots/live_drive_flow_19_drive_synced.png`: Google Drive connected after syncing the live annotation.
- `screenshots/live_drive_flow_20_drive_save_now.png`: manual Drive `Save now` succeeds after connection.
- `screenshots/drive_selection_current_drive_auth/01_drive_connect_light.png`: current APK state before OAuth cancellation, with `Connect` visible and `Change destination` kept separate for the local folder picker.
- `screenshots/drive_selection_current_drive_auth/03_drive_auth_failure_light.png`: current connected-test visual for the shorter cancelled/blocked Google Drive copy, with `Connect` still OAuth-only and the local folder picker kept under `Change destination`.
- `logs/r2_oauth_cancel_clean_window_focus_during_google_auth.txt`: confirms the `Connect` action opened Google Play services `AuthorizationActivity` before the back-out.
- `screenshots/reader_bottom_fit_current/01_tall_phone_default_text.png`: current reader body page with the last line fully visible above the footer.
- `screenshots/reader_bottom_fit_current/page-fit-summaries.txt`: current measured pagination summaries for body, large text, code-heavy, oversized-code, and compact viewport cases.

## Resolved Google OAuth Blocker

The previous live blocker was:

`Access blocked: Quality Alternative has not completed the Google verification process`

Google Cloud was verified for package `com.qualityalternative.app`, SHA-1 `15:8F:50:67:D8:1C:7A:79:F7:8C:94:BA:1E:5E:D2:1B:9E:F6:33:C9`, and Drive scope `https://www.googleapis.com/auth/drive.file`, then published. The configured internal emulator tester account now completes authorization.

OAuth cancel/back-out now reports that authorization was cancelled or blocked and confirms no folder destination changed. Runtime R2 evidence shows `Connect` opening Google Play services authorization, then returning to Settings with the local `Change destination` control still separate and no Android folder picker launched.

## Live Drive Write Proof

- `logs/rclone_quality_alternative_annotations_after_live_note_sync.txt`: `rclone` shows the Drive index plus a JSON-LD annotation file after connect-sync.
- `logs/rclone_live_note_jsonld_after_sync.json`: downloaded Drive JSON-LD includes note body `Sprint18_drive_live_note`.
- `logs/rclone_quality_alternative_annotations_after_save_now.txt`: `rclone` shows both Drive files updated again after manual `Save now`.

## Validation Logs

- `logs/unit_reader_document_gdrive_regressions.log`
- `logs/full_unit_tests_after_reader_bottom_fit.log`
- `logs/connected_onboarding_no_account_shortcut.log`
- `logs/connected_drive_cancel_copy_current.log`
- `logs/connected_drive_selection_regressions_current.log`
- `logs/connected_reader_bottom_fit_current.log`
- `logs/connected_reader_start_regression_fixed.log`
- `logs/connected_cross_page_annotation_controls.log`
- `logs/connected_annotation_surface_sizing.log`
- `logs/assemble_debug_after_reader_bottom_fit.log`
- `COMMAND_TRANSCRIPTS.md`
- `test_reports/connected_drive_cancel_copy_current.xml`
- `test_reports/connected_drive_selection_regressions_current.xml`
- `test_reports/connected_reader_bottom_fit_current.xml`
- `test_reports/unit_progress_snapshot_after_reader_bottom_fit.xml`

Older failed runs are retained only under `logs/pre_fix_failures/` to show the regression before the stable `sourceBlockIndex` fix.

## R2 Package Hygiene Fix

GPT Pro R2 passed the implementation, visual review, Google Drive E2E, annotation selection, and reader pagination checks, but scored `9/10` because the review ZIP omitted several files referenced by this README. R3 keeps this README aligned with shipped artifacts and includes the referenced standalone logs, current XML/textproto/HTML reports, current screenshots, and the small pre-fix provenance directory.
