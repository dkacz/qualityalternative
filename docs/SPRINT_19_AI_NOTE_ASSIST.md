# Sprint 19 - Reader Regression, Form Intervention, And AI Notes

Status: `meditation_calm_alternative_in_progress`

Requested on: 2026-05-07

## Goal

Start Sprint 19 by fixing the current reader and intervention regressions, release an APK with those fixes, and only then move into AI-assisted annotation notes in the second part of the sprint. AI work must not begin until there is a reviewed, installable APK that fixes annotation selection, progress, and form intervention. If late tester regressions appear before AI begins, ship those reader/intervention hotfixes first.

Mapped PRD items: FR6, FR7, FR8, FR8A, FR13, NFR privacy, NFR reliability, NFR calm interaction model.

## User-Reported Problems To Fix First

- Annotation range adjustment is still unstable. When the user tries to move the start of an annotation backward, it can jump as if it moved to the beginning of the book.
- Reading progress can still be wrong. The user can be in chapter 3 while the app shows `1%`, which suggests progress is being calculated from the wrong anchor, wrong display block, or wrong scope.
- Form intervention is not working as desired. The desired behavior is a lightweight form-style unlock similar in feel to the existing edit-breath interaction: the user performs the small intervention and waits about 5 seconds before unlock is available.
- Reader session progress can still be too weakly persisted. After reading for a while, locking the device, and reopening, the app may return to the pre-session location rather than the last viewed page.
- Meditation is missing as a visible alternative when the primary recommendation is reading-heavy content. The standing meditation reset must remain available as a finite backup option.
- AI-assisted notes remain desired, but they come after these regressions and after a release APK containing the fixes.

## Product Rules

- Sprint 19 order is regression-first, release APK second, AI last.
- Annotation selection must be source-anchored. Moving the start backward may cross display pages and source blocks, but it must never teleport to the beginning of the book unless the actual selected source range begins there.
- Reading progress must be derived from the stable source location, not transient paginated display indexes. If the UI shows whole-material progress, chapter navigation and font repagination must not reset it to a misleading low value. If any chapter-local progress is shown, the UI must label that scope clearly.
- Form intervention must be calm and bounded. It may slow unlocking with a short 5-second wait, but it must not become punitive, feed-like, or confusing.
- Reader progress persistence must be lifecycle-safe. The app must durably refresh the current source-anchored reader position on page moves, backward moves, lifecycle pause/stop, and reader disposal.
- Meditation remains a standing intervention option. When the primary recommendation is reading content and meditation is eligible, it must be presented as a distinct calm-reset alternative, not as a normal item inside `Other options`.
- Any form-intervention change must explicitly reconcile with FR7, which currently says no additional mandatory step is inserted after `Open anyway`. If Sprint 19 changes that behavior, the PRD must be updated intentionally instead of silently drifting.
- AI note assistance is optional and explicit. Ordinary annotation saving must keep working offline and without an AI key.
- No API key, OpenRouter credential, Google credential, account email, raw Drive file id, or model-provider secret may be exported through Portable Profile or annotation sync.
- Do not ship a private OpenRouter API key inside the APK. Sprint 19 must choose a safe tester configuration path before AI implementation, such as developer local properties for debug builds, a user-provided key, or a later backend/token broker.
- The active reader must remain calm and finite. `Ask AI` must not turn the reader into chat, browsing, or an infinite assistant feed.

## Implementation Notes

- 2026-05-07: EPUB reader extraction now assigns global source block indexes across spine documents instead of restarting at zero per chapter. This directly addresses chapter-local progress and annotation-selection jumps.
- 2026-05-07: Reader progress now resolves source positions by source block identity and preserves a source anchor across font-size repagination instead of holding only a display page number.
- 2026-05-07: Form intervention now gates `Open anyway` for 5 seconds with a visible waiting state; the close icon and button are disabled until the countdown completes.
- 2026-05-07: PRD FR7 was updated intentionally: the 5-second wait happens before `Open anyway` becomes available, not as an extra screen after choosing it.
- 2026-05-07: GPT Pro R1 regression gate returned `7/10 FAIL`; R2 work added raw logs, larger repagination evidence, saved/reopened annotation evidence, Portable Profile progress-autosave assertions, and form-intervention unlock/completion/abandonment analytics.
- 2026-05-07: GPT Pro R2 regression gate returned `10/10 PASS` with `VISUAL REVIEW: PASS`. Slice 19.5 release packaging may proceed.
- 2026-05-07: Slice 19.5 release candidate `v0.11.2-reader-regression-form-alpha` passed unit validation, debug APK build, connected reader/annotation E2E, connected form-intervention E2E, signature verification, emulator install smoke, launch smoke, and emulator shutdown.
- 2026-05-07: GPT Pro final release gate returned `10/10 PASS` with `VISUAL REVIEW: PASS`; tagging and GitHub release publication may proceed.
- 2026-05-08: A late tester regression requires an emergency hotfix before AI: reader session progress now refreshes durable storage on every visible page move, backward move, lifecycle pause/stop, and reader disposal; same-position lifecycle saves refresh the store without duplicate progress analytics.
- 2026-05-08: Meditation is restored as a guaranteed finite backup when the primary recommendation is reading and meditation is eligible.
- 2026-05-08: GPT Pro R1 hotfix review returned `8/10 FAIL`; R2 closes the blocker by persisting before reader back/skip clears active content and by rejecting late incomplete disposal saves after completed reading progress.
- 2026-05-08: GPT Pro R3 hotfix review returned `8/10 FAIL`; R4 moves completion-downgrade protection to the repository write boundary and adds a delayed unfinished-save/in-flight completion regression test.
- 2026-05-08: GPT Pro R4 hotfix review returned `10/10 PASS` with `VISUAL REVIEW: PASS`; release packaging may proceed.
- 2026-05-08: Release candidate `v0.11.3-session-progress-meditation-alpha` passed targeted unit validation, debug/APK build, connected session-progress E2E, connected meditation-backup E2E, signature verification, emulator install smoke, and launch smoke.
- 2026-05-08: A tester found Settings Mode was hard-coded to show Soft while behavior was Firm. Slice 19.5B adds a persisted Soft/Firm mode selector: Soft makes Open anyway immediate, Firm keeps the 5-second form-intervention pause. Visual E2E evidence covers both behaviors.
- 2026-05-08: A tester clarified meditation should not appear as a normal `Other options` row. Slice 19.5C separates eligible meditation into a calm-reset panel with its own start action and duration chips while leaving normal backup rows for reading/link/file alternatives.
- AI implementation remains blocked until the regression-fix APK and the emergency session-progress hotfix are reviewed and released.

## Slice Plan

### Slice 19.0: Contract And Regression Reproduction

Deliverables:

- Convert this sprint plan into a reviewable contract.
- Reproduce the annotation start-backward jump with an instrumented reader fixture.
- Reproduce the wrong `1%` progress state in a chaptered or multi-section source.
- Identify the current form-intervention path and the exact place where the 5-second unlock wait should live.
- Decide whether form intervention changes require a PRD update to FR7 before implementation.
- GPT Pro review of the contract before code begins.

Acceptance:

- The plan explicitly covers annotation jump, wrong progress, form intervention, and AI-last sequencing.
- Reproduction evidence exists for both reader regressions, or the plan records why a specific fixture is needed first.
- GPT Pro returns `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS` for the contract packet.

### Slice 19.1: Annotation Start-Backward Stability

Deliverables:

- Fix annotation start adjustment so moving backward walks stable source ranges instead of display-page or chapter-local indexes.
- Preserve end anchor while the start anchor moves backward.
- Keep cross-page selected quotes stable after save, reopen, pagination changes, and font-size changes.
- Add regression tests for backward movement from later chapters/sections.

Acceptance:

- Moving start backward from a later chapter never jumps to the beginning of the book unless the user actually reaches the first source range.
- Start and end controls can move across source/page boundaries without corrupting selected quote text.
- Connected visual evidence shows the selected range before movement, after backward movement, and after reopen.
- GPT Pro slice review returns `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS`.

### Slice 19.2: Reader Progress Scope And Chapter Correctness

Deliverables:

- Audit all reader progress inputs: source block index, text offset, chapter/section navigation, displayed page index, restored progress, and exported progress.
- Fix progress percent so it reflects the intended stable source scope and does not reset to `1%` when the reader is already in a later chapter.
- Ensure font-size repagination still preserves progress percent, building on Sprint 18's source-anchored progress hotfix.
- Add tests for chaptered material, imported documents, EPUB/table-of-contents jumps where applicable, and Markdown/plain-text sources.

Acceptance:

- A reader positioned in chapter 3 shows progress consistent with the whole source or a clearly labeled chapter-local scope.
- TOC/chapter jumps do not corrupt saved progress.
- Progress remains stable across reader font-size changes.
- Account Light/Portable Profile and annotation sync paths preserve the corrected progress anchors.
- GPT Pro slice review returns `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS`.

### Slice 19.3: Form Intervention 5-Second Unlock

Deliverables:

- Repair the form-intervention flow so it works end to end.
- Shape the interaction like the existing edit-breath style: small, finite, calm, and focused.
- Add a 5-second wait before unlock becomes available.
- Make unlock countdown, disabled state, completion state, and cancellation/back behavior visually clear.
- Record analytics for form shown, form completed, unlock enabled, unlock used, and abandonment.

Acceptance:

- The form intervention appears reliably from the intended trigger.
- Unlock is disabled for about 5 seconds, then becomes available.
- The user understands what is happening without moralizing copy.
- Tests cover timer behavior, unlock gating, cancellation, repeated trigger behavior, and analytics events.
- Visual evidence covers initial, waiting, unlock-ready, and completed states.
- GPT Pro slice review returns `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS`.

### Slice 19.4: Regression Gate Before Release APK

Deliverables:

- Run focused and full validation for annotation selection, reader progress, form intervention, Google Drive annotation sync, and Portable Profile.
- Produce visual evidence for the three regression areas.
- Run GPT Pro regression gate before building the first Sprint 19 APK.

Acceptance:

- Annotation start-backward, progress correctness, and form intervention all pass tests and visual review.
- GPT Pro regression gate returns `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS`.
- Only after this gate passes may Sprint 19 build the regression-fix APK.

### Slice 19.5: Regression-Fix APK Release Before AI

Deliverables:

- Version bump and release notes for the regression-fix APK.
- Full unit and connected Android validation.
- APK build, signature verification, emulator install smoke, and emulator shutdown evidence.
- GitHub release with the installable debug APK.
- Changelog versus the previous release, focused on annotation selection, progress correctness, and form intervention.

Acceptance:

- The APK installs successfully and contains the annotation/progress/form-intervention fixes.
- GPT Pro has already returned `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS` for the regression gate.
- Release notes clearly state that AI note assistance is not included yet and starts only in the second part of Sprint 19.
- Only after this APK is released may Sprint 19 begin AI note work.

### Slice 19.5A: Session Progress Durability And Meditation Backup Hotfix

Deliverables:

- Persist the current reader source position after forward page moves, backward page moves, lifecycle pause/stop, and reader disposal.
- Refresh durable reading-progress storage even when the visible source position is unchanged, while avoiding duplicate progress analytics for lifecycle retries.
- Preserve meditation as a visible finite backup alternative when the primary recommendation is reading content and meditation is eligible.
- Add unit and connected Android coverage for same-position lifecycle saves, reopen-to-last-viewed-page behavior, and meditation backup visibility.
- Produce visual evidence and GPT Pro review before any AI implementation begins.

Acceptance:

- After reading, navigating backward, closing/reopening the app, or lock/unlock-style lifecycle interruption, the reader returns to the last viewed source-anchored page rather than the pre-session location.
- The durable reading-progress row is refreshed on lifecycle stop/pause/disposal even when the UI page did not visibly change.
- Recommendation backups include the meditation reset when reading dominates the ranked list and meditation is otherwise eligible.
- GPT Pro hotfix review returns `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS`.

### Slice 19.5B: Intervention Mode Settings Fix

Deliverables:

- Replace the static Settings Mode rows with a real persisted `Soft`/`Firm` intervention mode.
- Keep the current released behavior as `Firm`: Open anyway becomes available after the 5-second form-intervention wait.
- Make `Soft` mode immediate: Open anyway is enabled as soon as the intervention appears.
- Include the mode in Portable Profile export/import without breaking older profiles that do not yet contain the field.
- Add unit coverage for persistence and behavior, plus connected visual E2E coverage for Settings selection, Soft behavior, and Firm behavior.

Acceptance:

- Settings no longer always shows Soft as selected.
- Tapping Soft persists `SOFT` and removes the Open anyway countdown.
- Tapping Firm persists `FIRM` and restores the 5-second countdown.
- GPT Pro review must pass before this fix is included in a release APK.

## AI Note Feature - Second Part Of Sprint 19

### User Story

When adding a note to selected reader text, the user can choose ordinary `Save` or `Ask AI`. `Ask AI` must save the note first and then request an AI response that understands the annotation in the context of the complete material, not a summary.

### Requested Model And Provider

- Target provider path: OpenRouter API.
- Requested target model: Google Gemini 3.1 Flash Lite Preview.
- Implementation must verify the current OpenRouter model id, context window, pricing, rate limits, and availability at Sprint 19 implementation time. The exact model name is treated as a requested target, not a hard-coded assumption until verified.

### Prompt Design Requirements

Sprint 19 must design and review a strong prompt before implementation. The prompt should adapt to the nature of both the annotation and the source material.

The prompt must provide:

- Full source material, preserving original order and section boundaries.
- Source title, content id, material type when known, and reader location metadata.
- Exact selected quote.
- Source-anchored selector/range data, including paragraph/block index and text offsets where available.
- User note text.
- Instruction to answer only from the provided material unless explicitly marking outside knowledge as unavailable.
- Instruction to quote short exact fragments only when needed and otherwise paraphrase.
- Instruction to identify uncertainty, ambiguous anchors, or missing context instead of inventing details.

The response should adapt by annotation character:

- Question or confusion: explain the selected passage in context, identify the exact source basis, and state what remains unclear.
- Disagreement or critique: steelman the source's position, then evaluate the user's objection against the material.
- Connection or synthesis: connect the selected passage to other relevant parts of the full source, with exact section/anchor references where possible.
- Definition or term note: define the term from the material first, then explain nearby usage.
- Quote capture: explain why this passage may matter in the source's argument without overproducing.
- Action or reflection note: distinguish what the material says from what the user might do with it.
- Long/complex source: provide a structured answer with local passage context, broader source context, and a concise takeaway.

The first implementation should produce one high-quality assistant response, not an ongoing chat thread.

### Slice 19.6: AI Contract, Model Verification, And Prompt Review

Deliverables:

- Verify OpenRouter model id and context limits for the requested Gemini 3.1 Flash Lite Preview target.
- Decide safe tester API-key configuration and document it.
- Draft the full prompt contract and expected response shape.
- Update PRD/sprint docs if provider/model constraints require a safer first implementation.
- GPT Pro review of the AI contract and prompt before AI code begins.

Acceptance:

- No AI implementation starts until the prompt and credential path are reviewed.
- The contract explicitly protects ordinary local note saving, privacy, and no-lossy-summary behavior.
- GPT Pro returns `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS` for the AI contract/prompt packet.

### Slice 19.7: OpenRouter Client And Safe Configuration

Deliverables:

- Add an OpenRouter client abstraction with injectable fake implementation for tests.
- Add timeout, cancellation, retry, rate-limit, and error mapping.
- Add safe debug/tester key configuration without committing secrets or bundling them into release artifacts unintentionally.
- Add visible disabled/unconfigured state for `Ask AI`.

Acceptance:

- No key appears in git, Portable Profile export, annotation export, logs, screenshots, or APK metadata.
- Unit tests cover success, timeout, provider error, missing key, cancellation, and oversized material behavior.

### Slice 19.8: Annotation UI And Full-Context Prompt Assembly

Deliverables:

- Add `Ask AI` next to ordinary save in the annotation popup.
- Ensure `Ask AI` saves the note first, then launches the AI request.
- Assemble prompt inputs from the complete source material, selected quote, source anchor/range, and user note.
- Preserve source order, paragraph/block boundaries, and reader anchor metadata.
- Detect context overflow before sending.
- Implement either a visible no-send overflow state or a reviewed non-lossy chunking protocol.

Acceptance:

- User can save a note without AI exactly as before.
- User can tap `Ask AI`, leave with the note saved, and see AI request state.
- Tests prove the prompt includes complete source text for normal-size sources.
- Tests prove the app does not silently truncate or summarize oversized sources.
- Visual evidence covers unconfigured, ready, loading, success, and failure states.

### Slice 19.9: Store, Export, And Sync AI Responses

Deliverables:

- Persist AI response state and response text linked to the annotation.
- Export/sync AI commentary distinctly from user-authored annotation text.
- Preserve local-first behavior and Drive sync safety.
- Exclude AI provider credentials from every portable/export path.

Acceptance:

- Saved AI response reopens with the annotation.
- JSON-LD or sidecar export distinguishes user note and AI commentary.
- Drive sync round-trip/readback proves both fields survive.
- Portable Profile remains secret-free.

### Slice 19.10: Final AI E2E Review And APK

Deliverables:

- Full unit and connected Android validation.
- Emulator visual QA for note save, Ask AI states, response persistence, and Drive sync.
- GPT Pro final release gate with screenshots.
- Version bump, installable APK, signature/install verification, GitHub release, and changelog versus the previous release.

Acceptance:

- Every implemented slice has preserved review/evidence.
- Final GPT Pro gate returns `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS`.
- APK installs successfully and release notes identify the reader fixes, form intervention behavior, AI note feature, privacy behavior, provider configuration, and known model limitations.

## Out Of Scope For Sprint 19

- General chat with the document.
- Open-web retrieval or browsing.
- Automatic AI calls on every note save.
- Server-side account system.
- Bundled production API key.
- Replacing user notes with AI-generated text.
