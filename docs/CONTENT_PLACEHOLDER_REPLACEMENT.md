# Placeholder Pack Replacement

Date: 2026-04-22

This slice replaces the original prototype editorial content in the `philosophy` and `science` starter packs with real renderable public-domain excerpts. The pack IDs remain stable so onboarding, preferences, history, and tests that select `philosophy` or `science` continue to work.

## Replaced Packs

- `philosophy`: four public-domain philosophy excerpts from Plato, Seneca, Marcus Aurelius, and Cicero.
- `science`: three public-domain science or psychology excerpts from Michael Faraday, John Tyndall, and William James.

## Policy Shape

- The app still uses finite replacement behavior: one primary suggestion plus two backups.
- Compliance remains a content selection and inventory metadata responsibility.
- No runtime copyright blocking, scraping, caching, summarizing, or reader-mode treatment of modern third-party articles was added.
- Each replacement item has `sourceUrl`, `licenseUrl`, attribution, `rightsReviewedAt`, `renderMode = IN_APP_READER`, and a local Markdown body asset.
