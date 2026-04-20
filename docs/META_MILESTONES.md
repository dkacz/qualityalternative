# Meta Milestones

Status: `living planning document`

This document is the cross-sprint map for the Android-first MVP. `PRD.md` remains the source of truth for requirements; sprint plans define execution details. This file answers the higher-level question: what comes next, when do we build UI, and what should not be pulled forward too early.

## Current Position

The project is past Android internal alpha for the core intervention loop.

Completed:

- Android app foundation in Kotlin + Jetpack Compose.
- Local-first onboarding, settings, delay logic, history, and analytics.
- Accessibility-driven app interception for the alpha support list.
- Soft intervention with one primary replacement, two backups, `Open anyway`, and `Delay for 15 minutes`.
- Real-device alpha smoke on YouTube, X, and Facebook.
- Public GitHub repo and internal alpha APK release.

Current active track:

- Sprint 4: user-added links v0.
- Work mode: emulator-only validation during the sprint.
- Gate: every slice must go through Pro review and fixes before the next slice starts.

## UI Timing

The current product UI work is **Sprint 4 Slice 4.2: Add Link UI**.

We do not build that UI until **Slice 4.1 Domain and Persistence Foundation** passes Pro review. The reason is intentional: the Add Link screen should sit on stable URL validation, stable content identity, safe Room persistence, and source-aware inventory behavior. Otherwise we risk building a nice-looking UI on a shaky model.

UI already exists for:

- onboarding
- home/debug trigger
- intervention
- reader
- feedback
- history/readiness surfaces

UI still to build in Sprint 4:

- `Add link` entry point on the home screen
- add-link form with URL, title, duration, and topic tags
- validation errors for bad or incomplete input
- saved-link visibility without creating a feed
- clear distinction between editorial content and external links
- external-link fallback/handoff state when a recommended link is accepted

The broader visual redesign should happen as a separate UI/UX pass after Sprint 4 functionality is stable. The reviewed mockups are captured in `docs/UI_UX_MOCKUP_INTAKE.md`. That pass should implement the mockup's calm analog direction with exactly two first-pass themes:

- `Light`
- `Ink` dark mode

Other mockup directions and dark variants should remain out of scope until the core UI pass is validated.

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

Status: `in progress`

Goal:

- Let users bring their own web links into the replacement inventory without turning the product into a read-later feed.

Slices:

- `4.1 Domain and Persistence Foundation`
  Status: implemented, under Pro review/fix loop.
- `4.2 Add Link UI`
  Status: next UI slice after 4.1 PASS.
- `4.3 Recommendation Inventory Integration`
  Status: pending.
- `4.4 Link Open/Fallback Session`
  Status: pending.

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

## Milestone 3: Sprint 5 PDF and Pilot Readiness

Status: `planned, not active`

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

- Sprint 4 must prove that user-owned content can enter the intervention loop cleanly.

## Milestone 4: Small External Pilot

Status: `planned, after Sprint 5`

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

## Milestone 5: iOS Discovery

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

## Milestone 6: Post-Pilot Product Bet

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
