# Slice 9.3 Wonder, Curiosity, and Science Summary

Status: Implemented for GPT Pro review.

## Scope

Slice 9.3 is sourcing-only. It adds candidate metadata to the shared editorial backlog and does not integrate new app content, change ranking, add feed behavior, or add runtime scraping, caching, rehosting, reader-mode extraction, copyright blocking, or AI summaries.

## Candidate Mix

- Total Slice 9.3 candidates: 26.
- Renderable candidates: 11, all `Project Gutenberg`, `RENDERABLE`, `IN_APP_READER`, `rights_pending`.
- Link-only candidates: 15, all `LINK_ONLY`, `EXTERNAL_HANDOFF`, and `must_not_scrape_cache_or_summarize = true`.
- Replacement moments: 14 `WONDER_CURIOSITY`, 12 `SCIENCE_CURIOSITY`.
- Current Sprint 9 pool after Slice 9.3: 70 candidates, 28 renderable and 42 link-only.
- Remaining target: 30 candidates, 14 renderable and 16 link-only.

## Renderable Candidates

The renderable candidates are public-domain candidates only after manual rights, edition, jurisdiction, and excerpt review:

- `s9-3-r01-fabre-storybook-science` - Jean-Henri Fabre, `The Story-book of Science`.
- `s9-3-r02-fabre-life-spider` - Jean-Henri Fabre, `The Life of the Spider`.
- `s9-3-r03-fabre-life-fly` - Jean-Henri Fabre, `The Life of the Fly`.
- `s9-3-r04-buckley-fairyland-science` - Arabella B. Buckley, `The Fairy-Land of Science`.
- `s9-3-r05-flammarion-astronomy-amateurs` - Camille Flammarion, `Astronomy for Amateurs`.
- `s9-3-r06-ball-story-heavens` - Robert Stawell Ball, `The Story of the Heavens`.
- `s9-3-r07-martin-piece-coal` - Edward A. Martin, `The Story of a Piece of Coal`.
- `s9-3-r08-darwin-insectivorous-plants` - Charles Darwin, `Insectivorous Plants`.
- `s9-3-r09-tyndall-lectures-light` - John Tyndall, `Six Lectures on Light`.
- `s9-3-r10-munro-story-electricity` - John Munro, `The Story of Electricity`.
- `s9-3-r11-figuier-ocean-world` - Louis Figuier, `The Ocean World`.

## Link-Only Candidates

The link-only candidates are external handoff metadata only:

- Quanta Magazine: 5 rows.
- NASA: 3 rows.
- NOAA: 2 rows.
- Our World in Data: 2 rows.
- Nautilus: 2 rows.
- Smithsonian Ocean: 1 row.

No modern source family exceeds the 10-row full-pool cap. `Aeon/Psyche` remains capped at 10 from Slice 9.1.

## Risk Handling

- Chart-heavy data pages (`s9-3-l11-owid-energy`, `s9-3-l12-owid-greenhouse-gases`) are link-only and flagged `chart_dependent_external_only`.
- NASA visual/data candidates are link-only and flagged for image/data context.
- Astronomy and natural-history renderable candidates with diagrams, species images, or plates require text-only excerpt selection.
- Recent Quanta research candidates are marked `mostly_evergreen` and flagged for durability review.
- Climate and energy rows carry medium political/current-events risk and stay external handoff only.
- `The Story of a Piece of Coal` is flagged for dated industrial energy framing and medium current-events sensitivity.

## Review Posture

All Slice 9.3 rows remain `example_needs_manual_verification`, `pro_review_status = not_submitted`, and `legal_review_needed = yes`. No row is `approved_for_future_integration`, and no row becomes app content until it passes source verification, rights review, Android suitability review, and GPT Pro review.
