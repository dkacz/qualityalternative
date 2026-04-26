# Sprint 9 Candidate Cut Log

Status: no final cuts; full Sprint 9 pool integrated in Slice 9.7 and reconciled in `final_release_approval_20260426.csv`.

The current pool preserves 100 candidates as a pre-integration sourcing audit and integrates the same 100 rows into finite Android starter packs. Final row-level release approval now lives in `final_release_approval_20260426.csv`; no row was cut from the Sprint 9 app-content pass.

## Current State

| State | Count |
|---|---:|
| Total Sprint 9 candidates | 100 |
| Integrated into `starter_packs.json` | 100 |
| Renderable body assets shipped | 42 |
| Link-only external handoff items shipped | 58 |
| `rejected` | 0 |
| Final release approval rows | 100 |
| Approved in-app reader rows | 42 |
| Approved link-only handoff rows | 58 |
| Historical source backlog `rights_pending` rows | 42 |
| Historical source backlog `triaged_candidate` rows | 58 |

## Cut or Downgrade Triggers

- Renderable row depends on images, diagrams, charts, or formatting that cannot be cleared or rendered cleanly.
- Public-domain status is uncertain outside the United States and the app distribution target requires broader clearance.
- Excerpt cannot stand alone in 3-20 minutes without misleading framing.
- Dated language or cultural framing would require too much editorial scaffolding for an impulse-time replacement.
- Link-only page is paywalled, gated, too topical, fragile, or too feed-like.
- Candidate duplicates current app inventory or another Sprint 9 candidate.
- Candidate encourages health, politics, spirituality, productivity, or self-improvement overreach.

## Prior Review Corrections Already Applied

- Slice 9.1 source-risk metadata fix.
- Slice 9.2 duplicate-source replacement fix.
- Slice 9.3 image-dependency and Quanta verification-method fix.
- Final distribution review source-overlap fix: replaced two pre-existing Slice 9.1 rows whose canonical URLs were already present in `starter_packs.json`.

## Next Review Action

Use `final_release_approval_20260426.csv`, Gradle instrumentation, and visual QA screenshots to decide whether any future edit should cut, downgrade, or rewrite an integrated row.
