# Agent Inbox Package Authoring Contract

This document is for AI agents and automation that prepare private replacement
content for Quality Alternative through the Agent Inbox.

The contract is intentionally local and portable. It does not assume the
operator's machine, rclone config, Google account, Drive folder id, or absolute
filesystem paths.

The destination Agent Inbox folder is runtime input from the operator. If the
operator has not supplied a destination, prepare and validate the local package
only; do not guess a Drive folder, remote name, account, or path.

## What The App Expects

The user selects an Agent Inbox folder in the Android app. Your automation writes
package folders as direct children of that selected folder.

One package is one folder. Do not upload a zip as the package.

```text
Agent Inbox/
  my-package-folder/
    manifest.json
    content.md
```

For Markdown packages, a small bounded set of image sidecars is allowed:

```text
Agent Inbox/
  my-markdown-package/
    manifest.json
    content.md
    cover.png
    chart-1.webp
```

EPUB packages cannot include sidecars:

```text
Agent Inbox/
  my-epub-package/
    manifest.json
    book.epub
```

## Required Manifest

`manifest.json` must be UTF-8 JSON and smaller than 64 KiB.

```json
{
  "schemaVersion": 1,
  "title": "Normatywna gra u Kanta",
  "topics": ["PHILOSOPHY"],
  "contentFile": "content.md",
  "format": "MARKDOWN",
  "rightsClass": "USER_PRIVATE",
  "sourceLabel": "Codex",
  "description": "Private replacement reading prepared by an agent.",
  "priority": "normal",
  "createdAt": "2026-06-15T10:00:00Z"
}
```

Required fields:

- `schemaVersion`: must be `1`.
- `title`: non-empty display title.
- `topics`: non-empty array using the app topic ids listed below.
- `contentFile`: exact filename of the one Markdown or EPUB file in the package.
- `format`: `MARKDOWN` or `EPUB`; must match the content filename extension.
- `rightsClass`: must be `USER_PRIVATE`.

Optional but recommended fields:

- `sourceLabel`: short safe producer label, for example `Codex` or `Claude`.
- `description`: short review summary shown before import.
- `priority`: `normal` or `high`; `high` is only a request and the user must
  explicitly accept it in the app.
- `documentSha256`: lowercase SHA-256 of the content file bytes. Strongly
  recommended because it lets the app detect changed content before import.
  Never upload a placeholder value; either omit this field or set it to the
  exact `contentSha256=` value printed by the validator.
- `createdAt`: ISO-like timestamp for auditability.

Allowed topic ids:

```text
ATTENTION
PRACTICAL
BODY
NATURE
HISTORY_CULTURE
ESSAYS
PHILOSOPHY
SCIENCE
DESIGN
POETRY
HISTORY
TECH
FICTION
CLIMATE
ECONOMICS
FOOD
ARCHITECTURE
CREATIVITY
PSYCHOLOGY
OTHER
```

## File Rules

- Keep all files directly inside the package folder. Do not use nested folders.
- Use exactly one `manifest.json`.
- Use exactly one content file:
  - Markdown: `.md` or `.markdown`
  - EPUB: `.epub`
- Keep the content file at or below 10 MiB.
- Keep the whole package file listing at or below 8 direct files.
- Do not modify package files while the app is scanning or importing. If content
  changes after review, the app rejects the import and the user must scan again.
- Do not include raw Drive ids, local absolute paths, tokens, or user-specific
  machine paths in the manifest.

Markdown image sidecars:

- Allowed extensions: `.png`, `.jpg`, `.jpeg`, `.webp`, `.gif`, `.bmp`.
- Maximum 6 image files.
- Maximum 5 MiB per image.
- Maximum 15 MiB total image bytes.
- Image filenames must be unique case-insensitively and after safe-name cleanup.

EPUB sidecars:

- Not supported. An EPUB package must contain only `manifest.json` and the EPUB
  content file.

## Validate Before Upload

Run this from the repository root before uploading or syncing a package folder:

```bash
python3 tools/validate_agent_inbox_package.py /path/to/package-folder
```

The validator checks the same package shape and manifest rules that the Android
app expects. A package that fails validation should not be uploaded.

## Upload Contract

After validation passes, upload the package folder as a direct child of the
folder that the user selected in the Android app.

Do not assume a global Drive scope. Current app releases use Android's folder
picker as the primary folder selector. When the selected tree is backed by
Google Drive, the app may request explicit Drive read consent, but it still
scans only the selected Agent Inbox folder id.

Do not upload partially built packages. Build the complete folder locally first,
including `manifest.json`, run the validator, and only then copy or sync that
complete package folder to the selected Agent Inbox folder. Avoid creating an
empty package folder in Drive and filling it file-by-file while the app may scan;
that can produce temporary `Package is missing manifest.json` or `Package
changed` states.

Any sync tool is acceptable if it preserves this folder shape:

```text
selected-agent-inbox-folder/
  validated-package-folder/
    manifest.json
    content.md
```

## If The App Shows "Needs Package Cleanup"

The app has already seen the package, so this is not a folder-connection problem.
It means the package cannot be imported safely.

Common causes:

- missing or malformed `manifest.json`
- manifest `contentFile` does not match the actual filename
- multiple Markdown/EPUB files in one package
- unsupported extra files
- `documentSha256` does not match the content bytes
- package files changed after the app reviewed them
- content or image sidecars exceed the size limits
- local import failed after review, which the current UI reports as `Package
  could not be saved`

Fix by removing the failed package folder, generating a fresh folder, running
the validator again, uploading the fresh folder, and scanning again in Settings.
