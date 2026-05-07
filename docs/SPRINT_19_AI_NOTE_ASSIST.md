# Sprint 19 - AI-Assisted Annotation Notes

Status: `planned_not_started`

Requested on: 2026-05-07

## Goal

Add an explicit `Ask AI` action to the reader annotation flow. The action saves the user's note, then asks an external LLM for a context-aware response using the full source material, the annotation's selected quote, its source anchor/range, and the user's note.

Mapped PRD items: FR8, FR8A, FR13, NFR privacy, NFR reliability, NFR calm interaction model.

## User Story

When adding a note to selected reader text, the user can choose ordinary `Save` or `Ask AI`. `Ask AI` must save the note first and then request an AI response that understands the annotation in the context of the complete material, not a summary.

## Requested Model And Provider

- Target provider path: OpenRouter API.
- Requested target model: Google Gemini 3.1 Flash Lite Preview.
- Implementation must verify the current OpenRouter model id, context window, pricing, rate limits, and availability at Sprint 19 implementation time. The exact model name is treated as a requested target, not a hard-coded assumption until verified.

## Product Rules

- `Ask AI` is optional and explicit. Ordinary annotation saving must keep working offline and without an AI key.
- No API key, OpenRouter credential, Google credential, account email, raw Drive file id, or model-provider secret may be exported through Portable Profile or annotation sync.
- Do not ship a private OpenRouter API key inside the APK. Sprint 19 must choose a safe tester configuration path before implementation, such as developer local properties for debug builds, a user-provided key, or a later backend/token broker. A bundled secret in source or APK is not acceptable.
- The app must clearly tell the user that `Ask AI` sends the full source material and annotation context to an external AI provider.
- The AI request must include the full source material without lossy summarization. If the material is too large for the configured model, the app must show a clear failure or use a non-lossy chunking protocol whose final answer is grounded in every chunk. It must not silently send a shortened summary.
- The saved user note is canonical user-authored data. The AI answer is generated commentary linked to the annotation and must be stored/exported as a distinct field or sidecar record.
- AI failure must be non-destructive: the note remains saved, the user can retry, and local/Drive annotation sync remains safe.
- The active reader must remain calm and finite. `Ask AI` must not turn the reader into chat, browsing, or an infinite assistant feed.

## Prompt Design Requirements

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

## Data Model Questions To Resolve

- Where to store AI responses: embedded annotation extension, separate AI response table, or sidecar JSON-LD body.
- How to represent AI output in W3C Web Annotation JSON-LD without confusing it with the user's note.
- Whether multiple AI attempts per annotation are retained, replaced, or versioned.
- Whether Drive sync should upload AI responses immediately with the annotation or only after explicit local save.
- How to expose retry, cancel, and failed states without crowding the annotation popup.

## Slice Plan

### Slice 19.0: Contract, Model Verification, And Prompt Review

Deliverables:

- Verify OpenRouter model id and context limits for the requested Gemini 3.1 Flash Lite Preview target.
- Decide safe tester API-key configuration and document it.
- Draft the full prompt contract and expected response shape.
- Update PRD/sprint docs if the provider/model constraints require a safer first implementation.
- GPT Pro review of the contract and prompt before code begins.

Acceptance:

- No implementation starts until the prompt and credential path are reviewed.
- The contract explicitly protects ordinary local note saving, privacy, and no-lossy-summary behavior.
- GPT Pro returns `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS` for the contract/prompt packet.

### Slice 19.1: OpenRouter Client And Safe Configuration

Deliverables:

- Add an OpenRouter client abstraction with injectable fake implementation for tests.
- Add timeout, cancellation, retry, rate-limit, and error mapping.
- Add safe debug/tester key configuration without committing secrets or bundling them into release artifacts unintentionally.
- Add visible disabled/unconfigured state for `Ask AI`.

Acceptance:

- No key appears in git, Portable Profile export, annotation export, logs, screenshots, or APK metadata.
- Unit tests cover success, timeout, provider error, missing key, cancellation, and oversized material behavior.

### Slice 19.2: Annotation UI Integration

Deliverables:

- Add `Ask AI` next to ordinary save in the annotation popup.
- Ensure `Ask AI` saves the note first, then launches the AI request.
- Show calm progress, success, failure, and retry states without expanding the reader page or breaking pagination.
- Keep ordinary `Save` visually clear and not downgraded.

Acceptance:

- User can save a note without AI exactly as before.
- User can tap `Ask AI`, leave with the note saved, and see AI request state.
- Failure never loses the note.
- Visual evidence covers unconfigured, ready, loading, success, and failure states.

### Slice 19.3: Full-Context Prompt Assembly

Deliverables:

- Assemble prompt inputs from the complete source material, selected quote, source anchor/range, and user note.
- Preserve source order, paragraph/block boundaries, and reader anchor metadata.
- Detect context overflow before sending.
- Implement either a visible no-send overflow state or a reviewed non-lossy chunking protocol.

Acceptance:

- Tests prove the prompt includes the complete source text for normal-size sources.
- Tests prove annotation quote, note, and source offsets are included.
- Tests prove the app does not silently truncate or summarize oversized sources.

### Slice 19.4: Store, Export, And Sync AI Responses

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

### Slice 19.5: Final E2E Review And APK

Deliverables:

- Full unit and connected Android validation.
- Emulator visual QA for note save, Ask AI states, response persistence, and Drive sync.
- GPT Pro final release gate with screenshots.
- Version bump, installable APK, signature/install verification, GitHub release, and changelog versus the previous release.

Acceptance:

- Every implemented slice has preserved review/evidence.
- Final GPT Pro gate returns `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS`.
- APK installs successfully and release notes identify the AI note feature, privacy behavior, provider configuration, and known model limitations.

## Out Of Scope For Sprint 19

- General chat with the document.
- Open-web retrieval or browsing.
- Automatic AI calls on every note save.
- Server-side account system.
- Bundled production API key.
- Replacing user notes with AI-generated text.
