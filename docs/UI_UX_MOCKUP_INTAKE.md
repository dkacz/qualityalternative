# UI/UX Mockup Intake

Status: `design source captured, not yet implemented`

Reviewed artifact:

- Local mockup export: `/Users/omare/Downloads/Quality Alternative.zip`
- Format: self-contained HTML/React prototype
- Reviewed screens: welcome, app selection, topic selection, duration, permissions, home, library, add link, intervention, reader, external handoff, feedback, progress, settings

This document converts the mockup into an implementation contract for the Android Compose app. It does not replace `PRD.md`; it defines the intended visual and interaction direction for the next UI pass.

## Product Fit

The mockup is strongly aligned with the product thesis: Quality Alternative should feel like a calm reading product that happens to intercept impulses, not like a punitive blocker.

The most important retained choices are:

- The intervention is soft by default and avoids guilt-heavy language.
- The core moment shows one primary replacement and two lighter backup choices.
- `Delay 15 min` and `Open anyway` remain visible and non-punitive.
- The reading surface feels finite and book-like rather than feed-like.
- Progress is framed as days converted or impulses redirected, not generic abstinence.
- Personal content is presented as a small quality library, not a bookmark dump.

## Visual Direction

The implementation target is the mockup's `analog` direction.

It should feel:

- quiet
- editorial
- warm
- intentional
- low-stimulation
- credible as a place to read

It should not feel:

- like a productivity dashboard
- like a generic Material demo
- like a social feed
- like a punitive parental-control app
- like a gamified streak machine

## Theme Scope

Implement exactly two themes in the first UI pass:

- `Light`: the default warm paper mode from the analog mockup.
- `Ink`: the dark analog mode from the mockup's `dark-ink` option.

Do not implement the other prototype directions or dark variants in the first pass:

- no `editorial` theme
- no `modern` theme
- no `midnight`
- no `graphite`
- no broad theme picker beyond `Light` and `Ink`

The first Compose implementation should introduce a small app theme layer with explicit design tokens for background, elevated surface, primary text, muted text, faint text, line, accent, accent-soft, success, and success-soft. The theme choice should be persisted locally once the Settings UI exposes it.

Default behavior:

- Use `Light` as the default alpha theme.
- Add `Ink` as an explicit user setting.
- A future `Use system setting` option can be considered later, but it is not required for the first UI pass.

## Screen Priorities

### P0: Core Intervention

The intervention screen is the most important design surface.

Implementation requirements:

- Show the app that triggered the intervention.
- Show exactly one primary recommendation.
- Show exactly two backup choices.
- Preserve the three action paths: read, delay, open anyway.
- Keep override visible, calm, and not shame-coded.
- Keep the layout compact enough for smaller Android screens.
- Avoid adding scrollable discovery beyond the three recommendations.

### P1: Add Link and Library

The Sprint 4 user-link flow should be restyled toward the mockup after the functional slices pass review.

Implementation requirements:

- Add link screen collects URL, title, estimated duration, and topic tags.
- Validation language stays local and non-moralizing.
- Saved links are visible without becoming a feed.
- Editorial items and user links are visually distinguishable.
- User links that require external handoff must be clearly labeled before opening.

### P1: Home and Settings

Home should communicate readiness, not become an analytics dashboard.

Implementation requirements:

- Show interception status.
- Show selected apps.
- Show library inventory at a high level.
- Keep the debug/preview intervention path available for internal builds.
- Settings should expose selected apps, permissions readiness, default duration, and theme.

Settings theme scope:

- `Light`
- `Ink`

### P2: Progress

Progress should support motivation without turning the product into a shame loop.

Implementation requirements:

- Prefer `days converted` over generic streak language.
- Show intervention outcomes: shown, chose alternative, delayed social, opened anyway.
- Show recent replacements as history, not a feed.
- Avoid loss aversion copy such as "you broke your streak."

### P2: Onboarding

Onboarding can follow the mockup's calm copy and step sequence, but it must remain accurate to the current Android implementation.

Implementation requirements:

- Explain Accessibility and overlay permissions truthfully.
- Do not claim the app never reads the screen unless that statement matches the exact implementation and policy wording.
- Keep minimum app/topic requirements aligned with `PRD.md`.
- Avoid requiring user-added links during the current internal alpha if that would block existing tester onboarding.

## Implementation Constraints

- Compose remains the app UI stack.
- Do not introduce a webview-based prototype shell.
- Do not make the intervention into a browsing surface.
- Do not introduce automatic article extraction as part of the visual pass.
- Do not introduce premium, unlimited library, EPUB, or PDF scope unless the active sprint explicitly includes it.
- Do not make hard/firm blocking the default mode.

## Acceptance Criteria for the Future UI Pass

- Light and Ink themes are available and visually coherent across the main app surfaces.
- Intervention remains one primary plus two backups.
- Add Link UI matches the new design direction without changing Sprint 4 scope.
- Permission/readiness copy remains technically accurate.
- Progress uses constructive replacement metrics.
- Existing unit, lint, and emulator instrumentation suites remain green.
- A Pro review validates that the redesign preserves the PRD constraints and does not introduce feed-like or blocker-first behavior.

