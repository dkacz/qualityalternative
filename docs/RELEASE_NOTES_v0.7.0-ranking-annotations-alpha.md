# Quality Alternative v0.7.0-ranking-annotations-alpha

Internal Android alpha for GitHub testers.

This release ships the GPT Pro-reviewed Sprint 14 ranking, reader, annotation, autosave, pagination, and UX hardening work on top of `v0.6.3-meditation-always-option-alpha`.

## What changed since v0.6.3-meditation-always-option-alpha

The previous release kept meditation as an always-available intervention option, outside the Library, and preserved completed-content/unlock behavior.

This release adds the Sprint 14 reading workflow:

- Freshly added EPUB/MD files now win over older deprioritized files when they are the best eligible replacement.
- Unfinished reading keeps absolute priority and preserves progress, so interrupted sessions continue where they left off.
- The app can be used as a direct reader from Home or Library without triggering an intervention.
- Reader content is paginated by default, including long EPUBs, instead of rendering one laggy continuous scroll.
- Page navigation preserves reading progress and avoids carrying scroll offset from one page to another.
- The active reader now supports annotations anchored to a concrete paragraph/text fragment.
- Saved annotations can be edited, previewed in the reader, and reopened later at the exact source fragment.
- A new annotation library lists all notes with source title, source type, quoted fragment, note text, and missing-source handling.
- Deleting user links/files cleans up their annotations.
- Annotation autosave can write a Markdown library to a user-selected Android document-provider file, including Drive-backed files.
- Autosave settings now show clear success/failure states: `Save now` for a healthy export target and `Retry` only after failure.
- Recommendation behavior still keeps meditation out of the Library while leaving it available in interventions.
- Completed non-meditation content stays out of recommendations until manually reactivated from Library.
- Final UX cleanup fixed release-blocking copy and visual issues: no `1 apps`, no raw provider errors, no misleading `Retry` on successful autosave, and no terminal pagination arrow/action mismatch.

## Validation

- Slice-by-slice GPT Pro review passed through Sprint 14, including visual review after each slice.
- Final GPT Pro release gate: `SCORE: 10/10`, `VERDICT: PASS`, `VISUAL REVIEW: PASS`, blockers `None`.
- Full local regression after final UX fixes: `./gradlew testDebugUnitTest connectedDebugAndroidTest` passed with 78/78 connected Android tests.
- Final release validation after version bump: unit tests, connected Android tests, debug APK build, install smoke, and APK signature verification passed.

## APK Assets

- Installable alpha APK: `quality-alternative-v0.7.0-ranking-annotations-alpha-debug.apk`
- APK versionCode: 11
- APK versionName: `0.7.0-alpha`
- SHA-256: `851ab3f290df17e3563e090b84dae75209d4b3f3f2a0145c3db493dda5bf5ed8`

## Evidence

- Final GPT Pro audit: `PRO_REVIEW_OUTPUT_SPRINT14_FINAL_RELEASE_GATE_20260503_024223/Sprint14_Final_Release_Gate_GPT_Pro.md`
- Sprint tracker: `docs/SPRINT_14_RANKING_AND_ANNOTATIONS.md`
- Final visual contact sheet: `docs/visual-qa/2026-05-03-sprint14-ux-review-r3-full/contact_sheet.png`
