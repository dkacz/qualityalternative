# Sprint 18 GPT Pro Review Bundle Manifest

This bundle is selective. It includes the files needed to audit Sprint 18 without shipping stale repo-wide artifacts.

## Included

- Sprint contract and evidence index:
  - `docs/SPRINT_18_GDRIVE_E2E_ACCOUNT_UX.md`
  - `evidence/sprint18_gdrive_e2e_account_ux/README.md`
  - `evidence/sprint18_gdrive_e2e_account_ux/GPT_PRO_REVIEW_R1.md`
  - `evidence/sprint18_gdrive_e2e_account_ux/GPT_PRO_REVIEW_R2.md`
- Current changed source and tests:
  - `app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt`
  - `app/src/main/java/com/qualityalternative/app/domain/model/ReaderDocumentModels.kt`
  - `app/src/androidTest/java/com/qualityalternative/app/MainActivityTest.kt`
  - `app/src/test/java/com/qualityalternative/app/ui/GoogleDriveAuthorizationUiTest.kt`
  - `app/src/test/java/com/qualityalternative/app/ui/ProgressSnapshotTest.kt`
  - `app/src/test/java/com/qualityalternative/app/domain/model/ReaderDocumentModelsTest.kt`
- Current code diff:
  - `evidence/sprint18_gdrive_e2e_account_ux/sprint18_current_code_diff.patch`
- Current validation logs:
  - full unit test log
  - focused reader/Drive unit regression logs
  - current Drive cancellation copy connected test log
  - current grouped Drive/annotation-selection connected regression log
  - current measured reader bottom-fit connected test log
  - onboarding no-account connected test log
  - reader start-regression connected test log
  - cross-page annotation controls connected test log
  - long annotation surface connected test log
  - current debug APK assemble log after reader bottom-fit changes
  - current XML/textproto/HTML reports for the connected and unit runs available under `test_reports/`
- Live Drive evidence:
  - Google Cloud OAuth screenshots
  - emulator Drive connect/sync/Save now screenshots
  - clean OAuth cancellation screenshots and window focus log, excluding personal account chooser screenshots
  - current reader bottom-fit screenshots and page-fit summaries
  - `rclone` Drive listing logs
  - downloaded JSON-LD annotation file containing `Sprint18_drive_live_note`

## Excluded

- Full repo snapshot, Gradle build caches, APK binaries, `.git`, and unrelated sprint archives.
- Superseded screenshots/logs not needed for the release gate, except the small `logs/pre_fix_failures/` directory retained as explicit pre-fix provenance.
- Personal account chooser screenshots/XML from exploratory setup runs.

## R2 Hygiene Correction

The R2 review returned `9/10` solely because the ZIP did not include every file referenced by the evidence README/manifest. The R3 bundle includes those referenced files and replaces the stale `connected_last_test_result.xml` README reference with the current named test reports.
