# Sprint 6 Content Governance and Starter Library

Status: `in progress`

## Goal

Create the content foundation required before scaling PDFs, premium packaging, or a broader curated library. Sprint 6 should make it explicit which content can be rendered inside the app, which content is link-only, and which content belongs only to the user who added it.

The sprint should protect the product thesis: replacement quality matters, but the app must not become a scraper, feed, or legally ambiguous read-later clone.

## PRD Mapping

- `FR3 Replacement Source Setup`: replacement sources must distinguish editorial, user-owned, and external content.
- `FR4 Content Item Model`: content items need rights-aware metadata before the inventory grows.
- `FR5/FR6 Recommendation Flow`: recommendations can include renderable items and link-only handoffs without expanding beyond one primary plus two backups.
- `FR8 Replacement Session Experience`: the reader/handoff must follow explicit inventory metadata; copyright compliance is handled during content selection, triage, and audit, not by hidden runtime blocking.
- `FR12 Analytics Instrumentation`: analytics should distinguish renderable editorial, link-only handoff, and user-private content.
- `NFR2 Calm Interaction Model`: content sourcing must not introduce feeds, discovery browsing, or infinite lists.

## Scope

In scope:

- Adopt `docs/CONTENT_POLICY.md` as the operating policy for content sourcing.
- Add explicit rights/render metadata to content models and starter-pack data.
- Audit current starter-pack metadata for misleading source labels.
- Reclassify current synthetic starter items as Quality Alternative editorial placeholders unless a real license/source exists.
- Define `renderable`, `link_only`, and `user_private` paths in code and UI copy.
- Add an attribution ledger format for public-domain and Creative Commons renderable content.
- Add a small pilot set of verified renderable public-domain or CC-safe items if source verification is straightforward.
- Keep user-added web links as link-only/external handoff unless the user provides private content.

Out of scope:

- PDF upload or PDF reader.
- EPUB support.
- RSS/newsletter ingestion.
- Open-web crawling.
- AI summarization of third-party works.
- Monetization implementation.
- Licensed publisher partnerships.
- Legal terms generation.

## Slices

### 6.1 Policy and Rights Model

Acceptance criteria:

- `docs/CONTENT_POLICY.md` is linked from planning docs.
- Domain models can represent rights class and render mode.
- Existing tests still pass without changing current recommendation behavior.
- No content becomes renderable by default unless it has explicit metadata.

Test expectations:

- Unit tests cover render-mode helpers without treating rights class as a runtime blocker.
- Unit tests cover safe defaults for missing rights metadata.

### 6.2 Starter Inventory Audit

Acceptance criteria:

- Existing starter-pack items no longer imply affiliation with real publications unless actually licensed.
- Starter-pack JSON carries source and rights metadata.
- In-app labels distinguish Quality Alternative editorial placeholders from user links and external sources.
- Analytics can still distinguish editorial from user-added links.

Test expectations:

- Asset loader tests cover required rights fields.
- UI/copy tests cover source labels for editorial versus link-only content.

### 6.3 Attribution Ledger and Safe Renderable Pack

Acceptance criteria:

- A repo-local attribution ledger exists for any public-domain or CC-safe renderable items.
- Each included item has source URL, author, license/status, review date, and edition note.
- If a safe pilot pack is added, it contains only verified renderable content.
- If verification is not complete, no new public-domain pack is shipped and the sprint records the blocker explicitly.

Test expectations:

- Asset validation test fails if a renderable item lacks attribution metadata.
- Regression test confirms finite recommendation set remains capped at one primary plus two backups.

### 6.4 Link-Only Guardrails

Acceptance criteria:

- Curated external recommendations and user-added web links remain external handoff unless separately cleared.
- The app does not scrape, cache, or reader-mode third-party web pages as part of shared editorial inventory.
- Handoff copy makes it clear when the user is leaving the in-app reader.
- Analytics distinguishes `renderable_started`, `link_handoff_started`, and `user_private_started` semantics.

Test expectations:

- ViewModel tests cover link-only accept path.
- Analytics tests cover rights-aware event metadata.
- Connected smoke confirms link-only handoff remains stable.

## Validation

Automated:

- `./gradlew testDebugUnitTest --no-daemon`
- `./gradlew lintDebug --no-daemon`
- `./gradlew connectedDebugAndroidTest --no-daemon`

Review:

- Pro review after each meaningful slice, with explicit checks for copyright/source-risk, scope drift, and accidental feed behavior.

Release stance:

- Sprint 6 can ship as a content-governance release only after the starter library no longer carries misleading source labels and all automated validation passes.
- PDF work should not start until the content triage and attribution model is stable.
