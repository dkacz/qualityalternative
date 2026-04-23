# Sprint 10: EPUB Reader, Reading Progress, Streak, Meditation Controls

## Goal

Improve the replacement experience for longer private documents and utility replacements without expanding into a general document manager or feed.

## PRD Mapping

- `FR4 Content Item Model`: EPUB user documents remain rankable private `ContentItem` records with source, format, duration, topics, availability, and private-reader render metadata.
- `FR8 Replacement Session Experience`: Markdown and EPUB can render in the in-app reader; PDF remains Android document-viewer handoff.
- `FR12 Analytics Instrumentation`: reading, meditation completion, and handoff events continue using the existing local analytics ledger.

## Implemented Scope

- EPUB private reader v1:
  - EPUB imports now use `USER_PRIVATE_READER` render mode.
  - EPUB body extraction reads the package document, manifest, spine, and XHTML/HTML chapter files from the local EPUB ZIP.
  - Extracted text is displayed in the existing finite in-app reader.
  - DRM, rich CSS fidelity, images, annotations, highlights, search, and document management remain out of scope.
- Reader progress:
  - The reader progress bar is now based on visible document position, not elapsed session time.
  - This is especially important for long Markdown/EPUB documents where estimated duration is only a rough planning signal.
- Progress/streak:
  - Progress now includes a current reading streak based on consecutive days with completed replacement sessions.
  - The streak is constructive and non-punitive: missed days are not framed as failure.
- Meditation controls:
  - Settings now allow meditation reset length selection: 1, 3, 5, or 10 minutes.
  - The meditation replacement uses the selected duration in recommendation inventory and the timer UI.
  - A short completion gong plays when the timer reaches zero, with safe fallback if audio output is unavailable.

## Explicit Non-Goals

- No native PDF renderer.
- No cloud sync, folders, annotations, highlights, or full document management.
- No EPUB DRM bypass.
- No feed, carousel, or larger recommendation browsing surface.
- No hidden runtime copyright blocking.

## Acceptance Criteria

- EPUB user documents import as private in-app reader content.
- Unsupported or unreadable EPUB files fail gracefully and are removed from future recommendations when needed.
- Reader progress changes as the user scrolls through text.
- Progress screen shows current reading streak and completed read count.
- Meditation duration persists across app restart.
- Meditation timer displays the selected duration and plays a completion gong.
- Unit, lint, and emulator instrumentation validation pass.
