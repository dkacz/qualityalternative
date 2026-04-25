# Android Product Polish - April 25, 2026

Status: implemented and locally validated on `codex/android-product-polish-favorites-selection`.

## Scope

- Replaced the Android default launcher icon with a Quality Alternative adaptive icon.
- Bumped the Android build to `0.4.3-alpha` and changed the Settings footer to read from `BuildConfig.VERSION_NAME`.
- Added individual Library priority picks so a user can mark one concrete item as more likely to appear in finite interventions.
- Kept the existing type-level content priority control, but added a visible count of individual priority picks.
- Reworked Settings app selection from ambiguous pills into checked rows for apps that trigger replacement prompts.

## Guardrails

- The feature remains a soft-intervention replacement flow, not hard blocking.
- Priority picks only boost finite recommendation ranking; they do not create a feed or infinite browsing surface.
- App selection remains within the MVP supported distracting-app catalog.
- No runtime copyright blocking, scraping, or reader-mode expansion was added.

## Validation

- `git diff --check`: pass
- `starter_packs.json` JSON validation: pass
- `testDebugUnitTest`: pass
- `lintDebug`: pass
- `connectedDebugAndroidTest` on `emulator-5554`: pass
- Targeted `VisualQaScreenshotTest` instrumentation: pass

## Visual QA

Fresh screenshots and contact sheet:

- `docs/visual-qa/2026-04-25-android-product-polish/contact_sheet.png`
