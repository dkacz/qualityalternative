# Quality Alternative v0.8.0-kindle-drive-annotations-alpha

Internal Android alpha for GitHub testers.

This release ships the GPT Pro-reviewed Sprint 15 reader, EPUB table-of-contents, W3C annotation, Google Drive sync, and final reader-minimalism work on top of `v0.7.0-ranking-annotations-alpha`.

## What changed since v0.7.0-ranking-annotations-alpha

The previous release introduced fresh-file ranking, direct reader mode, basic pagination, paragraph-tied annotations, an annotation library, and local Markdown autosave.

This release turns that into a proper Kindle-style reader and portable annotation workflow:

- Reader pages no longer vertically scroll. The active page is fixed, and tapping the page advances to the next page.
- Android/system Back now moves to the previous reader page before leaving the reader.
- Persistent reader buttons such as `Previous`, `Next page`, and `I'm done reading` were removed from the active reading surface.
- Reader chrome is reduced to a small footer with title, progress, page count, percent, and TOC access.
- EPUB imports now retain structured reader blocks and table-of-contents entries.
- EPUB TOC navigation opens in a bounded sheet and jumps to the closest page/section.
- Long EPUBs and long single paragraphs are split into reachable pages instead of creating laggy or clipped scroll surfaces.
- Reader annotations are created from long-press text selection, not margin icons.
- Annotation selection starts from the sentence under the press and can be expanded or contracted before saving.
- The annotation editor is an overlay; it does not resize the page or reintroduce reader scrolling.
- Saved annotations highlight the selected text fragment and can be reopened from the annotation library.
- Annotation records now preserve source title, source label/type/format, exact quote, text offsets, prefix/suffix context, source block index, and EPUB href/anchor locator data when available.
- Annotation export now uses W3C Web Annotation JSON-LD.
- Local annotation export writes one `.annotations.jsonld` file per annotated source plus `quality-alternative-annotations.index.json`.
- File names include sanitized source titles and stable content identity.
- Android document-provider autosave now uses a folder destination for per-source JSON-LD files instead of a single Markdown file.
- Legacy single-document export refuses multi-source annotation export rather than writing a misleading index-only file.
- Missing-source annotation rows preserve and display the stored source title.
- Google Drive sync is real Drive authorization through Google Identity, using the narrow `drive.file` scope only when the user connects Drive sync.
- Drive sync creates/reuses a `Quality Alternative annotations` folder and upserts one JSON-LD file per source plus an index.
- Drive failures are recoverable in Settings and do not block local annotation save/update/delete.
- Disconnect revokes Drive access and clears local Drive sync state.
- Recommendation ranking no longer uses whole-document duration as a score/filter, so long materials remain eligible as manageable reading segments.
- Imported Markdown/EPUB/PDF reading time is computed automatically; users are no longer asked to store manual document duration.
- Final UX cleanup removes explanatory clutter from intervention cards, annotation editor, export settings, Drive settings, and the reader.

## Validation

- GPT Pro final release gate R3: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`, blockers `None`.
- Full release validation after version bump: `./gradlew testDebugUnitTest connectedDebugAndroidTest` passed with 86/86 connected Android tests.
- Debug APK build passed.
- APK signature verification passed with Android Debug certificate.
- Emulator install smoke passed and reported `versionCode=12`, `versionName=0.8.0-alpha`.

## APK Assets

- Installable alpha APK: `quality-alternative-v0.8.0-kindle-drive-annotations-alpha-debug.apk`
- APK versionCode: 12
- APK versionName: `0.8.0-alpha`
- SHA-256: `39aab59a81d2a25f04995d27ab4bf8ecc742dc0479e2ffe19e4fd22cdf2619fb`

## Evidence

- Final GPT Pro audit: `PRO_REVIEW_OUTPUT_SPRINT15_FINAL_RELEASE_GATE_R3_20260503_2043/Sprint15_Final_Release_Gate_R3_GPT_Pro.md`
- Sprint tracker: `docs/SPRINT_15_KINDLE_READER_DRIVE_ANNOTATIONS.md`
- Final visual contact sheet: `docs/visual-qa/2026-05-03-sprint15-final-release-r2/contact_sheet.png`
- Release validation summary: `docs/release-gate-logs/2026-05-03-sprint15-release/VALIDATION_SUMMARY.md`
