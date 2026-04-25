# Sprint 9 Content Source Expansion

Status: Slice 9.3 implemented for Pro review.

## Goal

Build a Pro-reviewable pool of 100 new shared editorial content candidates before integrating more items into `starter_packs.json`.

This sprint is sourcing and triage first, integration second. A candidate is not app content until it has passed source verification, rights classification, Android suitability review, and Pro review.

## Scope Mapping

- `PRD.md` Content Strategy: grow a bounded inventory without introducing a feed.
- `PRD.md` Content Rights and Rendering Policy: classify every shared item as renderable or link-only before integration.
- `PRD.md` Editorial Inventory Requirement: move beyond the current 45 editorial items toward a broader, better-balanced catalog.

## Non-Goals

- No new app runtime behavior.
- No new recommendation ranking behavior in Slice 9.0.
- No runtime scraping, rehosting, reader-mode extraction, or copyright blocking.
- No user-private links, PDFs, Markdown, or EPUBs in the shared editorial sourcing pool.
- No integration of new candidates into the APK until the candidate pool and at least one integration pack pass review.

## Slice Plan

| Slice | Output | Pro gate |
|---|---|---|
| 9.0 Baseline, schema, taxonomy | Backlog schema, empty new-candidate backlog, current inventory audit, topic taxonomy decision, source-family caps | Pro approves structure before sourcing starts |
| 9.1 Attention reset and practical agency | 24 candidates | Pro approves quality, rights posture, and no-feed fit |
| 9.2 Embodied reset and calm philosophy | 20 candidates | Pro approves respectful framing and rejects health/spiritual overreach |
| 9.3 Wonder, curiosity, and science | 26 candidates | Pro approves durability and chart/image dependency handling |
| 9.4 Long-view thinking and history/culture | 22 candidates | Pro approves tone, evergreen fit, and cultural sensitivity |
| 9.5 Creativity/play and balance pass | 8 candidates plus distribution repair | Pro approves final 100-row distribution |
| 9.6 Rights/policy/Android readiness | Final Pro-ready candidate pool and cut log | Pro approves future integration planning |

## Slice 9.0 Artifacts

- `docs/content-sourcing/content_candidate_backlog.schema.json`
- `docs/content-sourcing/content_candidate_backlog.csv`
- `docs/content-sourcing/existing_inventory_audit.csv`
- `docs/content-sourcing/existing_inventory_audit.md`
- `docs/content-sourcing/topic_taxonomy_decision.md`
- `docs/content-sourcing/source_family_caps.md`

Slice 9.0 Pro result: `10/10 PASS` after the Long Now audit taxonomy fix and validation transcript addition.

## Slice 9.1 Artifacts

- `docs/content-sourcing/content_candidate_backlog.csv`
- `docs/content-sourcing/slice9_1_attention_practical_summary.md`

Slice 9.1 adds 24 sourcing-only candidate rows for Attention Reset and Practical Agency:

- 9 renderable candidates marked `rights_pending`.
- 15 link-only candidates marked `EXTERNAL_HANDOFF`.
- 0 new app content integrations.
- 0 runtime scraping, caching, rehosting, reader-mode extraction, or AI summary behavior.

Slice 9.1 Pro result: `10/10 PASS` after the source-risk metadata fix.

## Slice 9.2 Artifacts

- `docs/content-sourcing/content_candidate_backlog.csv`
- `docs/content-sourcing/slice9_2_embodied_calm_summary.md`

Slice 9.2 adds 20 sourcing-only candidate rows for Embodied Reset and Calm Philosophy:

- 8 renderable candidates marked `rights_pending`.
- 12 link-only candidates marked `EXTERNAL_HANDOFF`.
- The initial review duplicate of an already-integrated Marcus Aurelius source URL was replaced with a non-overlapping Confucius/Analects candidate.
- 0 new `Aeon/Psyche` rows because Slice 9.1 already reached that full-pool cap.
- 0 new app content integrations.
- 0 runtime scraping, caching, rehosting, reader-mode extraction, or AI summary behavior.

Slice 9.2 Pro result: `10/10 PASS` after the duplicate-source replacement fix.

## Slice 9.3 Artifacts

- `docs/content-sourcing/content_candidate_backlog.csv`
- `docs/content-sourcing/slice9_3_wonder_science_summary.md`

Slice 9.3 adds 26 sourcing-only candidate rows for Wonder, Curiosity, and Science:

- 11 renderable Project Gutenberg candidates marked `rights_pending`.
- 15 link-only candidates marked `EXTERNAL_HANDOFF`.
- 14 rows map to `WONDER_CURIOSITY`; 12 rows map to `SCIENCE_CURIOSITY`.
- Chart-heavy, image-dependent, and recent-research candidates stay link-only or require text-only excerpt selection.
- 0 new app content integrations.
- 0 runtime scraping, caching, rehosting, reader-mode extraction, or AI summary behavior.

## Current Inventory Baseline

- 5 editorial packs.
- 45 editorial items.
- 25 renderable in-app reader items.
- 20 link-only external handoff items.
- 70 new Sprint 9 candidates added through Slice 9.3.
- 28 renderable candidate rows.
- 42 link-only candidate rows.

The current 45 items are audited as `already_integrated` and explicitly do not count toward the new 100-candidate pool.

## Target Pool

- 100 new shared editorial candidates.
- Default mix: 42 renderable candidates and 58 link-only candidates.
- Current remaining target after Slice 9.3: 30 candidates, with 14 renderable candidates and 16 link-only candidates.
- Default duration mix: 40 candidates at 3-5 minutes, 40 at 5-10 minutes, 20 at 10-20 minutes.
- Every link-only candidate must keep `must_not_scrape_cache_or_summarize = true`.
- Every renderable candidate must include rights basis, attribution draft, jurisdiction note, and edition/translation note before it can become Pro-ready.

## Validation

- `content_candidate_backlog.schema.json` parses as JSON.
- `content_candidate_backlog.csv` header matches the schema field order.
- `existing_inventory_audit.csv` records 45 already-integrated items and matches the current inventory count.
- Unit guard: `ContentSourceExpansionArtifactsTest`.
