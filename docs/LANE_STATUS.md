# Lane Status

Status timestamp: 2026-05-31

This file is the repo-level index for active and recently completed execution lanes. It should point to the canonical branch, review lane, validation artifacts, and next gate for each lane.

## Current Rule

- Use this document for cross-lane status.
- Use the branch-specific sprint docs for detailed implementation notes.
- Heartbeats should exist only while waiting for a current GPT Pro lane.
- Do not infer lane status from untracked local files alone. For example, `ios/` can appear untracked on non-iOS branches; the canonical iOS implementation source is the pushed iOS branch listed below.

## Sprint 25 Markdown Media And Tables

Status: `gpt_pro_10_10_pass_release_prep`

- Branch: `codex/sprint25-md-image-embeds`
- Scope: Markdown reader import/rendering fixes for embedded images and Markdown tables.
- Current implementation state:
  - Added `ReaderDocumentImage` and image metadata on reader document blocks.
  - Added `MarkdownReaderDocumentParser` for standalone Markdown images, inline-image alt text, `data:image/...` payloads, relative `file://` image resolution, and picked image attachment URI maps.
  - Added Markdown image attachment URI persistence via `user_documents.imageAttachmentUrisJson` and migration `14 -> 15`.
  - Updated the Android document picker to allow image files and attach picked images to Markdown documents instead of saving image files as separate library items.
  - Updated reader UI to render image blocks from `content://`, `file://`, `android.resource://`, and `data:image/...`; placeholders appear when an image source cannot be opened.
  - Added pipe Markdown table parsing with header/body rows and alignment metadata.
  - Updated reader UI to render Markdown tables as structured rows and columns with header styling, cell alignment, and horizontal scrolling for wide tables.
  - Updated reader pagination, progress snapshots, and reading-time estimates so image payloads and Markdown table delimiter syntax do not distort page fit or time estimates.
  - Updated reader gesture handling so horizontally scrolling wide Markdown tables do not advance or complete reader pages.
  - Narrowed the table gesture guard after GPT Pro R2 so ordinary text taps/swipes still advance reader pages.
  - Updated table pagination measurement so wrapped cell text contributes to page fit and oversized tables split by visual row weight.
- Validation status:
  - Unit/instrumented tests have been added for parser, reading-time estimate, Room migration, document attachment persistence, reader image block pagination, reader table pagination, and Sprint 25 visual evidence.
  - Passed with Homebrew JDK 17: `./gradlew :app:testDebugUnitTest :app:lintDebug`.
  - Passed on emulator `qaApi36(AVD) - 16`: `VisualQaScreenshotTest#captureSprint25MarkdownMediaAndTableScreens`.
  - Passed on emulator `qaApi36(AVD) - 16`: `VisualQaScreenshotTest#captureSprint25WideMarkdownTableHorizontalScrollDoesNotAdvanceReaderPage`.
  - Passed on emulator `qaApi36(AVD) - 16`: `VisualQaScreenshotTest#captureSprint25OrdinaryTextNavigationStillWorksAfterTableGestureGuard`.
  - Passed on emulator `qaApi36(AVD) - 16`: `RoomUserDocumentRepositoryTest`.
  - Passed on emulator `qaApi36(AVD) - 16`: `QualityAlternativeDatabaseMigrationInstrumentedTest`.
  - Visual evidence: `evidence/sprint25_markdown_media_tables/screenshots-r3/contact_sheet_r3.png`.
  - R3 Android results: `evidence/sprint25_markdown_media_tables/android-results-r3/`.
  - Passed: `git diff --check`.
  - GPT Pro R3: `SCORE 10/10`, `VERDICT PASS`, `VISUAL REVIEW PASS`.
- Next gate:
  - Bump Android version, run final tests, build and verify the debug APK, then publish the GitHub release.

## Android Meditation / Content Priority Fix

Status: `complete_10_10_pass`

- Branch: `codex/android-meditation-priority-controls`
- Remote: `origin/codex/android-meditation-priority-controls`
- Latest implementation commit: `b37cdb8` (`Persist replacement history durations`)
- PR draft URL: `https://github.com/dkacz/qualityalternative/pull/new/codex/android-meditation-priority-controls`
- Current GPT Pro follow-up lane: `https://chatgpt.com/c/69eb34ec-0704-8388-b485-e94dc4080e4e`
- Heartbeat automation: `android-meditation-priority-pro-review`
- Current review prompt: `PRO_REVIEW_PROMPT_20260424_110657.md`
- Current review bundle: `QUALITY_ALTERNATIVE_REVIEW_BUNDLE_20260424_110657.zip`
- Final review output: `PRO_REVIEW_OUTPUT_20260424_110657_CLEAN/Android_Meditation_Review.md`
- Final verdict: `10/10 PASS`
- Prior review outputs: `PRO_REVIEW_OUTPUT_20260424_093850/`, `PRO_REVIEW_OUTPUT_20260424_103230/`
- Superseded mixed/accidental lane no longer tracked: `https://chatgpt.com/c/69eb335c-dc40-8391-84c3-383aefd24c0c`
- Visual QA contact sheet: `output/android-meditation-priority-visual-20260424/contact_sheet_meditation_priority.png`

Completed validation:

- `testDebugUnitTest`
- `lintDebug`
- `connectedDebugAndroidTest` on Android emulator, 50/50 passing
- `git diff --check`
- JSON validation for `app/src/main/assets/editorial/starter_packs.json`
- `VisualQaScreenshotTest#captureSprint10ReaderProgressStreakAndMeditationScreens`
- Manual visual review of the contact sheet

Next gate:

- Eligible for merge/release decision.
- Heartbeat `android-meditation-priority-pro-review` should be deleted after this PASS is reported.

## iOS Sprint 11 Discovery

Status: `complete_10_10_pass`

- Branch/source: `main` and `codex/sprint11-ios-discovery`
- Latest relevant commit on main: `bae1778` (`Remove iOS reopen gate overlap`)
- Canonical docs:
  - `docs/IOS_DISCOVERY.md`
  - `docs/SPRINT_11_IOS_DISCOVERY.md`
- Final GPT Pro output: `PRO_REVIEW_OUTPUT_20260423_233635_RETRY/iOS_Sprint_11_Review.md`
- Final verdict: `10/10 PASS`
- Recommendation: run a bounded `build_full_ios_spike`, not a promise of Android/iOS parity.

Important decision:

- iOS should be pursued as a Screen Time / FamilyControls / DeviceActivity / ManagedSettings feasibility spike.
- Exact Android overlay parity is not a public-API target on iOS.
- Android remains the MVP launch platform unless the PRD changes.

## iOS Sprint 12 Implementation Spike

Status: `slice_12_1_implemented_pushed_not_currently_under_review`

- Branch: `codex/sprint12-ios-implementation`
- Remote: `origin/codex/sprint12-ios-implementation`
- Commit: `571aef4` (`Add iOS visual parity skeleton`)
- Canonical branch doc: `docs/SPRINT_12_IOS_IMPLEMENTATION.md` on `codex/sprint12-ios-implementation`
- Visual QA artifacts on that branch: `docs/visual-qa/sprint12-ios-slice12-1/`
- Local reference bundle: `output/ios-android-visual-parity-reference-20260424_083016/IOS_ANDROID_VISUAL_PARITY_REFERENCE_20260424_083016.zip`

Implemented in Slice 12.1:

- Native SwiftUI project under `ios/`
- Visual parity skeleton for home, library, intervention, reader, link handoff, meditation, progress, and settings
- Shared visual language: parchment/dark palette, rounded cards, serif display typography, finite replacement shape
- iOS unit tests and UI screenshot tests
- Simulator visual QA screenshots

Known state:

- This is a visual/flow skeleton, not a complete iOS release implementation.
- Screen Time APIs, app shielding, FamilyControls authorization, DeviceActivity monitoring, real persistence, app selection, and signing/release packaging are not complete.
- The lane is not currently waiting on an active GPT Pro heartbeat.

Next gate:

- Resume this branch in a clean iOS worktree.
- Launch GPT Pro review for Slice 12.1 with the branch doc, source, tests, and visual QA screenshots.
- Iterate until `10/10 PASS`.
- Only then proceed to Slice 12.2.
