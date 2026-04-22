# Sprint 7 Source Discovery and Content Library v1

Status: `in progress`

## Goal

Build the first real replacement library from existing English-language sources. Sprint 7 should move the product from "we have a working intervention shell" toward "the app can reliably offer something worth choosing instead of social media."

This sprint is intentionally content-first. It should not become another governance sprint. The governance work from Sprint 6 is used only as a lightweight intake checklist.

## Product Direction

- Language: English.
- We are not writing first-party essays for this sprint.
- Content should come from existing sources that can be classified as either `renderable` or `link_only`.
- Runtime must not enforce copyright policy. Source and rights checks happen during source selection, triage, and inventory audit.
- The recommendation surface remains finite: one primary recommendation plus two backups.

## PRD Mapping

- `FR3 Replacement Source Setup`: identify real shared sources without adding crawler, RSS, PDF, or heavy ingestion scope.
- `FR4 Content Item Model`: every selected item must be able to carry title, source, rights, attribution, duration, format, topic tags, and render mode.
- `FR5/FR6 Recommendation Flow`: new content must still enter the existing one-primary, two-backup intervention shape.
- `FR8 Replacement Session Experience`: selected items should open in the in-app reader only when `renderable`; otherwise they should use external handoff.
- `NFR2 Calm Interaction Model`: source discovery must not create a browsing feed, discovery surface, or infinite content list.

## Source Strategy

Primary renderable candidates:

- Standard Ebooks public-domain classics and essays, with EU/Poland author/translator checks before app rendering.
- Our World in Data text-first explainers under CC BY, with careful handling of third-party charts/data.
- NASA Earth Observatory / NOAA material where official reuse rules allow reuse, attribution, and no implied endorsement.

Primary link-only candidates:

- SAPIENS articles under CC BY-ND or article-specific republication rules.
- The Conversation and similar outlets where republishing rules are article-specific or NoDerivatives.
- High-quality modern publications that are worth recommending but not reproducing.

Rejected for Sprint 7:

- Paywalled or subscriber-only articles.
- Sources requiring scraping, crawler behavior, or reader-mode extraction.
- Works with unclear rights status.
- NonCommercial materials for shared renderable inventory.
- Anything that forces the product into legal or editorial uncertainty before we know if users value the replacement loop.

## Slices

### 7.1 Source Candidate Discovery

Status: `passed Pro review`

Acceptance criteria:

- Create a repo-local source candidate document with 30-50 realistic candidates.
- Classify each candidate as `renderable_candidate`, `link_only_candidate`, or `reject_for_now`.
- Include a product-fit note for why the item could replace a social impulse.
- Include a next triage action for every candidate.
- No app content assets are changed in this slice.

Validation:

- Docs review.
- Pro review focused on source quality, legal/rights risk, and product fit.

### 7.2 First Renderable Pack Selection

Status: `selected for Pro review`

Acceptance criteria:

- Select one initial pack from the candidate list.
- Target 8-12 items.
- Prefer short, modular works that can be consumed in 5-12 minutes.
- Each item has title, source URL, author/source, license/status, proposed duration, topic tags, and triage status.
- If no source is safe enough to render, produce a `link_only` pack instead of forcing it.

Validation:

- Candidate pack review.
- Pro review focused on whether the pack is compelling enough to test as replacement content.

Current selection:

- See `docs/CONTENT_PACK_V1_SELECTION.md`.

### 7.3 App Integration for Pack v1

Acceptance criteria:

- Add the selected pack to local assets.
- Preserve rights/source metadata.
- Keep recommendation set finite.
- Do not add crawler, RSS, PDF, summarization, or network fetching.
- Add tests for asset metadata and recommendation eligibility.

Validation:

- `./gradlew testDebugUnitTest --no-daemon`
- `./gradlew lintDebug --no-daemon`
- `./gradlew connectedDebugAndroidTest --no-daemon`
- Pro review focused on code, content metadata, and no scope drift.

### 7.4 Replacement Quality Pass

Acceptance criteria:

- Review pack titles/descriptions for clickability at the moment of impulse.
- Ensure every item has a concrete "why this now?" reason.
- Remove weak items rather than padding the count.
- Update tester notes with the content-quality question: "Would I choose this instead of opening the target app?"

Validation:

- Pro review focused on editorial/product quality.

## Definition of Done

Sprint 7 is done when:

- At least one real content pack is integrated or a clear blocker is documented.
- The pack is sourced from existing English material, not first-party writing.
- Every included item has source and rights metadata.
- The app still offers a finite replacement set.
- Automated validation passes for any app-code changes.
- Pro review passes for each slice.
