# Sprint 9 Rights and Policy Risk Register

Status: Sprint 9 app integration completed; risk register retained as historical risk context.

The candidate backlog CSV remains the pre-integration sourcing audit trail. `final_release_approval_20260426.csv` is the row-level release approval artifact for the integrated Android inventory and supersedes the earlier `rights_pending` candidate statuses.

## Rights Classes

| Group | Count | Integration posture | Required action |
|---|---:|---|---|
| Renderable Project Gutenberg candidates | 42 | Integrated as local Markdown excerpts | Approved in `final_release_approval_20260426.csv`; keep attribution/source policy and no-summary constraint. |
| Link-only modern or unclear-rights candidates | 58 | Integrated as metadata-only external handoff | Approved in `final_release_approval_20260426.csv`; never scrape, cache, summarize, or reproduce body text. |
| Sprint 9 app manifest items | 100 | Integrated into `starter_packs.json` | Validate Android display, recommendation behavior, analytics, and external handoff. |

42 Sprint 9 Markdown body assets are app-local Project Gutenberg text excerpts. No modern link-only article body text is stored in app assets.

## High Cultural-Context Risk

These rows should not become primary recommendations without explicit editorial framing:

- `s9-4-r04-turner-frontier-history`
- `s9-4-r06-dubois-souls-black-folk`
- `s9-4-r07-equiano-interesting-narrative`
- `s9-4-r08-hearn-glimpses-japan`

## Medium Risk Buckets

Counts below are medium-only rows. The four high cultural-context rows are listed separately above.

| Risk bucket | Count | Notes |
|---|---:|---|
| Cultural-context risk | 25 | Includes religious, cultural transmission, museum, colonial, archaeology, civic-history, and cross-cultural framing. |
| Political/current-events risk | 21 | Mostly historical, climate, civic, and institutional context; reject if a page depends on news churn. |
| Religious/spiritual framing risk | 11 | Requires non-preachy and non-exoticizing framing. |
| Medical/health-claim risk | 6 | No medical advice, diagnosis, therapeutic promise, or wellness-course positioning. |

## Asset and Diagram Risk

| Asset risk | Count | Handling |
|---|---:|---|
| `image_credit_review_needed` | 18 | Keep link-only when modern; for renderable classics, avoid image-dependent excerpts unless asset rights are cleared. |
| `diagram_image_review_needed` | 7 | Use text-only excerpts or keep external if diagrams are necessary. |
| `third_party_chart_and_data_review_needed` | 2 | Keep external and avoid chart reproduction. |
| `diagram_review_needed` | 1 | Select only self-contained puzzle text or keep external. |

## Non-Negotiable Guards

- No candidate body text from modern link-only pages goes into app assets.
- The final release approval artifact is the source of truth for Sprint 9 release readiness.
- Future edits to public-domain excerpts must preserve jurisdiction, attribution, source fit, and cultural-context review.
- Dated public-domain material must keep focused excerpt boundaries and non-sensational presentation.
