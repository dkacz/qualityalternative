# Meta Milestones

Status: `living planning document`

This document is the cross-sprint map for the Android-first MVP. `PRD.md` remains the source of truth for requirements; sprint plans define execution details. This file answers the higher-level question: what comes next, when do we build UI, and what should not be pulled forward too early.

## Current Position

The project is past Android internal alpha for the core intervention loop.

Completed:

- Android app foundation in Kotlin + Jetpack Compose.
- Local-first onboarding, settings, delay logic, history, and analytics.
- Accessibility-driven app interception for the alpha support list.
- Soft intervention with one primary replacement, two backups, `Open anyway`, and `Pause 15 min`.
- Real-device alpha smoke on YouTube, X, and Facebook.
- Public GitHub repo and internal alpha APK release.
- Content governance model for renderable, link-only, and user-private inventory.
- First real renderable content pack: `attention-classics-v1`.

Current active track:

- Sprint 7 is complete.
- Work mode: decide whether the next move is PDF/pilot readiness, release packaging for the new content pack, or another content-quality iteration after tester signal.
- Gate: do not expand inventory unless each item is classified as `renderable`, `link_only`, or `user_private` with explicit metadata.

## UI Timing

The first major UI/UX pass is complete and should be treated as the current Android alpha visual baseline.

UI exists for:

- onboarding
- home
- intervention
- reader
- add link
- library
- external handoff
- feedback
- progress/history
- settings/readiness surfaces

The reviewed mockups are captured in `docs/UI_UX_MOCKUP_INTAKE.md`. The implemented first-pass themes are:

- `Light`
- `Dark` mode

Future UI work should improve parity or usability without changing the product scope unless the PRD is explicitly updated.

## Milestone 1: Internal Alpha Baseline

Status: `complete`

Goal:

- Prove that selected distracting apps can trigger a finite replacement intervention on Android.

Included:

- editorial starter packs only
- real app interception
- delay and conscious override
- local analytics and history
- public GitHub release for internal testers

Not included:

- user-added links
- PDFs
- iOS
- hosted analytics
- Play Store distribution

## Milestone 2: Sprint 4 User-Added Links

Status: `complete`

Goal:

- Let users bring their own web links into the replacement inventory without turning the product into a read-later feed.

Integrated on `main` as commit `882d115`.

Definition of done:

- user can add at least one web link locally
- added links survive restart
- added links can appear in the existing three-option intervention
- accepting a link records a replacement session
- analytics distinguishes editorial vs user-link replacements
- no PDF, RSS, crawler, newsletter, AI summarization, or feed scope enters the sprint

Release stance:

- This should not replace `v0.1.0-alpha` until Sprint 4 passes Pro review and emulator validation end to end.
- A feature APK can be released separately only after explicit decision.

## Milestone 3: Sprint 5 UI/UX Pass

Status: `complete`

Goal:

- Implement the reviewed calm analog UI direction without changing the product scope.

Included:

- `Light` warm paper theme
- `Dark` analog mode
- restyled core intervention
- restyled home, add-link, library, reader, external handoff, feedback, and progress/history surfaces
- theme setting persistence

Not included:

- PDFs
- iOS
- premium packaging
- hard-block mode
- streak mechanics beyond constructive progress framing
- additional ingestion sources

Sprint plan:

- See `docs/SPRINT_5_UI_UX.md`.

## Milestone 4: Sprint 6 Content Governance and Starter Library

Status: `complete`

Goal:

- Make content sourcing safe, explicit, and scalable before adding more formats or premium-facing inventory.

Likely scope:

- classify all shared content as `renderable`, `link_only`, or `user_private`
- add rights/render metadata to content models and starter-pack assets
- audit current starter-pack source labels
- add attribution ledger format for public-domain and Creative Commons content
- keep Substack/blog/media recommendations as link-only handoffs unless separately licensed
- document premium packaging boundaries around free/public-domain material

Out of scope:

- PDF upload or PDF reader
- EPUB support
- RSS/newsletter ingestion
- open-web crawling
- AI summarization of third-party works
- monetization implementation
- legal terms generation

Gate to start:

- Sprint 5 UI/UX baseline is merged and the team agrees that content provenance is now the next risk to remove.

Sprint plan:

- See `docs/SPRINT_6_CONTENT_GOVERNANCE.md`.

## Milestone 5: Sprint 7 Source Discovery and Content Library v1

Status: `complete`

Goal:

- Build the first real English-language replacement library from existing sources, without first-party essays and without turning the product into a scraper, crawler, or generic read-later app.

Likely scope:

- source candidate inventory across public-domain, permissive-license, and link-only sources
- first renderable or link-only pack selection
- source, rights, attribution, and duration metadata for each selected item
- app integration for one small finite pack if source triage is safe enough
- replacement-quality pass focused on whether the content is actually compelling at the moment of impulse

Delivered:

- Source candidate inventory and first pack selection.
- `attention-classics-v1` integrated as a finite local renderable pack.
- Author-facing source labels for the impulse card, with rights/source metadata preserved separately.
- Tester instructions that collect item-level content-quality signal.
- Pro review passed for all Sprint 7 slices.

Out of scope:

- first-party essay writing
- open-web crawling
- AI summarization of third-party articles
- PDF upload or reader work
- iOS
- premium packaging implementation

Gate to start:

- Sprint 6 rights/rendering model is merged and the team agrees the next risk is replacement quality, not more governance.

Sprint plan:

- See `docs/SPRINT_7_CONTENT_LIBRARY.md`.
- See `docs/CONTENT_SOURCE_CANDIDATES.md`.
- See `docs/CONTENT_PACK_V1_SELECTION.md`.
- See `docs/CONTENT_PACK_V1_QUALITY_PASS.md`.

## Milestone 6: PDF and Pilot Readiness

Status: `planned, after content library v1`

Goal:

- Move from internal alpha toward a small external pilot by supporting the second user-owned content format: PDFs.

Likely scope:

- Android document picker for PDF selection
- local URI/permission persistence
- simple PDF reader or controlled fallback
- PDF metadata as rankable content
- source-aware analytics for editorial, link, and PDF
- external-pilot checklist and tester reporting polish

Out of scope:

- annotations
- highlights
- folders
- full document management
- cloud sync
- AI PDF summarization

Gate to start:

- Sprint 7 established at least one real shared content pack; before PDF work starts, refresh or renumber the existing PDF plan.

Sprint plan:

- See `docs/SPRINT_6_PDF_PILOT_READINESS.md`; this document should be renumbered or refreshed before implementation starts.

## Milestone 7: Small External Pilot

Status: `planned, after UI/UX and PDF readiness`

Goal:

- Test whether the product changes real behavior outside the founder/friend testing loop.

Minimum pilot inputs:

- Android build with editorial packs, user links, and PDFs
- clear install/test instructions
- simple feedback and bug-reporting workflow
- stable local analytics/export path

Decision signals:

- replacement acceptance rate
- interventions that avoid opening the target app
- replacement sessions over 3 minutes
- feedback saying the recommendation was a good fit
- return to target app within 15 and 60 minutes

## Milestone 8: iOS Discovery

Status: `later discovery, not implementation`

Goal:

- Decide whether an iOS version is worth building and which technical path is realistic.

Likely work:

- Screen Time / Family Controls entitlement research
- shield UX constraints
- Shortcuts-based prototype comparison
- App Store policy risk assessment
- decision memo: full iOS, lightweight iOS, or Android-only for longer

Gate to start:

- Android pilot should show enough behavioral value to justify platform risk.

## Milestone 9: Post-Pilot Product Bet

Status: `future`

Goal:

- Choose the first larger product expansion after evidence from real users.

Candidate bets:

- stronger ranking
- better content ingestion
- free vs premium packaging
- broader curated premium library
- higher or unlimited personal-content capacity
- streaks and motivation layer
- premium hard-block mode
- custom delay durations
- iOS implementation
- lightweight hosted analytics

Default rule:

- Do not expand into these until pilot data shows which constraint actually matters.

## Packaging and Motivation Hypotheses

These are strategic hypotheses, not active Sprint 4 requirements.

Free tier hypothesis:

- core app-open interception
- basic editorial starter content
- limited user-owned content capacity
- enough value to prove the behavior loop without turning the product into an unlimited content manager

Premium tier hypothesis:

- broader curated library of high-quality replacement content
- materially higher or unlimited capacity for user-owned links, PDFs, EPUBs, and later formats
- advanced modes such as hard blocking only after soft intervention value is proven

Streak hypothesis:

- reward "days converted" rather than generic abstinence
- count a day when the user intercepts an impulse and spends meaningful time with a replacement
- avoid shame-heavy gamification that makes the product feel punitive

Decision gate:

- Do not implement monetization, unlimited storage, EPUB support, or streak mechanics until pilot data shows that users repeatedly accept replacements and ask for more content depth or motivation.

## Operating Rule

The product should advance in this order:

1. Prove the intervention loop.
2. Let the user bring high-quality material.
3. Prove substitution with small pilot data.
4. Only then expand platforms, ingestion, or monetization.

The main thing to protect is still the original wedge: **quality replacement at the moment of impulse**, not another blocker and not another infinite reading queue.
