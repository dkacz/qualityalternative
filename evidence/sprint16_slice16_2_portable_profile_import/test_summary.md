# Sprint 16 Slice 16.2 Validation Summary

Scope: Portable Profile settings import, safe merge, explicit replace, invalid profile handling, and Settings visual states.

R10 fixes covered after R9 GPT Pro review (`SCORE: 9/10`, `VERDICT: FAIL`, `VISUAL REVIEW: PASS`):

- Schema-complete nested validation before any replace mutation.
- Manual saved-link URL normalization is enforced for imported `normalizedUrl` values.
- Document fingerprint validation enforces `SHA256_BYTES`, `TEXT_SAMPLE_SHA256`, and `UNVERIFIED_METADATA_ONLY` strategy-specific rules.
- Document `sourceHint.lastKnownDisplayName` rejects raw URI/path/account-like values.
- Reading progress entries must reference imported library content or locally known content.
- Document `displayName` and `title` reject raw URI/path-like values.
- `sourceHint.providerLabel` rejects provider-id-like authorities such as Android/Google document provider ids.
- `documentFingerprint.normalizedTitle` must be lowercase, collapsed-whitespace, extension-free, and portable.
- `UNVERIFIED_METADATA_ONLY` documents must import as `MISSING_FILE_NEEDS_REATTACH`.
- `priorityContentIds` and `reactivatedCompletedContentIds` must reference imported or locally known content.
- `documentFingerprint.normalizedTitle` rejects leading/trailing whitespace, leftover `.html`/`.htm` extensions, uppercase, doubled whitespace, and raw platform identifiers.
- `library.userDocuments[].title` now uses the same unsafe raw-value rejection as document source display names for URI/account/provider/path-like values.
- Imported `warnings[].message` rejects Android/Google provider authorities such as `com.android.providers.media.documents`.
- `library.userLinks[].title` now uses the same unsafe raw-value rejection as document titles and rejects raw URI/path/account/token/provider strings.
- Imported `warnings[].message` also rejects generic reverse-DNS provider/class identifiers and raw exception/stacktrace-style text.
- Raw Gradle logs are included in `logs/` for unit, compile, and connected visual runs.
- Imported `warnings[].message` rejects bare exception class names such as `IllegalStateException`.
- Valid annotation sidecar filenames ending in `.annotations.jsonld`, such as `book.annotations.jsonld`, are accepted without being mistaken for provider/class identifiers.
- Manual JSON validation rejects quoted primitive values before mutation, including quoted `schemaVersion`, quoted link durations, quoted document timestamps, quoted fingerprint `sizeBytes`, and quoted reading progress percentages.
- Generated unknown-field warnings use sanitized section-level messages instead of raw dotted paths, so supported schema v1 imports with unknown nested fields remain non-fatal warnings.
- Generated unknown-field warnings also sanitize dotted warning sections such as `library.userLinks`, `library.userDocuments`, and `reading.progress` into user-facing copy without dots.
- Replace backup launches a timestamped `quality-alternative-profile-YYYYMMDD-HHMMSS.json` backup filename.
- Warning summaries surfaced in the import preview.
- Destructive replace confirmation includes a real export-backup action.
- Settings copy uses the product label `Portable profile`, not the internal `Account Light` name.
- Privacy notice visibly states that profile files may include preferences, saved links, document/source titles, and reading progress.

Commands run:

- `./gradlew testDebugUnitTest --tests 'com.qualityalternative.app.data.AccountLightProfileImporterTest' --tests 'com.qualityalternative.app.data.AccountLightProfileExporterTest'`
  - PASS after R10 dotted-section warning sanitization; raw log: `logs/r10_unit_import_export.log`.
- `./gradlew testDebugUnitTest compileDebugAndroidTestKotlin`
  - PASS after R10 dotted-section warning sanitization; raw log: `logs/r10_full_unit_compile.log`.
- `git diff --check`
  - PASS after R10 dotted-section warning sanitization; raw log: `logs/r10_git_diff_check.log`.
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.qualityalternative.app.MainActivityTest#accountLightImportSettingsShowsPreviewErrorsConfirmationAndSuccess`
  - PASS on `qaApi36(AVD) - 16` after R10 dotted-section warning sanitization; raw log: `logs/r10_connected_import_visual.log`; screenshots refreshed from this run.
- `git diff --check`
  - PASS

Screenshots captured from emulator:

- `screenshots/01_import_entry_light.png`
- `screenshots/02_merge_preview_with_unsupported_app_light.png`
- `screenshots/03_replace_confirmation_light.png`
- `screenshots/04_import_success_dark.png`
- `screenshots/05_invalid_import_dark.png`
- `screenshots/06_future_schema_import_dark.png`
