# Quality Alternative v0.11.12-markdown-media-tables-alpha

This release follows `v0.11.11-bedtime-hard-ban-alpha` and ships Sprint 25 Markdown media/table reader support.

## What Changed

- Markdown reader documents now render embedded images from relative files, selected image attachments, and supported data URI sources instead of showing raw `![alt](...)` syntax.
- Markdown pipe tables now render as structured reader tables with header styling, alignment metadata, and horizontal scrolling for wide tables.
- Reader pagination and reading-time estimation now ignore Markdown image payload noise and table delimiter syntax.
- Table pagination now measures wrapped table-cell text and splits oversized table rows by visual weight.
- Android document import now maps image files selected beside Markdown files into the Markdown document attachment map instead of saving those images as separate broken library entries.
- Room schema 15 persists Markdown image attachment URI maps via `user_documents.imageAttachmentUrisJson`.
- Reader gesture handling now protects wide-table horizontal scrolling without breaking ordinary text tap/swipe page navigation.

## Review And Validation

- GPT Pro Sprint 25 R3 review: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`, blockers `None`.
- JVM validation: `:app:testDebugUnitTest` and `:app:lintDebug` passed.
- Connected Android validation on `qaApi36(AVD) - 16`: 18/18 passing for Markdown media/table screenshots, wide-table scroll behavior, ordinary text tap/swipe navigation, Room repository persistence, and Room migration.
- Visual evidence: `evidence/sprint25_markdown_media_tables/screenshots-r3/contact_sheet_r3.png`.
- APK evidence includes `versionCode=28`, `versionName=0.11.12-alpha`, signature verification, SHA-256 hash, emulator install, and cold launch focus on `MainActivity`.

## Changelog Versus `v0.11.11-bedtime-hard-ban-alpha`

- Keeps the Sprint 24 Bedtime hard-ban behavior unchanged.
- Adds Markdown image rendering and persisted Markdown image attachments for private reader documents.
- Adds structured Markdown table rendering with wide-table scrolling and pagination-aware table measurement.
- Adds regression coverage proving table gesture handling does not steal normal reader text navigation.
- Adds schema migration and import-flow coverage for Markdown documents with sibling image assets.
