# Sprint 9 Content Source Expansion

Status: Android content integration implemented; fifth GPT Pro visual remediation passed GPT Pro review.

## Goal

Build, review, integrate, and test 100 new bounded shared editorial alternatives without turning the Android MVP into a feed.

The Sprint 9 source backlog remains the pre-integration sourcing audit trail. Final release readiness is reconciled row-by-row in `docs/content-sourcing/final_release_approval_20260426.csv`. The Android integration output is `starter_packs.json` plus 42 local Markdown body assets for renderable Project Gutenberg items; modern or unclear-rights rows remain link-only external handoff items.

## Scope Mapping

- `PRD.md` Content Strategy: grow a bounded inventory without introducing a feed.
- `PRD.md` Content Rights and Rendering Policy: classify every shared item as renderable or link-only before integration.
- `PRD.md` Editorial Inventory Requirement: move beyond the current 45 editorial items toward a broader, better-balanced catalog.

## Non-Goals

- No new app runtime behavior beyond adding bounded starter-pack inventory.
- No new recommendation ranking behavior in Slice 9.0.
- No runtime scraping, rehosting, reader-mode extraction, or copyright blocking.
- No user-private links, PDFs, Markdown, or EPUBs in the shared editorial sourcing pool.
- No runtime reproduction of modern link-only article bodies in app assets.

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
| 9.7 Android content integration and QA | 5 starter packs, 100 app items, 42 Markdown bodies, E2E/visual QA | Gradle, instrumentation, and screenshot validation |

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
- 0 new app content integrations in this sourcing slice; Slice 9.7 later integrates the reviewed pool.
- 0 runtime scraping, caching, rehosting, reader-mode extraction, or AI summary behavior.

Slice 9.1 Pro result: `10/10 PASS` after the source-risk metadata fix.

Final distribution review repair: two Slice 9.1 renderable rows that reused canonical URLs already present in `starter_packs.json` were replaced with non-overlapping Project Gutenberg candidates:

- `s9-1-r03-betts-mind-education` replaces the prior William James row that overlapped `attention-comes-in-beats`.
- `s9-1-r04-smiles-character-agency` replaces the prior John Stuart Mill row that overlapped `choose-your-own-plan`.

## Slice 9.2 Artifacts

- `docs/content-sourcing/content_candidate_backlog.csv`
- `docs/content-sourcing/slice9_2_embodied_calm_summary.md`

Slice 9.2 adds 20 sourcing-only candidate rows for Embodied Reset and Calm Philosophy:

- 8 renderable candidates marked `rights_pending`.
- 12 link-only candidates marked `EXTERNAL_HANDOFF`.
- The initial review duplicate of an already-integrated Marcus Aurelius source URL was replaced with a non-overlapping Confucius/Analects candidate.
- 0 new `Aeon/Psyche` rows because Slice 9.1 already reached that full-pool cap.
- 0 new app content integrations in this sourcing slice; Slice 9.7 later integrates the reviewed pool.
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
- 0 new app content integrations in this sourcing slice; Slice 9.7 later integrates the reviewed pool.
- 0 runtime scraping, caching, rehosting, reader-mode extraction, or AI summary behavior.

Slice 9.3 initial Pro result: `REVISE` for metadata consistency only. The follow-up fix aligns one spider image-dependency row with its structured risk fields and aligns four Quanta open-page spot-check rows with their verification method.

## Slice 9.4 Artifacts

- `docs/content-sourcing/content_candidate_backlog.csv`
- `docs/content-sourcing/slice9_4_long_view_history_summary.md`

Slice 9.4 adds 22 sourcing-only candidate rows for Long-View Thinking and History/Culture:

- 8 renderable Project Gutenberg candidates marked `rights_pending`.
- 14 link-only candidates marked `EXTERNAL_HANDOFF`.
- 10 rows map to `LONG_VIEW`; 12 rows map to `HISTORY_CULTURE`.
- Sensitive history rows flag settler-colonial, slavery, race, religious, museum, and cross-cultural framing risks before Pro review.
- 0 new app content integrations in this sourcing slice; Slice 9.7 later integrates the reviewed pool.
- 0 runtime scraping, caching, rehosting, reader-mode extraction, or AI summary behavior.

## Slice 9.5 Artifacts

- `docs/content-sourcing/content_candidate_backlog.csv`
- `docs/content-sourcing/slice9_5_creativity_play_summary.md`

Slice 9.5 adds 8 sourcing-only candidate rows for Creativity/Play and final rights-mix repair:

- 6 renderable Project Gutenberg candidates marked `rights_pending`.
- 2 link-only candidates marked `EXTERNAL_HANDOFF`.
- All 8 rows map to `CREATIVITY_PLAY` and primary topic `CREATIVITY`.
- The 6/2 rights split is intentional: it repairs the final Sprint 9 pool to the target 42 renderable and 58 link-only rows after earlier reviewed slice mix changes.
- Craft, puzzle, image, diagram, early-film, and design-history risks are flagged before Pro review.
- 0 new app content integrations in this sourcing slice; Slice 9.7 later integrates the reviewed pool.
- 0 runtime scraping, caching, rehosting, reader-mode extraction, or AI summary behavior.

## Slice 9.6 Artifacts

- `docs/content-sourcing/pro_review_packet.md`
- `docs/content-sourcing/rights_risk_register.md`
- `docs/content-sourcing/candidate_cut_log.md`
- `docs/content-sourcing/pack_cluster_plan.md`
- `docs/content-sourcing/final_release_approval_20260426.csv`

Slice 9.6 converts the completed 100-row sourcing pool into a final-review packet:

- Documents the final 100-row, 42 renderable, 58 link-only pool.
- Separates "ready for final Pro review" from "approved for app integration."
- Keeps all 42 renderable candidates in `rights_pending` at the pre-integration sourcing stage.
- Keeps all 58 link-only candidates as metadata-only external handoff rows at the pre-integration sourcing stage.
- Records rights, sensitivity, asset, and Android-readiness risks for the next manual review pass.
- Sketches future pack clusters without approving or integrating any candidate.
- Slice 9.7 later supersedes these pre-integration statuses with `final_release_approval_20260426.csv`.

## Slice 9.7 Android Integration

- `app/src/main/assets/editorial/starter_packs.json`
- `app/src/main/assets/editorial/items/s9_*.md`
- `app/src/main/java/com/qualityalternative/app/domain/model/ContentModels.kt`
- `app/src/test/java/com/qualityalternative/app/data/ContentSourceExpansionArtifactsTest.kt`
- `app/src/androidTest/java/com/qualityalternative/app/data/AssetContentRepositoryTest.kt`
- `app/src/androidTest/java/com/qualityalternative/app/VisualQaScreenshotTest.kt`

Slice 9.7 integrates the reviewed Sprint 9 pool into the Android app:

- 5 new finite starter packs.
- 100 new Sprint 9 editorial items in `starter_packs.json`.
- 42 renderable Project Gutenberg items with local Markdown body assets.
- 58 modern or unclear-rights items as link-only external handoffs only.
- 0 modern link-only body assets, scraping, caching, rehosting, reader-mode extraction, or AI summaries.
- New topic enum values added for Sprint 9 taxonomy: `ATTENTION`, `PRACTICAL`, `BODY`, `NATURE`, `HISTORY_CULTURE`.
- New Sprint 9 packs are included in the default starter-pack selection when present, so they are reachable after normal onboarding without test-only state seeding.
- New Sprint 9 topics are exposed in the user-facing topic chip list.
- User-facing Sprint 9 descriptions and intervention copy were cleaned to remove internal release, sourcing, review, and implementation vocabulary.
- Renderable Sprint 9 body assets were trimmed so reader openings begin with substantive excerpt text instead of source prefaces, contents, translator notes, publication-history material, bibliographic footnotes, book-purpose framing, subscriber lists, dedication framing, title-page notes, or figure-list material.

## Current App Inventory

- 10 editorial packs.
- 145 editorial items.
- 67 renderable in-app reader items.
- 78 link-only external handoff items.
- 100 new Sprint 9 items integrated through Slice 9.7.
- 42 Sprint 9 renderable Markdown body assets.
- 58 Sprint 9 link-only external handoff items.

The pre-Sprint 9 inventory audit still records the original 45 already-integrated items. It is retained as the baseline overlap guard, not as the current app item count.

## Target Pool

- 100 new shared editorial candidates.
- Default mix: 42 renderable candidates and 58 link-only candidates.
- Current remaining target after Slice 9.5: 0 candidates.
- Default duration mix: 40 candidates at 3-5 minutes, 40 at 5-10 minutes, 20 at 10-20 minutes.
- Documented duration-distribution deviation for final review: the sourced pool has 6 candidates at 3-5 minutes, 63 at 6-10 minutes, and 31 at 11-20 minutes. Slice 9.7 uses excerpt-level Markdown bodies for renderable items and keeps long modern pieces link-only.
- Every link-only candidate must keep `must_not_scrape_cache_or_summarize = true`.
- Every renderable candidate must include rights basis, attribution draft, jurisdiction note, and edition/translation note before it can become Pro-ready.

## Validation

- `content_candidate_backlog.schema.json` parses as JSON.
- `content_candidate_backlog.csv` header matches the schema field order.
- `existing_inventory_audit.csv` records the 45-item pre-Sprint 9 baseline and is checked against current starter-pack source URLs for overlap control.
- `starter_packs.json` includes all 100 Sprint 9 rows as app items.
- All 42 Sprint 9 renderable items have local Markdown body assets.
- All 58 Sprint 9 link-only items have external URLs and no body assets.
- `final_release_approval_20260426.csv` reconciles all 100 Sprint 9 Android rows with final rights/content approval status and supersedes the pre-integration `rights_pending` statuses retained in the sourcing backlog.
- Unit guard: `ContentSourceExpansionArtifactsTest` plus surfaced-copy guard in `RecommendationExplainerTest`.
- Android asset guard: `AssetContentRepositoryTest`.
- Visual guard: `VisualQaScreenshotTest#captureSprint9ContentExpansionScreens`.
- Latest local validation after fifth GPT Pro visual remediation: `:app:testDebugUnitTest` passed 169 tests, `:app:connectedDebugAndroidTest` passed 54 tests on `qaApi36`, and the direct screenshot runner passed `OK (1 test)`.
- GPT Pro final visual verdict: `PASS`, harvested at `PRO_REVIEW_OUTPUT_SPRINT9_ANDROID_VISUAL_FIX5_20260426_181457/Sprint_9_Android_Audit.md`.
- Final raw logs and refreshed screenshots are preserved under `docs/visual-qa/2026-04-26-sprint9-content-expansion/`.
