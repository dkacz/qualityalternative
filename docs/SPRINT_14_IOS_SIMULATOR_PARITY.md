# Sprint 14 iOS Simulator Parity

Status: `pdf_safearea_review_fixes_implemented_followup_pro_review_pending`
Branch: `codex/sprint14-ios-simulator-parity`

## Goal

Bring the iOS simulator build as close as possible to the Android MVP without claiming behavior that requires a signed physical iPhone. This slice targets content, visual, and local flow parity:

1. Android editorial starter packs are bundled into iOS.
2. The iOS library can show editorial reader items, link-only handoffs, simulator saved links, simulator private files, and the meditation utility replacement.
3. Intervention keeps the Android finite choice shape: one primary, two backups, pause, and intentional continue.
4. Markdown and EPUB-shaped content use the in-app reader; PDF remains an external handoff.
5. Meditation duration has a default in Settings and can be changed immediately before a meditation.
6. Content priority is visible in Settings and changes the replacement mix without growing into a feed.
7. Visual evidence covers the parity surfaces in light and dark mode where simulator-testable.

## Implemented Parity

### Content Catalog

- Imported Android editorial assets from `app/src/main/assets/editorial`.
- Bundled `starter_packs.json` and 25 Markdown body files into the iOS app target.
- iOS decodes the Android-compatible starter pack schema into `QAEditorialPack` and `QAContentItem`.
- iOS keeps Android source/rights metadata in the runtime model: source URL, license name, license URL, and review timestamp.
- Unit coverage proves the bundled catalog loads 5 packs and 45 editorial items.
- Unit coverage proves 25 items are in-app reader items and 20 are link-only external handoffs.
- The resource lookup handles Xcode's flat resource-copy behavior so `editorial/...` JSON paths still resolve in the app bundle.

### Library and Import Surfaces

- Home now shows a library summary with editorial picks, saved links, private files, and total finite minutes.
- Library now has filters for All, Editorial, Your links, and Files.
- Saved-link simulator flow mirrors Android scope without scraping, caching, summarizing, or rehosting third-party content.
- Saved links are locally editable, saved into simulator-local state, and proven to survive app relaunch.
- Private-file simulator flow distinguishes Markdown/EPUB reader support from PDF external handoff.
- Private Markdown, EPUB-shaped, and PDF fixtures are visible in the Library Files filter and direct route screenshots.
- Added dedicated Add Link and Import PDF / MD / EPUB screens for visual and E2E coverage.

### Intervention and Replacement Flow

- Intervention uses the active replacement session instead of fixed sample button copy.
- Primary action is routed from render mode: reader, external handoff, private reader, or meditation timer.
- Backups remain capped at two, use lower-or-equal commitment than the primary, and have stable UI test identifiers.
- Pause and Continue intentionally remain explicit actions; the simulator delay card becomes visible only after a pause.
- Meditation can be prioritized as the primary replacement and still remains finite.

### Reader, Handoff, Meditation, Feedback

- Reader renders Markdown blocks from bundled editorial bodies or private Markdown/EPUB-shaped fixtures.
- External handoff screen differentiates link-only web content from PDF/file handoff copy and exposes an explicit open action.
- Meditation timer uses the configured default duration and exposes duration chips before starting.
- Feedback screen is included as the post-replacement reflection surface.

### Settings

- Added Content priority controls: Balanced, Readings, My files, Saved links, Meditation.
- Added Default session length controls for meditation reset duration.
- Content priority and default meditation duration persist in local simulator state and affect the generated replacement session.
- Kept existing Screen Time, shield, and Device Activity simulator caveats.
- Maintained the iOS-native scope: public APIs only, token-safe state, no private foreground-app detection, and no Android overlay parity promise.

## Pro Review Fixes

The first Sprint 14 Pro lane returned `BLOCK`; the harvested audit is saved at `PRO_REVIEW_OUTPUT_20260424_192655/iOS_Simulator_Parity_Audit.md`.

Concrete fixes implemented before the follow-up review:

- Expanded the review evidence from 15 to 21 screenshots, covering private Markdown, private EPUB, private PDF handoff, Library Files filter, Settings default duration, and the active delay card.
- Made Add Link and Add Document local-state flows instead of static screens.
- Added a real simulator-local settings store for content priority and default meditation length.
- Added explicit external open buttons for link/PDF handoff.
- Preserved Android editorial source/rights metadata in the iOS runtime model.
- Enforced lower-or-equal commitment backups and capped backups at two.
- Added unit tests for rights metadata, backup commitment bounds, saved local content persistence, and settings persistence.
- Regenerated the review bundle with complete iOS source and Android source editorial assets.

The follow-up Sprint 14 Pro lane returned `REVISE`; the harvested audit is saved at `PRO_REVIEW_OUTPUT_20260424_202238/iOS_Simulator_Parity_Audit.md`.

Concrete fixes implemented before the next follow-up review:

- Replaced the placeholder PDF handoff URL with a bundled simulator-local `private-paper.pdf` fixture.
- Added project wiring so the PDF fixture is copied into the iOS app bundle.
- Added unit coverage proving sample and locally-created PDF handoff items resolve to an existing local file URL.
- Updated the Library Files visual test to scroll until the PDF row is hittable before capturing the Files-filter screenshot.
- Added a top safe-area background scrim so scrolled content no longer visibly collides with the iOS status bar.
- Regenerated all Sprint 14 visual QA screenshots and the contact sheet from the passing simulator run.

## Out of Scope

- No physical-device proof of Screen Time authorization, FamilyActivityPicker persistence on real device, ManagedSettings shield display, Shield Action extension invocation, or DeviceActivity callback delivery.
- No `.ipa`, TestFlight, or App Store release in this slice.
- No claim that simulator validation proves real iOS enforcement behavior.
- No browsing feed, infinite content list, scraping, third-party rehosting, or runtime copyright blocking.

## Validation

PDF/safe-area review-fix validation completed on 2026-04-24 with `DEVELOPER_DIR=/Applications/Xcode-26.1.1.app/Contents/Developer`:

- `xcodebuild test`: PASS
- Unit tests: 37 passed, 0 failed
- UI visual QA tests: 1 passed, 0 failed
- Total tests: 38 passed, 0 failed
- Visual screenshots captured: 21
- Result bundle: `output/ios_sprint14_parity_pdf_safearea_validation_20260424_205113/QualityAlternative.xcresult`
- Test log: `output/ios_sprint14_parity_pdf_safearea_validation_20260424_205113/xcodebuild_test.log`
- Test summary: `output/ios_sprint14_parity_pdf_safearea_validation_20260424_205113/test_summary.json`
- Screenshot attachments: `output/ios_sprint14_parity_pdf_safearea_validation_20260424_205113/attachments/manifest.json`

Visual QA artifacts:

- Contact sheet: `docs/visual-qa/sprint14-ios-simulator-parity/contact_sheet.png`
- Light screenshots: home, library, intervention, reader, handoff, meditation, progress, settings, add link, add document, feedback, Device Activity settings, private Markdown reader, private EPUB reader, private PDF handoff, Library Files filter, Settings default session length, active delay.
- Dark screenshots: intervention, reader, meditation.

## Review Target

GPT Pro should review whether this branch reaches full simulator-testable parity with Android content, visual language, and local flow behavior while preserving iOS platform constraints.

The expected verdict bar is strict:

- `10/10 PASS` only if there are no actionable simulator-testable parity issues.
- `REVISE` if there is any missing content, visual, state, scope, or package evidence that can be fixed without a physical iPhone.
