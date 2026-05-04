# Account Light Profile Schema

Status: Sprint 16 contract, schema v1

## Goal

Account Light gives Quality Alternative account-like portability without a Quality Alternative backend account. The user owns a local, versioned JSON profile file that can be exported, imported, and later autosaved to a user-selected destination.

The product remains local-first. Google Drive can be used as a user-authorized file destination, but Drive authorization is not a Quality Alternative account and must not be copied between devices.

## File Contract

Default filename:

```text
quality-alternative-profile.json
```

Timestamped backup filename:

```text
quality-alternative-profile-YYYYMMDD-HHMMSS.json
```

MIME type:

```text
application/json
```

All timestamps are Unix epoch milliseconds in UTC. Required string fields must be non-blank after trimming unless explicitly nullable.

## Stable Content Identifiers

Portable user content ids are public, stable app identifiers, not Room row ids.

Allowed schema v1 prefixes:

```text
user-link-
user-document-
editorial-
meditation-
```

User-created content ids must use lowercase UUID strings:

```text
user-link-6f3f8f4d-5d33-4a2a-a4ef-111111111111
user-document-6f3f8f4d-5d33-4a2a-a4ef-222222222222
```

Validation regex:

```text
^(user-link|user-document)-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$|^(editorial|meditation)-[a-z0-9][a-z0-9._-]{2,120}$
```

Generation policy:

- New user links created in the app receive `user-link-` plus a random UUID v4.
- New user documents created in the app receive `user-document-` plus a random UUID v4.
- Import must not silently rewrite imported ids.
- If an imported id is syntactically valid but collides with a different local record, the importer keeps the local record id and records the imported row as a conflict in the import plan.
- If `contentId` and a secondary match key point to two different local records, import must stop with `CONTENT_ID_SECONDARY_KEY_CONFLICT` before mutation.
- Editorial and meditation ids are app-defined and may be referenced by settings/progress only when the current app build knows them.

## Enum Domains

`themeMode`:

```text
LIGHT, DARK
```

`preferredDurationBucket`:

```text
QUICK, FOCUS, DEEP
```

`contentPriority`:

```text
BALANCED, READINGS, MY_FILES, SAVED_LINKS, MEDITATION
```

`topicTags`:

```text
ATTENTION, PRACTICAL, BODY, NATURE, HISTORY_CULTURE, ESSAYS, PHILOSOPHY,
SCIENCE, DESIGN, POETRY, HISTORY, TECH, FICTION, CLIMATE, ECONOMICS,
FOOD, ARCHITECTURE, CREATIVITY, PSYCHOLOGY, OTHER
```

`contentFormat`:

```text
MARKDOWN, HTML, PDF, EPUB
```

`availability`:

```text
AVAILABLE, UNAVAILABLE, NEEDS_FALLBACK
```

`documentImportState`:

```text
AVAILABLE_ON_THIS_DEVICE, MISSING_FILE_NEEDS_REATTACH, EXTERNAL_HANDOFF_ONLY
```

`autosaveProvider`:

```text
NONE, ANDROID_DOCUMENT_TREE, GOOGLE_DRIVE
```

## Top-Level JSON

All top-level keys are required. Unknown top-level keys are ignored with a non-fatal import warning when `schemaVersion` is supported. Unsupported future `schemaVersion` values are rejected before mutation.

```json
{
  "schemaVersion": 1,
  "exportedAtMillis": 1777879200000,
  "app": {
    "profileFormat": "quality-alternative-account-light",
    "packageName": "com.qualityalternative.app",
    "appVersionName": "0.8.1-alpha",
    "appVersionCode": 13
  },
  "profile": {
    "profileId": "qa-local-6f3f8f4d-5d33-4a2a-a4ef-111111111111",
    "createdAtMillis": 1777870000000,
    "updatedAtMillis": 1777879200000,
    "displayName": null
  },
  "settings": {
    "hasCompletedOnboarding": true,
    "selectedAppPackages": ["com.instagram.android"],
    "preferredTopics": ["ATTENTION", "SCIENCE", "PHILOSOPHY"],
    "preferredDurationBucket": "FOCUS",
    "selectedPackIds": ["attention_reset_v1"],
    "themeMode": "LIGHT",
    "meditationDurationMinutes": 3,
    "readerFontScale": 1.0,
    "contentPriority": "BALANCED",
    "priorityContentIds": [],
    "reactivatedCompletedContentIds": [],
    "openAnywayUnlockMinutes": 60
  },
  "library": {
    "userLinks": [],
    "userDocuments": []
  },
  "reading": {
    "progress": []
  },
  "annotations": {
    "export": {
      "destinationDisplayName": null,
      "lastSuccessfulAtMillis": null
    },
    "driveSync": {
      "wasEnabledOnSourceDevice": false,
      "folderDisplayName": null,
      "lastSuccessfulAtMillis": null
    },
    "sidecarIndex": []
  },
  "sync": {
    "profileAutosave": {
      "provider": "NONE",
      "destinationDisplayName": null,
      "lastSuccessfulAtMillis": null,
      "activationStateOnImport": "REQUIRES_LOCAL_SELECTION"
    }
  },
  "warnings": [
    {
      "code": "UNKNOWN_FIELD_IGNORED",
      "severity": "INFO",
      "section": "settings",
      "contentId": null,
      "message": "An unknown field was ignored."
    }
  ]
}
```

## Required Object Shapes

### `app`

Required fields:

- `profileFormat`: string, must equal `quality-alternative-account-light`.
- `packageName`: string, must equal `com.qualityalternative.app`.
- `appVersionName`: string.
- `appVersionCode`: integer, minimum `1`.

### `profile`

Required fields:

- `profileId`: string, regex `^qa-local-[0-9a-fA-F-]{36}$`.
- `createdAtMillis`: non-negative integer.
- `updatedAtMillis`: non-negative integer, must be `>= createdAtMillis`.
- `displayName`: nullable string, maximum 80 characters after trimming.

`profileId` is a portable local-profile identity, not a server account id. Merge import preserves the current device profile id. Replace import may adopt the imported profile id only after explicit confirmation.

### `warnings[]`

`warnings` is required and may be empty. Warning entries are machine-readable and must not contain raw platform identifiers, URIs, Drive ids, account emails, filesystem paths, provider document ids, access tokens, OAuth values, stack traces, or raw error strings.

Required fields:

- `code`: one of the warning codes below.
- `severity`: `INFO` or `WARNING`.
- `section`: one of `app`, `profile`, `settings`, `library.userLinks`, `library.userDocuments`, `reading.progress`, `annotations`, `sync`, `unknown`.
- `contentId`: nullable stable content id.
- `message`: nullable redacted display string, maximum 160 characters.

Warning codes:

```text
UNKNOWN_FIELD_IGNORED
DUPLICATE_SCALAR_DEDUPED
DOCUMENT_FINGERPRINT_UNVERIFIED
DOCUMENT_FILE_MISSING_ON_IMPORT
IMPORTED_SETTINGS_NOT_APPLIED
AUTOSAVE_REQUIRES_LOCAL_SELECTION
DRIVE_REAUTHORIZATION_REQUIRED
ANNOTATION_SIDECAR_MISSING
UNSUPPORTED_LOCAL_APP_PACKAGE
CONFLICT_RETAINED_LOCAL_VALUE
```

Warnings generated during import are shown in the import preview/result, but they are not persisted back into the local profile unless the user exports again.

### `settings`

Required fields:

- `hasCompletedOnboarding`: boolean.
- `selectedAppPackages`: array of package-name strings. Each string must match `^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z0-9_]+)+$`.
- `preferredTopics`: array of `topicTags`, minimum 0 for import, minimum 3 only for onboarding completion flows.
- `preferredDurationBucket`: `preferredDurationBucket`.
- `selectedPackIds`: array of non-blank strings.
- `themeMode`: `themeMode`.
- `meditationDurationMinutes`: integer in `1..60`.
- `readerFontScale`: decimal number in `0.80..1.60`, rounded to two decimal places on export. Default is `1.00`. This is the app-level reader font-size preference and must be portable across devices; users must not need to change Android system font size to adjust the reader.
- `contentPriority`: `contentPriority`.
- `priorityContentIds`: array of content ids.
- `reactivatedCompletedContentIds`: array of content ids.
- `openAnywayUnlockMinutes`: integer in `15..240`.

Arrays are de-duplicated while preserving first-seen order on export. Import treats duplicate values as non-fatal warnings and de-duplicates before validation output.

### `library.userLinks[]`

Required fields:

- `contentId`: stable content id, non-blank string.
- `normalizedUrl`: `http` or `https` URL accepted by the same validator as manual link add.
- `title`: non-blank string, maximum 200 characters.
- `description`: string, maximum 1000 characters.
- `durationMinutes`: integer in `1..240`; this is legacy/display metadata and must not be used as a whole-source recommendation filter.
- `topicTags`: array of `topicTags`, minimum 1.
- `availability`: `availability`.
- `createdAtMillis`: non-negative integer.
- `updatedAtMillis`: non-negative integer, must be `>= createdAtMillis`.

Optional fields:

- `sourceLabel`: nullable string, maximum 120 characters.

### `library.userDocuments[]`

Required fields:

- `contentId`: stable content id, non-blank string.
- `displayName`: non-blank string, maximum 240 characters.
- `mimeType`: nullable string.
- `documentFormat`: one of `MARKDOWN`, `PDF`, `EPUB`.
- `title`: non-blank string, maximum 200 characters.
- `description`: string, maximum 1000 characters.
- `durationMinutes`: integer in `1..240`; computed/display metadata only.
- `topicTags`: array of `topicTags`, minimum 1.
- `availability`: must be exported as `AVAILABLE`, `UNAVAILABLE`, or `NEEDS_FALLBACK`.
- `documentImportState`: `documentImportState`.
- `documentFingerprint`: object described below.
- `createdAtMillis`: non-negative integer.
- `updatedAtMillis`: non-negative integer, must be `>= createdAtMillis`.

Schema v1 must not export raw Android `content://` or `file://` URIs. A user-document object may include only display-safe source hints:

```json
{
  "sourceHint": {
    "lastKnownDisplayName": "book.epub",
    "providerLabel": null
  }
}
```

`sourceHint.providerLabel` is display-only and must not contain a raw URI, account email, filesystem path, or provider document id.

### `documentFingerprint`

Required fields:

- `strategy`: `SHA256_BYTES`, `TEXT_SAMPLE_SHA256`, or `UNVERIFIED_METADATA_ONLY`.
- `sha256`: nullable lowercase hex string.
- `sizeBytes`: nullable non-negative integer.
- `normalizedTitle`: non-blank string.
- `format`: `contentFormat`.

Rules:

- If the app can read the document bytes at export time, use `SHA256_BYTES`, set `sha256`, and set `sizeBytes`.
- If byte hashing is unavailable but reader text is available, use `TEXT_SAMPLE_SHA256`, set `sha256` to a hash of the deterministic text sample described below, and set `sizeBytes` to null.
- If neither is available, use `UNVERIFIED_METADATA_ONLY` with `sha256 = null`.
- Imported documents with `UNVERIFIED_METADATA_ONLY` must be marked `MISSING_FILE_NEEDS_REATTACH` until the user reattaches and confirms the file.
- Reading progress and annotation navigation for imported documents may become active only after the reattached file fingerprint matches `SHA256_BYTES` or `TEXT_SAMPLE_SHA256`, or after explicit user confirmation for `UNVERIFIED_METADATA_ONLY`.

`normalizedTitle` algorithm:

- trim leading/trailing whitespace
- collapse internal whitespace to one ASCII space
- lowercase with `Locale.ROOT`
- remove file extension only when it exactly matches the exported `format`
- maximum 200 Unicode scalar values after normalization

`TEXT_SAMPLE_SHA256` algorithm:

- Build reader plain text from blocks in source order.
- Normalize line endings to `\n`.
- Trim leading/trailing whitespace for each block.
- Collapse runs of horizontal whitespace inside each block to one ASCII space.
- Join non-blank blocks with `\n\n`.
- Take the first 4096 UTF-8 bytes, then the last 4096 UTF-8 bytes, separated by literal `\n---QA-SAMPLE-SPLIT---\n`. If the normalized text is 8192 bytes or shorter, hash the full normalized text without a separator.
- Hash the resulting UTF-8 byte sequence with SHA-256 and encode lowercase hex.

If an EPUB extractor changes block ordering or text normalization in a future app version, importer may require explicit user confirmation before activating imported progress from `TEXT_SAMPLE_SHA256`.

### `reading.progress[]`

Required fields:

- `contentId`: non-blank string.
- `progressPercent`: integer in `0..100`.
- `lastVisibleParagraphIndex`: integer `>= 0`.
- `paragraphCount`: integer `>= 1`.
- `updatedAtMillis`: non-negative integer.
- `completedAtMillis`: nullable non-negative integer.

Validation:

- `lastVisibleParagraphIndex < paragraphCount`.
- `completedAtMillis != null` implies `progressPercent = 100`.
- `completedAtMillis == null` may use any `progressPercent` in `0..99`.
- Progress imports only when the matching content exists locally or is imported in the same profile.

### `annotations`

`annotations.export` required fields:

- `destinationDisplayName`: nullable display-only string.
- `lastSuccessfulAtMillis`: nullable non-negative integer.

`annotations.driveSync` required fields:

- `wasEnabledOnSourceDevice`: boolean.
- `folderDisplayName`: nullable display-only string.
- `lastSuccessfulAtMillis`: nullable non-negative integer.

`annotations.sidecarIndex[]` optional entries:

- `contentId`: non-blank string.
- `sourceTitle`: non-blank string.
- `jsonLdFileName`: non-blank string ending in `.annotations.jsonld`.
- `sha256`: nullable lowercase hex string.
- `updatedAtMillis`: non-negative integer.

The profile does not replace the Sprint 15 per-source W3C Web Annotation JSON-LD sidecars. Import may show sidecar references as missing until the files are present in the chosen annotation folder or Drive folder.

### `sync.profileAutosave`

Required fields:

- `provider`: `autosaveProvider`.
- `destinationDisplayName`: nullable display-only string.
- `lastSuccessfulAtMillis`: nullable non-negative integer.
- `activationStateOnImport`: must equal `REQUIRES_LOCAL_SELECTION`.

Import rule:

- Imported autosave metadata is informational only.
- It must not enable autosave on the current device.
- `provider = GOOGLE_DRIVE` must show a reconnect/select-destination action before any sync attempt.
- `provider = ANDROID_DOCUMENT_TREE` must show a local folder selection action before any autosave attempt.
- The app must not export raw Drive folder ids, SAF tree URIs, account emails, raw filesystem paths, or last-error strings.

## Excluded Data

The profile must not export:

- Google access tokens.
- Google Identity authorization grants.
- OAuth client secrets.
- Raw Drive folder ids or file ids.
- Google account email addresses.
- Android permission-grant state.
- Raw Android `content://` or `file://` URIs.
- Raw SAF tree URIs or provider document ids.
- Raw analytics event logs.
- Accessibility-service runtime state.
- Delay windows.
- Raw app-internal database row ids unless they are already public stable content ids.
- Document binary contents in schema v1.
- Raw last-error strings from Drive, SAF, filesystem, or network operations.

## Export Rules

Exporter must:

- Emit UTF-8 JSON.
- Emit all required top-level objects even when arrays are empty.
- Sort arrays by stable user-facing order when available, otherwise by `updatedAtMillis` descending and then id.
- De-duplicate arrays before writing.
- Redact unsafe fields rather than writing placeholders that resemble real credentials or URIs.
- Add a warning entry when a document is exported with `UNVERIFIED_METADATA_ONLY`.

## Import Modes

All imports run in two phases:

1. Parse and validate the full profile into an import plan.
2. Apply the plan atomically.

No local state may mutate during phase 1. If phase 2 fails, the app must preserve previous local state and show a visible failure. Room-backed data should be written in one database transaction where possible; DataStore changes should be staged until the Room transaction is ready to commit.

### Merge

`merge` is the default mode.

Default merge behavior:

- Preserve the current device `profileId`.
- Preserve current active Google Drive authorization and current active local autosave destination.
- Import new user links.
- Update a user link only when `contentId` matches or `normalizedUrl` matches.
- Import document metadata, but mark imported user documents `MISSING_FILE_NEEDS_REATTACH` unless the current device can verify the fingerprint.
- Import reading progress only for content that exists locally, is imported in the same profile, or becomes verified during reattachment.
- Union `reactivatedCompletedContentIds`.
- Preserve local settings by default.

Field-level merge winner rules:

User links:

- Match by `contentId`; if no match, match by `normalizedUrl`.
- If both keys match the same local row, merge into that row.
- If `contentId` and `normalizedUrl` match different local rows, abort before mutation with `CONTENT_ID_SECONDARY_KEY_CONFLICT`.
- Existing local `title`, `description`, `durationMinutes`, `topicTags`, `availability`, and `sourceLabel` win by default.
- Imported mutable fields replace local fields only when the user enables `Apply imported library metadata`.
- `createdAtMillis` remains the earlier of local/imported timestamps.
- `updatedAtMillis` becomes the later of local/imported timestamps only when imported metadata is applied; otherwise local `updatedAtMillis` stays unchanged.

User documents:

- Match by `contentId`; if no match, match by verified `documentFingerprint.sha256`.
- If `contentId` and fingerprint match different local rows, abort before mutation with `CONTENT_ID_SECONDARY_KEY_CONFLICT`.
- Existing local `displayName`, `mimeType`, `documentFormat`, `title`, `description`, `durationMinutes`, `topicTags`, and `availability` win by default.
- Imported metadata replaces local metadata only when the user enables `Apply imported library metadata`.
- Imported documents that do not match a local verified file are created as `MISSING_FILE_NEEDS_REATTACH`.
- `createdAtMillis` remains the earlier of local/imported timestamps.
- `updatedAtMillis` becomes the later timestamp only when imported metadata is applied; otherwise local `updatedAtMillis` stays unchanged.

Reading progress:

- Match by `contentId` only.
- If both local and imported progress exist, keep the local progress by default.
- Imported progress replaces local progress only when the user enables `Apply imported reading progress`.
- If imported progress is applied, choose the record with the later `updatedAtMillis`.
- If timestamps are equal, choose the record with completed state over unfinished state.
- If both are unfinished and timestamps are equal, choose the higher `progressPercent`.
- Dormant progress for missing documents may be stored only as an import-plan pending value and must not drive recommendations or reader navigation until the document is verified.

Settings:

- Local settings always win unless the user enables `Apply imported settings`.
- When imported settings are applied, imported arrays replace local arrays after validation except `reactivatedCompletedContentIds`, which is unioned to avoid hiding user-reactivated completed content.

Every retained-local conflict must create a `CONFLICT_RETAINED_LOCAL_VALUE` warning in the import result.

Settings are applied in merge mode only when the user explicitly chooses `Apply imported settings`. When enabled, imported settings replace the portable settings object atomically after validation, including `readerFontScale`. Selected distracting app package names that are unsupported by the current app build are retained in the profile preview but not activated.

### Replace

`replace` is destructive and must require explicit confirmation.

Replace behavior:

- Show a preview of affected sections before confirmation.
- Create or offer a timestamped pre-replace export before mutation.
- Replace portable settings.
- Replace user links.
- Replace user-document metadata.
- Replace reading progress for imported content.
- Replace `reactivatedCompletedContentIds` and `priorityContentIds`.
- Adopt the imported `profileId` only after explicit confirmation.
- Preserve current Google authorization, because cloud authorization must be revoked through the proper disconnect flow.
- Preserve local annotations unless the user explicitly chooses an annotation sidecar restore flow; schema v1 does not delete annotations in replace mode.
- Mark imported documents missing until file reattachment/fingerprint verification succeeds.

Replace must not delete editorial starter packs, app assets, permissions, current Drive grants, or annotation sidecar files.

## Validation Rules

Importer must:

- Require `schemaVersion` to be a positive integer and `<= supportedSchemaVersion`.
- Reject unsupported future schema versions.
- Require all top-level sections.
- Require `app.profileFormat = "quality-alternative-account-light"`.
- Reject malformed JSON.
- Reject missing required fields.
- Reject invalid primitive types.
- Reject invalid enum values.
- Reject invalid timestamp ranges.
- Reject invalid reading progress bounds.
- Validate user links with the same URL rules as manual link add.
- Validate topic tags and content priority values.
- Validate duplicate content ids as errors within the same section.
- Validate all `contentId` values against the stable content-id regex.
- Reject `contentId` prefix/type mismatches, such as a `user-document-` id in `library.userLinks`.
- Reject `warnings[]` entries with unknown codes, unknown sections, unsafe message contents, or invalid content ids.
- Reject `CONTENT_ID_SECONDARY_KEY_CONFLICT` cases before mutation.
- Treat duplicate scalar arrays as warnings and de-duplicate.
- Treat unknown fields as warnings for supported schema versions.
- Complete validation before mutation.

Older supported schema versions may be migrated into the current in-memory import plan before validation. Schema v1 has no older migration.

## Conflict Rules

- User links match by `contentId` first, then `normalizedUrl`.
- User documents match by `contentId` first, then by matching verified `documentFingerprint.sha256`.
- Field-level conflict winners are defined in the Merge section and must be applied consistently in dry-run preview and actual import.
- Display name alone is never enough to match a document.
- Raw URI is never used for cross-device matching because raw URI is not exported.
- Reading progress imports only when the matching content id exists or is imported in the same profile.
- Imported progress for missing document files is retained as dormant state but cannot drive reader navigation until the document is verified or explicitly reattached.
- Local Google Drive connection always wins over imported sync metadata.
- Imported autosave metadata never activates autosave without local user action.

## Security And Privacy

The file is intentionally readable JSON. Settings UI must tell users that the profile may contain personal reading preferences, saved links, document titles, source titles, and reading progress.

Encryption is not required for schema v1 because there is no server account or recovery key model. If encryption is later added, it should be an explicit user-controlled export option rather than an invisible default that can strand users without a recovery path.

## Visual Requirements For Implementation Slices

Implementation slices that add UI must capture screenshots for:

- Settings Account Light entry.
- Export action and export success.
- Export failure.
- Import entry point.
- Merge import preview.
- Replace import confirmation.
- Invalid import.
- Unsupported future schema.
- Import success.
- Missing-document state after import.
- Reattach-document flow when implemented.
- Autosave active/inactive/reconnect states when implemented.

## Open Questions For Later Sprints

- Whether to add optional encrypted ZIP export that includes document binaries.
- Whether to support a conflict-review screen for individual imported rows.
- Whether Account Light profile and annotation JSON-LD files should live in one Drive folder or sibling folders.
