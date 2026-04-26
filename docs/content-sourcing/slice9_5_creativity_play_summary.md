# Slice 9.5 Creativity/Play Summary

Status: implemented for Pro review.

Slice 9.5 adds the final 8 sourcing-only candidates to `content_candidate_backlog.csv`, bringing Sprint 9 to the 100-row candidate target without adding app runtime behavior.

## Scope Mapping

- `PRD.md` Content Strategy: fills a bounded creativity/play gap without introducing a discovery feed.
- `PRD.md` Content Rights and Rendering Policy: keeps modern sources metadata-only and keeps public-domain renderable candidates in `rights_pending`.
- Sprint 9 target: moves the new candidate pool from 92 to 100 rows and repairs the final rights split to 42 renderable and 58 link-only.

## Candidate Mix

| Class | Count | Notes |
|---|---:|---|
| Renderable candidates | 6 | All Project Gutenberg rows, all `rights_pending`, all require manual rights and excerpt review before integration. |
| Link-only candidates | 2 | Quanta and The Met rows; both `EXTERNAL_HANDOFF`. |
| `CREATIVITY_PLAY` | 8 | Puzzles, nonsense verse, film art, writing craft, printmaking, book arts, graph play, and design history. |
| Primary topic `CREATIVITY` | 8 | The slice closes the previously empty creativity primary-topic gap. |

## Source-Family Mix

| Source cap group | Count |
|---|---:|
| Project Gutenberg | 6 |
| Quanta | 1 |
| Museum/Public Institution | 1 |

No modern source family exceeds the full-pool cap of 10 candidates after this slice.

## Rights and Policy Posture

- Renderable candidates store only candidate metadata and remain `rights_pending`.
- Link-only rows store canonical URLs and product-written card metadata only.
- Every link-only row keeps `must_not_scrape_cache_or_summarize = true`.
- No new candidate is approved for future integration.
- No candidate body text was added to app assets.

## Review Risks

- `s9-5-r01-dudeney-amusements-math` requires puzzle-spoiler and diagram-dependency review before excerpt selection.
- `s9-5-r02-lear-book-nonsense` requires image and children's-literature tone review before excerpt selection.
- `s9-5-r03-lindsay-art-moving-picture` requires early-film cultural-assumption review.
- `s9-5-r04-quiller-couch-art-writing` requires elitist lecture-tone review.
- `s9-5-r05-fletcher-wood-block-printing` requires Japanese craft-transmission and image-dependency review.
- `s9-5-l01-quanta-local-global-graph` and `s9-5-l02-met-design-1900-1925` remain link-only because modern article text and object or diagram context should stay external.

## Android Fit

The slice preserves the product shape:

- one primary recommendation and finite backups remain feasible;
- no browsing feed is introduced;
- no runtime retrieval, scraping, or summarization is required;
- all modern/copyright-unclear pages are external handoff candidates only.
