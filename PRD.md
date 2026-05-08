# Product Requirements Document

## Product

Quality Replacement at the Moment of Impulse

## Document Status

Draft v1

## Intended Audience

Founders, product, design, engineering, and early go-to-market collaborators

## Product Decision

Build an Android-first MVP that intercepts attempts to open selected distracting apps and replaces the impulse with one recommended piece of high-quality long-form content, a bounded set of backup options, and a conscious override path. The core product is a replacement engine, not a blocker and not a read-later library.

## Background

Existing products solve adjacent problems but not the full user need. Digital wellbeing apps such as one sec, Clearspace, Jomo, Opal, and AppBlock help users interrupt automatic entry into distracting apps. Reading and knowledge products such as Matter, Readwise Reader, and Blinkist make it easier to collect and consume worthwhile content. The market gap sits between those two categories.

When a user taps Instagram, X, Reddit, TikTok, or YouTube out of habit, current products usually do one of two things: they add friction before the user enters, or they ask the user to maintain a separate queue of better things to read later. Very few products make a strong, immediate substitution in that exact moment.

The product thesis is that the moment of impulse is the highest-leverage point in the habit loop. A short delay plus a credible substitute can convert an automatic opening into a more intentional choice. The substitute must be specific, finite, and good enough to feel like relief rather than punishment.

## Problem Statement

Knowledge-oriented smartphone users often open distracting apps habitually, not intentionally. They may already want to spend more of that time on essays, articles, PDFs, or other long-form material, but they do not have a system that helps them make that switch in the exact moment the habit fires.

Current blockers create friction without solving the "what do I do instead?" problem. Current reading products improve access to quality content without solving the "when should I choose it?" problem. As a result, users still default into low-intention scrolling even when they have both the desire and the available content to do something better.

## Goals

- Help users interrupt low-intention opens of selected distracting apps.
- Offer one highly relevant, high-quality replacement immediately at the point of impulse.
- Preserve user agency through a conscious override instead of default coercion.
- Learn which sources, formats, and contexts produce successful substitution.
- Prove that a replacement-first experience can deliver measurable behavioral change beyond generic blocking.

## Non-Goals

- Compete with generic app blockers on strictness alone.
- Build a full-featured read-later, RSS, or PDF management product in MVP.
- Create an infinite recommendation feed or discovery surface inside the intervention flow.
- Promise full Android/iOS feature parity at launch.
- Depend on a heavy AI system that crawls the open web to find the perfect item in real time.
- Support every distracting surface type, app, or edge-case interception workflow in v1.

## Target User

### Primary User

A knowledge-oriented adult smartphone user who:

- repeatedly opens a small set of distracting apps out of habit
- already values reading, learning, or reflective downtime
- wants help redirecting the impulse without feeling punished
- is willing to configure a lightweight intervention system if it clearly improves daily behavior

### User Characteristics

- Uses at least two high-frequency distracting apps several times per day.
- Already saves articles, PDFs, newsletters, highlights, or links somewhere, but inconsistently returns to them.
- Is open to behavior-shaping tools but does not want a permanently locked-down phone.
- Prefers a product that feels tasteful and editorial rather than moralizing or productivity-heavy.

### Users Explicitly Out of Scope for MVP

- Users seeking a parental-control or family-management tool.
- Users who want a pure time-tracking dashboard without behavior intervention.
- Users whose main need is a complete reading workflow with advanced annotation, sync, and archive features.

## Job to Be Done

When I reach for a distracting app out of habit, help me pause and give me one worthwhile thing to do instead, so I can convert the impulse into a better choice without feeling trapped.

## Product Principles

- Replace, do not merely block.
- Offer one smart suggestion rather than many mediocre options.
- Keep the intervention finite, calm, and fast.
- Preserve agency through a clear override path.
- Treat curation quality as the main moat.
- Do not recreate the scrolling behavior the product is meant to prevent.

## MVP Scope

### In Scope

- Android mobile app with system-level interception for a user-selected set of distracting apps.
- Supported distracting apps at MVP launch:
  - Instagram
  - X
  - YouTube
  - Facebook
  - Reddit
  - TikTok
- Soft intervention shown when a selected app is opened.
- One primary replacement recommendation plus a short, bounded backup list.
- Three core actions in the intervention: start replacement now, delay the distracting app for 15 minutes, open anyway.
- Replacement sources from:
  - editorial starter packs curated by the product
  - user-added links
  - user-owned documents, starting with PDF, Markdown, and EPUB import
  - a lightweight saved queue maintained inside the app
- Minimal preference capture for topic interests and preferred format. Session-length settings may shape a reading segment or reminder, but must not exclude or down-rank long source materials.
- A simple reader or handoff experience for articles and user-owned documents.
- Lightweight post-session feedback with two questions.
- Event logging and analytics needed to evaluate substitution behavior.

### Out of Scope

- iOS system-interception parity at launch.
- Full RSS ingestion.
- Full newsletter inbox integration.
- Browser extensions.
- Audio-first replacement sessions.
- Social surface blocking at the sub-surface level, such as Reels-only or Shorts-only blocking.
- Full writing-system annotation workflows beyond replacement reading.
- MVP-supported reader annotations are in scope only when they stay tied to actively shown reader text, remain locally manageable, use a portable annotation format, and can sync to a user-authorized Google Drive destination.
- Team, family, or accountability features.
- Hard-block mode as the default experience.

## Primary Use Case

The user taps Instagram on Android during an unplanned idle moment. Instead of entering Instagram immediately, the user sees a brief intervention card. The card shows one recommended reading segment or utility option, plus a short backup list. A long EPUB or Markdown book can be recommended as the next manageable segment from the user's current position, without being penalized because the whole source is long. If an item is already partly read, the intervention shows reading progress and remaining time so the user can continue without extra setup. The user chooses "Read now," spends several minutes in the replacement content, and either never returns to Instagram or returns later with more intention. After the session, the app asks whether the recommendation was a good fit and whether it helped prevent mindless scrolling.

## End-to-End User Flow

### 1. Onboarding

- User installs the app.
- User grants the required permissions for Android interception.
- User selects at least three distracting apps from a supported list.
- User chooses at least three topic interests.
- User may choose a preferred replacement-session target:
  - 3-5 minutes
  - 5-10 minutes
  - 10-20 minutes
- The target describes the next reading segment or timer length. It is not stored as the duration of imported materials and is not an eligibility filter for recommending long materials.
- User seeds the system by:
  - selecting at least one editorial starter pack
  - adding at least three links or one user-owned document

### 2. Intervention Trigger

- User opens a selected distracting app.
- The system intercepts the attempt before the distracting app becomes the primary experience, where platform support allows.
- The intervention view appears within a target of two seconds from app-open detection.

### 3. Intervention View

- The view shows:
  - the app the user attempted to open
  - one primary recommendation selected by topic, priority, freshness, unfinished state, availability, and past feedback rather than whole-source length
  - a short, bounded backup list
  - automatically calculated reading-segment time or timer time for each recommendation
  - progress already read and remaining time for unfinished continuation recommendations
  - three actions:
    - Read now
    - Pause 15 min
    - Open anyway

### 4. Replacement Session

- If the user chooses a recommendation, the product opens the content inside the app when possible.
- If the content cannot be rendered natively, the user is sent through the least-jarring supported fallback.
- The app tracks whether the user engaged with the replacement for at least 30 seconds, 3 minutes, and 7 minutes.

### 5. Delay Flow

- If the user chooses Pause 15 min, the product records a delay window and suppresses repeated prompts for the same distracting app during that period.
- If the user tries again during the delay window, the system may either re-show a lighter reminder or respect the delay silently, depending on implementation constraints. MVP should default to respecting the delay silently after confirmation.

### 6. Override Flow

- If the user chooses Open anyway, the selected distracting app opens.
- The event is still logged for learning and analytics.

### 7. Post-Session Feedback

- After a replacement session ends or becomes inactive, the user sees two optional prompts:
  - Was this a good fit?
  - Did this help you avoid mindless scrolling?
- Responses feed the ranking model and analytics dashboard.

## Functional Requirements

### FR1. Distracting App Selection

The system must allow the user to choose which apps should trigger an intervention.

#### Acceptance Criteria

- User can select and save a supported set of distracting apps during onboarding.
- User can edit the selected set later from settings.
- The product requires a minimum of three selected apps during onboarding unless fewer than three supported apps are installed.
- The product stores selections locally and restores them after app restart.

### FR2. Permission and Interception Setup

The system must guide the user through the permissions required for Android-first interception.

#### Acceptance Criteria

- The app explains why each permission is required before requesting it.
- The onboarding flow clearly indicates whether setup is complete, incomplete, or partially working.
- If required permissions are denied, the app provides a retry path and a limited-mode explanation.
- The app never claims interception is active when required permissions are missing.

### FR3. Replacement Source Setup

The system must give the user enough replacement inventory to support useful recommendations from day one.

#### Acceptance Criteria

- User can choose one or more editorial starter packs during onboarding.
- User can add a web link manually.
- User can upload at least one PDF, Markdown, or EPUB document manually.
- User can save at least ten content items before any archival or overflow handling is needed.
- Newsletter forwarding, inbox sync, and RSS import are not required for MVP.

### FR4. Content Item Model

The system must normalize each piece of replacement content into a small, rankable unit.

#### Acceptance Criteria

- Each content item stores:
  - title
  - source type
  - topic tags
  - format
  - automatically computed reading-length metadata when the app has body text
  - availability status
- User-added or imported materials must not require the user to save a manual reading-time estimate.
- Long readable materials can be recommended as smaller continuation segments from the current reader position.
- Each content item can be marked as accepted, skipped, completed, or opened anyway after recommendation.
- The system excludes previously completed reading items from recommendation slots unless the user manually reactivates that item from the content library.
- The meditation reset is not library content, does not count as a completed read, and is not hidden by completion state; it remains a standing intervention option.

### FR5. Recommendation Selection

The system must choose one primary recommendation and a short, bounded backup list for each intervention.

#### Acceptance Criteria

- The ranking logic uses, at minimum:
  - user topic preferences
  - time of day
  - prior accepts
  - prior skips
  - source availability
- The ranking logic must not penalize a readable item because the whole source is longer than the user's session target.
- The system returns exactly one primary recommendation and at least two backups when enough inventory exists.
- The system may expose additional finite backups in a bounded scrollable list, capped below feed-like scale.
- The primary recommendation may expose the next automatically calculated reading segment length, but the segment length is presentation metadata rather than a whole-source ranking filter.
- Backup recommendations are ranked by usefulness, priority, unfinished state, freshness, and availability rather than being capped to the primary item's total reading length.
- If the primary recommendation is reading content and the meditation reset is otherwise eligible, the finite backup list keeps meditation visible as an alternative even when reading items dominate the ranked inventory.
- If fewer than three suitable items exist, the system still returns at least one recommendation and logs an inventory-shortage event.
- The intervention flow must not require the user to browse the backup list before making a choice.

### FR6. Intervention UI

The intervention view must slow down the habit loop without feeling punitive.

#### Acceptance Criteria

- The intervention displays within a target of two seconds from interception trigger under normal conditions.
- The intervention emphasizes one primary recommendation and keeps backup recommendations in a bounded list.
- The intervention includes a clear path to open the original app.
- The intervention does not contain an infinite list, feed, or discovery surface.
- A bounded backup list may scroll when more than two finite alternatives are available.
- The intervention copy avoids moralizing language.
- If a recommendation continues unfinished reading, the intervention shows percent already read and remaining estimated time without requiring the user to open the reader first.

### FR7. Core User Actions

The intervention must provide three clear actions that map to the MVP value proposition.

#### Acceptance Criteria

- User can choose Read now for the primary recommendation.
- User can choose one of the backup recommendations instead of the primary item.
- User can choose Pause 15 min, which creates a 15-minute delay window.
- User can choose Open anyway immediately in Soft intervention mode.
- User can choose Open anyway after a short, visible 5-second form intervention wait in Firm intervention mode.
- When enabled, the wait is presented before unlock is available, not as an extra screen after the user chooses Open anyway.
- After Open anyway from a system intervention, repeated opens of the same distracting app are quiet for the user-configured unlock window.

### FR8. Replacement Session Experience

The system must deliver a calm replacement session that feels meaningfully different from opening the distracting app.

#### Acceptance Criteria

- The replacement session opens without showing additional recommendation choices first.
- The session presents the chosen content in a readable format when native rendering is supported.
- If the item is a user-owned Markdown or EPUB document, the user can read it inside the app.
- If the item is a user-owned PDF, the user can open it through a clearly controlled Android document-viewer fallback.
- Long-form reader surfaces show reading-position progress that is based on document position, not only elapsed time.
- Long-form reader progress is durably refreshed on page moves, backward moves, lifecycle pause/stop, and reader disposal so lock/unlock or app reopen returns to the last viewed source-anchored position.
- The in-app reader is paginated by default. It must not rely on vertical scrolling for active reading.
- Reader pagination must adapt to the actual page viewport, device size, orientation where supported, and the user's in-app reader font-size setting. It should fill the page comfortably without clipping text or leaving large avoidable empty regions.
- Reader font size is a first-class app setting, not something the user must change at Android system level. System accessibility font scale may still be respected by the platform, but the product must expose its own reader font-size control.
- Reader text size must be adjustable with compact numeric controls, immediate plus/minus changes, and a live preview of the effective reader text size. It must not be presented as a large modal with only coarse named choices.
- The app may expose a separate interface text or interface size control when reader tuning alone is not sufficient. Reader text size and interface size must be stored distinctly so changing active reading typography does not unexpectedly resize all Settings controls.
- Tapping most of the page advances to the next page. Previous-page navigation must use reader gestures or a very small left-edge tap zone; the Android back gesture or system back action exits the active reader to the prior app screen after first dismissing any open reader overlay.
- The active reader uses minimal reader chrome: no persistent Previous, Next, or Done buttons; content is the primary surface, with only tiny title/progress/TOC affordances outside the text.
- EPUB readers expose table-of-contents navigation that jumps to the matching section or nearest available page without turning the reader into a discovery surface.
- Meditation replacement duration can be adjusted by the user and plays a completion gong.
- The session avoids recommendation carousels or unrelated content suggestions during active reading.
- The app can record session duration for analytics.
- If the user annotates active reader text, the note must stay linked to the specific content item and reader fragment.

### FR8A. Reader Annotations

The system may let the user capture notes on actively shown reader text without turning the replacement session into a separate writing or browsing workflow.

#### Acceptance Criteria

- User can add or edit an annotation by long-pressing actively shown text in the reader.
- Long-press selection starts with the sentence under the press and lets the user adjust the selected range before saving.
- Annotation creation and editing use an overlay or popup that does not expand the page, enable vertical scrolling, or change pagination. The note surface may grow up to the available viewport when the selected quote is long, and once it reaches that limit the quote/note region must scroll internally rather than forcing the reader page to scroll.
- Annotation range adjustment must use minimal, icon-first start/end controls instead of verbose full-width labels, and the controls must actually refine the range inside a single sentence instead of becoming inert whenever only one sentence is selected.
- Cross-page annotation selection must be page-aware: the app must preserve source-anchored start/end positions across paginated display pages, keep the overlay from expanding the reader page, and avoid conflicts with reader next/previous navigation. Start/end adjustment must not be hard-limited by the currently visible page when the underlying source text continues before or after it.
- Notes must not be represented as margin icons in the active reading page.
- Each annotation stores content identity, source title, fragment position, quoted fragment text, selector/range data, note text, and timestamps.
- The app may offer an explicit `Ask AI` action while saving a reader annotation. This must be optional, user-triggered, and clearly separate from ordinary note saving. The ordinary local annotation must be saved first and must remain available even if the AI request fails.
- `Ask AI` may send the full source material, the selected quote, the source-anchored selector/range, and the user's note text to an external LLM provider only after clear consent and configuration. It must not silently summarize, omit, or replace the source material; if the material cannot fit the configured model context, the app must fail visibly or use a non-lossy multi-call strategy rather than sending a lossy summary.
- AI responses must stay linked to the original annotation and source fragment, and export/sync must distinguish user-authored note text from model-generated commentary.
- User can review annotations in a finite annotation library and return to the source fragment.
- User can authorize Google Drive sync for annotations from settings.
- On a supported Android device with Google Play services and a valid signed-in Google account, Google Drive authorization must complete and leave annotation sync in a connected state. Better failure copy alone is not an acceptable substitute for a working Drive connection.
- Drive sync writes one annotation file per annotated source material, with the source title included in the filename.
- The canonical export format is W3C Web Annotation JSON-LD. EPUB annotations should include an EPUB locator when available, such as an EPUB CFI or stable source-section locator.
- Annotation export failures are visible and do not lose local annotations.
- Annotation autosave must have a working local default destination so saving annotations does not require the user to choose a folder before first use. User-selected local folders and Google Drive sync are optional replacements or supplements to that default.
- Google Drive authorization failures must distinguish user cancellation from technical or configuration failure where Android APIs allow it, present a retry path, and never erase the local annotation library.

### FR9. Delay Behavior

The system must allow the user to consciously postpone access to the distracting app for 15 minutes.

#### Acceptance Criteria

- When Pause 15 min is selected, the system records the delay window.
- During the delay window, repeated attempts to open the same app do not create a full repeated intervention by default.
- The delay expires automatically after 15 minutes.
- The app logs whether the user returned after the delay window ended.
- Settings include a configurable Open anyway unlock duration so the user can choose a longer quiet window without changing the 15-minute pause behavior.
- Settings include a working Soft/Firm intervention mode selector. Soft mode keeps Open anyway immediate; Firm mode adds the 5-second form-intervention pause before Open anyway becomes available.

### FR10. Feedback Loop

The system must collect lightweight signals after replacement sessions.

#### Acceptance Criteria

- Post-session feedback consists of no more than two questions in MVP.
- Feedback is optional and dismissible.
- Feedback responses are attached to the associated intervention event.
- Recommendation ranking can consume accept, skip, fit, and helped/not-helped signals in later iterations.

### FR11. History and Review

The user must be able to see a simple record of recent replacement activity.

#### Acceptance Criteria

- User can view recent recommended items accepted in the last seven days.
- User can see whether each recent item was completed, skipped, or followed by opening the distracting app.
- The history view is informational only in MVP and does not require advanced filtering.

### FR12. Analytics Instrumentation

The product must log enough behavior to evaluate whether substitution is working.

#### Acceptance Criteria

- The system logs:
  - intervention shown
  - primary item accepted
  - backup item accepted
  - delay selected
  - open anyway selected
  - replacement session duration
  - feedback submitted
  - return to distracting app within 15 minutes
  - return to distracting app within 60 minutes
- Each event includes timestamp, target app, recommendation identifiers, and session context.
- Analytics can distinguish between no recommendation available and user chose Open anyway.

### FR13. Portable Profile

The system must provide account-like portability without requiring a Quality Alternative server account or backend authentication.

#### Acceptance Criteria

- The app creates and persists a local profile identifier on the device.
- User can export a versioned `quality-alternative-profile.json` file from Settings.
- User can import a compatible profile file from Settings and choose a safe restore mode.
- Profile export includes portable settings, selected distracting apps, topic preferences, starter-pack choices, reader font-size preference, content priority settings, user links, user-document metadata, reading progress, completed/reactivated state, and annotation sync/export preferences that are safe to move across devices.
- Profile export must not include Google access tokens, authorization grants, OAuth secrets, raw Drive file ids, raw Android `content://` or `file://` URIs, raw SAF tree URIs, account emails, raw analytics event logs, Android-only permission state, or unredacted platform-internal identifiers that would be misleading on another device.
- User-owned document binaries are not exported in the initial portable profile. The profile may preserve document metadata, stable content identity, and safe document fingerprints, but import must clearly mark missing or unverified local document files as unavailable until the user reattaches and verifies them.
- The profile format is schema-versioned and must reject unsupported future versions without corrupting local state.
- Import failures are visible and must not partially overwrite local settings or library data. Merge mode must preserve local settings unless the user explicitly chooses to apply imported settings, and replace mode must show affected scope before destructive mutation.
- Portable Profile may optionally autosave the profile to a user-authorized local or Google Drive destination, but imported autosave or Drive metadata must be informational only until the current device user reselects a destination or reauthorizes Drive. The canonical product behavior must remain usable without a backend server.
- Portable Profile autosave must also have a working local default destination. Choosing a folder changes where portable backups are written; it is not a prerequisite for the first successful local profile backup.
- Settings copy must make the distinction between annotation autosave/sync and Portable Profile backup clear: annotations export notes tied to reader text, while Portable Profile backs up app configuration, library metadata, and reading state.

## Non-Functional Requirements

### NFR1. Response Time

- On supported Android devices in normal operating conditions, the intervention should appear within two seconds of detecting a selected app-open event.
- If the intervention cannot be shown in that window, the app should log a degraded-performance event.

### NFR2. Calm Interaction Model

- The primary recommendation and core actions must remain usable without requiring vertical browsing.
- Any vertical scrolling in the intervention must be limited to the bounded backup list and must not become a feed-like discovery surface.
- The replacement session must not show unrelated recommendations while the user is actively consuming the selected item.

### NFR3. Reliability Signaling

- The product must clearly indicate when interception permissions are missing, degraded, or device-limited.
- The product must not silently fail while claiming protection is active.

### NFR4. Local-First Portability And Privacy

- Portable Profile data must be local-first and user-controlled.
- Portable profile exports must be human-auditable JSON rather than opaque database backups.
- Export/import should preserve behaviorally important state without pretending to provide server-backed identity, cross-device conflict resolution, or guaranteed document-file transfer.
- Any cloud sync must be explicit, revocable, and scoped to user-owned files. Cloud authorization state must be re-established on each device rather than copied through profile export.

## Content Strategy for MVP

The MVP should not depend on a large-scale AI recommendation system. Instead, it should use a bounded inventory and simple ranking rules.

### Content Sources

- Renderable editorial starter packs created, licensed, public-domain verified, or Creative Commons-cleared for product use
- Link-only external recommendations where the product stores metadata and opens the canonical source without reproducing the full work
- User-private saved links and user-owned documents that are available only to the user who added them
- Lightweight in-app saved queue with source and rights status attached to each item

### Content Rights and Rendering Policy

- Every shared content item must be classified before external pilot as `renderable`, `link_only`, or `user_private`.
- `renderable` content may be shown in the in-app reader only when the product has a clear basis to reproduce and display it, such as first-party authorship, written license, verified public-domain status, CC0, or a Creative Commons license compatible with commercial reuse and attribution.
- `link_only` content may be recommended and opened at the original source, but should not be scraped, cached, rehosted, summarized as a substitute, or displayed in reader mode without permission.
- `user_private` content may be displayed only to the user who added it and should not become shared inventory.
- Non-commercial Creative Commons content should not be used as shared renderable inventory for a product that may later monetize unless separate permission is obtained.
- Public-domain status must be checked by jurisdiction and edition. Modern translations, introductions, annotations, recordings, or publisher editions can have separate rights.
- The operating policy for content sourcing is `docs/CONTENT_POLICY.md`.
- Rights and source checks are a selection, triage, and inventory-audit responsibility. Runtime should follow explicit item metadata rather than acting as a hidden copyright-blocking engine.

### Editorial Inventory Requirement

- MVP launch should include at least 50 editorial items across at least 5 starter packs.
- Each starter pack should contain at least 10 items.
- Every editorial item should have topic tags, estimated duration, and a readable title and description.
- Before external pilot, every editorial item should have source URL, author/source attribution, rights status, render mode, and review date.
- Starter-pack metadata must not imply affiliation with real publications unless the product has the right to use that source relationship.

### Ranking Inputs

- explicit topic preferences
- time of day
- recent acceptance history
- recent skip history
- item completion history
- source freshness
- computed reading progress and segment availability for resume/presentation

### Deliberate Exclusions

- open-web crawling in real time
- generative summarization as the main replacement format
- infinite content feeds
- broad social discovery surfaces inside the app

## Platform Scope and Constraints

### Launch Platform

Android only for system-level interception MVP

### Android Constraints

- Android permissions for interception and overlays are sensitive and must be disclosed clearly.
- Overlay reliability may vary across OEMs and app contexts.
- Some apps and device states may reduce the reliability of the intervention surface.
- The product must be narrow and deterministic enough to reduce policy risk around accessibility-style permissions.

### iOS Constraints

- iOS may eventually support a deeper system product through Screen Time frameworks.
- iOS launch is not part of MVP because entitlement access, shield UX, and routing constraints materially increase execution risk.
- A lighter iOS prototype may be explored later, but it is not launch-blocking for MVP.

## Dependencies

- Android mobile client capable of interception and content rendering
- Basic content store for links, user-owned documents, starter packs, and recommendation state
- Analytics pipeline for intervention and session events
- Editorial workflow for starter-pack creation and maintenance

## Risks

### Product Risks

- Users may still open the original app too often, reducing perceived value.
- Recommendations may feel too weak or too generic if starter inventory is poor.
- The product may drift into a generic blocker if replacement quality is not strong.
- The product may drift into a read-later shelf if intervention quality is weak.

### UX Risks

- Too much friction can cause uninstalls or permission drop-off.
- Too little friction can make the product ineffective.
- Poorly timed or repetitive recommendations may feel nagging rather than helpful.

### Platform Risks

- Android permission sensitivity may create store-review or trust issues.
- Interception reliability may vary across devices.
- iOS parity expectations may create pressure before the Android wedge is proven.

## Success Metrics

### Core MVP Metrics

- Intervention abandonment rate:
  share of intercepted opens that do not lead to opening the target app within 5 minutes
- Replacement acceptance rate:
  share of interventions that lead to a primary or backup recommendation being opened
- Reclaimed minutes:
  minutes spent in replacement sessions instead of the target app
- Replacement completion rate:
  share of accepted recommendations consumed past defined depth thresholds
- Return rate:
  share of users who still open the target app within 15 and 60 minutes after intervention
- Perceived usefulness:
  share of feedback responses indicating the recommendation was a good fit and helped avoid mindless scrolling

### Pilot Success Thresholds

The MVP pilot should be considered promising if, within the first structured pilot cohort:

- at least 20 percent of intercepted opens do not lead to opening the target app within 5 minutes
- at least 15 percent of interventions lead to a replacement session of 3 minutes or more
- at least 50 percent of submitted fit responses are positive
- at least 40 percent of weekly active pilot users engage with at least one replacement session per week

These thresholds are directional go/no-go signals, not long-term company targets.

## Rollout Plan

### Phase 1

Internal alpha on Android with a narrow app-support list and editorial starter packs only

### Phase 2

Small external pilot with user-added links and user-owned documents enabled

### Phase 3

Decision on whether to invest further in:

- stronger ranking
- premium hard-block mode
- additional content ingestion sources
- free/premium packaging
- streaks or other motivation mechanics that reward successful substitutions and meaningful reading time
- future iOS exploration

## Open Questions

- What should the premium tier include first after MVP: hard-block mode, custom delay durations, or richer content ingestion?
- Should the free tier combine core interception, basic content, and a limited personal library, with premium reserved for a broader curated library and materially higher or unlimited user-owned content capacity?
- How strong should streak mechanics become without turning progress into shame or loss aversion?
- At what level of Android-device variability should the team narrow the officially supported-device list for public launch?

## Launch Recommendation

Proceed with an Android-first MVP that proves the replacement-first thesis. The product should be evaluated not as a better blocker and not as another reading app, but as a system that turns app-opening impulse into a high-quality alternative in the moment that matters most.
