# Slice 9.1 Attention Reset and Practical Agency Summary

Status: Implemented for Pro review.

## Scope

Slice 9.1 adds sourcing backlog rows only. It does not integrate new content into Android, change ranking, add feed behavior, or add runtime scraping, caching, rehosting, reader-mode extraction, or AI summarization.

## Candidate Mix

- 24 total candidate rows.
- 9 renderable candidates, all marked `rights_pending`.
- 15 link-only candidates, all marked `LINK_ONLY` and `EXTERNAL_HANDOFF`.
- Replacement moments are limited to `ATTENTION_RESET` and `PRACTICAL_AGENCY`.

## Renderable Candidates

The renderable set uses Project Gutenberg as a public-domain candidate source family, but every row remains rights-pending until manual rights, jurisdiction, edition, translator/editor, attribution, and excerpt review are complete.

Renderable candidates include John Dewey, Arnold Bennett, William James, John Stuart Mill, Aristotle, Ralph Waldo Emerson, Annie Payson Call, Alfred North Whitehead, and Charlotte M. Mason.

## Link-Only Candidates

The link-only set uses metadata-only external handoff for modern or unclear-rights sources. Every link-only row has `must_not_scrape_cache_or_summarize=true`.

Source-family distribution:

- `Aeon/Psyche`: 10 rows.
- `Nautilus`: 3 rows.
- `SEP`: 2 rows.

## Review Posture

Rows labeled `exact_candidate_from_attached_docs` come from prior candidate notes and still need canonical URL recheck before Pro-ready status. Rows labeled `example_needs_manual_verification` were located through current web search and still need open-page verification before Pro-ready status.

No row is marked `approved_for_future_integration`.
