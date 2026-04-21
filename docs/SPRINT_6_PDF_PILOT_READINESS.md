# Deferred Sprint: PDF and Pilot Readiness

Status: `planned after Sprint 6 Content Governance`

Note: this plan was originally drafted as Sprint 6. It is intentionally deferred until the content rights/rendering model from `docs/SPRINT_6_CONTENT_GOVERNANCE.md` is implemented, because PDFs should enter the product as `user_private` content rather than shared editorial inventory.

## Goal

Move the Android alpha from editorial packs plus user-added links toward a stronger external pilot by adding the first local PDF path. The sprint should keep the product thesis intact: PDFs become finite replacement candidates inside the existing one-primary/two-backup intervention, not a document manager, cloud library, or feed.

## PRD Mapping

- `FR3 Replacement Source Setup`: user can seed owned content beyond starter packs and links.
- `FR4 Content Item Model`: PDF items normalize into rankable `ContentItem` equivalents.
- `FR5/FR6 Recommendation Flow`: PDF items can appear inside the existing finite recommendation set.
- `FR8 Replacement Session Experience`: PDF reading or handoff must be calm, bounded, and clearly labeled.
- `FR12 Analytics Instrumentation`: analytics distinguishes editorial, link, and PDF replacements.
- `NFR2 Calm Interaction Model`: no infinite library browsing, no shame copy, no blocker-first drift.

## Scope

In scope:

- Android document picker for `application/pdf`.
- Persisted local URI permission where Android grants it.
- Minimal PDF metadata capture: title, estimated duration, topics, source type, availability.
- PDF inventory merged into the existing local recommendation repository.
- PDF reader or controlled external handoff, whichever is technically safer for alpha.
- Source-aware analytics for add, recommend, accept, complete/skip, handoff failure if applicable.
- Tester-facing instructions for adding and validating one PDF.

Out of scope:

- PDF annotation, highlights, search, folders, or document management.
- PDF text extraction as a prerequisite for recommendation.
- AI summarization or embeddings.
- Cloud sync or account system.
- EPUB support.
- iOS.
- Premium limits or monetization.

## Slices

### 6.1 PDF Domain and Persistence

Acceptance criteria:

- Domain can represent a local PDF as a source-aware content item.
- Room persists PDF references and availability state.
- Repository APIs can list, add, and mark PDF items unavailable.
- Existing editorial and user-link inventory behavior remains unchanged.

### 6.2 Add PDF Flow

Acceptance criteria:

- User can start an `Add PDF` flow from the library surface.
- Android picker accepts PDF files only.
- User can add title, estimated duration, and topics.
- Invalid or missing metadata is handled locally and non-moralizingly.

### 6.3 Recommendation Integration

Acceptance criteria:

- PDF items participate in ranking alongside editorial items and user links.
- Completed PDFs are excluded from future primary recommendations.
- Recommendation cards clearly distinguish PDF, saved link, and editorial sources.
- The intervention remains one primary plus up to two backups.

### 6.4 PDF Read/Handoff and Pilot Polish

Acceptance criteria:

- Accepting a PDF opens a finite reader or clearly labeled handoff.
- Completion, skip, feedback, and return behavior record the correct analytics.
- Tester docs explain how to validate one PDF without needing a backend.
- External pilot checklist is updated for editorial, links, and PDFs.

## Validation

Automated:

- `./gradlew testDebugUnitTest --no-daemon`
- `./gradlew lintDebug --no-daemon`
- `./gradlew connectedDebugAndroidTest --no-daemon`

Review:

- Pro review after each meaningful slice, with a strict scope-drift check against PDF becoming document management or AI ingestion.

Release stance:

- This deferred PDF sprint may produce a pilot-readiness APK only after emulator validation and Pro review pass.
- Real-device validation should cover at least one local PDF add/open path before any external pilot signoff.
