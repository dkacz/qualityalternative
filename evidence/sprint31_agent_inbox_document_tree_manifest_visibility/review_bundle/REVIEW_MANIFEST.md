# Sprint 31 Review Bundle: Agent Inbox Google Drive Folder Manifest Visibility

## Review Goal

Audit the fix for the false `Package is missing manifest.json` state shown for Agent Inbox packages selected through Android's Google Drive document-tree provider.

## User-Visible Bug

The app showed multiple Google Drive package folders as `NEEDS PACKAGE CLEANUP` with `Package is missing manifest.json`, although direct Google Drive inspection showed each package folder has direct children `manifest.json`, `content.md`, and one image attachment.

## Drive Evidence

- Agent Inbox parent folder ID inspected through Google Drive API: `10adaGo_eN3Pnb-FplpkJfxF4cNJCXiz-`.
- Six package folders were visible under that parent.
- Example package folder `1fmgakiA2qRfUtSMavPpi7pcnhxukxmsM` (`hegel-maybee-kant-obiektywnosc-a-intersubiektywnosc`) had direct children:
  - `manifest.json`, file ID `1Dv5P7PcS8fxd-g2wBHiK2M5Q9cDIs1rQ`, MIME `application/json`.
  - `content.md`, file ID `1YII1FcXTLPMB-Ri0pLv-n1gYfN6Kv-aZ`.
  - one image attachment.
- Fetching the example manifest returned valid package JSON with `schemaVersion`, `title`, `topics`, `contentFile`, `format`, `rightsClass`, `sourceLabel`, `documentSha256`, and `createdAt`.

## Intended Fix

- Preserve ordinary Android document-tree folders as local, no-Google-token imports.
- Detect Android document-tree URIs backed by Google Drive (`content://com.google.android.apps.docs.../tree/...`).
- For those Google Drive document-tree folders, require Drive read access, extract the Drive folder ID from the tree document ID (`doc=<folderId>`), and scan packages via the existing Drive API client.
- Preserve the original `content://...` folder URI in app settings so Android permission release and UI state remain coherent.
- Use Drive API file IDs for scanned package files, so import downloads content through the same authorized Drive path.

## Evidence Included

- `CURRENT_DIFF.patch`: current implementation diff.
- `src/main/...`: changed production files plus the existing Drive client for context.
- `src/test/...`: new hybrid routing tests and relevant existing ViewModel/auth tests.
- `build_reports/testDebugUnitTest`: XML results from the passing unit test run.
- `build_reports/lint-results-debug.html`: passing lint report.
- `docs/LANE_STATUS.md`: running project lane notes, including the direct Drive package-shape triage.

## Commands Run

```bash
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home ./gradlew testDebugUnitTest --tests com.qualityalternative.app.data.AndroidHybridAgentInboxDriveClientTest --tests com.qualityalternative.app.ui.MainViewModelTest --tests com.qualityalternative.app.ui.GoogleDriveAuthorizationUiTest --tests com.qualityalternative.app.ui.GoogleDriveAuthorizationTest
```

Result: pass.

```bash
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home ./gradlew testDebugUnitTest lintDebug
```

Result: pass.

## Known Local Gap

Connected visual/e2e tests were not run in this session because `adb devices -l` showed no connected device, and the machine has saved AVD definitions but no accessible Android emulator binary in the usual SDK paths.
