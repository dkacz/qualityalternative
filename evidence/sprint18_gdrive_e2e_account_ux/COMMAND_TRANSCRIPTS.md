# Sprint 18 Command Transcripts

These are the exact validation commands used for the current post-fix evidence.

```bash
cd /Users/omare/Documents/qualityalternative
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew :app:testDebugUnitTest | tee evidence/sprint18_gdrive_e2e_account_ux/logs/full_unit_tests_after_reader_bottom_fit.log
```

```bash
cd /Users/omare/Documents/qualityalternative
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#onboardingWelcomeDoesNotShowAccountShortcutWithoutAccountFlow | tee evidence/sprint18_gdrive_e2e_account_ux/logs/connected_onboarding_no_account_shortcut.log
```

```bash
cd /Users/omare/Documents/qualityalternative
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#readerAnnotationStartCanMoveBackIntoPreviousSourceBlocks | tee evidence/sprint18_gdrive_e2e_account_ux/logs/connected_reader_start_regression_fixed.log
```

```bash
cd /Users/omare/Documents/qualityalternative
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#readerAnnotationControlsExpandAndReopenAcrossPages | tee evidence/sprint18_gdrive_e2e_account_ux/logs/connected_cross_page_annotation_controls.log
```

```bash
cd /Users/omare/Documents/qualityalternative
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#annotationDriveSyncSettingsShowsConnectFailureConnectedAndRetryStates,com.qualityalternative.app.MainActivityTest#readerAnnotationStartCanMoveBackIntoPreviousSourceBlocks,com.qualityalternative.app.MainActivityTest#crossPageAnnotationSelectionPersistsAcrossPagedSourceChunks,com.qualityalternative.app.MainActivityTest#readerAnnotationControlsExpandAndReopenAcrossPages | tee evidence/sprint18_gdrive_e2e_account_ux/logs/connected_drive_selection_regressions_current.log
```

```bash
cd /Users/omare/Documents/qualityalternative
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#readerPaginationFitRespondsToViewportAndReaderTextSize | tee evidence/sprint18_gdrive_e2e_account_ux/logs/connected_reader_bottom_fit_current.log
```

```bash
cd /Users/omare/Documents/qualityalternative
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#readerAnnotationEditorContainsLongQuoteAndLongNoteWithinViewport | tee evidence/sprint18_gdrive_e2e_account_ux/logs/connected_annotation_surface_sizing.log
```

```bash
cd /Users/omare/Documents/qualityalternative
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18 ./gradlew :app:assembleDebug | tee evidence/sprint18_gdrive_e2e_account_ux/logs/assemble_debug_after_reader_bottom_fit.log
```

```bash
cd /Users/omare/Documents/qualityalternative
rclone lsf gdrive:"Quality Alternative annotations" --format "ptsm" --separator "\t" | tee evidence/sprint18_gdrive_e2e_account_ux/logs/rclone_quality_alternative_annotations_after_save_now.txt
```

```bash
cd /Users/omare/Documents/qualityalternative
rclone cat gdrive:"Quality Alternative annotations/quality-alternative-care-for-the-soul-first-care-for-the-soul-first.annotations.jsonld" | tee evidence/sprint18_gdrive_e2e_account_ux/logs/rclone_live_note_jsonld_after_sync.json
```
