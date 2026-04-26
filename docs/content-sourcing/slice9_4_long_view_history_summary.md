# Slice 9.4 Long-View and History/Culture Summary

Status: implemented for Pro review.

Slice 9.4 adds 22 sourcing-only candidates to `content_candidate_backlog.csv`. These rows widen the Sprint 9 pool toward long-view thinking, history, cultural memory, material culture, and cross-generational context without changing app runtime behavior.

## Scope Mapping

- `PRD.md` Content Strategy: expands the bounded shared editorial candidate pool without creating a feed.
- `PRD.md` Content Rights and Rendering Policy: keeps modern sources as metadata-only external handoffs and keeps public-domain candidates in `rights_pending`.
- Sprint 9 target: moves the new candidate pool from 70 to 92 rows.

## Candidate Mix

| Class | Count | Notes |
|---|---:|---|
| Renderable candidates | 8 | All Project Gutenberg rows, all `rights_pending`, all require manual rights and excerpt review before integration. |
| Link-only candidates | 14 | Long Now, SAPIENS, The Met, NMAAHC, JSTOR Daily, and Nautilus rows; all `EXTERNAL_HANDOFF`. |
| `LONG_VIEW` | 10 | Time, education, deep history, deep time, and long-term preservation frames. |
| `HISTORY_CULTURE` | 12 | Civic history, trade routes, archaeology, material culture, slavery/freedom, and cross-cultural observation. |

## Source-Family Mix

| Source cap group | Count |
|---|---:|
| Project Gutenberg | 8 |
| Long Now | 4 |
| SAPIENS | 4 |
| Museum/Public Institution | 3 |
| JSTOR Daily | 2 |
| Nautilus | 1 |

No modern source family exceeds the full-pool cap of 10 candidates.

## Rights and Policy Posture

- Renderable candidates store only candidate metadata and remain `rights_pending`.
- Link-only rows store canonical URLs and product-written card metadata only.
- Every link-only row keeps `must_not_scrape_cache_or_summarize = true`.
- No new candidate is approved for future integration.
- No candidate body text was added to app assets.

## Review Risks

- `s9-4-r04-turner-frontier-history` requires strong settler-colonial and Indigenous-context review before any excerpt is considered.
- `s9-4-r06-dubois-souls-black-folk` is US-public-domain-friendly but not clean in all life-plus-70 jurisdictions until 2034.
- `s9-4-r07-equiano-interesting-narrative` requires slavery, violence, and religious-context excerpt review.
- `s9-4-r08-hearn-glimpses-japan` requires non-exoticizing cultural framing and may be rejected if no respectful excerpt is available.
- Museum, Long Now, JSTOR Daily, SAPIENS, and Nautilus rows remain link-only because rights, images, and modern article text should stay external.

## Android Fit

The slice preserves the product shape:

- one primary recommendation and finite backups remain feasible;
- no browsing feed is introduced;
- no runtime retrieval, scraping, or summarization is required;
- all modern/copyright-unclear pages are external handoff candidates only.
