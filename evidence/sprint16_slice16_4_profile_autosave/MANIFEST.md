# Sprint 16 Slice 16.4 Evidence

Scope: Portable Profile autosave destination.

Changed behavior:

- Settings can store an optional Portable Profile autosave destination selected by the user.
- Autosave writes `quality-alternative-profile.json` to a local file, local folder, Android document URI, or Android document tree.
- The exported profile includes only portable autosave metadata: provider, display name, last successful timestamp, and `REQUIRES_LOCAL_SELECTION`.
- Raw `content://`, `file://`, Drive ids, permission grants, OAuth tokens, account emails, and last-error strings are not exported.
- Autosave write failures are recoverable and do not block app use or import/merge flows.
- Autosave now runs after ordinary Portable Profile mutations, including settings changes, priority toggles,
  saved-link creation, document import, library deletion, reading progress, completion, annotation export/sync
  status changes, and user-content availability changes.
- R3 closes the R2 blocker by also autosaving after direct theme-mode changes through `selectThemeMode()`,
  with a regression test proving the rewritten profile JSON contains `"themeMode": "DARK"`.
- The Android document-tree writer is covered by an instrumented provider readback test that creates and rewrites
  `quality-alternative-profile.json` through `DocumentsContract` tree/query/create/open URI paths.
- Google Drive profile autosave remains deferred until there is explicit profile-specific Drive authorization and destination selection.

Artifacts:

- `test_summary.md`
- `slice16_4_full_diff.patch` includes tracked changes plus the new Android autosave writer, document-tree
  connected test, test document provider, and androidTest manifest.
- `screenshots/sprint16-profile-autosave-1777942518986/01_profile_autosave_empty_light.png`
- `screenshots/sprint16-profile-autosave-1777942518986/02_profile_autosave_success_light.png`
- `screenshots/sprint16-profile-autosave-1777942518986/03_profile_autosave_failure_dark.png`
- `logs/connected_profile_autosave_test.xml`
- `logs/connected_profile_autosave_logcat.txt`
- `logs/connected_profile_autosave_ui_test.xml`
- `logs/connected_profile_autosave_ui_logcat.txt`
- `logs/connected_profile_autosave_document_tree_test.xml`
- `logs/connected_profile_autosave_document_tree_logcat.txt`
- `logs/unit_main_view_model_test.xml`
- `logs/unit_account_light_profile_exporter_test.xml`
- `logs/unit_preferences_settings_repository_test.xml`
