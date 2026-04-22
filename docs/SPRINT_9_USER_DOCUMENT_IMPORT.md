# Sprint 9 User Document Import

Status: `implemented on codex/user-documents-import`

## Goal

Let testers bring owned reading material into the same finite replacement loop without turning Quality Alternative into a document manager. The user can import PDF, Markdown, or EPUB files; those files become rankable private replacement candidates alongside editorial content, link-only recommendations, user-added links, and the meditation utility.

## PRD Mapping

- `FR3 Replacement Source Setup`: user can seed owned content beyond shared packs and links.
- `FR4 Content Item Model`: imported documents normalize into rankable `ContentItem` records with format, duration, topics, source type, availability, and rights/render metadata.
- `FR5/FR6 Recommendation Flow`: user documents can appear in the existing one-primary/two-backup intervention without adding a feed or browsing surface.
- `FR8 Replacement Session Experience`: Markdown opens in the in-app private reader; PDF and EPUB use a clearly labeled Android document-viewer handoff.
- `FR12 Analytics Instrumentation`: document import and document handoff events are recorded in the local analytics ledger.

## Implemented Scope

- Android document picker entry points from Home, Library, and Add Link.
- Supported import formats:
  - PDF: saved as private external handoff content.
  - Markdown: saved as private in-app reader content.
  - EPUB: saved as private external handoff content.
- Local `Room` persistence for imported document metadata.
- Persistable read URI permission for imported files where Android grants it.
- User-private rights metadata through `ContentRightsMetadata.userPrivateReader`.
- Composite inventory merge across editorial items, user links, user documents, and meditation.
- Finite recommendation behavior preserved: exactly one primary plus up to two backups when enough inventory exists.
- Generic external-handoff analytics for PDF/EPUB so they are not mislabeled as user-added web-link fallback events.
- Inventory diagnostics include available and unavailable user document counts.

## Explicit Non-Goals

- No cloud document sync.
- No folders, tags beyond existing topic chips, search, highlights, annotations, or document management.
- No native PDF renderer in this slice.
- No native EPUB renderer in this slice.
- No text extraction, summarization, OCR, AI ingestion, or open-web crawling.
- No runtime copyright blocking; user-private content compliance remains a user/private-inventory responsibility and shared-content compliance remains selection/triage/inventory-audit time.

## Acceptance Criteria

- A user can pick a PDF, Markdown, or EPUB from Android storage.
- The app validates unsupported file types before saving.
- The user can edit title, estimated duration, and topics before saving.
- Imported documents survive app restart.
- Markdown documents open in the in-app reader.
- PDF and EPUB documents open through the external handoff surface and pass URI read permission to Android.
- Imported documents can appear in the existing three-choice intervention.
- Document import and external handoff events are logged locally.
- Existing editorial, link-only, user-link, and meditation behavior remains unchanged.

## Validation

- `git diff --check`
- `./gradlew testDebugUnitTest --no-daemon`
- `./gradlew lintDebug --no-daemon`
- `./gradlew assembleDebugAndroidTest --no-daemon`
- `./gradlew connectedDebugAndroidTest --no-daemon` on `qaApi36`

## Follow-Up Options

- Add native PDF reading only if tester behavior shows PDF handoff is too jarring.
- Add native EPUB reading only after PDF/Markdown value is proven.
- Add a tester-facing manual smoke script for importing one Markdown file and one PDF on a physical device before external pilot signoff.
