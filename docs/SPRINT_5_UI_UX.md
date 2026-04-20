# Sprint 5 UI/UX Pass

Status: `active`

## Goal

Bring the Android app closer to the reviewed mockup direction without changing the product scope. Sprint 5 is a visual and interaction-quality pass, not a new ingestion sprint.

The target is the mockup's calm `analog` direction: warm, editorial, quiet, finite, and credible as a reading product. The app should feel like a place that helps users convert an impulse into a better choice, not like a punitive blocker or a generic productivity dashboard.

## PRD Mapping

- `FR6 Intervention UI`: restyle the core one-primary/two-backup intervention while preserving delay and conscious override.
- `FR8 Replacement Session Experience`: make reader and external handoff feel calm and deliberate.
- `FR10 Feedback Loop`: keep feedback lightweight and attached to the selected session.
- `FR12 Analytics Instrumentation`: preserve existing event identity while improving human-readable state surfaces.
- `NFR2 Calm Interaction Model`: no feed, no shame copy, no hard-block default.

## Scope

In scope:

- Light and Ink theme foundation with explicit design tokens.
- Theme selection persisted locally.
- Restyled intervention surface.
- Restyled home, add-link, library, reader, external handoff, feedback, and progress/history surfaces.
- Clear source distinction between editorial items and user links.
- Permission/readiness copy that remains technically accurate.
- Emulator validation and Pro review for scope drift.

Out of scope:

- PDF import or PDF reader.
- iOS work.
- Premium packaging.
- Hard-block mode.
- Streak mechanics beyond constructive progress framing.
- Additional content ingestion.
- Open-web crawling or AI summarization.

## Slices

### 5.1 Theme Foundation

Acceptance criteria:

- `Light` is the default warm paper theme.
- `Ink` is available as an explicit dark analog option.
- Theme choice persists locally.
- A visible settings surface exposes only `Light` and `Ink`.
- Existing tests remain green.

### 5.2 Core Intervention Restyle

Acceptance criteria:

- Intervention remains exactly one primary recommendation plus up to two backups.
- The triggering app is clear.
- The primary replacement feels editorial and prominent.
- `Delay 15 min` and `Open anyway` remain visible, calm, and non-shaming.
- No feed-like discovery surface is introduced.

### 5.3 Home, Library, Add Link

Acceptance criteria:

- Home communicates readiness and next useful action.
- Saved links are visible as a compact library summary, not a feed.
- Add-link flow matches the analog visual direction.
- Validation remains local and non-moralizing.

### 5.4 Reader, Handoff, Feedback, Progress

Acceptance criteria:

- Reader is book-like and finite.
- External handoff clearly labels saved links before opening.
- Feedback remains two lightweight questions.
- Progress emphasizes converted impulses and recent replacements without shame-heavy streak copy.

## Validation

Automated:

- `./gradlew testDebugUnitTest --no-daemon`
- `./gradlew lintDebug --no-daemon`
- `./gradlew connectedDebugAndroidTest --no-daemon`

Review:

- Pro review after meaningful UI slices, focused on preserving PRD constraints and avoiding feed-like or blocker-first drift.
