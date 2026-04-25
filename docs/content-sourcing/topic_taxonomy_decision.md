# Topic Taxonomy Decision

Status: Slice 9.0 decision for Pro review.

## Decision

Use a small user-facing topic set for ranking/onboarding and a separate internal `replacement_moment` field for editorial intent.

Do not add `OTHER` as a user-facing topic for Sprint 9. `OTHER` is too broad, weakens ranking, and hides sourcing gaps. Use `UNCATEGORIZED_PENDING_REVIEW` only as an internal triage status for rows that have not passed review.

## User-Facing Topic Set

| Topic | Decision | Rationale |
|---|---|---|
| `ATTENTION` | Add | Core product use case; should not be hidden under psychology or tech |
| `PRACTICAL` | Add | Agency, habit repair, study, decision-making, and attention hygiene without productivity filler |
| `BODY` | Add | Embodied reset, walking, senses, rest, and low-reading alternatives without medical positioning |
| `PHILOSOPHY` | Keep but cap | Still important, but current inventory is philosophy-heavy |
| `SCIENCE` | Keep | Strong wonder/curiosity fit |
| `PSYCHOLOGY` | Keep | Useful for habit, emotion, attention, and self-regulation |
| `HISTORY_CULTURE` | Add | Covers anthropology, history, institutions, craft, and human stories |
| `CREATIVITY` | Keep | Play, craft, design, mathematics-as-play, and making |
| `NATURE` | Add | Natural history, place-writing, ecology, and outward attention |
| `TECH` | Keep but constrain | Use only when technology is the subject, not as a proxy for modernity |

## Format Genre Tags

Move `ESSAYS` and `POETRY` out of primary topics in future candidate rows. They describe form, not why an item helps at the intervention moment.

Suggested `format_genre_tags` examples:

- `essay`
- `poetry`
- `reference`
- `guide`
- `excerpt`
- `profile`
- `natural_history`
- `public_domain_classic`

## Replacement Moments

| Moment | Use |
|---|---|
| `ATTENTION_RESET` | Interrupt automatic app-opening and redirect attention |
| `LONG_VIEW` | Widen time horizon and reduce urgency |
| `WONDER_CURIOSITY` | Offer cognitive novelty without feed dynamics |
| `PRACTICAL_AGENCY` | Give a concrete next mental move or action |
| `BODY_RESET` | Move attention into senses, walking, breathing, rest, or environment |
| `HISTORY_CULTURE` | Reframe the moment through human stories and cultural context |
| `SCIENCE_CURIOSITY` | Durable science and explainers |
| `CREATIVITY_PLAY` | Play, craft, patterns, design, and making |
| `CALM_PHILOSOPHY` | Reflective, non-preachy philosophical reset |

## Migration Note

Slice 9.0 does not change Android enums or app UI. Topic changes are sourcing taxonomy decisions only. Production model changes should happen later, after Pro approves the candidate taxonomy and at least one integration slice needs those topics in-app.
