# Sprint 14 iOS Simulator Parity

Status: `implemented_simulator_validated_pro_review_pending`
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
- Unit coverage proves the bundled catalog loads 5 packs and 45 editorial items.
- Unit coverage proves 25 items are in-app reader items and 20 are link-only external handoffs.
- The resource lookup handles Xcode's flat resource-copy behavior so `editorial/...` JSON paths still resolve in the app bundle.

### Library and Import Surfaces

- Home now shows a library summary with editorial picks, saved links, private files, and total finite minutes.
- Library now has filters for All, Editorial, Your links, and Files.
- Saved-link simulator flow mirrors Android scope without scraping, caching, summarizing, or rehosting third-party content.
- Private-file simulator flow distinguishes Markdown/EPUB reader support from PDF external handoff.
- Added dedicated Add Link and Import PDF / MD / EPUB screens for visual and E2E coverage.

### Intervention and Replacement Flow

- Intervention uses the active replacement session instead of fixed sample button copy.
- Primary action is routed from render mode: reader, external handoff, private reader, or meditation timer.
- Backups remain capped at two and now have stable UI test identifiers.
- Pause and Continue intentionally remain explicit actions.
- Meditation can be prioritized as the primary replacement and still remains finite.

### Reader, Handoff, Meditation, Feedback

- Reader renders Markdown blocks from bundled editorial bodies or private Markdown/EPUB-shaped fixtures.
- External handoff screen differentiates link-only web content from PDF/file handoff copy.
- Meditation timer uses the configured default duration and exposes duration chips before starting.
- Feedback screen is included as the post-replacement reflection surface.

### Settings

- Added Content priority controls: Balanced, Readings, My files, Saved links, Meditation.
- Added Default session length controls for meditation reset duration.
- Kept existing Screen Time, shield, and Device Activity simulator caveats.
- Maintained the iOS-native scope: public APIs only, token-safe state, no private foreground-app detection, and no Android overlay parity promise.

## Out of Scope

- No physical-device proof of Screen Time authorization, FamilyActivityPicker persistence on real device, ManagedSettings shield display, Shield Action extension invocation, or DeviceActivity callback delivery.
- No `.ipa`, TestFlight, or App Store release in this slice.
- No claim that simulator validation proves real iOS enforcement behavior.
- No browsing feed, infinite content list, scraping, third-party rehosting, or runtime copyright blocking.

## Validation

Completed on 2026-04-24 with `DEVELOPER_DIR=/Applications/Xcode-26.1.1.app/Contents/Developer`:

- `xcodebuild test`: PASS
- Unit tests: 32 passed, 0 failed
- UI visual QA tests: 1 passed, 0 failed
- Visual screenshots captured: 15
- Result bundle: `output/ios_sprint14_parity_validation_20260424_195500/DerivedData/Logs/Test/Test-QualityAlternative-2026.04.24_19-22-52-+0200.xcresult`
- Test summary: `output/ios_sprint14_parity_validation_20260424_195500/test_summary.json`
- Test result details: `output/ios_sprint14_parity_validation_20260424_195500/test_results.json`
- Screenshot attachments: `output/ios_sprint14_parity_validation_20260424_195500/attachments/manifest.json`

Visual QA artifacts:

- Contact sheet: `docs/visual-qa/sprint14-ios-simulator-parity/contact_sheet.png`
- Light screenshots: home, library, intervention, reader, handoff, meditation, progress, settings, add link, add document, feedback, Device Activity settings.
- Dark screenshots: intervention, reader, meditation.

## Review Target

GPT Pro should review whether this branch reaches full simulator-testable parity with Android content, visual language, and local flow behavior while preserving iOS platform constraints.

The expected verdict bar is strict:

- `10/10 PASS` only if there are no actionable simulator-testable parity issues.
- `REVISE` if there is any missing content, visual, state, scope, or package evidence that can be fixed without a physical iPhone.
