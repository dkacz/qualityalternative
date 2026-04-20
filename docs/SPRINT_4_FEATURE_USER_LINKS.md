# Sprint 4 Feature Plan: User-Added Links v0

Status: `planned`

This sprint can run in parallel with internal-alpha feedback hardening. It should stay on a separate feature branch until the alpha feedback branch is stable enough to merge forward.

## Goal

Add the first user-owned content source without turning the product into a generic read-later app.

The feature wedge is deliberately narrow: a tester can manually add a small number of web links, see them in local inventory, and receive those links as replacement candidates in the existing three-choice intervention surface.

## PRD Mapping

- `FR3 Replacement Source Setup`: user can add a web link manually and keep at least ten content items locally.
- `FR4 Content Item Model`: user-added links normalize into rankable `ContentItem` equivalents with title, source type, topic tags, estimated duration, format, and availability status.
- `FR5/FR6 Recommendation Flow`: user-added links can appear in the same one-primary, two-backup recommendation set without adding a feed.
- `FR8 Replacement Session Experience`: selected links open through a calm, controlled reader or fallback path.
- `FR10 Feedback Loop`: accept/skip/fit/helped signals remain attached to the selected item.
- `FR12 Analytics Instrumentation`: link add, link recommendation, link accept, fallback open, and unavailable-link events are logged locally.
- `NFR2 Calm Interaction Model`: no browsing feed, no infinite list, no discovery surface.

## Scope

In scope:

- Manual `Add link` entry point from the home screen.
- User enters URL, title, estimated duration, and at least one topic tag.
- URL validation for `http` and `https` only.
- Local persistence for user-added links.
- User-added links are merged into the recommendation inventory.
- User-added links can be recommended as primary or backup items.
- Link reading uses a controlled fallback: open the URL in an external browser or Custom Tab-style handoff if available.
- Local analytics events distinguish editorial items from user-added links.
- Debug/export visibility is sufficient for engineering validation.

Out of scope:

- Automatic article extraction.
- Open-web crawling.
- AI summarization.
- RSS, newsletter, or browser-extension ingestion.
- PDF upload.
- Rich read-later management, folders, tags beyond existing topic tags, highlights, notes, or archive workflows.
- Cloud sync or accounts.
- Public Play Store distribution hardening.

## Product Rules

- The intervention surface must still show exactly one primary recommendation and two backups.
- Adding links must not create a scrollable recommendation feed.
- The app should not promise native article readability until extraction exists.
- The user should always understand whether they are opening local editorial content or an external link.
- If a user-added link is unavailable or malformed, the system should log the condition and fall back gracefully instead of silently failing.

## Data Model Direction

Introduce explicit source metadata instead of overloading editorial fields forever.

Recommended domain additions:

- `ContentSourceType`: `EDITORIAL`, `USER_LINK`.
- `ContentAvailability`: `AVAILABLE`, `UNAVAILABLE`, `NEEDS_FALLBACK`.
- `ContentItem.externalUrl: String?`.
- `ContentItem.bodyAssetPath: String?` or a sealed content body reference.

Persistence direction:

- Keep editorial starter packs asset-backed.
- Add a Room table for user links.
- Expose a composite repository that combines asset-backed editorial inventory with persisted user-link inventory.
- Do not move editorial content into Room in this sprint unless required by implementation simplicity.

## Slices

### Slice 4.1: Domain and Persistence Foundation

Acceptance criteria:

- Domain model can represent editorial content and user-added links.
- A `UserLinkRepository` or equivalent local repository can create, list, and mark user links unavailable.
- Room migration or database versioning is handled safely.
- Unit tests cover URL validation and persistence mapping.
- Existing editorial recommendation tests still pass.

Suggested tests:

- Valid `https://` and `http://` URLs are accepted.
- Non-web schemes are rejected.
- User links round-trip through Room.
- Editorial asset inventory remains available after adding user-link persistence.

### Slice 4.2: Add Link UI

Acceptance criteria:

- Home screen exposes a clear `Add link` action.
- User can enter URL, title, duration bucket or minutes, and topic tags.
- Invalid input gives a local, non-moralizing error.
- Successful save returns to home and logs a local event.
- No onboarding requirement changes yet; this keeps alpha testers unblocked.

Suggested tests:

- Compose/UI test for disabled save with invalid URL.
- Compose/UI test for successful save path.
- ViewModel test for state transitions and analytics event mapping.

### Slice 4.3: Recommendation Inventory Integration

Acceptance criteria:

- User-added links participate in recommendation ranking.
- Completed user links are excluded from primary slots like editorial content.
- Ranking v0 prefers topic and duration fit across both source types.
- Inventory shortage behavior remains explicit.
- Intervention still displays only three options.

Suggested tests:

- Recommendation engine can select a user link when it is the best topic/duration fit.
- Backup selection can mix editorial and user-link items.
- Completed user links are excluded from primary recommendation slots.
- Inventory shortage event still fires when combined inventory is too small.

### Slice 4.4: Link Open/Fallback Session

Acceptance criteria:

- Accepting a user link starts a replacement session and logs the same core accept/session events as editorial content.
- The app clearly hands the user to the external URL when native rendering is not available.
- Fallback open is logged with the content id and target app context.
- Feedback still attaches to the original replacement session.

Suggested tests:

- ViewModel test for accepting a user link.
- Analytics mapping test for fallback open.
- Manual test on Android device for URL handoff from an intervention.

## Parallel Work Discipline

- Keep `main` stable for alpha testers.
- Keep alpha feedback fixes on a separate branch, for example `codex/alpha-feedback-hardening`.
- Keep this sprint on `codex/sprint4-user-links`.
- Merge alpha feedback fixes first when they affect shared app state, analytics, or interception.
- Rebase or merge `main` into the feature branch before each review packet or Pro review.
- Do not publish a new tester APK from the feature branch until the feature has passed review and manual smoke.

## Definition of Done

- User can add at least one web link locally.
- Added links survive app restart.
- Added links can appear in the existing three-option intervention.
- Accepting a link records a replacement session and feedback can be submitted.
- Analytics distinguishes editorial replacement from user-link replacement.
- Existing Sprint 0-3 behavior remains green.
- No PDF, RSS, newsletter, crawler, or AI summarization scope is introduced.

## Validation Plan

Automated:

- `./gradlew testDebugUnitTest --no-daemon`
- `./gradlew lintDebug --no-daemon`
- `./gradlew connectedDebugAndroidTest --no-daemon`

Manual:

- Add a valid link.
- Restart app and confirm the link remains present.
- Trigger intervention for a selected app.
- Confirm the link can appear as one of the three recommendation choices.
- Accept the link and confirm the handoff is clear.
- Submit feedback and confirm history/analytics stay coherent.

Release gate:

- This sprint should not replace `v0.1.0-alpha` until internal testers have finished the current alpha pass or the feature is explicitly released as a separate experimental build.
