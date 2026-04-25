# Slice 9.2 Embodied Reset and Calm Philosophy Summary

Status: Implemented for Pro review.

## Scope

Slice 9.2 adds sourcing backlog rows only. It does not integrate new content into Android, change ranking, add feed behavior, or add runtime scraping, caching, rehosting, reader-mode extraction, copyright blocking, or AI summarization.

## Candidate Mix

- 20 total candidate rows.
- 8 renderable candidates, all marked `rights_pending`.
- 12 link-only candidates, all marked `LINK_ONLY` and `EXTERNAL_HANDOFF`.
- Replacement moments are limited to `BODY_RESET` and `CALM_PHILOSOPHY`.

## Renderable Candidates

The renderable set uses Project Gutenberg as a public-domain candidate source family, but every row remains rights-pending until manual rights, jurisdiction, edition, translator/editor, attribution, cultural-context, and excerpt review are complete.

Renderable candidates include John Muir, Gilbert White, John Burroughs, Kakuzo Okakura, Confucius, Boethius, Arthur Schopenhauer, and Laozi.

## Link-Only Candidates

The link-only set uses metadata-only external handoff for modern or unclear-rights sources. Every link-only row has `must_not_scrape_cache_or_summarize=true`.

Source-family distribution:

- `SEP`: 2 rows.
- `IEP`: 3 rows.
- `SAPIENS`: 4 rows.
- `Museum/Public Institution`: 3 rows.

No `Aeon/Psyche` rows were added because Slice 9.1 already reached the full-pool source-family cap of 10.

## Review Posture

Rows labeled `example_needs_manual_verification` were located through current web search and still need open-page verification before Pro-ready status.

Rows that still need manual verification intentionally leave `canonical_url_verified_at` and `canonical_url_verified_by` blank. Their current URL provenance lives in `source_reference_note` and `url_verification_method`, not in canonical-verification fields.

No row is marked `approved_for_future_integration`.

## Risk Guardrails

- Public-domain spiritual/philosophical texts such as `The Book of Tea`, `The Analects of Confucius`, `The Tao Teh King`, and `The Consolation of Philosophy` require cultural, spiritual, translator/editor, and excerpt review before integration.
- Body-awareness and exercise-adjacent link-only rows are marked with medium medical/health risk where they discuss pain, interoception, exercise, or health claims.
- SAPIENS and museum rows with cultural, pandemic-era, exhibition-date, or body-image context are marked with explicit sensitivity flags and remain link-only.
