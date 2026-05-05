# Sprint 17: Reader Settings, Sync, And Annotation Polish

## Goal

Close the post-Sprint-16 usability gaps reported on a real phone before the next APK release. This sprint keeps the Portable Profile direction, but it must make Settings clearer, local defaults useful out of the box, Google Drive authorization reliable, pagination better fitted to the actual screen, and annotation editing genuinely source-anchored across pages.

Mapped PRD items: FR8, FR8A, FR13, NFR privacy, NFR reliability, NFR accessibility.

## User-Reported Problems

- Settings exposes `Reader text` as an oversized four-choice surface instead of a precise control.
- The user needs an in-app reader text size control and may also need a separate interface text or interface size control.
- Annotation autosave and Portable Profile are visually and conceptually too similar.
- Portable Profile `Choose folder` starts with no useful default; annotations and profile backup should both work locally before any external destination is selected.
- Google Drive connection fails with `Google Drive authorization was canceled` even though Drive works in other apps.
- Reader pagination still leaves too much unused bottom space on a real phone.
- Annotation range adjustment feels page-limited: the end cannot move forward past an apparent hard cutoff, and cross-page selection is not working.
- Start/end range controls still occupy too much space.
- The annotation note surface does not scale well when the selected anchor/quote is long; it should grow to the available screen and then scroll internally.

## Product Rules

- Local-first behavior remains mandatory. Google Drive and user-selected folders are optional destinations, not prerequisites for annotation or profile backup.
- Google Drive must actually work when the device supports it and the user has a valid signed-in Google account. A slice that only improves the error message or explains why Drive failed is not acceptable.
- Reader text size and interface size are distinct concepts. The reader can be tuned without unexpectedly resizing the Settings UI.
- Active reading remains paginated and content-first. No persistent Previous, Next, or Done buttons return to the reader.
- Annotation selection is source-anchored, not page-bound. Pagination may change how text is displayed, but it must not corrupt or truncate the selected range.
- Settings copy should use professional labels such as `Portable Profile`, `Profile backup`, `Annotation sync`, and `Reading text`; avoid internal labels like `Account Light`.
- Review evidence must include visual proof for each UI slice, not only unit or instrumentation logs.

## Slice 17.0: Contract And Sprint Plan

Deliverables:

- Update PRD acceptance criteria for numeric reader text controls, optional interface size, default local destinations, Drive auth failure handling, cross-page selection, and annotation note sizing.
- Add this sprint plan with slice order, scope boundaries, and review gates.
- GPT Pro review of the contract before implementation proceeds.

Acceptance:

- The plan explicitly covers every user-reported problem.
- The plan preserves local-first/no-backend constraints.
- The plan requires per-slice GPT Pro code and visual review with `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS`.

## Slice 17.1: Settings Typography Controls

Deliverables:

- Replace the coarse `Reader text` choice surface with compact numeric plus/minus controls.
- Show a live preview using the effective reader text size.
- Add a separate interface text or interface size setting when technically feasible in the current Compose architecture.
- Persist reader and interface sizing distinctly, export/import safe sizing fields through Portable Profile, and preserve reasonable defaults.

Acceptance:

- Settings no longer opens a large four-option `Reader text` window.
- Plus/minus controls update the preview immediately.
- Reader typography changes affect reader pages; interface size changes affect app chrome only where supported.
- Tests cover persistence, profile export/import, and bounds clamping.
- Visual evidence covers default, smaller, and larger reader text plus at least one interface-size state if shipped.

## Slice 17.2: Defaults And Settings Information Architecture

Deliverables:

- Provide a working app-local default for annotation autosave.
- Provide a working app-local default for Portable Profile backup.
- Rework Settings labels and status copy so annotation sync and profile backup are clearly different.
- Treat `Choose folder` as `Change destination`, not the first step required for backup to work.

Acceptance:

- A fresh install can save annotations and create a profile backup without selecting a folder.
- Settings shows where each local default is stored in user-meaningful language, without raw provider URIs.
- User-selected local destinations still work and remain explicit.
- Tests cover fresh default state, manual save, changed destination, and failure visibility.
- Visual evidence covers annotation default, profile default, and changed-destination states.

## Slice 17.3: Google Drive Authorization Repair

Deliverables:

- Reproduce and diagnose the failed Drive connection path.
- Fix the authorization flow so a valid Google Drive account can connect from Settings and remain connected.
- Verify that the connected state can perform a real annotation sync write through the existing Drive annotation sync path.
- Distinguish true user cancellation from technical/configuration failure when possible.
- Keep local annotations safe when auth or Drive API calls fail.

Acceptance:

- Drive connection no longer fails immediately with a misleading cancellation status in the normal signed-in flow.
- On a supported emulator or real Android device with a valid signed-in Google account, tapping Connect reaches a connected state and manual sync writes the expected Quality Alternative annotation files or folder.
- Settings offers a clear retry path and preserves disconnect/revoke behavior.
- API errors, missing Play Services, and cancellation states are visible but non-destructive.
- Tests cover token-provider success, cancellation, recoverable failure, and existing annotation sync retry behavior.
- Visual evidence covers disconnected, connecting/failure, connected, and retry states.

## Slice 17.4: Adaptive Reader Pagination Fit

Deliverables:

- Revisit reader page measurement so pagination uses the actual available viewport, current reader text size, safe insets, and reader chrome dimensions.
- Reduce avoidable blank bottom space while still preventing clipping.
- Keep tap-to-next, swipe, left-edge previous, TOC jumps, reading progress restore, and Android Back-to-exit behavior.

Acceptance:

- Representative phone viewports fill the reader page without large avoidable blank regions.
- Small/default/large reader text sizes produce stable pages without clipping.
- Pagination changes do not break annotation anchors or reading progress.
- Tests cover viewport and font-size combinations.
- Visual evidence includes at least small phone, tall phone, and large-font reader cases.

## Slice 17.5: Cross-Page Annotation Selection And Compact Controls

Deliverables:

- Make start/end adjustment source-based so it can cross page boundaries.
- Replace verbose start/end text buttons with compact icon-first controls with accessible labels.
- Ensure range expansion/contraction works forward and backward from the current quote.
- Keep reader navigation and annotation adjustment gestures/actions from conflicting.

Acceptance:

- End can move forward beyond the current display page when source text continues.
- Start can move backward beyond the current display page when source text exists before it.
- The saved quote can span paginated pages and reopen with the same source-anchored range.
- Controls are compact and do not dominate the overlay.
- Tests cover same-page and cross-page expansion, contraction, save, reopen, and page navigation during selection.
- Visual evidence covers a cross-page selected quote with compact controls.

## Slice 17.6: Annotation Note Surface Sizing

Deliverables:

- Let the annotation surface grow to the available screen when the selected quote/anchor is long.
- Once the available screen is filled, make quote and note content scroll inside the overlay rather than pushing or scrolling the reader page.
- Preserve keyboard behavior, save/cancel actions, and quote visibility.

Acceptance:

- Long selected quotes no longer crush the note editor or push controls off screen.
- The overlay stays inside the viewport and the underlying reader page does not repaginate.
- Tests cover short quote, long quote, long note, and keyboard-visible states where feasible.
- Visual evidence covers long-quote and long-note states.

## Slice 17.7: Final Release Gate And APK

Deliverables:

- Final UX pass across Settings, Drive sync, reader pagination, annotation selection, annotation note editing, Portable Profile import/export, and local defaults.
- Full unit and connected Android validation.
- GPT Pro final release gate with code and visual review.
- Version bump, installable debug APK, signature/install verification, GitHub release, and changelog versus `v0.9.0-portable-profile-alpha`.

Acceptance:

- Every Sprint 17 slice has a preserved GPT Pro review artifact with `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS`.
- Final release gate also returns `SCORE: 10/10`, `VERDICT: PASS`, and `VISUAL REVIEW: PASS`.
- APK installs successfully on emulator/device and release notes identify the new Settings, Drive, pagination, and annotation fixes.
