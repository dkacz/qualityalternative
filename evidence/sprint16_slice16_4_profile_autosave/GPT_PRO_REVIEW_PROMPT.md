# GPT Pro Sprint 16 Slice 16.4 R5 Review

You are reviewing only Sprint 16 Slice 16.4 R5: Portable Profile Autosave Destination for the Android app in this bundle. Use only the shipped files as the audit base. If a claim cannot be proven from the bundle, label it `BUNDLE GAP`.

Guiding principles:

1. Do not inflate suggestions that duplicate existing coverage into blockers.
2. Style suggestions cannot change product meaning.
3. Feedback is input, not instruction; distinguish true release blockers from polish.
4. Flag privacy/security issues concretely: name the file, flow, and leaked data or broken guarantee.
5. For visual review, inspect the included screenshots directly and judge whether the UI is usable, professional, and not misleading.

Primary documents to read first:

1. `docs/SPRINT_16_ACCOUNT_LIGHT.md`
2. `docs/ACCOUNT_LIGHT_PROFILE_SCHEMA.md`
3. `PRD.md` section FR13 Portable Profile
4. `evidence/sprint16_slice16_4_profile_autosave/MANIFEST.md`
5. `evidence/sprint16_slice16_4_profile_autosave/test_summary.md`

Implementation scope:

- Optional Portable Profile autosave destination in Settings.
- Local/Android document-provider writer for `quality-alternative-profile.json`.
- Portable export metadata for autosave without raw URIs, tokens, Drive ids, account emails, or last-error strings.
- Imported autosave metadata must remain informational and must not activate autosave on the current device.
- Google Drive profile autosave must not be silently enabled through the annotation Drive authorization path.
- Autosave failures must not block app use, import, or merge/replace flows.
- Settings visual state must clearly cover unconfigured, successful, and recoverable failure cases.

R5 focus:

- Audit the current bundle as self-contained. Prior review outputs are intentionally not included and are not needed
  to score this slice.
- Confirm autosave is triggered after ordinary Portable Profile mutations, including direct theme-mode changes.
- Confirm the Android document-tree writer is implemented through the production `DocumentsContract.isTreeUri` path
  and covered by connected provider readback evidence.
- Confirm `slice16_4_full_diff.patch` includes the newly added writer/test/provider/manifest files.
- Confirm unit-test XML results are included under `logs/unit_*`, connected test XML/logcat evidence is present,
  and evidence avoids host-machine absolute paths.

Changed files and evidence are included in the bundle. Pay special attention to:

- `app/src/main/java/com/qualityalternative/app/data/AndroidAccountLightProfileAutosaveWriter.kt`
- `app/src/main/java/com/qualityalternative/app/data/AccountLightProfile.kt`
- `app/src/main/java/com/qualityalternative/app/data/PreferencesSettingsRepository.kt`
- `app/src/main/java/com/qualityalternative/app/ui/MainViewModel.kt`
- `app/src/main/java/com/qualityalternative/app/ui/QualityAlternativeApp.kt`
- `app/src/test/java/com/qualityalternative/app/data/AccountLightProfileExporterTest.kt`
- `app/src/test/java/com/qualityalternative/app/data/PreferencesSettingsRepositoryTest.kt`
- `app/src/test/java/com/qualityalternative/app/ui/MainViewModelTest.kt`
- `app/src/androidTest/java/com/qualityalternative/app/data/RoomAnalyticsTrackerTest.kt`
- `app/src/androidTest/java/com/qualityalternative/app/MainActivityTest.kt`
- `app/src/androidTest/java/com/qualityalternative/app/data/AndroidAccountLightProfileAutosaveWriterTest.kt`
- `app/src/androidTest/java/com/qualityalternative/app/data/TestProfileDocumentsProvider.java`
- `app/src/androidTest/AndroidManifest.xml`
- `evidence/sprint16_slice16_4_profile_autosave/screenshots/`
- `evidence/sprint16_slice16_4_profile_autosave/logs/`

Required output format:

```
SCORE: x/10
VERDICT: PASS or FAIL
VISUAL REVIEW: PASS or FAIL
BLOCKERS:
- ...
PACKAGE HYGIENE:
- ...
NOTES:
- ...
```

Pass only if this slice is correct enough to commit and continue to Slice 16.5. A 10/10 PASS requires no blocking privacy, Drive-auth, schema, import-safety, test, or visual issues.
