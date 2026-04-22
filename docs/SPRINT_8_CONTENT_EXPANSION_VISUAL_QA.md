# Sprint 8 Content Expansion and Visual QA

Status: `in progress`

## Goal

Move from one real renderable pack to a substantially larger replacement inventory, while proving on emulator screenshots that replacement surfaces render well in the intervention, library, reader, feedback, progress, timer, and dark-mode surfaces.

Sprint 8 should add breadth without changing the product into a feed. The user still sees one primary recommendation and two backups. The library can grow; the intervention remains finite.

## Product Principles

- Expand inventory, not browsing behavior.
- Use content classes from Sprint 6: `renderable`, `link_only`, and `user_private`.
- Copyright and reuse decisions happen during source selection and triage, not through hidden runtime blocking.
- Link-only items are recommendations to canonical external pages; the app should not scrape, cache, reader-mode, summarize, or rehost them.
- Utility replacements such as the meditation timer are allowed when they stay finite, local-first, and non-feed-like.
- Every shared item needs a clear "why this now?" reason for the impulse moment.
- Remove weak items rather than padding the count.

## Target Inventory Shape

The near-term target before a broader pilot is 80-120 high-quality replacement options:

- 30-45 renderable public-domain or permissively licensed readings.
- 35-60 link-only modern essays, explainers, and deep reads.
- 1-3 utility replacements, starting with a 3-minute meditation timer.
- User-private links and later PDFs as personal inventory, not shared catalog content.

The app should still show only three options per intervention.

## Source Tracks

### Track A: Renderable Classics and Wonder

Purpose: content that can be read fully inside the app.

Likely sources:

- Public-domain classics already identified in `docs/CONTENT_SOURCE_CANDIDATES.md`.
- Additional short excerpts from Epictetus, Marcus Aurelius, William James, Thoreau, Emerson, Mary Austin, Francis Bacon.
- Candidate additions after item-level triage: Seneca, Darwin, Booker T. Washington, Mill, Montaigne, older translations of Laozi or `The Dhammapada` only if edition and translator status are clean.
- Modern permissive sources such as Our World in Data, NASA Earth Observatory, NOAA, or OpenStax only after item-level license and asset checks.

Acceptance criteria:

- Every item has source URL, attribution, rights metadata, duration, topics, and render mode.
- Every excerpt is short enough to feel like a real alternative in 3-12 minutes.
- The source label shown in the card is human-readable, not legal/provenance-heavy.
- Body assets render in the in-app reader without truncation.

### Track B: Link-Only Modern Library

Purpose: recommendations for high-quality modern writing that should open externally.

Likely sources:

- SAPIENS
- The Conversation
- Aeon / Psyche
- Quanta Magazine
- Nautilus
- Long Now
- Stanford Encyclopedia of Philosophy
- Internet Encyclopedia of Philosophy
- selected public Substack/blog posts only as canonical external links

Acceptance criteria:

- Add shared link-only editorial items to app assets or a local manifest.
- Each link-only item has title, source, description, canonical URL, duration estimate, topics, and `EXTERNAL_HANDOFF` render mode.
- The app opens link-only recommendations through the existing handoff surface.
- No article text is reproduced in-app unless a separate rights decision upgrades the item to `renderable`.
- Ranking can mix renderable and link-only items without creating a scrolling discovery surface.

### Track C: Ranking and Diversity

Purpose: avoid showing three near-identical recommendations.

Acceptance criteria:

- Keep one primary plus two backups.
- Prefer a mix of duration, topic, and source family when inventory is large enough.
- Avoid repeating the same source family too aggressively.
- Continue excluding completed items.
- Log whether selected content was `EDITORIAL`, `USER_LINK`, and renderable versus external handoff.

### Track D: Visual QA

Purpose: make content display quality a release gate.

Acceptance criteria:

- Capture emulator screenshots for Light and Dark modes.
- Cover at least: home, library, intervention, reader, feedback, progress, settings, and external handoff.
- Include at least one renderable `Attention Classics v1` item in reader screenshots.
- Include at least one link-only handoff screenshot once shared link-only items exist.
- Include the meditation timer in both Light and Dark modes once implemented.
- Record whether titles, source labels, metadata rows, line breaks, scroll behavior, and CTA placement look acceptable.

### Track E: Utility Replacement

Purpose: add a copyright-free, low-energy alternative for moments when reading is too much.

Acceptance criteria:

- Add a default 3-minute meditation timer as an app utility replacement.
- The timer can appear as one of the existing finite intervention choices, not as a separate feed or browsing surface.
- Completion records a replacement session and routes to the same feedback loop.
- Early exit records a skipped timer session.
- Meditation remains repeatable; completing it once must not permanently exclude it from future primary recommendations.
- No guided-audio library, health claims, notifications, background timer, or meditation-course scope enters this slice.

## Proposed Slices

### 8.1 Visual QA Harness and Baseline

Status: `implemented`

- Add or run an emulator screenshot harness for core app surfaces.
- Save screenshots under a dated ignored or documented artifact folder.
- Write a visual QA report with pass/fail findings.
- Do not add new content in this slice.

Exit criteria:

- Screenshots exist for Light and Dark core flows.
- Any visual defects are documented with screenshot names.
- Unit/lint/instrumentation validation still passes.

Result:

- See `docs/VISUAL_QA_20260422_CONTENT_DISPLAY.md`.

### 8.2 Shared Link-Only Manifest Support

Status: `implemented`

- Extend local editorial assets to support `EXTERNAL_HANDOFF` items with `externalUrl` and no body asset.
- Keep existing user-added links behavior unchanged.
- Add tests for mixed renderable and link-only inventory.

Exit criteria:

- A local shared link-only item can appear in the finite intervention.
- Accepting it opens the saved-link/external handoff surface.
- No body text is required or rendered for link-only items.

Result:

- `link-only-modern-v1` proves shared editorial link-only items can be parsed from assets.
- Shared link-only recommendations remain editorial inventory but route through external handoff.
- Generic external-handoff analytics distinguish shared editorial links from user-added links.

### 8.2b Meditation Timer Replacement

Status: `implemented`

- Add a 3-minute meditation timer replacement with its own app-utility render mode.
- Keep the timer inside the same one-primary/two-backup intervention shape.
- Record meditation completion and skip analytics separately from reader completion.
- Add unit, instrumentation, and visual QA coverage.

Exit criteria:

- A meditation-only recommendation opens the timer screen.
- The timer defaults to `3:00`.
- Completion can be logged into the normal feedback flow.
- The timer appears in Light and Dark screenshots.
- The timer is repeatable after completion.

### 8.3 Source Candidate Discovery and Scoring

Status: `implemented`

- Build a larger candidate inventory before adding more app content.
- Include roughly 50 link-only modern candidates and 30 renderable public-domain or permissive candidates.
- Score candidates for impulse-fit, durability, first-batch suitability, and concrete next triage step.
- Verify canonical source families and exact URLs where possible; mark source-family placeholders explicitly when exact item selection remains open.

Exit criteria:

- Candidate inventory exists and separates `link_only_candidate` from renderable candidates.
- The first mixed-batch shortlist is clear enough for Slice 8.4 and Slice 8.5 implementation.
- The document does not add runtime copyright blocking or app manifest content.

Result:

- See `docs/CONTENT_SOURCE_CANDIDATES_SPRINT8.md`.

### 8.4 Link-Only Pack v1

Status: `implemented`

- Add 20 curated link-only recommendations from modern high-quality sources.
- Treat every item as canonical external handoff.
- Prefer evergreen essays and explainers, not news.

Exit criteria:

- Every item has title, source, description, canonical URL, duration estimate, topics, rights/render metadata, and "why this now?" note.
- Pro review or equivalent review focuses on source quality, link-only correctness, and product fit.

Result:

- `link-only-modern-v1` adds 20 canonical external recommendations across Long Now, Psyche, Aeon, Quanta, SAPIENS, SEP, IEP, and Nautilus.
- See `docs/CONTENT_LINK_ONLY_PACK_V1.md`.

### 8.5 Renderable Pack Expansion v2

- Add 10 more renderable items from already triaged public-domain or permissive sources.
- Prefer short, vivid excerpts over famous-but-dense material.

Exit criteria:

- Tests confirm body assets exist, metadata is complete, durations are plausible, and reader rendering is not capped.

### 8.6 Replacement Quality and Release Candidate

- Review item titles and descriptions for pickability at the impulse moment.
- Remove weak items before release.
- Run visual QA again after content scale-up.

Exit criteria:

- Total shared inventory reaches roughly 45-55 items across renderable, link-only, and utility replacement classes.
- The intervention still shows only one primary plus two backups.
- Visual QA report has no blocker findings.

## Non-Goals

- No RSS/newsletter ingestion.
- No article scraping or reader-mode extraction.
- No AI summaries of third-party content.
- No PDF implementation in this sprint unless the PRD is updated.
- No premium packaging implementation.
- No iOS implementation.

## Open Decisions

- Resolved: the 20-item link-only pack is selected in `docs/CONTENT_LINK_ONLY_PACK_V1.md`.
- Pro review should run after every Sprint 8 slice.
- Whether to create a small internal content-review checklist for tester feedback before adding more than 60 shared items.
