# Sprint 9 Pro Review Packet

Status: final GPT Pro source review passed; Sprint 9 app integration completed; fifth post-integration GPT Pro visual remediation passed GPT Pro review.

This packet summarizes the 100-row shared editorial candidate pool produced by Sprint 9 and its Android integration. `content_candidate_backlog.csv` is the pre-integration sourcing audit trail. Final row-level release approval is recorded in `final_release_approval_20260426.csv`; the integrated app manifest is `app/src/main/assets/editorial/starter_packs.json`.

## Scope

- Android-first MVP content sourcing and bounded starter-pack integration.
- 100 new Sprint 9 `starter_packs.json` items.
- No app runtime behavior changes beyond finite inventory expansion.
- No scraping, caching, rehosting, reader-mode extraction, open-web crawling, or AI summaries.
- No link-only article body text stored in app assets.

## Canonical Counts

| Check | Count |
|---|---:|
| New Sprint 9 candidates | 100 |
| Renderable candidates | 42 |
| Link-only candidates | 58 |
| Integrated Sprint 9 starter-pack items | 100 |
| Sprint 9 Markdown body assets | 42 |
| Sprint 9 link-only external handoff items | 58 |
| Rows requiring legal review after final release approval | 0 |
| Current app inventory audit rows excluded from target | 45 |

## Slice Status

| Slice | Scope | Rows | Status |
|---|---|---:|---|
| 9.0 | Baseline, schema, taxonomy, source caps | 0 | Pro passed after taxonomy fix |
| 9.1 | Attention reset and practical agency | 24 | Pro passed after source-risk metadata fix |
| 9.2 | Embodied reset and calm philosophy | 20 | Pro passed after duplicate-source replacement fix |
| 9.3 | Wonder, curiosity, and science | 26 | Pro follow-up fix implemented |
| 9.4 | Long-view thinking and history/culture | 22 | GPT Pro passed after follow-up fix |
| 9.5 | Creativity/play and final balance repair | 8 | GPT Pro passed after follow-up fix |
| 9.6 | Rights, policy, and Android readiness packet | 100 | Final GPT Pro review passed |
| 9.7 | Android content integration and QA | 100 | Implemented; fifth visual remediation passed GPT Pro review |

## Final Pool Distribution

| Dimension | Distribution |
|---|---|
| Rights class | 42 `RENDERABLE`; 58 `LINK_ONLY` |
| Render mode | 42 `IN_APP_READER`; 58 `EXTERNAL_HANDOFF` |
| Candidate status in source backlog | Historical pre-integration audit; superseded by `final_release_approval_20260426.csv` |
| Estimated duration | 6 candidates at 3-5 minutes; 63 at 6-10 minutes; 31 at 11-20 minutes |
| Primary viability | 100 approved in final release artifact |
| App integration status | 100 integrated into finite Sprint 9 packs |
| Legal review | 100 reconciled in `final_release_approval_20260426.csv` |

## Duration Deviation

The original planning target was 40 candidates at 3-5 minutes, 40 at 5-10 minutes, and 20 at 10-20 minutes. The sourced pool does not meet that duration target. The final review packet documents this as a distribution deviation rather than silently claiming the target was met.

Rationale for accepting the deviation at final-review stage:

- Sprint 9 prioritized rights posture, source diversity, and non-feed policy classification before integration.
- All renderable rows still require manual excerpt selection, so final integration can choose shorter excerpts from longer works.
- Future 10-item pack selection should prefer shorter candidates and can cut longer rows if they do not work at impulse time.
- Slice 9.7 integrates the full reviewed pool, with renderable duration controlled by local excerpt bodies and modern long reads kept as link-only handoffs.

## Policy Gates

- Every modern or unclear-rights article remains link-only.
- Every link-only row keeps `must_not_scrape_cache_or_summarize = true`.
- Every renderable app item has a local Project Gutenberg Markdown excerpt and source metadata.
- Renderable Markdown excerpts open with substantive reader text instead of source frontmatter, translator notes, publication-history material, bibliographic footnotes, book-purpose framing, subscriber lists, dedication framing, title-page notes, or figure-list material.
- Surfaced Sprint 9 `description` and `whyThisNow` copy is guarded against internal release, review, and implementation vocabulary.
- `final_release_approval_20260426.csv` records 42 approved in-app reader rows and 58 approved link-only handoff rows.
- Modern source families remain under the 10-row full-pool cap.
- Source URLs are candidate metadata only; they do not authorize reproduction.

## Reviewer Questions

1. Are any rows weak padding rather than useful finite impulse replacements?
2. Should any renderable Project Gutenberg candidate be downgraded because image, diagram, edition, translation, jurisdiction, or sensitivity risk is too high?
3. Should any link-only page be rejected because it is too topical, gated, image-dependent, or feed-like?
4. Are the seven pack concepts in `pack_cluster_plan.md` still coherent after the full app integration?
5. Do visual QA screenshots show the Sprint 9 library, reader, and external handoff states cleanly?

## Local Test Environment

Java Runtime/JDK is available through Homebrew OpenJDK 17, and Android SDK is configured through `local.properties`. Sprint 9 validation must run through Gradle unit tests plus Android instrumentation/visual screenshot tests.

Latest local validation after the fifth GPT Pro visual remediation:

- `:app:testDebugUnitTest`: passed 169 tests, 0 failures, 0 errors, 0 skipped.
- `:app:connectedDebugAndroidTest`: passed 54 tests, 0 failures, 0 errors, 0 skipped on `qaApi36`.
- Direct `VisualQaScreenshotTest#captureSprint9ContentExpansionScreens` runner: `OK (1 test)`.
- GPT Pro final visual verdict: `PASS`, harvested at `PRO_REVIEW_OUTPUT_SPRINT9_ANDROID_VISUAL_FIX5_20260426_181457/Sprint_9_Android_Audit.md`.
- Final raw logs and refreshed screenshots are preserved under `docs/visual-qa/2026-04-26-sprint9-content-expansion/`.
